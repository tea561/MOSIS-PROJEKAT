package elfak.mosis.capturetheflag.game.map

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider


class MapViewModel(app: Application, var uid: String) : ViewModel(), IMyLocationConsumer {
    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private var _userLocation = MutableLiveData<Location>()
    var userLocation: LiveData<Location> = _userLocation

    init {
        //subscribeToLocationInDB()
    }

    fun setLocation(location: GeoPoint) {
        dbRef.child("locations").child(uid).setValue(location)
    }

    fun subscribeToLocationInDB() {
        dbRef.child("locations").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("MAP", error.message)
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(uid)) {
                    val location = snapshot.child(uid).getValue(Location::class.java)
                    Log.i("MAPS", "Location: ${location!!.latitude}, ${location.longitude}")
                } else {
                    Log.e("MAPS", "Location unavailable.")
                    dbRef.child("locations").child(uid).setValue("")
                }
            }
        })
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        //TODO: write location to DB
        Log.i("MAPS", "LOKACIJAAAAAAAAAAAA")
    }
}

class MapViewModelFactory(private val app: Application, private val uid: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapViewModel(app, uid) as T
    }
}