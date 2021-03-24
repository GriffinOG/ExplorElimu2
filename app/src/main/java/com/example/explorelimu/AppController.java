package com.example.explorelimu;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.explorelimu.xmpp.RoosterConnectionService;

public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

//        if (PreferenceManager.getDefaultSharedPreferences(getInstance())
//                .getBoolean("xmpp_logged_in",true)){
//            Intent intent = new Intent("com.example.explorelimu.START_MAIN");
//            sendBroadcast(intent);
//
//            Intent connectionService = new Intent(mInstance, RoosterConnectionService.class);
//
//            startService(connectionService);
//        } else {
//            Log.d(getClass().getName(), "User isn't logged in");
//        }
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Global.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
