package elfak.mosis.capturetheflag.game.map

import android.Manifest
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.FirebaseError
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.utils.extensions.FirebaseLocation
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.userId
import elfak.mosis.capturetheflag.utils.helpers.sendNotification


class LocationService : Service(), LocationListener {

    private var userID: String? = ""

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private var friendsList = mutableListOf<String>()
    private lateinit var geoQueryListener: GeoQueryEventListener
    private lateinit var geoQuery: GeoQuery


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

        }.start()
        val prefs = PreferenceHelper.customPreference(this, "User_data")
        userID = prefs.userId
        getFriends()
        initGeoFire()
        return START_STICKY
    }

    private fun putLocationToDB(location: Location) {
        userID?.let { dbRef.child("locations").child(it).setValue(location) }

    }

    private fun getFriends(){
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(ContentValues.TAG, "onChildAdded:" + dataSnapshot.key!!)

                val value = dataSnapshot.getValue<Boolean>()
                val key = dataSnapshot.key
                Log.i("onChildAdded", "${value.toString()} key: $key")

                if (key != null) {
                    friendsList.add(key)
                }

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(ContentValues.TAG, "onChildChanged: ${dataSnapshot.key}")

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
        if(userID != null)
        {
            dbRef.child("friends").child(userID!!).addChildEventListener(childEventListener)
        }


    }

    private fun initGeoFire()
    {
        val context = this
        geoQueryListener = object: GeoQueryEventListener{
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                Log.i("GEOFIRE", "entered $key")
                if(friendsList.contains(key))
                {
                    if (key != null) {
                        getFriendByUid(context, key)
                    }
                }
            }

            override fun onKeyExited(key: String?) {
                Log.i("GEOFIRE", "exited $key")
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                Log.i("GEOFIRE", "moved $key")
            }

            override fun onGeoQueryReady() {
                Log.i("GEOFIRE", "ready")
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Log.i("GEOFIRE", "error")
            }

        }

        val reference = dbRef.child("locationsGeoFire")
        val geoFire = GeoFire(reference)
        geoFire.setLocation(
            "uYRmzF5E2LR2YW9jPBORz4sBOxL2",
            GeoLocation(43.31585166666667, 21.90415833333333)
        ) { key, error ->
            if (error != null) {
                System.err.println("There was an error saving the location to GeoFire: $error")
            } else {
                println("Location saved on server successfully!")
            }
        }
        geoQuery = geoFire.queryAtLocation(GeoLocation(0.0, 0.0), 0.0)
        geoQuery.addGeoQueryEventListener(geoQueryListener)

    }

    private fun getFriendByUid(context: Context, friendUid: String)
    {
        var friend: User? = null
        val ref = dbRef.child("users").child(friendUid)

        val valueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if(user != null) {
                    sendNotification(context, "${user.username} is near!", R.drawable.ic_person_solid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(ContentValues.TAG, error.message)
            }

        }
        ref.addListenerForSingleValueEvent(valueEventListener)

    }

    override fun onLocationChanged(location: Location) {
        Log.i("LOCATION", "Putting location to DB...")
        putLocationToDB(location)
        geoQuery.center = GeoLocation(location.latitude, location.longitude)
        geoQuery.radius = 0.3
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.i("LOCATION", "onStatusChanged")
    }

}
