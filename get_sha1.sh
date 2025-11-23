#!/bin/bash

echo "============================================"
echo "  Obtention du SHA-1 pour Google Sign-In"
echo "============================================"
echo ""

echo "Méthode 1: Via Gradle (RECOMMANDÉ)"
echo ""
echo "Exécution de: ./gradlew signingReport"
echo ""

./gradlew signingReport

echo ""
echo "============================================"
echo ""
echo "Recherchez dans le résultat ci-dessus la ligne qui commence par 'SHA1:'"
echo "Copiez tout ce qui est après 'SHA1:' (les lettres et chiffres séparés par des deux-points)"
echo ""
echo "Exemple: A1:B2:C3:D4:E5:F6:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE"
echo ""
echo "============================================"

