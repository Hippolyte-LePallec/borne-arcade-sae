package fr.iutlittoral.systems;

import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;

import fr.iutlittoral.events.TargetDestroyed;

public class Score implements Listener<TargetDestroyed> {

    private int score = 0;

    public int getScore() {
        return score;
    }

    @Override
    public void receive(Signal<TargetDestroyed> signal, TargetDestroyed object) {
        this.score += object.score;
    }

   
}
