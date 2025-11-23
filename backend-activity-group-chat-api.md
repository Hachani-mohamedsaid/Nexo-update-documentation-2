# Backend API - Chat de Groupe depuis une Activité

## Endpoint : Créer/Rejoindre un Chat de Groupe depuis une Activité

### POST `/activities/:activityId/group-chat`

Crée un chat de groupe pour une activité ou retourne le chat existant si déjà créé.

**Authentification** : Requise (JWT)

**Paramètres de route** :
- `activityId` (string, required) : ID de l'activité

**Réponse** :
```json
{
  "chat": {
    "id": "chat-id-123",
    "groupName": "Swimming Group",
    "groupAvatar": "https://example.com/avatar.png",
    "participants": [
      {
        "id": "user-id-1",
        "name": "Sarah Mitchell",
        "profileImageUrl": "https://example.com/avatar1.png"
      },
      {
        "id": "user-id-2",
        "name": "John Doe",
        "profileImageUrl": "https://example.com/avatar2.png"
      }
    ],
    "isGroup": true,
    "createdAt": "2025-11-15T10:30:00Z",
    "updatedAt": "2025-11-15T10:30:00Z"
  },
  "message": "Chat de groupe créé avec succès" // ou "Chat de groupe existant"
}
```

**Codes de réponse** :
- `200 OK` : Chat créé ou existant retourné
- `201 Created` : Nouveau chat créé
- `400 Bad Request` : Activité invalide ou pas de participants
- `401 Unauthorized` : Token manquant ou invalide
- `404 Not Found` : Activité non trouvée
- `403 Forbidden` : Utilisateur non participant de l'activité

---

## Implémentation NestJS

### 1. DTOs

```typescript
// dto/create-activity-group-chat.dto.ts
export class CreateActivityGroupChatDto {
  // Pas de body nécessaire, tout est dans l'URL
}

// dto/activity-group-chat-response.dto.ts
export class ActivityGroupChatResponseDto {
  chat: {
    id: string;
    groupName: string;
    groupAvatar?: string;
    participants: Array<{
      id: string;
      name: string;
      profileImageUrl?: string;
    }>;
    isGroup: boolean;
    createdAt: string;
    updatedAt: string;
  };
  message: string;
}
```

### 2. Controller

```typescript
// activities.controller.ts

import { Controller, Post, Param, UseGuards, Get } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { ActivitiesService } from './activities.service';
import { ChatsService } from '../chats/chats.service';
import { ActivityGroupChatResponseDto } from './dto/activity-group-chat-response.dto';

@Controller('activities')
@UseGuards(JwtAuthGuard)
export class ActivitiesController {
  constructor(
    private readonly activitiesService: ActivitiesService,
    private readonly chatsService: ChatsService,
  ) {}

  /**
   * Créer ou récupérer le chat de groupe d'une activité
   * POST /activities/:activityId/group-chat
   */
  @Post(':activityId/group-chat')
  async createOrGetActivityGroupChat(
    @Param('activityId') activityId: string,
    @CurrentUser() user: any,
  ): Promise<ActivityGroupChatResponseDto> {
    // 1. Vérifier que l'activité existe
    const activity = await this.activitiesService.findOne(activityId);
    if (!activity) {
      throw new NotFoundException('Activité non trouvée');
    }

    // 2. Vérifier que l'utilisateur est participant de l'activité
    const isParticipant = await this.activitiesService.isUserParticipant(
      activityId,
      user.userId,
    );
    if (!isParticipant) {
      throw new ForbiddenException(
        'Vous devez être participant de l\'activité pour accéder au chat de groupe',
      );
    }

    // 3. Vérifier si un chat de groupe existe déjà pour cette activité
    const existingChat = await this.chatsService.findGroupChatByActivityId(
      activityId,
    );

    if (existingChat) {
      // Retourner le chat existant
      return {
        chat: {
          id: existingChat._id.toString(),
          groupName: existingChat.groupName || activity.title,
          groupAvatar: existingChat.groupAvatar,
          participants: existingChat.participants.map((p: any) => ({
            id: p._id.toString(),
            name: p.name,
            profileImageUrl: p.profileImageUrl,
          })),
          isGroup: true,
          createdAt: existingChat.createdAt.toISOString(),
          updatedAt: existingChat.updatedAt.toISOString(),
        },
        message: 'Chat de groupe existant',
      };
    }

    // 4. Récupérer tous les participants de l'activité
    const participants = await this.activitiesService.getActivityParticipants(
      activityId,
    );

    if (participants.length === 0) {
      throw new BadRequestException(
        'Aucun participant trouvé pour cette activité',
      );
    }

    // 5. Créer le chat de groupe avec tous les participants
    const participantIds = participants.map((p: any) => p._id.toString());

    const newChat = await this.chatsService.createGroupChat({
      participantIds,
      groupName: activity.title || `Groupe ${activity.sportType}`,
      groupAvatar: null, // Optionnel : utiliser une image par défaut selon le sport
      activityId: activityId, // Lier le chat à l'activité
    });

    return {
      chat: {
        id: newChat._id.toString(),
        groupName: newChat.groupName,
        groupAvatar: newChat.groupAvatar,
        participants: newChat.participants.map((p: any) => ({
          id: p._id.toString(),
          name: p.name,
          profileImageUrl: p.profileImageUrl,
        })),
        isGroup: true,
        createdAt: newChat.createdAt.toISOString(),
        updatedAt: newChat.updatedAt.toISOString(),
      },
      message: 'Chat de groupe créé avec succès',
    };
  }
}
```

