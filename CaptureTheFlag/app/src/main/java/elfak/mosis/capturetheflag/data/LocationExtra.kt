package elfak.mosis.capturetheflag.data

import com.google.firebase.database.Exclude

data class LocationExtra(var type: String = "", var additionalInfo: String = "", var g: String = "", var l: List<Double> = mutableListOf())
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "type" to type,
            "additionalInfo" to additionalInfo,
             "g" to g,
            "l" to l
        )
    }
}
