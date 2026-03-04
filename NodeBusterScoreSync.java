import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Classe pour synchroniser les scores du jeu NodeBuster avec la borne d'arcade.
 * Crée un lien symbolique ou copie le fichier de scores depuis le jeu vers le
 * répertoire borne.
 */
public class NodeBusterScoreSync {

    /**
     * Chemin du fichier de scores généré par le jeu NodeBuster
     */
    private static final String GAME_SCORES_FILE = "nodebusters_scores.txt";

    /**
     * Chemin où la borne d'arcade cherche les scores pour NodeBuster
     */
    private static final String BORNE_SCORES_FILE = "projet/TP-Jeu-NodeBuster-main/highscore";

    /**
     * Synchronise les fichiers de scores entre le jeu et la borne.
     * Crée un lien symbolique si possible, sinon copie le fichier.
     */
    public static void syncScores() {
        try {
            File gameScoresFile = new File(GAME_SCORES_FILE);
            File borneScoresDir = new File("projet/TP-Jeu-NodeBuster-main");

            // Créer le répertoire s'il n'existe pas
            if (!borneScoresDir.exists()) {
                borneScoresDir.mkdirs();
            }

            // Essayer de créer un lien symbolique (Linux/Mac)
            try {
                Files.createSymbolicLink(
                        Paths.get(BORNE_SCORES_FILE),
                        Paths.get(gameScoresFile.getAbsolutePath()));
                System.out.println("Lien symbolique créé pour les scores");
            } catch (Exception e) {
                // Si le lien symbolique échoue (Windows ou déjà existant), copier le fichier
                System.out.println("Lien symbolique échoué, tentative de copie...");
                if (gameScoresFile.exists()) {
                    Files.copy(
                            gameScoresFile.toPath(),
                            Paths.get(BORNE_SCORES_FILE),
                            StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Fichier des scores copié pour la borne");
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la synchronisation des scores: " + e.getMessage());
        }
    }

    /**
     * Met à jour le fichier des scores de la borne à partir du fichier du jeu.
     * À appeler régulièrement pour mettre les scores à jour.
     */
    public static void updateBorneScores() {
        try {
            File gameScoresFile = new File(GAME_SCORES_FILE);
            if (gameScoresFile.exists()) {
                Files.copy(
                        gameScoresFile.toPath(),
                        Paths.get(BORNE_SCORES_FILE),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour des scores: " + e.getMessage());
        }
    }
}
