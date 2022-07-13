package elfak.mosis.capturetheflag.game.viewmodel

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.geofire.core.GeoHash

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import elfak.mosis.capturetheflag.data.GameTeam
import elfak.mosis.capturetheflag.data.MapObject
import elfak.mosis.capturetheflag.model.StoreUploadState
import elfak.mosis.capturetheflag.utils.enums.MapFilters
import elfak.mosis.capturetheflag.utils.extensions.FirebaseLocation
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GameViewModel : ViewModel() {
    private val _findGameState by lazy { MutableLiveData<FindGameState>(FindGameState.Idle)}
    var findGameState: LiveData<FindGameState> = _findGameState

    private val _joinGameState by lazy { MutableLiveData<FindGameState>(FindGameState.Idle)}
    var joinGameState: LiveData<FindGameState> = _joinGameState

    private val _triggeredRiddleImage by lazy { MutableLiveData<Bitmap>()}
    var triggeredRiddleImage: LiveData<Bitmap> = _triggeredRiddleImage
    private val _triggeredRiddleAnswer by lazy { MutableLiveData<String>()}
    var triggeredRiddleAnswer: LiveData<String> = _triggeredRiddleAnswer


    private val _winner by lazy { MutableLiveData<String>("")}
    var winner: LiveData<String> = _winner

    private var isSubscribedToWinner = false


    var riddleType: String = ""

    var gameUid: String = ""
    var team: String = ""
    var opposingTeamName: String = ""



    var image: Bitmap? = null

    var objectType: String = ""
    var objectLatitude: Double = 0.0
    var objectLongitude: Double = 0.0
    private var objectID: String = ""

    private val _uploadState by lazy { MutableLiveData<StoreUploadState>(StoreUploadState.Idle) }
    val uploadState: LiveData<StoreUploadState> = _uploadState

    var teams: Map<String, GameTeam> = mapOf("team1" to GameTeam(), "team2" to GameTeam())

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private val storage = Firebase.storage("gs://capturetheflag-56f1c.appspot.com")
    private val storageRef = storage.reference

    fun resetUploadState() {
        _uploadState.value = StoreUploadState.Idle
    }

    fun createGame(team1: String, team2: String): String{
        val uniqueID: String = UUID.randomUUID().toString()
        val gameCode: String = randomString()
        gameUid = uniqueID

        teams["team1"]!!.teamName = team1
        teams["team2"]!!.teamName = team2

        dbRef.child("games").child(uniqueID).child("team1").child("name").setValue(team1)
        dbRef.child("games").child(uniqueID).child("team2").child("name").setValue(team2)
        dbRef.child("games").child(uniqueID).child("gameCode").setValue(gameCode)
        dbRef.child("games").child(uniqueID).child("flagCount").setValue(0)
        dbRef.child("games").child(uniqueID).child("winner").setValue("")
        return gameCode
    }

    fun getGame(gameCode: String){
        dbRef.child("games").orderByChild("gameCode").equalTo(gameCode).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value != null){
                    val hashMap: HashMap<Any, Any> = snapshot.value as HashMap<Any, Any> /* = java.util.HashMap<kotlin.Any, kotlin.Any> */
                    hashMap.forEach{ entry ->
                        val data = entry.value as HashMap<Any, Any>

                        val team1info = data["team1"] as Map<String, Any>
                        val team2info = data["team2"] as Map<String, Any>

                        teams["team1"]!!.teamName = team1info.getValue("name") as String
                        teams["team2"]!!.teamName = team2info.getValue("name") as String

                        if (team1info.containsKey("players")) {
                            teams["team1"]!!.teamMembers = ArrayList((team1info.getValue("players") as Map<String, Any>).keys)
                            teams["team1"]!!.memberCount = teams["team1"]!!.teamMembers.count()
                        }
                        else {
                            teams["team1"]!!.memberCount = 0
                        }
                        if (team2info.containsKey("players")) {
                            teams["team2"]!!.teamMembers = ArrayList((team1info.getValue("players") as Map<String, Any>).keys)
                            teams["team2"]!!.memberCount = teams["team1"]!!.teamMembers.count()
                        }
                        else {
                            teams["team2"]!!.memberCount = 0
                        }
                        gameUid = entry.key as String
                    }
                    _findGameState.value = FindGameState.Success("Game found.")
                }
                else{
                    _findGameState.value = FindGameState.FindGameError("Game not found.")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                _findGameState.value = FindGameState.FindGameError(error.message)
            }
        })
    }

    fun addPlayerToGame(userUid: String, teamNum: Int)
    {
        team = if(teamNum == 1) "team1" else "team2"
        val opponent = if(teamNum == 1) "team2" else "team1"
        opposingTeamName = teams[opponent]!!.teamName
        val key = dbRef.child("games").child(gameUid).child(team).push().key
        if(key == null){
            _joinGameState.value = FindGameState.FindGameError("Couldn't get push key for posts")
            Log.w(ContentValues.TAG, "Couldn't get push key for posts")
        }
        else {
            dbRef.child("games").child(gameUid).child(team).child("players").child(userUid).setValue(true)
                .addOnSuccessListener {

                    _joinGameState.value = FindGameState.Success("User added to game.")
                }
                .addOnFailureListener {
                    _joinGameState.value = FindGameState.FindGameError(it.message)
                }

        }
    }

    fun putGameObjectToDB(objectImgUrl: String, answer: String) {
        //TODO: put to DB
        val gameUid = gameUid
        var objectUid = objectID
        if ( objectUid.isEmpty()) {
            val uniqueID: String = UUID.randomUUID().toString()
            objectUid = uniqueID
        }
        val timestamp = System.currentTimeMillis()
        val mapObject = MapObject(
            objectUid,
            objectLatitude,
            objectLongitude,
            objectType,
            timestamp,
            objectImgUrl,
            answer,
            team
        )

        dbRef.child("locationsGeoFire").child(objectID).child("type").setValue(objectType)
        val geoHash: GeoHash = GeoHash(objectLatitude,
            objectLongitude)
        dbRef.child("locationsGeoFire").child(objectID).child("l").setValue(arrayListOf(objectLatitude, objectLongitude))
        dbRef.child("locationsGeoFire").child(objectID).child("g").setValue(geoHash.geoHashString)
        dbRef.child("locationsGeoFire").child(objectID).child("additionalInfo").setValue(teams[team]!!.teamName)
        dbRef.child("locationsGeoFire").child(objectID).child("team").setValue(team)

        dbRef.child("games").child(gameUid)
            .child(team)
            .child("objects")
            .child(objectUid).setValue(mapObject)
            .addOnSuccessListener {
                Log.i("GAME", "Object of type ${mapObject.type} inserted into DB with uid: $objectUid .")
                if(mapObject.type == MapFilters.TeamFlag.value)
                {
                    dbRef.child("games").child(gameUid).child("flagCount").get().addOnSuccessListener {
                        val count = it.value as Long
                        dbRef.child("games").child(gameUid).child("flagCount").setValue(count + 1)
                    }
                }

            }
            .addOnFailureListener {
                Log.e("GAME", "Error while inserting object of type $objectType, message was: ${it.message}")
            }
    }

    fun resetObjectInfo() {
        objectType = ""
        objectLongitude = 0.0
        objectLatitude = 0.0
        image = null
    }

    fun resetGame(){
        team = ""
        _winner.value = ""
        _findGameState.value = FindGameState.Idle
        _joinGameState.value = FindGameState.Idle
        opposingTeamName = ""
        teams = mapOf("team1" to GameTeam(), "team2" to GameTeam())

        object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }
            override fun onFinish() {
                dbRef.child("games").child(gameUid).setValue(null)
                gameUid = ""
            }
        }.start()
    }

    fun uploadRiddlePhoto() {
        val uniqueID: String = UUID.randomUUID().toString()
        objectID = uniqueID
        val photoRef = storageRef.child("riddlePictures").child("$uniqueID.jpg")
        val baos = ByteArrayOutputStream()
        val bitmap = image
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = photoRef.putBytes(data)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception.let {
                    _uploadState.value = StoreUploadState.UploadError("Upload error: ${it?.message}")
                }
            }
            photoRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val photoUrl = task.result.toString()
                _uploadState.value =
                    StoreUploadState.Success(photoUrl)
            }
        }
    }

    fun getRiddlePhotoAndAnswer(riddleID: String, team: String, gameID: String) {
        dbRef.child("games").child(gameID).child(team).child("objects").child(riddleID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e("MAP", error.message)
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val imageUrl = snapshot.child("riddleImgUrl").getValue(String::class.java)
                    if (imageUrl == null) {
                        Log.i("GAME", "Riddle image URL does not exist.")
                        dbRef.child("games").child(gameID).child(team).child(riddleID).child("riddleImgUrl").setValue("")
                    }
                    else {
                        val executor = Executors.newSingleThreadExecutor()
                        val handler = Handler(Looper.getMainLooper())
                        executor.execute{
                            try {
                                val `in` = java.net.URL(imageUrl).openStream()
                                image = BitmapFactory.decodeStream(`in`)
                                handler.post{
                                    _triggeredRiddleImage.value = image
                                }
                            }
                            catch(e: Exception){
                                e.printStackTrace()
                            }
                        }
                    }

                    val riddleAnswer = snapshot.child("riddleAnswer").getValue(String::class.java)
                    if (riddleAnswer != null) {
                        _triggeredRiddleAnswer.value = riddleAnswer
                    }

                    val type = snapshot.child("type").getValue(String::class.java)
                    if (type != null) {
                        riddleType = type
                    }
                }
                catch(e: DatabaseException) {
                    dbRef.child("games").child(gameID).child(team).child(riddleID).child("riddleImgUrl").setValue("")
                }
            }
        })
    }

    fun subscribeToWinnerInDB(){
        if (!isSubscribedToWinner) {
            if (gameUid != "") {
                isSubscribedToWinner = true
                dbRef.child("games").child(gameUid).child("winner").addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val temp = snapshot.getValue(String::class.java)
                        if (temp != null) {
                            _winner.value = temp
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                    }

                })
            }
        }
    }

    fun setWinner(){
        dbRef.child("games").child(gameUid).child("winner").setValue(team)
    }

    fun deleteRiddleFromDB(riddleID: String, team: String, gameID: String) {
        dbRef.child("games").child(gameID).child(team).child("objects").child(riddleID).setValue(null)
        dbRef.child("locationsGeoFire").child(riddleID).setValue(null)
    }

    private fun randomString(): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString = (1..6)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("");
        return randomString
    }

}

sealed class FindGameState {
    object Idle : FindGameState()
    class Success(val message: String = "Successful"): FindGameState()
    class FindGameError(val message: String? = null): FindGameState()
}
