import MG2D.Couleur;
import MG2D.audio.*;
import MG2D.geometrie.Rectangle;
import java.awt.Font;
import java.io.File;

/**
 * Classe représentant une boîte de sélection interactive pour naviguer dans un menu de jeux.
 * Cette classe gère la navigation avec un pointeur et les interactions avec le clavier d'une borne d'arcade.
 * 
 * <p>Elle permet de :</p>
 * <ul>
 *   <li>Naviguer vers le haut et le bas dans une liste de jeux</li>
 *   <li>Gérer le défilement circulaire (retour au début/fin de liste)</li>
 *   <li>Afficher dynamiquement les textes au fur et à mesure de la navigation</li>
 *   <li>Jouer des effets sonores lors de la sélection</li>
 *   <li>Valider une sélection avec le bouton Z</li>
 * </ul>
 * 
 * <p>Elle hérite de la classe {@link Boite} et utilise la bibliothèque MG2D pour
 * le rendu graphique et la gestion audio.</p>
 * 
 * @see Boite
 * @see Pointeur
 * @see ClavierBorneArcade
 * @version 1.1
 * @since 07/11/2019 (amélioration de la navigation)
 */
public class BoiteSelection extends Boite {
    
    /**
     * Le pointeur indiquant l'élément actuellement sélectionné dans le menu.
     * Sa valeur correspond à l'index du jeu sélectionné dans le tableau.
     */
    Pointeur pointeur;
    
    /**
     * La police de caractères utilisée pour afficher les textes du menu.
     * Police par défaut : PrStart.ttf en taille 26.
     */
    Font font;
    
    /**
     * Constructeur de la boîte de sélection.
     * Initialise la boîte avec un rectangle défini et associe un pointeur pour la navigation.
     * 
     * @param rectangle Le rectangle définissant la zone d'affichage de la boîte
     * @param pointeur Le pointeur permettant de suivre l'élément sélectionné
     */
    public BoiteSelection(Rectangle rectangle, Pointeur pointeur) {
        super(rectangle);
        this.pointeur = pointeur;
    }
    
