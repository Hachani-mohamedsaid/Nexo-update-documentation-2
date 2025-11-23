# Backend NestJS - AI Matchmaker Integration

## Structure des fichiers à créer

### 1. DTOs (Data Transfer Objects)

**src/ai-matchmaker/dto/chat-request.dto.ts**
```typescript
import { IsString, IsOptional, IsArray, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';

export class ChatMessageDto {
  @IsString()
  role: 'user' | 'assistant';

  @IsString()
  content: string;
}

export class ChatRequestDto {
  @IsString()
  message: string;

  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ChatMessageDto)
  conversationHistory?: ChatMessageDto[];
}
```

**src/ai-matchmaker/dto/chat-response.dto.ts**
```typescript
import { IsString, IsOptional, IsArray, IsNumber, ValidateNested } from 'class-validator';
import { Type } from 'class-transformer';

export class SuggestedActivityDto {
  @IsString()
  id: string;

  @IsString()
  title: string;

  @IsString()
  sportType: string;

  @IsString()
  location: string;

  @IsString()
  date: string;

  @IsString()
  time: string;

  @IsNumber()
  participants: number;

  @IsNumber()
  maxParticipants: number;

  @IsString()
  level: string;

  @IsOptional()
  @IsNumber()
  matchScore?: number;
}

export class SuggestedUserDto {
  @IsString()
  id: string;

  @IsString()
  name: string;

  @IsOptional()
  @IsString()
  profileImageUrl?: string;

  @IsString()
  sport: string;

  @IsOptional()
  @IsString()
  distance?: string;

  @IsOptional()
  @IsNumber()
  matchScore?: number;

  @IsOptional()
  @IsString()
  bio?: string;

  @IsOptional()
  @IsString()
  availability?: string;
}

export class ChatResponseDto {
  @IsString()
  message: string;

  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SuggestedActivityDto)
  suggestedActivities?: SuggestedActivityDto[];

  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SuggestedUserDto)
  suggestedUsers?: SuggestedUserDto[];

  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  options?: string[];
}
```

### 2. Service AI Matchmaker

