package elfak.mosis.capturetheflag.model

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.data.User
import kotlinx.coroutines.selects.select
import java.io.ByteArrayOutputStream

class UserViewModel : ViewModel() {
    var selectedUser: User? = null

    private val _image = MutableLiveData<Bitmap>()
    var image: LiveData<Bitmap> = _image

    private val _authState by lazy { MutableLiveData<AuthState>(AuthState.Idle) }
    val authState: LiveData<AuthState> = _authState
    private val _uploadState by lazy { MutableLiveData<StoreUploadState>(StoreUploadState.Idle) }
    val uploadState: LiveData<StoreUploadState> = _uploadState

    private var auth: FirebaseAuth = Firebase.auth
    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")
    private val storage = Firebase.storage("gs://capturetheflag-56f1c.appspot.com")
    private val storageRef = storage.reference

    fun signupUser(user: User, password: String) {
        if (validateSignup(user, password)) {
            dbRef.child("users").addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(user.phoneNum)) {
                        _authState.value = AuthState.AuthError("This phone is already registered.")
                    } else {
                        auth.createUserWithEmailAndPassword("${user.username}@capturetheflag.mosis", password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    user.uid = auth.currentUser!!.uid
                                    addUserToDB(user)
                                    Log.i("AUTH", "Signup Successful: ${user.username}, phone ${user.phoneNum}")
                                    selectedUser = user
                                    _authState.value = AuthState.Success
                                } else {
                                    task.exception?.let {
                                        Log.e("AUTH", "Signup Error: ${it.message}")
                                        _authState.value = AuthState.AuthError(it.message)
                                    }
                                }
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    _authState.value = AuthState.AuthError(error.message)
                }
            })
        }
    }

    fun loginUser(username: String, password: String) {
        if (validateLogin(username, password)) {
            auth.signInWithEmailAndPassword("${username}@capturetheflag.mosis", password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dbRef.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                _authState.value = AuthState.AuthError(error.message)
                                Log.e("AUTH", error.message)
                            }
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val uid = auth.currentUser!!.uid
                                if (snapshot.hasChild(uid)) {
                                    val currentUser = snapshot.child(uid).getValue(User::class.java)
                                    currentUser?.uid = uid
                                    selectedUser = currentUser
                                    Log.i("AUTH", "Login Successful: ${currentUser!!.username}, ${currentUser.uid}")
                                    _authState.value = AuthState.Success
                                } else {
                                    _authState.value = AuthState.AuthError("Login Error: user does not exist in database.")
                                }
                            }
                        })
                    } else {
                        task.exception?.let {
                            Log.i("AUTH", "Login Error: ${it.message}")
                            _authState.value = AuthState.AuthError(it.message)
                        }
                    }
                }
        }
    }

    fun updateUserData(parameterName: String, parameterValue: Any) : Boolean {
        val key = dbRef.child("users").push().key
        if (key == null) {
            Log.w(ContentValues.TAG, "Couldn't get push key for posts")
        }
        else {
            val id: String = selectedUser!!.uid
            dbRef.child("users").child(id).child(parameterName).setValue(parameterValue)
            dbRef.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e("TAG", error.message)
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(id)) {
                        val currentUser = snapshot.child(id).getValue(User::class.java)
                        currentUser?.uid = id
                        selectedUser = currentUser
                    } else {
                        Log.e("TAG", "Error")
                    }
                }
            })
            return true
        }
        return false
    }

    fun setImage(image: Bitmap) {
        _image.value = image
    }

    fun uploadProfilePhoto() {
        val photoRef = storageRef.child("profilePictures").child("${selectedUser!!.uid}.jpg")
        val baos = ByteArrayOutputStream()
        val bitmap = image.value
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = photoRef.putBytes(data)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception.let {
                    _uploadState.value = StoreUploadState.UploadError("Upload error: ${it?.message}")
                }
            }
            photoRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val photoUrl = task.result.toString()
                updateUserData("imgUrl", photoUrl)
                _uploadState.value =
                    StoreUploadState.Success("Upload successful with image URL: $photoUrl")
            }
        }
    }

    private fun validateLogin(username: String, password: String): Boolean {
        if (username.isBlank()) {
            _authState.value = AuthState.AuthError("Username blank or empty.")
            return false
        }
        if (password.isBlank()) {
            _authState.value = AuthState.AuthError("Password blank or empty.")
            return false
        }
        return true
    }

    private fun validateSignup(user: User, password: String): Boolean {
        if (user.phoneNum.isBlank() || !user.phoneNum.all { char -> char.isDigit()}) {
            _authState.value = AuthState.AuthError("Phone number invalid.")
            return false
        }
        if (user.username!!.isBlank()) {
            _authState.value = AuthState.AuthError("Username blank or empty.")
            return false
        }
        if (password.isBlank()) {
            _authState.value = AuthState.AuthError("Password blank or empty.")
            return false
        }
        if (user.firstName!!.isBlank()) {
            _authState.value = AuthState.AuthError("First name blank or empty.")
            return false
        }
        if (user.lastName!!.isBlank()) {
            _authState.value = AuthState.AuthError("Last name blank or empty.")
            return false
        }
        return true
    }

    private fun addUserToDB(user: User) {
        dbRef.child("users").child(user.uid).child("firstName").setValue(user.firstName)
        dbRef.child("users").child(user.uid).child("lastName").setValue(user.lastName)
        dbRef.child("users").child(user.uid).child("username").setValue(user.username)
        dbRef.child("users").child(user.uid).child("phoneNum").setValue(user.phoneNum)
        dbRef.child("users").child(user.uid).child("desc").setValue("")
        dbRef.child("users").child(user.uid).child("imgUrl").setValue("")
    }
}

sealed class AuthState {
    object Idle : AuthState()
/*    object Loading : AuthState()*/
    object Success : AuthState()
    class AuthError(val message: String? = null) : AuthState()
}

sealed class StoreUploadState {
    object Idle : StoreUploadState()
    class Success(val message: String = "Upload Successful.") : StoreUploadState()
    class UploadError(val message: String? = null) : StoreUploadState()
}