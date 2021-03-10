package bf.fasobizness.bafatech.helper;

import android.content.Context;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import bf.fasobizness.bafatech.interfaces.MyListener;

public class NotificationService extends NotificationListenerService {

    private final String TAG = this .getClass().getSimpleName() ;
    Context context ;
    static MyListener myListener ;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext() ;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log. d ( TAG , "********** onNotificationPosted" ) ;
        Log. d ( TAG , "ID :" + sbn.getId() + " \t " + sbn.getNotification(). tickerText + " \t " + sbn.getPackageName()) ;
        Intent notificationMessage = new Intent("SomeData");
        notificationMessage.putExtra("Hello", "World");

        LocalBroadcastManager.getInstance(context).sendBroadcast(notificationMessage);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log. i ( TAG , "********** onNotificationRemoved" ) ;
        Log. i ( TAG , "ID :" + sbn.getId() + " \t " + sbn.getNotification(). tickerText + " \t " + sbn.getPackageName()) ;
        myListener .setValue( "Remove: " + sbn.getPackageName()) ;
    }

    public void setListener (MyListener myListener) {
        NotificationService. myListener = myListener ;
    }
}