### 3. Service Activities

```typescript
// activities.service.ts

async isUserParticipant(activityId: string, userId: string): Promise<boolean> {
  const activity = await this.activityModel
    .findById(activityId)
    .populate('participants');
  
  if (!activity) {
    return false;
  }

  // Vérifier si l'utilisateur est le créateur
  if (activity.creator.toString() === userId) {
    return true;
  }

  // Vérifier si l'utilisateur est dans la liste des participants
  return activity.participants.some(
    (p: any) => p._id.toString() === userId,
  );
}

async getActivityParticipants(activityId: string): Promise<any[]> {
  const activity = await this.activityModel
    .findById(activityId)
    .populate('participants')
    .populate('creator');
  
  if (!activity) {
    return [];
  }

  // Inclure le créateur dans les participants
  const allParticipants = [
    activity.creator,
    ...activity.participants,
  ];

  // Éliminer les doublons
  const uniqueParticipants = Array.from(
    new Map(
      allParticipants.map((p: any) => [p._id.toString(), p])
    ).values()
  );

  return uniqueParticipants;
}
```

### 4. Service Chats

```typescript
// chats.service.ts

async findGroupChatByActivityId(activityId: string): Promise<any> {
  return this.chatModel.findOne({
    activityId: activityId,
    isGroup: true,
  }).populate('participants');
}

async createGroupChat(data: {
  participantIds: string[];
  groupName: string;
  groupAvatar?: string;
  activityId?: string;
}): Promise<any> {
  const participants = await this.userModel.find({
    _id: { $in: data.participantIds },
  });

  const chat = new this.chatModel({
    participants: participants.map((p) => p._id),
    groupName: data.groupName,
    groupAvatar: data.groupAvatar,
    isGroup: true,
    activityId: data.activityId, // Lier à l'activité
  });

  await chat.save();
  return chat.populate('participants');
}
```

### 5. Modèle Chat (Mongoose Schema)

```typescript
// schemas/chat.schema.ts

import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';

@Schema({ timestamps: true })
export class Chat extends Document {
  @Prop({ type: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }] })
  participants: mongoose.Types.ObjectId[];

  @Prop()
  groupName?: string;

  @Prop()
  groupAvatar?: string;

  @Prop({ default: false })
  isGroup: boolean;

  @Prop({ type: mongoose.Schema.Types.ObjectId, ref: 'Activity' })
  activityId?: mongoose.Types.ObjectId; // Lier le chat à une activité

  @Prop({ type: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Message' }] })
  messages: mongoose.Types.ObjectId[];
}
```

---

## Notes importantes

1. **Liaison Activité-Chat** : Le champ `activityId` dans le modèle Chat permet de lier un chat de groupe à une activité spécifique.

2. **Vérification des participants** : L'utilisateur doit être participant de l'activité pour créer/rejoindre le chat.

3. **Création unique** : Si un chat existe déjà pour l'activité, on le retourne au lieu d'en créer un nouveau.

4. **Nom du groupe** : Par défaut, le nom du groupe est le titre de l'activité.

5. **Participants** : Tous les participants de l'activité (y compris le créateur) sont automatiquement ajoutés au chat.

6. **Gestion des doublons** : Le créateur et les participants sont fusionnés sans doublons.

---

## Exemple d'utilisation

```bash
# Créer/rejoindre le chat de groupe d'une activité
POST https://apinest-production.up.railway.app/activities/activity-id-123/group-chat
Authorization: Bearer <jwt-token>

# Réponse
{
  "chat": {
    "id": "chat-id-456",
    "groupName": "Morning lap swimming at City Pool",
    "participants": [
      {
        "id": "user-1",
        "name": "Sarah Mitchell",
        "profileImageUrl": "https://..."
      },
      {
        "id": "user-2",
        "name": "John Doe",
        "profileImageUrl": "https://..."
      }
    ],
    "isGroup": true,
    "createdAt": "2025-11-15T10:30:00Z",
    "updatedAt": "2025-11-15T10:30:00Z"
  },
  "message": "Chat de groupe créé avec succès"
}
```

