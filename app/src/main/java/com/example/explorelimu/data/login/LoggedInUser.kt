package com.example.explorelimu.data.login

import com.example.explorelimu.xmpp.RoosterConnection

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val displayName: String,
)