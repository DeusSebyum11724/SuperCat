package com.supercat.model;

import com.supercat.ui.Theme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Chien : ennemi du jeu. Il se deplace de facon autonome (mouvement de
 * patrouille) le long d'un couloir, sur un seul axe. Quand il rencontre un
 * mur (collision detectee par le GameEngine) ou atteint la limite de sa zone
 * de patrouille, il fait demi-tour.
 *
 * Le deplacement autonome des ennemis est l'une des fonctionnalites avancees
 * exigees pour un projet de Type A.
 */
public class Dog extends GameObject {

    public static final double SIZE = 32;

    private double speed = 1.6;
    private double vx;
    private double vy;
    private final double startX;
    private final double startY;
    private final double patrolRange;
    private boolean facingLeft = false;
    private double animTick = 0;

    public Dog(double x, double y, double patrolRange) {
        super(x, y, SIZE, SIZE);
        this.startX = x;
        this.startY = y;
        this.patrolRange = patrolRange;
        this.vx = speed;   // patrouille horizontale par defaut
        this.vy = 0;
    }

    /** Oriente la patrouille sur l'axe horizontal. */
    public void setHorizontal() { vx = speed; vy = 0; }

    /** Oriente la patrouille sur l'axe vertical. */
    public void setVertical() { vx = 0; vy = speed; }

    /** Definit la vitesse en conservant la direction courante. */
    public void setSpeed(double speed) {
        this.speed = speed;
        if (vx != 0) vx = (vx > 0 ? speed : -speed);
        if (vy != 0) vy = (vy > 0 ? speed : -speed);
    }

    /** Fait faire demi-tour au chien (appele lors d'une collision avec un mur). */
    public void reverse() {
        vx = -vx;
        vy = -vy;
    }

    @Override
    public void update() {
        x += vx;
        y += vy;
        if (vx != 0) {
            facingLeft = vx < 0;
        }
        animTick += 0.25;

        // demi-tour aux limites de la zone de patrouille
        if (vx != 0 && Math.abs(x - startX) > patrolRange) {
            x = startX + Math.signum(x - startX) * patrolRange;
            reverse();
        }
        if (vy != 0 && Math.abs(y - startY) > patrolRange) {
            y = startY + Math.signum(y - startY) * patrolRange;
            reverse();
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        double cx = getCenterX();
        double cy = getCenterY() + Math.sin(animTick) * 1.3;

        gc.save();
        if (facingLeft) {
            gc.translate(cx * 2, 0);
            gc.scale(-1, 1);
        }
        drawDogFacingRight(gc, cx, cy);
        gc.restore();
    }

    /** Dessine un chien de dessin anime oriente vers la droite. */
    private void drawDogFacingRight(GraphicsContext gc, double cx, double cy) {
        double legSwing = Math.sin(animTick) * 2.4;
        double tailWag = Math.sin(animTick * 1.5) * 5;

        // --- queue ---
        gc.setStroke(Theme.DOG_BODY);
        gc.setLineWidth(5);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.beginPath();
        gc.moveTo(cx - 13, cy - 2);
        gc.quadraticCurveTo(cx - 20, cy - 6, cx - 19 + tailWag, cy - 14);
        gc.stroke();

        // --- pattes ---
        gc.setFill(Theme.DOG_DARK);
        gc.fillOval(cx - 12, cy + 8 - legSwing, 7, 9);
        gc.fillOval(cx + 6,  cy + 8 + legSwing, 7, 9);
        gc.setFill(Theme.DOG_BODY);
        gc.fillOval(cx - 6, cy + 8 + legSwing, 7, 9);
        gc.fillOval(cx + 1, cy + 8 - legSwing, 7, 9);

        // --- corps ---
        gc.setFill(Theme.DOG_BODY);
        gc.fillOval(cx - 15, cy - 8, 28, 21);
        gc.setFill(Theme.DOG_BELLY);
        gc.fillOval(cx - 9, cy + 1, 17, 11);

        // --- tete ---
        gc.setFill(Theme.DOG_BODY);
        gc.fillOval(cx + 2, cy - 15, 19, 18);

        // --- museau ---
        gc.setFill(Theme.DOG_BELLY);
        gc.fillOval(cx + 13, cy - 5, 11, 9);
        gc.setFill(Color.web("#2B2B2B"));
        gc.fillOval(cx + 20, cy - 4, 4.5, 4);

        // --- oreilles tombantes ---
        gc.setFill(Theme.DOG_DARK);
        gc.fillOval(cx + 1, cy - 14, 8, 14);
        gc.fillOval(cx + 15, cy - 15, 8, 12);

        // --- yeux ---
        gc.setFill(Color.WHITE);
        gc.fillOval(cx + 8, cy - 10, 6, 7);
        gc.fillOval(cx + 14, cy - 10, 6, 7);
        gc.setFill(Color.web("#2B2B2B"));
        gc.fillOval(cx + 10, cy - 9, 3, 4);
        gc.fillOval(cx + 16, cy - 9, 3, 4);

        // --- sourcils fronces (air mechant) ---
        gc.setStroke(Theme.DOG_DARK);
        gc.setLineWidth(2);
        gc.strokeLine(cx + 7, cy - 12, cx + 13, cy - 10);
        gc.strokeLine(cx + 14, cy - 10, cx + 20, cy - 12);

        // --- collier rouge ---
        gc.setStroke(Color.web("#C0392B"));
        gc.setLineWidth(3);
        gc.strokeLine(cx + 1, cy + 1, cx + 9, cy + 4);
        gc.setFill(Color.web("#F1C40F"));
        gc.fillOval(cx + 3, cy + 3, 4, 4);
    }

    public double getSpeed() { return speed; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
}
