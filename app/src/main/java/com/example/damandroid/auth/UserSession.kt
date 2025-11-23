package com.example.damandroid.auth

import com.example.damandroid.api.UserDto

/**
 * Lightweight in-memory store for the currently authenticated user.
 * This lets UI layers read the latest name/location without wiring a full persistence layer yet.
 */
object UserSession {
    @Volatile
    var token: String? = null
        private set

    @Volatile
    var user: UserDto? = null
        private set

    fun update(token: String?, user: UserDto?) {
        this.token = token
        this.user = user
    }

    fun clear() {
        token = null
        user = null
    }
}
