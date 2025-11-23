# 🔧 Correction : Erreur "invalid redirect_uri" Strava OAuth

## ❌ Problème

Quand vous cliquez sur "Connecter Strava", le navigateur s'ouvre mais affiche une erreur JSON :
```json
{
  "message": "Bad Request",
  "errors": [{
    "resource": "Application",
    "field": "redirect_uri",
    "code": "invalid"
  }]
}
```

## ✅ Solution

Le problème vient de la configuration du `redirect_uri` dans votre application Strava. Il doit correspondre **EXACTEMENT** à ce qui est dans le code.

### Étape 1 : Vérifier le redirect_uri dans le code

Dans `StravaOAuthHelper.kt`, le `REDIRECT_URI` est configuré comme :
```kotlin
private const val REDIRECT_URI = "nexofitness://strava/callback"
```

### Étape 2 : Configurer dans Strava

1. Allez sur https://www.strava.com/settings/api
2. Cliquez sur votre application (ou créez-en une nouvelle)
3. Dans la section **"Authorization Callback Domain"**, entrez :
   ```
   nexofitness
   ```
   **⚠️ IMPORTANT : Pas de `://`, pas de `/strava/callback`, juste `nexofitness`**

4. Dans la section **"Website"**, vous pouvez mettre :
   ```
   https://yourwebsite.com
   ```
   (ou n'importe quelle URL valide)

5. **Dans la section "Authorization Callback URI" (si elle existe)** ou dans un champ séparé, entrez le redirect URI complet :
   ```
   nexofitness://strava/callback
   ```

### Étape 3 : Si le champ "Authorization Callback URI" n'existe pas

Si Strava n'a pas de champ séparé pour le redirect URI complet, essayez une de ces solutions :

#### Solution A : Utiliser une URL HTTP comme redirect_uri (Recommandée)

1. Créez une page web simple qui redirige vers votre deep link
2. Dans Strava, configurez cette URL comme redirect_uri
3. Modifiez le code pour utiliser cette URL

#### Solution B : Vérifier la configuration exacte de Strava

Parfois, Strava accepte les deep links directement. Vérifiez que :
- Le domaine de callback est `nexofitness`
- Le redirect URI envoyé correspond exactement

## 🔍 Vérification

D'après les logs, l'URL d'autorisation générée est :
```
https://www.strava.com/oauth/authorize?client_id=186697&redirect_uri=nexofitness%3A%2F%2Fstrava%2Fcallback&response_type=code&scope=activity%3Aread_all%2Cprofile%3Aread_all&approval_prompt=force
```

Le `redirect_uri` décodé est : `nexofitness://strava/callback`

**Assurez-vous que cette valeur EXACTE est configurée dans votre application Strava.**

## 📝 Configuration Strava Recommandée

### Si Strava a un champ "Redirect URI" ou "Callback URI" :

```
Authorization Callback Domain: nexofitness
Redirect URI: nexofitness://strava/callback
```

### Si Strava n'a que "Authorization Callback Domain" :

1. Entrez seulement : `nexofitness`
2. Strava devrait automatiquement accepter `nexofitness://strava/callback`

### Si cela ne fonctionne toujours pas :

Nous devrons peut-être utiliser une URL HTTP intermédiaire. Contactez-moi si cette solution ne fonctionne pas.

## 🚀 Test

Après avoir mis à jour la configuration Strava :

1. **Attendez 1-2 minutes** (Strava peut prendre du temps pour propager les changements)
2. **Fermez complètement votre application** et relancez-la
3. Cliquez à nouveau sur "Connecter Strava"
4. Cette fois, Strava devrait accepter le redirect_uri et vous rediriger vers la page d'autorisation

---

**Important :** Si vous ne voyez toujours pas de champ "Redirect URI" dans Strava, dites-moi et nous utiliserons une autre approche (URL HTTP intermédiaire).

