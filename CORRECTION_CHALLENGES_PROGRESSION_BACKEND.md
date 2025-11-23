# 🔧 Correction Backend - Progression des Challenges

## ❌ Problème Actuel

Après avoir créé ou complété une activité, la progression des challenges ne se met pas à jour automatiquement. Par exemple, le challenge "Compléter 2 activités aujourd'hui" reste à "0/2" même après avoir créé une activité.

## 🎯 Solution : Mettre à Jour la Progression des Challenges

### 1. Vérifier la Progression des Challenges Après Création/Complétion d'Activité

```typescript
// achievements.service.ts

/**
 * Met à jour la progression des challenges actifs de l'utilisateur
 * Appelé après création ou complétion d'activité
 */
async updateChallengeProgress(userId: string, activityType: 'created' | 'completed', activityData?: any): Promise<void> {
    const achievement = await this.achievementModel.findOne({ userId });
    if (!achievement) {
        return;
    }

    // Récupérer les challenges actifs de l'utilisateur
    const activeChallenges = await this.challengeModel.find({
        userId,
        status: 'active',
        expiresAt: { $gt: new Date() } // Challenge non expiré
    });

    for (const challenge of activeChallenges) {
        let shouldUpdate = false;
        let newProgress = challenge.currentProgress || 0;

        // Vérifier le type de challenge
        switch (challenge.challengeType) {
            case 'daily':
            case 'weekly':
            case 'monthly':
                // Challenge basé sur le nombre d'activités
                if (activityType === 'completed') {
                    // Vérifier si l'activité correspond aux critères du challenge
                    const matchesCriteria = this.activityMatchesChallengeCriteria(activityData, challenge);
                    if (matchesCriteria) {
                        newProgress = (challenge.currentProgress || 0) + 1;
                        shouldUpdate = true;
                    }
                }
                break;

            case 'distance':
                // Challenge basé sur la distance
                if (activityType === 'completed' && activityData?.distanceKm) {
                    newProgress = (challenge.currentProgress || 0) + activityData.distanceKm;
                    shouldUpdate = true;
                }
                break;

            case 'duration':
                // Challenge basé sur la durée
                if (activityType === 'completed' && activityData?.durationMinutes) {
                    newProgress = (challenge.currentProgress || 0) + activityData.durationMinutes;
                    shouldUpdate = true;
                }
                break;

            case 'streak':
                // Challenge basé sur les séries (streak)
                // Géré séparément par le système de streak
                break;
        }

        // Mettre à jour la progression si nécessaire
        if (shouldUpdate) {
            challenge.currentProgress = Math.min(newProgress, challenge.target); // Ne pas dépasser la cible
            
            // Vérifier si le challenge est complété
            if (challenge.currentProgress >= challenge.target && challenge.status !== 'completed') {
                challenge.status = 'completed';
                challenge.completedAt = new Date();
                
                // Ajouter l'XP de récompense
                if (challenge.xpReward) {
                    await this.addXp(userId, challenge.xpReward);
                }
                
                // Créer une notification de complétion
                await this.createNotification(userId, {
                    type: 'challenge_completed',
                    title: '🎯 Défi Complété !',
                    message: `Félicitations ! Vous avez complété le défi "${challenge.name}" !`,
                    metadata: {
                        challengeName: challenge.name,
                        xpReward: challenge.xpReward || 0
                    }
                });
            }
            
            await challenge.save();
        }
    }
}

/**
 * Vérifie si une activité correspond aux critères d'un challenge
 */
private activityMatchesChallengeCriteria(activityData: any, challenge: any): boolean {
    // Vérifier le type de sport si le challenge le spécifie
    if (challenge.sportType && activityData.sportType !== challenge.sportType) {
        return false;
    }
    
    // Vérifier la date (pour les challenges quotidiens/hebdomadaires/mensuels)
    if (challenge.challengeType === 'daily') {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const activityDate = new Date(activityData.completedAt || activityData.createdAt);
        activityDate.setHours(0, 0, 0, 0);
        if (activityDate.getTime() !== today.getTime()) {
            return false;
        }
    }
    
    // Ajouter d'autres critères si nécessaire
    
    return true;
}
```

### 2. Appeler la Mise à Jour lors de la Création d'Activité

```typescript
// activities.service.ts

import { AchievementsService } from '../achievements/achievements.service';

@Injectable()
export class ActivitiesService {
    constructor(
        @InjectModel(Activity.name) private activityModel: Model<ActivityDocument>,
        private readonly achievementsService: AchievementsService,
        // ... autres dépendances
    ) {}

    async createActivity(userId: string, createActivityDto: CreateActivityDto) {
        // 1. Créer l'activité
        const activity = new this.activityModel({
            ...createActivityDto,
            creatorId: userId,
            status: 'pending',
            createdAt: new Date(),
        });
        await activity.save();

        // 2. Vérifier les badges de création
        await this.achievementsService.onActivityCreated(userId);

        // 3. Mettre à jour la progression des challenges (si la création compte)
        // Note: Généralement, seules les activités complétées comptent pour les challenges
        // Si vous voulez que la création compte aussi, décommentez la ligne suivante:
        // await this.achievementsService.updateChallengeProgress(userId, 'created', activity);

        return activity;
    }
}
```

