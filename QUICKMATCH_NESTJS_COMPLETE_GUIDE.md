# 🚀 Guide Complet NestJS pour QuickMatch

## 📋 Vue d'ensemble

QuickMatch permet aux utilisateurs de découvrir et matcher avec d'autres utilisateurs basé sur leurs sports/intérêts communs. Le système :
- Filtre les profils par sports/intérêts communs (au moins un en commun)
- Exclut les profils déjà likés, passés ou matchés
- Enregistre les likes/passes dans la base de données
- Détecte les matches mutuels

---

## 📁 Structure des Fichiers

```
quick-match/
├── dto/
│   ├── like-profile.dto.ts
│   └── pass-profile.dto.ts
├── schemas/
│   ├── like.schema.ts
│   ├── match.schema.ts
│   └── pass.schema.ts
├── quick-match.controller.ts
├── quick-match.service.ts
└── quick-match.module.ts
```

---

## 1️⃣ Schémas Mongoose

### like.schema.ts

```typescript
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type LikeDocument = Like & Document;

@Schema({ timestamps: true })
export class Like {
  @Prop({ required: true, type: Types.ObjectId, ref: 'User' })
  fromUser: Types.ObjectId; // Utilisateur qui a liké

  @Prop({ required: true, type: Types.ObjectId, ref: 'User' })
  toUser: Types.ObjectId; // Utilisateur qui a été liké

  @Prop({ default: false })
  isMatch: boolean; // true si c'est un match mutuel
}

export const LikeSchema = SchemaFactory.createForClass(Like);

// Index pour éviter les doublons
LikeSchema.index({ fromUser: 1, toUser: 1 }, { unique: true });

// Index pour les requêtes de matching
LikeSchema.index({ toUser: 1, fromUser: 1, isMatch: 1 });
```

### match.schema.ts

```typescript
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type MatchDocument = Match & Document;

@Schema({ timestamps: true })
export class Match {
  @Prop({ required: true, type: Types.ObjectId, ref: 'User' })
  user1: Types.ObjectId; // Premier utilisateur du match

  @Prop({ required: true, type: Types.ObjectId, ref: 'User' })
  user2: Types.ObjectId; // Deuxième utilisateur du match

  @Prop({ default: false })
  hasChatted: boolean; // true si les utilisateurs ont démarré une conversation

  @Prop({ type: Types.ObjectId, ref: 'Chat' })
  chatId?: Types.ObjectId; // ID du chat créé pour ce match
}

export const MatchSchema = SchemaFactory.createForClass(Match);

// Index pour éviter les doublons (user1-user2 et user2-user1 sont considérés comme le même match)
MatchSchema.index({ user1: 1, user2: 1 }, { unique: true });

// Index pour les requêtes de matching
MatchSchema.index({ user1: 1 });
MatchSchema.index({ user2: 1 });
```

### pass.schema.ts

```typescript
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type PassDocument = Pass & Document;

@Schema({ timestamps: true })
export class Pass {
  @Prop({ required: true, type: Types.ObjectId, ref: 'User' })
  fromUser: Types.ObjectId; // Utilisateur qui a passé

  @Prop({ required: true, type: Types.ObjectId, ref: 'User' })
  toUser: Types.ObjectId; // Utilisateur qui a été passé
}

export const PassSchema = SchemaFactory.createForClass(Pass);

// Index pour éviter les doublons
PassSchema.index({ fromUser: 1, toUser: 1 }, { unique: true });

// Index pour les requêtes de filtrage
PassSchema.index({ fromUser: 1 });
```

---

## 2️⃣ DTOs

### like-profile.dto.ts

```typescript
import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class LikeProfileDto {
  @ApiProperty({
    description: 'ID du profil à liker',
    example: '507f1f77bcf86cd799439011',
  })
  @IsString()
  @IsNotEmpty()
  profileId: string;
}
```

### pass-profile.dto.ts

```typescript
import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class PassProfileDto {
  @ApiProperty({
    description: 'ID du profil à passer',
    example: '507f1f77bcf86cd799439011',
  })
  @IsString()
  @IsNotEmpty()
  profileId: string;
}
```

---

## 3️⃣ Service (Logique Métier)

### quick-match.service.ts

