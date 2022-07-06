package elfak.mosis.capturetheflag.data

import elfak.mosis.capturetheflag.utils.extensions.FirebaseLocation

data class UserWithLocation(val user: User = User(), var location: FirebaseLocation? = null) {

}
