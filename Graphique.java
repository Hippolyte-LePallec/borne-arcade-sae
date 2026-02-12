import java.awt.Font;
import java.io.IOException;
import java.nio.file.*;
import javax.swing.*;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;

import MG2D.geometrie.*;
import MG2D.geometrie.Point;
import MG2D.audio.*;
import MG2D.*;
import MG2D.FenetrePleinEcran;

/**
 * Classe principale gérant l'interface graphique du menu de la borne d'arcade.
 *
 * <p>Cette classe orchestre l'ensemble de l'interface utilisateur et gère :</p>
 * <ul>
 *   <li>L'initialisation et l'affichage de la fenêtre plein écran</li>
 *   <li>Le chargement dynamique des jeux disponibles depuis le répertoire "projet/"</li>
 *   <li>L'affichage du menu de sélection avec les boutons de jeux</li>
 *   <li>Les boîtes d'information (image, description, highscores)</li>
 *   <li>La gestion du clavier et de la navigation</li>
 *   <li>La lecture de musiques de fond aléatoires</li>
 *   <li>L'écran de confirmation de sortie</li>
 * </ul>
 *
 * <p>L'interface est divisée en plusieurs zones :</p>
 * <ul>
 *   <li><b>Zone gauche (0-640px)</b> : Menu de sélection des jeux avec pointeur</li>
 *   <li><b>Zone droite haute (640-1280px, 0-512px)</b> : Description du jeu, contrôles et highscores</li>
 *   <li><b>Zone droite basse (640-1280px, 512-1024px)</b> : Aperçu image du jeu</li>
 * </ul>
 *
 * <h2>Résolution d'écran</h2>
 * <p>Résolution fixe : 1280x1024 pixels en plein écran</p>
 *
 * <h2>Polices utilisées</h2>
 * <ul>
 *   <li>PrStart.ttf (32pt) : Textes du menu et messages</li>
 *   <li>PrStart.ttf (48pt) : Texte sélectionné (optionnel)</li>
 * </ul>
 *
 * <h2>Structure des fichiers</h2>
 * <pre>
 * projet/
 *   ├── [nom_jeu_1]/
 *   │   ├── description.txt
 *   │   ├── bouton.txt
 *   │   ├── highscore
 *   │   └── photo_small.png
 *   └── [nom_jeu_2]/
 *       └── ...
 * fonts/
 *   └── PrStart.ttf
 * img/
 *   ├── fondretro3.png
 *   ├── bouton2.png
 *   ├── joystick2.png
 *   ├── ibouton2.png
 *   └── blancTransparent.png
 * sound/
 *   ├── bip.mp3
 *   └── bg/
 *       ├── musique1.mp3
 *       └── musique2.mp3
 * </pre>
 *
 * @see BoiteSelection
 * @see BoiteImage
 * @see BoiteDescription
 * @see Bouton
 * @see Pointeur
 * @see ClavierBorneArcade
 * @version 1.0
 */
public class Graphique {

    /**
     * Fenêtre principale en plein écran.
     * Fenêtre statique partagée pour l'affichage de tous les éléments graphiques.
     */
    private static final FenetrePleinEcran f = new FenetrePleinEcran("_Menu Borne D'arcade_");

    /**
     * Largeur de la fenêtre en pixels.
     * Valeur fixe : 1280 pixels
     */
    private int TAILLEX;

    /**
     * Hauteur de la fenêtre en pixels.
     * Valeur fixe : 1024 pixels
     */
    private int TAILLEY;

    /**
     * Gestionnaire du clavier de la borne d'arcade.
     * Capture les entrées des deux joueurs.
     */
    private ClavierBorneArcade clavier;

    /**
     * Boîte de sélection contenant le menu des jeux.
     * Gère la navigation et la sélection dans la liste des jeux.
     */
    private BoiteSelection bs;

    /**
     * Boîte d'image affichant l'aperçu du jeu sélectionné.
     * Affiche photo_small.png du jeu actuellement pointé.
     */
    private BoiteImage bi;

    /**
     * Boîte de description affichant les informations du jeu.
     * Contient la description, les contrôles et les highscores.
     */
    private BoiteDescription bd;

    /**
     * Tableau statique contenant tous les boutons de jeux disponibles.
     * Taille dynamique basée sur le nombre de sous-répertoires dans "projet/".
     * Partagé avec d'autres classes pour l'accès aux jeux.
     */
    public static Bouton[] tableau;

    /**
     * Pointeur visuel indiquant le jeu actuellement sélectionné.
     * Affiche un triangle de sélection et gère la valeur de sélection.
     */
    private Pointeur pointeur;

    /**
     * Police de caractères principale pour les textes du menu.
     * PrStart.ttf en taille 32pt.
     */
    Font font;