```typescript
import {
  Injectable,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';
import { User, UserDocument } from '../users/schemas/user.schema';
import { Activity, ActivityDocument } from '../activities/schemas/activity.schema';
import { Like, LikeDocument } from './schemas/like.schema';
import { Match, MatchDocument } from './schemas/match.schema';
import { Pass, PassDocument } from './schemas/pass.schema';

@Injectable()
export class QuickMatchService {
  constructor(
    @InjectModel(User.name) private userModel: Model<UserDocument>,
    @InjectModel(Activity.name) private activityModel: Model<ActivityDocument>,
    @InjectModel(Like.name) private likeModel: Model<LikeDocument>,
    @InjectModel(Match.name) private matchModel: Model<MatchDocument>,
    @InjectModel(Pass.name) private passModel: Model<PassDocument>,
  ) {}

  /**
   * Récupère les profils compatibles avec l'utilisateur connecté
   * 
   * LOGIQUE DE FILTRAGE :
   * 1. Récupère les sportsInterests de l'utilisateur connecté
   * 2. Récupère les activités créées par l'utilisateur
   * 3. Combine : sportsInterests + sports des activités = liste complète des sports
   * 4. Filtre les autres utilisateurs qui ont AU MOINS UN sport en commun
   * 5. Exclut les profils déjà likés, passés ou matchés
   * 
   * @param userId ID de l'utilisateur connecté
   * @param page Numéro de page (défaut: 1)
   * @param limit Nombre de résultats par page (défaut: 20)
   */
  async getCompatibleProfiles(
    userId: string,
    page: number = 1,
    limit: number = 20,
  ): Promise<{ profiles: any[]; total: number; page: number; totalPages: number }> {
    // 1. Récupérer l'utilisateur connecté
    const currentUser = await this.userModel.findById(userId).exec();
    if (!currentUser) {
      throw new NotFoundException('User not found');
    }

    // 2. Récupérer les sportsInterests de l'utilisateur
    const userSportsInterests = currentUser.sportsInterests || [];

    // 3. Récupérer les activités créées par l'utilisateur
    const userActivities = await this.activityModel
      .find({ creator: new Types.ObjectId(userId) })
      .exec();

    // 4. Extraire les sports des activités de l'utilisateur
    const activitySports = userActivities
      .map((activity) => activity.sportType)
      .filter(Boolean); // Filtrer les valeurs vides

    // 5. Combiner sportsInterests + sports des activités (sans doublons)
    const allUserSports = [
      ...new Set([...userSportsInterests, ...activitySports]),
    ].filter(Boolean);

    // Si l'utilisateur n'a aucun sport, retourner une liste vide
    if (allUserSports.length === 0) {
      return { profiles: [], total: 0, page, totalPages: 0 };
    }

    // 6. Récupérer les IDs des profils déjà likés, passés ou matchés
    const [likedProfiles, passedProfiles, matchedProfiles] = await Promise.all([
      this.likeModel
        .find({ fromUser: new Types.ObjectId(userId) })
        .select('toUser')
        .exec(),
      this.passModel
        .find({ fromUser: new Types.ObjectId(userId) })
        .select('toUser')
        .exec(),
      this.matchModel
        .find({
          $or: [
            { user1: new Types.ObjectId(userId) },
            { user2: new Types.ObjectId(userId) },
          ],
        })
        .select('user1 user2')
        .exec(),
    ]);

    // Construire la liste des IDs à exclure
    const excludedUserIds = new Set<string>();
    likedProfiles.forEach((like) => excludedUserIds.add(like.toUser.toString()));
    passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString()));
    matchedProfiles.forEach((match) => {
      excludedUserIds.add(
        match.user1.toString() === userId
          ? match.user2.toString()
          : match.user1.toString(),
      );
    });

    // 7. Construire la requête MongoDB
    const excludedIds = [
      new Types.ObjectId(userId), // Exclure l'utilisateur connecté
      ...Array.from(excludedUserIds).map((id) => new Types.ObjectId(id)),
    ];

    // 8. Requête pour trouver les utilisateurs avec au moins un sport en commun
    // Utiliser $in avec regex pour la recherche case-insensitive
    const query: any = {
      _id: { $nin: excludedIds },
    };

    // Filtrer par sports communs (au moins un sport en commun)
    if (allUserSports.length > 0) {
      query.sportsInterests = {
        $in: allUserSports.map((sport) => new RegExp(`^${sport}$`, 'i')),
      };
    }

    // 9. Compter le total de profils compatibles
    const total = await this.userModel.countDocuments(query).exec();

    // 10. Récupérer les profils avec pagination
    const skip = (page - 1) * limit;
    const allUsers = await this.userModel
      .find(query)
      .skip(skip)
      .limit(limit)
      .exec();

    // 11. Double vérification : filtrer les utilisateurs qui ont au moins un sport en commun
    const compatibleProfiles = allUsers.filter((user) => {
      const userSports = user.sportsInterests || [];
      
      // Vérifier s'il y a au moins un sport en commun (case-insensitive)
      const hasCommonSport = allUserSports.some((sport) =>
        userSports.some(
          (userSport) =>
            userSport.toLowerCase().trim() === sport.toLowerCase().trim(),
        ),
      );

      return hasCommonSport;
    });

    // 12. Enrichir avec les données des activités et distance
    const enrichedProfiles = await Promise.all(
      compatibleProfiles.map(async (user) => {
        // Compter les activités créées par cet utilisateur
        const activitiesCount = await this.activityModel.countDocuments({
          creator: user._id,
        }).exec();

        // Calculer la distance (si on a les coordonnées GPS)
        const distance = this.calculateDistance(currentUser, user);

        return {
          ...user.toObject(),
          activitiesCount,
          distance: distance !== null ? `${distance.toFixed(1)} km` : null,
        };
      }),
    );

    // 13. Trier par pertinence (nombre de sports en commun, distance, etc.)
    const sortedProfiles = this.sortByRelevance(enrichedProfiles, allUserSports);

    const totalPages = Math.ceil(total / limit);

    return {
      profiles: sortedProfiles,
      total,
      page,
      totalPages,
    };
  }

  /**
   * Calcule la distance entre deux utilisateurs en utilisant la formule de Haversine
   * Retourne la distance en kilomètres
   */
  private calculateDistance(
    user1: UserDocument,
    user2: UserDocument,
  ): number | null {
    // Vérifier si les deux utilisateurs ont des coordonnées GPS
    if (
      !user1.latitude ||
      !user1.longitude ||
      !user2.latitude ||
      !user2.longitude
    ) {
      return null;
    }

    const R = 6371; // Rayon de la Terre en kilomètres
    const dLat = this.toRadians(user2.latitude - user1.latitude);
    const dLon = this.toRadians(user2.longitude - user1.longitude);

    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRadians(user1.latitude)) *
        Math.cos(this.toRadians(user2.latitude)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;

    return distance;
  }

  /**
   * Convertit des degrés en radians
   */
  private toRadians(degrees: number): number {
    return degrees * (Math.PI / 180);
  }

  /**
   * Trie les profils par pertinence
   */
  private sortByRelevance(profiles: any[], userSports: string[]): any[] {
    return profiles.sort((a, b) => {
      const scoreA = this.calculateRelevanceScore(a, userSports);
      const scoreB = this.calculateRelevanceScore(b, userSports);
      return scoreB - scoreA; // Tri décroissant
    });
  }

  /**
   * Calcule un score de pertinence basé sur :
   * - Nombre de sports en commun (poids: 10)
   * - Nombre d'activités (poids: 1)
   * - Distance (poids: 5 max)
   */
  private calculateRelevanceScore(profile: any, userSports: string[]): number {
    const profileSports = profile.sportsInterests || [];

    // Compter les sports en commun (case-insensitive)
    const commonSports = userSports.filter((sport) =>
      profileSports.some(
        (ps) => ps.toLowerCase().trim() === sport.toLowerCase().trim(),
      ),
    ).length;

    // Score basé sur les sports en commun (poids: 10)
    let score = commonSports * 10;

    // Bonus pour le nombre d'activités (poids: 1)
    score += (profile.activitiesCount || 0) * 1;

    // Bonus pour la distance (plus proche = meilleur score)
    if (profile.distance) {
      const distanceKm = parseFloat(profile.distance.replace(' km', ''));
      if (distanceKm !== null && !isNaN(distanceKm)) {
        // Plus la distance est petite, plus le score est élevé
        // Max 5 points bonus pour distance < 1km
        const distanceBonus = Math.max(0, 5 - distanceKm);
        score += distanceBonus;
      }
    }

    return score;
  }

  /**
   * Enregistre un like d'un utilisateur vers un profil
   * Vérifie si c'est un match mutuel et crée un Match si nécessaire
   * 
   * IMPORTANT : Cette méthode doit être appelée à chaque fois qu'un utilisateur like un profil
   */
  async likeProfile(userId: string, profileId: string): Promise<{ isMatch: boolean }> {
    // Vérifier que les utilisateurs existent
    const user = await this.userModel.findById(userId).exec();
    const profile = await this.userModel.findById(profileId).exec();

    if (!user || !profile) {
      throw new NotFoundException('User or profile not found');
    }

    // Vérifier si le like existe déjà
    const existingLike = await this.likeModel
      .findOne({
        fromUser: new Types.ObjectId(userId),
        toUser: new Types.ObjectId(profileId),
      })
      .exec();

    if (existingLike) {
      throw new ConflictException('Profile already liked');
    }

    // Vérifier si l'utilisateur a déjà passé ce profil
    const existingPass = await this.passModel
      .findOne({
        fromUser: new Types.ObjectId(userId),
        toUser: new Types.ObjectId(profileId),
      })
      .exec();

    if (existingPass) {
      throw new ConflictException('Cannot like a profile that was passed');
    }

    // Vérifier si c'est un match mutuel (l'autre utilisateur a déjà liké)
    const reverseLike = await this.likeModel
      .findOne({
        fromUser: new Types.ObjectId(profileId),
        toUser: new Types.ObjectId(userId),
      })
      .exec();

    const isMatch = !!reverseLike;

    // Créer le like
    const like = new this.likeModel({
      fromUser: new Types.ObjectId(userId),
      toUser: new Types.ObjectId(profileId),
      isMatch,
    });
    await like.save();

    // Si c'est un match, créer l'enregistrement Match
    if (isMatch) {
      // Mettre à jour le like inverse
      reverseLike.isMatch = true;
      await reverseLike.save();

      // Créer le match (s'assurer que user1 < user2 pour éviter les doublons)
      const user1Id = userId < profileId ? userId : profileId;
      const user2Id = userId < profileId ? profileId : userId;

      const existingMatch = await this.matchModel
        .findOne({
          user1: new Types.ObjectId(user1Id),
          user2: new Types.ObjectId(user2Id),
        })
        .exec();

      if (!existingMatch) {
        const match = new this.matchModel({
          user1: new Types.ObjectId(user1Id),
          user2: new Types.ObjectId(user2Id),
          hasChatted: false,
        });
        await match.save();
      }
    }

    return { isMatch };
  }

  /**
   * Enregistre un pass (utilisateur passe ce profil)
   * 
   * IMPORTANT : Cette méthode doit être appelée à chaque fois qu'un utilisateur passe un profil
   */
  async passProfile(userId: string, profileId: string): Promise<void> {
    // Vérifier que les utilisateurs existent
    const user = await this.userModel.findById(userId).exec();
    const profile = await this.userModel.findById(profileId).exec();

    if (!user || !profile) {
      throw new NotFoundException('User or profile not found');
    }

    // Vérifier si le pass existe déjà
    const existingPass = await this.passModel
      .findOne({
        fromUser: new Types.ObjectId(userId),
        toUser: new Types.ObjectId(profileId),
      })
      .exec();

    if (existingPass) {
      throw new ConflictException('Profile already passed');
    }

    // Vérifier si l'utilisateur a déjà liké ce profil
    const existingLike = await this.likeModel
      .findOne({
        fromUser: new Types.ObjectId(userId),
        toUser: new Types.ObjectId(profileId),
      })
      .exec();

    if (existingLike) {
      throw new ConflictException('Cannot pass a profile that was liked');
    }

    // Créer le pass
    const pass = new this.passModel({
      fromUser: new Types.ObjectId(userId),
      toUser: new Types.ObjectId(profileId),
    });
    await pass.save();
  }

  /**
   * Récupère un profil par ID avec les données enrichies
   */
  async getProfileById(profileId: string): Promise<any> {
    const profile = await this.userModel.findById(profileId).exec();

    if (!profile) {
      throw new NotFoundException('Profile not found');
    }

    // Compter les activités créées par cet utilisateur
    const activitiesCount = await this.activityModel.countDocuments({
      creator: profile._id,
    }).exec();

    return {
      ...profile.toObject(),
      activitiesCount,
    };
  }

  /**
   * Récupère tous les matches d'un utilisateur
   */
  async getMatches(userId: string): Promise<any[]> {
    const matches = await this.matchModel
      .find({
        $or: [
          { user1: new Types.ObjectId(userId) },
          { user2: new Types.ObjectId(userId) },
        ],
      })
      .populate('user1', 'name email profileImageUrl')
      .populate('user2', 'name email profileImageUrl')
      .sort({ createdAt: -1 })
      .exec();

    return matches.map((match) => {
      const matchObj = match.toObject();
      const otherUser =
        matchObj.user1._id.toString() === userId ? matchObj.user2 : matchObj.user1;
      return {
        matchId: matchObj._id.toString(),
        user: otherUser,
        hasChatted: matchObj.hasChatted,
        chatId: matchObj.chatId?.toString(),
        createdAt: matchObj.createdAt,
      };
    });
  }
}
```

