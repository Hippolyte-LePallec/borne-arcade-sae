#!/bin/bash
# =============================================================================
# install.sh - VERSION CORRIGÉE (STRUCTURE DE DOSSIERS & JAVA DEFAULT)
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
NO_RUN=false
[ "$1" = "--no-run" ] && NO_RUN=true

if [ "$EUID" -ne 0 ]; then
    die "Ce script doit être lancé avec sudo : sudo $0"
fi

REAL_USER="${SUDO_USER:-$USER}"
REAL_HOME=$(getent passwd "$REAL_USER" | cut -d: -f6)
BASE="$REAL_HOME/git"
LOCAL_BIN="/usr/local/bin"

# ─── 1. Préparation et Installation de Java Default ──────────────────────────
prepare_system() {
    section "Installation des dépendances"
    apt-get update -qq
    # Installation du JDK par défaut pour éviter les erreurs d'architecture ARM
    apt-get install -y default-jdk maven git wget
    ok "Java et Maven prêts."
}

# ─── 2. Configuration des chemins (CORRECTIF) ────────────────────────────────
setup_paths() {
    section "Vérification des chemins"
    
    # Chemin vers MG2D (ajustez si MG2D est aussi dans un sous-dossier)
    # Si le pom.xml de MG2D est dans git/MG2D/MG2D, changez ici :
    PATH_MG2D="$BASE/MG2D" 
    
    # Chemin exact vers NodeBuster d'après votre message
    PATH_NODEBUSTER="$BASE/borne-arcade-sae/projet/NodeBuster"

    mkdir -p "$BASE"
    chown "$REAL_USER:$REAL_USER" "$BASE"
}

# ─── 3. Compilation de MG2D ──────────────────────────────────────────────────
compile_mg2d() {
    section "Compilation de MG2D"
    
    if [ -d "$PATH_MG2D" ]; then
        cd "$PATH_MG2D"
        # Vérification de la présence du pom.xml
        if [ -f "pom.xml" ]; then
            log "Lancement de mvn install dans $PATH_MG2D"
            sudo -u "$REAL_USER" mvn install -DskipTests
            ok "MG2D installé dans le dépôt local."
        else
            die "Erreur : pom.xml introuvable dans $PATH_MG2D. Vérifiez l'emplacement exact de MG2D."
        fi
    else
        log "Dossier MG2D absent, vérifiez le clonage."
    fi
}

# ─── 4. Compilation de NodeBuster ─────────────────────────────────────────────
compile_nodebuster() {
    section "Compilation de NodeBuster"
    
    if [ -d "$PATH_NODEBUSTER" ]; then
        cd "$PATH_NODEBUSTER"
        if [ -f "pom.xml" ]; then
            log "Compilation dans $PATH_NODEBUSTER"
            sudo -u "$REAL_USER" mvn clean install -DskipTests
            ok "NodeBuster compilé avec succès."
        else
            die "Erreur : pom.xml introuvable dans $PATH_NODEBUSTER. Vérifiez le chemin."
        fi
    else
        die "Dossier $PATH_NODEBUSTER introuvable."
    fi
}

# ─── 5. Configuration du lancement ───────────────────────────────────────────
setup_launcher() {
    section "Configuration du lanceur"
    
    # Création du script de commande
    cat <<EOF > "$LOCAL_BIN/borne-arcade"
#!/bin/bash
cd "$PATH_NODEBUSTER"
mvn exec:java@borne
EOF
    chmod +x "$LOCAL_BIN/borne-arcade"
    ok "Commande 'borne-arcade' créée."
}

# ─── Main ────────────────────────────────────────────────────────────────────
main() {
    prepare_system
    setup_paths
    # compile_mg2d # Décommentez si MG2D doit être compilé à part
    compile_nodebuster
    setup_launcher
    
    if [ "$NO_RUN" = false ]; then
        section "Lancement de la borne"
        sudo -u "$REAL_USER" "$LOCAL_BIN/borne-arcade"
    fi
}

main