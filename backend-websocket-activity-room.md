# Backend NestJS - WebSocket pour Activity Room (Optionnel)

## Vue d'ensemble

Ce document décrit comment implémenter un système WebSocket pour les messages en temps réel dans les Activity Rooms. WebSocket est plus efficace que le polling car il permet une communication bidirectionnelle en temps réel sans requêtes HTTP répétées.

## Avantages de WebSocket vs Polling

- **Efficacité** : Pas de requêtes HTTP répétées, connexion persistante
- **Temps réel** : Messages instantanés sans délai
- **Moins de charge serveur** : Pas de polling constant
- **Bidirectionnel** : Le serveur peut pousser des notifications

## Implémentation NestJS

### 1. Installation des dépendances

```bash
npm install @nestjs/websockets @nestjs/platform-socket.io socket.io
```

### 2. Gateway WebSocket

Créer `src/activities/activity-room.gateway.ts` :

```typescript
import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  OnGatewayConnection,
  OnGatewayDisconnect,
  MessageBody,
  ConnectedSocket,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { UseGuards } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { ActivityMessagesService } from './activity-messages.service';

@WebSocketGateway({
  cors: {
    origin: '*', // Configurer selon vos besoins
  },
  namespace: '/activity-room',
})
export class ActivityRoomGateway
  implements OnGatewayConnection, OnGatewayDisconnect
{
  @WebSocketServer()
  server: Server;

  private connectedUsers = new Map<string, Set<string>>(); // activityId -> Set of socketIds

  constructor(
    private jwtService: JwtService,
    private activityMessagesService: ActivityMessagesService,
  ) {}

  async handleConnection(client: Socket) {
    try {
      // Authentifier l'utilisateur via le token JWT
      const token = client.handshake.auth.token || client.handshake.headers.authorization?.replace('Bearer ', '');
      
      if (!token) {
        client.disconnect();
        return;
      }

      const payload = await this.jwtService.verifyAsync(token);
      client.data.userId = payload.sub || payload.userId;
      client.data.userEmail = payload.email;

      console.log(`Client connected: ${client.id}, User: ${client.data.userId}`);
    } catch (error) {
      console.error('WebSocket authentication failed:', error);
      client.disconnect();
    }
  }

  handleDisconnect(client: Socket) {
    // Retirer l'utilisateur de toutes les salles
    this.connectedUsers.forEach((users, activityId) => {
      users.delete(client.id);
      if (users.size === 0) {
        this.connectedUsers.delete(activityId);
      }
    });
    console.log(`Client disconnected: ${client.id}`);
  }

  @SubscribeMessage('join-activity')
  handleJoinActivity(
    @MessageBody() data: { activityId: string },
    @ConnectedSocket() client: Socket,
  ) {
    const { activityId } = data;
    const userId = client.data.userId;

    if (!activityId || !userId) {
      return { error: 'Missing activityId or userId' };
    }

    // Rejoindre la room Socket.IO pour cette activité
    client.join(`activity:${activityId}`);

    // Enregistrer la connexion
    if (!this.connectedUsers.has(activityId)) {
      this.connectedUsers.set(activityId, new Set());
    }
    this.connectedUsers.get(activityId)!.add(client.id);

    console.log(`User ${userId} joined activity ${activityId}`);

    // Notifier les autres utilisateurs (optionnel)
    client.to(`activity:${activityId}`).emit('user-joined', {
      userId,
      activityId,
    });

    return { success: true, activityId };
  }

  @SubscribeMessage('leave-activity')
  handleLeaveActivity(
    @MessageBody() data: { activityId: string },
    @ConnectedSocket() client: Socket,
  ) {
    const { activityId } = data;
    const userId = client.data.userId;

    client.leave(`activity:${activityId}`);

    if (this.connectedUsers.has(activityId)) {
      this.connectedUsers.get(activityId)!.delete(client.id);
      if (this.connectedUsers.get(activityId)!.size === 0) {
        this.connectedUsers.delete(activityId);
      }
    }

    client.to(`activity:${activityId}`).emit('user-left', {
      userId,
      activityId,
    });

    return { success: true };
  }

  @SubscribeMessage('send-message')
  async handleSendMessage(
    @MessageBody() data: { activityId: string; content: string },
    @ConnectedSocket() client: Socket,
  ) {
    const { activityId, content } = data;
    const userId = client.data.userId;

    if (!activityId || !content || !userId) {
      return { error: 'Missing required fields' };
    }

    try {
      // Sauvegarder le message dans la base de données
      const message = await this.activityMessagesService.sendMessage(
        activityId,
        userId,
        content,
      );

      // Diffuser le message à tous les utilisateurs dans la room
      this.server.to(`activity:${activityId}`).emit('new-message', {
        _id: message._id,
        activity: message.activity,
        sender: {
          _id: message.sender._id,
          name: message.sender.name,
          profileImageUrl: message.sender.profileImageUrl,
        },
        content: message.content,
        createdAt: message.createdAt,
      });

      return { success: true, message };
    } catch (error) {
      console.error('Error sending message:', error);
      return { error: 'Failed to send message' };
    }
  }

  @SubscribeMessage('typing')
  handleTyping(
    @MessageBody() data: { activityId: string; isTyping: boolean },
    @ConnectedSocket() client: Socket,
  ) {
    const { activityId, isTyping } = data;
    const userId = client.data.userId;

    // Diffuser l'état de frappe aux autres utilisateurs
    client.to(`activity:${activityId}`).emit('user-typing', {
      userId,
      isTyping,
    });
  }
}
```

