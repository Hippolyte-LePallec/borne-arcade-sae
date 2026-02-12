import MG2D.Couleur;
import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texte;
import MG2D.geometrie.Texture;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Classe représentant une boîte de description pour afficher des informations de jeu.
 * Cette classe gère l'affichage de messages, de contrôles (joystick et boutons),
 * et des meilleurs scores (highscores).
 * 
 * <p>Elle hérite de la classe {@link Boite} et utilise la bibliothèque MG2D pour
 * le rendu graphique des éléments.</p>
 * 
 * @author BENDAL (modifications)
 * @version 1.0
 */
public class BoiteDescription extends Boite {

    /** Tableau de textes pour afficher les messages de description (max 10 lignes) */
    private Texte[] message;
    
    /** Indicateur d'arrêt pour la lecture des fichiers */
    private boolean stop;
    
    /** Compteur du nombre de lignes lues */
    private int nombreLigne;
    
    /** Texture représentant le joystick */
    private Texture joystick;
    
    /** Tableau des textures des boutons (6 boutons) */
    private Texture[] bouton;
    
    /** Texte associé au joystick */
    private Texte tJoystick;
    
    /** Tableau des textes associés aux boutons */
    private Texte[] tBouton;
    
    /** Tableau contenant le texte des boutons et du joystick */
    private String[] texteBouton;
    
    /** Texte du titre "HIGHSCORE" */
    private Texte highscore;
    
    /** Liste des 10 meilleurs scores à afficher */
    private Texte[] listeHighScore;
    
    /** Police de caractères pour les textes de taille 15 */
    private Font font1 = null;
    
    /** Police de caractères pour les textes de taille 20 */
    private Font font2 = null;
    
    /** Police de caractères pour les textes de taille 25 */
    private Font font3 = null;
    
    /** Police de caractères pour les textes de taille 14 */
    private Font font4 = null;

