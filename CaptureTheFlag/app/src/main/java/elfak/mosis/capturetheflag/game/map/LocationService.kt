package elfak.mosis.capturetheflag.game.map

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.firebase.geofire.*
import com.firebase.geofire.core.GeoHash
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.MainActivity
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.LocationExtra
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.utils.enums.MapFilters
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.gameID
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.isAppActive
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.opposingTeam
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.userId
import elfak.mosis.capturetheflag.utils.helpers.sendNotification
import java.util.*


class LocationService : Service(), LocationListener {
    val NOTIFICATION_CHANNEL_ID = "CTFChannel"
    private var userID: String? = ""
    private var isAppActive: Boolean = false
    private var gameID: String? = ""

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private val referenceGeoFire = dbRef.child("locationsGeoFire")

    private var friendsList = mutableListOf<String>()
    private lateinit var geoQueryDataListener: GeoQueryDataEventListener
    private lateinit var geoQuery: GeoQuery
    private lateinit var geoFire: GeoFire


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
        gameID = prefs.gameID
        isAppActive = prefs.isAppActive

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
        val prefs = PreferenceHelper.customPreference(context!!, "User_data")
        geoQueryDataListener = object: GeoQueryDataEventListener {
            override fun onDataEntered(dataSnapshot: DataSnapshot?, location: GeoLocation?) {
                val temp = dataSnapshot?.getValue<LocationExtra>()
                if(temp?.type == "user" && dataSnapshot?.key != null)
                {
                    if(friendsList.contains(dataSnapshot?.key))
                        getFriendByUid(context, dataSnapshot?.key!!)
                }
                else if(temp?.type == MapFilters.TeamBarriers.value && prefs.opposingTeam == temp?.additionalInfo){
                    if(prefs.isAppActive){
                        val notificationIntent = Intent("SOME_ACTION")
                        notificationIntent.putExtra("message", "You have triggered the riddle")
                        sendBroadcast(notificationIntent)
                    }
                    else{
                        sendNotification(context, "You have triggered the riddle", R.drawable.ic_burst_solid)
                    }
                }
                else if(temp?.type == MapFilters.TeamFlag.value && prefs.opposingTeam == temp?.additionalInfo){
                    if(prefs.isAppActive){
                        val notificationIntent = Intent("SOME_ACTION")
                        notificationIntent.putExtra("message", "You came across the enemy flag")
                        sendBroadcast(notificationIntent)
                    }
                    else {
                        sendNotification(context, "You came across the enemy flag", R.drawable.ic_flag_solid)
                    }
                }

            }

            override fun onDataExited(dataSnapshot: DataSnapshot?) {
                Log.i("GEOFIRE", "exited ${dataSnapshot?.key}")
            }

            override fun onDataMoved(dataSnapshot: DataSnapshot?, location: GeoLocation?) {
                Log.i("GEOFIRE", "moved ${dataSnapshot?.key}")
            }

            override fun onDataChanged(dataSnapshot: DataSnapshot?, location: GeoLocation?) {
                Log.i("GEOFIRE", "changed ${dataSnapshot?.key}")
            }

            override fun onGeoQueryReady() {
                Log.i("GEOFIRE", "ready")
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Log.i("GEOFIRE", error?.message.toString())
            }


        }

        geoFire = GeoFire(referenceGeoFire)
        userID?.let { referenceGeoFire.child(it).child("type").setValue("user") }
        userID?.let { referenceGeoFire.child(it).child("additionalInfo").setValue("") }

//        referenceGeoFire.child("keyZaRiddle").child("type").setValue("riddle")
//        val geoHash: GeoHash = GeoHash(43.31585166666667,
//            21.91415833333333)
//
//        referenceGeoFire.child("keyZaRiddle").child("l").setValue(arrayListOf(43.31585166666667,
//            21.91415833333333))
//        referenceGeoFire.child("keyZaRiddle").child("g").setValue(geoHash.geoHashString)

        geoQuery = geoFire.queryAtLocation(GeoLocation(0.0, 0.0), 0.0)
        geoQuery.addGeoQueryDataEventListener(geoQueryDataListener)

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
        val geoHash: GeoHash = GeoHash(location.latitude, location.longitude)
        userID?.let { referenceGeoFire.child(it).child("l").setValue(arrayListOf(location.latitude, location.longitude)) }
        userID?.let { referenceGeoFire.child(it).child("g").setValue(geoHash.geoHashString) }

        geoQuery.center = GeoLocation(location.latitude, location.longitude)
        geoQuery.radius = 0.3
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.i("LOCATION", "onStatusChanged")
    }

    override fun onProviderDisabled(provider: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            super.onProviderDisabled(provider)
    }

    override fun onProviderEnabled(provider: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            super.onProviderEnabled(provider)
    }


}
