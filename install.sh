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

# ─── 1. Préparation du système ──────────────────────────────────────────────
prepare_system() {
    section "Préparation du système"
    log "Mise à jour des dépôts..."
    apt-get update -qq
}

# ─── 2. Installation de Java (DEFAULT-JDK) et Dépendances ─────────────────────
install_packages() {
    section "Installation des dépendances ARM"
    
    log "Installation de Java (version système), Maven et Git..."
    # On installe les paquets natifs du Raspberry Pi
    apt-get install -y default-jdk maven git wget libx11-dev libxext-dev libxrender-dev libxtst-dev

    if [ $? -eq 0 ]; then
        ok "Installation des paquets réussie ✓"
    else
        die "Erreur lors de l'installation via apt-get."
    fi

    java_version=$(java -version 2>&1 | head -n1)
    ok "Java actif : $java_version"
}

# ─── 3. Gestion des Répertoires ──────────────────────────────────────────────
setup_env() {
    section "Configuration de l'environnement"
    mkdir -p "$BASE"
    chown "$REAL_USER:$REAL_USER" "$BASE"
}

# ─── 4. Synchronisation des projets Git ──────────────────────────────────────
sync_repos() {
    section "Synchronisation des sources"
    
    cd "$BASE"

    # MG2D
    if [ ! -d "MG2D" ]; then
        log "Clonage de MG2D..."
        # REMPLACEZ PAR VOTRE LIEN REEL SI NECESSAIRE
        sudo -u "$REAL_USER" git clone https://github.com/MG2D/MG2D.git MG2D || warn "Lien MG2D inaccessible"
    else
        log "Mise à jour de MG2D..."
        cd MG2D && sudo -u "$REAL_USER" git pull && cd ..
    fi

    # Borne Arcade
    if [ ! -d "borne_arcade" ]; then
        log "Clonage de borne_arcade..."
        # REMPLACEZ PAR VOTRE LIEN REEL SI NECESSAIRE
        sudo -u "$REAL_USER" git clone https://github.com/votre-compte/borne_arcade.git borne_arcade || warn "Lien borne_arcade inaccessible"
    else
        log "Mise à jour de la borne..."
        cd borne_arcade && sudo -u "$REAL_USER" git pull && cd ..
    fi
}

# ─── 5. Compilation ──────────────────────────────────────────────────────────
build() {
    section "Compilation Maven"
    
    # Compilation MG2D (nécessaire pour la borne)
    if [ -d "$BASE/MG2D" ]; then
        log "Installation locale de MG2D..."
        cd "$BASE/MG2D"
        sudo -u "$REAL_USER" mvn clean install -DskipTests
    fi

    # Compilation Borne
    if [ -d "$BASE/borne_arcade" ]; then
        log "Build de la Borne Arcade..."
        cd "$BASE/borne_arcade"
        sudo -u "$REAL_USER" mvn clean install -DskipTests
        ok "Compilation terminée ✓"
    else
        die "Dossier borne_arcade introuvable."
    fi
}

# ─── 6. Lancement Automatique ────────────────────────────────────────────────
setup_autostart() {
    section "Configuration de l'Autostart"

    # Wrapper binaire
    cat <<EOF > "$LOCAL_BIN/borne-arcade"
#!/bin/bash
cd "$BASE/borne_arcade"
exec mvn exec:java@borne
EOF
    chmod +x "$LOCAL_BIN/borne-arcade"

    # Entrée Desktop pour la session graphique
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
        section "Lancement..."
        sudo -u "$REAL_USER" "$LOCAL_BIN/borne-arcade"
    else
        ok "Installation terminée. Tapez 'borne-arcade' pour jouer !"
    fi
}

main