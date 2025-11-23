# 🔧 Correction Backend - Calcul Automatique du Niveau

## ❌ Problème Actuel

L'utilisateur a **300 XP total** mais reste au **Niveau 2** alors qu'il devrait être au **Niveau 3** selon la formule :
- Niveau 1 : 0-149 XP
- Niveau 2 : 150-299 XP
- **Niveau 3 : 300-499 XP** ✅

## 🎯 Solution : Recalculer le Niveau Automatiquement

### 1. Formule de Calcul du Niveau

```typescript
// Dans votre service achievements (NestJS)
calculateLevel(totalXp: number): number {
    // Formule simple: Niveau = floor((XP Total) / 150) + 1
    return Math.floor(totalXp / 150) + 1;
    
    // OU formule exponentielle (plus réaliste):
    // let level = 1;
    // let xpRequired = 0;
    // while (totalXp >= xpRequired) {
    //     xpRequired += 100 * Math.pow(level, 1.5);
    //     level++;
    // }
    // return level - 1;
}
```

### 2. Mise à Jour Automatique du Niveau

Le niveau doit être **recalculé automatiquement** à chaque fois que l'XP change :

```typescript
// Dans achievements.service.ts (NestJS)
async updateUserAchievements(userId: string, xpGained: number) {
    // 1. Récupérer les achievements actuels
    const achievements = await this.achievementsModel.findOne({ userId });
    
    // 2. Ajouter l'XP
    const newTotalXp = achievements.totalXp + xpGained;
    
    // 3. Calculer le nouveau niveau
    const newLevel = this.calculateLevel(newTotalXp);
    
    // 4. Vérifier si le niveau a augmenté
    const oldLevel = achievements.level;
    const levelUp = newLevel > oldLevel;
    
    // 5. Calculer currentLevelXp et xpForNextLevel
    const { currentLevelXp, xpForNextLevel } = this.calculateLevelProgress(newTotalXp, newLevel);
    
    // 6. Mettre à jour dans la base de données
    await this.achievementsModel.updateOne(
        { userId },
        {
            $set: {
                totalXp: newTotalXp,
                level: newLevel,
                currentLevelXp,
                xpForNextLevel,
                updatedAt: new Date()
            }
        }
    );
    
    // 7. Si level up, créer une notification
    if (levelUp) {
        await this.createNotification(userId, {
            type: 'level_up',
            title: 'Niveau Supérieur ! ⬆️',
            message: `Félicitations ! Vous êtes maintenant niveau ${newLevel} avec ${newTotalXp} XP total!`,
            metadata: {
                oldLevel,
                newLevel,
                totalXp: newTotalXp
            }
        });
    }
    
    return { levelUp, newLevel, newTotalXp };
}

// Calculer la progression dans le niveau actuel
calculateLevelProgress(totalXp: number, level: number): { currentLevelXp: number, xpForNextLevel: number } {
    // XP minimum pour atteindre ce niveau
    const xpForCurrentLevel = (level - 1) * 150;
    
    // XP dans le niveau actuel
    const currentLevelXp = totalXp - xpForCurrentLevel;
    
    // XP nécessaire pour passer au niveau suivant (dans le niveau actuel)
    const xpForNextLevel = 150; // Ou une formule plus complexe
    
    return { currentLevelXp, xpForNextLevel };
}
```

### 3. Endpoints à Modifier

#### A. Lors de la Complétion d'Activité

```typescript
// activities.service.ts
async completeActivity(userId: string, activityId: string) {
    // ... logique de complétion d'activité ...
    
    // Calculer XP gagné
    const xpGained = this.calculateActivityXp(activity);
    
    // Mettre à jour les achievements (et recalculer le niveau)
    await this.achievementsService.updateUserAchievements(userId, xpGained);
    
    return { success: true, xpGained };
}
```

#### B. Lors de l'Obtention d'un Badge

```typescript
// achievements.service.ts
async unlockBadge(userId: string, badgeId: string) {
    // ... débloquer le badge ...
    
    // Si le badge donne de l'XP bonus
    const badge = await this.badgesModel.findById(badgeId);
    if (badge.xpReward) {
        await this.updateUserAchievements(userId, badge.xpReward);
    }
}
```

