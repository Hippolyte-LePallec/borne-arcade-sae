import MG2D.*;
import MG2D.geometrie.*;
import java.io.File;
import java.awt.Font;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import MG2D.Couleur;

/**
 * Classe utilitaire pour la gestion des meilleurs scores (highscores) dans les jeux.
 * 
 * <p>Cette classe fournit des fonctionnalités pour :</p>
 * <ul>
 *   <li>Afficher un écran de saisie du nom du joueur (3 caractères)</li>
 *   <li>Lire et écrire les highscores depuis/vers un fichier</li>
 *   <li>Insérer automatiquement un nouveau score à la position appropriée</li>
 *   <li>Gérer une liste des 10 meilleurs scores</li>
 *   <li>Naviguer dans l'alphabet pour la saisie du nom</li>
 * </ul>
 * 
 * <h2>Format du fichier highscore</h2>
 * <p>Chaque ligne du fichier représente un score au format :</p>
 * <pre>
 * NOM:SCORE
 * 
 * Exemple :
 * MAX:15000
 * BOB:12500
 * SAM:10000
 * </pre>
 * 
 * <h2>Interface de saisie</h2>
 * <p>L'écran de saisie permet au joueur de :</p>
 * <ul>
 *   <li><b>Naviguer</b> : Joystick gauche/droite entre les 3 caractères + bouton de validation</li>
 *   <li><b>Changer de lettre</b> : Joystick haut/bas pour parcourir l'alphabet (A-Z, ., espace)</li>
 *   <li><b>Valider</b> : Bouton A sur le caractère '#' pour enregistrer</li>
 * </ul>
 * 
 * <p>L'interface affiche également :</p>
 * <ul>
 *   <li>Le score atteint par le joueur</li>
 *   <li>Sa position dans le classement (1er, 2ème, etc.)</li>
 *   <li>Le score précédent et suivant dans le classement (contexte)</li>
 * </ul>
 * 
 * <h2>Ordre des caractères</h2>
 * <p>Navigation cyclique : A → B → ... → Z → . → (espace) → A</p>
 * 
 * @see LigneHighScore
 * @version 1.0
 */
class HighScore {

    /**
     * Retourne le caractère suivant dans la séquence de saisie.
     * 
     * <p>Séquence de navigation circulaire :</p>
     * <ul>
     *   <li>A à Y : retourne la lettre suivante (A→B, B→C, etc.)</li>
     *   <li>Z : retourne '.' (point)</li>
     *   <li>'.' : retourne ' ' (espace)</li>
     *   <li>' ' : retourne 'A' (retour au début)</li>
     * </ul>
     * 
     * @param c Le caractère actuel
     * @return Le caractère suivant dans la séquence
     */
    public static char suivant(char c) {
        if (c >= 'A' && c < 'Z')
            return (char) (c + 1);
        if (c == 'Z')
            return '.';
        if (c == '.')
            return ' ';
        return 'A';
    }

    /**
     * Retourne le caractère précédent dans la séquence de saisie.
     * 
     * <p>Séquence de navigation circulaire inverse :</p>
     * <ul>
     *   <li>B à Z : retourne la lettre précédente (Z→Y, Y→X, etc.)</li>
     *   <li>A : retourne ' ' (espace)</li>
     *   <li>' ' : retourne '.' (point)</li>
     *   <li>'.' : retourne 'Z' (fin de l'alphabet)</li>
     * </ul>
     * 
     * @param c Le caractère actuel
     * @return Le caractère précédent dans la séquence
     */
    public static char precedent(char c) {
        if (c > 'A' && c <= 'Z')
            return (char) (c - 1);
        if (c == 'A')
            return ' ';
        if (c == ' ')
            return '.';
        return 'Z';
    }