### 3. Enregistrer le Gateway dans le Module

Dans `src/activities/activities.module.ts` :

```typescript
import { Module } from '@nestjs/common';
import { ActivitiesController } from './activities.controller';
import { ActivitiesService } from './activities.service';
import { ActivityMessagesService } from './activity-messages.service';
import { ActivityRoomGateway } from './activity-room.gateway';
import { MongooseModule } from '@nestjs/mongoose';
import { Activity, ActivitySchema } from './schemas/activity.schema';
import { ActivityMessage, ActivityMessageSchema } from './schemas/activity-message.schema';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Activity.name, schema: ActivitySchema },
      { name: ActivityMessage.name, schema: ActivityMessageSchema },
    ]),
  ],
  controllers: [ActivitiesController],
  providers: [ActivitiesService, ActivityMessagesService, ActivityRoomGateway],
  exports: [ActivitiesService, ActivityMessagesService],
})
export class ActivitiesModule {}
```

### 4. Configuration CORS pour WebSocket

Dans `main.ts` :

```typescript
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  
  // CORS pour HTTP
  app.enableCors({
    origin: '*', // Configurer selon vos besoins
    credentials: true,
  });

  await app.listen(process.env.PORT || 3000);
}
bootstrap();
```

## Client Android - Implémentation WebSocket

### 1. Ajouter la dépendance Socket.IO

Dans `app/build.gradle.kts` :

```kotlin
dependencies {
    // ... autres dépendances
    implementation("io.socket:socket.io-client:2.1.0")
}
```

### 2. Service WebSocket

Créer `app/src/main/java/com/example/damandroid/api/ActivityRoomWebSocketService.kt` :

