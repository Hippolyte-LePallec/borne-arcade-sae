#!/bin/bash
# =============================================================================
# install.sh - VERSION CORRIGÉE POUR RASPBERRY PI (ARM) - DEFAULT JDK
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

# ─── 1. Mise à jour des dépôts ────────────────────────────────────────────────
check_dependencies() {
    section "Mise à jour du système"
    apt-get update -qq
    ok "Dépôts mis à jour"
}

# ─── 2. Installation de Java (DEFAULT-JDK) et Maven ──────────────────────────
install_system_packages() {
    section "Installation des paquets système"
    
    log "Installation du JDK par défaut et de Maven..."
    # Utilisation de default-jdk pour une compatibilité maximale avec l'OS
    if apt-get install -y default-jdk maven git wget; then
        ok "Java (default) et Maven installés avec succès ✓"
    else
        die "Échec de l'installation. Vérifiez votre connexion ou vos dépôts."
    fi

    # Affichage de la version pour vérification
    java_version=$(java -version 2>&1 | head -n1)
    ok "Version installée : $java_version"
}

# ─── 3. Préparation des répertoires ──────────────────────────────────────────
setup_directories() {
    section "Préparation des répertoires"
    mkdir -p "$BASE"
    chown "$REAL_USER:$REAL_USER" "$BASE"
    log "Répertoire de travail : $BASE"
}

# ─── 4. Gestion des dépôts Git ───────────────────────────────────────────────
clone_or_update() {
    local repo_url=$1
    local dir_name=$2
    cd "$BASE"
    if [ -d "$dir_name" ]; then
        log "Mise à jour de $dir_name..."
        cd "$dir_name" && sudo -u "$REAL_USER" git pull
    else
        log "Clonage de $dir_name..."
        sudo -u "$REAL_USER" git clone "$repo_url" "$dir_name"
    fi
}

# ─── 5. Compilation ──────────────────────────────────────────────────────────
compile_projects() {
    section "Compilation des projets"
    
    # MG2D
    if [ -d "$BASE/MG2D" ]; then
        log "Compilation de MG2D..."
        cd "$BASE/MG2D"
        sudo -u "$REAL_USER" mvn install -DskipTests
    fi
    
    # Borne Arcade
    if [ -d "$BASE/borne_arcade" ]; then
        log "Compilation de Borne Arcade..."
        cd "$BASE/borne_arcade"
        sudo -u "$REAL_USER" mvn clean install -DskipTests
        ok "Compilations terminées ✓"
    else
        warn "Dossier borne_arcade introuvable, compilation sautée."
    fi
}

# ─── 6. Configuration du lancement automatique ───────────────────────────────
setup_autostart() {
    section "Configuration du lancement automatique"
    
    # Création du script de lancement rapide
    cat <<EOF > "$LOCAL_BIN/borne-arcade"
#!/bin/bash
cd "$BASE/borne_arcade"
mvn exec:java@borne
EOF
    chmod +x "$LOCAL_BIN/borne-arcade"

    # Création du dossier autostart pour l'utilisateur
    mkdir -p "$REAL_HOME/.config/autostart"
    
    cat <<EOF > "$REAL_HOME/.config/autostart/borne.desktop"
[Desktop Entry]
Type=Application
Name=Borne Arcade
Exec=$LOCAL_BIN/borne-arcade
X-GNOME-Autostart-enabled=true
EOF
    
    chown -R "$REAL_USER:$REAL_USER" "$REAL_HOME/.config"
    ok "Autostart configuré dans $REAL_HOME/.config/autostart ✓"
}

# ─── Exécution principale ─────────────────────────────────────────────────────
main() {
    check_dependencies
    install_system_packages
    setup_directories
    
    # --- DECOMMENTER ET MODIFIER LES URLS CI-DESSOUS ---
    # clone_or_update "https://github.com/votre-compte/MG2D.git" "MG2D"
    # clone_or_update "https://github.com/votre-compte/borne_arcade.git" "borne_arcade"
    
    compile_projects
    setup_autostart
    
    if [ "$NO_RUN" = false ]; then
        section "Lancement de la borne..."
        sudo -u "$REAL_USER" "$LOCAL_BIN/borne-arcade"
    else
        ok "Installation terminée (sans lancement)."
    fi
}

main