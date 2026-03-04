package fr.iutlittoral.components;

import com.badlogic.ashley.core.Component;

/**
 * Penalty component marks an entity as a penalty circle.
 * When destroyed, it causes the player to lose points.
 */
public class Penalty implements Component {
    public Penalty() {
    }
}
