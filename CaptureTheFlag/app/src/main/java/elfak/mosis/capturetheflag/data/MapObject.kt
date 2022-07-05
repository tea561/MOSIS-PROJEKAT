package elfak.mosis.capturetheflag.data

import com.google.firebase.database.Exclude
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class MapObject(
    val uid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "",
    val timestamp: Long = System.currentTimeMillis()) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "type" to type,
            "timestamp" to timestamp,
            "uid" to uid
        )
    }
}