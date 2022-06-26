package elfak.mosis.capturetheflag.game.viewmodel

import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    //var _gameState: MutableLiveData<GameState>

}

sealed class GameState {
    object Playing : GameState()
    object Cooldown : GameState()
    object SolvingRiddle : GameState()
    //class UploadError(val message: String? = null) : StoreUploadState()
}