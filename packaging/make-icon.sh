#!/usr/bin/env bash
#
# Regenere l'icone macOS de SuperCat (packaging/SuperCat.icns).
#
# Dessine les images avec IconGenerator.java puis assemble le .icns avec
# l'outil macOS "iconutil". A relancer apres toute modification de l'icone.
#
set -euo pipefail
cd "$(dirname "$0")"

ICONSET="../target/SuperCat.iconset"
rm -rf "$ICONSET"
mkdir -p "$ICONSET"

echo "==> Dessin des images de l'icone..."
java IconGenerator.java "$ICONSET"

echo "==> Assemblage du fichier .icns..."
iconutil -c icns "$ICONSET" -o SuperCat.icns

echo "Termine : packaging/SuperCat.icns"