---

## 4️⃣ Controller

### quick-match.controller.ts

```typescript
import {
  Controller,
  Get,
  Post,
  Body,
  UseGuards,
  Request,
  Query,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiQuery,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { QuickMatchService } from './quick-match.service';
import { LikeProfileDto } from './dto/like-profile.dto';
import { PassProfileDto } from './dto/pass-profile.dto';

@ApiTags('quick-match')
@Controller('quick-match')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class QuickMatchController {
  constructor(private readonly quickMatchService: QuickMatchService) {}

  @Get('profiles')
  @ApiOperation({ 
    summary: 'Get compatible profiles based on sports interests',
    description: 'Returns users who have at least one common sport/interest. Excludes already liked, passed, or matched profiles.'
  })
  @ApiQuery({ 
    name: 'page', 
    required: false, 
    type: Number, 
    description: 'Page number (default: 1)' 
  })
  @ApiQuery({ 
    name: 'limit', 
    required: false, 
    type: Number, 
    description: 'Results per page (default: 20)' 
  })
  @ApiResponse({
    status: 200,
    description: 'List of compatible profiles retrieved successfully',
  })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  @ApiResponse({ status: 404, description: 'User not found' })
  async getProfiles(
    @Request() req,
    @Query('page') page?: string,
    @Query('limit') limit?: string,
  ) {
    const userId = req.user._id.toString();
    const pageNum = page ? parseInt(page, 10) : 1;
    const limitNum = limit ? parseInt(limit, 10) : 20;

    const result = await this.quickMatchService.getCompatibleProfiles(
      userId,
      pageNum,
      limitNum,
    );

    return {
      profiles: result.profiles.map((profile) => this.mapToResponse(profile)),
      pagination: {
        total: result.total,
        page: result.page,
        totalPages: result.totalPages,
        limit: limitNum,
      },
    };
  }

  @Post('like')
  @ApiOperation({ 
    summary: 'Like a profile',
    description: 'Records a like. If the other user also liked you, creates a match.'
  })
  @ApiResponse({
    status: 200,
    description: 'Profile liked successfully',
  })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  @ApiResponse({ status: 404, description: 'User or profile not found' })
  @ApiResponse({ status: 409, description: 'Profile already liked or passed' })
  async likeProfile(@Request() req, @Body() body: LikeProfileDto) {
    const userId = req.user._id.toString();
    const { profileId } = body;

    // Enregistrer le like (retourne isMatch)
    const { isMatch } = await this.quickMatchService.likeProfile(userId, profileId);

    if (isMatch) {
      // Récupérer le profil matché
      const matchedProfile = await this.quickMatchService.getProfileById(profileId);
      if (matchedProfile) {
        return {
          isMatch: true,
          matchedProfile: this.mapToResponse(matchedProfile),
        };
      }
    }

    return {
      isMatch: false,
      matchedProfile: null,
    };
  }

  @Post('pass')
  @ApiOperation({ 
    summary: 'Pass on a profile',
    description: 'Records a pass. This profile will not appear again in future searches.'
  })
  @ApiResponse({
    status: 200,
    description: 'Profile passed successfully',
  })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  @ApiResponse({ status: 404, description: 'User or profile not found' })
  @ApiResponse({ status: 409, description: 'Profile already passed or liked' })
  async passProfile(@Request() req, @Body() body: PassProfileDto) {
    const userId = req.user._id.toString();
    const { profileId } = body;

    await this.quickMatchService.passProfile(userId, profileId);

    return { success: true };
  }

  @Get('matches')
  @ApiOperation({ summary: 'Get all matches for the current user' })
  @ApiResponse({
    status: 200,
    description: 'List of matches retrieved successfully',
  })
  @ApiResponse({ status: 401, description: 'Unauthorized' })
  async getMatches(@Request() req) {
    const userId = req.user._id.toString();
    const matches = await this.quickMatchService.getMatches(userId);
    return matches;
  }

  /**
   * Mappe un profil utilisateur vers le format de réponse attendu par Android
   */
  private mapToResponse(profile: any) {
    return {
      _id: profile._id.toString(),
      id: profile._id.toString(),
      name: profile.name,
      age: this.calculateAge(profile.dateOfBirth),
      email: profile.email,
      avatarUrl: profile.profileImageUrl || profile.profileImageThumbnailUrl,
      coverImageUrl: profile.profileImageUrl || profile.profileImageThumbnailUrl,
      location: profile.location,
      distance: profile.distance || null,
      bio: profile.about,
      about: profile.about,
      sportsInterests: profile.sportsInterests || [],
      sports: this.mapSports(profile.sportsInterests),
      interests: profile.sportsInterests || [], // Utiliser sportsInterests comme interests
      rating: 0, // À implémenter si vous avez un système de rating
      activitiesJoined: profile.activitiesCount || 0,
      profileImageUrl: profile.profileImageUrl,
    };
  }

  /**
   * Calcule l'âge à partir de la date de naissance
   */
  private calculateAge(dateOfBirth: string | undefined): number {
    if (!dateOfBirth) return 0;
    try {
      const today = new Date();
      const birthDate = new Date(dateOfBirth);
      let age = today.getFullYear() - birthDate.getFullYear();
      const monthDiff = today.getMonth() - birthDate.getMonth();
      if (
        monthDiff < 0 ||
        (monthDiff === 0 && today.getDate() < birthDate.getDate())
      ) {
        age--;
      }
      return age > 0 ? age : 0;
    } catch (error) {
      return 0;
    }
  }

  /**
   * Mappe les sportsInterests vers un format détaillé avec icônes
   */
  private mapSports(sportsInterests: string[] | undefined): any[] {
    if (!sportsInterests || sportsInterests.length === 0) return [];

    return sportsInterests.map((sportName) => ({
      name: sportName,
      icon: this.getSportIcon(sportName),
      level: 'Intermediate', // Par défaut, ou récupérer depuis le profil utilisateur
    }));
  }

  /**
   * Retourne l'icône emoji pour un sport
   */
  private getSportIcon(sportName: string): string {
    const icons: { [key: string]: string } = {
      Football: '⚽',
      Basketball: '🏀',
      Running: '🏃',
      Cycling: '🚴',
      Tennis: '🎾',
      Swimming: '🏊',
      Yoga: '🧘',
      Volleyball: '🏐',
      Soccer: '⚽',
      Badminton: '🏸',
      TableTennis: '🏓',
      Golf: '⛳',
      Skiing: '⛷️',
      Snowboarding: '🏂',
      Surfing: '🏄',
      Climbing: '🧗',
      Boxing: '🥊',
      MartialArts: '🥋',
      Hiking: '🥾',
      Dance: '💃',
      Pilates: '🧘‍♀️',
      Zumba: '🎵',
      CrossFit: '💪',
    };
    return icons[sportName] || '🏃';
  }
}
```

