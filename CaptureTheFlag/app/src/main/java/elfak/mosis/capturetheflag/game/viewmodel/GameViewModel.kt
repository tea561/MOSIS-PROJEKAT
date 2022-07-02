package elfak.mosis.capturetheflag.game.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class GameViewModel : ViewModel() {
    //var _gameState: MutableLiveData<GameState>

    private val _findGameState by lazy { MutableLiveData<FindGameState>(FindGameState.Idle)}
    var findGameState: LiveData<FindGameState> = _findGameState

    private lateinit var team1name: String
    private lateinit var team2name: String

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    public fun createGame(team1: String, team2: String): String{
        val uniqueID: String = UUID.randomUUID().toString()
        val gameCode: String = randomString()

        dbRef.child("games").child(uniqueID).child("team1").child("name").setValue(team1)
        dbRef.child("games").child(uniqueID).child("team2").child("name").setValue(team2)
        dbRef.child("games").child(uniqueID).child("gameCode").setValue(gameCode)
        return gameCode
    }

    public fun getGame(gameCode: String){
        dbRef.child("games").orderByChild("gameCode").equalTo(gameCode).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value != null){
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