    /**
     * Affiche l'écran de saisie du nom pour enregistrer un nouveau highscore.
     * 
     * <p>Cette méthode :</p>
     * <ol>
     *   <li>Charge les highscores existants depuis le fichier</li>
     *   <li>Détermine la position du nouveau score dans le classement</li>
     *   <li>Si le score est dans le top 10 :
     *     <ul>
     *       <li>Affiche un écran interactif de saisie du nom (3 caractères)</li>
     *       <li>Permet la navigation avec le joystick et la sélection des lettres</li>
     *       <li>Affiche le contexte (scores précédent et suivant)</li>
     *       <li>Enregistre le nouveau score dans le fichier</li>
     *     </ul>
     *   </li>
     *   <li>Si le score n'est pas dans le top 10 : quitte immédiatement</li>
     *   <li>Ferme l'application après validation</li>
     * </ol>
     * 
     * <h3>Contrôles</h3>
     * <table border="1" cellpadding="5">
     *   <tr><th>Action</th><th>Touche</th><th>Description</th></tr>
     *   <tr><td>Caractère suivant (droite)</td><td>Joystick droite</td><td>Passe au caractère suivant (max 4 positions : 3 lettres + validation)</td></tr>
     *   <tr><td>Caractère précédent (gauche)</td><td>Joystick gauche</td><td>Revient au caractère précédent (min position 0)</td></tr>
     *   <tr><td>Lettre suivante</td><td>Joystick haut</td><td>Passe à la lettre suivante (A→B→...→Z→.→espace→A)</td></tr>
     *   <tr><td>Lettre précédente</td><td>Joystick bas</td><td>Passe à la lettre précédente (inverse)</td></tr>
     *   <tr><td>Valider</td><td>Bouton A</td><td>Enregistre le nom (uniquement sur le caractère '#')</td></tr>
     * </table>
     * 
     * <h3>Affichage</h3>
     * <ul>
     *   <li><b>Titre</b> : "HIGHSCORE" en haut de l'écran</li>
     *   <li><b>Instruction</b> : "Enter Your name"</li>
     *   <li><b>Position actuelle</b> : Affiche "1er", "2ème", "3ème", etc.</li>
     *   <li><b>Score atteint</b> : Le score du joueur</li>
     *   <li><b>Nom</b> : 3 caractères éditables + caractère '#' pour validation</li>
     *   <li><b>Contexte</b> : Score précédent et suivant dans le classement (si disponibles)</li>
     *   <li><b>Triangle de sélection</b> : Indique le caractère actuellement édité</li>
     *   <li><b>Rectangles</b> : Encadrent chaque caractère éditable</li>
     * </ul>
     * 
     * <h3>Positionnement des éléments</h3>
     * <ul>
     *   <li>Caractères du nom : Positions X = 690, 840, 990 pixels</li>
     *   <li>Caractère de validation '#' : Position X = 1140 pixels</li>
     *   <li>Espacement horizontal : 150 pixels entre chaque caractère</li>
     *   <li>Ligne du score actuel : Y = 400 pixels</li>
     *   <li>Ligne du score précédent : Y = 580 pixels</li>
     *   <li>Ligne du score suivant : Y = 170 pixels</li>
     * </ul>
     * 
     * @param f La fenêtre dans laquelle afficher l'interface
     * @param clavier Le clavier de la borne d'arcade pour capturer les entrées
     * @param t Texture optionnelle de fond (peut être {@code null})
     * @param s Le score atteint par le joueur
     * @param fichierHighScore Le chemin du fichier de sauvegarde des highscores
     * 
     * @see #lireFichier(String)
     * @see #enregistrerFichier(String, ArrayList, String, int)
     * @see #suivant(char)
     * @see #precedent(char)
     */
    public static void demanderEnregistrerNom(Fenetre f, ClavierBorneArcade clavier, Texture t, int s, String fichierHighScore) {

        ArrayList<LigneHighScore> list = lireFichier(fichierHighScore);
        for (LigneHighScore l : list)
            System.out.println(l);

        int position = 0;
        boolean fin = false;
        while (!fin) {
            if (position == list.size())
                fin = true;
            else if (s <= list.get(position).getScore())
                position++;
            else {
                fin = true;
            }
        }

        if (position >= 10)
            System.exit(0);

        String score = s + "";

        char cprec[] = {' ', ' ', ' '};
        char c[] = {'A', ' ', ' ', '#'};
        char csuiv[] = {' ', ' ', ' '};
        int indexSelec = 0;

        Font font;
        font = null;
        try {
            File in = new File("/home/pi/git/borne_arcade/fonts/PrStart.ttf");
            font = font.createFont(Font.TRUETYPE_FONT, in);
            font = font.deriveFont(40.0f);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        Texte highscore = new Texte(Couleur.NOIR, "H  I  G  H  S  C  O  R  E", font, new Point(640, 950));
        Texte scoreAtteint = new Texte(Couleur.NOIR, score, font, new Point(420, 400));
        Texte enterYourName = new Texte(Couleur.NOIR, "E n t e r   Y o u r   n a m e", font, new Point(640, 800));
        Texte posNum = new Texte(Couleur.NOIR, (position + 1) + "eme", font, new Point(120, 400));

        if (position == 0)
            posNum.setTexte("1er");

        Texte posNumPrec = new Texte(Couleur.NOIR, "", font, new Point(120, 580));
        Texte posNumSuiv = new Texte(Couleur.NOIR, "", font, new Point(120, 170));

        Texte caracteres[] = new Texte[4];
        caracteres[0] = new Texte(Couleur.NOIR, c[0] + "", font, new Point(690, 400));
        caracteres[1] = new Texte(Couleur.NOIR, c[1] + "", font, new Point(840, 400));
        caracteres[2] = new Texte(Couleur.NOIR, c[2] + "", font, new Point(990, 400));
        caracteres[3] = new Texte(Couleur.NOIR, c[3] + "", font, new Point(1140, 400));
        
        Texte caracteresPrec[] = new Texte[3];
        caracteresPrec[0] = new Texte(Couleur.NOIR, cprec[0] + "", font, new Point(690, 580));
        caracteresPrec[1] = new Texte(Couleur.NOIR, cprec[1] + "", font, new Point(840, 580));
        caracteresPrec[2] = new Texte(Couleur.NOIR, cprec[2] + "", font, new Point(990, 580));
        Texte scorePrec = new Texte(Couleur.NOIR, "", font, new Point(420, 580));
        
        Texte caracteresSuiv[] = new Texte[3];
        caracteresSuiv[0] = new Texte(Couleur.NOIR, csuiv[0] + "", font, new Point(690, 170));
        caracteresSuiv[1] = new Texte(Couleur.NOIR, csuiv[1] + "", font, new Point(840, 170));
        caracteresSuiv[2] = new Texte(Couleur.NOIR, csuiv[2] + "", font, new Point(990, 170));
        Texte scoreSuiv = new Texte(Couleur.NOIR, "", font, new Point(420, 170));

        Rectangle rect1 = new Rectangle(Couleur.NOIR, new Point(650, 350), new Point(720, 480), false);
        Rectangle rect2 = new Rectangle(Couleur.NOIR, new Point(800, 350), new Point(870, 480), false);
        Rectangle rect3 = new Rectangle(Couleur.NOIR, new Point(950, 350), new Point(1020, 480), false);
        Rectangle rect4 = new Rectangle(Couleur.NOIR, new Point(1100, 350), new Point(1170, 480), false);

        Triangle select = new Triangle(Couleur.NOIR, new Point(690, 340), new Point(670, 300), new Point(710, 300), true);

        Texture blancTrans = new Texture("img/blancTransparent.png", new Point(0, 0));

        if (t != null)
            f.ajouter(t);

        f.ajouter(blancTrans);
        f.ajouter(highscore);
        f.ajouter(scoreAtteint);
        f.ajouter(scorePrec);
        f.ajouter(scoreSuiv);
        f.ajouter(enterYourName);
        f.ajouter(caracteres[0]);
        f.ajouter(caracteres[1]);
        f.ajouter(caracteres[2]);
        f.ajouter(caracteres[3]);
        f.ajouter(caracteresPrec[0]);
        f.ajouter(caracteresPrec[1]);
        f.ajouter(caracteresPrec[2]);
        f.ajouter(caracteresSuiv[0]);
        f.ajouter(caracteresSuiv[1]);
        f.ajouter(caracteresSuiv[2]);
        f.ajouter(posNum);
        f.ajouter(posNumPrec);
        f.ajouter(posNumSuiv);
        f.ajouter(rect1);
        f.ajouter(rect2);
        f.ajouter(rect3);
        f.ajouter(rect4);
        f.ajouter(select);

        if (position != 0) {
            caracteresPrec[0].setTexte(list.get(position - 1).getNom().charAt(0) + "");
            caracteresPrec[1].setTexte(list.get(position - 1).getNom().charAt(1) + "");
            caracteresPrec[2].setTexte(list.get(position - 1).getNom().charAt(2) + "");
            scorePrec.setTexte(list.get(position - 1).getScore() + "");
            if (position == 1)
                posNumPrec.setTexte("1er");
            else
                posNumPrec.setTexte(position + "eme");
        }
        
        if (position != list.size()) {
            caracteresSuiv[0].setTexte(list.get(position).getNom().charAt(0) + "");
            caracteresSuiv[1].setTexte(list.get(position).getNom().charAt(1) + "");
            caracteresSuiv[2].setTexte(list.get(position).getNom().charAt(2) + "");
            scoreSuiv.setTexte(list.get(position).getScore() + "");
            posNumSuiv.setTexte((position + 2) + "eme");
        }

        fin = false;

        while (!fin) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }

            if (clavier.getJoyJ1DroiteTape()) {
                if (indexSelec < 3) {
                    indexSelec++;
                    select.translater(150, 0);
                }
            }

            if (clavier.getJoyJ1GaucheTape()) {
                if (indexSelec > 0) {
                    indexSelec--;
                    select.translater(-150, 0);
                }
            }

            if (clavier.getJoyJ1HautTape()) {
                if (indexSelec != 3) {
                    c[indexSelec] = suivant(c[indexSelec]);
                    caracteres[indexSelec].setTexte(c[indexSelec] + "");
                }
            }

            if (clavier.getJoyJ1BasTape()) {
                if (indexSelec != 3) {
                    c[indexSelec] = precedent(c[indexSelec]);
                    caracteres[indexSelec].setTexte(c[indexSelec] + "");
                }
            }

            if (clavier.getBoutonJ1ATape() && indexSelec == 3)
                fin = true;

            f.rafraichir();
        }

