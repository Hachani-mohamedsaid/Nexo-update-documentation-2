# Backend NestJS - WebSocket pour Chats Individuels

## Vue d'ensemble

Ce document décrit comment implémenter un système WebSocket pour les messages en temps réel dans les chats individuels. Ce WebSocket remplace le polling précédent (requêtes toutes les 3 secondes) pour offrir une communication bidirectionnelle instantanée.

## Avantages de WebSocket vs Polling

- **Efficacité** : Pas de requêtes HTTP répétées, connexion persistante
- **Temps réel** : Messages instantanés sans délai (vs 0-3 secondes de polling)
- **Moins de charge serveur** : Pas de polling constant
- **Bidirectionnel** : Le serveur peut pousser des notifications
- **Indicateurs de frappe** : Support natif pour "user is typing"

## Implémentation NestJS

### 1. Installation des dépendances

```bash
npm install @nestjs/websockets @nestjs/platform-socket.io socket.io
```

### 2. Gateway WebSocket

Créer `src/chats/chats.gateway.ts` :

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
import { UseGuards, Logger } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { ChatsService } from './chats.service';
import { MessagesService } from './messages.service';

@WebSocketGateway({
  cors: {
    origin: '*', // Configurer selon vos besoins
  },
  namespace: '/chats',
})
export class ChatsGateway
  implements OnGatewayConnection, OnGatewayDisconnect
{
  @WebSocketServer()
  server: Server;

  private readonly logger = new Logger(ChatsGateway.name);
  private readonly userSockets = new Map<string, Set<string>>(); // userId -> Set of socketIds
  private readonly chatSockets = new Map<string, Set<string>>(); // chatId -> Set of socketIds

  constructor(
    private readonly jwtService: JwtService,
    private readonly chatsService: ChatsService,
    private readonly messagesService: MessagesService,
  ) {}

  async handleConnection(client: Socket) {
    try {
      // Extraire le token depuis auth.token
      const token = client.handshake.auth?.token;
      
      if (!token) {
        this.logger.warn(`Connection rejected: No token provided for socket ${client.id}`);
        client.disconnect();
        return;
      }

      // Vérifier et décoder le JWT
      const payload = this.jwtService.verify(token, {
        secret: process.env.JWT_SECRET,
      });

      const userId = payload.sub || payload.id;
      
      if (!userId) {
        this.logger.warn(`Connection rejected: Invalid token for socket ${client.id}`);
        client.disconnect();
        return;
      }

      // Stocker l'association socketId -> userId
      (client as any).userId = userId;

      // Ajouter le socket à la map des sockets de l'utilisateur
      if (!this.userSockets.has(userId)) {
        this.userSockets.set(userId, new Set());
      }
      this.userSockets.get(userId)!.add(client.id);

      this.logger.log(`User ${userId} connected (socket ${client.id})`);
    } catch (error) {
      this.logger.error(`Connection error: ${error.message}`);
      client.disconnect();
    }
  }

  async handleDisconnect(client: Socket) {
    const userId = (client as any).userId;
    
    if (userId) {
      // Retirer le socket de la map des sockets de l'utilisateur
      const userSockets = this.userSockets.get(userId);
      if (userSockets) {
        userSockets.delete(client.id);
        if (userSockets.size === 0) {
          this.userSockets.delete(userId);
        }
      }

      // Retirer le socket de tous les chats
      for (const [chatId, sockets] of this.chatSockets.entries()) {
        sockets.delete(client.id);
        if (sockets.size === 0) {
          this.chatSockets.delete(chatId);
        }
      }

      this.logger.log(`User ${userId} disconnected (socket ${client.id})`);
    }
  }

  /**
   * Rejoindre un chat
   */
  @SubscribeMessage('join-chat')
  async handleJoinChat(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { chatId: string },
  ) {
    const userId = (client as any).userId;
    const { chatId } = data;

    if (!userId || !chatId) {
      return { success: false, error: 'Missing userId or chatId' };
    }

    try {
      // Vérifier que l'utilisateur a accès au chat
      const hasAccess = await this.chatsService.userHasAccessToChat(
        userId,
        chatId,
      );

      if (!hasAccess) {
        return { success: false, error: 'Access denied' };
      }

      // Rejoindre la room du chat
      client.join(`chat:${chatId}`);

      // Ajouter le socket à la map des sockets du chat
      if (!this.chatSockets.has(chatId)) {
        this.chatSockets.set(chatId, new Set());
      }
      this.chatSockets.get(chatId)!.add(client.id);

      this.logger.log(`User ${userId} joined chat ${chatId}`);

      return { success: true, chatId };
    } catch (error) {
      this.logger.error(`Error joining chat: ${error.message}`);
      return { success: false, error: error.message };
    }
  }

  /**
   * Quitter un chat
   */
  @SubscribeMessage('leave-chat')
  async handleLeaveChat(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { chatId: string },
  ) {
    const userId = (client as any).userId;
    const { chatId } = data;

    if (!userId || !chatId) {
      return { success: false, error: 'Missing userId or chatId' };
    }

    // Quitter la room du chat
    client.leave(`chat:${chatId}`);

    // Retirer le socket de la map des sockets du chat
    const chatSockets = this.chatSockets.get(chatId);
    if (chatSockets) {
      chatSockets.delete(client.id);
      if (chatSockets.size === 0) {
        this.chatSockets.delete(chatId);
      }
    }

    this.logger.log(`User ${userId} left chat ${chatId}`);

    return { success: true, chatId };
  }

  /**
   * Envoyer un message
   */
  @SubscribeMessage('send-message')
  async handleSendMessage(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { chatId: string; text: string },
  ) {
    const userId = (client as any).userId;
    const { chatId, text } = data;

    if (!userId || !chatId || !text) {
      return { success: false, error: 'Missing required fields' };
    }

    try {
      // Vérifier l'accès au chat
      const hasAccess = await this.chatsService.userHasAccessToChat(
        userId,
        chatId,
      );

      if (!hasAccess) {
        return { success: false, error: 'Access denied' };
      }

      // Créer le message via le service
      const message = await this.messagesService.createMessage({
        chatId,
        senderId: userId,
        text,
      });

      // Diffuser le message à tous les participants du chat
      this.server.to(`chat:${chatId}`).emit('new-message', {
        _id: message._id,
        id: message._id,
        chat: chatId,
        sender: {
          _id: message.sender._id || message.sender.id,
          id: message.sender._id || message.sender.id,
          name: message.sender.name,
          email: message.sender.email,
          profileImageUrl: message.sender.profileImageUrl,
          avatar: message.sender.profileImageUrl,
        },
        text: message.text,
        content: message.text,
        createdAt: message.createdAt,
        timestamp: message.createdAt,
      });

      this.logger.log(`Message sent in chat ${chatId} by user ${userId}`);

      return { success: true, message };
    } catch (error) {
      this.logger.error(`Error sending message: ${error.message}`);
      return { success: false, error: error.message };
    }
  }

  /**
   * Indicateur de frappe
   */
  @SubscribeMessage('typing')
  async handleTyping(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { chatId: string; isTyping: boolean },
  ) {
    const userId = (client as any).userId;
    const { chatId, isTyping } = data;

    if (!userId || !chatId) {
      return { success: false, error: 'Missing required fields' };
    }

    try {
      // Vérifier l'accès au chat
      const hasAccess = await this.chatsService.userHasAccessToChat(
        userId,
        chatId,
      );

      if (!hasAccess) {
        return { success: false, error: 'Access denied' };
      }

      // Diffuser l'indicateur de frappe à tous les autres participants
      client.to(`chat:${chatId}`).emit('user-typing', {
        userId,
        chatId,
        isTyping,
      });

      return { success: true };
    } catch (error) {
      this.logger.error(`Error handling typing: ${error.message}`);
      return { success: false, error: error.message };
    }
  }

  /**
   * Marquer les messages comme lus
   */
  @SubscribeMessage('mark-read')
  async handleMarkRead(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { chatId: string },
  ) {
    const userId = (client as any).userId;
    const { chatId } = data;

    if (!userId || !chatId) {
      return { success: false, error: 'Missing required fields' };
    }

    try {
      // Vérifier l'accès au chat
      const hasAccess = await this.chatsService.userHasAccessToChat(
        userId,
        chatId,
      );

      if (!hasAccess) {
        return { success: false, error: 'Access denied' };
      }

      // Marquer les messages comme lus via le service
      await this.chatsService.markChatAsRead(userId, chatId);

      // Notifier les autres participants que les messages ont été lus
      client.to(`chat:${chatId}`).emit('message-read', {
        userId,
        chatId,
        readAt: new Date().toISOString(),
      });

      this.logger.log(`Chat ${chatId} marked as read by user ${userId}`);

      return { success: true };
    } catch (error) {
      this.logger.error(`Error marking chat as read: ${error.message}`);
      return { success: false, error: error.message };
    }
  }

  /**
   * Diffuser un nouveau message (appelé depuis le service après création)
   */
  broadcastNewMessage(chatId: string, message: any) {
    this.server.to(`chat:${chatId}`).emit('new-message', {
      _id: message._id,
      id: message._id,
      chat: chatId,
      sender: {
        _id: message.sender._id || message.sender.id,
        id: message.sender._id || message.sender.id,
        name: message.sender.name,
        email: message.sender.email,
        profileImageUrl: message.sender.profileImageUrl,
        avatar: message.sender.profileImageUrl,
      },
      text: message.text,
      content: message.text,
      createdAt: message.createdAt,
      timestamp: message.createdAt,
    });
  }
}
```

### 3. Mettre à jour le Module

Ajouter le Gateway au module `src/chats/chats.module.ts` :

```typescript
import { Module } from '@nestjs/common';
import { ChatsGateway } from './chats.gateway';
import { ChatsService } from './chats.service';
import { MessagesService } from './messages.service';
import { JwtModule } from '@nestjs/jwt';