**src/ai-matchmaker/ai-matchmaker.service.ts**
```typescript
import { Injectable, HttpException, HttpStatus } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import axios from 'axios';
import { ChatRequestDto } from './dto/chat-request.dto';
import { ChatResponseDto, SuggestedActivityDto, SuggestedUserDto } from './dto/chat-response.dto';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { User } from '../users/schemas/user.schema';
import { Activity } from '../activities/schemas/activity.schema';

@Injectable()
export class AIMatchmakerService {
  private readonly openaiApiKey: string;
  private readonly openaiApiUrl = 'https://api.openai.com/v1/chat/completions';

  constructor(
    private configService: ConfigService,
    @InjectModel(User.name) private userModel: Model<User>,
    @InjectModel(Activity.name) private activityModel: Model<Activity>,
  ) {
    this.openaiApiKey = this.configService.get<string>('OPENAI_API_KEY') || 
      'your_openai_api_key_here';
  }

  async chat(userId: string, chatRequest: ChatRequestDto): Promise<ChatResponseDto> {
    try {
      // Récupérer les données de l'application pour le contexte
      const user = await this.userModel.findById(userId).exec();
      const activities = await this.activityModel
        .find({ visibility: 'public' })
        .limit(20)
        .populate('creator', 'name email profileImageUrl')
        .exec();
      
      const users = await this.userModel
        .find({ _id: { $ne: userId } })
        .limit(20)
        .select('name email location sportsInterests profileImageUrl about')
        .exec();

      // Construire le contexte pour l'IA
      const context = this.buildContext(user, activities, users);

      // Préparer les messages pour ChatGPT
      const messages = this.prepareMessages(chatRequest, context);

      // Appeler l'API ChatGPT
      const response = await axios.post(
        this.openaiApiUrl,
        {
          model: 'gpt-3.5-turbo', // Utiliser gpt-3.5-turbo qui est plus accessible
          messages: messages,
          temperature: 0.7,
          max_tokens: 1000,
        },
        {
          headers: {
            'Authorization': `Bearer ${this.openaiApiKey}`,
            'Content-Type': 'application/json',
          },
        }
      );

      const aiResponse = response.data.choices[0].message.content;

      // Parser la réponse de l'IA pour extraire les suggestions
      const parsedResponse = this.parseAIResponse(aiResponse, activities, users);

      return parsedResponse;
    } catch (error) {
      console.error('Error in AI Matchmaker chat:', error);
      if (error.response) {
        const statusCode = error.response.status || HttpStatus.INTERNAL_SERVER_ERROR;
        const errorMessage = error.response.data?.error?.message || error.message;
        
        // Gestion spéciale pour le quota dépassé (429) - Utiliser le fallback
        if (statusCode === 429) {
          console.log('Quota OpenAI dépassé, utilisation du mode fallback');
          // Récupérer les données pour le fallback
          const user = await this.userModel.findById(userId).exec();
          const activities = await this.activityModel
            .find({ visibility: 'public' })
            .limit(20)
            .populate('creator', 'name email profileImageUrl')
            .exec();
          
          const users = await this.userModel
            .find({ _id: { $ne: userId } })
            .limit(20)
            .select('name email location sportsInterests profileImageUrl about')
            .exec();
          
          // Générer une réponse de fallback
          return this.generateFallbackResponse(chatRequest.message, user, activities, users);
        }
        
        throw new HttpException(
          {
            statusCode: statusCode,
            message: `OpenAI API Error: ${errorMessage}`,
          },
          statusCode,
        );
      }
      throw new HttpException(
        {
          statusCode: HttpStatus.INTERNAL_SERVER_ERROR,
          message: 'Erreur lors de la communication avec l\'IA',
        },
        HttpStatus.INTERNAL_SERVER_ERROR,
      );
    }
  }

  private buildContext(user: any, activities: any[], users: any[]): string {
    let context = `Tu es un assistant AI matchmaker pour une application de sport. `;
    context += `Tu aides les utilisateurs à trouver des partenaires de sport et des activités. `;
    context += `L'utilisateur actuel est: ${user?.name || 'Utilisateur'} (${user?.location || 'Localisation inconnue'}). `;
    
    if (user?.sportsInterests && user.sportsInterests.length > 0) {
      context += `Ses sports préférés sont: ${user.sportsInterests.join(', ')}. `;
    }

    context += `\n\nVoici les activités disponibles dans l'application:\n`;
    activities.forEach((activity, index) => {
      context += `${index + 1}. ID: ${activity._id} - ${activity.title} (${activity.sportType}) - ${activity.location} - ${activity.date} ${activity.time} - Niveau: ${activity.level} - Participants: ${activity.participants}/${activity.participants || 10}\n`;
    });

    context += `\n\nVoici les utilisateurs disponibles:\n`;
    users.forEach((u, index) => {
      context += `${index + 1}. ID: ${u._id} - ${u.name} - ${u.location || 'Localisation inconnue'} - Sports: ${u.sportsInterests?.join(', ') || 'Non spécifiés'}\n`;
    });

    context += `\n\nIMPORTANT - Instructions pour les réponses:\n`;
    context += `1. Réponds en français de manière amicale et utile.\n`;
    context += `2. Quand tu suggères des activités, mentionne explicitement le titre de l'activité ET son ID dans ta réponse.\n`;
    context += `3. Quand tu suggères des utilisateurs, mentionne explicitement le nom de l'utilisateur ET son ID dans ta réponse.\n`;
    context += `4. Si l'utilisateur demande "trouver un partenaire de course", suggère des utilisateurs qui aiment la course à pied.\n`;
    context += `5. Si l'utilisateur demande des activités de groupe, suggère des activités avec plusieurs participants.\n`;
    context += `6. Sois spécifique et mentionne les détails (lieu, date, niveau) des activités suggérées.\n`;

    return context;
  }

  private prepareMessages(chatRequest: ChatRequestDto, context: string): any[] {
    const messages: any[] = [
      {
        role: 'system',
        content: context,
      },
    ];

    // Ajouter l'historique de conversation
    if (chatRequest.conversationHistory && chatRequest.conversationHistory.length > 0) {
      chatRequest.conversationHistory.forEach(msg => {
        messages.push({
          role: msg.role,
          content: msg.content,
        });
      });
    }

    // Ajouter le message actuel
    messages.push({
      role: 'user',
      content: chatRequest.message,
    });

    return messages;
  }

  private parseAIResponse(
    aiResponse: string,
    activities: any[],
    users: any[],
  ): ChatResponseDto {
    // Extraire les suggestions d'activités et d'utilisateurs de la réponse
    const suggestedActivities: SuggestedActivityDto[] = [];
    const suggestedUsers: SuggestedUserDto[] = [];
    const options: string[] = [];

    // Chercher des références aux activités dans la réponse par ID ou titre
    activities.forEach(activity => {
      const activityId = activity._id.toString();
      const titleLower = activity.title.toLowerCase();
      const sportTypeLower = activity.sportType.toLowerCase();
      const responseLower = aiResponse.toLowerCase();
      
      // Vérifier si l'ID ou le titre est mentionné dans la réponse
      if (aiResponse.includes(activityId) || 
          responseLower.includes(titleLower) ||
          (responseLower.includes(sportTypeLower) && responseLower.includes(activity.location.toLowerCase()))) {
        // Éviter les doublons
        if (!suggestedActivities.find(a => a.id === activityId)) {
          suggestedActivities.push({
            id: activityId,
            title: activity.title,
            sportType: activity.sportType,
            location: activity.location,
            date: activity.date,
            time: activity.time,
            participants: activity.participants || 0,
            maxParticipants: activity.participants || 10,
            level: activity.level,
            matchScore: 85 + Math.floor(Math.random() * 15), // Score entre 85 et 100
          });
        }
      }
    });

    // Chercher des références aux utilisateurs dans la réponse par ID ou nom
    users.forEach(user => {
      const userId = user._id.toString();
      const nameLower = user.name.toLowerCase();
      const responseLower = aiResponse.toLowerCase();
      
      // Vérifier si l'ID ou le nom est mentionné dans la réponse
      if (aiResponse.includes(userId) || 
          responseLower.includes(nameLower)) {
        // Éviter les doublons
        if (!suggestedUsers.find(u => u.id === userId)) {
          suggestedUsers.push({
            id: userId,
            name: user.name,
            profileImageUrl: user.profileImageUrl,
            sport: user.sportsInterests?.[0] || 'Sport',
            distance: 'Proche',
            matchScore: 80 + Math.floor(Math.random() * 20),
            bio: user.about,
            availability: 'Disponible',
          });
        }
      }
    });

    // Si aucune suggestion n'a été trouvée mais que l'utilisateur demande des activités/partenaires,
    // suggérer les meilleures correspondances basées sur les sports préférés
    if (suggestedActivities.length === 0 && suggestedUsers.length === 0) {
      const responseLower = aiResponse.toLowerCase();
      
      // Si la demande concerne des activités
      if (responseLower.includes('activité') || responseLower.includes('activite') || 
          responseLower.includes('groupe') || responseLower.includes('rejoindre')) {
        // Prendre les 3 premières activités disponibles
        activities.slice(0, 3).forEach(activity => {
          suggestedActivities.push({
            id: activity._id.toString(),
            title: activity.title,
            sportType: activity.sportType,
            location: activity.location,
            date: activity.date,
            time: activity.time,
            participants: activity.participants || 0,
            maxParticipants: activity.participants || 10,
            level: activity.level,
            matchScore: 80 + Math.floor(Math.random() * 15),
          });
        });
      }
      
      // Si la demande concerne des partenaires
      if (responseLower.includes('partenaire') || responseLower.includes('course') || 
          responseLower.includes('running') || responseLower.includes('coureur')) {
        // Prendre les 3 premiers utilisateurs disponibles
        users.slice(0, 3).forEach(user => {
          suggestedUsers.push({
            id: user._id.toString(),
            name: user.name,
            profileImageUrl: user.profileImageUrl,
            sport: user.sportsInterests?.[0] || 'Sport',
            distance: 'Proche',
            matchScore: 75 + Math.floor(Math.random() * 20),
            bio: user.about,
            availability: 'Disponible',
          });
        });
      }
    }

    // Extraire les options de la réponse (si l'IA suggère des actions)
    if (aiResponse.includes('?')) {
      const sentences = aiResponse.split(/[.!?]/);
      sentences.forEach(sentence => {
        const trimmed = sentence.trim();
        if (trimmed.length > 10 && trimmed.length < 50 && !trimmed.includes('ID:')) {
          options.push(trimmed);
        }
      });
      // Limiter à 3 options
      options.splice(3);
    }

    return {
      message: aiResponse,
      suggestedActivities: suggestedActivities.length > 0 ? suggestedActivities : undefined,
      suggestedUsers: suggestedUsers.length > 0 ? suggestedUsers : undefined,
      options: options.length > 0 ? options : undefined,
    };
  }

  /**
   * Génère une réponse de fallback sans utiliser l'API OpenAI
   * Utilisé quand le quota est dépassé ou l'API est indisponible
   */
  private generateFallbackResponse(
    userMessage: string,
    user: any,
    activities: any[],
    users: any[],
  ): ChatResponseDto {
    const messageLower = userMessage.toLowerCase();
    const suggestedActivities: SuggestedActivityDto[] = [];
    const suggestedUsers: SuggestedUserDto[] = [];
    let responseMessage = '';
    const options: string[] = [];

    // Analyser le message de l'utilisateur pour déterminer l'intention
    if (messageLower.includes('partenaire') || messageLower.includes('course') || 
        messageLower.includes('running') || messageLower.includes('coureur') ||
        messageLower.includes('trouver') && messageLower.includes('partenaire')) {
      
      // Rechercher des utilisateurs qui correspondent aux sports préférés de l'utilisateur
      const userSports = user?.sportsInterests || [];
      const matchingUsers = users.filter(u => {
        if (!u.sportsInterests || u.sportsInterests.length === 0) return false;
        return u.sportsInterests.some((sport: string) => 
          userSports.some((userSport: string) => 
            sport.toLowerCase().includes(userSport.toLowerCase()) ||
            userSport.toLowerCase().includes(sport.toLowerCase())
          )
        );
      });

      // Si on cherche spécifiquement la course
      if (messageLower.includes('course') || messageLower.includes('running')) {
        const runningUsers = users.filter(u => 
          u.sportsInterests?.some((s: string) => 
            s.toLowerCase().includes('running') || 
            s.toLowerCase().includes('course')
          )
        );
        
        runningUsers.slice(0, 3).forEach(user => {
          suggestedUsers.push({
            id: user._id.toString(),
            name: user.name,
            profileImageUrl: user.profileImageUrl,
            sport: 'Running',
            distance: 'Proche',
            matchScore: 85 + Math.floor(Math.random() * 15),
            bio: user.about || 'Passionné de course à pied',
            availability: 'Disponible',
          });
        });

        responseMessage = runningUsers.length > 0
          ? `J'ai trouvé ${runningUsers.length} coureur(s) qui pourraient t'intéresser ! Voici quelques partenaires de course qui correspondent à tes préférences.`
          : `Je n'ai pas trouvé de coureurs spécifiques pour le moment, mais voici quelques utilisateurs actifs qui pourraient t'intéresser.`;
      } else {
        // Utilisateurs correspondant aux sports préférés
        (matchingUsers.length > 0 ? matchingUsers : users).slice(0, 3).forEach(user => {
          suggestedUsers.push({
            id: user._id.toString(),
            name: user.name,
            profileImageUrl: user.profileImageUrl,
            sport: user.sportsInterests?.[0] || 'Sport',
            distance: 'Proche',
            matchScore: 80 + Math.floor(Math.random() * 20),
            bio: user.about || 'Passionné de sport',
            availability: 'Disponible',
          });
        });

        responseMessage = matchingUsers.length > 0
          ? `J'ai trouvé ${matchingUsers.length} partenaire(s) qui partagent tes intérêts sportifs ! Voici quelques suggestions basées sur tes préférences.`
          : `Voici quelques utilisateurs actifs qui pourraient t'intéresser comme partenaires de sport.`;
      }

      options.push('Voir plus de partenaires', 'Filtrer par sport', 'Rechercher une activité');
    } 
    else if (messageLower.includes('activité') || messageLower.includes('activite') || 
             messageLower.includes('groupe') || messageLower.includes('rejoindre') ||
             messageLower.includes('événement') || messageLower.includes('evenement')) {
      
      // Filtrer les activités selon les préférences de l'utilisateur
      const userSports = user?.sportsInterests || [];
      const matchingActivities = activities.filter(activity => {
        if (userSports.length === 0) return true;
        return userSports.some((sport: string) => 
          activity.sportType.toLowerCase().includes(sport.toLowerCase()) ||
          sport.toLowerCase().includes(activity.sportType.toLowerCase())
        );
      });

      (matchingActivities.length > 0 ? matchingActivities : activities).slice(0, 3).forEach(activity => {
        suggestedActivities.push({
          id: activity._id.toString(),
          title: activity.title,
          sportType: activity.sportType,
          location: activity.location,
          date: activity.date,
          time: activity.time,
          participants: activity.participants || 0,
          maxParticipants: activity.participants || 10,
          level: activity.level,
          matchScore: 85 + Math.floor(Math.random() * 15),
        });
      });

      responseMessage = matchingActivities.length > 0
        ? `J'ai trouvé ${matchingActivities.length} activité(s) qui correspondent à tes préférences ! Voici quelques suggestions d'activités de groupe.`
        : `Voici quelques activités de groupe disponibles dans l'application.`;

      options.push('Voir plus d\'activités', 'Filtrer par sport', 'Créer une activité');
    }
    else if (messageLower.includes('sport') || messageLower.includes('nouveau') || 
             messageLower.includes('découvrir') || messageLower.includes('decouvrir')) {
      
      // Suggérer des sports basés sur les activités disponibles
      const availableSports = [...new Set(activities.map(a => a.sportType))];
      responseMessage = `Voici les sports disponibles dans l'application : ${availableSports.slice(0, 5).join(', ')}. `;
      responseMessage += `Tu peux demander à trouver des partenaires ou des activités pour l'un de ces sports !`;
      
      options.push('Trouver un partenaire', 'Rejoindre une activité', 'Créer une activité');
    }
    else {
      // Réponse générique
      responseMessage = `Je comprends ta demande. Je peux t'aider à trouver des partenaires de sport ou des activités de groupe. `;
      responseMessage += `Dis-moi quel sport t'intéresse ou si tu veux rejoindre une activité !`;
      
      options.push('Trouver un partenaire de course', 'Rejoindre une activité de groupe', 'Découvrir de nouveaux sports');
    }

    return {
      message: responseMessage,
      suggestedActivities: suggestedActivities.length > 0 ? suggestedActivities : undefined,
      suggestedUsers: suggestedUsers.length > 0 ? suggestedUsers : undefined,
      options: options.length > 0 ? options : undefined,
    };
  }
}
```

### 3. Controller

**src/ai-matchmaker/ai-matchmaker.controller.ts**
```typescript
import { Controller, Post, Body, UseGuards, Request } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { AIMatchmakerService } from './ai-matchmaker.service';
import { ChatRequestDto } from './dto/chat-request.dto';
import { ChatResponseDto } from './dto/chat-response.dto';

