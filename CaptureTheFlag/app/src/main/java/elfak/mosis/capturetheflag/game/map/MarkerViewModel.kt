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
import elfak.mosis.capturetheflag.data.MapObject
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.data.UserWithLocation
import elfak.mosis.capturetheflag.utils.enums.MapFilters
import elfak.mosis.capturetheflag.utils.extensions.FirebaseLocation

class MarkerViewModel : ViewModel() {

    private val database = Firebase.database
    private val auth = Firebase.auth
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private var _friendsWithLocations = MutableLiveData<MutableMap<String, UserWithLocation>>()
    val friendsWithLocations: LiveData<MutableMap<String, UserWithLocation>> = _friendsWithLocations

    private var _teamBarriers = MutableLiveData<MutableMap<String, MapObject>>()
    val teamBarriers: LiveData<MutableMap<String, MapObject>> = _teamBarriers

    private var _enemyBarriers = MutableLiveData<MutableMap<String, MapObject>>()
    val enemyBarriers: LiveData<MutableMap<String, MapObject>> = _enemyBarriers

    private var _teamFlag = MutableLiveData<MapObject>()
    val teamFlag: LiveData<MapObject> = _teamFlag

    private var _enemyFlag = MutableLiveData<MapObject>()
    val enemyFlag: LiveData<MapObject> = _enemyFlag

    private var _filters = MutableLiveData<MutableMap<String, Boolean>>()
    val filters: LiveData<MutableMap<String, Boolean>> = _filters

    private var isSubscribedToGameObjects = false

    init {
        _filters.value = mutableMapOf(
            MapFilters.Friends.value to true, MapFilters.Players.value to true,
            MapFilters.TeamBarriers.value to true, MapFilters.EnemyBarriers.value to true,
            MapFilters.TeamFlag.value to true, MapFilters.EnemyFlag.value to true)
        _friendsWithLocations.value = mutableMapOf()
        _teamBarriers.value = mutableMapOf()
        _enemyBarriers.value = mutableMapOf()
    }

    fun getFriendsWithLocations() {
        val userUid = auth.currentUser!!.uid
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(ContentValues.TAG, "onChildAdded:" + dataSnapshot.key!!)
                onChildDBFriends(dataSnapshot, previousChildName)
            }
            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(ContentValues.TAG, "onChildChanged: ${dataSnapshot.key}")
                onChildDBFriends(dataSnapshot, previousChildName)
            }
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(ContentValues.TAG, "onChildRemoved:" + dataSnapshot.key!!)
            }
            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(ContentValues.TAG, "onChildMoved:" + dataSnapshot.key!!)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "postComments:onCancelled", databaseError.toException())
            }
        }
        dbRef.child("friends").child(userUid).addChildEventListener(childEventListener)
    }

    fun getGameObjects(gameUid: String, team: String) {

        if (!isSubscribedToGameObjects) {
            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(ContentValues.TAG, "onChildAdded:" + dataSnapshot.key!!)
                    onChildDBGameObjects(dataSnapshot, previousChildName)
                }
                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(ContentValues.TAG, "onChildChanged: ${dataSnapshot.key}")
                    onChildDBGameObjects(dataSnapshot, previousChildName)
                }
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    Log.d(ContentValues.TAG, "onChildRemoved:" + dataSnapshot.key!!)
                    onChildRemovedGameObject(dataSnapshot)
                }
                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(ContentValues.TAG, "onChildMoved:" + dataSnapshot.key!!)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(ContentValues.TAG, "postComments:onCancelled", databaseError.toException())
                }
            }
            dbRef.child("games")
                .child(gameUid)
                .child(team)
                .child("objects").addChildEventListener(childEventListener)
            isSubscribedToGameObjects = true
        }
    }

    fun setFilters(newFilters: MutableMap<String, Boolean>) {
        _filters.value = newFilters
    }

    private fun onChildDBFriends(dataSnapshot: DataSnapshot, previousChildName: String?) {
        val key = dataSnapshot.key
        val friendList = _friendsWithLocations.value
        friendList!![key!!] = UserWithLocation()
        _friendsWithLocations.value = friendList
        getFriendFromDB(key)
    }

    private fun onChildDBGameObjects(dataSnapshot: DataSnapshot, previousChildName: String?) {
        val key = dataSnapshot.key
        val gameObject = MapObject.fromMap(dataSnapshot.value as Map<String, Any?>)

        when (gameObject.type) {
            MapFilters.TeamBarriers.value -> {
                val teamBarriersList = _teamBarriers.value
                teamBarriersList!![key!!] = gameObject
                _teamBarriers.value = teamBarriersList
            }
            MapFilters.EnemyBarriers.value -> {
                val enemyBarriersList = _enemyBarriers.value
                enemyBarriersList!![key!!] = gameObject
                _enemyBarriers.value = enemyBarriersList
            }
            MapFilters.TeamFlag.value -> {
                _teamFlag.value = gameObject
            }
            MapFilters.EnemyFlag.value -> {
                _enemyFlag.value = gameObject
            }
            else -> {
                // ovo ne bi trebalo da se desi
                Log.d("MARKERS", "Unknown map object type: ${gameObject.type}")
            }
        }
    }

    private fun onChildRemovedGameObject(dataSnapshot: DataSnapshot) {
        val key = dataSnapshot.key

        if (_teamBarriers.value!!.containsKey(key)) {
            val teamBarriersList = _teamBarriers.value
            teamBarriersList!!.remove(key)
            _teamBarriers.value = teamBarriersList
        }
        else if (_enemyBarriers.value!!.containsKey(key)) {
            val enemyBarriersList = _enemyBarriers.value
            enemyBarriersList!!.remove(key)
            _enemyBarriers.value = enemyBarriersList
        }
        else if (_teamFlag.value != null && _teamFlag.value!!.uid == key) {
            _teamFlag.value = MapObject()
        }
        else if (_enemyFlag.value != null && _enemyFlag.value!!.uid == key) {
            _enemyFlag.value = MapObject()
        }
        else {
                // ovo ne bi trebalo da se desi
                Log.d("MARKERS", "Unknown map object with key: $key")
        }

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