#### C. Dans l'Endpoint Summary

```typescript
// achievements.controller.ts
@Get('summary')
async getSummary(@Request() req) {
    const userId = req.user.id;
    
    // Toujours recalculer le niveau pour être sûr qu'il est à jour
    const achievements = await this.achievementsModel.findOne({ userId });
    
    // Vérifier si le niveau est correct
    const correctLevel = this.calculateLevel(achievements.totalXp);
    if (achievements.level !== correctLevel) {
        // Mettre à jour si nécessaire
        const { currentLevelXp, xpForNextLevel } = this.calculateLevelProgress(
            achievements.totalXp, 
            correctLevel
        );
        
        await this.achievementsModel.updateOne(
            { userId },
            {
                $set: {
                    level: correctLevel,
                    currentLevelXp,
                    xpForNextLevel
                }
            }
        );
        
        achievements.level = correctLevel;
        achievements.currentLevelXp = currentLevelXp;
        achievements.xpForNextLevel = xpForNextLevel;
    }
    
    return {
        level: {
            currentLevel: achievements.level,
            totalXp: achievements.totalXp,
            xpForNextLevel: achievements.xpForNextLevel,
            currentLevelXp: achievements.currentLevelXp,
            progressPercentage: (achievements.currentLevelXp / achievements.xpForNextLevel) * 100
        },
        stats: {
            totalBadges: achievements.totalBadges,
            currentStreak: achievements.currentStreak,
            bestStreak: achievements.bestStreak
        }
    };
}
```

### 4. Script de Correction pour les Utilisateurs Existants

```typescript
// Script de migration (une seule fois)
async fixAllUserLevels() {
    const users = await this.achievementsModel.find({});
    
    for (const user of users) {
        const correctLevel = this.calculateLevel(user.totalXp);
        const { currentLevelXp, xpForNextLevel } = this.calculateLevelProgress(
            user.totalXp, 
            correctLevel
        );
        
        await this.achievementsModel.updateOne(
            { userId: user.userId },
            {
                $set: {
                    level: correctLevel,
                    currentLevelXp,
                    xpForNextLevel
                }
            }
        );
        
        console.log(`User ${user.userId}: Level ${user.level} → ${correctLevel}`);
    }
}
```

## ✅ Checklist de Vérification

- [ ] Le niveau est recalculé automatiquement lors de la complétion d'activité
- [ ] Le niveau est recalculé automatiquement lors de l'obtention d'un badge
- [ ] Le niveau est vérifié et corrigé dans l'endpoint `/achievements/summary`
- [ ] `currentLevelXp` est calculé correctement (XP dans le niveau actuel, pas XP total)
- [ ] `xpForNextLevel` représente l'XP nécessaire pour passer au niveau suivant (dans le niveau actuel)
- [ ] Une notification `level_up` est créée quand le niveau augmente
- [ ] Script de migration exécuté pour corriger les utilisateurs existants

## 📝 Exemple de Calcul

**Utilisateur avec 300 XP :**
- `totalXp = 300`
- `level = floor(300 / 150) + 1 = 2 + 1 = 3` ✅
- `xpForCurrentLevel = (3 - 1) * 150 = 300`
- `currentLevelXp = 300 - 300 = 0`
- `xpForNextLevel = 150` (pour passer au niveau 4)
- Affichage : "0 / 150" ✅

**Utilisateur avec 350 XP :**
- `totalXp = 350`
- `level = floor(350 / 150) + 1 = 2 + 1 = 3` ✅
- `xpForCurrentLevel = (3 - 1) * 150 = 300`
- `currentLevelXp = 350 - 300 = 50`
- `xpForNextLevel = 150`
- Affichage : "50 / 150" ✅

## 🚨 Important

Le problème actuel est que le **backend ne met pas à jour automatiquement le niveau**. 

**Avec 300 XP, vous devriez être niveau 3, mais le backend renvoie toujours niveau 2.**

Cette correction doit être faite **côté backend NestJS**.

