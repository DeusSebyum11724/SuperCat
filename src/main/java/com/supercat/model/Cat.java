package com.supercat.model;

import com.supercat.ui.Theme;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 * Le chat SuperCat : personnage controle par le joueur.
 *
 * Il se deplace dans les 4 directions du labyrinthe. Le deplacement reel et
 * la detection des collisions avec les murs sont assures par le GameEngine ;
 * cette classe memorise la vitesse souhaitee et anime le personnage
 * (demarche, balancement du corps, mouvement de la queue).
 */
public class Cat extends GameObject {

    public static final double SIZE = 30;

    private double speed = 3.6;
    private double vx;
    private double vy;
    private boolean facingLeft = false;
    private boolean moving = false;
    private double animTick = 0;

    public Cat(double x, double y) {
        super(x, y, SIZE, SIZE);
    }

    // ----- intentions de deplacement (appelees par le GameController) -----
    public void moveLeft()  { vx = -speed; facingLeft = true; }
    public void moveRight() { vx =  speed; facingLeft = false; }
    public void moveUp()    { vy = -speed; }
    public void moveDown()  { vy =  speed; }

    /** Remet la vitesse a zero en debut de frame, avant la relecture clavier. */
    public void clearVelocity() {
        vx = 0;
        vy = 0;
    }

    @Override
    public void update() {
        moving = (vx != 0 || vy != 0);
        if (moving) {
            animTick += 0.35;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        double cx = getCenterX();
        double cy = getCenterY() + (moving ? Math.sin(animTick) * 1.8 : 0);

        gc.save();
        if (facingLeft) {
            // miroir horizontal autour de l'axe vertical du chat
            gc.translate(cx * 2, 0);
            gc.scale(-1, 1);
        }
        drawCatFacingRight(gc, cx, cy);
        gc.restore();
    }

    /** Dessine un chat de dessin anime oriente vers la droite. */
    private void drawCatFacingRight(GraphicsContext gc, double cx, double cy) {
        double legSwing = moving ? Math.sin(animTick) * 2.2 : 0;
        double tailWag = moving ? Math.sin(animTick * 0.8) * 4 : 2;

        // --- queue (dessinee en premier, derriere le corps) ---
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setStroke(Theme.CAT_BODY);
        gc.setLineWidth(5);
        gc.beginPath();
        gc.moveTo(cx - 12, cy + 4);
        gc.quadraticCurveTo(cx - 21, cy - 2, cx - 18 + tailWag, cy - 12);
        gc.stroke();

        // --- pattes (animees en opposition de phase) ---
        gc.setFill(Theme.CAT_STRIPE);
        gc.fillOval(cx - 11, cy + 8 - legSwing, 6, 8);
        gc.fillOval(cx + 6,  cy + 8 + legSwing, 6, 8);
        gc.setFill(Theme.CAT_BODY);
        gc.fillOval(cx - 6, cy + 8 + legSwing, 6, 8);
        gc.fillOval(cx + 1, cy + 8 - legSwing, 6, 8);

        // --- corps ---
        gc.setFill(Theme.CAT_BODY);
        gc.fillOval(cx - 14, cy - 8, 26, 20);

        // --- ventre clair ---
        gc.setFill(Theme.CAT_BELLY);
        gc.fillOval(cx - 8, cy - 1, 16, 11);

        // --- rayures du dos ---
        gc.setStroke(Theme.CAT_STRIPE);
        gc.setLineWidth(2.5);
        gc.strokeLine(cx - 6, cy - 7, cx - 4, cy - 2);
        gc.strokeLine(cx - 1, cy - 8, cx + 1, cy - 3);
        gc.strokeLine(cx + 4, cy - 7, cx + 6, cy - 3);

        // --- tete ---
        gc.setFill(Theme.CAT_BODY);
        gc.fillOval(cx + 1, cy - 14, 18, 17);

        // --- oreilles ---
        gc.setFill(Theme.CAT_BODY);
        gc.fillPolygon(new double[]{cx + 2, cx + 4, cx + 11},
                       new double[]{cy - 11, cy - 22, cy - 13}, 3);
        gc.fillPolygon(new double[]{cx + 10, cx + 17, cx + 19},
                       new double[]{cy - 13, cy - 22, cy - 10}, 3);
        gc.setFill(Theme.CAT_PINK);
        gc.fillPolygon(new double[]{cx + 4, cx + 5, cx + 9},
                       new double[]{cy - 12, cy - 18, cy - 13}, 3);
        gc.fillPolygon(new double[]{cx + 11, cx + 14, cx + 16},
                       new double[]{cy - 13, cy - 18, cy - 11}, 3);

        // --- yeux ---
        gc.setFill(Color.WHITE);
        gc.fillOval(cx + 5, cy - 9, 6, 7);
        gc.fillOval(cx + 12, cy - 9, 6, 7);
        gc.setFill(Color.web("#2B2B2B"));
        gc.fillOval(cx + 7, cy - 8, 3, 4);
        gc.fillOval(cx + 14, cy - 8, 3, 4);
        gc.setFill(Color.WHITE);
        gc.fillOval(cx + 7.5, cy - 7.5, 1.6, 1.6);
        gc.fillOval(cx + 14.5, cy - 7.5, 1.6, 1.6);

        // --- nez ---
        gc.setFill(Theme.CAT_PINK);
        gc.fillPolygon(new double[]{cx + 16, cx + 20, cx + 18},
                       new double[]{cy - 3, cy - 3, cy}, 3);

        // --- moustaches ---
        gc.setStroke(Color.web("#FFFFFF"));
        gc.setLineWidth(1);
        gc.strokeLine(cx + 17, cy - 2, cx + 24, cy - 4);
        gc.strokeLine(cx + 17, cy - 1, cx + 24, cy);
        gc.strokeLine(cx + 17, cy, cx + 23, cy + 3);
    }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public boolean isFacingLeft() { return facingLeft; }
    public boolean isMoving() { return moving; }
}