---

## 5️⃣ Module

### quick-match.module.ts

```typescript
import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { QuickMatchController } from './quick-match.controller';
import { QuickMatchService } from './quick-match.service';
import { User, UserSchema } from '../users/schemas/user.schema';
import { Activity, ActivitySchema } from '../activities/schemas/activity.schema';
import { Like, LikeSchema } from './schemas/like.schema';
import { Match, MatchSchema } from './schemas/match.schema';
import { Pass, PassSchema } from './schemas/pass.schema';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: User.name, schema: UserSchema },
      { name: Activity.name, schema: ActivitySchema },
      { name: Like.name, schema: LikeSchema },
      { name: Match.name, schema: MatchSchema },
      { name: Pass.name, schema: PassSchema },
    ]),
  ],
  controllers: [QuickMatchController],
  providers: [QuickMatchService],
  exports: [QuickMatchService],
})
export class QuickMatchModule {}
```

---

## 6️⃣ Schéma User (Vérification)

Assurez-vous que votre schéma User contient :

```typescript
@Schema({ timestamps: true })
export class User {
  // ... autres champs ...
  
  @Prop({ type: [String], default: [] })
  sportsInterests?: string[]; // REQUIRED pour QuickMatch
  
  @Prop()
  latitude?: number; // Optionnel pour calculer la distance
  
  @Prop()
  longitude?: number; // Optionnel pour calculer la distance
  
  @Prop()
  dateOfBirth?: string; // Pour calculer l'âge
  
  @Prop()
  about?: string; // Bio de l'utilisateur
  
  @Prop()
  profileImageUrl?: string; // Avatar
}
```

