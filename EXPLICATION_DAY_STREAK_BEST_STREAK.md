# 🔥 Explication : Day Streak vs Best Streak

## 📊 Vue d'Ensemble

Le système d'achievements utilise deux métriques de "série" (streak) pour motiver les utilisateurs à maintenir une régularité dans leurs activités sportives :

1. **Day Streak** (Série Actuelle / Current Streak)
2. **Best Streak** (Meilleure Série / Longest Streak)

---

## 🔥 Day Streak (Série Actuelle / Current Streak)

### **Qu'est-ce que c'est ?**

Le **Day Streak** représente le nombre de jours consécutifs où l'utilisateur a complété au moins une activité sportive **en ce moment**.

### **Fonctionnement**

✅ **Augmente** : Quand l'utilisateur complète une activité le jour suivant
- Si vous avez un streak de 5 jours et complétez une activité aujourd'hui → Streak passe à 6 jours

❌ **Diminue ou se réinitialise** : Si l'utilisateur manque un ou plusieurs jours
- Si vous avez un streak de 5 jours et ne faites aucune activité pendant 2 jours → Streak revient à 0 ou 1

### **Exemples Concrets**

```
Jour 1 : Compléter activité → Streak = 1 jour
Jour 2 : Compléter activité → Streak = 2 jours
Jour 3 : Compléter activité → Streak = 3 jours
Jour 4 : Pas d'activité      → Streak = 0 jour (réinitialisé)
Jour 5 : Compléter activité → Streak = 1 jour (nouveau départ)
```

### **Règles Importantes**

- **Plusieurs activités le même jour** : Ne compte que comme **1 jour** dans le streak
- **Un seul jour manqué** : Selon la logique backend, peut réinitialiser ou permettre un jour de grâce
- **Activité complétée** : Seule la complétion d'activité compte, pas la création

### **Utilité**

- 💪 **Motivation quotidienne** : Encourage à faire une activité chaque jour
- 📈 **Progression visible** : Voir votre série actuelle vous motive à continuer
- 🏆 **Badges de série** : Permet de débloquer des badges liés aux streaks (ex: "On Fire 🔥" pour 7 jours)

---

## 👑 Best Streak (Meilleure Série / Longest Streak)

### **Qu'est-ce que c'est ?**

Le **Best Streak** représente le **record personnel** - la plus longue série de jours consécutifs que l'utilisateur a jamais atteinte.

### **Fonctionnement**

✅ **Augmente** : Uniquement si vous dépassez votre record précédent
- Si votre record est de 10 jours et vous atteignez 11 jours → Best Streak passe à 11 jours

❌ **Ne diminue jamais** : Une fois établi, le record reste permanent
- Même si votre streak actuel revient à 0, votre Best Streak reste intact

### **Exemples Concrets**

```
Jour 1-7   : Activités chaque jour → Streak = 7 jours, Best Streak = 7 jours
Jour 8-10  : Activités chaque jour → Streak = 10 jours, Best Streak = 10 jours
Jour 11-15 : Pas d'activité        → Streak = 0 jours, Best Streak = 10 jours (toujours le record)
Jour 16-20 : Activités chaque jour → Streak = 5 jours, Best Streak = 10 jours (pas encore dépassé)
Jour 21-25 : Activités chaque jour → Streak = 10 jours, Best Streak = 10 jours (égalité)
Jour 26-30 : Activités chaque jour → Streak = 15 jours, Best Streak = 15 jours (nouveau record !)
```

### **Utilité**

- 🎯 **Objectif à battre** : Vous incite à battre votre record personnel
- 🏆 **Badges légendaires** : Permet de débloquer des badges de série exceptionnels (ex: "Legend 👑" pour 100 jours)
- 📊 **Statistique personnelle** : Affiche votre meilleure performance jamais réalisée
- 💪 **Fierté** : Montre votre capacité à maintenir une régularité sur le long terme

---

## 🔄 Comparaison Day Streak vs Best Streak

| Caractéristique | Day Streak (Current) | Best Streak (Longest) |
|----------------|---------------------|----------------------|
| **Signification** | Série actuelle en cours | Record personnel |
| **Évolution** | Augmente ou diminue | Ne fait qu'augmenter |
| **Réinitialisation** | Oui, si pas d'activité | Jamais |
| **Valeur initiale** | 0 ou 1 jour | 0 jour |
| **Utilisation** | Motivation quotidienne | Objectif à battre |
| **Badges** | Badges de série en cours | Badges de records |

---

## 📱 Affichage dans l'Application

Dans l'écran **Achievements**, vous verrez deux cartes distinctes :

```
┌─────────────────────┐
│   🔥 Day Streak     │
│        5            │
│   Jours actuels     │
└─────────────────────┘

┌─────────────────────┐
│  📈 Best Streak     │
│       12            │
│   Votre record      │
└─────────────────────┘
```

---

## 🎯 Stratégie d'Utilisation

### **Pour maximiser votre Day Streak :**

1. ✅ Faites au moins **une activité par jour**
2. ⏰ Planifiez vos activités à l'avance
3. 🏃 Choisissez des activités courtes si nécessaire (même 15 minutes comptent !)
4. 📅 Utilisez les rappels/notifications pour ne pas oublier

### **Pour améliorer votre Best Streak :**

1. 🎯 Fixez-vous un objectif de jours consécutifs à atteindre
2. 💪 Tenez compte de votre record actuel pour vous motiver
3. 📊 Suivez votre progression jour par jour
4. 🏆 Débloquez les badges de série pour vous récompenser

---

## 🏆 Badges Liés aux Streaks

Les streaks permettent de débloquer des badges spéciaux :

| Badge | Condition Day Streak | Description |
|-------|---------------------|-------------|
| 🔥 **On Fire** | 7 jours consécutifs | Votre série de base |
| ⚡ **Unstoppable** | 30 jours consécutifs | Un mois complet ! |
| 👑 **Legend** | 100 jours consécutifs | Légende de la régularité |

**Note** : Ces badges peuvent être basés sur le Day Streak actuel OU le Best Streak, selon la logique backend.

---

## 🔧 Mise à Jour Backend

Les streaks sont mis à jour automatiquement par le backend lorsqu'une activité est complétée :

1. **Backend vérifie** : Y a-t-il eu une activité complétée aujourd'hui ?
2. **Backend calcule** : Le streak actuel augmente-t-il ou se réinitialise-t-il ?
3. **Backend met à jour** : Le Best Streak est-il dépassé ?
4. **Backend sauvegarde** : Les nouvelles valeurs sont stockées dans la base de données

**L'utilisateur n'a rien à faire** - tout est automatique ! 🎉

---

## 📝 Résumé

- **Day Streak** = Votre série actuelle (peut augmenter ou diminuer)
- **Best Streak** = Votre record personnel (ne fait qu'augmenter)
- Les deux motivent la régularité mais de manières différentes
- Les streaks permettent de débloquer des badges spéciaux
- Les streaks sont mis à jour automatiquement par le backend

**Continuez vos activités quotidiennes pour maintenir et améliorer vos streaks !** 🔥

