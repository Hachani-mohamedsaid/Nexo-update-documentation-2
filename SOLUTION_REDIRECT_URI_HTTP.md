# 🔧 Solution : Utiliser une URL HTTP pour le Redirect URI

## ❌ Problème

Strava ne supporte **PAS** les custom schemes (`nexofitness://`) directement comme `redirect_uri`. Il faut utiliser une URL HTTP/HTTPS.

## ✅ Solution : Page de Redirection sur votre Backend

### Option 1 : Utiliser votre Backend (Recommandé)

Puisque vous avez un backend sur `apinest-production.up.railway.app`, créons un endpoint de redirection.

#### Étape 1 : Créer l'endpoint de redirection dans votre backend

Créez une route dans votre backend NestJS (ou servez une page HTML statique) :

```typescript
// strava-redirect.controller.ts ou dans votre controller existant
@Get('strava/callback')
@Redirect()
stravaCallback(@Query('code') code: string, @Query('error') error: string) {
  if (error) {
    // En cas d'erreur, rediriger vers l'app avec l'erreur
    return { url: `nexofitness://strava/callback?error=${error}` };
  }
  
  // Rediriger vers l'app avec le code d'autorisation
  return { url: `nexofitness://strava/callback?code=${code}` };
}
```

Ou avec une page HTML :

```html
<!-- strava-redirect.html -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Redirecting to NexoFitness...</title>
    <script>
        // Récupérer le code ou l'erreur de l'URL
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');
        const error = urlParams.get('error');
        
        // Construire l'URL de redirection
        let redirectUrl = 'nexofitness://strava/callback';
        if (code) {
            redirectUrl += '?code=' + encodeURIComponent(code);
        } else if (error) {
            redirectUrl += '?error=' + encodeURIComponent(error);
        }
        
        // Rediriger immédiatement
        window.location.href = redirectUrl;
    </script>
</head>
<body>
    <p>Redirecting to NexoFitness...</p>
    <p>If you are not redirected automatically, <a href="nexofitness://strava/callback">click here</a>.</p>
</body>
</html>
```

#### Étape 2 : Modifier le REDIRECT_URI dans le code

Modifiez `StravaOAuthHelper.kt` :

```kotlin
// URL de redirection OAuth (doit correspondre à celle configurée dans Strava)
private const val REDIRECT_URI = "https://apinest-production.up.railway.app/strava/callback"
```

#### Étape 3 : Configurer dans Strava

Dans le formulaire Strava :
- **"Domaine du rappel pour l'autorisation"** : `apinest-production.up.railway.app`
- **"Site Web"** : `https://apinest-production.up.railway.app`

### Option 2 : Utiliser un Service Gratuit (Alternative)

Si vous ne pouvez pas modifier le backend maintenant, utilisez un service comme `https://redirect.me` ou créez une page GitHub Pages.

## 🚀 Mise en Place Rapide

### Si vous utilisez Option 1 (Backend) :

1. **Créez l'endpoint de redirection** dans votre backend
2. **Testez** : Allez sur `https://apinest-production.up.railway.app/strava/callback?code=test123`
   - Devrait rediriger vers `nexofitness://strava/callback?code=test123`
3. **Modifiez le code** pour utiliser `https://apinest-production.up.railway.app/strava/callback`
4. **Configurez Strava** avec le nouveau redirect URI
5. **Testez** dans l'app

## 📝 Configuration Strava Finale

Après la mise en place :

```
Domaine du rappel pour l'autorisation: apinest-production.up.railway.app
Site Web: https://apinest-production.up.railway.app
```

Dans le code Android :
```
REDIRECT_URI = "https://apinest-production.up.railway.app/strava/callback"
```

---

**Quelle option préférez-vous ?**
- ✅ Option 1 : Utiliser votre backend (meilleur, plus propre)
- ⚠️ Option 2 : Utiliser un service gratuit (rapide mais moins idéal)

Je peux vous aider à mettre en place l'Option 1 si vous voulez !