---

## 7️⃣ Schéma Activity (Vérification)

Assurez-vous que votre schéma Activity contient :

```typescript
@Schema({ timestamps: true })
export class Activity {
  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  creator: Types.ObjectId; // REQUIRED pour filtrer les activités
  
  @Prop({ required: true })
  sportType: string; // REQUIRED - "Football", "Basketball", etc.
  
  // ... autres champs ...
}
```

---

## 🔍 Logique de Filtrage Détaillée

### Étape par étape :

1. **Récupérer l'utilisateur connecté**
   ```typescript
   const currentUser = await this.userModel.findById(userId);
   ```

2. **Récupérer ses sportsInterests**
   ```typescript
   const userSportsInterests = currentUser.sportsInterests || [];
   // Exemple: ["Football", "Basketball"]
   ```

3. **Récupérer ses activités créées**
   ```typescript
   const userActivities = await this.activityModel.find({ creator: userId });
   // Exemple: [Activity(sportType: "Running"), Activity(sportType: "Tennis")]
   ```

4. **Combiner les sports**
   ```typescript
   const allUserSports = [...new Set([...userSportsInterests, ...activitySports])];
   // Résultat: ["Football", "Basketball", "Running", "Tennis"]
   ```

5. **Récupérer les profils exclus (likés, passés, matchés)**
   ```typescript
   const excludedIds = [userId, ...likedIds, ...passedIds, ...matchedIds];
   ```

