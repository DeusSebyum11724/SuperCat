package com.supercat.model;

import com.supercat.ui.Theme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Objet bonus special. Deux types existent :
 *  - POINTS : une etoile qui rapporte 250 points supplementaires ;
 *  - TIME   : une horloge qui ajoute 10 secondes au chronometre.
 *
 * Les objets speciaux font partie des fonctionnalites avancees exigees pour
 * un projet de Type A. Le bonus pulse et tourne sur lui-meme : c'est une
 * animation plus complexe que le simple deplacement.
 */
public class Bonus extends GameObject {

    public enum Type { POINTS, TIME }

    public static final double SIZE = 28;

    private final Type type;
    private final int value;
    private boolean collected = false;
    private double animTick;

    public Bonus(double x, double y, Type type) {
        super(x, y, SIZE, SIZE);
        this.type = type;
        this.value = (type == Type.POINTS) ? 250 : 10;   // 250 points ou +10 s
        this.animTick = Math.random() * Math.PI * 2;
    }

    @Override
    public void update() {
        animTick += 0.06;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (collected) {
            return;
        }
        double cx = getCenterX();
        double cy = getCenterY();
        double pulse = 1.0 + Math.sin(animTick * 2) * 0.12;

        gc.save();
        gc.translate(cx, cy);
        gc.scale(pulse, pulse);
        if (type == Type.POINTS) {
            drawStar(gc);
        } else {
            drawClock(gc);
        }
        gc.restore();
    }

    /** Etoile de points : tourne lentement sur elle-meme. */
    private void drawStar(GraphicsContext gc) {
        gc.rotate(Math.toDegrees(animTick));
        gc.setFill(Color.rgb(155, 89, 182, 0.30));
        gc.fillOval(-17, -17, 34, 34);

        double[] xs = new double[10];
        double[] ys = new double[10];
        for (int i = 0; i < 10; i++) {
            double r = (i % 2 == 0) ? 13 : 5.5;
            double a = Math.PI / 2 + i * Math.PI / 5;
            xs[i] = Math.cos(a) * r;
            ys[i] = -Math.sin(a) * r;
        }
        gc.setFill(Theme.BONUS_STAR);
        gc.fillPolygon(xs, ys, 10);
        gc.setFill(Color.web("#F1C40F"));
        gc.fillOval(-4, -4, 8, 8);
    }

    /** Horloge bonus : les aiguilles tournent. */
    private void drawClock(GraphicsContext gc) {
        gc.setFill(Color.rgb(46, 134, 222, 0.30));
        gc.fillOval(-17, -17, 34, 34);
        gc.setFill(Theme.BONUS_CLOCK);
        gc.fillOval(-13, -13, 26, 26);
        gc.setFill(Color.WHITE);
        gc.fillOval(-9, -9, 18, 18);

        gc.setStroke(Theme.BONUS_CLOCK);
        gc.setLineWidth(2);
        gc.strokeLine(0, 0, 0, -6);
        gc.strokeLine(0, 0, Math.cos(animTick) * 5, Math.sin(animTick) * 5);
        gc.setFill(Color.web("#2B2B2B"));
        gc.fillOval(-2, -2, 4, 4);
    }

    public boolean isCollected() { return collected; }
    public void collect() { this.collected = true; }
    public Type getType() { return type; }
    public int getValue() { return value; }
}