```kotlin
package com.example.damandroid.api

import android.util.Log
import com.example.damandroid.data.UserSession
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject

class ActivityRoomWebSocketService {
    private var socket: Socket? = null
    private val _messages = MutableSharedFlow<ActivityMessageDto>()
    val messages: SharedFlow<ActivityMessageDto> = _messages
    
    private val _connectionState = MutableSharedFlow<Boolean>()
    val connectionState: SharedFlow<Boolean> = _connectionState

    fun connect(activityId: String) {
        if (socket?.connected() == true) {
            return
        }

        try {
            val token = UserSession.getToken()
            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
            }

            socket = IO.socket("https://apinest-production.up.railway.app/activity-room", options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("WebSocket", "Connected")
                _connectionState.tryEmit(true)
                joinActivity(activityId)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("WebSocket", "Disconnected")
                _connectionState.tryEmit(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("WebSocket", "Connection error: ${args[0]}")
                _connectionState.tryEmit(false)
            }

            socket?.on("new-message") { args ->
                try {
                    val messageJson = args[0] as? JSONObject
                    if (messageJson != null) {
                        val message = parseMessage(messageJson)
                        _messages.tryEmit(message)
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Error parsing message: ${e.message}", e)
                }
            }

            socket?.on("user-typing") { args ->
                // Gérer l'indicateur de frappe
                val data = args[0] as? JSONObject
                // Émettre un événement si nécessaire
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e("WebSocket", "Failed to connect: ${e.message}", e)
            _connectionState.tryEmit(false)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun joinActivity(activityId: String) {
        socket?.emit("join-activity", JSONObject().apply {
            put("activityId", activityId)
        })
    }

    fun leaveActivity(activityId: String) {
        socket?.emit("leave-activity", JSONObject().apply {
            put("activityId", activityId)
        })
    }

    fun sendMessage(activityId: String, content: String) {
        socket?.emit("send-message", JSONObject().apply {
            put("activityId", activityId)
            put("content", content)
        })
    }

    fun setTyping(activityId: String, isTyping: Boolean) {
        socket?.emit("typing", JSONObject().apply {
            put("activityId", activityId)
            put("isTyping", isTyping)
        })
    }

    private fun parseMessage(json: JSONObject): ActivityMessageDto {
        val senderJson = json.optJSONObject("sender")
        val sender = if (senderJson != null) {
            ActivityMessageSender(
                _id = senderJson.optString("_id"),
                id = senderJson.optString("id"),
                name = senderJson.optString("name"),
                profileImageUrl = senderJson.optString("profileImageUrl")
            )
        } else null

        return ActivityMessageDto(
            _id = json.optString("_id"),
            id = json.optString("id"),
            activity = json.optString("activity"),
            sender = sender,
            content = json.optString("content"),
            createdAt = json.optString("createdAt")
        )
    }
}
```

### 3. Intégrer dans le ViewModel

Dans `ActivityRoomViewModel.kt`, ajouter :

```kotlin
private val webSocketService = ActivityRoomWebSocketService()

init {
    loadData()
    // Utiliser WebSocket au lieu de polling
    connectWebSocket()
}

private fun connectWebSocket() {
    viewModelScope.launch {
        webSocketService.connect(activityId)
        
        // Écouter les nouveaux messages
        webSocketService.messages.collect { message ->
            val chatMessage = convertToChatMessage(message)
            _uiState.update { state ->
                val messageExists = state.messages.any { it.id == chatMessage.id }
                if (!messageExists) {
                    state.copy(messages = state.messages + chatMessage)
                } else {
                    state
                }
            }
        }
    }
}

override fun onCleared() {
    super.onCleared()
    webSocketService.disconnect()
}

fun sendMessage(content: String) {
    // Envoyer via WebSocket
    webSocketService.sendMessage(activityId, content)
    // Le message sera reçu via le flux WebSocket
}
```

## Comparaison Polling vs WebSocket

| Critère | Polling | WebSocket |
|---------|---------|-----------|
| **Simplicité** | ✅ Très simple | ⚠️ Plus complexe |
| **Temps réel** | ⚠️ Délai (3-5s) | ✅ Instantané |
| **Charge serveur** | ⚠️ Requêtes constantes | ✅ Connexion persistante |
| **Batterie** | ⚠️ Consomme plus | ✅ Plus efficace |
| **Backend requis** | ✅ Aucun changement | ⚠️ Nécessite WebSocket |
| **Maintenance** | ✅ Facile | ⚠️ Plus complexe |

## Recommandation

- **Pour commencer** : Utiliser le polling (déjà implémenté)
- **Pour la production** : Migrer vers WebSocket pour une meilleure expérience utilisateur

