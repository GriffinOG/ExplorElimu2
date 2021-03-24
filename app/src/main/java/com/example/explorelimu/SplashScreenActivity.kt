package com.example.explorelimu

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.explorelimu.ui.login.LoginSignupActivity
import com.example.explorelimu.xmpp.RoosterConnectionService


class SplashScreenActivity : AppCompatActivity() {

    val authBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == RoosterConnectionService.UI_AUTHENTICATED){
                Log.d(localClassName, "Received broadcast")
                Intent(this@SplashScreenActivity, MainActivity::class.java).also { myIntent -> startActivity(myIntent) }
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("xmpp_logged_in", false)){
            Log.d(localClassName, "Logged in")
            Intent(this, RoosterConnectionService::class.java).also { intent -> startService(intent) }
        } else {
            Log.d(localClassName, "Not logged in")
            Intent(this, LoginSignupActivity::class.java).also { intent -> startActivity(intent) }
        }
    }

    override fun onResume() {
        super.onResume()
       registerReceiver(authBroadcastReceiver, IntentFilter(RoosterConnectionService.UI_AUTHENTICATED))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(authBroadcastReceiver)
    }

//    private fun createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = getString(R.string.sub_notification)
//            val descriptionText = getString(R.string.sub_not_desc)
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
//                description = descriptionText
//            }
//            // Register the channel with the system
//            val notificationManager: NotificationManager =
//                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
}