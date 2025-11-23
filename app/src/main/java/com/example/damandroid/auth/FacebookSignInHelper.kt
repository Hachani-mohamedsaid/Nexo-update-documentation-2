package com.example.damandroid.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FacebookSignInHelper(private val context: Context) {
    
    private val callbackManager = CallbackManager.Factory.create()
    
    /**
     * Get the CallbackManager for handling Facebook login callbacks
     */
    fun getCallbackManager(): CallbackManager {
        return callbackManager
    }
    
    /**
     * Request Facebook login permissions and return the result
     */
    suspend fun login(permissions: List<String> = listOf("email", "public_profile")): FacebookLoginResult {
        return suspendCancellableCoroutine { continuation ->
            var callbackRegistered = false
            val callback = object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val accessToken = result.accessToken
                    Log.d("FacebookSignInHelper", "Facebook login successful: ${accessToken.userId}")
                    
                    // Get user info from GraphRequest (this is also async, so we need to handle it)
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        try {
                            val userInfo = getUserInfo(accessToken)
                            
                            continuation.resume(
                                FacebookLoginResult.Success(
                                    accessToken = accessToken,
                                    userId = accessToken.userId,
                                    email = userInfo.first,
                                    name = userInfo.second,
                                    photoUrl = userInfo.third
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("FacebookSignInHelper", "Error getting user info: ${e.message}", e)
                            // Still return success with what we have
                            continuation.resume(
                                FacebookLoginResult.Success(
                                    accessToken = accessToken,
                                    userId = accessToken.userId,
                                    email = null,
                                    name = null,
                                    photoUrl = null
                                )
                            )
                        }
                    }
                }
                
                override fun onCancel() {
                    Log.d("FacebookSignInHelper", "Facebook login cancelled")
                    continuation.resume(FacebookLoginResult.Cancelled)
                }
                
                override fun onError(error: FacebookException) {
                    Log.e("FacebookSignInHelper", "Facebook login error: ${error.message}", error)
                    continuation.resumeWithException(error)
                }
            }
            
            LoginManager.getInstance().registerCallback(callbackManager, callback)
            callbackRegistered = true
            
            // Start login
            val activity = context as? androidx.activity.ComponentActivity
            if (activity != null) {
                LoginManager.getInstance().logInWithReadPermissions(activity, permissions)
            } else {
                continuation.resumeWithException(IllegalStateException("Context is not a ComponentActivity"))
            }
        }
    }
    
    /**
     * Get user info from Facebook Graph API
     */
    private suspend fun getUserInfo(accessToken: AccessToken): Triple<String?, String?, String?> {
        return suspendCancellableCoroutine { continuation ->
            val request = GraphRequest.newMeRequest(
                accessToken
            ) { jsonObject, response ->
                if (response?.error != null) {
                    Log.e("FacebookSignInHelper", "GraphRequest error: ${response.error}")
                    continuation.resume(Triple(null, null, null))
                } else {
                    val email = jsonObject?.optString("email")
                    val name = jsonObject?.optString("name")
                    val picture = jsonObject?.optJSONObject("picture")
                        ?.optJSONObject("data")
                        ?.optString("url")
                    
                    Log.d("FacebookSignInHelper", "User info - Email: $email, Name: $name")
                    continuation.resume(Triple(email, name, picture))
                }
            }
            
            val parameters = Bundle().apply {
                putString("fields", "id,name,email,picture.type(large)")
            }
            request.parameters = parameters
            request.executeAsync()
        }
    }
    
    /**
     * Check if user is currently logged in
     */
    fun isLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }
    
    /**
     * Get current access token
     */
    fun getCurrentAccessToken(): AccessToken? {
        return AccessToken.getCurrentAccessToken()
    }
    
    /**
     * Sign out from Facebook
     */
    suspend fun signOut() {
        try {
            LoginManager.getInstance().logOut()
            Log.d("FacebookSignInHelper", "Facebook Sign-Out successful")
        } catch (e: Exception) {
            Log.e("FacebookSignInHelper", "Facebook Sign-Out failed", e)
        }
    }
    
    /**
     * Sealed class for Facebook login results
     */
    sealed class FacebookLoginResult {
        data class Success(
            val accessToken: AccessToken,
            val userId: String,
            val email: String?,
            val name: String?,
            val photoUrl: String?
        ) : FacebookLoginResult()
        
        object Cancelled : FacebookLoginResult()
    }
}

