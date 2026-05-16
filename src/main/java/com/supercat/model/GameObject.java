package com.supercat.model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Classe de base abstraite de tous les objets du jeu : le chat, les chiens,
 * les poissons, les murs, les bonus et la sortie en heritent.
 *
 * Conforme au diagramme de classes UML : chaque objet possede une position
 * (x, y), une taille (width, height) et expose les operations update() et
 * getBounds(). La methode render() a ete ajoutee pour que chaque objet sache
 * se dessiner lui-meme sur le canvas (principe de responsabilite unique).
 */
public abstract class GameObject {

    protected double x;
    protected double y;
    protected double width;
    protected double height;

    protected GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Met a jour la logique de l'objet (deplacement, animation) a chaque frame. */
    public abstract void update();

    /** Dessine l'objet sur le contexte graphique fourni. */
    public abstract void render(GraphicsContext gc);

    /**
     * Rectangle englobant l'objet. Utilise par le CollisionManager pour la
     * detection de collisions (intersection de deux Rectangle2D).
     */
    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }
}
