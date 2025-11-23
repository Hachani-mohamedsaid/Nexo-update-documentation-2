# 🚀 Guide Backend NestJS pour QuickMatch

## 📋 Exigences

QuickMatch doit :
1. **Récupérer les utilisateurs** qui ont créé ou rejoint des activités
2. **Filtrer par sports/intérêts communs** : au moins un `sportsInterests` en commun avec l'utilisateur connecté
3. **Basé sur les activités** : considérer les sports des activités créées/jointe par l'utilisateur

---

## 🔌 Endpoint : GET /quick-match/profiles

### Controller NestJS

```typescript
// quick-match.controller.ts
import { Controller, Get, UseGuards, Request } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { QuickMatchService } from './quick-match.service';

@Controller('quick-match')
@UseGuards(JwtAuthGuard)
export class QuickMatchController {
  constructor(private readonly quickMatchService: QuickMatchService) {}

  @Get('profiles')
  async getProfiles(@Request() req) {
    const userId = req.user.id;
    
    // Récupérer les profils avec filtrage par sports/intérêts communs
    const profiles = await this.quickMatchService.getCompatibleProfiles(userId);
    
    return profiles.map(profile => this.mapToResponse(profile));
  }

  private mapToResponse(profile: any) {
    return {
      _id: profile._id,
      id: profile._id,
      name: profile.name,
      age: this.calculateAge(profile.dateOfBirth),
      email: profile.email,
      avatarUrl: profile.profileImageUrl,
      coverImageUrl: profile.profileImageUrl, // Ou une image de couverture dédiée
      location: profile.location,
      distance: profile.distance, // Calculé dans le service
      bio: profile.about,
      about: profile.about,
      sportsInterests: profile.sportsInterests || [],
      sports: this.mapSports(profile.sportsInterests), // Convertir en format détaillé
      interests: profile.interests || [],
      rating: profile.rating || 0,
      activitiesJoined: profile.activitiesCount || 0,
      profileImageUrl: profile.profileImageUrl
    };
  }

  private calculateAge(dateOfBirth: string): number {
    if (!dateOfBirth) return 0;
    const today = new Date();
    const birthDate = new Date(dateOfBirth);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  }

  private mapSports(sportsInterests: string[]): any[] {
    if (!sportsInterests || sportsInterests.length === 0) return [];
    
    return sportsInterests.map(sportName => ({
      name: sportName,
      icon: this.getSportIcon(sportName),
      level: 'Intermediate' // Par défaut, ou récupérer depuis le profil utilisateur
    }));
  }

  private getSportIcon(sportName: string): string {
    const icons: { [key: string]: string } = {
      'Football': '⚽',
      'Basketball': '🏀',
      'Running': '🏃',
      'Cycling': '🚴',
      'Tennis': '🎾',
      'Swimming': '🏊',
      'Yoga': '🧘',
      'Volleyball': '🏐',
      // Ajouter d'autres sports...
    };
    return icons[sportName] || '🏃';
  }
}
```

### Service NestJS

