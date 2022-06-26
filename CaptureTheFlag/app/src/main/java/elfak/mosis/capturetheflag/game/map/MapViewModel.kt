package elfak.mosis.capturetheflag.game.map

import android.app.Application
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider


class MapViewModel(app: Application, var uid: String) : ViewModel(), MapEventsReceiver {
    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private var _userLocation = MutableLiveData<Location>()
    var userLocation: LiveData<Location> = _userLocation

    private var _mapState = MutableLiveData<MapState>()
    var mapState: LiveData<MapState> = _mapState

    init {
        subscribeToLocationInDB()
    }

    fun setLocation(location: GeoPoint) {
        dbRef.child("locations").child(uid).setValue(location)
    }

    private fun subscribeToLocationInDB() {
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
                        _userLocation.value = location!!
                    }
                }
                catch(e: DatabaseException) {
                    dbRef.child("locations").child(uid).setValue("")
                }


               /* } else {
                    Log.e("MAPS", "Location unavailable.")
                    dbRef.child("locations").child(uid).setValue("")
                }*/
            }
        })

    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        Log.d("singleTapConfirmedHelper", "${p?.latitude} - ${p?.longitude}")
        //TODO: proveriti stanje mape i handleovati event na pravi nacin
        return true
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        Log.d("longPressHelper", "${p?.latitude} - ${p?.longitude}")
        return false
    }
}

class MapViewModelFactory(private val app: Application, private val uid: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapViewModel(app, uid) as T
    }
}

class FirebaseLocation: Location(LocationManager.GPS_PROVIDER) {

}

sealed class MapState {
    object PlacingFlag : MapState()
    object PlacingBarrier : MapState()
    object PlacingEnemyBarrier : MapState()
    object PlacingEnemyFlag : MapState()
    object Idle: MapState()
    //class UploadError(val message: String? = null) : StoreUploadState()
}