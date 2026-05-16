package com.supercat.engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Petit texte qui s'eleve et s'estompe progressivement. Utilise comme retour
 * visuel immediat lorsqu'un poisson ou un bonus est collecte (regle RM7 :
 * "le jeu doit fournir un feedback immediat lors de chaque interaction").
 */
public class FloatingText {

    private final double x;
    private double y;
    private final String text;
    private final Color color;
    private double life = 1.0;   // 1.0 (neuf) -> 0.0 (disparu)

    public FloatingText(double x, double y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
    }

    /** Fait monter le texte et reduit sa duree de vie. */
    public void update() {
        y -= 0.9;
        life -= 0.022;
    }

    /** Vrai lorsque le texte a totalement disparu et peut etre supprime. */
    public boolean isDead() {
        return life <= 0;
    }

    public void render(GraphicsContext gc) {
        gc.save();
        gc.setGlobalAlpha(Math.max(0, life));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.fillText(text, x + 1, y + 1);
        gc.setFill(color);
        gc.fillText(text, x, y);
        gc.restore();
    }
}
