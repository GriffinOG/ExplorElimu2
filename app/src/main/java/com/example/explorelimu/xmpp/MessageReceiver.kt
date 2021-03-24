package com.example.explorelimu.xmpp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals("com.myapp.main.TEST_INTENT"))
            Log.d(javaClass.name, "Broadcast received")
    }
}