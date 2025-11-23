# Backend API: Chat Group Endpoints

Ce document décrit l'implémentation des endpoints backend pour gérer les groupes de chat (quitter un groupe et récupérer les participants).

## 1. Endpoint: Quitter un Groupe

### DELETE `/chats/:id/leave`

Permet à un utilisateur de quitter un groupe de chat.

**Authentification** : Requise (JWT)

**Paramètres de route** :
- `id` (string, required) : ID du chat de groupe

**Réponse (Success - HTTP 200 OK)** :
```json
{
  "message": "Successfully left the group"
}
```

**Réponse (Error)** :
- **HTTP 400 Bad Request:**
  ```json
  {
    "statusCode": 400,
    "message": "Cannot leave a direct chat. This endpoint is only for group chats.",
    "error": "Bad Request"
  }
  ```
- **HTTP 401 Unauthorized:**
  ```json
  {
    "statusCode": 401,
    "message": "Unauthorized",
    "error": "Unauthorized"
  }
  ```
- **HTTP 403 Forbidden:**
  ```json
  {
    "statusCode": 403,
    "message": "You are not a participant of this chat.",
    "error": "Forbidden"
  }
  ```
- **HTTP 404 Not Found:**
  ```json
  {
    "statusCode": 404,
    "message": "Chat not found.",
    "error": "Not Found"
  }
  ```

## 2. Endpoint: Récupérer les Participants

### GET `/chats/:id/participants`

Récupère la liste de tous les participants d'un chat (groupe ou direct).

**Authentification** : Requise (JWT)

**Paramètres de route** :
- `id` (string, required) : ID du chat

**Réponse (Success - HTTP 200 OK)** :
```json
[
  {
    "id": "user_id_1",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "profileImageUrl": "https://example.com/john.jpg",
    "avatar": "https://example.com/john.jpg"
  },
  {
    "id": "user_id_2",
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "profileImageUrl": "https://example.com/jane.jpg",
    "avatar": "https://example.com/jane.jpg"
  }
]
```

**Réponse (Error)** :
- **HTTP 401 Unauthorized:**
  ```json
  {
    "statusCode": 401,
    "message": "Unauthorized",
    "error": "Unauthorized"
  }
  ```
- **HTTP 403 Forbidden:**
  ```json
  {
    "statusCode": 403,
    "message": "You are not a participant of this chat.",
    "error": "Forbidden"
  }
  ```
- **HTTP 404 Not Found:**
  ```json
  {
    "statusCode": 404,
    "message": "Chat not found.",
    "error": "Not Found"
  }
  ```

---

## Implémentation NestJS

### 1. Controller (`chats.controller.ts`)

```typescript
import { Controller, Delete, Get, Param, UseGuards, Req } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { ChatsService } from './chats.service';
import { ChatParticipantDto } from './dto/chat-participant.dto';

@Controller('chats')
@UseGuards(JwtAuthGuard)
export class ChatsController {
  constructor(private readonly chatsService: ChatsService) {}

  /**
   * Quitter un groupe de chat
   * DELETE /chats/:id/leave
   */
  @Delete(':id/leave')
  async leaveGroup(
    @Param('id') chatId: string,
    @Req() req
  ) {
    const userId = req.user.userId; // ID de l'utilisateur connecté
    
    await this.chatsService.leaveGroup(chatId, userId);
    
    return {
      message: 'Successfully left the group'
    };
  }

  /**
   * Récupérer les participants d'un chat
   * GET /chats/:id/participants
   */
  @Get(':id/participants')
  async getParticipants(
    @Param('id') chatId: string,
    @Req() req
  ): Promise<ChatParticipantDto[]> {
    const userId = req.user.userId; // ID de l'utilisateur connecté
    
    const participants = await this.chatsService.getParticipants(chatId, userId);
    
    return participants.map(p => ({
      id: p._id.toString(),
      name: p.name,
      email: p.email,
      profileImageUrl: p.profileImageUrl,
      avatar: p.profileImageUrl || p.avatar
    }));
  }
}
```

### 2. Service (`chats.service.ts`)