@Module({
  imports: [JwtModule],
  providers: [ChatsGateway, ChatsService, MessagesService],
  exports: [ChatsGateway, ChatsService],
})
export class ChatsModule {}
```

### 4. Intégration avec le service de messages

Modifier `src/chats/messages.service.ts` pour diffuser via WebSocket :

```typescript
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { ChatsGateway } from './chats.gateway';

@Injectable()
export class MessagesService {
  constructor(
    @InjectModel('Message') private messageModel: Model<any>,
    private readonly chatsGateway: ChatsGateway,
  ) {}

  async createMessage(data: { chatId: string; senderId: string; text: string }) {
    // Créer le message
    const message = await this.messageModel.create({
      chat: data.chatId,
      sender: data.senderId,
      text: data.text,
      createdAt: new Date(),
    });

    // Populate le sender
    await message.populate('sender', 'name email profileImageUrl');

    // Diffuser via WebSocket
    this.chatsGateway.broadcastNewMessage(data.chatId, message);

    return message;
  }
}
```

### 5. Méthode helper dans ChatsService

Ajouter une méthode pour vérifier l'accès au chat dans `src/chats/chats.service.ts` :

```typescript
async userHasAccessToChat(userId: string, chatId: string): Promise<boolean> {
  try {
    const chat = await this.chatModel.findById(chatId);
    
    if (!chat) {
      return false;
    }

    // Vérifier si l'utilisateur est un participant
    const isParticipant = chat.participants.some(
      (p: any) => p._id?.toString() === userId || p.toString() === userId,
    );

    return isParticipant;
  } catch (error) {
    this.logger.error(`Error checking chat access: ${error.message}`);
    return false;
  }
}
```

## Structure des événements WebSocket

### Événements émis par le client (Android)

| Événement | Paramètres | Description |
|-----------|-----------|-------------|
| `join-chat` | `{ chatId: string }` | Rejoindre un chat |
| `leave-chat` | `{ chatId: string }` | Quitter un chat |
| `send-message` | `{ chatId: string, text: string }` | Envoyer un message |
| `typing` | `{ chatId: string, isTyping: boolean }` | Indicateur de frappe |
| `mark-read` | `{ chatId: string }` | Marquer comme lu |

### Événements émis par le serveur

| Événement | Données | Description |
|-----------|---------|-------------|
| `new-message` | Message object | Nouveau message reçu |
| `user-typing` | `{ userId: string, chatId: string, isTyping: boolean }` | Utilisateur en train de taper |
| `message-read` | `{ userId: string, chatId: string, readAt: string }` | Message lu |
| `user-joined` | `{ userId: string, chatId: string }` | Utilisateur rejoint |
| `user-left` | `{ userId: string, chatId: string }` | Utilisateur quitte |

## Format des messages

### Format d'un message émis par `new-message`

```json
{
  "_id": "message_id",
  "id": "message_id",
  "chat": "chat_id",
  "sender": {
    "_id": "user_id",
    "id": "user_id",
    "name": "John Doe",
    "email": "john@example.com",
    "profileImageUrl": "https://...",
    "avatar": "https://..."
  },
  "text": "Message content",
  "content": "Message content",
  "createdAt": "2025-11-22T04:00:00.000Z",
  "timestamp": "2025-11-22T04:00:00.000Z"
}
```

## Authentification

Le token JWT est envoyé via `auth.token` dans les options de connexion Socket.IO :

```typescript
const options = IO.Options().apply {
    auth = mapOf("token" to token)
    // ...
}
```

Le backend doit :
1. Extraire le token depuis `client.handshake.auth.token`
2. Vérifier et décoder le JWT
3. Stocker `userId` dans `(client as any).userId`
4. Rejeter la connexion si le token est invalide

## Gestion des rooms

Les sockets rejoignent des rooms Socket.IO pour chaque chat :
- Room format : `chat:${chatId}`
- Permet de diffuser les messages uniquement aux participants du chat
- Utilisation : `client.join('chat:chatId')` et `this.server.to('chat:chatId').emit(...)`

## Sécurité

1. **Vérification du token JWT** : Toute connexion nécessite un token valide
2. **Vérification d'accès au chat** : Avant de rejoindre/envoyer, vérifier que l'utilisateur est participant
3. **Isolation des rooms** : Les messages ne sont diffusés qu'aux participants du chat

## Tests

### Tester la connexion

```bash
# Connexion au WebSocket avec token
socket = io('https://apinest-production.up.railway.app/chats', {
  auth: { token: 'your_jwt_token' }
});
```

### Tester les événements

```javascript
// Rejoindre un chat
socket.emit('join-chat', { chatId: 'chat_id' });

// Envoyer un message
socket.emit('send-message', { 
  chatId: 'chat_id', 
  text: 'Hello!' 
});

// Indicateur de frappe
socket.emit('typing', { 
  chatId: 'chat_id', 
  isTyping: true 
});

// Écouter les nouveaux messages
socket.on('new-message', (message) => {
  console.log('New message:', message);
});
```

## Déploiement

1. S'assurer que Socket.IO est configuré dans `main.ts`
2. Vérifier que CORS autorise les connexions WebSocket
3. Configurer le namespace `/chats` dans le Gateway
4. Tester la connexion depuis l'application Android

## URL de production

- **Base URL** : `https://apinest-production.up.railway.app`
- **Namespace WebSocket** : `/chats`
- **URL complète** : `https://apinest-production.up.railway.app/chats`

## Support

En cas de problème :
1. Vérifier les logs du Gateway (`ChatsGateway`)
2. Vérifier que le token JWT est valide
3. Vérifier que l'utilisateur a accès au chat
4. Vérifier que les rooms sont correctement créées

