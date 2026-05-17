#!/usr/bin/env bash
#
# Construit SuperCat en application macOS (.app) autonome et l'installe dans
# le dossier Applications.
#
# L'application embarque son propre environnement Java (jpackage) : aucune
# installation de Java n'est requise pour la lancer ensuite.
#
# Prerequis : un JDK 21+ (avec l'outil jpackage) et Apache Maven.
# Utilisation : ./package-mac.sh
#
set -euo pipefail
cd "$(dirname "$0")"

APP_NAME="SuperCat"
VERSION="1.0.0"
APP_SUPPORT="$HOME/Library/Application Support/$APP_NAME"

echo "==> Compilation et construction du jar autonome..."
mvn -q clean package

FAT_JAR="target/supercat-app.jar"
if [ ! -f "$FAT_JAR" ]; then
    echo "ERREUR : $FAT_JAR introuvable apres le build." >&2
    exit 1
fi

echo "==> Construction de l'application macOS (jpackage)..."
rm -rf target/app-input target/dist
mkdir -p target/app-input
cp "$FAT_JAR" target/app-input/

# JDK complet a embarquer dans l'application : garantit la presence de tous
# les modules necessaires a l'execution -- en particulier la resolution DNS
# (MongoDB "mongodb+srv"), l'audio, les preferences, TLS et SASL.
JDK_HOME="${JAVA_HOME:-}"
if [ -z "$JDK_HOME" ] || [ ! -d "$JDK_HOME" ]; then
    JDK_HOME="$(java -XshowSettings:properties -version 2>&1 \
        | awk -F'= ' '/[[:space:]]java\.home/ {print $2; exit}')"
fi
# resolution des liens symboliques (jpackage exige un chemin reel)
JDK_HOME="$(cd "$JDK_HOME" && pwd -P)"
echo "    Runtime Java embarque : $JDK_HOME"

# icone de l'application (voir packaging/make-icon.sh pour la regenerer)
ICON_OPT=""
if [ -f packaging/SuperCat.icns ]; then
    ICON_OPT="--icon packaging/SuperCat.icns"
    echo "    Icone : packaging/SuperCat.icns"
fi

jpackage \
    --type app-image \
    --name "$APP_NAME" \
    --app-version "$VERSION" \
    --vendor "UPB FILS" \
    --description "SuperCat - jeu de labyrinthe en JavaFX" \
    --input target/app-input \
    --main-jar supercat-app.jar \
    --main-class com.supercat.Main \
    --runtime-image "$JDK_HOME" \
    --java-options "--enable-native-access=ALL-UNNAMED" \
    $ICON_OPT \
    --dest target/dist

echo "==> Installation des identifiants (config.properties)..."
mkdir -p "$APP_SUPPORT"
if [ -f config.properties ]; then
    cp config.properties "$APP_SUPPORT/config.properties"
    echo "    Copie dans : $APP_SUPPORT/config.properties"
else
    echo "    ATTENTION : aucun config.properties trouve a la racine du projet."
    echo "    Cree le fichier suivant (modele : config.properties.example) :"
    echo "      $APP_SUPPORT/config.properties"
    echo "    sans quoi l'application ne pourra pas se connecter a la base."
fi

echo "==> Installation dans /Applications..."
rm -rf "/Applications/$APP_NAME.app"
if cp -R "target/dist/$APP_NAME.app" "/Applications/$APP_NAME.app" 2>/dev/null; then
    echo "    Installe : /Applications/$APP_NAME.app"
else
    echo "    Permissions insuffisantes sur /Applications, essai avec sudo..."
    sudo cp -R "target/dist/$APP_NAME.app" "/Applications/$APP_NAME.app"
    echo "    Installe : /Applications/$APP_NAME.app"
fi

echo ""
echo "Termine. Lance SuperCat depuis le Launchpad ou le dossier Applications."
