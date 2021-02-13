package bf.fasobizness.bafatech.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.activities.user.messaging.DefaultMessagesActivity
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.models.MyResponse
import bf.fasobizness.bafatech.utils.MySharedManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var broadcaster: LocalBroadcastManager

    override fun onCreate() {
        super.onCreate()
        broadcaster = LocalBroadcastManager.getInstance(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            val title = remoteMessage.notification?.title
            val body = remoteMessage.notification?.body
            showNotification(title, body, remoteMessage.data)
            val intent = Intent("MyData")
            intent.putExtra("body", remoteMessage.notification!!.body)
            intent.putExtra("msg", remoteMessage.data["msg"])
            broadcaster.sendBroadcast(intent)
        }
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "c: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val api = RetrofitClient.getClient().create(API::class.java)
        val sharedManager = MySharedManager(this)
        val user = sharedManager.user
        val auth = sharedManager.token
        if (user.isNotEmpty() && !auth.isEmpty()) {
            val call = api.updateFCM(token, "Bearer $auth")
            call.enqueue(object : Callback<MyResponse?> {
                override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                    Log.d(TAG, response.toString())
                }

                override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                    Log.d(TAG, t.toString())
                }
            })
        }
    }

    private fun showNotification(title: String?, body: String?, data: Map<String, String?>) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "bf.fasobizness.bafatech"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "bf.fasobizness.bafatech",
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        notificationBuilder
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.megaphone)
                //.setColor(resources.getColor(R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(body)
        if (data["discussion_id"] != null) {
            val i = Intent(this, DefaultMessagesActivity::class.java)
            // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra("discussion_id", data["discussion_id"])
            i.putExtra("receiver_id", data["receiver_id"])
            val contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            notificationBuilder.setContentIntent(contentIntent)
        }
        notificationManager.notify(Random().nextInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMessaging"
    }
}