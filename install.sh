#!/bin/bash
# =============================================================================
# install.sh
# Script ENTIÈREMENT AUTOMATIQUE pour la borne d'arcade sur Raspberry Pi OS / Linux.
# - Vérifie et installe les dépendances système
# - Clone/met à jour MG2D, borne_arcade et les jeux
# - Compile MG2D et l'installe dans le dépôt Maven local
# - Compile borne_arcade
# - Configure le lancement automatique via X11
# - Lance la borne automatiquement
#
# Usage : sudo ./install.sh [--no-run]
#   --no-run : compile sans lancer automatiquement
# =============================================================================

set -e

# ─── Couleurs pour les logs ───────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log()  { echo -e "${CYAN}[INFO]${NC}  $*"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }
die()  { echo -e "${RED}[ERREUR]${NC} $*" >&2; exit 1; }
section() { echo -e "\n${BLUE}━━━ $* ━━━${NC}\n"; }

# ─── Configuration ───────────────────────────────────────────────────────────
NO_RUN=false
[ "$1" = "--no-run" ] && NO_RUN=true

# ─── Vérification root ───────────────────────────────────────────────────────
if [ "$EUID" -ne 0 ]; then
    die "Ce script doit être lancé avec des privilèges root : sudo $0"
fi

# L'utilisateur réel (celui qui a lancé sudo)
REAL_USER="${SUDO_USER:-$USER}"
REAL_HOME=$(getent passwd "$REAL_USER" | cut -d: -f6)
BASE="$REAL_HOME/git"

# Chemins importants
X11_AUTOSTART="/etc/X11/app-defaults"
DESKTOP_AUTOSTART="/etc/xdg/autostart"
LOCAL_BIN="/usr/local/bin"

