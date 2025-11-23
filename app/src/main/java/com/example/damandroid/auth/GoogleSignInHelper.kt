package com.example.damandroid.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleSignInHelper(private val context: Context) {
    
    // OAuth Client ID from Google Cloud Console
    // Vous pouvez utiliser DEFAULT_SIGN_IN (détection automatique) OU spécifier explicitement le client_id
    // Option 1: Détection automatique avec ID Token (pour backend)
    // Note: Pour obtenir l'ID Token, nous devons utiliser le Client ID Web, pas Android
    // Pour l'instant, on utilise DEFAULT_SIGN_IN et on enverra email/name au backend
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        // Pour obtenir l'ID Token pour le backend, vous devrez créer un Client ID Web
        // et utiliser: .requestIdToken("WEB_CLIENT_ID.apps.googleusercontent.com")
        .build()
    
    // Option 2: Client ID Web pour ID Token (à utiliser si vous avez créé un Client ID Web)
    // private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    //     .requestEmail()
    //     .requestProfile()
    //     .requestIdToken("VOTRE_CLIENT_ID_WEB_ICI.apps.googleusercontent.com") // Client ID Web (pas Android)
    //     .build()
    
    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
    
    /**
     * Get the GoogleSignInClient for launching sign-in intent
     */
    fun getSignInClient(): GoogleSignInClient {
        return googleSignInClient
    }
    
    /**
     * Handle the result from Google Sign-In activity
     * Returns GoogleSignInAccount on success, null on failure
     */
    suspend fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInAccount? {
        return try {
            val account = task.getResult(ApiException::class.java)
            Log.d("GoogleSignInHelper", "Google Sign-In successful: ${account?.email}")
            account
        } catch (e: ApiException) {
            val errorMessage = when (e.statusCode) {
                10 -> {
                    "DEVELOPER_ERROR: Google Sign-In configuration issue. " +
                    "Please verify:\n" +
                    "1. SHA-1 fingerprint is added in Google Cloud Console\n" +
                    "2. Package name matches: com.example.damandroid\n" +
                    "3. Wait 5-10 minutes after adding SHA-1"
                }
                12501 -> "Sign-in cancelled by user"
                7 -> "Network error. Please check your internet connection"
                8 -> "Internal error. Please try again"
                else -> "Unknown error: ${e.statusCode}"
            }
            Log.e("GoogleSignInHelper", "Google Sign-In failed: ${e.statusCode} - $errorMessage", e)
            null
        }
    }
    
    /**
     * Get the last signed-in account (if any)
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    /**
     * Sign out from Google
     */
    suspend fun signOut() {
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                googleSignInClient.signOut()
                    .addOnSuccessListener {
                        Log.d("GoogleSignInHelper", "Google Sign-Out successful")
                        continuation.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        Log.e("GoogleSignInHelper", "Google Sign-Out failed", e)
                        continuation.resumeWithException(e)
                    }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignInHelper", "Google Sign-Out failed", e)
        }
    }
    
    /**
     * Revoke access and sign out
     */
    suspend fun revokeAccess() {
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                googleSignInClient.revokeAccess()
                    .addOnSuccessListener {
                        Log.d("GoogleSignInHelper", "Google access revoked")
                        continuation.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        Log.e("GoogleSignInHelper", "Google revoke access failed", e)
                        continuation.resumeWithException(e)
                    }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignInHelper", "Google revoke access failed", e)
        }
    }
}

