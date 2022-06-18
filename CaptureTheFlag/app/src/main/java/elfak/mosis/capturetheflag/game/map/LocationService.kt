package elfak.mosis.capturetheflag.game.map

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LocationService : Service(), LocationListener {

/*    private lateinit var locationProviderClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private lateinit var notificationManager: NotificationManager*/

    private var userID: String? = ""

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    override fun onCreate() {
        super.onCreate()
        val mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, Float.MIN_VALUE, this)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
                Log.i("LOCATION", "Location Service is running in the background.")
                /*locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            locationProviderClient.lastLocation.addOnCompleteListener {
                if (it.result != null) {
                    putLocationToDB(it.result)
                }
            }
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        lateinit var newLocation: Location
                        for (location in locationResult.locations){
                            newLocation = location
                        }
                        putLocationToDB(newLocation)
                    }
                }
                val locationRequest = com.google.android.gms.location.LocationRequest().apply {
                    interval = 1000
                    fastestInterval = 500
                }

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                }

                locationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())*/

        }.start()
        userID = intent!!.getStringExtra("userID")
        return START_STICKY
    }

    private fun putLocationToDB(location: Location) {
        userID?.let { dbRef.child("locations").child(it).setValue(location) }
    }

    override fun onLocationChanged(location: Location) {
        Log.i("LOCATION", "Putting location to DB...")
        putLocationToDB(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.i("LOCATION", "onStatusChanged")
    }
}
