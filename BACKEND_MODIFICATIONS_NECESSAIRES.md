# 🔧 Modifications Backend NestJS Nécessaires

## 📋 Vue d'Ensemble

Pour que les badges soient automatiquement créés lors de la **création d'activité**, le backend NestJS doit être modifié.

---

## ❌ Problème Actuel

Actuellement, le backend ne vérifie/crée **pas** les badges lors de :
- ✅ La **création** d'activité → Devrait créer le badge "Premier Hôte"
- ✅ La **complétion** d'activité → Devrait créer le badge "Premier Pas" et autres

---

## ✅ Solution : Modifications Backend Requises

### **1. Modifier `activities.service.ts` - Création d'Activité**

Quand une activité est créée, appeler le service achievements :

```typescript
// activities.service.ts

import { AchievementsService } from '../achievements/achievements.service';

@Injectable()
export class ActivitiesService {
  constructor(
    @InjectModel(Activity.name) private activityModel: Model<ActivityDocument>,
    private readonly achievementsService: AchievementsService, // ← AJOUTER
    // ... autres dépendances
  ) {}

  /**
   * Créer une nouvelle activité
   */
  async createActivity(userId: string, createActivityDto: CreateActivityDto) {
    // 1. Créer l'activité
    const activity = new this.activityModel({
      ...createActivityDto,
      creatorId: userId,
      status: 'pending',
      createdAt: new Date(),
    });
    await activity.save();

    // 2. AJOUTER CETTE LIGNE ← IMPORTANT !
    // Vérifier et créer le badge "Premier Hôte" si c'est la première activité
    await this.achievementsService.onActivityCreated(userId);

    return activity;
  }
}
```

---

### **2. Ajouter la méthode `onActivityCreated()` dans `achievements.service.ts`**

```typescript
// achievements.service.ts

/**
 * Appelé lors de la création d'une activité
 * Vérifie et débloque le badge "Premier Hôte" si c'est la première activité
 */
async onActivityCreated(userId: string): Promise<void> {
  const achievement = await this.achievementModel.findOne({ userId });
  if (!achievement) {
    // Si l'utilisateur n'a pas d'achievements, les initialiser
    await this.initializeUserAchievements(userId);
    return;
  }

  // Incrémenter le compteur d'activités créées
  achievement.totalActivitiesCreated += 1;

  // Vérifier le badge "Premier Hôte" (première activité créée)
  const premierHoteBadgeId = 'premier-hote';
  const alreadyEarned = achievement.earnedBadges.some(
    (b) => b.badgeId === premierHoteBadgeId,
  );

  if (!alreadyEarned && achievement.totalActivitiesCreated === 1) {
    // Débloquer le badge "Premier Hôte"
    achievement.earnedBadges.push({
      badgeId: premierHoteBadgeId,
      name: 'Premier Hôte',
      description: 'Créer votre première activité',
      iconUrl: '🎨',
      rarity: 'common',
      category: 'creation',
      earnedAt: new Date(),
    });

    this.logger.log(`Badge débloqué: Premier Hôte pour l'utilisateur ${userId}`);
  }

  // Vérifier d'autres badges de création si nécessaire
  // Ex: "Hôte Populaire" après 5 activités créées
  const hotePopulaireBadgeId = 'hote-populaire';
  const alreadyEarnedPopulaire = achievement.earnedBadges.some(
    (b) => b.badgeId === hotePopulaireBadgeId,
  );

  if (!alreadyEarnedPopulaire && achievement.totalActivitiesCreated === 5) {
    achievement.earnedBadges.push({
      badgeId: hotePopulaireBadgeId,
      name: 'Hôte Populaire',
      description: 'Créer 5 activités',
      iconUrl: '👑',
      rarity: 'rare',
      category: 'creation',
      earnedAt: new Date(),
    });

    this.logger.log(`Badge débloqué: Hôte Populaire pour l'utilisateur ${userId}`);
  }

  await achievement.save();
}
```

---

### **3. Modifier `activities.controller.ts` - Appeler le Service**

Assurez-vous que le controller appelle bien le service :

```typescript
// activities.controller.ts

@Controller('activities')
@UseGuards(JwtAuthGuard)
export class ActivitiesController {
  constructor(
    private readonly activitiesService: ActivitiesService,
  ) {}

  @Post()
  async createActivity(
    @Request() req,
    @Body() createActivityDto: CreateActivityDto,
  ) {
    const userId = req.user.sub; // Depuis le JWT
    
    // Le service va automatiquement appeler achievementsService.onActivityCreated()
    const activity = await this.activitiesService.createActivity(
      userId,
      createActivityDto,
    );

    return activity;
  }
}
```

---

### **4. Vérifier que `AchievementsModule` est exporté**

Dans `activities.module.ts`, assurez-vous que `AchievementsModule` est importé :

```typescript
// activities.module.ts

import { Module } from '@nestjs/common';
import { ActivitiesController } from './activities.controller';
import { ActivitiesService } from './activities.service';
import { AchievementsModule } from '../achievements/achievements.module'; // ← AJOUTER

