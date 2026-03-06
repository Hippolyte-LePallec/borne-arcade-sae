#!/bin/bash
# =============================================================================
# install.sh - VERSION RASPBERRY PI (ARM) - CIBLE UNIQUE NODEBUSTER
# =============================================================================

set -e

# ─── Couleurs pour les logs ───────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BLUE='\033[0;34m'
NC='\033[0m'

log()  { echo -e "${CYAN}[INFO]${NC}  $*"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $*"; }
die()  { echo -e "${RED}[ERREUR]${NC} $*" >&2; exit 1; }
section() { echo -e "\n${BLUE}━━━ $* ━━━${NC}\n"; }

# ─── Configuration ───────────────────────────────────────────────────────────
if [ "$EUID" -ne 0 ]; then
    die "Ce script doit être lancé avec sudo : sudo $0"
fi

REAL_USER="${SUDO_USER:-$USER}"
REAL_HOME=$(getent passwd "$REAL_USER" | cut -d: -f6)
BASE="$REAL_HOME/git"
# Chemin exact vers NodeBuster d'après vos informations
PATH_NODEBUSTER="$BASE/borne-arcade-sae/projet/NodeBuster"
LOCAL_BIN="/usr/local/bin"

# ─── 1. Préparation du système ──────────────────────────────────────────────
prepare_system() {
    section "Installation des dépendances"
    apt-get update -qq
    # Installation du JDK par défaut (compatible ARM) et Maven
    apt-get install -y default-jdk maven git wget
    ok "Système prêt avec Java $(java -version 2>&1 | head -n1)"
}

# ─── 2. Compilation de NodeBuster ─────────────────────────────────────────────
compile_nodebuster() {
    section "Compilation du projet NodeBuster"
    
    if [ -d "$PATH_NODEBUSTER" ]; then
        cd "$PATH_NODEBUSTER"
        
        # On vérifie si le pom.xml est présent ici
        if [ -f "pom.xml" ]; then
            log "Lancement de la compilation Maven dans : $PATH_NODEBUSTER"
            # On lance en tant qu'utilisateur normal pour éviter les problèmes de droits
            sudo -u "$REAL_USER" mvn clean install -DskipTests
            ok "NodeBuster a été compilé avec succès."
        else
            die "ERREUR : Aucun fichier pom.xml trouvé dans $PATH_NODEBUSTER"
        fi
    else
        die "ERREUR : Le répertoire $PATH_NODEBUSTER n'existe pas."
    fi
}

# ─── 3. Configuration du lancement ───────────────────────────────────────────
setup_launcher() {
    section "Configuration du lanceur"

    # Création d'un script de commande simple pour lancer le jeu n'importe où
    cat <<EOF > "$LOCAL_BIN/borne-arcade"
#!/bin/bash
cd "$PATH_NODEBUSTER"
exec mvn exec:java@borne
EOF
    chmod +x "$LOCAL_BIN/borne-arcade"
    
    # Création du dossier autostart s'il n'existe pas
    local AUTO_DIR="$REAL_HOME/.config/autostart"
    sudo -u "$REAL_USER" mkdir -p "$AUTO_DIR"

    # Fichier de lancement automatique pour l'interface graphique du Pi
    cat <<EOF > "$AUTO_DIR/borne.desktop"
[Desktop Entry]
Type=Application
Name=NodeBuster Arcade
Exec=$LOCAL_BIN/borne-arcade
Terminal=false
EOF
    chown "$REAL_USER:$REAL_USER" "$AUTO_DIR/borne.desktop"
    
    ok "Commande 'borne-arcade' créée et configurée pour le démarrage."
}

# ─── Exécution ────────────────────────────────────────────────────────────────
main() {
    prepare_system
    compile_nodebuster
    setup_launcher
    
    section "Installation terminée !"
    log "Vous pouvez lancer le jeu avec la commande : borne-arcade"
    
    # Lancement automatique immédiat
    sudo -u "$REAL_USER" "$LOCAL_BIN/borne-arcade"
}

main