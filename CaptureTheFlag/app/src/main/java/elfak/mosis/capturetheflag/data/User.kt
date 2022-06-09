package elfak.mosis.capturetheflag.data

import com.google.firebase.database.Exclude

data class User(var firstName: String? = "", var lastName: String? = "", var phoneNum: String = "", val imgUrl: String? = "",
                var desc: String? = "", var username: String? = "")
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