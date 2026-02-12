import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texture;

/**
 * Classe représentant une boîte contenant une image.
 * Cette classe gère l'affichage d'une texture graphique dans une zone rectangulaire définie.
 * 
 * <p>Elle hérite de la classe {@link Boite} et utilise la bibliothèque MG2D pour
 * le rendu graphique de l'image.</p>
 * 
 * <p>L'image est automatiquement chargée depuis un fichier nommé "photo_small.png"
 * situé dans le répertoire spécifié.</p>
 * 
 * @see Boite
 * @see MG2D.geometrie.Texture
 * @version 1.0
 */
public class BoiteImage extends Boite {
    
    /**
     * La texture contenant l'image à afficher.
     * Cette texture est positionnée au point (760, 648) par défaut.
     */
    Texture image;
    
    /**
     * Constructeur de la boîte image.
     * Initialise la boîte avec un rectangle défini et charge l'image depuis le chemin spécifié.
     * Le fichier image doit se nommer "photo_small.png" et se trouver dans le répertoire indiqué.
     * 
     * @param rectangle Le rectangle définissant la zone d'affichage de la boîte
     * @param image Le chemin du répertoire contenant le fichier photo_small.png
     * 
     * @see MG2D.geometrie.Texture
     * @see MG2D.geometrie.Point
     */
    BoiteImage(Rectangle rectangle, String image) {
        super(rectangle);
        this.image = new Texture(image + "/photo_small.png", new Point(760, 648));
    }
    
    /**
     * Retourne la texture de l'image affichée.
     * 
     * @return La {@link Texture} contenant l'image
     */
    public Texture getImage() {
        return this.image;
    }
    
    /**
     * Modifie l'image affichée en chargeant une nouvelle texture depuis un chemin spécifié.
     * Le nouveau fichier image doit également se nommer "photo_small.png" et se trouver
     * dans le répertoire indiqué.
     * 
     * @param chemin Le chemin du répertoire contenant le nouveau fichier photo_small.png
     * 
     * @see Texture#setImg(String)
     */
    public void setImage(String chemin) {
        this.image.setImg(chemin + "/photo_small.png");
        //this.image.setTaille(400, 320);
    }
}