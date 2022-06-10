package elfak.mosis.capturetheflag.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.data.User

class UserViewModel : ViewModel() {
    var selectedUser: User? = null

    private var auth: FirebaseAuth = Firebase.auth
    private val _authState by lazy { MutableLiveData<AuthState>(AuthState.Idle) }
    val authState: LiveData<AuthState> = _authState

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl("https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

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
                                    addUserToDB(user)
                                    Log.i("AUTH", "Signup Successful: ${user.username}, phone ${user.phoneNum}")
                                    selectedUser = user
                                    _authState.value = AuthState.Success
                                } else {
                                    task.exception?.let {
                                        Log.i("AUTH", "Signup Error: ${it.message}")
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
        dbRef.child("users").child(user.phoneNum).child("firstName").setValue(user.firstName)
        dbRef.child("users").child(user.phoneNum).child("lastName").setValue(user.lastName)
        dbRef.child("users").child(user.phoneNum).child("username").setValue(user.username)
        dbRef.child("users").child(user.phoneNum).child("desc").setValue("")
        dbRef.child("users").child(user.phoneNum).child("imgUrl").setValue("")
    }

    //TODO: dodati firebase auth
}

sealed class AuthState {
    object Idle : AuthState()
/*    object Loading : AuthState()*/
    object Success : AuthState()
    class AuthError(val message: String? = null) : AuthState()
}