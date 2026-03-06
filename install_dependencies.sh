#!/bin/bash

# install_dependencies.sh
# Script d'installation mis à jour pour Java 17
# Usage : sudo ./install_dependencies.sh

set -e

if [ "$EUID" -ne 0 ]; then
    echo "Ce script doit être lancé avec des privilèges root (sudo)."
    exit 1
fi

echo "Mise à jour des dépôts..."
apt-get update

echo "Installation des paquets nécessaires (Java 17, Git, Maven...)"
apt-get install -y \
    openjdk-17-jdk \
    git \
    maven \
    x11-apps \
    libasound2-dev \
    pulseaudio \
    alsa-utils

# On s'assure que Java 17 est bien la version par défaut
update-java-alternatives -s java-1.17.0-openjdk-$(dpkg --print-architecture) || true

# Création d'un répertoire git standard
# Note : $HOME en sudo peut pointer vers /root. 
# Si tu veux l'installer pour l'utilisateur pi, il faudra ajuster le chemin.
BASE="$HOME/git"
mkdir -p "$BASE"
cd "$BASE"

# Clonage ou mise à jour de MG2D
if [ ! -d "MG2D" ]; then
    echo "Clonage de la bibliothèque MG2D..."
    git clone http://iut.univ-littoral.fr/gitlab/synave/MG2D.git
else
    echo "Mise à jour de MG2D..."
    cd MG2D && git pull && cd ..
fi

# Clonage ou mise à jour du projet borne_arcade
if [ ! -d "borne_arcade" ]; then
    echo "Clonage du projet borne_arcade..."
    git clone http://iut.univ-littoral.fr/gitlab/synave/borne_arcade.git
else
    echo "Mise à jour de borne_arcade..."
    cd borne_arcade && git pull && cd ..
fi

# Installation du fichier .desktop pour démarrage automatique
AUTOSTART_DIR="/etc/xdg/autostart"
if [ -f "$BASE/borne_arcade/borne.desktop" ]; then
    echo "Installation du lanceur dans $AUTOSTART_DIR"
    mkdir -p "$AUTOSTART_DIR"
    cp "$BASE/borne_arcade/borne.desktop" "$AUTOSTART_DIR/"
fi

echo "-------------------------------------------------------"
echo "Installation terminée avec Java 17."
java -version
echo "-------------------------------------------------------"

cat <<'EOF'

Les dépôts sont prêts dans :
    $HOME/git/MG2D
    $HOME/git/borne_arcade

Prochaines étapes :
    cd ~/git/borne_arcade
    ./Script/compilation.sh
EOF