package com.supercat.model;

import com.supercat.ui.Theme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Poisson d'or : objet a collecter. Le chat gagne 100 points par poisson
 * collecte (regle metier RM3). Tous les poissons d'un niveau doivent etre
 * ramasses pour deverrouiller la sortie (regle metier RM2).
 *
 * Le poisson flotte verticalement : c'est l'animation de flottement
 * mentionnee dans le diagramme de classes UML.
 */
public class Fish extends GameObject {

    public static final double SIZE = 24;
    private static final int VALUE = 100;

    private boolean collected = false;
    private final double baseY;
    private double bobTick;

    public Fish(double x, double y) {
        super(x, y, SIZE, SIZE);
        this.baseY = y;
        this.bobTick = Math.random() * Math.PI * 2;   // dephasage aleatoire
    }

    @Override
    public void update() {
        bobTick += 0.08;
        y = baseY + Math.sin(bobTick) * 3.0;   // flottement haut / bas
    }

    @Override
    public void render(GraphicsContext gc) {
        if (collected) {
            return;
        }
        double cx = getCenterX();
        double cy = getCenterY();
        double wobble = Math.sin(bobTick * 1.4) * 2;   // ondulation de la queue

        // halo dore
        gc.setFill(Color.rgb(255, 210, 63, 0.25));
        gc.fillOval(cx - 16, cy - 16, 32, 32);

        // queue
        gc.setFill(Theme.FISH_DARK);
        gc.fillPolygon(new double[]{cx - 6, cx - 14, cx - 14},
                       new double[]{cy, cy - 7 + wobble, cy + 7 + wobble}, 3);

        // corps
        gc.setFill(Theme.FISH_BODY);
        gc.fillOval(cx - 10, cy - 7, 20, 14);

        // nageoire dorsale
        gc.setFill(Theme.FISH_DARK);
        gc.fillPolygon(new double[]{cx - 3, cx + 3, cx + 1},
                       new double[]{cy - 6, cy - 6, cy - 12}, 3);

        // reflet brillant
        gc.setFill(Color.rgb(255, 255, 255, 0.6));
        gc.fillOval(cx - 4, cy - 4, 6, 4);

        // oeil
        gc.setFill(Color.WHITE);
        gc.fillOval(cx + 3, cy - 4, 5, 5);
        gc.setFill(Color.web("#2B2B2B"));
        gc.fillOval(cx + 4.5, cy - 2.5, 2.5, 2.5);
    }

    public boolean isCollected() { return collected; }

    /** Marque le poisson comme collecte : il ne sera plus dessine ni teste. */
    public void collect() { this.collected = true; }

    public int getValue() { return VALUE; }
}
