package bf.fasobizness.bafatech.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.user.messaging.ActivityMessage;
import bf.fasobizness.bafatech.activities.user.messaging.DefaultMessagesActivity;
import bf.fasobizness.bafatech.interfaces.API;
import bf.fasobizness.bafatech.models.MyResponse;
import bf.fasobizness.bafatech.utils.MySharedManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessaging";
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    public MyFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            String title = Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
            String body = Objects.requireNonNull(remoteMessage.getNotification()).getBody();
            showNotification(title, body, remoteMessage.getData());
            Intent intent = new Intent("MyData");
            intent.putExtra("body", remoteMessage.getNotification().getBody());
            intent.putExtra("msg", remoteMessage.getData().get("msg"));
            broadcaster.sendBroadcast(intent);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }

    @Override
    public void onNewToken(@NonNull String token) {
        // Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        API api = RetrofitClient.getClient().create(API.class);
        MySharedManager sharedManager = new MySharedManager(this);
        String user = sharedManager.getUser();
        String auth = sharedManager.getToken();
        if (!user.isEmpty() && !auth.isEmpty()) {
            Call<MyResponse> call = api.updateFCM(token, "Bearer " + auth);
            call.enqueue(new Callback<MyResponse>() {
                @Override
                public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                    Log.d(TAG, response.toString());
                }

                @Override
                public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                    Log.d(TAG, t.toString());
                }
            });
        }
    }

    private void showNotification(String title, String body, Map<String, String> data) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "bf.fasobizness.bafatech";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "bf.fasobizness.bafatech",
                    NotificationManager.IMPORTANCE_DEFAULT);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.megaphone)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(body);

        if (data.get("discussion_id") != null) {
            Intent i = new Intent(this, DefaultMessagesActivity.class);
            // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra("discussion_id", data.get("discussion_id"));
            i.putExtra("receiver_id", data.get("receiver_id"));
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(contentIntent);
        }

        if (notificationManager != null) {
            notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
        }
    }
}
