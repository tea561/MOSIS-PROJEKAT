package elfak.mosis.capturetheflag.game.map

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.data.UserWithLocation
import elfak.mosis.capturetheflag.utils.extensions.FirebaseLocation

class MarkerViewModel : ViewModel() {

    private val database = Firebase.database
    private val auth = Firebase.auth
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private var _friendsWithLocations = MutableLiveData<MutableMap<String, UserWithLocation>>()
    val friendsWithLocations: LiveData<MutableMap<String, UserWithLocation>> = _friendsWithLocations

    private var _filters = MutableLiveData<MutableMap<String, Boolean>>()
    val filters: LiveData<MutableMap<String, Boolean>> = _filters

    init {
        _filters.value = mutableMapOf(
            "Friends" to true, "Players" to true,
            "Team Barriers" to true, "Enemy Barriers" to true,
            "Team Flag" to true, "Enemy Flag" to true)
        _friendsWithLocations.value = mutableMapOf()
    }

    fun getFriendsWithLocations() {
        val userUid = auth.currentUser!!.uid
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(ContentValues.TAG, "onChildAdded:" + dataSnapshot.key!!)
                val value = dataSnapshot.getValue<Boolean>()
                val key = dataSnapshot.key
                Log.i("onChildAdded", "${value.toString()} key: $key")
                val friendList = _friendsWithLocations.value
                friendList!![key!!] = UserWithLocation()
                _friendsWithLocations.value = friendList
                getFriendFromDB(key)
            }
            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(ContentValues.TAG, "onChildChanged: ${dataSnapshot.key}")
                val value = dataSnapshot.getValue<Boolean>()
                val key = dataSnapshot.key
                val friendList = _friendsWithLocations.value
                friendList!![key!!] = UserWithLocation()
                _friendsWithLocations.value = friendList
                getFriendFromDB(key)
                //TODO:Not implemented yet
            }
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(ContentValues.TAG, "onChildRemoved:" + dataSnapshot.key!!)

                //TODO:Not implemented yet
            }
            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(ContentValues.TAG, "onChildMoved:" + dataSnapshot.key!!)

                //TODO:Not implemented yet

                // ...
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "postComments:onCancelled", databaseError.toException())
                //TODO:Not implemented yet
            }
        }
        dbRef.child("friends").child(userUid).addChildEventListener(childEventListener)
    }

    fun setFilters(newFilters: MutableMap<String, Boolean>) {
        _filters.value = newFilters
    }

    private fun getFriendFromDB(key: String) {
        val ref = dbRef.child("users").child(key)
        val valueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if(_friendsWithLocations.value == null)
                    _friendsWithLocations.value = mutableMapOf()
                if(user != null) {
                    val friendsList  = _friendsWithLocations.value
                    friendsList!![key]!!.user = user
                    _friendsWithLocations.value = friendsList
                    getFriendLocationFromDB(key)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(ContentValues.TAG, error.message)
            }

        }
        ref.addListenerForSingleValueEvent(valueEventListener)
    }

    private fun getFriendLocationFromDB(key: String) {
        val ref = dbRef.child("locations").child(key)
        val valueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(FirebaseLocation::class.java)
                if(_friendsWithLocations.value == null)
                    _friendsWithLocations.value = mutableMapOf()
                if(location != null) {
                    val friendsList  = _friendsWithLocations.value
                    friendsList!![key]!!.location = location
                    _friendsWithLocations.value = friendsList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(ContentValues.TAG, error.message)
            }

        }
        ref.addValueEventListener(valueEventListener)
    }
}