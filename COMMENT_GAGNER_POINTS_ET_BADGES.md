# 🏆 Comment Gagner des Points XP et des Badges

## 📊 Vue d'Ensemble

Le système d'achievements récompense les utilisateurs pour leur participation et leur engagement dans l'application. Il y a **3 façons principales** de gagner des récompenses :

1. **Compléter des activités** → Points XP
2. **Accomplir des objectifs** → Badges
3. **Terminer des challenges** → Points XP + Récompenses spéciales

---

## 🎯 1. Gagner des Points XP

### **Méthode Principale : Compléter une Activité**

Quand vous participez à une activité sportive et la complétez :

```kotlin
// Dans ActivityRoomViewModel.kt
fun completeActivity() {
    viewModelScope.launch {
        // Appel API pour marquer l'activité comme complétée
        repository.completeActivity(activityId)
        // Le backend attribue automatiquement des points XP
    }
}
```

**API Endpoint :**
```
POST /activities/{id}/complete
```

**Ce qui se passe côté backend :**
1. ✅ L'activité est marquée comme complétée
2. 💰 L'utilisateur gagne des **points XP** (généralement 50-200 XP par activité)
3. 📈 Le niveau de l'utilisateur peut augmenter si l'XP total dépasse le seuil
4. 🔥 La série (streak) quotidienne est mise à jour

### **Montant des Points XP par Activité**

| Type d'Action | Points XP | Description |
|--------------|-----------|-------------|
| **Compléter une activité** | 50-200 XP | Varie selon le type de sport et la durée |
| **Créer une activité** | 25-50 XP | Récompense pour l'organisation |
| **Rejoindre une activité** | 10-20 XP | Encouragement à participer |
| **Compléter un challenge** | 100-500 XP | Dépend de la difficulté du challenge |

### **Système de Niveaux**

Votre niveau est calculé automatiquement selon votre XP total :

```
Niveau 1 : 0-149 XP
Niveau 2 : 150-299 XP
Niveau 3 : 300-499 XP
...
Niveau X : (Niveau - 1) * 150 XP minimum
```

**Exemple :**
- Si vous avez **250 XP total** → Vous êtes **Niveau 2**
- Pour atteindre le **Niveau 3**, il vous faut **300 XP** (il vous manque 50 XP)

---

## 🏅 2. Obtenir des Badges

Les badges sont des récompenses pour accomplir des **objectifs spécifiques**.

### **Types de Badges**

#### **Badges de Participation**
- 🏃 **Premier Pas** : Compléter votre première activité
- 🎯 **Débutant Actif** : Compléter 5 activités
- 🚀 **Marathonien** : Compléter 10 activités
- ⭐ **Champion** : Compléter 25 activités

#### **Badges de Consistance**
- 🔥 **Série de 3 jours** : Maintenir une série de 3 jours consécutifs
- 🌟 **Série de 7 jours** : Maintenir une série de 7 jours consécutifs
- 💪 **Série de 30 jours** : Maintenir une série de 30 jours consécutifs

#### **Badges de Création**
- 🎨 **Premier Hôte** : Créer votre première activité
- 👑 **Hôte Populaire** : Créer 5 activités qui ont été complétées
- 🏆 **Organisateur Pro** : Créer 10 activités réussies

#### **Badges de Sport**
- ⚽ **Footballeur** : Compléter 10 activités de football
- 🏀 **Basketteur** : Compléter 10 activités de basketball
- 🏃 **Coureur** : Compléter 10 activités de course

### **Rareté des Badges**

| Rareté | Fréquence | Description |
|--------|-----------|-------------|
| **Common** | ⭐ | Badges faciles à obtenir |
| **Rare** | ⭐⭐ | Badges nécessitant plus d'effort |
| **Epic** | ⭐⭐⭐ | Badges très difficiles |
| **Legendary** | ⭐⭐⭐⭐ | Badges exceptionnels |

### **Comment les Badges sont Attribués**

Les badges sont **attribués automatiquement** par le backend quand :
1. ✅ Vous complétez une activité → Vérifie les badges de participation
2. 📅 Votre série quotidienne continue → Vérifie les badges de consistance
3. 🎯 Vous créez une activité → Vérifie les badges de création
4. 🏃 Vous complétez un sport spécifique → Vérifie les badges de sport

**Vous n'avez rien à faire !** Les badges apparaissent automatiquement dans votre profil.

---

## 🎮 3. Compléter des Challenges

Les challenges sont des **défis limités dans le temps** avec des récompenses spéciales.

### **Types de Challenges**

#### **Challenge Hebdomadaire**
```
"Défi Hebdomadaire"
Description: Compléter 5 activités cette semaine
Récompense: 500 XP + Badge "Weekend Warrior"
Temps restant: 3 jours
```

#### **Challenge Mensuel**
```
"Marathon Mensuel"
Description: Compléter 20 activités ce mois
Récompense: 1500 XP + Badge "Marathonien"
Temps restant: 15 jours
```

#### **Challenge Spécial**
```
"Explorateur"
Description: Essayer 3 sports différents cette semaine
Récompense: 750 XP + Badge "Explorateur"
Temps restant: 5 jours
```

### **Comment Compléter un Challenge**

1. **Voir les challenges actifs** :
   - Ouvrez l'écran Achievements
   - Allez dans l'onglet **"Challenges"**
   
2. **Suivez votre progression** :
   - Chaque challenge montre votre progression actuelle
   - Exemple : "3 / 5 activités complétées"

3. **Gagner la récompense** :
   - Quand vous atteignez l'objectif, la récompense est **automatiquement** attribuée
   - Vous recevez les points XP + le badge immédiatement

---

## 📈 Comment Voir Votre Progression

### **Dans l'Écran Achievements**

