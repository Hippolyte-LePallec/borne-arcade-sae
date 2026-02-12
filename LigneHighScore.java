/**
 * Classe représentant une ligne de highscore (meilleur score) dans le système de classement.
 * 
 * <p>Chaque ligne contient :</p>
 * <ul>
 *   <li>Un nom de joueur (3 caractères maximum)</li>
 *   <li>Un score (entier positif)</li>
 * </ul>
 * 
 * <p>Cette classe est utilisée par {@link HighScore} pour gérer la liste des meilleurs scores
 * et permet la sérialisation/désérialisation depuis un fichier texte.</p>
 * 
 * <h2>Format de sérialisation</h2>
 * <p>Format texte : {@code NOM-SCORE}</p>
 * <p>Exemples :</p>
 * <pre>
 * MAX-15000
 * BOB-12500
 * SAM-10000
 * </pre>
 * 
 * <h2>Validation des données</h2>
 * <ul>
 *   <li><b>Nom</b> : Maximum 3 caractères. Si supérieur, remplacé par "AAA"</li>
 *   <li><b>Score</b> : Doit être positif. Si négatif, remplacé par 0</li>
 * </ul>
 * 
 * @see HighScore
 * @version 1.0
 */
class LigneHighScore {
    
    /**
     * Le nom du joueur (3 caractères maximum).
     * Valeur par défaut : "AAA"
     */
    private String nom;
    
    /**
     * Le score du joueur (entier positif).
     * Valeur par défaut : 0
     */
    private int score;
    
    /**
     * Constructeur par défaut créant une ligne de highscore vide.
     * Initialise le nom à "AAA" et le score à 0.
     */
    public LigneHighScore() {
        nom = "AAA";
        score = 0;
    }
    
    /**
     * Constructeur avec paramètres pour créer une ligne de highscore.
     * 
     * <p>Validation des paramètres :</p>
     * <ul>
     *   <li>Si le nom contient plus de 3 caractères, il est remplacé par "AAA"</li>
     *   <li>Si le score est négatif, il est remplacé par 0</li>
     * </ul>
     * 
     * @param nnom Le nom du joueur (3 caractères maximum recommandé)
     * @param sscore Le score du joueur (doit être positif)
     */
    public LigneHighScore(String nnom, int sscore) {
        if (nnom.length() > 3)
            nnom = "AAA";
        else
            nom = new String(nnom);
        if (sscore < 0)
            score = 0;
        else
            score = sscore;
    }
    
    /**
     * Constructeur par copie créant une ligne de highscore à partir d'une autre.
     * Effectue une copie profonde des attributs.
     * 
     * @param l La ligne de highscore à copier
     */
    public LigneHighScore(LigneHighScore l) {
        nom = new String(l.nom);
        score = l.score;
    }
    
    /**
     * Constructeur à partir d'une chaîne de caractères sérialisée.
     * Parse une chaîne au format "NOM-SCORE" pour créer l'objet.
     * 
     * <p><b>Format attendu :</b> {@code NOM-SCORE}</p>
     * <p><b>Exemples valides :</b></p>
     * <pre>
     * "MAX-15000" → nom="MAX", score=15000
     * "BOB-12500" → nom="BOB", score=12500
     * "A  -100"   → nom="A  ", score=100
     * </pre>
     * 
     * <p><b>Gestion des erreurs :</b></p>
     * <ul>
     *   <li>Si la chaîne ne contient pas exactement un séparateur '-', initialise à "AAA" et 0</li>
     *   <li>Si le score n'est pas un entier valide, lève une {@link NumberFormatException}</li>
     * </ul>
     * 
     * @param str La chaîne à parser au format "NOM-SCORE"
     * @throws NumberFormatException Si la partie score n'est pas un entier valide
     * 
     * @see #toString()
     */
    public LigneHighScore(String str) {
        String[] tab = str.split("-");
        if (tab.length != 2) {
            nom = "AAA";
            score = 0;
        } else {
            nom = new String(tab[0]);
            score = Integer.parseInt(tab[1]);
        }
    }
    
    /**
     * Retourne le score du joueur.
     * 
     * @return Le score (entier positif ou nul)
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Retourne le nom du joueur.
     * 
     * @return Le nom (3 caractères maximum)
     */
    public String getNom() {
        return nom;
    }
    
    /**
     * Retourne la représentation textuelle de la ligne de highscore.
     * Format utilisé pour la sérialisation dans les fichiers.
     * 
     * <p><b>Format de sortie :</b> {@code NOM-SCORE}</p>
     * 
     * <p><b>Exemples :</b></p>
     * <pre>
     * nom="MAX", score=15000  → "MAX-15000"
     * nom="BOB", score=12500  → "BOB-12500"
     * nom="A  ", score=100    → "A  -100"
     * </pre>
     * 
     * @return La chaîne au format "NOM-SCORE"
     * 
     * @see #LigneHighScore(String)
     */
    public String toString() {
        return nom + "-" + score;
    }
}