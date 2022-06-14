package elfak.mosis.capturetheflag.game.map

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LocationService : Service() {

    private lateinit var locationProviderClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private lateinit var notificationManager: NotificationManager

    private var userID: String? = ""

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    override fun onCreate() {
        super.onCreate()

    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
                Log.i("LOCATIONSERVICE", "Location Service is running in the background.")
                /*try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }*/
                locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
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
                    Looper.getMainLooper())

        }.start()
        userID = intent!!.getStringExtra("userID")
        return START_STICKY
    }

    private fun putLocationToDB(location: Location) {
        userID?.let { dbRef.child("locations").child(it).setValue(location) }
    }
}