1. **Résumé** :
   - Niveau actuel
   - XP total et XP pour le prochain niveau
   - Pourcentage de progression
   - Série actuelle et meilleure série

2. **Badges** :
   - Badges obtenus (avec date d'obtention)
   - Badges en cours de progression (avec progression actuelle)

3. **Challenges** :
   - Challenges actifs avec progression
   - Temps restant pour chaque challenge
   - Récompenses promises

4. **Leaderboard** :
   - Votre position dans le classement
   - Classement des meilleurs joueurs par XP total

---

## 🚀 Conseils pour Maximiser vos Gains

### **1. Participez Régulièrement**
- ✅ Complétez au moins **1 activité par jour** pour maintenir votre série
- 🔥 Les séries longues donnent des badges rares

### **2. Variez vos Activités**
- 🏃 Essayez différents sports pour débloquer des badges de sport
- 🎯 Complétez les challenges "Explorateur"

### **3. Organisez des Activités**
- 👑 Créez des activités pour gagner des badges d'hôte
- 💰 Gagnez des points XP supplémentaires

### **4. Complétez les Challenges**
- 🎮 Focalisez-vous sur les challenges actifs
- ⏰ Respectez les deadlines pour ne pas manquer les récompenses

### **5. Restez Actif**
- 📅 Connectez-vous quotidiennement
- 🔥 Maintenez votre série pour les badges de consistance

---

## 🔄 Comment Votre XP est Calculé

### **Calcul Automatique**

Le backend calcule automatiquement votre XP total à chaque fois que vous :
- ✅ Complétez une activité
- 🎯 Terminez un challenge
- 🎁 Recevez un badge (parfois donne des bonus XP)

### **Formule Simple**

```
XP Total = Somme de toutes vos actions
Niveau = floor((XP Total) / 150) + 1
```

**Exemple :**
- Vous avez complété 3 activités (100 XP chacune) = 300 XP
- Votre niveau = floor(300 / 150) + 1 = **Niveau 3**
- Pour le niveau suivant, il vous faut 450 XP total

---

## 📱 Endpoints API Utilisés

### **Pour Compléter une Activité**

```kotlin
// POST /activities/{id}/complete
// Le backend attribue automatiquement :
// - Points XP (50-200 selon le type d'activité)
// - Mise à jour de la série quotidienne
// - Vérification des badges
// - Mise à jour du niveau si nécessaire
```

### **Pour Voir vos Récompenses**

```kotlin
// GET /achievements/summary
// Retourne : niveau, XP, statistiques

// GET /achievements/badges
// Retourne : badges obtenus et en cours

// GET /achievements/challenges
// Retourne : challenges actifs avec progression

// GET /achievements/leaderboard
// Retourne : classement global
```

---

## 🎯 Exemple Concret : Journée Typique

### **Scénario : Nouveau Utilisateur**

**Jour 1 :**
- ✅ Crée une activité de football → +25 XP
- ✅ Rejoint une activité de course → +10 XP
- ✅ Complète l'activité de course → +100 XP
- 🏅 **Badge obtenu** : "Premier Pas" (première activité complétée)
- 📊 **XP Total** : 135 XP
- 📈 **Niveau** : 1 (progression 90% vers niveau 2)

**Jour 2 :**
- ✅ Complète 2 activités → +200 XP
- 🔥 **Série** : 2 jours consécutifs
- 📊 **XP Total** : 335 XP
- 📈 **Niveau** : 3 (progression 22% vers niveau 4)

**Jour 7 :**
- ✅ Maintenu une série de 7 jours
- 🏅 **Badge obtenu** : "Série de 7 jours" (rare)
- 📊 **XP Total** : 1200 XP
- 📈 **Niveau** : 9

---

## ⚡ Actions Automatiques vs Manuelles

### **Automatiques** (vous n'avez rien à faire)
- ✅ Attribution des points XP après complétion d'activité
- 🏅 Déblocage des badges quand vous atteignez les objectifs
- 📈 Calcul automatique du niveau
- 🔥 Mise à jour de la série quotidienne
- 🎁 Attribution des récompenses de challenges

### **Manuelles** (vous devez faire)
- ✅ Marquer une activité comme complétée (bouton "Compléter")
- 📱 Vérifier vos progrès dans l'écran Achievements
- 🎮 Voir les challenges actifs et vous concentrer dessus

---

## 💡 FAQ

### **Q : Combien de points XP puis-je gagner par jour ?**
**R :** Il n'y a pas de limite ! Plus vous êtes actif, plus vous gagnez.

### **Q : Les badges peuvent-ils être retirés ?**
**R :** Non, une fois obtenu, un badge est permanent dans votre collection.

### **Q : Que se passe-t-il si je manque un challenge ?**
**R :** Vous perdez la récompense, mais un nouveau challenge apparaîtra bientôt.

### **Q : Comment voir quels badges je n'ai pas encore ?**
**R :** Dans l'onglet Badges, vous verrez les badges "En cours" avec votre progression.

### **Q : Est-ce que créer une activité donne plus de XP que rejoindre ?**
**R :** Oui, créer donne généralement plus de XP (25-50 XP) que rejoindre (10-20 XP).

---

## 🎉 Résumé en 5 Points

1. **Complétez des activités** → Gagnez 50-200 XP par activité
2. **Maintenez une série quotidienne** → Débloquez des badges de consistance
3. **Variez vos sports** → Obtenez des badges de sport spécifiques
4. **Organisez des activités** → Badges d'hôte + bonus XP
5. **Complétez les challenges** → Récompenses importantes (500-1500 XP)

**Le système est automatique** : vous n'avez qu'à participer et être actif, les récompenses arrivent automatiquement ! 🚀

---

**Dernière mise à jour** : 2025-01-20

