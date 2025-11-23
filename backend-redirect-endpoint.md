# 🚀 Endpoint Backend pour Redirection Strava

## 📝 Code Backend NestJS

Ajoutez cet endpoint dans votre backend NestJS :

### Option A : Controller avec @Redirect() (Recommandé)

```typescript
// strava.controller.ts ou dans un controller existant
import { Controller, Get, Query, Redirect } from '@nestjs/common';

@Controller()
export class StravaController {
  @Get('strava/callback')
  @Redirect()
  stravaCallback(
    @Query('code') code: string,
    @Query('error') error: string,
    @Query('error_description') errorDescription: string,
  ) {
    // En cas d'erreur
    if (error) {
      const errorUrl = `nexofitness://strava/callback?error=${encodeURIComponent(error)}`;
      if (errorDescription) {
        return { url: `${errorUrl}&error_description=${encodeURIComponent(errorDescription)}` };
      }
      return { url: errorUrl };
    }
    
    // Rediriger vers l'app avec le code d'autorisation
    return { url: `nexofitness://strava/callback?code=${code}` };
  }
}
```

### Option B : Page HTML Simple

Si vous préférez servir une page HTML statique :

```typescript
import { Controller, Get, Query, Res } from '@nestjs/common';
import { Response } from 'express';

@Controller()
export class StravaController {
  @Get('strava/callback')
  stravaCallback(
    @Query('code') code: string,
    @Query('error') error: string,
    @Res() res: Response,
  ) {
    const html = `
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Redirecting to NexoFitness...</title>
    <script>
        (function() {
            const urlParams = new URLSearchParams(window.location.search);
            const code = urlParams.get('code');
            const error = urlParams.get('error');
            const errorDescription = urlParams.get('error_description');
            
            let redirectUrl = 'nexofitness://strava/callback';
            const params = new URLSearchParams();
            
            if (code) {
                params.append('code', code);
            }
            if (error) {
                params.append('error', error);
                if (errorDescription) {
                    params.append('error_description', errorDescription);
                }
            }
            
            if (params.toString()) {
                redirectUrl += '?' + params.toString();
            }
            
            // Rediriger immédiatement
            window.location.href = redirectUrl;
            
            // Fallback si la redirection ne fonctionne pas
            setTimeout(function() {
                document.getElementById('fallback').style.display = 'block';
                document.getElementById('redirect-link').href = redirectUrl;
            }, 2000);
        })();
    </script>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .container {
            text-align: center;
            padding: 2rem;
        }
        #fallback {
            display: none;
            margin-top: 2rem;
        }
        a {
            color: white;
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Redirecting to NexoFitness...</h1>
        <p>Please wait...</p>
        <div id="fallback">
            <p>If you are not redirected automatically, <a id="redirect-link" href="#">click here</a>.</p>
        </div>
    </div>
</body>
</html>
    `;
    
    res.setHeader('Content-Type', 'text/html');
    res.send(html);
  }
}
```

## 🔧 Instructions

1. **Ajoutez le code** dans votre backend NestJS
2. **Testez l'endpoint** en allant sur :
   - `https://apinest-production.up.railway.app/strava/callback?code=test123`
   - Devrait rediriger vers `nexofitness://strava/callback?code=test123`
3. **Configurez Strava** avec le nouveau redirect URI
4. **Modifiez le code Android** pour utiliser l'URL HTTP

## ✅ Avantages

- ✅ URL HTTP valide (Strava l'accepte)
- ✅ Redirection automatique vers le deep link
- ✅ Gestion des erreurs
- ✅ Fonctionne sur tous les navigateurs

---

**Après avoir ajouté cet endpoint, dites-moi et je modifierai le code Android !**

