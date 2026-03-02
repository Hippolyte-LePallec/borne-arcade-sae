package fr.iutlittoral.systems;

import java.util.Random;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;

import fr.iutlittoral.components.AlphaDecay;
import fr.iutlittoral.components.BoxShape;
import fr.iutlittoral.components.LimitedLifespan;
import fr.iutlittoral.components.Position;
import fr.iutlittoral.components.Shade;
import fr.iutlittoral.components.Velocity;
import fr.iutlittoral.events.TargetDestroyed;
import javafx.scene.paint.Color;

public class ExplosionListener implements Listener<TargetDestroyed>{

    private Color color;
    private Engine engine;

    public ExplosionListener(Color color, Engine engine) {
        this.engine = engine;
        this.color = Color.ORANGE;
    }

    @Override
    public void receive(Signal<TargetDestroyed> signal, TargetDestroyed event) {
        double x = event.x;
        double y = event.y;

        for (double angle = 0; angle < 360; angle += 2) {
            // Convertir l'angle en radians
            double radians = Math.toRadians(angle);

            // Générer un rayon aléatoire pour donner un effet d'explosion irrégulière
            double radius = 200 * Math.random();

            double dx = radius * Math.cos(radians);
            double dy = radius * Math.sin(radians);

            Entity entity = new Entity();
            entity.add(new Position(x, y));
            entity.add(new BoxShape(5, 5));
            entity.add(new Shade(randColor()));
            entity.add(new LimitedLifespan(randomLifespan()));
            entity.add(new AlphaDecay());

            entity.add(new Velocity(dx, dy));

            engine.addEntity(entity);
        }

        
    }

    private static Color randColor() {
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return Color.rgb(r, g, b);
    }   

    private static double randomLifespan() {
        Random random = new Random();
        double lifespan = random.nextDouble(1) + 0.2;
        return lifespan;
    }
    
}