#!/bin/bash
# =============================================================================
# install.sh - VERSION CORRIGÉE POUR RASPBERRY PI
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

# ─── 2. Installation de Java (DEFAULT) et Dépendances ─────────────────────────
# CORRECTION : Utilisation de default-jdk pour éviter les erreurs d'architecture
install_system_packages() {
    section "Installation des paquets système"
    
    log "Installation du JDK par défaut et de Maven..."
    if apt-get install -y default-jdk maven git wget; then
        ok "Java (default) et Maven installés avec succès ✓"
    else
        die "Échec de l'installation des paquets système."
    fi

    # Vérification de la version active
    java_version=$(java -version 2>&1 | head -n1)
    ok "Utilisation de : $java_version"
}

# ─── 3. Préparation des répertoires ──────────────────────────────────────────
setup_directories() {
    section "Préparation des répertoires"
    mkdir -p "$BASE"
    chown "$REAL_USER:$REAL_USER" "$BASE"
}

# ─── 5. Compilation (CORRIGÉE) ───────────────────────────────────────────────
# CORRECTION : Chemin mis à jour pour pointer vers le bon pom.xml
compile_projects() {
    section "Compilation des projets"
    
    # 1. Compilation de MG2D (souvent requis par les autres projets)
    if [ -d "$BASE/MG2D" ]; then
        log "Compilation de MG2D..."
        cd "$BASE/MG2D"
        sudo -u "$REAL_USER" mvn install -DskipTests
    fi
    
    # 2. Compilation de NodeBuster avec le chemin spécifique
    # Chemin : $BASE/borne-arcade-sae/projet/NodeBuster/
    local NODEBUSTER_PATH="$BASE/borne-arcade-sae/projet/NodeBuster"
    
    if [ -f "$NODEBUSTER_PATH/pom.xml" ]; then
        log "Compilation de NodeBuster dans le bon dossier..."
        cd "$NODEBUSTER_PATH"
        sudo -u "$REAL_USER" mvn clean install -DskipTests
        ok "NodeBuster compilé avec succès ✓"
    else
        die "ERREUR : pom.xml introuvable dans $NODEBUSTER_PATH"
    fi
}

# ─── 6. Configuration du lancement automatique ───────────────────────────────
setup_autostart() {
    section "Configuration du lancement"
    
    # Création du lanceur
    cat <<EOF > "$LOCAL_BIN/borne-arcade"
#!/bin/bash
cd "$BASE/borne-arcade-sae/projet/NodeBuster"
mvn exec:java@borne
EOF
    chmod +x "$LOCAL_BIN/borne-arcade"
    ok "Script de lancement créé dans $LOCAL_BIN/borne-arcade ✓"
}

# ─── Main ────────────────────────────────────────────────────────────────────
main() {
    prepare_system
    install_system_packages
    setup_directories
    
    # Assurez-vous que le dossier git contient bien borne-arcade-sae
    # sync_repos (si vous avez une fonction de clonage)
    
    compile_projects
    setup_autostart
    
    if [ "$NO_RUN" = false ]; then
        log "Lancement de l'application..."
        sudo -u "$REAL_USER" "$LOCAL_BIN/borne-arcade"
    fi
}

main