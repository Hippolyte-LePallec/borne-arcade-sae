package fr.iutlittoral.systems;

import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;

import fr.iutlittoral.events.TargetDestroyed;

public class Score implements Listener<TargetDestroyed> {

    // SYSTÈME DE SCORE : garde en mémoire le score actuel du jeu
    // Il est incrémenté à chaque fois qu'une cible est détruite
    private int score = 0;

    /**
     * SYSTÈME DE SCORE : renvoie le score actuel du joueur
     * Utilisé pour vérifier la condition de victoire (score >= 30) et
     * la condition de défaite (score < 10 après le temps imparti)
     */
    public int getScore() {
        return score;
    }

    /**
     * ÉCOUTEUR DE SCORE : traite les événements de destruction de cible
     * Appelé automatiquement lorsqu'un signal TargetDestroyed est émis
     * Incrémente le score de la valeur du point de la cible détruite
     *
     * Flux : BulletCollisionSystem -> événement TargetDestroyed -> Score.receive()
     * ->
     * score mis à jour
     *
     * @param signal L'objet signal (non utilisé, requis par l'interface Listener)
     * @param object L'événement TargetDestroyed contenant la valeur de score de la
     *               cible
     */
    @Override
    public void receive(Signal<TargetDestroyed> signal, TargetDestroyed object) {
        // SCORE SYSTEM: Add the value of the destroyed target to the total score
        this.score += object.score;
    }

}
