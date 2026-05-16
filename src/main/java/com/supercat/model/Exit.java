package com.supercat.model;

import com.supercat.ui.Theme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * Sortie du labyrinthe. Elle reste verrouillee (rouge, avec un cadenas) tant
 * que tous les poissons d'or n'ont pas ete collectes (regle metier RM2).
 * Une fois deverrouillee, elle s'illumine en vert : franchir la sortie
 * termine le niveau.
 */
public class Exit extends GameObject {

    public static final double SIZE = 34;

    private boolean locked = true;
    private double glowTick = 0;

    public Exit(double x, double y) {
        super(x, y, SIZE, SIZE);
    }

    /** Deverrouille la sortie : tous les poissons ont ete ramasses. */
    public void unlock() {
        this.locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public void update() {
        if (!locked) {
            glowTick += 0.12;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        double cx = getCenterX();
        double cy = getCenterY();
        Color frame = locked ? Theme.EXIT_LOCKED : Theme.EXIT_OPEN;

        // halo pulsant lorsque la sortie est ouverte
        if (!locked) {
            double glow = 0.30 + Math.sin(glowTick) * 0.22;
            gc.setFill(Color.color(Theme.EXIT_OPEN.getRed(),
                                   Theme.EXIT_OPEN.getGreen(),
                                   Theme.EXIT_OPEN.getBlue(),
                                   Math.max(0.08, glow)));
            gc.fillOval(cx - 25, cy - 25, 50, 50);
        }

        // encadrement de la porte
        gc.setFill(frame);
        gc.fillRoundRect(cx - 17, cy - 17, 34, 34, 9, 9);
        // panneau de la porte
        gc.setFill(locked ? Color.web("#7B241C") : Color.web("#1E8449"));
        gc.fillRoundRect(cx - 12, cy - 13, 24, 27, 5, 5);

        if (locked) {
            // cadenas
            gc.setStroke(Color.web("#F1C40F"));
            gc.setLineWidth(2.5);
            gc.strokeArc(cx - 5, cy - 9, 10, 10, 0, 180, ArcType.OPEN);
            gc.setFill(Color.web("#F1C40F"));
            gc.fillRoundRect(cx - 6, cy - 5, 12, 10, 2, 2);
            gc.setFill(Color.web("#2B2B2B"));
            gc.fillOval(cx - 1.5, cy - 2, 3, 3);
        } else {
            // chevron vers le haut : "c'est par ici la sortie !"
            gc.setStroke(Color.web("#F1C40F"));
            gc.setLineWidth(3.5);
            gc.strokeLine(cx - 6, cy + 3, cx, cy - 5);
            gc.strokeLine(cx, cy - 5, cx + 6, cy + 3);
            gc.strokeLine(cx - 6, cy + 9, cx, cy + 1);
            gc.strokeLine(cx, cy + 1, cx + 6, cy + 9);
        }
    }
}