        enregistrerFichier(fichierHighScore, list, "" + c[0] + c[1] + c[2], s);

        System.exit(0);
    }

    /**
     * Lit le fichier de highscores et retourne la liste des scores.
     * 
     * <p>Cette méthode :</p>
     * <ul>
     *   <li>Ouvre le fichier spécifié</li>
     *   <li>Lit chaque ligne et crée un objet {@link LigneHighScore}</li>
     *   <li>Retourne une {@link ArrayList} contenant tous les scores</li>
     *   <li>En cas d'erreur (fichier inexistant, etc.), retourne une liste vide</li>
     * </ul>
     * 
     * <p><b>Format du fichier :</b> Chaque ligne doit être au format "NOM:SCORE"</p>
     * 
     * <p><b>Exemple de fichier :</b></p>
     * <pre>
     * MAX:15000
     * BOB:12500
     * SAM:10000
     * </pre>
     * 
     * @param fichier Le chemin du fichier de highscores à lire
     * @return Une liste de {@link LigneHighScore} contenant tous les scores du fichier,
     *         ou une liste vide si le fichier n'existe pas ou est illisible
     * 
     * @see LigneHighScore
     * @see #enregistrerFichier(String, ArrayList, String, int)
     */
    public static ArrayList<LigneHighScore> lireFichier(String fichier) {
        ArrayList<LigneHighScore> l = new ArrayList<LigneHighScore>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fichier));
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                l.add(new LigneHighScore(currentLine));
            }
            reader.close();
        } catch (Exception e) {
        }

        return l;
    }

    /**
     * Enregistre la liste des highscores dans un fichier après insertion d'un nouveau score.
     * 
     * <p>Cette méthode :</p>
     * <ol>
     *   <li>Détermine la position du nouveau score dans le classement (tri décroissant)</li>
     *   <li>Insère le nouveau score à la position appropriée</li>
     *   <li>Supprime les scores au-delà de la 10ème position (garde uniquement le top 10)</li>
     *   <li>Écrit la liste mise à jour dans le fichier</li>
     * </ol>
     * 
     * <p><b>Règles d'insertion :</b></p>
     * <ul>
     *   <li>Les scores sont triés par ordre décroissant (meilleur score en premier)</li>
     *   <li>Si deux scores sont égaux, le plus ancien reste devant</li>
     *   <li>Seuls les 10 meilleurs scores sont conservés</li>
     * </ul>
     * 
     * <p><b>Format de sauvegarde :</b> Chaque ligne au format "NOM:SCORE"</p>
     * 
     * <p><b>Exemple :</b></p>
     * <pre>
     * Si la liste contient : [MAX:15000, BOB:12500, SAM:10000]
     * Et qu'on insère TOM:13000
     * Résultat : [MAX:15000, TOM:13000, BOB:12500, SAM:10000]
     * </pre>
     * 
     * @param fichier Le chemin du fichier de highscores à modifier
     * @param list La liste actuelle des highscores (sera modifiée)
     * @param nom Le nom du joueur (3 caractères)
     * @param score Le score atteint par le joueur
     * 
     * @see LigneHighScore
     * @see #lireFichier(String)
     */
    public static void enregistrerFichier(String fichier, ArrayList<LigneHighScore> list, String nom, int score) {
        int position = 0;
        boolean fin = false;
        while (!fin) {
            if (position == list.size())
                fin = true;
            else if (score <= list.get(position).getScore())
                position++;
            else {
                fin = true;
            }
        }

        list.add(position, new LigneHighScore(nom, score));
        while (list.size() > 10)
            list.remove(list.size() - 1);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fichier));
            for (int i = 0; i < list.size(); i++) {
                writer.write(list.get(i).toString());
                if (i != (list.size() - 1))
                    writer.write("\n");
            }
            writer.close();
        } catch (Exception e) {
        }
    }
}