import java.io.File;
import java.io.IOException;

public class Main_Borne {
    public static void main(String[] args) {
        try {
            // Récupère le dossier courant (où se trouve le .java / .class)
            File dossier = new File(System.getProperty("user.dir"));

            // Chemin vers le script shell
            File script = new File(dossier, "lancerBorne.sh");

            // Commande pour exécuter le script
            ProcessBuilder pb = new ProcessBuilder("bash", script.getAbsolutePath());
            pb.directory(dossier);
            pb.inheritIO(); // pour afficher la sortie du script dans le terminal

            Process process = pb.start();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
