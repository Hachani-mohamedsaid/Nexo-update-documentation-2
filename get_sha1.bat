@echo off
echo ============================================
echo   Obtention du SHA-1 pour Google Sign-In
echo ============================================
echo.

echo Methode 1: Via Gradle (RECOMMANDE)
echo.
echo Execution de: gradlew signingReport
echo.

call gradlew signingReport

echo.
echo ============================================
echo.
echo Recherchez dans le resultat ci-dessus la ligne qui commence par "SHA1:"
echo Copiez tout ce qui est apres "SHA1:" (les lettres et chiffres separes par des deux-points)
echo.
echo Exemple: A1:B2:C3:D4:E5:F6:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE
echo.
echo ============================================
echo.
echo Appuyez sur une touche pour continuer...
pause > nul

