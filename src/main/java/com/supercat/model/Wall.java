package com.supercat.model;

import com.supercat.ui.Theme;
import javafx.scene.canvas.GraphicsContext;

/**
 * Mur du labyrinthe. Objet statique : il ne bouge pas mais bloque les
 * deplacements du chat et fait rebrousser chemin aux chiens.
 */
public class Wall extends GameObject {

    public Wall(double x, double y, double size) {
        super(x, y, size, size);
    }

    @Override
    public void update() {
        // un mur est immobile : rien a mettre a jour
    }

    @Override
    public void render(GraphicsContext gc) {
        // bloc de base
        gc.setFill(Theme.WALL);
        gc.fillRect(x, y, width, height);

        // effet de relief (bord clair en haut/gauche, sombre en bas/droite)
        gc.setFill(Theme.WALL_LIGHT);
        gc.fillRect(x, y, width, 3);
        gc.fillRect(x, y, 3, height);
        gc.setFill(Theme.WALL_DARK);
        gc.fillRect(x, y + height - 3, width, 3);
        gc.fillRect(x + width - 3, y, 3, height);

        // joints de brique
        gc.setStroke(Theme.WALL_DARK);
        gc.setLineWidth(1.5);
        double mid = y + height / 2.0;
        gc.strokeLine(x + 3, mid, x + width - 3, mid);
        gc.strokeLine(x + width / 2.0, y + 3, x + width / 2.0, mid);
        gc.strokeLine(x + width / 4.0, mid, x + width / 4.0, y + height - 3);
        gc.strokeLine(x + 3 * width / 4.0, mid, x + 3 * width / 4.0, y + height - 3);
    }
}
