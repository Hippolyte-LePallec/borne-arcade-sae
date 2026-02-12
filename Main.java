/**
 * Classe principale du menu de la borne d'arcade.
 * Point d'entrée de l'application.
 * 
 * @see Graphique
 * @version 1.0
 */
public class Main {
    
    /**
     * Point d'entrée de l'application.
     * Initialise l'interface graphique et lance la boucle de sélection de jeux.
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        Graphique g = new Graphique();
        while (true) {
            try {
                // Thread.sleep(250);
            } catch (Exception e) {
            }
            g.selectionJeu();
        }
    }
}