package com.example.damandroid.api

import android.content.Context
import com.example.damandroid.auth.SocialPasswordStore
import com.example.damandroid.auth.UserSession
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.security.MessageDigest

class AuthRepository(context: Context) {
    private val apiService = RetrofitClient.apiService
    private val socialPasswordStore = SocialPasswordStore(context.applicationContext)
    
    sealed class AuthResult {
        data class Success(val token: String, val user: UserDto?) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }
    
    sealed class PasswordResetResult {
        data class Success(val message: String) : PasswordResetResult()
        data class Error(val message: String) : PasswordResetResult()
    }
    
    private fun storeSession(token: String?, user: UserDto?) {
        UserSession.update(token, user)
    }

    suspend fun login(email: String, password: String, rememberMe: Boolean = false): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginDto(email, password, rememberMe))
            android.util.Log.d("AuthRepository", "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                // Get the raw response body from the interceptor
                val rawBodyString = ResponseBodyInterceptor.getRawResponseBody()
                android.util.Log.d("AuthRepository", "Raw response body: $rawBodyString")
                
                val authResponse = response.body()
                android.util.Log.d("AuthRepository", "Parsed response: $authResponse")
                
                // Clear the ThreadLocal after use
                ResponseBodyInterceptor.clearRawResponseBody()
                
                // Try to extract token from parsed response first
                val token = authResponse?.access_token ?: authResponse?.token
                
                if (token != null) {
                    storeSession(token, authResponse?.user)
                    AuthResult.Success(token, authResponse?.user)
                } else if (rawBodyString != null) {
                    // If parsed response doesn't have token, try to extract from raw JSON
                    val tokenFromRaw = extractTokenFromJson(rawBodyString)
                    if (tokenFromRaw != null) {
                        storeSession(tokenFromRaw, authResponse?.user)
                        AuthResult.Success(tokenFromRaw, authResponse?.user)
                    } else {
                        AuthResult.Error("Token not found in response. Raw: $rawBodyString")
                    }
                } else {
                    AuthResult.Error("Token not found and could not read raw response")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Error response: ${response.code()} - $errorBody")
                val errorMessage = parseError(response.code(), errorBody)
                AuthResult.Error(errorMessage)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("AuthRepository", "HttpException: ${e.code()} - $errorBody")
            val errorMessage = parseError(e.code(), errorBody)
            AuthResult.Error(errorMessage)
        } catch (e: IOException) {
            android.util.Log.e("AuthRepository", "IOException: ${e.message}")
            AuthResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Exception: ${e.message}", e)
            AuthResult.Error("Unexpected error: ${e.message}")
        } finally {
            // Always clear ThreadLocal in finally block
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
    
    suspend fun register(
        email: String,
        password: String,
        name: String,
        location: String
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(RegisterDto(email, password, name, location))
            android.util.Log.d("AuthRepository", "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val registerResponse = response.body()
                android.util.Log.d("AuthRepository", "Register response: $registerResponse")
                
                if (registerResponse != null) {
                    // Convert RegisterResponse to UserDto
                    val userDto = UserDto(
                        id = registerResponse._id ?: registerResponse.id ?: "",
                        email = registerResponse.email,
                        name = registerResponse.name,
                        location = registerResponse.location
                    )
                    
                    // Registration successful - return success without token (user needs to login)
                    // Return empty token to indicate registration-only (no auto-login)
                    android.util.Log.d("AuthRepository", "Registration successful")
                    storeSession("", userDto)
                    AuthResult.Success("", userDto)
                } else {
                    AuthResult.Error("Registration response is null")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Error response: ${response.code()} - $errorBody")
                val errorMessage = parseError(response.code(), errorBody)
                AuthResult.Error(errorMessage)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("AuthRepository", "HttpException: ${e.code()} - $errorBody")
            val errorMessage = parseError(e.code(), errorBody)
            AuthResult.Error(errorMessage)
        } catch (e: IOException) {
            android.util.Log.e("AuthRepository", "IOException: ${e.message}")
            AuthResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Exception: ${e.message}", e)
            AuthResult.Error("Unexpected error: ${e.message}")
        }
    }
    
    private fun extractTokenFromJson(json: String): String? {
        return try {
            android.util.Log.d("AuthRepository", "Extracting token from JSON: $json")
            val jsonObject = Gson().fromJson(json, Map::class.java) as? Map<*, *>
            if (jsonObject != null) {
                // Try different possible field names (case-sensitive and case-insensitive)
                val token = (jsonObject["access_token"] as? String)
                    ?: (jsonObject["token"] as? String)
                    ?: (jsonObject["accessToken"] as? String)
                    ?: (jsonObject["jwt"] as? String)
                    ?: (jsonObject["jwtToken"] as? String)
                    ?: (jsonObject["AccessToken"] as? String)
                    ?: (jsonObject["Access_Token"] as? String)
                
                android.util.Log.d("AuthRepository", "Extracted token: ${if (token != null) "found" else "not found"}")
                token
            } else {
                android.util.Log.e("AuthRepository", "Could not parse JSON as Map")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error extracting token: ${e.message}", e)
            null
        }
    }
    
    suspend fun forgotPassword(email: String): PasswordResetResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.forgotPassword(ForgotPasswordDto(email))
            android.util.Log.d("AuthRepository", "Forgot password response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val messageResponse = response.body()
                val message = messageResponse?.message ?: "Password reset email sent successfully"
                android.util.Log.d("AuthRepository", "Forgot password success: $message")
                PasswordResetResult.Success(message)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Forgot password error: ${response.code()} - $errorBody")
                val errorMessage = parseError(response.code(), errorBody)
                PasswordResetResult.Error(errorMessage)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("AuthRepository", "HttpException: ${e.code()} - $errorBody")
            val errorMessage = parseError(e.code(), errorBody)
            PasswordResetResult.Error(errorMessage)
        } catch (e: IOException) {
            android.util.Log.e("AuthRepository", "IOException: ${e.message}")
            PasswordResetResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Exception: ${e.message}", e)
            PasswordResetResult.Error("Unexpected error: ${e.message}")
        }
    }

    suspend fun sendVerificationEmail(email: String): PasswordResetResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendVerificationEmail(VerificationEmailDto(email))
            android.util.Log.d("AuthRepository", "Send verification response code: ${response.code()}")

            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Verification email sent"
                PasswordResetResult.Success(message)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Send verification error: ${response.code()} - $errorBody")
                val errorMessage = parseError(response.code(), errorBody)
                PasswordResetResult.Error(errorMessage)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("AuthRepository", "HttpException send verification: ${e.code()} - $errorBody")
            val errorMessage = parseError(e.code(), errorBody)
            PasswordResetResult.Error(errorMessage)
        } catch (e: IOException) {
            android.util.Log.e("AuthRepository", "IOException send verification: ${e.message}")
            PasswordResetResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Exception send verification: ${e.message}", e)
            PasswordResetResult.Error("Unexpected error: ${e.message}")
        }
    }
    
    suspend fun resetPassword(token: String, password: String): PasswordResetResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resetPassword(ResetPasswordDto(token, password))
            android.util.Log.d("AuthRepository", "Reset password response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val messageResponse = response.body()
                val message = messageResponse?.message ?: "Password reset successfully"
                android.util.Log.d("AuthRepository", "Reset password success: $message")
                PasswordResetResult.Success(message)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("AuthRepository", "Reset password error: ${response.code()} - $errorBody")
                val errorMessage = parseError(response.code(), errorBody)
                PasswordResetResult.Error(errorMessage)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("AuthRepository", "HttpException: ${e.code()} - $errorBody")
            val errorMessage = parseError(e.code(), errorBody)
            PasswordResetResult.Error(errorMessage)
        } catch (e: IOException) {
            android.util.Log.e("AuthRepository", "IOException: ${e.message}")
            PasswordResetResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Exception: ${e.message}", e)
            PasswordResetResult.Error("Unexpected error: ${e.message}")
        }
    }
    
    suspend fun loginWithGoogle(
        email: String,
        name: String,
        idToken: String? = null,
        photoUrl: String? = null
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            // Essayer d'abord l'endpoint /auth/google dédié
            try {
                val response = apiService.loginWithGoogle(
                    GoogleLoginDto(
                        email = email,
                        name = name,
                        idToken = idToken,
                        photoUrl = photoUrl
                    )
                )
                android.util.Log.d("AuthRepository", "Google login response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val rawBodyString = ResponseBodyInterceptor.getRawResponseBody()
                    android.util.Log.d("AuthRepository", "Google login raw response: $rawBodyString")
                    
                    val authResponse = response.body()
                    android.util.Log.d("AuthRepository", "Google login parsed response: $authResponse")
                    
                    ResponseBodyInterceptor.clearRawResponseBody()
                    
                    val token = authResponse?.access_token ?: authResponse?.token
                    
                    if (token != null) {
                        storeSession(token, authResponse?.user)
                        return@withContext AuthResult.Success(token, authResponse?.user)
                    } else if (rawBodyString != null) {
                        val tokenFromRaw = extractTokenFromJson(rawBodyString)
                        if (tokenFromRaw != null) {
                            storeSession(tokenFromRaw, authResponse?.user)
                            return@withContext AuthResult.Success(tokenFromRaw, authResponse?.user)
                        }
                    }
                } else if (response.code() == 404) {
                    // Endpoint /auth/google n'existe pas - utiliser l'option fallback
                    android.util.Log.w("AuthRepository", "Endpoint /auth/google not found (404). Using fallback method.")
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("AuthRepository", "Google login error: ${response.code()} - $errorBody")
                    val errorMessage = parseError(response.code(), errorBody)
                    return@withContext AuthResult.Error(errorMessage)
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    // Endpoint n'existe pas, continuer avec l'option fallback
                    android.util.Log.w("AuthRepository", "Endpoint /auth/google not found (404). Using fallback method.")
                } else {
                    // Autre erreur HTTP, la propager
                    val errorBody = e.response()?.errorBody()?.string()
                    android.util.Log.e("AuthRepository", "HttpException Google login: ${e.code()} - $errorBody")
                    val errorMessage = parseError(e.code(), errorBody)
                    return@withContext AuthResult.Error(errorMessage)
                }
            }
            
            // Option Fallback : Utiliser /auth/register puis /auth/login
            // Chercher un mot de passe déjà stocké, sinon en générer un déterministe
            val fallbackPassword = socialPasswordStore.getPassword("google", email)
                ?: generateDeterministicSocialPassword("google", email).also {
                    socialPasswordStore.savePassword("google", email, it)
                }
            
            android.util.Log.d("AuthRepository", "Fallback: Trying to register Google user with email: $email")
            
            // Étape 1 : Essayer de s'inscrire (si l'utilisateur n'existe pas)
            val registerResponse = apiService.register(
                RegisterDto(
                    email = email,
                    password = fallbackPassword, // Mot de passe déterministe
                    name = name,
                    location = "Unknown" // Location par défaut
                )
            )
            
            android.util.Log.d("AuthRepository", "Register response code: ${registerResponse.code()}")
            
            if (registerResponse.isSuccessful) {
                socialPasswordStore.savePassword("google", email, fallbackPassword)
                // Utilisateur créé avec succès
                android.util.Log.d("AuthRepository", "Google user registered successfully")
                
                // Étape 2 : Se connecter avec le mot de passe généré pour obtenir un token
                val loginResponse = apiService.login(
                    LoginDto(email = email, password = fallbackPassword)
                )
                
                android.util.Log.d("AuthRepository", "Login after register response code: ${loginResponse.code()}")
                
                if (loginResponse.isSuccessful) {
                    val rawBodyString = ResponseBodyInterceptor.getRawResponseBody()
                    val authResponse = loginResponse.body()
                    ResponseBodyInterceptor.clearRawResponseBody()
                    
                    val token = authResponse?.access_token ?: authResponse?.token
                    
                    if (token != null) {
                        storeSession(token, authResponse?.user)
                        android.util.Log.d("AuthRepository", "Google user logged in successfully with token")
                        return@withContext AuthResult.Success(token, authResponse?.user)
                    } else if (rawBodyString != null) {
                        val tokenFromRaw = extractTokenFromJson(rawBodyString)
                        if (tokenFromRaw != null) {
                            storeSession(tokenFromRaw, authResponse?.user)
                            return@withContext AuthResult.Success(tokenFromRaw, authResponse?.user)
                        }
                    }
                }
                
                // Si la connexion échoue, retourner au moins l'utilisateur créé
                val registerResult = registerResponse.body()
                if (registerResult != null) {
                    val userDto = UserDto(
                        id = registerResult._id ?: registerResult.id ?: "",
                        email = registerResult.email,
                        name = registerResult.name,
                        location = registerResult.location
                    )
                    android.util.Log.w("AuthRepository", "User created but login failed. User needs to set a password.")
                    storeSession("", userDto)
                    return@withContext AuthResult.Success("", userDto) // Token vide mais utilisateur créé
                }
            } else {
                val errorBody = registerResponse.errorBody()?.string()
                val errorCode = registerResponse.code()
                android.util.Log.e("AuthRepository", "Register error: $errorCode - $errorBody")
                
                // Si l'utilisateur existe déjà (409 Conflict), essayer de se connecter
                if (errorCode == 409) {
                    android.util.Log.d("AuthRepository", "User already exists, trying to login with fallback password...")

                    val loginResponse = apiService.login(
                        LoginDto(email = email, password = fallbackPassword)
                    )

                    if (loginResponse.isSuccessful) {
                        val rawBodyString = ResponseBodyInterceptor.getRawResponseBody()
                        val authResponse = loginResponse.body()
                        ResponseBodyInterceptor.clearRawResponseBody()

                        val token = authResponse?.access_token ?: authResponse?.token

                        if (token != null) {
                            socialPasswordStore.savePassword("google", email, fallbackPassword)
                            storeSession(token, authResponse?.user)
                            return@withContext AuthResult.Success(token, authResponse?.user)
                        } else if (rawBodyString != null) {
                            val tokenFromRaw = extractTokenFromJson(rawBodyString)
                            if (tokenFromRaw != null) {
                                socialPasswordStore.savePassword("google", email, fallbackPassword)
                                storeSession(tokenFromRaw, authResponse?.user)
                                return@withContext AuthResult.Success(tokenFromRaw, authResponse?.user)
                            }
                        }
                    } else {
                        val loginError = loginResponse.errorBody()?.string()
                        android.util.Log.e("AuthRepository", "Fallback login error after 409: ${loginResponse.code()} - $loginError")
                    }

                    return@withContext AuthResult.Error(
                        "Account already exists. If the login keeps failing, please ask the backend team to enable /auth/google."
                    )
                }
                
                val errorMessage = parseError(errorCode, errorBody)
                return@withContext AuthResult.Error(errorMessage)
            }
            
            AuthResult.Error("Failed to register or login with Google. Please create the /auth/google endpoint in your backend for better integration.")
            
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("AuthRepository", "HttpException Google login: ${e.code()} - $errorBody")
            val errorMessage = parseError(e.code(), errorBody)
            AuthResult.Error(errorMessage)
        } catch (e: IOException) {
            android.util.Log.e("AuthRepository", "IOException Google login: ${e.message}")
            AuthResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Exception Google login: ${e.message}", e)
            AuthResult.Error("Unexpected error: ${e.message}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
    
    suspend fun loginWithFacebook(
        email: String,
        name: String,
        userId: String? = null,
        accessToken: String? = null,
        photoUrl: String? = null
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            // Essayer d'abord l'endpoint /auth/facebook dédié
            try {
                val response = apiService.loginWithFacebook(
                    FacebookLoginDto(
                        email = email,
                        name = name,
                        userId = userId,
                        accessToken = accessToken,
                        photoUrl = photoUrl
                    )
                )
                android.util.Log.d("AuthRepository", "Facebook login response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val rawBodyString = ResponseBodyInterceptor.getRawResponseBody()
                    android.util.Log.d("AuthRepository", "Facebook login raw response: $rawBodyString")
                    
                    val authResponse = response.body()
                    android.util.Log.d("AuthRepository", "Facebook login parsed response: $authResponse")
                    
                    ResponseBodyInterceptor.clearRawResponseBody()
                    
                    val token = authResponse?.access_token ?: authResponse?.token
                    
                    if (token != null) {
                        storeSession(token, authResponse?.user)
                        return@withContext AuthResult.Success(token, authResponse?.user)
                    } else if (rawBodyString != null) {
                        val tokenFromRaw = extractTokenFromJson(rawBodyString)
                        if (tokenFromRaw != null) {
                            storeSession(tokenFromRaw, authResponse?.user)
                            return@withContext AuthResult.Success(tokenFromRaw, authResponse?.user)
                        }
                    }
                } else if (response.code() == 404) {
                    // Endpoint /auth/facebook n'existe pas - utiliser l'option fallback
                    android.util.Log.w("AuthRepository", "Endpoint /auth/facebook not found (404). Using fallback method.")
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("AuthRepository", "Facebook login error: ${response.code()} - $errorBody")
                    val errorMessage = parseError(response.code(), errorBody)
                    return@withContext AuthResult.Error(errorMessage)
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    // Endpoint n'existe pas, continuer avec l'option fallback
                    android.util.Log.w("AuthRepository", "Endpoint /auth/facebook not found (404). Using fallback method.")
                } else {
                    // Autre erreur HTTP, la propager
                    val errorBody = e.response()?.errorBody()?.string()
                    android.util.Log.e("AuthRepository", "HttpException Facebook login: ${e.code()} - $errorBody")
                    val errorMessage = parseError(e.code(), errorBody)
                    return@withContext AuthResult.Error(errorMessage)
                }
            }
            
            // Option Fallback : Utiliser /auth/register puis /auth/login
            // Chercher un mot de passe déjà stocké, sinon en générer un déterministe
            val fallbackPassword = socialPasswordStore.getPassword("facebook", email)
                ?: generateDeterministicSocialPassword("facebook", email).also {
                    socialPasswordStore.savePassword("facebook", email, it)
                }
            
            android.util.Log.d("AuthRepository", "Fallback: Trying to register Facebook user with email: $email")
            
            // Étape 1 : Essayer de s'inscrire (si l'utilisateur n'existe pas)
            val registerResponse = apiService.register(
                RegisterDto(
                    email = email,
                    password = fallbackPassword, // Mot de passe déterministe
                    name = name,
                    location = "Unknown" // Location par défaut
                )
            )
            
            android.util.Log.d("AuthRepository", "Register response code: ${registerResponse.code()}")
            
            if (registerResponse.isSuccessful) {
                socialPasswordStore.savePassword("facebook", email, fallbackPassword)
                // Utilisateur créé avec succès
                android.util.Log.d("AuthRepository", "Facebook user registered successfully")
                
                // Étape 2 : Se connecter avec le mot de passe généré pour obtenir un token
                val loginResponse = apiService.login(
                    LoginDto(email = email, password = fallbackPassword)
                )
                
                android.util.Log.d("AuthRepository", "Login after register response code: ${loginResponse.code()}")
                
                if (loginResponse.isSuccessful) {
                    val rawBodyString = ResponseBodyInterceptor.getRawResponseBody()
                    val authResponse = loginResponse.body()
                    ResponseBodyInterceptor.clearRawResponseBody()
                    
                    val token = authResponse?.access_token ?: authResponse?.token
                    
                    if (token != null) {
                        storeSession(token, authResponse?.user)
                        android.util.Log.d("AuthRepository", "Facebook user logged in successfully with token")
                        return@withContext AuthResult.Success(token, authResponse?.user)
                    } else if (rawBodyString != null) {
                        val tokenFromRaw = extractTokenFromJson(rawBodyString)
                        if (tokenFromRaw != null) {
                            storeSession(tokenFromRaw, authResponse?.user)
                            return@withContext AuthResult.Success(tokenFromRaw, authResponse?.user)
                        }
                    }
                }
                
                // Si la connexion échoue, retourner au moins l'utilisateur créé
                val registerResult = registerResponse.body()
                if (registerResult != null) {
                    val userDto = UserDto(
                        id = registerResult._id ?: registerResult.id ?: "",
                        email = registerResult.email,
                        name = registerResult.name,
                        location = registerResult.location
                    )
                    android.util.Log.w("AuthRepository", "User created but login failed. User needs to set a password.")
                    storeSession("", userDto)
                    return@withContext AuthResult.Success("", userDto) // Token vide mais utilisateur créé
                }
            } else {
                val errorBody = registerResponse.errorBody()?.string()
                val errorCode = registerResponse.code()
                android.util.Log.e("AuthRepository", "Register error: $errorCode - $errorBody")
                
                // Si l'utilisateur existe déjà (409 Conflict), essayer de se connecter
                if (errorCode == 409) {
                    android.util.Log.d("AuthRepository", "User already exists, trying to login with fallback password...")

                    val loginResponse = apiService.login(
                        LoginDto(email = email, password = fallbackPassword)
                    )

                    if (loginResponse.isSuccessful) {
                        val rawBodyString = ResponseBodyInterceptor.getRawResponseBody()
                        val authResponse = loginResponse.body()
                        ResponseBodyInterceptor.clearRawResponseBody()

                        val token = authResponse?.access_token ?: authResponse?.token

                        if (token != null) {
                            socialPasswordStore.savePassword("facebook", email, fallbackPassword)
                            storeSession(token, authResponse?.user)
                            return@withContext AuthResult.Success(token, authResponse?.user)
                        } else if (rawBodyString != null) {
                            val tokenFromRaw = extractTokenFromJson(rawBodyString)
                            if (tokenFromRaw != null) {
                                socialPasswordStore.savePassword("facebook", email, fallbackPassword)
                                storeSession(tokenFromRaw, authResponse?.user)
                                return@withContext AuthResult.Success(tokenFromRaw, authResponse?.user)
                            }
                        }
                    } else {
                        val loginError = loginResponse.errorBody()?.string()
                        android.util.Log.e("AuthRepository", "Fallback login error after 409: ${loginResponse.code()} - $loginError")
                    }

                    return@withContext AuthResult.Error(
                        "Account already exists. If the login keeps failing, please ask the backend team to enable /auth/facebook."
                    )
                }
                
                val errorMessage = parseError(errorCode, errorBody)
                return@withContext AuthResult.Error(errorMessage)
            }
            
            AuthResult.Error("Failed to register or login with Facebook. Please create the /auth/facebook endpoint in your backend for better integration.")
            
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("AuthRepository", "HttpException Facebook login: ${e.code()} - $errorBody")
            val errorMessage = parseError(e.code(), errorBody)
            AuthResult.Error(errorMessage)
        } catch (e: IOException) {
            android.util.Log.e("AuthRepository", "IOException Facebook login: ${e.message}")
            AuthResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Exception Facebook login: ${e.message}", e)
            AuthResult.Error("Unexpected error: ${e.message}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
    
    private fun generateDeterministicSocialPassword(provider: String, email: String): String {
        return try {
            val secretInput = "${provider.lowercase()}|${email.lowercase()}|damandroid_social_secret"
            val digest = MessageDigest.getInstance("SHA-256").digest(secretInput.toByteArray())
            digest.joinToString(separator = "") { byte -> "%02x".format(byte) }.take(32)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error generating deterministic password: ${e.message}")
            // Fallback minimal password in case of unforeseen error
            "${provider.lowercase()}_${email.hashCode()}_social"
        }
    }
    
    private fun parseError(code: Int, errorBody: String?): String {
        return try {
            if (!errorBody.isNullOrEmpty()) {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: errorResponse.error ?: "Error $code"
            } else {
                when (code) {
                    400 -> "Invalid request"
                    401 -> "Invalid credentials"
                    404 -> "Service not found"
                    500 -> "Server error"
                    else -> "Error $code"
                }
            }
        } catch (e: Exception) {
            "Error $code"
        }
    }
}

