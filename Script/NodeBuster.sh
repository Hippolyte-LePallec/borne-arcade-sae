#!/bin/bash
# Launcher script for the NodeBuster game used by the arcade menu.
# This script is executed from the root of the repository by Graphique/Pointeur.

# Navigate to project directory (one level up from Script/)
cd "$(dirname "$0")/../projet/NodeBuster" || exit 1

# if maven is available we can build or run via the javafx plugin
if command -v mvn >/dev/null 2>&1; then
    mvn clean javafx:run -q
else
    # no maven: try to run using compiled classes if they already exist
    CLASSES=target/classes
    if [ -d "$CLASSES" ]; then
        java -cp "$CLASSES" fr.iutlittoral.App
    else
        echo "Erreur : ni maven ni classes compilées disponibles."
        exit 2
    fi
fi
