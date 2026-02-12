import MG2D.geometrie.Rectangle;

/**
 * Représente une entité abstraite possédant une forme rectangulaire.
 *
 * <p>La classe {@code Boite} encapsule un objet {@link Rectangle}
 * utilisé pour représenter la position et les dimensions d’un élément
 * graphique dans le jeu.</p>
 *
 * <p>Cette classe est abstraite et doit être étendue par des classes
 * concrètes représentant des objets spécifiques.</p>
 */
public abstract class Boite {

    /**
     * Rectangle associé à la boîte.
     * Il définit la position et les dimensions de l’objet.
     */
    private Rectangle rectangle;
	
    /**
     * Construit une boîte à partir d’un rectangle donné.
     *
     * @param rectangle le rectangle représentant la position
     *                  et les dimensions de la boîte
     */
    Boite(Rectangle rectangle){
        this.rectangle = rectangle;
    }

    /**
     * Retourne le rectangle associé à la boîte.
     *
     * @return le {@link Rectangle} représentant cette boîte
     */
    public Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * Modifie le rectangle associé à la boîte.
     *
     * @param rectangle le nouveau {@link Rectangle}
     *                  représentant la boîte
     */
    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }
}