    /**
     * Police de caractères pour le texte sélectionné (optionnel).
     * PrStart.ttf en taille 48pt.
     */
    Font fontSelect;

    /**
     * Tableau indiquant quels textes sont actuellement affichés.
     * Utilisé pour l'optimisation de l'affichage et l'animation de clignotement.
     * {@code true} si le texte est affiché, {@code false} sinon.
     */
    public static boolean[] textesAffiches;

    /**
     * Musique de fond actuellement en lecture.
     * Choisie aléatoirement parmi les fichiers du répertoire "sound/bg/".
     */
    public static Bruitage musiqueFond;

    /**
     * Tableau contenant les noms de tous les fichiers musicaux disponibles.
     * Rempli dynamiquement depuis le répertoire "sound/bg/".
     */
    private static String[] tableauMusiques;

    /**
     * Nombre total de musiques de fond disponibles.
     * Calculé lors du scan du répertoire "sound/bg/".
     */
    private static int cptMus;

    /**
     * Constructeur de la classe Graphique.
     *
     * <p>Initialise l'ensemble de l'interface graphique du menu :</p>
     * <ol>
     *   <li>Configure la fenêtre plein écran (1280x1024)</li>
     *   <li>Charge les polices personnalisées (PrStart.ttf)</li>
     *   <li>Initialise le clavier et les listeners</li>
     *   <li>Scanne le répertoire "projet/" pour détecter les jeux disponibles</li>
     *   <li>Crée et remplit le tableau de boutons dynamiquement</li>
     *   <li>Initialise les boîtes (sélection, image, description)</li>
     *   <li>Charge et affiche tous les éléments graphiques :
     *     <ul>
     *       <li>Fond d'écran</li>
     *       <li>Boutons de jeux</li>
     *       <li>Pointeur de sélection</li>
     *       <li>Messages de description</li>
     *       <li>Icônes de contrôles (joystick et boutons)</li>
     *       <li>Highscores</li>
     *       <li>Lignes de séparation</li>
     *     </ul>
     *   </li>
     *   <li>Scanne le répertoire "sound/bg/" pour les musiques de fond</li>
     *   <li>Lance une musique de fond aléatoire</li>
     * </ol>
     *
     * @see Bouton#remplirBouton()
     * @see #lectureMusiqueFond()
     */
    public Graphique() {

        TAILLEX = 1280;
        TAILLEY = 1024;

        font = null;
        try {
            File in = new File("fonts/PrStart.ttf");
            font = font.createFont(Font.TRUETYPE_FONT, in);
            font = font.deriveFont(32.0f);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        f.setVisible(true);
        clavier = new ClavierBorneArcade();
        f.addKeyListener(clavier);
        f.getP().addKeyListener(clavier);

        /* Retrouver le nombre de jeux dispo */
        Path yourPath = FileSystems.getDefault().getPath("projet/");
        int cpt = 0;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(yourPath)) {
            for (Path path : directoryStream) {
                cpt++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        tableau = new Bouton[cpt];
        textesAffiches = new boolean[cpt];
        for (int i = 0; i < cpt; i++) {
            textesAffiches[i] = true;
        }

        Bouton.remplirBouton();
        pointeur = new Pointeur();
        bs = new BoiteSelection(new Rectangle(Couleur.GRIS_CLAIR, new Point(0, 0), new Point(640, TAILLEY), true), pointeur);
        bi = new BoiteImage(new Rectangle(Couleur.GRIS_FONCE, new Point(640, 512), new Point(TAILLEX, TAILLEY), true),
                new String(tableau[pointeur.getValue()].getChemin()));
        bd = new BoiteDescription(new Rectangle(Couleur.GRIS, new Point(640, 0), new Point(TAILLEX, 512), true));
        bd.lireFichier(tableau[pointeur.getValue()].getChemin());
        bd.lireHighScore(tableau[pointeur.getValue()].getChemin());

        Texture fond = new Texture("img/fondretro3.png", new Point(0, 0), TAILLEX, TAILLEY);
        f.ajouter(fond);
        // Ajout après fond car bug graphique sinon
        f.ajouter(bi.getImage());
        for (int i = 0; i < bd.getMessage().length; i++) {
            f.ajouter(bd.getMessage()[i]);
        }
        f.ajouter(pointeur.getTriangleGauche());
        f.ajouter(pointeur.getTriangleDroite());
        for (int i = 0; i < tableau.length; i++) {
            f.ajouter(tableau[i].getTexture());
        }
        f.ajouter(pointeur.getRectangleCentre());
        for (int i = 0; i < tableau.length; i++) {
            f.ajouter(tableau[i].getTexte());
            tableau[i].getTexte().setPolice(font);
            tableau[i].getTexte().setCouleur(Couleur.BLANC);
        }
        // Add texture
        for (int i = 0; i < bd.getBouton().length; i++) {
            f.ajouter(bd.getBouton()[i]);
        }
        f.ajouter(bd.getJoystick());
        // Add texte
        for (int i = 0; i < bd.gettBouton().length; i++) {
            f.ajouter(bd.gettBouton()[i]);
        }
        f.ajouter(bd.gettJoystick());
        f.ajouter(new Ligne(Couleur.NOIR, new Point(670, 360), new Point(1250, 360)));
        f.ajouter(new Ligne(Couleur.NOIR, new Point(670, 190), new Point(1250, 190)));
        f.ajouter(new Ligne(Couleur.NOIR, new Point(960, 210), new Point(960, 310)));
        f.ajouter(bd.getHighscore());
        for (int i = 0; i < bd.getListeHighScore().length; i++) {
            f.ajouter(bd.getListeHighScore()[i]);
        }

        /* Musique de fond */
        // Comptage du nombre de musiques disponibles
        Path cheminMusiques = FileSystems.getDefault().getPath("sound/bg/");
        cptMus = 0;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cheminMusiques)) {
            for (Path path : directoryStream) {
                cptMus++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Création d'un tableau de musiques
        tableauMusiques = new String[cptMus];
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cheminMusiques)) {
            int i = cptMus - 1;
            for (Path path : directoryStream) {
                tableauMusiques[i] = path.getFileName().toString();
                i--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Choix d'une musique aléatoire et lecture de celle-ci
        this.lectureMusiqueFond();
    }

    /**
     * Boucle principale de sélection et de navigation dans le menu des jeux.
     *
     * <p>Cette méthode gère :</p>
     * <ul>
     *   <li><b>Animation de clignotement</b> : Le texte du jeu sélectionné clignote toutes les 6 frames</li>
     *   <li><b>Navigation</b> : Déplacement haut/bas dans la liste avec mise à jour en temps réel de :
     *     <ul>
     *       <li>L'image d'aperçu</li>
     *       <li>La description du jeu</li>
     *       <li>Les contrôles (boutons et joystick)</li>
     *       <li>Les highscores</li>
     *     </ul>
     *   </li>
     *   <li><b>Lancement de jeu</b> : Validation de la sélection via le bouton Z</li>
     *   <li><b>Écran de confirmation de sortie</b> : Appui sur bouton A pour afficher un dialogue de confirmation
     *     <ul>
     *       <li>Navigation gauche/droite pour choisir OUI/NON</li>
     *       <li>Validation avec bouton A</li>
     *       <li>NON : retour au menu</li>
     *       <li>OUI : fermeture de l'application</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>Système d'animation :</b></p>
     * <ul>
     *   <li>Frame 0 : Suppression du texte sélectionné</li>
     *   <li>Frame 3 : Réaffichage du texte sélectionné</li>
     *   <li>Frame 6 : Réinitialisation du compteur</li>
     *   <li>Délai : 50ms entre chaque frame</li>
     * </ul>
     *
     * <p><b>Écran de confirmation :</b></p>
     * <ul>
     *   <li>Fond blanc semi-transparent</li>
     *   <li>Message : "Voulez vous vraiment quitter ?"</li>
     *   <li>Deux boutons : NON (rouge) et OUI (vert)</li>
     *   <li>Rectangle de sélection bleu indiquant le choix actuel</li>
     * </ul>
     *
     * <p><b>ATTENTION :</b> Cette méthode contient une boucle infinie et ne retourne jamais.</p>
     *
     * @see BoiteSelection#selection(ClavierBorneArcade)
     * @see Pointeur#lancerJeu(ClavierBorneArcade)
     * @see BoiteImage#setImage(String)
     * @see BoiteDescription#lireFichier(String)
     * @see BoiteDescription#lireHighScore(String)
     * @see BoiteDescription#lireBouton(String)
     */
    public void selectionJeu() {
        Texture fondBlancTransparent = new Texture("./img/blancTransparent.png", new Point(0, 0));
        Rectangle boutonNon = new Rectangle(Couleur.ROUGE, new Point(340, 600), 200, 100, true);
        Rectangle boutonOui = new Rectangle(Couleur.VERT, new Point(740, 600), 200, 100, true);
        Texte message = new Texte(Couleur.NOIR, "Voulez vous vraiment quitter ?", font, new Point(640, 800));
        Texte non = new Texte(Couleur.NOIR, "NON", font, new Point(440, 650));
        Texte oui = new Texte(Couleur.NOIR, "OUI", font, new Point(840, 650));
        Rectangle rectSelection = new Rectangle(Couleur.BLEU, new Point(330, 590), 220, 120, true);
        int frame = 0;
        boolean fermetureMenu = false;
        int selectionSur = 0;
        Texte textePrec = tableau[pointeur.getValue()].getTexte();

        while (true) {
            try {
                if (frame == 0) {
                    if (textesAffiches[pointeur.getValue()] == true) {
                        f.supprimer(tableau[pointeur.getValue()].getTexte());
                        textesAffiches[pointeur.getValue()] = false;
                    }
                }
                if (frame == 3) {
                    if (textesAffiches[pointeur.getValue()] == false) {
                        f.ajouter(tableau[pointeur.getValue()].getTexte());
                        textesAffiches[pointeur.getValue()] = true;
                    }
                }
                if (frame == 6) {
                    frame = -1;
                }
                frame++;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }

            if (!fermetureMenu) {
                if (bs.selection(clavier)) {
                    bi.setImage(tableau[pointeur.getValue()].getChemin());

                    fontSelect = null;
                    try {
                        File in = new File("fonts/PrStart.ttf");
                        fontSelect = fontSelect.createFont(Font.TRUETYPE_FONT, in);
                        fontSelect = fontSelect.deriveFont(48.0f);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }

                    tableau[pointeur.getValue()].getTexte().setPolice(font);

                    bd.lireFichier(tableau[pointeur.getValue()].getChemin());
                    bd.lireHighScore(tableau[pointeur.getValue()].getChemin());
                    bd.lireBouton(tableau[pointeur.getValue()].getChemin());

                    pointeur.lancerJeu(clavier);

                } else {
                    f.ajouter(fondBlancTransparent);
                    f.ajouter(message);
                    f.ajouter(rectSelection);
                    f.ajouter(boutonNon);
                    f.ajouter(boutonOui);
                    f.ajouter(non);
                    f.ajouter(oui);
                    fermetureMenu = true;
                }
            } else {
                if (clavier.getJoyJ1DroiteEnfoncee()) {
                    selectionSur = 1;
                }

                if (clavier.getJoyJ1GaucheEnfoncee()) {
                    selectionSur = 0;
                }

                if (selectionSur == 0) {
                    rectSelection.setA(new Point(330, 590));
                    rectSelection.setB(new Point(550, 710));
                } else {
                    rectSelection.setB(new Point(950, 710));
                    rectSelection.setA(new Point(730, 590));
                }

                if (clavier.getBoutonJ1ATape()) {
                    if (selectionSur == 0) {
                        f.supprimer(fondBlancTransparent);
                        f.supprimer(message);
                        f.supprimer(rectSelection);
                        f.supprimer(boutonNon);
                        f.supprimer(boutonOui);
                        f.supprimer(non);
                        f.supprimer(oui);
                        fermetureMenu = false;
                    } else {
                        System.exit(0);
                    }
                }
            }
            f.rafraichir();
        } // fin while true
    }

    /**
     * Lance la lecture d'une musique de fond choisie aléatoirement.
     *
     * <p>Cette méthode statique :</p>
     * <ul>
     *   <li>Sélectionne aléatoirement un fichier musical dans le tableau {@link #tableauMusiques}</li>
     *   <li>Crée un nouveau {@link Bruitage} à partir du fichier sélectionné</li>
     *   <li>Lance la lecture de la musique</li>
     * </ul>
     *
     * <p>Le fichier musical est cherché dans : "sound/bg/[nom_fichier]"</p>
     *
     * <p><b>Note :</b> Pour changer de musique, il faut d'abord appeler {@link #stopMusiqueFond()}
     * avant de rappeler cette méthode.</p>
     *
     * @see #tableauMusiques
     * @see #cptMus
     * @see #stopMusiqueFond()
     */
    public static void lectureMusiqueFond() {
        musiqueFond = new Bruitage("sound/bg/" + tableauMusiques[(int) (Math.random() * cptMus)]);
        musiqueFond.lecture();
    }

    /**
     * Arrête la lecture de la musique de fond en cours.
     *
     * <p>Appelle la méthode {@code arret()} sur l'objet {@link #musiqueFond} actuel.</p>
     *
     * @see #lectureMusiqueFond()
     */
    public static void stopMusiqueFond() {
        musiqueFond.arret();
    }

    /**
     * Affiche le texte d'un bouton de jeu spécifique dans la fenêtre.
     *
     * <p>Cette méthode statique permet d'ajouter dynamiquement le texte d'un jeu
     * à l'affichage, utilisée notamment lors de la navigation dans le menu.</p>
     *
     * @param valeur L'index du jeu dans le tableau {@link #tableau} dont on veut afficher le texte
     *
     * @see #tableau
     * @see #textesAffiches
     */
    public static void afficherTexte(int valeur) {
        f.ajouter(tableau[valeur].getTexte());
    }
}