6. **Construire la requête MongoDB**
   ```typescript
   const query = {
     _id: { $nin: excludedIds },
     sportsInterests: { $in: allUserSports.map(s => new RegExp(`^${s}$`, 'i')) }
   };
   ```

7. **Filtrer et retourner les profils compatibles**

---

## ✅ Points Critiques à Vérifier

### 1. **Exclusion des profils likés/passés**

Le backend DOIT exclure les profils dans `getCompatibleProfiles()` :
- ✅ Profils déjà likés par l'utilisateur
- ✅ Profils déjà passés par l'utilisateur
- ✅ Profils avec lesquels l'utilisateur a déjà matché

### 2. **Enregistrement des likes/passes**

Les méthodes `likeProfile()` et `passProfile()` DOIVENT :
- ✅ Créer un enregistrement dans la collection `Like` ou `Pass`
- ✅ Vérifier les doublons (index unique)
- ✅ Vérifier les conflits (ne pas liker un profil déjà passé)

### 3. **Détection de match**

La méthode `likeProfile()` DOIT :
- ✅ Vérifier si l'autre utilisateur a déjà liké
- ✅ Si oui, créer un enregistrement dans `Match`
- ✅ Mettre à jour les deux `Like` avec `isMatch = true`

### 4. **Format de réponse**

