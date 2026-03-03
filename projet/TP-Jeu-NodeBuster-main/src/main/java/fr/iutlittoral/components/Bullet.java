package fr.iutlittoral.components;

import com.badlogic.ashley.core.Component;

public class Bullet implements Component {
    /** top-left corner of the bullet hitbox */
    public double x;
    public double y;
    /** dimensions of the square hitbox */
    public double width;
    public double height;

    /**
     * Constructs a square bullet.
     * 
     * @param x      top-left x coordinate
     * @param y      top-left y coordinate
     * @param width  width of the bullet (usually Cursor.SIZE)
     * @param height height of the bullet
     */
    public Bullet(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
