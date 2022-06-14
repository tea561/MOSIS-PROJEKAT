package elfak.mosis.capturetheflag.model

import android.content.Intent
import androidx.lifecycle.ViewModel
import elfak.mosis.capturetheflag.game.map.LocationService

class MainViewModel : ViewModel() {
    var keepLocationServiceAlive = true
}