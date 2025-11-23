package com.example.damandroid.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ActivityRoomRepository {
    private val apiService = RetrofitClient.activityApiService
    
    sealed class ActivityRoomResult<out T> {
        data class Success<out T>(val data: T) : ActivityRoomResult<T>()
        data class Error(val message: String) : ActivityRoomResult<Nothing>()
    }
    
    suspend fun getActivity(activityId: String): ActivityRoomResult<com.example.damandroid.api.ActivityResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getActivity(activityId)
            if (response.isSuccessful) {
                response.body()?.let { ActivityRoomResult.Success(it) } ?: ActivityRoomResult.Error("Empty response")
            } else {
                ActivityRoomResult.Error("Failed to get activity: ${response.code()}")
            }
        } catch (e: HttpException) {
            ActivityRoomResult.Error("HTTP Error: ${e.message()}")
        } catch (e: IOException) {
            ActivityRoomResult.Error("Network Error: ${e.message}")
        } catch (e: Exception) {
            ActivityRoomResult.Error("Unknown Error: ${e.message}")
        }
    }
    
    /**
     * Rejoindre une activité
     */
    suspend fun joinActivity(activityId: String): ActivityRoomResult<JoinActivityResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.joinActivity(activityId)
            when {
                response.isSuccessful && response.body() != null -> {
                    ActivityRoomResult.Success(response.body()!!)
                }
                response.code() == 400 -> {
                    // L'utilisateur est peut-être déjà participant, on continue quand même
                    // Le backend devrait retourner un succès dans ce cas, mais on gère l'erreur 400
                    ActivityRoomResult.Error("Vous êtes déjà participant de cette activité")
                }
                response.code() == 403 -> {
                    ActivityRoomResult.Error("Vous n'êtes pas autorisé à rejoindre cette activité")
                }
                response.code() == 404 -> {
                    ActivityRoomResult.Error("Activité non trouvée")
                }
                else -> {
                    val errorMessage = when (response.code()) {
                        400 -> "Impossible de rejoindre l'activité"
                        401 -> "Non autorisé. Veuillez vous reconnecter."
                        403 -> "Vous n'êtes pas autorisé à rejoindre cette activité"
                        404 -> "Activité non trouvée"
                        else -> "Erreur: ${response.code()}"
                    }
                    ActivityRoomResult.Error(errorMessage)
                }
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Impossible de rejoindre l'activité (peut-être déjà participant)"
                401 -> "Non autorisé. Veuillez vous reconnecter."
                403 -> "Vous n'êtes pas autorisé à rejoindre cette activité"
                404 -> "Activité non trouvée"
                else -> "Erreur HTTP: ${e.code()}"
            }
            ActivityRoomResult.Error(errorMessage)
        } catch (e: IOException) {
            ActivityRoomResult.Error("Erreur de connexion. Vérifiez votre connexion internet.")
        } catch (e: Exception) {
            ActivityRoomResult.Error("Erreur: ${e.message ?: "Erreur inconnue"}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
    
    /**
     * Récupérer les messages de chat
     */
    suspend fun getMessages(activityId: String): ActivityRoomResult<List<ActivityMessageDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getActivityMessages(activityId)
            if (response.isSuccessful && response.body() != null) {
                ActivityRoomResult.Success(response.body()!!.messages)
            } else {
                ActivityRoomResult.Error("Erreur: ${response.code()}")
            }
        } catch (e: HttpException) {
            ActivityRoomResult.Error("Erreur HTTP: ${e.code()}")
        } catch (e: IOException) {
            ActivityRoomResult.Error("Erreur de connexion")
        } catch (e: Exception) {
            ActivityRoomResult.Error("Erreur: ${e.message ?: "Erreur inconnue"}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
    
    /**
     * Envoyer un message
     */
    suspend fun sendMessage(activityId: String, content: String): ActivityRoomResult<ActivityMessageDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendActivityMessage(activityId, ActivitySendMessageRequest(content))
            if (response.isSuccessful && response.body() != null) {
                ActivityRoomResult.Success(response.body()!!)
            } else {
                ActivityRoomResult.Error("Erreur: ${response.code()}")
            }
        } catch (e: HttpException) {
            ActivityRoomResult.Error("Erreur HTTP: ${e.code()}")
        } catch (e: IOException) {
            ActivityRoomResult.Error("Erreur de connexion")
        } catch (e: Exception) {
            ActivityRoomResult.Error("Erreur: ${e.message ?: "Erreur inconnue"}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
    
    /**
     * Récupérer les participants
     */
    suspend fun getParticipants(activityId: String): ActivityRoomResult<List<ActivityParticipantDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getActivityParticipants(activityId)
            if (response.isSuccessful && response.body() != null) {
                ActivityRoomResult.Success(response.body()!!.participants)
            } else {
                ActivityRoomResult.Error("Erreur: ${response.code()}")
            }
        } catch (e: HttpException) {
            ActivityRoomResult.Error("Erreur HTTP: ${e.code()}")
        } catch (e: IOException) {
            ActivityRoomResult.Error("Erreur de connexion")
        } catch (e: Exception) {
            ActivityRoomResult.Error("Erreur: ${e.message ?: "Erreur inconnue"}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
    
    /**
     * Quitter une activité
     */
    suspend fun leaveActivity(activityId: String): ActivityRoomResult<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.leaveActivity(activityId)
            if (response.isSuccessful && response.body() != null) {
                ActivityRoomResult.Success(response.body()!!.message)
            } else {
                ActivityRoomResult.Error("Erreur: ${response.code()}")
            }
        } catch (e: HttpException) {
            ActivityRoomResult.Error("Erreur HTTP: ${e.code()}")
        } catch (e: IOException) {
            ActivityRoomResult.Error("Erreur de connexion")
        } catch (e: Exception) {
            ActivityRoomResult.Error("Erreur: ${e.message ?: "Erreur inconnue"}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
    
    /**
     * Marquer comme complété
     */
    suspend fun completeActivity(activityId: String): ActivityRoomResult<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.completeActivity(activityId)
            if (response.isSuccessful && response.body() != null) {
                ActivityRoomResult.Success(response.body()!!.message)
            } else {
                ActivityRoomResult.Error("Erreur: ${response.code()}")
            }
        } catch (e: HttpException) {
            ActivityRoomResult.Error("Erreur HTTP: ${e.code()}")
        } catch (e: IOException) {
            ActivityRoomResult.Error("Erreur de connexion")
        } catch (e: Exception) {
            ActivityRoomResult.Error("Erreur: ${e.message ?: "Erreur inconnue"}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }

    /**
     * Créer ou récupérer le chat de groupe d'une activité
     */
    suspend fun createOrGetActivityGroupChat(activityId: String): ActivityRoomResult<ActivityGroupChatResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createOrGetActivityGroupChat(activityId)
            if (response.isSuccessful) {
                response.body()?.let { ActivityRoomResult.Success(it) } ?: ActivityRoomResult.Error("Empty response")
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Activité invalide ou pas de participants"
                    401 -> "Non autorisé. Veuillez vous reconnecter."
                    403 -> "Vous devez être participant de l'activité pour accéder au chat"
                    404 -> "Activité non trouvée"
                    else -> "Erreur: ${response.code()}"
                }
                ActivityRoomResult.Error(errorMessage)
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Activité invalide ou pas de participants"
                401 -> "Non autorisé. Veuillez vous reconnecter."
                403 -> "Vous devez être participant de l'activité pour accéder au chat"
                404 -> "Activité non trouvée"
                else -> "Erreur HTTP: ${e.code()}"
            }
            ActivityRoomResult.Error(errorMessage)
        } catch (e: IOException) {
            ActivityRoomResult.Error("Erreur de connexion. Vérifiez votre connexion internet.")
        } catch (e: Exception) {
            ActivityRoomResult.Error("Erreur inattendue: ${e.message ?: "Erreur inconnue"}")
        } finally {
            ResponseBodyInterceptor.clearRawResponseBody()
        }
    }
}