```typescript
// quick-match.service.ts
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { User } from '../users/schemas/user.schema';
import { Activity } from '../activities/schemas/activity.schema';

@Injectable()
export class QuickMatchService {
  constructor(
    @InjectModel(User.name) private userModel: Model<User>,
    @InjectModel(Activity.name) private activityModel: Model<Activity>,
  ) {}

  /**
   * Récupère les profils compatibles avec l'utilisateur connecté
   * Filtre par sports/intérêts communs basés sur les activités
   */
  async getCompatibleProfiles(userId: string): Promise<any[]> {
    // 1. Récupérer l'utilisateur connecté
    const currentUser = await this.userModel.findById(userId);
    if (!currentUser) {
      throw new Error('User not found');
    }

    // 2. Récupérer les sportsInterests de l'utilisateur
    const userSportsInterests = currentUser.sportsInterests || [];

    // 3. Récupérer les activités créées/jointe par l'utilisateur
    const userActivities = await this.activityModel.find({
      $or: [
        { creator: userId }, // Activités créées
        { participants: userId } // Activités jointes (si vous avez un champ participants)
      ]
    });

    // 4. Extraire les sports des activités de l'utilisateur
    const activitySports = userActivities.map(activity => activity.sportType);
    const allUserSports = [...new Set([...userSportsInterests, ...activitySports])];

    // 5. Récupérer tous les utilisateurs (exclure l'utilisateur connecté)
    const allUsers = await this.userModel.find({
      _id: { $ne: userId }
    });

    // 6. Filtrer les utilisateurs qui ont au moins un sport/intérêt commun
    const compatibleProfiles = allUsers.filter(user => {
      const userSports = user.sportsInterests || [];
      
      // Vérifier s'il y a au moins un sport en commun
      const hasCommonSport = allUserSports.some(sport => 
        userSports.some(userSport => 
          userSport.toLowerCase() === sport.toLowerCase()
        )
      );

      return hasCommonSport;
    });

    // 7. Enrichir avec les données des activités
    const enrichedProfiles = await Promise.all(
      compatibleProfiles.map(async (user) => {
        // Compter les activités créées/jointe
        const activitiesCount = await this.activityModel.countDocuments({
          $or: [
            { creator: user._id },
            { participants: user._id }
          ]
        });

        // Calculer la distance (si vous avez les coordonnées GPS)
        const distance = this.calculateDistance(
          currentUser.location,
          user.location
        );

        return {
          ...user.toObject(),
          activitiesCount,
          distance: distance ? `${distance.toFixed(1)} km` : null
        };
      })
    );

    // 8. Trier par pertinence (nombre de sports en commun, distance, etc.)
    return this.sortByRelevance(enrichedProfiles, allUserSports);
  }

  /**
   * Calcule la distance entre deux localisations
   * Utilise la formule de Haversine si vous avez lat/lng
   */
  private calculateDistance(location1: string, location2: string): number | null {
    // Implémenter le calcul de distance basé sur vos données de localisation
    // Si vous avez lat/lng, utilisez la formule de Haversine
    // Sinon, retournez null
    return null; // À implémenter selon votre structure de données
  }

  /**
   * Trie les profils par pertinence
   */
  private sortByRelevance(profiles: any[], userSports: string[]): any[] {
    return profiles.sort((a, b) => {
      // Calculer le score de pertinence pour chaque profil
      const scoreA = this.calculateRelevanceScore(a, userSports);
      const scoreB = this.calculateRelevanceScore(b, userSports);
      
      return scoreB - scoreA; // Tri décroissant
    });
  }

  /**
   * Calcule un score de pertinence basé sur :
   * - Nombre de sports en commun
   * - Nombre d'activités
   * - Distance
   */
  private calculateRelevanceScore(profile: any, userSports: string[]): number {
    const profileSports = profile.sportsInterests || [];
    
    // Compter les sports en commun
    const commonSports = userSports.filter(sport =>
      profileSports.some(ps => ps.toLowerCase() === sport.toLowerCase())
    ).length;

    // Score basé sur les sports en commun (poids: 10)
    let score = commonSports * 10;

    // Bonus pour le nombre d'activités (poids: 1)
    score += (profile.activitiesCount || 0) * 1;

    // Bonus pour la distance (plus proche = meilleur score)
    // À implémenter selon votre logique

    return score;
  }
}
```

---

## 📊 Structure de la Base de Données

### Schéma User (Mongoose)

```typescript
// users/schemas/user.schema.ts
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';

@Schema()
export class User extends Document {
  @Prop({ required: true })
  name: string;

  @Prop({ required: true, unique: true })
  email: string;

  @Prop()
  dateOfBirth?: string;

  @Prop()
  location?: string;

  @Prop()
  latitude?: number;

  @Prop()
  longitude?: number;

  @Prop()
  about?: string;

  @Prop({ type: [String], default: [] })
  sportsInterests: string[]; // ["Football", "Basketball", "Running"]

  @Prop()
  profileImageUrl?: string;

  @Prop({ type: [String], default: [] })
  interests?: string[];
}
```

### Schéma Activity (Mongoose)

