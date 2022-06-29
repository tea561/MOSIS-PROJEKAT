package elfak.mosis.capturetheflag.game.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class GameViewModel : ViewModel() {
    //var _gameState: MutableLiveData<GameState>

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl(
        "https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

    public fun createGame(team1: String, team2: String){
        val uniqueID: String = UUID.randomUUID().toString()

        dbRef.child("games").child(uniqueID).child("team1").child("name").setValue(team1)
        dbRef.child("games").child(uniqueID).child("team2").child("name").setValue(team2)

    }

}

sealed class GameState {
    object Playing : GameState()
    object Cooldown : GameState()
    object SolvingRiddle : GameState()
    //class UploadError(val message: String? = null) : StoreUploadState()
}