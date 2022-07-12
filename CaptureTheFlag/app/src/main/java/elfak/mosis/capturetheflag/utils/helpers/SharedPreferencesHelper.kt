package elfak.mosis.capturetheflag.utils.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PreferenceHelper {

    val USER_ID = "USER_ID"
    val USER_PASSWORD = "PASSWORD"
    val IS_APP_ACTIVE = "IS_APP_ACTIVE"
    val GAME_ID = "GAME_ID"
    val OPPOSING_TEAM = "OPPOSING_TEAM"

    fun defaultPreference(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun customPreference(context: Context, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    inline fun SharedPreferences.editPref(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    fun SharedPreferences.Editor.put(pair: Pair<String, Any>) {
        val key = pair.first
        val value = pair.second
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            else -> error("Only primitive types can be stored in SharedPreferences")
        }
    }

    var SharedPreferences.userId
        get() = getString(USER_ID, "0")
        set(value) {
            editPref {
                it.putString(USER_ID, value)
            }
        }

    var SharedPreferences.isAppActive
        get() = getBoolean(IS_APP_ACTIVE, true)
        set(value) {
            editPref {
                it.putBoolean(IS_APP_ACTIVE, value)
            }
        }

    var SharedPreferences.gameID
        get() = getString(GAME_ID, "")
        set(value) {
            editPref {
                it.putString(GAME_ID, value)
            }
        }

    var SharedPreferences.opposingTeam
        get() = getString(OPPOSING_TEAM, "")
        set(value) {
            editPref {
                it.putString(OPPOSING_TEAM, value)
            }
        }

    var SharedPreferences.password
        get() = getString(USER_PASSWORD, "")
        set(value) {
            editPref {
                //it.put(USER_PASSWORD to value)
                it.putString(USER_PASSWORD, value)
            }
        }

    var SharedPreferences.clearValues
        get() = { }
        set(value) {
            editPref {
                /*it.remove(USER_ID)
                it.remove(USER_PASSWORD)*/
                it.clear()
            }
        }
}