package fr.iutlittoral.events;

/**
 * Event dispatched when a spawned target expires without being destroyed by a
 * bullet.
 * Can be used to trigger a game-over condition or decrease player lives.
 */
public class TargetMissed {
    // could add additional information (position, score penalty) later
    public TargetMissed() {
    }
}
