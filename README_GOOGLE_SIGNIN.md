# 🔐 Configuration Google Sign-In - Guide Rapide

## 📝 Résumé en 3 étapes

### 1️⃣ Obtenir le SHA-1 (Votre empreinte digitale)

**Dans Android Studio :**
- Ouvrez le **Terminal** (en bas)
- Tapez : `.\gradlew signingReport` (Windows) ou `./gradlew signingReport` (Mac/Linux)
- Cherchez `SHA1: A1:B2:C3:...` dans le résultat
- **Copiez seulement la partie après "SHA1:"**

### 2️⃣ Aller sur Google Cloud Console

- Ouvrez : https://console.cloud.google.com/
- Créez un projet (ou utilisez un existant)
- Activez "Google Sign-In API"
- Créez un "OAuth Client ID" pour Android

### 3️⃣ Ajouter le SHA-1 dans Google Cloud

- Dans "Identifiants" > "OAuth Client ID"
- Collez votre SHA-1
- Package name : `com.example.damandroid`
- Sauvegardez

---

## 🎯 Guide Détaillé

Pour un guide complet avec toutes les étapes détaillées, ouvrez :
- **`CONFIGURATION_GOOGLE_SIGNIN.md`** ← Guide simple en français
- **`GOOGLE_SIGN_IN_SETUP.md`** ← Guide complet avec détails
- **`GET_SHA1.md`** ← Comment obtenir le SHA-1

---

## ⚡ Commande Rapide pour obtenir SHA-1

**Double-cliquez sur** :
- `get_sha1.bat` (Windows)
- `get_sha1.sh` (Mac/Linux)

Ou dans le terminal du projet :
```bash
.\gradlew signingReport    # Windows
./gradlew signingReport    # Mac/Linux
```

---

## ✅ Checklist

- [ ] SHA-1 obtenu
- [ ] Projet créé dans Google Cloud Console
- [ ] Google Sign-In API activée
- [ ] OAuth Client ID créé avec SHA-1
- [ ] Package name vérifié : `com.example.damandroid`
- [ ] Attendu 5-10 minutes après configuration

---

## 🆘 Problème ?

1. **Erreur "DEVELOPER_ERROR"** → Vérifiez le SHA-1 dans Google Cloud Console
2. **Rien ne s'affiche** → Attendez 5-10 minutes après avoir ajouté le SHA-1
3. **Package name incorrect** → Vérifiez qu'il correspond exactement dans `build.gradle.kts`

---

**Votre code est déjà prêt ! Il ne reste qu'à configurer Google Cloud Console.** 🚀

