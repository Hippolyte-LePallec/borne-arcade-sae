import os
import time
from pathlib import Path
from api_client import OllamaWrapper

# ==============================
# CONFIGURATION
# ==============================
PROJECT_PATH = Path(r"C:\Users\cleme\Desktop\SAE\borne-arcade-sae")
DOCS_PATH = Path("docs")
DOCS_PATH.mkdir(exist_ok=True)
SUPPORTED_EXTENSIONS = {".java"}

MODEL_NAME = "gemma2:latest"
client = OllamaWrapper()

# ==============================
# ANALYSE ET GÉNÉRATION
# ==============================

def analyze_javadoc_with_ai(file_path: Path, content: str) -> str:
    prompt = f"""
Tu es un expert en qualité de code Java. Analyse le fichier suivant : {file_path.name}
Vérifie si :
1. Chaque classe et chaque méthode publique possède un commentaire Javadoc (/** ... */).
2. Les commentaires sont pertinents et expliquent le 'pourquoi' et non juste le 'quoi'.
3. Les paramètres (@param) et retours (@return) sont présents.

SI TOUT EST CORRECT : Réponds uniquement "RAS".
SI DES COMMENTAIRES MANQUENT OU SONT MAUVAIS : 
- Liste les méthodes concernées.
- Propose une version corrigée du Javadoc pour chaque méthode.

CODE :
{content}
"""
    try:
        result = client.generate_text(model=MODEL_NAME, prompt=prompt)
        return result.response
    except Exception as e:
        return f"Erreur IA : {e}"

def main():
    print("=" * 60)
    print("🚀 TEST : ANALYSE DES 5 PREMIERS FICHIERS")
    print("=" * 60)

    if not client.is_server_running():
        print("❌ Erreur : Le serveur Ollama n'est pas lancé.")
        return

    # Collecte des fichiers
    all_files = [p for p in PROJECT_PATH.rglob("*") if p.suffix in SUPPORTED_EXTENSIONS and "target" not in str(p)]
    
    if not all_files:
        print("❌ Aucun fichier Java trouvé.")
        return

    # --- MODIFICATION : ON LIMITE À 5 POUR LE TEST ---
    files_to_test = all_files[:5]
    print(f"🔍 {len(all_files)} fichiers trouvés au total.")
    print(f"🧪 Lancement du test sur les {len(files_to_test)} premiers fichiers...\n")

    report_content = [f"# Rapport de Test Javadoc\nCalculé sur les 5 premiers fichiers de : {PROJECT_PATH}\n"]
    issues_found = 0
    start_global_time = time.time()

    for idx, file_path in enumerate(files_to_test, 1):
        rel_path = file_path.relative_to(PROJECT_PATH)
        print(f"[{idx}/{len(files_to_test)}] Analyse de {rel_path}...", end=" ", flush=True)

        start_file_time = time.time() # Timer par fichier
        content = file_path.read_text(encoding="utf-8", errors="ignore")
        
        # Appel à l'IA
        analysis = analyze_javadoc_with_ai(file_path, content)
        duration = time.time() - start_file_time

        if "RAS" in analysis.upper() and len(analysis) < 20:
            print(f"✅ OK ({duration:.1f}s)")
        else:
            print(f"⚠️ Corrections trouvées ({duration:.1f}s)")
            issues_found += 1
            report_content.append(f"## 📄 Fichier : {rel_path}")
            report_content.append(f"**Temps d'analyse :** {duration:.1f}s\n")
            report_content.append(f"{analysis}\n")
            report_content.append("---\n")

    # Sauvegarde du rapport de test
    total_duration = time.time() - start_global_time
    report_file = DOCS_PATH / "TEST_RAPPORT_5_FILES.md"
    
    # On assemble le texte final
    final_text = "\n".join(report_content)
    final_text += f"\n\n**Temps total du test :** {total_duration/60:.1f} minutes."
    
    report_file.write_text(final_text, encoding="utf-8")

    print("\n" + "=" * 60)
    print(f"✅ TEST TERMINÉ en {total_duration:.1f}s")
    print(f"📄 Rapport généré : {report_file.resolve()}")
    print("=" * 60)

if __name__ == "__main__":
    main()