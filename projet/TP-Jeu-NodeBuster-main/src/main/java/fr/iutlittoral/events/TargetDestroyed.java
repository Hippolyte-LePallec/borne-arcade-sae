package fr.iutlittoral.events;

public class TargetDestroyed {
    public final int score;
    public double x;
    public double y;
    /** true when the destroyed target had a Slime component */
    public final boolean slime;

    public TargetDestroyed(int score, double x, double y, boolean slime) {
        this.score = score;
        this.x = x;
        this.y = y;
        this.slime = slime;
    }
}