@Module({
  imports: [
    AchievementsModule, // ← AJOUTER pour utiliser AchievementsService
    // ... autres modules
  ],
  controllers: [ActivitiesController],
  providers: [ActivitiesService],
  exports: [ActivitiesService],
})
export class ActivitiesModule {}
```

---

## 📝 Checklist de Vérification Backend

### **Dans `activities.service.ts` :**
- [ ] `AchievementsService` est injecté dans le constructeur
- [ ] `onActivityCreated(userId)` est appelé dans `createActivity()`
- [ ] `onActivityCompleted(userId, activityData)` est appelé dans `completeActivity()`

### **Dans `achievements.service.ts` :**
- [ ] La méthode `onActivityCreated(userId)` existe
- [ ] Elle vérifie le badge "Premier Hôte" (première activité)
- [ ] Elle vérifie le badge "Hôte Populaire" (5 activités)
- [ ] Elle incrémente `totalActivitiesCreated`
- [ ] Elle sauvegarde les changements

### **Dans `activities.module.ts` :**
- [ ] `AchievementsModule` est importé dans les imports
- [ ] `AchievementsService` est disponible pour injection

---

## 🔍 Vérification dans MongoDB

Après avoir créé une activité, vérifiez dans MongoDB :

```javascript
// Se connecter à MongoDB
db.achievements.findOne({ userId: ObjectId("VOTRE_USER_ID") })

// Vérifier :
// 1. totalActivitiesCreated doit être > 0
// 2. earnedBadges doit contenir le badge "Premier Hôte" si c'est la première activité
```

---

## ✅ Résumé des Changements Backend

### **Ce qui doit être fait :**

1. ✅ Modifier `activities.service.ts` pour appeler `achievementsService.onActivityCreated(userId)` lors de la création
2. ✅ Ajouter la méthode `onActivityCreated()` dans `achievements.service.ts`
3. ✅ Vérifier que `AchievementsModule` est importé dans `ActivitiesModule`
4. ✅ Tester que le badge "Premier Hôte" est créé lors de la première activité

### **Endpoints concernés :**

- `POST /activities` → Doit déclencher la vérification des badges de création
- `POST /activities/:id/complete` → Doit déclencher la vérification des badges de complétion

---

## 🚀 Code Complet de `onActivityCreated()`

```typescript
// achievements.service.ts

/**
 * Appelé lors de la création d'une activité
 * Vérifie et débloque les badges de création
 */
async onActivityCreated(userId: string): Promise<void> {
  const achievement = await this.achievementModel.findOne({ userId });
  if (!achievement) {
    await this.initializeUserAchievements(userId);
    return;
  }

  // Incrémenter le compteur d'activités créées
  achievement.totalActivitiesCreated += 1;

  // Badge "Premier Hôte" - Première activité créée
  const premierHoteBadgeId = 'premier-hote';
  const alreadyEarnedPremierHote = achievement.earnedBadges.some(
    (b) => b.badgeId === premierHoteBadgeId,
  );

  if (!alreadyEarnedPremierHote && achievement.totalActivitiesCreated === 1) {
    achievement.earnedBadges.push({
      badgeId: premierHoteBadgeId,
      name: 'Premier Hôte',
      description: 'Créer votre première activité',
      iconUrl: '🎨',
      rarity: 'common',
      category: 'creation',
      earnedAt: new Date(),
    });
    this.logger.log(`Badge débloqué: Premier Hôte pour ${userId}`);
  }

  // Badge "Hôte Populaire" - 5 activités créées
  const hotePopulaireBadgeId = 'hote-populaire';
  const alreadyEarnedPopulaire = achievement.earnedBadges.some(
    (b) => b.badgeId === hotePopulaireBadgeId,
  );

  if (!alreadyEarnedPopulaire && achievement.totalActivitiesCreated === 5) {
    achievement.earnedBadges.push({
      badgeId: hotePopulaireBadgeId,
      name: 'Hôte Populaire',
      description: 'Créer 5 activités',
      iconUrl: '👑',
      rarity: 'rare',
      category: 'creation',
      earnedAt: new Date(),
    });
    this.logger.log(`Badge débloqué: Hôte Populaire pour ${userId}`);
  }

  // Badge "Organisateur Pro" - 10 activités créées
  const organisateurProBadgeId = 'organisateur-pro';
  const alreadyEarnedPro = achievement.earnedBadges.some(
    (b) => b.badgeId === organisateurProBadgeId,
  );

  if (!alreadyEarnedPro && achievement.totalActivitiesCreated === 10) {
    achievement.earnedBadges.push({
      badgeId: organisateurProBadgeId,
      name: 'Organisateur Pro',
      description: 'Créer 10 activités',
      iconUrl: '🏆',
      rarity: 'epic',
      category: 'creation',
      earnedAt: new Date(),
    });
    this.logger.log(`Badge débloqué: Organisateur Pro pour ${userId}`);
  }

  await achievement.save();
}
```

---

## 📊 Schéma MongoDB - Ajout du Champ

Assurez-vous que le schéma `Achievement` contient `totalActivitiesCreated` :

```typescript
// achievement.schema.ts

@Schema({ timestamps: true })
export class Achievement {
  // ... autres champs

  @Prop({ default: 0 })
  totalActivitiesCreated: number; // ← Doit exister

  @Prop({ default: 0 })
  totalActivitiesCompleted: number; // ← Doit exister

  // ... autres champs
}
```

---

**Dernière mise à jour** : 2025-01-20

**Note** : Ces modifications backend sont **obligatoires** pour que les badges de création fonctionnent correctement.