### 3. Appeler la Mise à Jour lors de la Complétion d'Activité

```typescript
// activities.service.ts

async completeActivity(userId: string, activityId: string, completionData?: { durationMinutes?: number, distanceKm?: number }) {
    // 1. Marquer l'activité comme complétée
    const activity = await this.activityModel.findById(activityId);
    if (!activity) {
        throw new NotFoundException('Activité non trouvée');
    }

    activity.status = 'completed';
    activity.completedAt = new Date();
    if (completionData?.durationMinutes) {
        activity.durationMinutes = completionData.durationMinutes;
    }
    if (completionData?.distanceKm) {
        activity.distanceKm = completionData.distanceKm;
    }
    await activity.save();

    // 2. Vérifier les badges de complétion
    await this.achievementsService.onActivityCompleted(userId, activity);

    // 3. Mettre à jour la progression des challenges
    await this.achievementsService.updateChallengeProgress(userId, 'completed', {
        ...activity.toObject(),
        durationMinutes: completionData?.durationMinutes,
        distanceKm: completionData?.distanceKm,
        completedAt: activity.completedAt
    });

    // 4. Ajouter l'XP pour la complétion
    const baseXp = 50; // XP de base pour compléter une activité
    await this.achievementsService.addXp(userId, baseXp);

    return activity;
}
```

### 4. Endpoint GET /achievements/challenges - Inclure la Progression à Jour

```typescript
// achievements.controller.ts

@Get('challenges')
async getChallenges(@Request() req) {
    const userId = req.user.id;
    
    // Récupérer les challenges actifs
    const challenges = await this.challengeModel.find({
        userId,
        status: { $in: ['active', 'completed'] },
        expiresAt: { $gt: new Date() }
    }).sort({ createdAt: -1 });

    // Formater la réponse avec la progression à jour
    const activeChallenges = challenges.map(challenge => ({
        _id: challenge._id,
        name: challenge.name,
        description: challenge.description,
        challengeType: challenge.challengeType,
        xpReward: challenge.xpReward,
        currentProgress: challenge.currentProgress || 0,
        target: challenge.target,
        daysLeft: Math.ceil((challenge.expiresAt.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24)),
        expiresAt: challenge.expiresAt.toISOString(),
        status: challenge.status
    }));

    return { activeChallenges };
}
```

## ✅ Checklist de Vérification

- [ ] La méthode `updateChallengeProgress()` est implémentée dans `achievements.service.ts`
- [ ] `updateChallengeProgress()` est appelée après la complétion d'activité
- [ ] `updateChallengeProgress()` met à jour correctement `currentProgress` selon le type de challenge
- [ ] `updateChallengeProgress()` vérifie si le challenge est complété et ajoute l'XP de récompense
- [ ] `updateChallengeProgress()` crée une notification quand un challenge est complété
- [ ] L'endpoint `/achievements/challenges` retourne la progression à jour
- [ ] La progression est calculée correctement pour les challenges quotidiens/hebdomadaires/mensuels

## 📝 Exemples de Challenges

### Challenge Quotidien : "Compléter 2 activités aujourd'hui"

```typescript
{
    userId: "user123",
    name: "Défi Quotidien",
    description: "Compléter 2 activités aujourd'hui",
    challengeType: "daily",
    target: 2,
    currentProgress: 0, // Sera mis à jour à 1 après la première activité complétée
    xpReward: 200,
    status: "active",
    expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000) // Expire dans 24h
}
```

**Progression après complétion de la première activité :**
```typescript
{
    currentProgress: 1, // ✅ Mis à jour
    status: "active" // Toujours actif car target = 2
}
```

**Progression après complétion de la deuxième activité :**
```typescript
{
    currentProgress: 2, // ✅ Mis à jour
    status: "completed", // ✅ Challenge complété !
    completedAt: new Date(),
    // XP ajoutée automatiquement : +200 XP
    // Notification créée : "🎯 Défi Complété !"
}
```

## 🚨 Important

Le problème actuel est que le **backend ne met pas à jour automatiquement la progression des challenges** quand une activité est créée ou complétée.

**Cette correction doit être faite côté backend NestJS.**

Une fois corrigé, le frontend rafraîchira automatiquement les challenges et affichera la progression à jour.

