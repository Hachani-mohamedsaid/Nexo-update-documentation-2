# 🚀 Guide Rapide : Connecter Strava en 5 Minutes

## ✅ Ce qui est déjà configuré

- ✅ Client ID et Client Secret configurés
- ✅ Deep link configuré (`nexofitness://strava/callback`)
- ✅ Intent-filter configuré dans `AndroidManifest.xml`
- ✅ MainActivity prête à recevoir le callback

## 📱 Comment connecter Strava - Étapes Simples

### **Étape 1 : Ouvrir AI Coach**
1. Lancez votre application **NexoFitness**
2. Cliquez sur l'onglet **AI Coach** (icône 🤖 en bas)
3. Vous verrez l'écran **"Synchronisation Fitness requise"**

### **Étape 2 : Cliquer sur "Connecter Strava"**
1. Cliquez sur le bouton bleu **"Connecter Strava"**
2. ⏳ **Attendez 2-3 secondes**
3. Votre navigateur web (Chrome/Firefox) devrait s'ouvrir automatiquement

### **Étape 3 : Autoriser sur Strava**
Sur la page web Strava qui s'ouvre :

1. **Connectez-vous** si vous n'êtes pas connecté :
   - Entrez votre email et mot de passe Strava
   - Ou utilisez "Continuer avec Google" si configuré

2. **Vous verrez cette page** :
   ```
   Authorize nexofitness to use your account?
   
   This application will have access to:
   • View data about your activities
   • View data about your profile
   
   [Cancel]  [Authorize]
   ```

3. **Cliquez sur "Authorize"** (Autoriser)

### **Étape 4 : Redirection automatique**
1. Après avoir cliqué sur "Authorize", Strava vous redirige automatiquement
2. Votre application **NexoFitness s'ouvrira automatiquement**
3. Vous ne verrez pas le deep link, c'est normal - il se passe en arrière-plan

### **Étape 5 : Vérifier la connexion**
1. Revenez à l'écran **AI Coach**
2. Cliquez sur **"J'ai accordé les permissions - Vérifier"**
   - OU attendez quelques secondes (la vérification se fait automatiquement)

3. ✅ **Si ça fonctionne**, vous verrez :
   - L'écran AI Coach avec vos vraies données
   - Vos workouts de la semaine
   - Vos calories brûlées
   - Vos minutes d'activité
   - Votre streak

## 🔍 Comment savoir si ça fonctionne ?

### Dans les logs Logcat, vous devriez voir :

**Quand vous cliquez sur "Connecter Strava"** :
```
StravaOAuthHelper: 🚀 Launching Strava OAuth flow...
StravaOAuthHelper: ✅ Browser opened for Strava OAuth
```

**Quand vous autorisez et êtes redirigé** :
```
MainActivity: ✅ Strava OAuth callback received: nexofitness://strava/callback?code=...
MainActivity: 🔄 Exchanging authorization code for access token...
MainActivity: ✅✅✅ Strava OAuth success! Token received
MainActivity: ✅ Strava marked as synced
```

**Si la connexion réussit** :
```
AICoachViewModel: ✅ Strava connecté via OAuth! Marking as synced and refreshing...
StravaDataSource: ✅ Fetched X activities from Strava
```

## ❌ Problèmes Courants

### Problème 1 : Le navigateur ne s'ouvre pas
**Solution** :
- Vérifiez les logs Logcat pour voir l'erreur
- Vérifiez que vous avez une connexion Internet
- Essayez de redémarrer l'application

### Problème 2 : Erreur "Invalid redirect_uri"
**Solution** :
- Vérifiez dans Strava que le **"Domaine du rappel"** est `nexofitness`
- Pas de slash, juste `nexofitness`

### Problème 3 : L'app s'ouvre mais rien ne change
**Solution** :
- Cliquez sur "J'ai accordé les permissions - Vérifier"
- Vérifiez les logs pour voir si le callback a été reçu
- Essayez de fermer et rouvrir l'application

### Problème 4 : Vous voyez toujours l'écran de synchronisation
**Solution** :
1. Vérifiez les logs - y a-t-il une erreur ?
2. Essayez de vous déconnecter et reconnecter
3. Vérifiez que le token a bien été sauvegardé

## 🎯 Résumé Ultra-Rapide

```
1. Ouvrir AI Coach
2. Cliquer "Connecter Strava"
3. Autoriser sur la page web Strava
4. Attendre la redirection automatique
5. Cliquer "J'ai accordé les permissions - Vérifier"
6. ✅ Profiter de vos données Strava !
```

## 📸 À quoi ça ressemble ?

### Écran de synchronisation :
```
┌─────────────────────────────┐
│   [Icône Fitness Center]    │
│                             │
│ Synchronisation Fitness     │
│       requise               │
│                             │
│ Strava est installé mais... │
│                             │
│  [Connecter Strava]         │
│                             │
│  [J'ai accordé les          │
│   permissions - Vérifier]   │
│                             │
│  [Retour]                   │
└─────────────────────────────┘
```

### Après la connexion réussie :
```
┌─────────────────────────────┐
│      AI Coach Overview      │
│                             │
│  📊 Cette Semaine           │
│  ─────────────────          │
│  Workouts: 5                │
│  Calories: 2,350            │
│  Minutes: 180               │
│  Streak: 7 jours            │
│                             │
│  [Voir Détails]             │
└─────────────────────────────┘
```

---

**Bon test ! 🎉**

Si vous rencontrez un problème, consultez les logs Logcat ou le guide détaillé `GUIDE_ETAPES_CONNEXION_STRAVA.md`.

