package com.example.damandroid.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class AIMatchmakerChatResult {
    data class Success(val response: AIMatchmakerChatResponse) : AIMatchmakerChatResult()
    data class Error(val message: String) : AIMatchmakerChatResult()
}

class AIMatchmakerRepository {
    private val apiService = RetrofitClient.aiMatchmakerApiService
    
    /**
     * Envoyer un message à l'IA et recevoir une réponse
     */
    suspend fun sendMessage(
        message: String,
        conversationHistory: List<ChatMessageDto>? = null
    ): AIMatchmakerChatResult = withContext(Dispatchers.IO) {
        try {
            val request = AIMatchmakerChatRequest(
                message = message,
                conversationHistory = conversationHistory
            )
            
            val response = apiService.sendMessage(request)
            
            if (response.isSuccessful) {
                val chatResponse = response.body()
                if (chatResponse != null) {
                    AIMatchmakerChatResult.Success(chatResponse)
                } else {
                    AIMatchmakerChatResult.Error("Réponse vide du serveur")
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Non autorisé. Veuillez vous reconnecter."
                    403 -> "Accès refusé."
                    404 -> "Service non trouvé."
                    429 -> "Quota API dépassé. Le service AI est temporairement indisponible. Veuillez réessayer plus tard."
                    500 -> "Erreur serveur. Veuillez réessayer plus tard."
                    else -> "Erreur: ${response.code()}"
                }
                AIMatchmakerChatResult.Error(errorMessage)
            }
        } catch (e: HttpException) {
            android.util.Log.e("AIMatchmakerRepository", "HttpException: ${e.code()}", e)
            val errorMessage = when (e.code()) {
                429 -> "Quota API dépassé. Le service AI est temporairement indisponible. Veuillez réessayer plus tard."
                401 -> "Non autorisé. Veuillez vous reconnecter."
                403 -> "Accès refusé."
                404 -> "Service non trouvé."
                500 -> "Erreur serveur. Veuillez réessayer plus tard."
                else -> "Erreur HTTP: ${e.code()}"
            }
            AIMatchmakerChatResult.Error(errorMessage)
        } catch (e: IOException) {
            android.util.Log.e("AIMatchmakerRepository", "IOException: ${e.message}", e)
            AIMatchmakerChatResult.Error("Erreur de connexion. Vérifiez votre connexion internet.")
        } catch (e: Exception) {
            android.util.Log.e("AIMatchmakerRepository", "Exception: ${e.message}", e)
            AIMatchmakerChatResult.Error("Erreur inattendue: ${e.message ?: "Erreur inconnue"}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
}