```typescript
import { Injectable, NotFoundException, ForbiddenException, BadRequestException } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Chat, ChatDocument } from './schemas/chat.schema';
import { User, UserDocument } from '../users/schemas/user.schema';

@Injectable()
export class ChatsService {
  constructor(
    @InjectModel(Chat.name) private chatModel: Model<ChatDocument>,
    @InjectModel(User.name) private userModel: Model<UserDocument>
  ) {}

  /**
   * Quitter un groupe de chat
   */
  async leaveGroup(chatId: string, userId: string): Promise<void> {
    // 1. Récupérer le chat
    const chat = await this.chatModel.findById(chatId).exec();
    
    if (!chat) {
      throw new NotFoundException('Chat not found.');
    }

    // 2. Vérifier que c'est un groupe (pas un chat direct)
    if (!chat.isGroup) {
      throw new BadRequestException('Cannot leave a direct chat. This endpoint is only for group chats.');
    }

    // 3. Vérifier que l'utilisateur est participant
    const isParticipant = chat.participants.some(
      p => p.toString() === userId
    );
    
    if (!isParticipant) {
      throw new ForbiddenException('You are not a participant of this chat.');
    }

    // 4. Retirer l'utilisateur de la liste des participants
    chat.participants = chat.participants.filter(
      p => p.toString() !== userId
    );

    // 5. Si le groupe devient vide, optionnellement supprimer le chat
    // (ou garder le chat pour l'historique)
    if (chat.participants.length === 0) {
      // Option 1: Supprimer le chat
      await chat.deleteOne();
      // Option 2: Garder le chat vide (décommenter cette ligne et commenter la précédente)
      // await chat.save();
    } else {
      // Sauvegarder les modifications
      await chat.save();
    }
  }

  /**
   * Récupérer les participants d'un chat
   */
  async getParticipants(chatId: string, userId: string): Promise<UserDocument[]> {
    // 1. Récupérer le chat avec les participants peuplés
    const chat = await this.chatModel
      .findById(chatId)
      .populate('participants')
      .exec();
    
    if (!chat) {
      throw new NotFoundException('Chat not found.');
    }

    // 2. Vérifier que l'utilisateur est participant
    const isParticipant = chat.participants.some(
      p => p._id.toString() === userId || p.toString() === userId
    );
    
    if (!isParticipant) {
      throw new ForbiddenException('You are not a participant of this chat.');
    }

    // 3. Retourner les participants peuplés
    // Si les participants sont déjà peuplés, les retourner directement
    if (chat.participants.length > 0 && chat.participants[0] instanceof this.userModel) {
      return chat.participants as UserDocument[];
    }

    // Sinon, peupler les participants manuellement
    const participantIds = chat.participants.map(p => 
      typeof p === 'string' ? p : p.toString()
    );
    
    const participants = await this.userModel.find({
      _id: { $in: participantIds }
    }).exec();

    return participants;
  }
}
```

### 3. DTO (`dto/chat-participant.dto.ts`)

```typescript
export class ChatParticipantDto {
  id: string;
  name: string;
  email?: string;
  profileImageUrl?: string;
  avatar?: string;
}
```

### 4. Schéma Chat (`schemas/chat.schema.ts`)

```typescript
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';
import { User } from '../users/schemas/user.schema';

@Schema({ timestamps: true })
export class Chat extends Document {
  @Prop({ 
    type: [{ type: Types.ObjectId, ref: 'User' }], 
    required: true 
  })
  participants: Types.ObjectId[];

  @Prop()
  groupName?: string;

  @Prop()
  groupAvatar?: string;

  @Prop({ default: false })
  isGroup: boolean;

  @Prop({ type: Types.ObjectId, ref: 'Activity' })
  activityId?: Types.ObjectId;

  // ... autres champs (messages, etc.)
}

export const ChatSchema = SchemaFactory.createForClass(Chat);
```

---

## Notes importantes

### 1. Vérification des permissions
- Les deux endpoints vérifient que l'utilisateur est participant du chat avant d'autoriser l'action.
- Pour `leaveGroup`, on vérifie également que c'est un groupe (pas un chat direct).

### 2. Gestion des groupes vides
- Quand le dernier participant quitte un groupe, vous pouvez choisir de :
  - **Supprimer le chat** (recommandé pour libérer de l'espace)
  - **Garder le chat vide** (pour préserver l'historique)

### 3. Population des participants
- Utilisez `.populate('participants')` pour récupérer les détails complets des utilisateurs.
- Gérer les cas où les participants sont des ObjectId non peuplés.

### 4. Sécurité
- Toujours vérifier l'authentification avec `JwtAuthGuard`.
- Vérifier que l'utilisateur est participant avant d'autoriser l'accès.
- Ne pas exposer d'informations sensibles (mots de passe, tokens, etc.).

### 5. Gestion des erreurs
- Retourner des codes HTTP appropriés (400, 401, 403, 404).
- Fournir des messages d'erreur clairs et informatifs.

---

## Tests

### Test 1: Quitter un groupe
```bash
DELETE https://apinest-production.up.railway.app/chats/chat-id-123/leave
Authorization: Bearer <jwt-token>
```

### Test 2: Récupérer les participants
```bash
GET https://apinest-production.up.railway.app/chats/chat-id-123/participants
Authorization: Bearer <jwt-token>
```

### Scénarios de test
1. ✅ Quitter un groupe avec succès
2. ✅ Essayer de quitter un chat direct (doit retourner 400)
3. ✅ Essayer de quitter un groupe sans être participant (doit retourner 403)
4. ✅ Récupérer les participants d'un groupe
5. ✅ Récupérer les participants d'un chat direct
6. ✅ Essayer de récupérer les participants sans être participant (doit retourner 403)
7. ✅ Quitter un groupe et vérifier que le participant est retiré
8. ✅ Quitter un groupe vide (dernier participant) et vérifier la suppression/archivage

---

## Intégration avec le frontend Android

Le frontend Android est déjà configuré pour utiliser ces endpoints :

- **`ChatApiService.kt`** : Contient les méthodes `leaveGroup()` et `getParticipants()`
- **`ChatRepository.kt`** : Implémente la logique de communication avec l'API
- **`ParticipantsScreen.kt`** : Affiche la liste des participants
- **`ChatScreen.kt`** : Menu avec option "Quitter le groupe"

Une fois ces endpoints implémentés dans le backend, l'application Android pourra :
1. Afficher la liste des participants d'un groupe
2. Permettre à un utilisateur de quitter un groupe
3. Rafraîchir automatiquement la liste des chats après avoir quitté un groupe

