package fr.iutlittoral.events;

import com.badlogic.ashley.core.Entity;

public class TargetDestroyed {
    public final int score;
    public double x;
    public double y;
    /** true when the destroyed target had a Slime component */
    public final boolean slime;
    public final Entity entity;

    public TargetDestroyed(int score, double x, double y, boolean slime) {
        this(score, x, y, slime, null);
    }

    public TargetDestroyed(int score, double x, double y, boolean slime, Entity entity) {
        this.score = score;
        this.x = x;
        this.y = y;
        this.slime = slime;
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
