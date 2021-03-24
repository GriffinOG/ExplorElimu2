package com.example.explorelimu.ui.login

import com.example.explorelimu.xmpp.RoosterConnection

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)