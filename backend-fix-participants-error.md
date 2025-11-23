# Correction Backend: Erreur getParticipants/getActivityParticipants

## Problème

L'erreur suivante se produit lors de l'appel à `createOrGetActivityGroupChat`:

```
TypeError: Cannot read properties of undefined (reading 'toString')
    at <anonymous> (/app/src/modules/activities/activities.service.ts:214:31)
    at Array.map (<anonymous>)
    at ActivitiesService.getParticipants (/app/src/modules/activities/activities.service.ts:210:50)
```

Et aussi:

```
TypeError: Cannot read properties of undefined (reading 'toString')
    at <anonymous> (/app/src/modules/activities/activities.service.ts:305:24)
    at Array.forEach (<anonymous>)
    at ActivitiesService.getActivityParticipants (/app/src/modules/activities/activities.service.ts:304:21)
    at async ActivitiesController.createOrGetActivityGroupChat (/app/src/modules/activities/activities.controller.ts:254:26)
```

## Cause

Le problème vient du fait que le tableau `participants` contient des valeurs `null` ou `undefined`, ou que les participants ne sont pas correctement peuplés (populated) depuis MongoDB. Quand le code essaie d'appeler `.toString()` sur ces valeurs, cela génère l'erreur.

## Solution

### 1. Corriger `getParticipants` (ligne ~210-214)

**Avant (code problématique):**
```typescript
getParticipants(activity: ActivityDocument): string[] {
  return activity.participants.map(p => p.toString());
}
```

**Après (code corrigé):**
```typescript
getParticipants(activity: ActivityDocument): string[] {
  if (!activity.participants || activity.participants.length === 0) {
    return [];
  }
  return activity.participants
    .filter(p => p != null) // Filtrer les valeurs null/undefined
    .map(p => {
      // Gérer les cas où p est un ObjectId ou déjà une string
      if (typeof p === 'string') {
        return p;
      }
      if (p && p.toString) {
        return p.toString();
      }
      return null;
    })
    .filter((id): id is string => id != null); // Filtrer les null restants
}
```

### 2. Corriger `getActivityParticipants` (ligne ~304-305)

**Avant (code problématique):**
```typescript
async getActivityParticipants(activityId: string): Promise<ActivityParticipantDto[]> {
  const activity = await this.activityModel
    .findById(activityId)
    .populate('participants')
    .exec();

  if (!activity) {
    throw new NotFoundException('Activity not found');
  }

  const participants: ActivityParticipantDto[] = [];
  activity.participants.forEach(participant => {
    participants.push({
      id: participant._id.toString(),
      name: participant.name,
      profileImageUrl: participant.profileImageUrl || null,
    });
  });

  return participants;
}
```

**Après (code corrigé):**
```typescript
async getActivityParticipants(activityId: string): Promise<ActivityParticipantDto[]> {
  const activity = await this.activityModel
    .findById(activityId)
    .populate('participants')
    .exec();

  if (!activity) {
    throw new NotFoundException('Activity not found');
  }

  if (!activity.participants || activity.participants.length === 0) {
    return [];
  }

  const participants: ActivityParticipantDto[] = [];
  
  // Filtrer les participants null/undefined avant de les traiter
  const validParticipants = activity.participants.filter(p => p != null);
  
  for (const participant of validParticipants) {
    // Vérifier que le participant est bien peuplé (populated)
    if (participant && participant._id) {
      participants.push({
        id: participant._id.toString(),
        name: participant.name || 'Unknown',
        profileImageUrl: participant.profileImageUrl || null,
      });
    }
  }

  return participants;
}
```

### 3. Vérifier le schéma Activity

Assurez-vous que le schéma `Activity` définit correctement le champ `participants`:

```typescript
// activities/schemas/activity.schema.ts
@Schema({ timestamps: true })
export class Activity {
  // ... autres champs

  @Prop({ 
    type: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
    default: []
  })
  participants: mongoose.Types.ObjectId[];

  // ... autres champs
}
```

### 4. Vérifier la méthode `createOrGetActivityGroupChat` dans le controller

Dans `activities.controller.ts`, ligne ~254, assurez-vous que l'activité est correctement peuplée:

```typescript
@Post(':id/group-chat')
async createOrGetActivityGroupChat(
  @Param('id') activityId: string,
  @Req() req
) {
  const userId = req.user.userId;

  // 1. Récupérer l'activité avec les participants peuplés
  const activity = await this.activitiesService.findOne(activityId);
  if (!activity) {
    throw new NotFoundException('Activity not found.');
  }

  // 2. Vérifier que l'utilisateur est participant
  const participants = this.activitiesService.getParticipants(activity);
  const isParticipant = participants.includes(userId);
  
  if (!isParticipant) {
    throw new ForbiddenException('You must be a participant of the activity to access the chat.');
  }

  // 3. Récupérer les participants avec leurs détails
  const activityParticipants = await this.activitiesService.getActivityParticipants(activityId);
  
  if (!activityParticipants || activityParticipants.length === 0) {
    throw new BadRequestException('Activity has no participants.');
  }

  // 4. Vérifier si un chat existe déjà
  let chat = await this.chatsService.findGroupChatByActivityId(activityId);

  if (!chat) {
    // 5. Créer un nouveau chat de groupe
    const participantIds = activityParticipants.map(p => p.id);
    const groupName = activity.title;
    const groupAvatar = activity.sportIconUrl;

    chat = await this.chatsService.createGroupChat({
      participantIds,
      groupName,
      groupAvatar,
      activityId
    });
    
    return { 
      chat: this.mapChatToDto(chat), 
      message: 'Activity group chat created successfully.' 
    };
  }

  return { 
    chat: this.mapChatToDto(chat), 
    message: 'Existing activity group chat retrieved.' 
  };
}
```

### 5. Méthode helper pour mapper le chat (optionnel)

Ajoutez une méthode helper dans le controller pour mapper le chat vers le DTO:

```typescript
private mapChatToDto(chat: ChatDocument): ActivityGroupChatDto {
  return {
    id: chat._id.toString(),
    groupName: chat.groupName || 'Group Chat',
    groupAvatar: chat.groupAvatar || null,
    participants: chat.participants.map(p => ({
      id: p._id.toString(),
      name: p.name || 'Unknown',
      profileImageUrl: p.profileImageUrl || null,
    })),
    isGroup: chat.isGroup,
    createdAt: chat.createdAt.toISOString(),
    updatedAt: chat.updatedAt.toISOString(),
  };
}
```

## Points importants

1. **Toujours vérifier null/undefined**: Avant d'appeler `.toString()` ou d'accéder à des propriétés, vérifiez que l'objet existe.

2. **Filtrer les valeurs invalides**: Utilisez `.filter()` pour supprimer les valeurs `null` ou `undefined` avant de les traiter.

3. **Vérifier le populate**: Assurez-vous que les références MongoDB sont correctement peuplées avec `.populate('participants')`.

4. **Gérer les cas limites**: Si une activité n'a pas de participants, retournez un tableau vide plutôt que de générer une erreur.

5. **Logging**: Ajoutez des logs pour déboguer:
   ```typescript
   console.log('Activity participants:', activity.participants);
   console.log('Valid participants:', validParticipants);
   ```

## Test

Après avoir appliqué ces corrections, testez:

1. Créer une activité avec des participants
2. Appeler `POST /activities/:id/group-chat` avec un utilisateur participant
3. Vérifier que le chat est créé sans erreur
4. Tester avec une activité sans participants (devrait retourner une erreur appropriée)
5. Tester avec un utilisateur non-participant (devrait retourner 403 Forbidden)


