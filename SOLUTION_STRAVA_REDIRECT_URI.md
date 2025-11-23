# 🔧 Solution : Erreur "invalid redirect_uri" Strava

## 🎯 Problème Identifié

Le navigateur s'ouvre bien ✅, mais Strava affiche cette erreur :
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

## ✅ Solution 1 : Configurer Correctement dans Strava

### Étape 1 : Aller sur Strava API Settings

1. Allez sur : https://www.strava.com/settings/api
2. Cliquez sur votre application (ID: 186697)

### Étape 2 : Configurer le Redirect URI

**Option A : Si vous voyez un champ "Authorization Callback URI" ou "Redirect URI" :**

Entrez exactement :
```
nexofitness://strava/callback
```

**Option B : Si vous ne voyez que "Authorization Callback Domain" :**

Entrez seulement :
```
nexofitness
```

**Et dans "Website" (obligatoire) :**
```
https://yourwebsite.com
```
(ou n'importe quelle URL valide, Strava en a besoin)

### Étape 3 : Sauvegarder et Tester

1. Cliquez sur "Save" / "Enregistrer"
2. **Attendez 1-2 minutes** (Strava met parfois du temps à propager)
3. Testez à nouveau dans votre app

## 🔄 Solution 2 : Utiliser une URL HTTP Intermédiaire (Si Solution 1 ne fonctionne pas)

Si Strava ne supporte pas directement les deep links, nous devons utiliser une URL HTTP qui redirige vers le deep link.

### Étape 1 : Créer une Page de Redirection

Créez une page HTML simple sur votre serveur (si vous en avez un) :

```html
<!DOCTYPE html>
<html>
<head>
    <title>Redirecting...</title>
    <script>
        window.location.href = "nexofitness://strava/callback" + window.location.search;
    </script>
</head>
<body>
    <p>Redirecting to app...</p>
</body>
</html>
```

### Étape 2 : Modifier le Code

Si nécessaire, je peux modifier le code pour utiliser une URL HTTP au lieu du deep link direct.

## 🔍 Vérification Actuelle

Votre code envoie actuellement :
```
redirect_uri=nexofitness://strava/callback
```

Dans Strava, assurez-vous que c'est **EXACTEMENT** ce qui est configuré.

## 📸 Ce que vous devez voir dans Strava

Dans les paramètres de votre application Strava, vous devriez voir quelque chose comme :

```
Application Name: [votre nom]
Client ID: 186697
Client Secret: cc6c2bed42ca1be1d98b052aec096135412e6c09
Authorization Callback Domain: nexofitness
Website: https://yourwebsite.com (ou autre URL)
```

**Si vous voyez un champ "Redirect URI" ou "Callback URI", entrez-y :**
```
nexofitness://strava/callback
```

## 🚀 Test Rapide

1. Ouvrez votre application Strava dans https://www.strava.com/settings/api
2. Vérifiez les champs mentionnés ci-dessus
3. Si quelque chose manque ou est incorrect, corrigez-le
4. Sauvegardez
5. Testez à nouveau dans votre app Android

## ❓ Si ça ne fonctionne toujours pas

Envoyez-moi une capture d'écran de vos paramètres Strava (en masquant le Client Secret), et je vous dirai exactement quoi modifier.

---

**Testez d'abord la Solution 1. Si ça ne fonctionne pas, dites-moi et nous passerons à la Solution 2.**

