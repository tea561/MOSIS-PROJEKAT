package elfak.mosis.capturetheflag.model

import androidx.lifecycle.ViewModel
import elfak.mosis.capturetheflag.data.User

class UserViewModel : ViewModel() {
    var selectedUser: User? = null

    //TODO: dodati firebase auth
}