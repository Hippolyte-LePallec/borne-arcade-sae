package fr.iutlittoral.components;

import com.badlogic.ashley.core.Component;

/**
 * Composant Hourglass (Sablier)
 * Augmente le temps de jeu quand le joueur le détruit
 */
public class Hourglass implements Component {
    public int timeBonus; // Temps à ajouter en secondes (2-10)

    public Hourglass(int timeBonus) {
        this.timeBonus = timeBonus;
    }

    public int getTimeBonus() {
        return timeBonus;
    }
}
