#!/bin/bash

# install_dependencies.sh
# Script d'installation des dépendances nécessaires pour jouer
# à tous les jeux de la borne d'arcade sur une fresh install de
# Raspberry Pi OS (Raspbian).
#
# Usage : sudo ./install_dependencies.sh
#          ou simplement ./install_dependencies.sh si l'utilisateur
#          a les droits sudoers.

set -e

if [ "$EUID" -ne 0 ]; then
    echo "Ce script doit être lancé avec des privilèges root (sudo)."
    exit 1
fi

echo "Mise à jour des paquets..."
apt-get update

echo "Installation des paquets nécessaires : Java, Git, Maven, etc..."
apt-get install -y \
    openjdk-8-jdk \
    git \
    maven \
    x11-apps \
    libasound2-dev \
    pulseaudio \
    alsa-utils

# on peut ajouter d'autres bibliothèques si besoin

# création d'un répertoire git standard
BASE="$HOME/git"
mkdir -p "$BASE"
cd "$BASE"

# clonage de MG2D si absent
if [ ! -d "MG2D" ]; then
    echo "Clonage de la bibliothèque MG2D..."
    git clone http://iut.univ-littoral.fr/gitlab/synave/MG2D.git
else
    echo "Le répertoire MG2D existe déjà, mise à jour..."
    cd MG2D && git pull && cd ..
fi

# clonage du projet borne_arcade si absent
if [ ! -d "borne_arcade" ]; then
    echo "Clonage du projet borne_arcade..."
    git clone http://iut.univ-littoral.fr/gitlab/synave/borne_arcade.git
else
    echo "Le répertoire borne_arcade existe déjà, mise à jour..."
    cd borne_arcade && git pull && cd ..
fi

# installation du fichier .desktop pour démarrage automatique
AUTOSTART_DIR="/etc/xdg/autostart"  # system-wide
if [ -f "$BASE/borne_arcade/borne.desktop" ]; then
    echo "Installation du lanceur dans $AUTOSTART_DIR"
    cp "$BASE/borne_arcade/borne.desktop" "$AUTOSTART_DIR/"
fi

cat <<'EOF'

Installation terminée.

Les dépôts MG2D et borne_arcade sont disponibles dans :
    $HOME/git/MG2D
    $HOME/git/borne_arcade

Pour compiler et lancer la borne :
    cd ~/git/borne_arcade
    ./Script/compilation.sh
    # ou lancez le menu avec java comme indiqué dans README

Vous pouvez redémarrer la machine pour que le menu se lance automatiquement.
EOF
