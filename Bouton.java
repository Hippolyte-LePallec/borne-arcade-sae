import java.awt.Font;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.File;
import MG2D.Couleur;
import MG2D.geometrie.Point;
import MG2D.geometrie.Texture;
import MG2D.geometrie.Texte;

/**
 * Classe représentant un bouton de menu pour la sélection de jeux.
 * Chaque bouton correspond à un jeu disponible et contient les informations
 * nécessaires pour son affichage et sa sélection.
 *
 * <p>Un bouton est composé de :</p>
 * <ul>
 *   <li>Un texte affichant le nom du jeu</li>
 *   <li>Une texture de fond pour le bouton</li>
 *   <li>Le chemin d'accès vers le répertoire du jeu</li>
 *   <li>Le nom du jeu</li>
 *   <li>Un numéro identifiant unique</li>
 * </ul>
 *
 * <p>Cette classe permet également de scanner automatiquement le répertoire "projet/"
 * pour créer dynamiquement tous les boutons correspondant aux jeux disponibles.</p>
 *
 * @see MG2D.geometrie.Texte
 * @see MG2D.geometrie.Texture
 * @version 1.0
 */
public class Bouton {

    /**
     * Le texte affiché sur le bouton, contenant le nom du jeu.
     */
    private Texte texte;

    /**
     * Le chemin d'accès complet vers le répertoire du jeu.
     * Format : "projet/[nom_du_jeu]"
     */
    private String chemin;

    /**
     * Le nom du jeu (nom du répertoire).
     */
    private String nom;

    /**
     * La texture de fond du bouton (image de bouton).
     */
    private Texture texture;

    /**
     * Le numéro unique identifiant ce jeu/bouton dans le tableau.
     */
    private int numeroDeJeu;

    /**
     * Constructeur par défaut créant un bouton vide.
     * Initialise tous les attributs à {@code null}.
     * Utilisé principalement par la méthode {@link #remplirBouton()} avant de remplir
     * le tableau avec les données réelles.
     */
    public Bouton() {
        this.texte = null;
        this.texture = null;
        this.chemin = null;
        this.nom = null;
    }

    /**
     * Constructeur avec paramètres pour créer un bouton complet.
     *
     * @param texte Le texte à afficher sur le bouton
     * @param texture La texture de fond du bouton
     * @param chemin Le chemin d'accès vers le répertoire du jeu
     * @param nom Le nom du jeu
     */
    public Bouton(Texte texte, Texture texture, String chemin, String nom) {
        this.texte = texte;
        this.texture = texture;
        this.chemin = chemin;
        this.nom = nom;
    }

    /**
     * Méthode statique qui scanne le répertoire "projet/" et crée automatiquement
     * tous les boutons correspondant aux jeux disponibles.
     *
     * <p>Cette méthode :</p>
     * <ul>
     *   <li>Initialise le tableau {@link Graphique#tableau} avec des boutons vides</li>
     *   <li>Parcourt tous les sous-répertoires du dossier "projet/"</li>
     *   <li>Pour chaque jeu trouvé, crée un bouton avec :
     *     <ul>
     *       <li>Un texte noir en police Calibri taille 30</li>
     *       <li>Une texture de bouton (img/bouton2.png) de dimensions 400x65 pixels</li>
     *       <li>Le chemin complet vers le jeu</li>
     *       <li>Le nom du jeu (nom du répertoire)</li>
     *       <li>Un numéro de jeu unique</li>
     *     </ul>
     *   </li>
     *   <li>Positionne les boutons verticalement avec un espacement de 110 pixels</li>
     * </ul>
     *
     * <p>Les boutons sont remplis dans l'ordre inverse (du dernier au premier index)
     * pour que le premier jeu trouvé soit en haut de la liste.</p>
     *
     * <p><b>Position de base :</b> Point(310, 510) pour le texte, Point(100, 478) pour la texture</p>
     * <p><b>Espacement vertical :</b> 110 pixels entre chaque bouton</p>
     *
     * @see Graphique#tableau
     * @see DirectoryStream
     * @see Files#newDirectoryStream(Path)
     */
    public static void remplirBouton() {
        for (int i = 0; i < Graphique.tableau.length; i++) {
            Graphique.tableau[i] = new Bouton();
        }
        Path yourPath = FileSystems.getDefault().getPath("projet/");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(yourPath)) {
            int i = Graphique.tableau.length - 1;
            for (Path path : directoryStream) {
                Graphique.tableau[i].setTexte(new Texte(Couleur.NOIR, path.getFileName().toString(),
                        new Font("Calibri", Font.TYPE1_FONT, 30), new Point(310, 510)));
                Graphique.tableau[i].setTexture(new Texture("img/bouton2.png", new Point(100, 478), 400, 65));
                for (int j = 0; j < Graphique.tableau.length - (i + 1); j++) {
                    Graphique.tableau[i].getTexte().translater(0, -110);
                    Graphique.tableau[i].getTexture().translater(0, -110);
                }
                Graphique.tableau[i].setChemin("projet/" + path.getFileName().toString());
                Graphique.tableau[i].setNom(path.getFileName().toString());
                Graphique.tableau[i].setNumeroDeJeu(i);
                i--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retourne le chemin d'accès complet vers le répertoire du jeu.
     *
     * @return Le chemin du jeu au format "projet/[nom_du_jeu]"
     */
    public String getChemin() {
        return chemin;
    }

    /**
     * Définit le chemin d'accès vers le répertoire du jeu.
     *
     * @param chemin Le chemin d'accès complet au format "projet/[nom_du_jeu]"
     */
    public void setChemin(String chemin) {
        this.chemin = chemin;
    }

    /**
     * Retourne le nom du jeu.
     *
     * @return Le nom du jeu (nom du répertoire)
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom du jeu.
     *
     * @param nom Le nom du jeu à définir
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne le texte affiché sur le bouton.
     *
     * @return Le {@link Texte} du bouton
     */
    public Texte getTexte() {
        return texte;
    }

    /**
     * Définit le texte à afficher sur le bouton.
     *
     * @param texte Le {@link Texte} à afficher
     */
    public void setTexte(Texte texte) {
        this.texte = texte;
    }

    /**
     * Retourne la texture de fond du bouton.
     *
     * @return La {@link Texture} du bouton
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Définit la texture de fond du bouton.
     *
     * @param texture La {@link Texture} à utiliser comme fond
     */
    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    /**
     * Retourne le numéro unique identifiant ce jeu dans le tableau.
     *
     * @return Le numéro du jeu
     */
    public int getNumeroDeJeu() {
        return numeroDeJeu;
    }

    /**
     * Définit le numéro unique identifiant ce jeu.
     *
     * @param numeroDeJeu Le numéro à assigner au jeu
     */
    public void setNumeroDeJeu(int numeroDeJeu) {
        this.numeroDeJeu = numeroDeJeu;
    }
}