# ─── 1. Vérification des dépendances requises ────────────────────────────────
check_dependencies() {
    section "Vérification des prérequis"
    
    local missing=()
    
    # Vérifie les commandes essentielles
    for cmd in git java javac mvn; do
        if ! command -v "$cmd" &> /dev/null; then
            missing+=("$cmd")
        fi
    done
    
    if [ ${#missing[@]} -gt 0 ]; then
        warn "Commandes manquantes : ${missing[*]}"
        return 1
    fi
    
    # Vérifie la version de Java
    java_version=$(java -version 2>&1 | grep -oP 'version "\K[0-9]+')
    if [ "$java_version" -lt 8 ]; then
        warn "Java 8+ recommandé, trouvé : Java $java_version"
    else
        ok "Java $java_version détecté ✓"
    fi
    
    ok "Dépendances vérifiées ✓"
}

# ─── 2. Installation/Mise à jour des paquets système ──────────────────────────
install_system_packages() {
    section "Mise à jour des paquets système"
    
    log "Mise à jour des listes de paquets..."
    apt-get update -qq
    
    log "Installation des dépendances système..."
    apt-get install -y \
        openjdk-17-jdk \
        git \
        maven \
        build-essential \
        libx11-dev \
        libxext-dev \
        x11-apps \
        x11-xserver-utils \
        libasound2-dev \
        pulseaudio \
        alsa-utils \
        alsa-base \
        libpulse-dev \
        love \
        libgl1-mesa-glx \
        libglu1-mesa \
        2>&1 | grep -v "already" | grep -v "is already" || true
    
    ok "Paquets système installés ✓"
}

# ─── 3. Création et configuration du répertoire de travail ────────────────────
setup_directories() {
    section "Configuration des répertoires"
    
    log "Création de $BASE..."
    sudo -u "$REAL_USER" mkdir -p "$BASE"
    
    # Crée les dossiers X11
    mkdir -p "$X11_AUTOSTART"
    mkdir -p "$DESKTOP_AUTOSTART"
    
    ok "Répertoires créés ✓"
}

# ─── 4. Clone / mise à jour de MG2D et compilation ───────────────────────────
setup_mg2d() {
    section "Configuration de MG2D"
    
    log "Récupération de MG2D..."
    
    if [ ! -d "$BASE/MG2D" ]; then
        log "Clonage de MG2D..."
        sudo -u "$REAL_USER" git clone \
            http://iut.univ-littoral.fr/gitlab/synave/MG2D.git \
            "$BASE/MG2D" 2>&1 | tail -n5 \
            || die "Impossible de cloner MG2D. Vérifiez votre réseau / VPN IUT."
        ok "MG2D cloné ✓"
    else
        log "Mise à jour de MG2D existant..."
        sudo -u "$REAL_USER" git -C "$BASE/MG2D" pull -q 2>&1 | tail -n3 || true
        ok "MG2D mis à jour ✓"
    fi
    
    log "Compilation et installation de MG2D..."
    sudo -u "$REAL_USER" bash -c "
        cd '$BASE/MG2D'
        mvn --batch-mode clean install -DskipTests -q 2>&1 | grep -E '(BUILD|ERROR)' || true
    " || die "La compilation de MG2D a échoué."
    
    ok "MG2D installé dans ~/.m2 ✓"
}

# ─── 5. Clone / mise à jour de borne_arcade ──────────────────────────────────
setup_borne_arcade() {
    section "Configuration de borne_arcade"
    
    log "Récupération de borne_arcade..."
    
    if [ ! -d "$BASE/borne_arcade" ]; then
        log "Clonage de borne_arcade..."
        sudo -u "$REAL_USER" git clone \
            https://github.com/Hippolyte-LePallec/borne-arcade-sae.git \
            "$BASE/borne_arcade" 2>&1 | tail -n5 \
            || die "Impossible de cloner borne_arcade. Vérifiez votre connexion internet."
        ok "borne_arcade cloné ✓"
    else
        log "Mise à jour de borne_arcade existant..."
        sudo -u "$REAL_USER" git -C "$BASE/borne_arcade" pull -q 2>&1 | tail -n3 || true
        ok "borne_arcade mis à jour ✓"
    fi
    
    log "Récupération avec les sous-modules et dépendances..."
    sudo -u "$REAL_USER" git -C "$BASE/borne_arcade" submodule update --init --recursive -q 2>/dev/null || true
}

# ─── 6. Compilation de borne_arcade ──────────────────────────────────────────
compile_borne_arcade() {
    section "Compilation de borne_arcade"
    
    log "Compilation en cours..."
    
    if [ -f "$BASE/borne_arcade/Script/compilation.sh" ]; then
        log "Utilisation du script de compilation fourni..."
        sudo -u "$REAL_USER" bash -c "
            cd '$BASE/borne_arcade'
            chmod +x Script/compilation.sh
            bash Script/compilation.sh 2>&1 | tail -n10
        " || die "Le script de compilation a échoué."
    else
        log "Utilisation de Maven directement..."
        sudo -u "$REAL_USER" bash -c "
            cd '$BASE/borne_arcade'
            mvn --batch-mode clean package -DskipTests -q 2>&1 | grep -E '(BUILD|ERROR)' || true
        " || die "La compilation Maven de borne_arcade a échoué."
    fi
    
    ok "borne_arcade compilé ✓"
}

# ─── 7. Configuration X11 et autostart ──────────────────────────────────────
setup_autostart() {
    section "Configuration du lancement automatique (X11)"
    
    local desktop_src="$BASE/borne_arcade/borne.desktop"
    local desktop_name="borne-arcade.desktop"
    
    # Cherche le JAR à lancer
    local jar=$(find "$BASE/borne_arcade/target" -maxdepth 1 -name "*.jar" \
          ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -n1)
    
    if [ -z "$jar" ]; then
        jar=$(find "$BASE/borne_arcade" -path "*/target/*.jar" \
              ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -n1)
    fi
    
    if [ -z "$jar" ]; then
        warn "Aucun JAR trouvé. Skipping autostart setup."
        return 0
    fi
    
    # Copie le fichier .desktop original s'il existe
    if [ -f "$desktop_src" ]; then
        log "Installation de borne.desktop depuis le projet..."
        cp "$desktop_src" "$DESKTOP_AUTOSTART/$desktop_name"
        chmod 644 "$DESKTOP_AUTOSTART/$desktop_name"
        ok "Desktop file copié dans $DESKTOP_AUTOSTART ✓"
    else
        log "Création d'un fichier .desktop personnalisé..."
    fi
    
    # Crée/met à jour le .desktop dans X11 avec le JAR trouvé
    log "Installation du lanceur dans X11..."
    cat > "$DESKTOP_AUTOSTART/$desktop_name" <<DESK
[Desktop Entry]
Type=Application
Name=Borne Arcade SAE
Comment=Jeux d'arcade pour la borne
Exec=sh -c 'DISPLAY=:0 java -jar $jar'
Icon=application-x-executable
Terminal=false
Categories=Game;
X-GNOME-Autostart-enabled=true
X-GNOME-Autostart-Delay=3
StartupNotify=true
DESK
    
    chmod 644 "$DESKTOP_AUTOSTART/$desktop_name"
    ok "Lanceur X11 configuré : $DESKTOP_AUTOSTART/$desktop_name ✓"
    
    # Crée également un wrapper shell dans /usr/local/bin pour plus de flexibilité
    log "Création d'un script de lancement dans $LOCAL_BIN..."
    cat > "$LOCAL_BIN/borne-arcade" <<'LAUNCHER'
#!/bin/bash
# Script wrapper pour lancer la borne arcade
set -e

# Alternative Java versions
JAR_PATH=""

# Essaie plusieurs chemins possibles
for path in \
    "$HOME/git/borne_arcade/target/"*.jar \
    "/opt/borne/borne.jar" \
    "/usr/local/share/borne/borne.jar"
do
    if [ -f "$path" ] && ! [[ "$path" == *"sources"* ]] && ! [[ "$path" == *"javadoc"* ]]; then
        JAR_PATH="$path"
        break
    fi
done

if [ -z "$JAR_PATH" ]; then
    echo "❌ Erreur : Aucun JAR trouvé pour la borne arcade."
    exit 1
fi

echo "🎮 Lancement de la borne arcade..."
echo "   JAR: $JAR_PATH"

# Force le fullscreen et la configuration audio
export SDL_VIDEODRIVER=x11
export JAVA_TOOL_OPTIONS="-Djava.awt.headless=false"

exec java -jar "$JAR_PATH"
LAUNCHER
    
    chmod +x "$LOCAL_BIN/borne-arcade"
    ok "Script de lancement créé : $LOCAL_BIN/borne-arcade ✓"
}

# ─── 8. Lancement de la borne ───────────────────────────────────────────────
launch_borne() {
    section "Lancement de la borne d'arcade"
    
    if [ "$NO_RUN" = true ]; then
        ok "Installation terminée. Lancement skippé (--no-run)."
        return 0
    fi
    
    # Détermine le JAR à lancer
    local jar
    if [ -f "$BASE/borne_arcade/Script/run.sh" ]; then
        log "Utilisation du script de lancement fourni..."
        sudo -u "$REAL_USER" bash -c "
            cd '$BASE/borne_arcade'
            export DISPLAY=:0
            bash Script/run.sh
        "
        return $?
    else
        jar=$(find "$BASE/borne_arcade/target" -maxdepth 1 -name "*.jar" \
              ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -n1)
        
        if [ -z "$jar" ]; then
            jar=$(find "$BASE/borne_arcade" -name "*.jar" \
                  ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -n1)
        fi
        
        if [ -z "$jar" ]; then
            die "Aucun JAR trouvé pour le lancement."
        fi
        
        log "Lancement du JAR : $jar"
        
        # Lance en tant qu'utilisateur normal avec l'affichage X
        local display="${DISPLAY:-:0}"
        
        ok "▶ Démarrage de la borne arcade..."
        sudo -u "$REAL_USER" bash -c "
            export DISPLAY='$display'
            export SDL_VIDEODRIVER=x11
            java -jar '$jar'
        "
    fi
}

# ─── 9. Aide et résumé ───────────────────────────────────────────────────────
print_summary() {
    section "Installation terminée ✓"
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Résumé de l'installation :"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    cat <<INFO
📁 Répertoires créés :
   - Base : $BASE
   - Borne : $BASE/borne_arcade
   - MG2D  : $BASE/MG2D

🎮 Lancement automatique configuré :
   - X11 Desktop entry : $DESKTOP_AUTOSTART/borne-arcade.desktop
   - Script wrapper    : $LOCAL_BIN/borne-arcade

🚀 Pour relancer la borne :
   # Via le script wrapper
   borne-arcade
   
   # Via Maven
   cd $BASE/borne_arcade && mvn exec:java@borne
   
   # Directement (JAR)
   java -jar $BASE/borne_arcade/target/*.jar

🔄 Pour mettre à jour et recompiler :
   sudo $0 --no-run
   sudo $0

📝 Logs : /var/log/borne-arcade.log (à configurer avec systemd)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
INFO
    
    ok "Borne arcade prête ! 🎊"
}

# ─── 10. Main - Exécution du script ──────────────────────────────────────────
main() {
    section "INSTALLATION DE LA BORNE ARCADE"
    
    # Vérifie les dépendances minimales
    if ! check_dependencies; then
        log "Installation des dépendances manquantes..."
        install_system_packages
    fi
    
    setup_directories
    setup_mg2d
    setup_borne_arcade
    compile_borne_arcade
    setup_autostart
    
    print_summary
    
    # Optionnel : lance la borne
    launch_borne
}

# Exécute main

main "$@"