Le controller DOIT retourner exactement ce format :
```json
{
  "profiles": [...],
  "pagination": {
    "total": 50,
    "page": 1,
    "totalPages": 3,
    "limit": 20
  }
}
```

---

## 🧪 Tests à Effectuer

### Test 1 : Filtrage par sports communs
1. User A a `sportsInterests: ["Football", "Basketball"]`
2. User A crée une activité `sportType: "Running"`
3. User B a `sportsInterests: ["Football", "Swimming"]`
4. **Résultat attendu** : User B doit apparaître (Football en commun)

### Test 2 : Exclusion des profils likés
1. User A like User B
2. User A demande les profils
3. **Résultat attendu** : User B ne doit PAS apparaître

### Test 3 : Exclusion des profils passés
1. User A passe User B
2. User A demande les profils
3. **Résultat attendu** : User B ne doit PAS apparaître

### Test 4 : Détection de match
1. User A like User B → `isMatch: false`
2. User B like User A → `isMatch: true`, création d'un `Match`

### Test 5 : Pagination
1. Demander page 1, limit 20
2. Vérifier que `pagination.total`, `pagination.page`, `pagination.totalPages` sont corrects

---

## 📝 Checklist d'Implémentation

- [ ] Créer les schémas Mongoose (Like, Match, Pass)
- [ ] Créer les DTOs (LikeProfileDto, PassProfileDto)
- [ ] Implémenter `getCompatibleProfiles()` avec filtrage par sports communs
- [ ] Implémenter l'exclusion des profils likés/passés/matchés
- [ ] Implémenter `likeProfile()` avec détection de match
- [ ] Implémenter `passProfile()`
- [ ] Implémenter le controller avec tous les endpoints
- [ ] Enregistrer le module dans `app.module.ts`
- [ ] Tester avec Postman/Swagger
- [ ] Vérifier que les profils likés ne réapparaissent plus

