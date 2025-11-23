# Backend NestJS - Activity Room API

## Endpoints pour la page Activity Room

### 1. Rejoindre une activité

**POST /activities/:id/join**

**Authentification** : Requis (Bearer Token JWT)

**Réponse** :
```json
{
  "message": "Successfully joined activity",
  "activity": { ... }
}
```

### 2. Récupérer les messages de chat

**GET /activities/:id/messages**

**Authentification** : Requis (Bearer Token JWT)

**Réponse** :
```json
{
  "messages": [
    {
      "_id": "...",
      "activity": "...",
      "sender": {
        "_id": "...",
        "name": "...",
        "profileImageUrl": "..."
      },
      "content": "...",
      "createdAt": "2025-11-14T10:30:00Z"
    }
  ]
}
```

### 3. Envoyer un message

**POST /activities/:id/messages**

**Authentification** : Requis (Bearer Token JWT)

**Body** :
```json
{
  "content": "Message text"
}
```

**Réponse** :
```json
{
  "_id": "...",
  "activity": "...",
  "sender": { ... },
  "content": "...",
  "createdAt": "2025-11-14T10:30:00Z"
}
```

### 4. Récupérer les participants

**GET /activities/:id/participants**

**Authentification** : Requis (Bearer Token JWT)

**Réponse** :
```json
{
  "participants": [
    {
      "_id": "...",
      "name": "...",
      "profileImageUrl": "...",
      "joinedAt": "2025-11-14T10:00:00Z",
      "isHost": true
    }
  ]
}
```

### 5. Quitter une activité

**POST /activities/:id/leave**

**Authentification** : Requis (Bearer Token JWT)

**Réponse** :
```json
{
  "message": "Successfully left activity"
}
```

### 6. Marquer comme complété

**POST /activities/:id/complete**

**Authentification** : Requis (Bearer Token JWT) - Seulement le créateur

**Réponse** :
```json
{
  "message": "Activity marked as complete"
}
```

## Implémentation NestJS

### Schema ActivityMessage

```typescript
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

@Schema({ timestamps: true })
export class ActivityMessage extends Document {
  @Prop({ type: Types.ObjectId, ref: 'Activity', required: true })
  activity: Types.ObjectId;

  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  sender: Types.ObjectId;

  @Prop({ required: true })
  content: string;

  @Prop({ default: Date.now })
  createdAt: Date;
}

export const ActivityMessageSchema = SchemaFactory.createForClass(ActivityMessage);
```

### Schema Activity (ajouter le champ participants)

```typescript
@Schema({ timestamps: true })
export class Activity extends Document {
  // ... champs existants ...
  
  @Prop([{ type: Types.ObjectId, ref: 'User' }])
  participants: Types.ObjectId[];
}
```

### Controller

```typescript
import { Controller, Get, Post, Body, Param, UseGuards, Request } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { ActivitiesService } from './activities.service';
import { ActivityMessagesService } from './activity-messages.service';

@Controller('activities')
@UseGuards(JwtAuthGuard)
export class ActivitiesController {
  constructor(
    private readonly activitiesService: ActivitiesService,
    private readonly activityMessagesService: ActivityMessagesService,
  ) {}

  @Post(':id/join')
  async joinActivity(@Param('id') id: string, @Request() req) {
    return this.activitiesService.joinActivity(id, req.user.userId);
  }

  @Get(':id/messages')
  async getMessages(@Param('id') id: string) {
    return this.activityMessagesService.getMessages(id);
  }

  @Post(':id/messages')
  async sendMessage(
    @Param('id') id: string,
    @Body() body: { content: string },
    @Request() req,
  ) {
    return this.activityMessagesService.sendMessage(id, req.user.userId, body.content);
  }

  @Get(':id/participants')
  async getParticipants(@Param('id') id: string) {
    return this.activitiesService.getParticipants(id);
  }

  @Post(':id/leave')
  async leaveActivity(@Param('id') id: string, @Request() req) {
    return this.activitiesService.leaveActivity(id, req.user.userId);
  }

  @Post(':id/complete')
  async completeActivity(@Param('id') id: string, @Request() req) {
    return this.activitiesService.completeActivity(id, req.user.userId);
  }
}
```

### Service ActivitiesService (méthodes à ajouter)

```typescript
async joinActivity(activityId: string, userId: string) {
  const activity = await this.activityModel.findById(activityId);
  if (!activity) {
    throw new NotFoundException('Activity not found');
  }

  if (activity.participants.includes(userId)) {
    throw new BadRequestException('Already joined this activity');
  }

  if (activity.participants.length >= activity.participants) {
    throw new BadRequestException('Activity is full');
  }

  activity.participants.push(userId);
  await activity.save();

  return {
    message: 'Successfully joined activity',
    activity: await this.activityModel.findById(activityId).populate('creator'),
  };
}

async getParticipants(activityId: string) {
  const activity = await this.activityModel
    .findById(activityId)
    .populate('participants', 'name profileImageUrl')
    .populate('creator', 'name profileImageUrl');

  if (!activity) {
    throw new NotFoundException('Activity not found');
  }

  const participants = activity.participants.map((participant: any) => ({
    _id: participant._id,
    name: participant.name,
    profileImageUrl: participant.profileImageUrl,
    isHost: participant._id.toString() === activity.creator._id.toString(),
  }));

  return { participants };
}

async leaveActivity(activityId: string, userId: string) {
  const activity = await this.activityModel.findById(activityId);
  if (!activity) {
    throw new NotFoundException('Activity not found');
  }

  if (activity.creator.toString() === userId) {
    throw new BadRequestException('Host cannot leave the activity');
  }

  activity.participants = activity.participants.filter(
    (p: any) => p.toString() !== userId,
  );
  await activity.save();

  return { message: 'Successfully left activity' };
}

async completeActivity(activityId: string, userId: string) {
  const activity = await this.activityModel.findById(activityId);
  if (!activity) {
    throw new NotFoundException('Activity not found');
  }

  if (activity.creator.toString() !== userId) {
    throw new ForbiddenException('Only the host can mark activity as complete');
  }

  // Ajouter un champ isCompleted au schema si nécessaire
  activity.isCompleted = true;
  await activity.save();

  return { message: 'Activity marked as complete' };
}
```

### Service ActivityMessagesService

```typescript
import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { ActivityMessage } from './schemas/activity-message.schema';

@Injectable()
export class ActivityMessagesService {
  constructor(
    @InjectModel(ActivityMessage.name)
    private activityMessageModel: Model<ActivityMessage>,
    @InjectModel(Activity.name)
    private activityModel: Model<Activity>,
  ) {}

  async getMessages(activityId: string) {
    const activity = await this.activityModel.findById(activityId);
    if (!activity) {
      throw new NotFoundException('Activity not found');
    }

    const messages = await this.activityMessageModel
      .find({ activity: activityId })
      .populate('sender', 'name profileImageUrl')
      .sort({ createdAt: 1 });

    return { messages };
  }

  async sendMessage(activityId: string, userId: string, content: string) {
    const activity = await this.activityModel.findById(activityId);
    if (!activity) {
      throw new NotFoundException('Activity not found');
    }

    // Vérifier que l'utilisateur est participant
    if (!activity.participants.includes(userId)) {
      throw new ForbiddenException('You must join the activity to send messages');
    }

    const message = new this.activityMessageModel({
      activity: activityId,
      sender: userId,
      content,
    });

    await message.save();
    await message.populate('sender', 'name profileImageUrl');

    return message;
  }
}
```

