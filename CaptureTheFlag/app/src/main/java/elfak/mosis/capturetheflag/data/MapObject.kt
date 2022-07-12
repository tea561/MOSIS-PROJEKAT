package elfak.mosis.capturetheflag.data

import com.google.firebase.database.Exclude
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class MapObject(
    val uid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val riddleImgUrl: String,
    val riddleAnswer: String,
    val team: String) {



    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "type" to type,
            "timestamp" to timestamp,
            "uid" to uid,
            "riddleImgUrl" to riddleImgUrl,
            "riddleAnswer" to riddleAnswer,
            "team" to team
        )
    }

    companion object {
        @Exclude
        fun fromMap(map: Map<String, Any?>): MapObject {
            return MapObject(
                map["uid"] as String,
                map["latitude"] as Double,
                map["longitude"] as Double,
                map["type"] as String,
                map["timestamp"] as Long,
                map["riddleImgUrl"] as String,
                map["riddleAnswer"] as String,
                map["team"] as String
            )
        }
    }
}