    /**
     * Gère la navigation et la sélection dans le menu de jeux.
     * Cette méthode traite les entrées du clavier pour :
     * <ul>
     *   <li>Naviguer vers le haut (joystick haut) : remonte dans la liste, avec retour circulaire au dernier élément si on est au premier</li>
     *   <li>Naviguer vers le bas (joystick bas) : descend dans la liste, avec retour circulaire au premier élément si on est au dernier</li>
     *   <li>Valider la sélection (bouton Z) : retourne false pour indiquer qu'une sélection a été faite</li>
     * </ul>
     * 
     * <p>À chaque déplacement :</p>
     * <ul>
     *   <li>Un bruitage de sélection est joué</li>
     *   <li>Les textes sont affichés dynamiquement s'ils ne l'étaient pas déjà</li>
     *   <li>Les éléments du menu sont déplacés visuellement (translation de 110 pixels)</li>
     *   <li>La police et la couleur des textes sont mises à jour</li>
     * </ul>
     * 
     * <p><b>Navigation circulaire :</b></p>
     * <ul>
     *   <li><b>HAUT depuis le dernier élément :</b> retour au premier élément (index 0)</li>
     *   <li><b>BAS depuis le premier élément :</b> passage au dernier élément</li>
     * </ul>
     * 
     * @param clavier Le clavier de la borne d'arcade permettant de capturer les entrées utilisateur
     * @return {@code true} si la navigation continue, {@code false} si une sélection a été validée (bouton Z)
     * 
     * @see ClavierBorneArcade#getJoyJ1HautTape()
     * @see ClavierBorneArcade#getJoyJ1BasTape()
     * @see ClavierBorneArcade#getBoutonJ1ZTape()
     * @see Graphique#tableau
     * @see Graphique#afficherTexte(int)
     */
    public boolean selection(ClavierBorneArcade clavier) {
        Bruitage selection = new Bruitage("sound/bip.mp3");
        font = null;
        try {
            File in = new File("fonts/PrStart.ttf");
            font = font.createFont(Font.TRUETYPE_FONT, in);
            font = font.deriveFont(26.0f);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        // Modifier le 07/11/2019 pour améliorer la navigation
        /*
         * BACK:
         * repasse au premier élément du tableau lorsque la valeur du pointeur est égale à la
         * taille du tableau-1
         *
         * FRONT:
         * descend au dernier jeux de la liste afficher sur le menu
         */
        if (clavier.getJoyJ1HautTape() && (pointeur.getValue() <= Graphique.tableau.length - 1)) {
            if (Graphique.textesAffiches[pointeur.getValue()] == false) {
                Graphique.afficherTexte(pointeur.getValue());
                Graphique.textesAffiches[pointeur.getValue()] = true;
            }
            selection.lecture();
            if (pointeur.getValue() == Graphique.tableau.length - 1) {
                pointeur.setValue(0);
                for (int i = 0; i < Graphique.tableau.length; i++) {
                    Graphique.tableau[i].getTexte().translater(0, 110 * (Graphique.tableau.length - 1));
                    Graphique.tableau[i].getTexture().translater(0, 110 * (Graphique.tableau.length - 1));
                    Graphique.tableau[i].getTexte().setPolice(font);
                    Graphique.tableau[i].getTexte().setCouleur(Couleur.BLANC);
                }
            } else {
                for (int i = 0; i < Graphique.tableau.length; i++) {
                    Graphique.tableau[i].getTexte().translater(0, -110);
                    Graphique.tableau[i].getTexture().translater(0, -110);
                    Graphique.tableau[i].getTexte().setPolice(font);
                    Graphique.tableau[i].getTexte().setCouleur(Couleur.BLANC);
                }
                pointeur.setValue(pointeur.getValue() + 1);
            }
        }
        
        // Modifier le 07/11/2019 pour améliorer la navigation
        /*
         * BACK:
         * repasse au dernier élément du tableau lorsque la valeur du pointeur est égale à 0
         *
         * FRONT:
         * Remonte au premier jeux de la liste afficher sur le menu
         */
        if (clavier.getJoyJ1BasTape() && pointeur.getValue() >= 0) {
            if (Graphique.textesAffiches[pointeur.getValue()] == false) {
                Graphique.afficherTexte(pointeur.getValue());
                Graphique.textesAffiches[pointeur.getValue()] = true;
            }
            try {
                selection.lecture();
            } catch (Exception e) {
            }
            if (pointeur.getValue() == 0) {
                pointeur.setValue(Graphique.tableau.length - 1);
                for (int i = 0; i < Graphique.tableau.length; i++) {
                    Graphique.tableau[i].getTexte().translater(0, -110 * (Graphique.tableau.length - 1));
                    Graphique.tableau[i].getTexture().translater(0, -110 * (Graphique.tableau.length - 1));
                    Graphique.tableau[i].getTexte().setPolice(font);
                    Graphique.tableau[i].getTexte().setCouleur(Couleur.BLANC);
                }
            } else {
                for (int i = 0; i < Graphique.tableau.length; i++) {
                    Graphique.tableau[i].getTexte().translater(0, 110);
                    Graphique.tableau[i].getTexture().translater(0, 110);
                    Graphique.tableau[i].getTexte().setPolice(font);
                    Graphique.tableau[i].getTexte().setCouleur(Couleur.BLANC);
                }
                pointeur.setValue(pointeur.getValue() - 1);
                System.out.println(pointeur.getValue());
            }
        }
        
        if (clavier.getBoutonJ1ZTape()) {
            return false;
        }
        return true;
    }
    
    /**
     * Retourne le pointeur associé à cette boîte de sélection.
     * 
     * @return Le {@link Pointeur} indiquant l'élément actuellement sélectionné
     */
    public Pointeur getPointeur() {
        return pointeur;
    }
    
    /**
     * Modifie le pointeur associé à cette boîte de sélection.
     * 
     * @param pointeur Le nouveau {@link Pointeur} à associer
     */
    public void setPointeur(Pointeur pointeur) {
        this.pointeur = pointeur;
    }
}