```typescript
// activities/schemas/activity.schema.ts
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

@Schema()
export class Activity extends Document {
  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  creator: Types.ObjectId;

  @Prop({ required: true })
  sportType: string; // "Football", "Basketball", etc.

  @Prop({ required: true })
  title: string;

  @Prop()
  description?: string;

  @Prop({ required: true })
  location: string;

  @Prop()
  latitude?: number;

  @Prop()
  longitude?: number;

  @Prop({ type: [Types.ObjectId], ref: 'User', default: [] })
  participants: Types.ObjectId[]; // Utilisateurs qui ont rejoint

  @Prop({ required: true })
  date: Date;

  @Prop({ required: true })
  time: Date;

  @Prop({ required: true, min: 1, max: 100 })
  participants: number;

  @Prop({ required: true })
  level: string; // "Beginner", "Intermediate", "Advanced"

  @Prop({ required: true })
  visibility: string; // "public", "friends"
}
```

---

## 🔍 Logique de Filtrage

### Algorithme de Filtrage

1. **Récupérer l'utilisateur connecté** et ses `sportsInterests`
2. **Récupérer les activités** créées/jointe par l'utilisateur
3. **Extraire les sports** de ces activités (`sportType`)
4. **Combiner** `sportsInterests` + sports des activités = liste complète des sports de l'utilisateur
5. **Pour chaque autre utilisateur** :
   - Vérifier si `sportsInterests` de l'utilisateur a **au moins un élément en commun** avec la liste complète
   - Si oui, inclure dans les résultats
6. **Trier par pertinence** (nombre de sports en commun, distance, etc.)

### Exemple

**Utilisateur connecté :**
- `sportsInterests`: ["Football", "Basketball"]
- Activités créées: [Activity(sportType: "Running"), Activity(sportType: "Tennis")]
- **Sports complets**: ["Football", "Basketball", "Running", "Tennis"]

**Autres utilisateurs :**
- User A: `sportsInterests`: ["Football", "Swimming"] → ✅ **Match** (Football en commun)
- User B: `sportsInterests`: ["Swimming", "Cycling"] → ❌ **Pas de match**
- User C: `sportsInterests`: ["Running", "Yoga"] → ✅ **Match** (Running en commun)

---

## 📝 Endpoints Supplémentaires

### POST /quick-match/like

```typescript
@Post('like')
async likeProfile(@Request() req, @Body() body: { profileId: string }) {
  const userId = req.user.id;
  const { profileId } = body;
  
  // Enregistrer le like
  await this.quickMatchService.likeProfile(userId, profileId);
  
  // Vérifier si c'est un match
  const isMatch = await this.quickMatchService.checkMatch(userId, profileId);
  
  if (isMatch) {
    const matchedProfile = await this.userModel.findById(profileId);
    return {
      isMatch: true,
      matchedProfile: this.mapToResponse(matchedProfile)
    };
  }
  
  return {
    isMatch: false,
    matchedProfile: null
  };
}
```

### POST /quick-match/pass

```typescript
@Post('pass')
async passProfile(@Request() req, @Body() body: { profileId: string }) {
  const userId = req.user.id;
  const { profileId } = body;
  
  await this.quickMatchService.passProfile(userId, profileId);
  
  return { success: true };
}
```

---

## ✅ Checklist d'Implémentation Backend

- [ ] Créer le module `QuickMatchModule`
- [ ] Créer le controller `QuickMatchController`
- [ ] Créer le service `QuickMatchService`
- [ ] Implémenter la logique de filtrage par sports communs
- [ ] Récupérer les activités créées/jointe par l'utilisateur
- [ ] Calculer la distance entre utilisateurs (optionnel)
- [ ] Trier les profils par pertinence
- [ ] Implémenter les endpoints `like` et `pass`
- [ ] Gérer les matches (quand deux utilisateurs se likent)
- [ ] Ajouter la pagination si nécessaire
- [ ] Tester avec différents scénarios de sportsInterests

---

## 🎯 Points Importants

1. **Filtrage côté backend** : Le filtrage par sports communs doit être fait côté backend pour des performances optimales

2. **Sports des activités** : Les sports des activités créées/jointe doivent être considérés en plus des `sportsInterests` de l'utilisateur

3. **Au moins un sport commun** : Un utilisateur est inclus s'il a **au moins un** sport/intérêt en commun

4. **Performance** : Pour de grandes bases de données, considérer :
   - Index sur `sportsInterests`
   - Cache des résultats
   - Pagination

5. **Distance** : Si vous avez les coordonnées GPS (latitude/longitude), utilisez la formule de Haversine pour calculer la distance réelle

