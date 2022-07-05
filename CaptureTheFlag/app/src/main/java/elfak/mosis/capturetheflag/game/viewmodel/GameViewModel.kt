package elfak.mosis.capturetheflag.game.viewmodel

import android.content.ContentValues
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
import elfak.mosis.capturetheflag.data.MapObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class GameViewModel : ViewModel() {
    //var _gameState: MutableLiveData<GameState>

    private val _findGameState by lazy { MutableLiveData<FindGameState>(FindGameState.Idle)}
    var findGameState: LiveData<FindGameState> = _findGameState

    private val _joinGameState by lazy { MutableLiveData<FindGameState>(FindGameState.Idle)}
    var joinGameState: LiveData<FindGameState> = _joinGameState

    lateinit var team1name: String
    lateinit var team2name: String
    lateinit var gameUid: String
    var teamNumber: Int = -1

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    public fun createGame(team1: String, team2: String): String{
        val uniqueID: String = UUID.randomUUID().toString()
        val gameCode: String = randomString()
        gameUid = uniqueID
        team1name = team1
        team2name = team2

        dbRef.child("games").child(uniqueID).child("team1").child("name").setValue(team1)
        dbRef.child("games").child(uniqueID).child("team2").child("name").setValue(team2)
        dbRef.child("games").child(uniqueID).child("gameCode").setValue(gameCode)
        return gameCode
    }

    public fun getGame(gameCode: String){
        dbRef.child("games").orderByChild("gameCode").equalTo(gameCode).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value != null){
                    val hashMap: HashMap<Any, Any> = snapshot.value as HashMap<Any, Any> /* = java.util.HashMap<kotlin.Any, kotlin.Any> */
                    hashMap.forEach{ entry ->
                        val data = entry.value as HashMap<Any, Any>
                        team1name = (data["team1"] as Map<String, Any>).getValue("name") as String
                        team2name = (data["team2"] as Map<String, Any>).getValue("name") as String
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

    public fun addPlayerToGame(userUid: String, teamNum: Int)
    {
        teamNumber = teamNum
        val team = if(teamNum == 1) "team1" else "team2"
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

    fun putGameObjectToDB(gameID: String, type: String, team: Int, lat: Double, long: Double) {
        //TODO: put to DB
        val teamString = if(team == 1) "team1" else "team2"
        val uniqueID: String = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()  //LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val mapObject = MapObject(uniqueID, lat, long, type, timestamp)
        dbRef.child("games").child(gameUid).child(teamString).child("objects").child(uniqueID).setValue(mapObject)
            .addOnSuccessListener {
                Log.i("GAME", "Object of type $type inserted into DB with uid: $uniqueID .")
            }
            .addOnFailureListener {
                Log.e("GAME", "Error while inserting object of type $type, message was: ${it.message}")
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
