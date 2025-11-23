# Comment Obtenir votre SHA-1 Certificate Fingerprint

Ce guide vous montre différentes méthodes pour obtenir votre SHA-1, nécessaire pour configurer Google Sign-In.

## 🔍 Méthode 1 : Via Android Studio Gradle (RECOMMANDÉ - La plus simple)

1. **Ouvrez Android Studio**
2. **Ouvrez le terminal** en bas de l'écran (onglet "Terminal")
3. **Dans le terminal**, naviguez vers le dossier racine de votre projet
4. **Exécutez cette commande** :

### Sur Windows :
```powershell
.\gradlew signingReport
```

### Sur macOS/Linux :
```bash
./gradlew signingReport
```

5. **Cherchez dans la sortie** les lignes qui contiennent "SHA1"
6. **Copiez le SHA-1** pour :
   - `V1 signing config: debug` - Pour le debug
   - `V1 signing config: release` - Pour le release (si configuré)

**Exemple de sortie** :
```
V1 signing config: debug
  - SHA1: A1:B2:C3:D4:E5:F6:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE
```

Copiez seulement : `A1:B2:C3:D4:E5:F6:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE`

---

## 🔍 Méthode 2 : Via Keytool (Manuel)

### Pour Debug Keystore :

#### Windows (PowerShell) :
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

#### Windows (CMD) :
```cmd
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### macOS/Linux :
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Pour Release Keystore :

```bash
keytool -list -v -keystore /chemin/vers/votre/keystore.jks -alias votre-alias
```

Remplacez :
- `/chemin/vers/votre/keystore.jks` : Le chemin complet vers votre fichier keystore
- `votre-alias` : L'alias de votre clé

**Il vous sera demandé le mot de passe** du keystore et de la clé.

---

## 🔍 Méthode 3 : Via Android Studio (Interface Graphique)

1. **Ouvrez Android Studio**
2. **Allez dans** : **File** > **Project Structure** (ou appuyez sur `Ctrl+Alt+Shift+S` / `Cmd+;` sur Mac)
3. **Sélectionnez** : **Modules** > **app** > **Signing Configs**
4. **Pour debug** : Le SHA-1 peut être visible ici si configuré
5. **Sinon**, utilisez la méthode 1 (Gradle)

---

## 📋 Exemple de Sortie Complète

Quand vous exécutez `keytool -list -v`, vous verrez quelque chose comme :

```
Alias name: androiddebugkey
Creation date: Jan 1, 2024
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=Android Debug, O=Android, C=US
Issuer: CN=Android Debug, O=Android, C=US
Serial number: 1234567890abcdef
Valid from: Mon Jan 01 00:00:00 UTC 2024 until: Mon Jan 01 00:00:00 UTC 2054
Certificate fingerprints:
     SHA1: A1:B2:C3:D4:E5:F6:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE
     SHA256: 12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3
```

**Pour Google Sign-In, vous avez besoin du SHA-1** : `A1:B2:C3:D4:E5:F6:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE`

---

## ⚠️ Important

1. **Ne partagez JAMAIS votre keystore de release** ou son mot de passe
2. **Le SHA-1 debug** est le même pour tous sur votre machine locale
3. **Le SHA-1 release** est unique à votre keystore
4. **Copiez seulement la partie après "SHA1:"** (sans "SHA1:" au début)
5. **Pas d'espaces** avant ou après le SHA-1

---

## 🐛 Problèmes Courants

### Erreur : "keystore was tampered with, or password was incorrect"
- **Solution** : Vérifiez que vous utilisez le bon mot de passe
- Pour debug, le mot de passe est toujours `android`

### Erreur : "keytool: command not found"
- **Solution** : Assurez-vous que Java JDK est installé et dans votre PATH
- Vérifiez avec : `java -version`

### Le fichier debug.keystore n'existe pas
- **Solution** : Lancez l'application une fois depuis Android Studio, il sera créé automatiquement

---

## ✅ Vérification

Une fois que vous avez votre SHA-1 :

1. ✅ Copiez-le (seulement les caractères hexadécimaux, sans "SHA1:")
2. ✅ Allez dans Google Cloud Console > APIs & Services > Credentials
3. ✅ Modifiez votre OAuth 2.0 Client ID Android
4. ✅ Collez le SHA-1 dans le champ "SHA-1 certificate fingerprint"
5. ✅ Sauvegardez

**Attendez 5-10 minutes** après avoir ajouté le SHA-1 pour que les changements prennent effet.

