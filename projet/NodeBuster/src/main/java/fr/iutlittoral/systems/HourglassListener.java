package fr.iutlittoral.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;
import fr.iutlittoral.components.Hourglass;
import fr.iutlittoral.events.TargetDestroyed;

/**
 * Listener pour les événements TargetDestroyed qui détecte les sabliers
 * et ajoute du temps quand ils sont détruits
 */
public class HourglassListener implements Listener<TargetDestroyed> {
    private Runnable onHourglassDestroyed;
    private int timeBonus;
    private ComponentMapper<Hourglass> hourglasseMapper = ComponentMapper.getFor(Hourglass.class);

    public HourglassListener(Runnable onHourglassDestroyed) {
        this.onHourglassDestroyed = onHourglassDestroyed;
    }

    @Override
    public void receive(Signal<TargetDestroyed> signal, TargetDestroyed event) {
        Entity entity = event.getEntity();

        // Vérifier si l'entité détruite a un composant Hourglass
        if (entity != null && hourglasseMapper.has(entity)) {
            Hourglass hourglass = hourglasseMapper.get(entity);
            timeBonus = hourglass.getTimeBonus();
            // Appeler le callback pour ajouter le temps
            onHourglassDestroyed.run();
        }
    }

    public int getTimeBonus() {
        return timeBonus;
    }
}
