package elfak.mosis.capturetheflag.game.map

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.data.UserWithLocation
import elfak.mosis.capturetheflag.utils.extensions.FirebaseLocation
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class MapViewModel(app: Application, var uid: String) : ViewModel(), MapEventsReceiver, Marker.OnMarkerClickListener {
    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private var _userLocation = MutableLiveData<Location>()
    var userLocation: LiveData<Location> = _userLocation

    private var _mapState = MutableLiveData<MapState>()
    var mapState: LiveData<MapState> = _mapState

    private var _friends = MutableLiveData<MutableMap<String,UserWithLocation>>()
    var friends: LiveData<MutableMap<String,UserWithLocation>> = _friends

    var gameBeginning = true

    init {
        _friends.value = mutableMapOf()
        subscribeToLocationsInDB()
        _mapState.value = MapState.Idle

    }

    fun setLocation(location: GeoPoint) {
        dbRef.child("locations").child(uid).setValue(location)
    }

    fun setPlacingMarkerMapState(type: String) {
        _mapState.value = MapState.PlacingMarker(type)
    }

    fun setCooldownMapState(cooldownTime: Int) {
        _mapState.value = MapState.Cooldown(cooldownTime)
    }

    fun setMapState(state: MapState) {
        _mapState.value = state
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        Log.d("singleTapConfirmedHelper", "single tap on map pap ha")
        if (_mapState.value is MapState.PlacingMarker) {
            val state = _mapState.value as MapState.PlacingMarker
            _mapState.value = MapState.ConfirmingMarker(state.type, p!!.latitude, p.longitude)
        }
        else if (_mapState.value is MapState.PlacingFlag) {
            _mapState.value = MapState.ConfirmingFlag(p!!.latitude, p.longitude)
        }
        return true
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        Log.d("longPressHelper", "${p?.latitude} - ${p?.longitude}")
        return true
    }

    private fun subscribeToLocationsInDB() {
        dbRef.child("locations").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("MAP", error.message)
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val location = snapshot.child(uid).getValue(FirebaseLocation::class.java)
                    if (location == null) {
                        Log.i("MAPS", "Location not set yet.")
                        dbRef.child("locations").child(uid).setValue("")
                    }
                    else {
                        Log.i("MAPS", "Location: ${location.latitude}, ${location.longitude}")
                        _userLocation.value = location
                    }
                }
                catch(e: DatabaseException) {
                    dbRef.child("locations").child(uid).setValue("")
                }
            }
        })
    }

    override fun onMarkerClick(marker: Marker?, mapView: MapView?): Boolean {
        return false
    }
}

class MapViewModelFactory(private val app: Application, private val uid: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(app, uid) as T
    }
}

sealed class MapState {
    object Idle: MapState()
    class InGame(val gameId: String): MapState()
    class Cooldown(val time: Number = 120): MapState()
    class PlacingMarker(val type: String = "") : MapState()
    class ConfirmingMarker(val type: String = "", val latitude: Double, val longitude: Double) : MapState()
    object BeginGame: MapState()
    object PlacingFlag: MapState()
    class ConfirmingFlag(val latitude: Double, val longitude: Double) : MapState()
    object WaitingForFlags: MapState()
    object SolvingRiddle: MapState()
}

/*
class MapFilters {
    val friends = MutableLiveData<Boolean>()
    val players = MutableLiveData<Boolean>()
    val teamBarriers = MutableLiveData<Boolean>()
    val teamFlag = MutableLiveData<Boolean>()
    val enemyBarriers = MutableLiveData<Boolean>()
    val enemyFlag = MutableLiveData<Boolean>()

    init {
        friends.value = true
        players.value = true
        teamBarriers.value = true
        teamFlag.value = true
        enemyBarriers.value = true
        enemyFlag.value = true
    }
}*/
