package com.example.explorelimu.data.login

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.explorelimu.xmpp.RoosterConnection
import com.example.explorelimu.xmpp.RoosterConnectionService


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private val TAG = "LoginDataSource"

    fun login(context: Context, email: String, password: String): Result<LoggedInUser> {

        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(
            context
        )
        prefs.edit()
            .putString("xmpp_jid", email.split("@").toTypedArray()[0])
            .putString("xmpp_password", password)
//            .putBoolean("logged_in", true)
            .apply()

        return try {
            val myIntent = Intent(context, RoosterConnectionService::class.java)
            context.startService(myIntent)

            val loggedInUser = LoggedInUser(email)
            Result.Success(loggedInUser)
        } catch (e: Throwable) {
            Log.e("$TAG login", e.message!!)
            Result.Error(Exception("Error logging in", e))
        }
    }

    fun register(
            context: Context,
            email: String,
            username: String,
            school: String,
            isEducator: Boolean,
            password: String
    ): Result<LoggedInUser> {
        return try {
            val mRoosterConnection = RoosterConnection(context)
            mRoosterConnection.registerUser(email, username, school, isEducator, password)

            Result.RegSuccess()
        } catch (e: Throwable) {
            Log.e("$TAG signup", e.message!!)
            Result.Error(Exception("Error logging in", e))
        }
    }
    
    fun logout() {
        // TODO: revoke authentication
    }
}