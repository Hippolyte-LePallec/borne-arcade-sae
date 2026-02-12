import MG2D.geometrie.Point;
import MG2D.geometrie.Texture;
import java.io.IOException;

/**
 * Classe représentant le pointeur de sélection dans le menu des jeux.
 * Gère l'affichage visuel de la sélection et le lancement des jeux.
 * 
 * @see Graphique
 * @see BoiteSelection
 * @version 1.0
 */
public class Pointeur {
    
    /** Index du jeu actuellement sélectionné dans le tableau */
    private int value;
    
    /** Texture de l'étoile gauche du pointeur */
    private Texture triangleGauche;
    
    /** Texture de l'étoile droite du pointeur */
    private Texture triangleDroite;
    
    /** Texture du rectangle de sélection central */
    private Texture rectangleCentre;
    
    /**
     * Constructeur du pointeur.
     * Initialise les textures de sélection et positionne le pointeur sur le dernier jeu.
     */
    public Pointeur() {
        this.triangleGauche = new Texture("img/star.png", new Point(30, 492), 40, 40);
        this.triangleDroite = new Texture("img/star.png", new Point(530, 492), 40, 40);
        this.rectangleCentre = new Texture("img/select2.png", new Point(80, 460), 440, 100);
        this.value = Graphique.tableau.length - 1;
    }
    
    /**
     * Lance le jeu sélectionné lorsque le bouton A est pressé.
     * Exécute le script shell correspondant au jeu et attend sa fin avant de reprendre le menu.
     * La musique de fond est arrêtée pendant le jeu puis relancée au retour.
     * 
     * @param clavier Le clavier de la borne d'arcade
     */
    public void lancerJeu(ClavierBorneArcade clavier) {
        if (clavier.getBoutonJ1ATape()) {
            try {
                Graphique.stopMusiqueFond();
                Process process = Runtime.getRuntime().exec("./" + Graphique.tableau[getValue()].getNom() + ".sh");
                process.waitFor(); // Attend la fin du jeu avant de reprendre le menu
                Graphique.lectureMusiqueFond();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Retourne l'index du jeu sélectionné.
     * @return L'index dans le tableau de jeux
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Définit l'index du jeu sélectionné.
     * @param value Le nouvel index
     */
    public void setValue(int value) {
        this.value = value;
    }
    
    /**
     * Retourne la texture de l'étoile gauche.
     * @return La texture du triangle gauche
     */
    public Texture getTriangleGauche() {
        return triangleGauche;
    }
    
    /**
     * Définit la texture de l'étoile gauche.
     * @param triangleGauche La nouvelle texture
     */
    public void setTriangleGauche(Texture triangleGauche) {
        this.triangleGauche = triangleGauche;
    }
    
    /**
     * Retourne la texture de l'étoile droite.
     * @return La texture du triangle droit
     */
    public Texture getTriangleDroite() {
        return triangleDroite;
    }
    
    /**
     * Définit la texture de l'étoile droite.
     * @param triangleDroite La nouvelle texture
     */
    public void setTriangleDroite(Texture triangleDroite) {
        this.triangleDroite = triangleDroite;
    }
    
    /**
     * Retourne la texture du rectangle de sélection.
     * @return La texture du rectangle central
     */
    public Texture getRectangleCentre() {
        return rectangleCentre;
    }
    
    /**
     * Définit la texture du rectangle de sélection.
     * @param rectangleCentre La nouvelle texture
     */
    public void setRectangleCentre(Texture rectangleCentre) {
        this.rectangleCentre = rectangleCentre;
    }
}