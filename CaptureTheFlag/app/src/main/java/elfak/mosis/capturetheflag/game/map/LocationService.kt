package elfak.mosis.capturetheflag.game.map

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class LocationService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            while (true) {
                Log.i("LOCATIONSERVICE", "Location Service is running in the background.")
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
        return START_STICKY
    }
}