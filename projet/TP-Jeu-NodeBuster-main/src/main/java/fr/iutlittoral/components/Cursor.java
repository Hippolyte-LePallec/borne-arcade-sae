package fr.iutlittoral.components;

import com.badlogic.ashley.core.Component;

/**
 * Cursor component used for tagging the player's cursor entity.
 *
 * This class also exposes a central size constant so that other
 * systems and creators can reference the cursor dimensions without
 * hard‑coding values in multiple places.
 */
public class Cursor implements Component {
    /**
     * Side length of the square cursor in pixels. Used for rendering
     * and for the area covered by a shot.
     */
    public static final double SIZE = 24;

    // marker component, no instance data required
}
