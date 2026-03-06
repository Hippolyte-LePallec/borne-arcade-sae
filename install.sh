#!/bin/bash
# =============================================================================
# install.sh - VERSION RASPBERRY PI (ARM) - CORRECTIF DÉFINITIF
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
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }
die()  { echo -e "${RED}[ERREUR]${NC} $*" >&2; exit 1; }
section() { echo -e "\n${BLUE}━━━ $* ━━━${NC}\n"; }

# ─── Configuration ───────────────────────────────────────────────────────────
NO_RUN=false
[ "$1" = "--no-run" ] && NO_RUN=true

if [ "$EUID" -ne 0 ]; then
    die "Ce script doit être lancé avec sudo : sudo $0"
fi

REAL_USER="${SUDO_USER:-$USER}"
REAL_HOME=$(getent passwd "$REAL_USER" | cut -d: -f6)
BASE="$REAL_HOME/git"
LOCAL_BIN="/usr/local/bin"

# ─── 1. Mise à jour et Nettoyage ──────────────────────────────────────────────
prepare_system() {
    section "Préparation du système"
    log "Mise à jour des listes de paquets..."
    apt-get update -qq
    ok "Dépôts mis à jour"
}

# ─── 2. Installation de Java (DEFAULT-JDK) et Dépendances ─────────────────────
install_packages() {
    section "Installation des dépendances"
    
    log "Installation de default-jdk (Java), Maven et Git..."
    # On installe les paquets natifs du Raspberry Pi (ARM)
    # default-jdk installe la version la plus stable pour votre version d'OS
    apt-get install -y default-jdk maven git wget libx11-dev libxext-dev libxrender-dev libxtst-dev

    if [ $? -eq 0 ]; then
        ok "Installation des paquets réussie ✓"
    else
        die "Erreur lors de l'installation des paquets via apt."
    fi

    java_version=$(java -version 2>&1 | head -n1)
    ok "Java utilisé : $java_version"
}

# ─── 3. Gestion des Répertoires ──────────────────────────────────────────────
setup_env() {
    section "Configuration de l'environnement"
    mkdir -p "$BASE"
    chown "$REAL_USER:$REAL_USER" "$BASE"
    log "Base de travail : $BASE"
}

# ─── 4. Clonage / Mise à jour des dépôts ──────────────────────────────────────
sync_repos() {
    section "Synchronisation des sources"
    
    # MG2D
    cd "$BASE"
    if [ ! -d "MG2D" ]; then
        log "Clonage de MG2D..."
        sudo -u "$REAL_USER" git clone https://github.com/synave/MG2D.git MG2D || warn "Lien MG2D à vérifier"
    else
        log "Mise à jour de MG2D..."
        cd MG2D && sudo -u "$REAL_USER" git pull && cd ..
    fi

    # Borne Arcade
    if [ ! -d "borne_arcade" ]; then
        log "Clonage de la borne..."
        sudo -u "$REAL_USER" git clone https://github.com/Hippolyte-LePallec/borne-arcade-sae.git borne_arcade || warn "Lien borne_arcade à vérifier"
    else
        log "Mise à jour de la borne..."
        cd borne_arcade && sudo -u "$REAL_USER" git pull && cd ..
    fi
}

# ─── 5. Compilation ──────────────────────────────────────────────────────────
build() {
    section "Compilation Maven"
    
    # Compilation MG2D
    if [ -d "$BASE/MG2D" ]; then
        log "Build MG2D..."
        cd "$BASE/MG2D"
        sudo -u "$REAL_USER" mvn clean install -DskipTests
        ok "MG2D compilé et installé localement."
    fi

    # Compilation Borne
    if [ -d "$BASE/borne_arcade" ]; then
        log "Build Borne Arcade..."
        cd "$BASE/borne_arcade"
        sudo -u "$REAL_USER" mvn clean install -DskipTests
        ok "Borne Arcade compilée."
    else
        die "Dossier borne_arcade introuvable pour la compilation."
    fi
}

# ─── 6. Lancement Automatique ────────────────────────────────────────────────
setup_autostart() {
    section "Configuration de l'Autostart"

    # Création du lanceur binaire
    cat <<EOF > "$LOCAL_BIN/borne-arcade"
#!/bin/bash
cd "$BASE/borne_arcade"
exec mvn exec:java@borne
EOF
    chmod +x "$LOCAL_BIN/borne-arcade"

    # Création de l'entrée Desktop
    local AUTOSTART_DIR="$REAL_HOME/.config/autostart"
    sudo -u "$REAL_USER" mkdir -p "$AUTOSTART_DIR"
    
    cat <<EOF > "$AUTOSTART_DIR/borne.desktop"
[Desktop Entry]
Type=Application
Name=Borne Arcade
Exec=$LOCAL_BIN/borne-arcade
Terminal=false
X-GNOME-Autostart-enabled=true
EOF
    chown "$REAL_USER:$REAL_USER" "$AUTOSTART_DIR/borne.desktop"
    ok "Le jeu se lancera automatiquement au démarrage de la session graphique."
}

# ─── Exécution ────────────────────────────────────────────────────────────────
main() {
    prepare_system
    install_packages
    setup_env
    sync_repos
    build
    setup_autostart

    if [ "$NO_RUN" = false ]; then
        section "Lancement immédiat..."
        sudo -u "$REAL_USER" "$LOCAL_BIN/borne-arcade"
    else
        ok "Installation terminée avec succès ! Redémarrez ou tapez 'borne-arcade' pour lancer."
    fi
}

main