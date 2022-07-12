package elfak.mosis.capturetheflag.game.viewmodel

import android.content.ContentValues
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import elfak.mosis.capturetheflag.data.GameTeam
import elfak.mosis.capturetheflag.data.MapObject
import elfak.mosis.capturetheflag.model.StoreUploadState
import elfak.mosis.capturetheflag.utils.enums.MapFilters
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GameViewModel : ViewModel() {
    private var _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private val _findGameState by lazy { MutableLiveData<FindGameState>(FindGameState.Idle)}
    var findGameState: LiveData<FindGameState> = _findGameState

    private val _joinGameState by lazy { MutableLiveData<FindGameState>(FindGameState.Idle)}
    var joinGameState: LiveData<FindGameState> = _joinGameState

    lateinit var gameUid: String
    var team: String = ""


    var image: Bitmap? = null

    var objectType: String = ""
    var objectLatitude: Double = 0.0
    var objectLongitude: Double = 0.0

    private val _uploadState by lazy { MutableLiveData<StoreUploadState>(StoreUploadState.Idle) }
    val uploadState: LiveData<StoreUploadState> = _uploadState

    val teams: Map<String, GameTeam> = mapOf("team1" to GameTeam(), "team2" to GameTeam())

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    private val storage = Firebase.storage("gs://capturetheflag-56f1c.appspot.com")
    private val storageRef = storage.reference

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
        val uniqueID: String = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val mapObject = MapObject(
            uniqueID,
            objectLatitude,
            objectLongitude,
            objectType,
            timestamp,
            objectImgUrl,
            answer,
            team
        )
        dbRef.child("games").child(gameUid)
            .child(team)
            .child("objects")
            .child(uniqueID).setValue(mapObject)
            .addOnSuccessListener {
                Log.i("GAME", "Object of type ${mapObject.type} inserted into DB with uid: $uniqueID .")
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

    fun setState(state: GameState) {
        _gameState.value = state
    }


    fun uploadRiddlePhoto() {
        //TODO: uzeti id map objecta
        val photoRef = storageRef.child("riddlePictures").child("todo.jpg")
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

    private fun randomString(): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString = (1..6)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("");
        return randomString
    }

}

sealed class GameState {
    object PlacingFlag: GameState()
    object Playing : GameState()
    object Cooldown : GameState()
    object SolvingRiddle : GameState()
    //class UploadError(val message: String? = null) : StoreUploadState()
}

sealed class FindGameState {
    object Idle : FindGameState()
    class Success(val message: String = "Successful"): FindGameState()
    class FindGameError(val message: String? = null): FindGameState()
}
