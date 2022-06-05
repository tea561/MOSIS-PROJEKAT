package elfak.mosis.capturetheflag.data

import com.google.firebase.database.Exclude

data class User(var firstName: String? = null, var lastName: String? = null, var phoneNum: String, val imgUrl: String? = null,
                var desc: String? = null, var username: String? = null)
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "phoneNum" to phoneNum,
            "username" to username,
            "desc" to desc,
            "imgUrl" to imgUrl
        )
    }
}