    /**
     * Constructeur de la boîte de description.
     * Initialise tous les éléments graphiques (textures, textes, polices)
     * et positionne les différents composants à l'écran.
     * 
     * @param rectangle Le rectangle définissant la zone d'affichage de la boîte
     */
    BoiteDescription(Rectangle rectangle) {
        super(rectangle);
        
        // Chargement des polices personnalisées
        try {
            Font font = null;
            Font fontTexte = null;
            File in = new File("fonts/PrStart.ttf");
            font = font.createFont(Font.TRUETYPE_FONT, in);
            in = new File("fonts/Volter__28Goldfish_29.ttf");
            fontTexte = fontTexte.createFont(Font.TRUETYPE_FONT, in);
            font1 = fontTexte.deriveFont(15.0f);
            font2 = fontTexte.deriveFont(20.0f);
            font3 = font.deriveFont(25.0f);
            font4 = font.deriveFont(14.0f);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        bouton = new Texture[6];
        tBouton = new Texte[6];
        texteBouton = new String[7];
        
        // Déclaration des textures bouton + joystick
        this.joystick = new Texture("img/joystick2.png", new Point(740, 100), 40, 40);
        for (int i = 0; i < 3; i++) {
            this.bouton[i] = new Texture("img/ibouton2.png", new Point(890 + 130 * i, 130), 40, 40);
        }
        for (int i = 3; i < 6; i++) {
            this.bouton[i] = new Texture("img/ibouton2.png", new Point(890 + 130 * (i - 3), 50), 40, 40);
        }
        
        // Déclaration des textes bouton + joystick
        this.tJoystick = new Texte(Couleur.NOIR, "...", font1, new Point(760, 80));
        for (int i = 0; i < 3; i++) {
            this.tBouton[i] = new Texte(Couleur.NOIR, "...", font1, new Point(910 + 130 * i, 120));
        }
        for (int i = 3; i < 6; i++) {
            this.tBouton[i] = new Texte(Couleur.NOIR, "...", font1, new Point(910 + 130 * (i - 3), 40));
        }
        
        stop = false;
        message = new Texte[10];
        for (int i = 0; i < message.length; i++) {
            message[i] = new Texte(Couleur.NOIR, "", font2, new Point(960, 590));
            message[i].translater(0, -i * 30);
        }
        nombreLigne = 0;

        highscore = new Texte(Couleur.NOIR, "HIGHSCORE", font3, new Point(960, 335));
        listeHighScore = new Texte[10];
        for (int i = 0; i < 5; i++) {
            listeHighScore[i] = new Texte(Couleur.NOIR, "", font4, new Point(820, 310));
            listeHighScore[i].translater(0, -i * 25);
        }
        for (int i = 5; i < 10; i++) {
            listeHighScore[i] = new Texte(Couleur.NOIR, "", font4, new Point(1100, 310));
            listeHighScore[i].translater(0, -(i - 5) * 25);
        }
    }

    /**
     * Lit un fichier de description et charge son contenu dans les messages.
     * Le fichier doit se nommer "description.txt" et se trouver dans le répertoire spécifié.
     * Lit jusqu'à 10 lignes maximum.
     * 
     * @param path Le chemin du répertoire contenant le fichier description.txt
     */
    public void lireFichier(String path) {
        String fichier = path + "/description.txt";
        
        // Lecture du fichier texte
        try {
            InputStream ips = new FileInputStream(fichier);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String ligne;
            while (stop == false) {
                ligne = br.readLine();
                if (ligne != null) {
                    message[nombreLigne].setTexte(ligne);
                    setMessage(ligne, nombreLigne);
                } else {
                    message[nombreLigne].setTexte("");
                    setMessage("", nombreLigne);
                }
                nombreLigne++;
                if (nombreLigne >= 10) {
                    stop = true;
                    nombreLigne = 0;
                }
            }
            stop = false;
            br.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Lit et affiche les meilleurs scores depuis un fichier.
     * Le fichier doit se nommer "highscore" et se trouver dans le répertoire spécifié.
     * Affiche les 10 meilleurs scores en deux colonnes (5 scores par colonne).
     * 
     * @param path Le chemin du répertoire contenant le fichier highscore
     */
    public void lireHighScore(String path) {
        for (int i = 0; i < 10; i++) {
            if (i == 0)
                listeHighScore[i].setTexte("1er - ");
            else
                listeHighScore[i].setTexte((i + 1) + "eme - ");
        }
        
        String fichier = path + "/highscore";
        
        File f = new File(fichier);
        if (!f.exists()) {
            for (int i = 0; i < 10; i++)
                listeHighScore[i].setTexte("/");
        } else {
            ArrayList<LigneHighScore> liste = HighScore.lireFichier(fichier);
            for (int i = 0; i < liste.size(); i++) {
                if (i == 0)
                    listeHighScore[i].setTexte("1er : " + liste.get(i).getNom() + " - " + liste.get(i).getScore());
                else
                    listeHighScore[i].setTexte((i + 1) + "eme : " + liste.get(i).getNom() + " -  " + liste.get(i).getScore());
            }
        }
    }

    /**
     * Lit la configuration des boutons depuis un fichier.
     * Le fichier doit se nommer "bouton.txt" et contenir les labels séparés par des deux-points (:).
     * Format attendu : "labelJoystick:bouton1:bouton2:bouton3:bouton4:bouton5:bouton6"
     * 
     * @param path Le chemin du répertoire contenant le fichier bouton.txt
     */
    public void lireBouton(String path) {
        String fichier = path + "/bouton.txt";
        
        // Lecture du fichier texte
        try {
            InputStream ips = new FileInputStream(fichier);
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String ligne;
            ligne = br.readLine();
            if (ligne == null) {
                System.err.println("le fichier bouton est surement vide!");
            } else {
                texteBouton = ligne.split(":");
                // Changer le texte des boutons
                settJoystick(texteBouton[0]);
                for (int i = 0; i < 6; i++) {
                    settBouton(texteBouton[i + 1], i);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Retourne le tableau des messages affichés.
     * 
     * @return Un tableau de {@link Texte} contenant les messages
     */
    public Texte[] getMessage() {
        return message;
    }

    /**
     * Définit le texte d'un message à un index spécifique.
     * 
     * @param message Le texte du message à définir
     * @param a L'index du message dans le tableau (0-9)
     */
    public void setMessage(String message, int a) {
        this.message[a].setTexte(message);
    }

    /**
     * Retourne le tableau des textures des boutons.
     * 
     * @return Un tableau de {@link Texture} contenant les 6 boutons
     */
    public Texture[] getBouton() {
        return this.bouton;
    }

    /**
     * Retourne la texture du joystick.
     * 
     * @return La {@link Texture} du joystick
     */
    public Texture getJoystick() {
        return this.joystick;
    }

    /**
     * Retourne le tableau des textes des boutons.
     * 
     * @return Un tableau de {@link Texte} contenant les labels des 6 boutons
     */
    public Texte[] gettBouton() {
        return this.tBouton;
    }

    /**
     * Retourne le texte du joystick.
     * 
     * @return Le {@link Texte} associé au joystick
     */
    public Texte gettJoystick() {
        return this.tJoystick;
    }

    /**
     * Retourne le texte du titre "HIGHSCORE".
     * 
     * @return Le {@link Texte} du titre des meilleurs scores
     */
    public Texte getHighscore() {
        return this.highscore;
    }

    /**
     * Retourne la liste des meilleurs scores.
     * 
     * @return Un tableau de {@link Texte} contenant les 10 meilleurs scores
     */
    public Texte[] getListeHighScore() {
        return this.listeHighScore;
    }

    /**
     * Définit le texte associé au joystick.
     * 
     * @param s Le texte à afficher pour le joystick
     */
    public void settJoystick(String s) {
        this.tJoystick.setTexte(s);
    }

    /**
     * Définit le texte d'un bouton spécifique.
     * 
     * @param s Le texte à afficher pour le bouton
     * @param a L'index du bouton (0-5)
     */
    public void settBouton(String s, int a) {
        this.tBouton[a].setTexte(s);
    }
}