package fr.iutlittoral.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;
import fr.iutlittoral.events.TargetDestroyed;
import fr.iutlittoral.components.Penalty;
import fr.iutlittoral.components.Slime;
import fr.iutlittoral.components.Target;
import fr.iutlittoral.components.Velocity;

public class Score implements Listener<TargetDestroyed> {
    private int valeur = 0;

    // Mappers pour identifier les types d'entités
    private ComponentMapper<Penalty> pm = ComponentMapper.getFor(Penalty.class);
    private ComponentMapper<Slime> sm = ComponentMapper.getFor(Slime.class);
    private ComponentMapper<Target> tm = ComponentMapper.getFor(Target.class);
    private ComponentMapper<Velocity> vm = ComponentMapper.getFor(Velocity.class);

    @Override
    public void receive(Signal<TargetDestroyed> signal, TargetDestroyed event) {
        // Utilisation de 'entity' car c'est le nom défini dans TargetDestroyed.java
        Entity e = event.entity;

        if (e == null)
            return;

        if (pm.has(e)) {
            // Cercles VIRUS VIOLETS : -10 points
            valeur -= 10;
        } else if (sm.has(e)) {
            // Slimes CYAN : +3 points
            valeur += 3;
        } else if (tm.has(e)) {
            // Distinction par la vitesse (Velocity)
            if (vm.has(e)) {
                // Boîtes BLEUES FONCÉES (bougent) : +2 points
                valeur += 2;
            } else {
                // Boîtes DORÉES (statiques) : +1 point
                valeur += 1;
            }
        }

        // Empêche le score d'être négatif
        if (valeur < 0)
            valeur = 0;
    }

    public int getScore() {
        return valeur;
    }

    public void reset() {
        this.valeur = 0;
    }
}