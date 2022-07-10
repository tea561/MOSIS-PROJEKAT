package elfak.mosis.capturetheflag.utils.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import elfak.mosis.capturetheflag.MainActivity
import elfak.mosis.capturetheflag.R
import java.util.*

fun sendNotification(context: Context, message: String, iconResource: Int) {
    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val NOTIFICATION_CHANNEL_ID = "CTFChannel"
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null)
    {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_MAX)

        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("Capture The Flag")
        .setSmallIcon(iconResource)
        .setContentText(message)
        .setContentIntent(pendingIntent)
        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true)
        .build()

//    with(NotificationManagerCompat.from(context)){
//        notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
//    }

    notificationManager.notify(Random().nextInt(5000), notification)


}