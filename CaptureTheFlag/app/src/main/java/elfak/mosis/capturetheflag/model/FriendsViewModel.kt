package elfak.mosis.capturetheflag.model

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.data.User

class FriendsViewModel : ViewModel() {

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private var _friends = MutableLiveData<MutableList<User>>()
    var friends: LiveData<MutableList<User>> = _friends




    fun getFriends(currentUserUid: String) {
        subscribeToFriendsDB(currentUserUid)

    }

    fun getFriendByUid(friendUid: String)
    {
        var friend: User? = null
        val ref = dbRef.child("users").child(friendUid)

        val valueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.uid = friendUid
                if(_friends.value == null)
                    _friends.value = mutableListOf()
                if(user != null) {
                    val list  = _friends.value
                    val m: Int? = list?.indexOfFirst { u -> u.phoneNum == user?.phoneNum }
                    if(m != null && m != -1)
                    {
                            list?.set(m, user)
                    }
                    else {
                        list?.add(user)
                    }
                    _friends.value = list

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, error.message)
            }

        }
        ref.addListenerForSingleValueEvent(valueEventListener)

    }

    fun addFriend(currentUserUid: String, friendUid: String){
        val key = dbRef.child("users").push().key
        if (key == null) {
            Log.w(ContentValues.TAG, "Couldn't get push key for posts")
        }
        else {
            dbRef.child("friends").child(currentUserUid).child(friendUid).setValue(true)
        }
    }

    private fun subscribeToFriendsDB(currentUserUid: String){
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                val value = dataSnapshot.getValue<Boolean>()
                val key = dataSnapshot.key
                Log.i("onChildAdded", "${value.toString()} key: $key")

                getFriendByUid(dataSnapshot.key!!)

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                //TODO:Not implemented yet
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                //TODO:Not implemented yet
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)

                //TODO:Not implemented yet

                // ...
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                //TODO:Not implemented yet
            }
        }
        dbRef.child("friends").child(currentUserUid).addChildEventListener(childEventListener)
    }

}