@Controller('ai-matchmaker')
@UseGuards(JwtAuthGuard)
export class AIMatchmakerController {
  constructor(private readonly aiMatchmakerService: AIMatchmakerService) {}

  @Post('chat')
  async chat(
    @Request() req,
    @Body() chatRequest: ChatRequestDto,
  ): Promise<ChatResponseDto> {
    return this.aiMatchmakerService.chat(req.user.userId, chatRequest);
  }
}
```

### 4. Module

**src/ai-matchmaker/ai-matchmaker.module.ts**
```typescript
import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { AIMatchmakerController } from './ai-matchmaker.controller';
import { AIMatchmakerService } from './ai-matchmaker.service';
import { User, UserSchema } from '../users/schemas/user.schema';
import { Activity, ActivitySchema } from '../activities/schemas/activity.schema';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: User.name, schema: UserSchema },
      { name: Activity.name, schema: ActivitySchema },
    ]),
  ],
  controllers: [AIMatchmakerController],
  providers: [AIMatchmakerService],
})
export class AIMatchmakerModule {}
```

### 5. Ajouter le module à app.module.ts

Dans votre `src/app.module.ts`, ajoutez:

```typescript
import { AIMatchmakerModule } from './ai-matchmaker/ai-matchmaker.module';

@Module({
  imports: [
    // ... autres modules
    AIMatchmakerModule,
  ],
  // ...
})
export class AppModule {}
```

### 6. Variables d'environnement

Ajoutez dans votre `.env`:

```
OPENAI_API_KEY=your_openai_api_key_here
```

### 7. Installation des dépendances

```bash
npm install axios
```

## Notes importantes

1. **Sécurité**: L'API key ChatGPT est actuellement hardcodée dans le service. Il est recommandé de la déplacer vers les variables d'environnement.

2. **Performance**: Le service récupère toutes les activités et utilisateurs à chaque requête. Pour améliorer les performances, vous pourriez:
   - Mettre en cache les données
   - Limiter le nombre de résultats
   - Utiliser des requêtes plus spécifiques basées sur la demande de l'utilisateur

3. **Parsing de la réponse**: Le parsing actuel est basique. Vous pourriez améliorer cela en:
   - Utilisant des prompts plus structurés pour ChatGPT
   - Utilisant des fonctions/tools de ChatGPT pour un format JSON structuré
   - Implémentant un parsing plus sophistiqué avec regex ou NLP

4. **Gestion d'erreurs**: Ajoutez une meilleure gestion d'erreurs et des logs pour le débogage.

5. **Gestion du quota API (Erreur 429)**: 
   - Si vous recevez une erreur 429 (quota dépassé), voici les solutions possibles:
     - **Vérifier votre compte OpenAI**: Connectez-vous à https://platform.openai.com pour vérifier votre quota et ajouter des crédits
     - **Utiliser un modèle moins cher**: `gpt-3.5-turbo` est moins cher que `gpt-4`
     - **Implémenter un système de cache**: Mettre en cache les réponses pour éviter les appels répétés
     - **Limiter les requêtes**: Ajouter un rate limiting côté backend
     - **Utiliser une alternative**: Considérer d'autres services AI comme:
       - Google Gemini API (gratuit jusqu'à un certain quota)
       - Anthropic Claude API
       - Hugging Face Inference API
       - Solutions locales avec des modèles open-source

6. **Amélioration de la gestion d'erreur 429**:
   - Le code a été mis à jour pour mieux gérer l'erreur 429
   - Les messages d'erreur sont maintenant plus clairs pour l'utilisateur
   - Considérez implémenter un système de retry avec backoff exponentiel

