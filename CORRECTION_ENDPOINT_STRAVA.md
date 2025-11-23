# 🔧 Correction de l'Endpoint Strava

## ❌ Problème

L'endpoint reçoit des requêtes sans code :
```
[StravaController] [Strava] Callback received - code: missing, error: none
[StravaController] [Strava] No authorization code received
```

## ✅ Solution

Modifiez l'endpoint pour gérer tous les cas, y compris les requêtes de test :

```typescript
import { Controller, Get, Query, Redirect } from '@nestjs/common';

@Controller()
export class StravaController {
  @Get('strava/callback')
  @Redirect()
  stravaCallback(
    @Query('code') code?: string,
    @Query('error') error?: string,
    @Query('error_description') errorDescription?: string,
  ) {
    // Log pour déboguer
    console.log('[Strava] Callback received:', { code: code || 'missing', error: error || 'none' });
    
    // En cas d'erreur
    if (error) {
      const errorUrl = `nexofitness://strava/callback?error=${encodeURIComponent(error)}`;
      if (errorDescription) {
        return { url: `${errorUrl}&error_description=${encodeURIComponent(errorDescription)}` };
      }
      return { url: errorUrl };
    }
    
    // Si pas de code (requête de test ou erreur)
    if (!code) {
      // Rediriger quand même vers l'app pour tester
      return { url: 'nexofitness://strava/callback?error=no_code' };
    }
    
    // Rediriger vers l'app avec le code d'autorisation
    return { url: `nexofitness://strava/callback?code=${code}` };
  }
}
```

## 🧪 Test de l'Endpoint

### Test 1 : Avec un code (simulation réussie)
```
https://apinest-production.up.railway.app/strava/callback?code=test123
```
Devrait rediriger vers : `nexofitness://strava/callback?code=test123`

### Test 2 : Avec une erreur
```
https://apinest-production.up.railway.app/strava/callback?error=access_denied
```
Devrait rediriger vers : `nexofitness://strava/callback?error=access_denied`

### Test 3 : Sans paramètres (test simple)
```
https://apinest-production.up.railway.app/strava/callback
```
Devrait rediriger vers : `nexofitness://strava/callback?error=no_code`

## ✅ Vérification

1. **Modifiez l'endpoint** avec le code ci-dessus
2. **Redéployez** le backend
3. **Testez** l'endpoint dans votre navigateur :
   - `https://apinest-production.up.railway.app/strava/callback?code=test123`
   - Devrait vous rediriger vers l'app Android

## 🚀 Prochaines Étapes

Une fois l'endpoint corrigé et testé :

1. ✅ Vérifiez que l'endpoint redirige correctement
2. ✅ Configurez Strava avec le domaine
3. ✅ Testez le flux complet dans l'app Android

---

**Modifiez l'endpoint, redéployez, et testez ! Ensuite, configurez Strava.** 🎯