---

## 🔗 Enregistrement dans app.module.ts

```typescript
import { QuickMatchModule } from './quick-match/quick-match.module';

@Module({
  imports: [
    // ... autres modules ...
    QuickMatchModule,
  ],
  // ...
})
export class AppModule {}
```

---

## 🎯 Format de Réponse pour Android

### GET /quick-match/profiles

**Réponse :**
```json
{
  "profiles": [
    {
      "_id": "507f1f77bcf86cd799439011",
      "id": "507f1f77bcf86cd799439011",
      "name": "John Doe",
      "age": 28,
      "email": "john@example.com",
      "avatarUrl": "https://...",
      "coverImageUrl": "https://...",
      "location": "New York",
      "distance": "2.5 km",
      "bio": "Love sports!",
      "about": "Love sports!",
      "sportsInterests": ["Football", "Basketball"],
      "sports": [
        {
          "name": "Football",
          "icon": "⚽",
          "level": "Intermediate"
        },
        {
          "name": "Basketball",
          "icon": "🏀",
          "level": "Intermediate"
        }
      ],
      "interests": ["Football", "Basketball"],
      "rating": 0,
      "activitiesJoined": 15,
      "profileImageUrl": "https://..."
    }
  ],
  "pagination": {
    "total": 50,
    "page": 1,
    "totalPages": 3,
    "limit": 20
  }
}
```

### POST /quick-match/like

**Requête :**
```json
{
  "profileId": "507f1f77bcf86cd799439011"
}
```

**Réponse (si match) :**
```json
{
  "isMatch": true,
  "matchedProfile": {
    "_id": "507f1f77bcf86cd799439011",
    "id": "507f1f77bcf86cd799439011",
    "name": "John Doe",
    // ... autres champs comme dans profiles
  }
}
```

**Réponse (si pas de match) :**
```json
{
  "isMatch": false,
  "matchedProfile": null
}
```

### POST /quick-match/pass

**Requête :**
```json
{
  "profileId": "507f1f77bcf86cd799439011"
}
```

**Réponse :**
```json
{
  "success": true
}
```

---

## ⚠️ Points Importants

1. **Les profils likés/passés ne réapparaîtront plus** car ils sont exclus dans `getCompatibleProfiles()`

2. **Le filtrage par sports communs** se fait en deux étapes :
   - Requête MongoDB avec `$in` et regex
   - Double vérification côté JavaScript pour être sûr

3. **Les matches sont créés automatiquement** quand deux utilisateurs se likent mutuellement

4. **La pagination** permet de charger les profils progressivement

5. **La distance** est calculée uniquement si les deux utilisateurs ont des coordonnées GPS

---

## 🚀 Démarrage Rapide

1. Créer les fichiers dans `quick-match/`
2. Copier le code des schémas, DTOs, service et controller
3. Enregistrer le module dans `app.module.ts`
4. Tester avec Postman :
   - `GET /quick-match/profiles` (avec token JWT)
   - `POST /quick-match/like` (avec body `{ "profileId": "..." }`)
   - `POST /quick-match/pass` (avec body `{ "profileId": "..." }`)

---

## 📚 Documentation Swagger

Une fois implémenté, vous pouvez tester les endpoints sur :
```
https://apinest-production.up.railway.app/docs
```

Les endpoints apparaîtront sous le tag `quick-match`.

