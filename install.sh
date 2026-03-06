#!/bin/bash
# =============================================================================
# install.sh - VERSION RASPBERRY PI (ARM) - FULL UPDATE & NODEBUSTER FIX
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
# Chemin mis à jour vers NodeBuster
PATH_NODEBUSTER="$BASE/borne_arcade/projet/NodeBuster"
LOCAL_BIN="/usr/local/bin"

# ─── 1. Mise à jour du système (UPDATE & UPGRADE) ─────────────────────────────
prepare_system() {
    section "Mise à jour du système"
    
    log "Exécution de apt-get update..."
    apt-get update -y
    
    log "Exécution de apt-get upgrade (cela peut prendre du temps)..."
    apt-get upgrade -y
    
    ok "Système à jour."
}

# ─── 2. Installation des dépendances (JAVA DEFAULT) ───────────────────────────
install_packages() {
    section "Installation des paquets"
    
    log "Installation du JDK par défaut, Maven et Git..."
    # On installe les paquets natifs compatibles ARM
    apt-get install -y default-jdk maven git wget libx11-dev
    
    ok "Dépendances installées."
    log "Version de Java : $(java -version 2>&1 | head -n1)"
}

# ─── 3. Compilation de NodeBuster (Cible le bon POM) ──────────────────────────
compile_nodebuster() {
    section "Compilation du projet"
    
    if [ -d "$PATH_NODEBUSTER" ]; then
        cd "$PATH_NODEBUSTER"
        
        if [ -f "pom.xml" ]; then
            log "Fichier pom.xml trouvé dans $PATH_NODEBUSTER"
            log "Lancement de la compilation Maven..."
            # Exécution en tant qu'utilisateur pour les droits du dossier .m2
            sudo -u "$REAL_USER" mvn clean install -DskipTests
            ok "NodeBuster compilé avec succès."
        else
            die "ERREUR : Aucun pom.xml trouvé dans $PATH_NODEBUSTER. Vérifiez l'arborescence."
        fi
    else
        die "ERREUR : Le dossier $PATH_NODEBUSTER n'existe pas."
    fi
}

# ─── 4. Configuration du lancement automatique ───────────────────────────────
setup_launcher() {
    section "Configuration du lanceur"

    # Création du script de commande utilisable partout
    cat <<EOF > "$LOCAL_BIN/borne_arcade"
#!/bin/bash
cd "$PATH_NODEBUSTER"
exec mvn exec:java@borne
EOF
    chmod +x "$LOCAL_BIN/borne_arcade"
    
    # Configuration Autostart X11 (Interface graphique)
    local AUTO_DIR="$REAL_HOME/.config/autostart"
    sudo -u "$REAL_USER" mkdir -p "$AUTO_DIR"

    cat <<EOF > "$AUTO_DIR/borne.desktop"
[Desktop Entry]
Type=Application
Name=NodeBuster Arcade
Exec=$LOCAL_BIN/borne_arcade
Terminal=false
X-GNOME-Autostart-enabled=true
EOF
    chown "$REAL_USER:$REAL_USER" "$AUTO_DIR/borne.desktop"
    
    ok "Lancement automatique configuré."
}

# ─── Exécution du script ──────────────────────────────────────────────────────
main() {
    prepare_system
    install_packages
    # On suppose que le code est déjà présent dans $BASE via Git
    compile_nodebuster
    setup_launcher
    
    section "INSTALLATION RÉUSSIE"
    log "Vous pouvez lancer le jeu manuellement avec la commande : borne_arcade"
    
    # Lancement immédiat
    sudo -u "$REAL_USER" "$LOCAL_BIN/borne_arcade"
}

main