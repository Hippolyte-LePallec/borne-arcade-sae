import os
import time
from pathlib import Path
from api_client import OllamaWrapper

# ==============================
# CONFIGURATION
# ==============================
BASE_DIR = Path(__file__).parent
PROJET_PATH = BASE_DIR / "projet"
DOCS_PATH = BASE_DIR / "doc_jeu"
DOCS_PATH.mkdir(exist_ok=True)

SUPPORTED_EXTENSIONS = {".java", ".py", ".lua"}
MODEL_NAME = "gemma2:latest"

client = OllamaWrapper()


# ==============================
# LECTURE DU PROJET DE JEU
# ==============================

def collect_game_files(game_dir: Path) -> dict[str, str]:
    """Collecte tous les fichiers source d'un dossier de jeu."""
    files = {}
    for file_path in game_dir.rglob("*"):
        if file_path.suffix in SUPPORTED_EXTENSIONS and "target" not in str(file_path):
            try:
                content = file_path.read_text(encoding="utf-8", errors="ignore")
                rel = file_path.relative_to(game_dir)
                files[str(rel)] = content
            except Exception:
                pass
    return files


def build_game_summary(game_name: str, files: dict[str, str]) -> str:
    """Construit un résumé du code source à envoyer à l'IA (limité pour éviter overflow)."""
    MAX_CHARS_PER_FILE = 2000
    MAX_TOTAL_CHARS = 12000

    parts = [f"Nom du jeu : {game_name}\n", f"Fichiers trouvés : {', '.join(files.keys())}\n\n"]
    total = sum(len(p) for p in parts)

    for rel_path, content in files.items():
        snippet = content[:MAX_CHARS_PER_FILE]
        block = f"--- {rel_path} ---\n{snippet}\n\n"
        if total + len(block) > MAX_TOTAL_CHARS:
            parts.append("... (autres fichiers tronqués pour la longueur) ...\n")
            break
        parts.append(block)
        total += len(block)

    return "".join(parts)


# ==============================
# GÉNÉRATION DE DOC VIA IA
# ==============================

def generate_user_doc(game_name: str, code_summary: str) -> str:
    prompt = f"""
Tu es un rédacteur technique expert en documentation de jeux vidéo.
À partir du code source fourni, génère une documentation utilisateur complète et bien structurée en Markdown.

La documentation DOIT contenir les sections suivantes (dans cet ordre) :

# 🎮 {game_name}

## 📖 Description
(Explique ce qu'est le jeu, son univers, son ambiance générale.)

## 🎯 Objectif
(Quel est le but du jeu ? Qu'est-ce que le joueur doit accomplir pour gagner ?)

## 🕹️ Comment jouer
(Explique les mécaniques de jeu : déplacements, actions, interactions.)

## ⌨️ Contrôles
(Liste les touches / boutons avec leur action, sous forme de tableau Markdown si possible.)

## 📜 Règles
(Les règles importantes : conditions de victoire, de défaite, limites du jeu.)

## 💡 Conseils
(2 ou 3 astuces utiles pour bien débuter.)

---
Sois clair, accessible pour un joueur non-développeur.
Utilise des emojis pour rendre la doc vivante et agréable à lire.
Rédige en français.

CODE SOURCE DU JEU :
{code_summary}
"""
    try:
        result = client.generate_text(model=MODEL_NAME, prompt=prompt)
        return result.response
    except Exception as e:
        return f"# ❌ Erreur lors de la génération\n\nErreur IA : {e}"


# ==============================
# PROGRAMME PRINCIPAL
# ==============================

def main():
    print("=" * 60)
    print("🎮 GÉNÉRATEUR DE DOC UTILISATEUR — PROJETS DE JEUX")
    print("=" * 60)

    # Vérification serveur Ollama
    if not client.is_server_running():
        print("❌ Erreur : Le serveur Ollama n'est pas lancé.")
        return

    # Vérification du dossier PROJET
    if not PROJET_PATH.exists() or not PROJET_PATH.is_dir():
        print(f"❌ Erreur : Le dossier '{PROJET_PATH.resolve()}' est introuvable.")
        print("   Crée un dossier 'PROJET' à côté de ce script et place tes jeux dedans.")
        return

    # Détection des sous-dossiers de jeux
    game_dirs = [d for d in PROJET_PATH.iterdir() if d.is_dir()]

    if not game_dirs:
        print(f"❌ Aucun sous-dossier de jeu trouvé dans '{PROJET_PATH}'.")
        return

    print(f"🔍 {len(game_dirs)} jeu(x) détecté(s) : {[d.name for d in game_dirs]}\n")

    generated = []
    failed = []
    start_global = time.time()

    for idx, game_dir in enumerate(game_dirs, 1):
        game_name = game_dir.name
        print(f"[{idx}/{len(game_dirs)}] 📂 Traitement de '{game_name}'...", flush=True)

        # Collecte des fichiers source
        files = collect_game_files(game_dir)

        if not files:
            print(f"   ⚠️  Aucun fichier .java/.py/.lua trouvé — jeu ignoré.\n")
            failed.append(game_name)
            continue

        print(f"   📄 {len(files)} fichier(s) source trouvé(s).")

        # Résumé du code à envoyer à l'IA
        code_summary = build_game_summary(game_name, files)

        # Génération de la doc
        print(f"   🤖 Génération de la documentation...", end=" ", flush=True)
        start_file = time.time()
        doc_content = generate_user_doc(game_name, code_summary)
        duration = time.time() - start_file
        print(f"✅ ({duration:.1f}s)")

        # Sauvegarde du fichier Markdown
        # Nom de fichier : jeux_doc_<NomDuJeu>.md
        safe_name = game_name.replace(" ", "_").replace("/", "-")
        output_file = DOCS_PATH / f"jeux_doc_{safe_name}.md"
        output_file.write_text(doc_content, encoding="utf-8")
        print(f"   💾 Sauvegardé : {output_file.resolve()}\n")
        generated.append((game_name, output_file))

    # ── Résumé final ──
    total_duration = time.time() - start_global
    print("=" * 60)
    print(f"✅ TERMINÉ en {total_duration:.1f}s ({total_duration/60:.1f} min)")
    print(f"📄 {len(generated)} doc(s) générée(s) dans '{DOCS_PATH.resolve()}'")
    if failed:
        print(f"⚠️  {len(failed)} jeu(x) ignoré(s) (pas de fichiers source) : {failed}")

    # ── Index récapitulatif ──
    if generated:
        index_lines = [
            "# 📚 Index des documentations de jeux\n",
            f"> Généré automatiquement — {len(generated)} jeu(x)\n",
            "",
        ]
        for game_name, path in generated:
            index_lines.append(f"- 🎮 [{game_name}]({path.name})")
        index_file = DOCS_PATH / "INDEX.md"
        index_file.write_text("\n".join(index_lines), encoding="utf-8")
        print(f"📑 Index créé : {index_file.resolve()}")

    print("=" * 60)


if __name__ == "__main__":
    main()