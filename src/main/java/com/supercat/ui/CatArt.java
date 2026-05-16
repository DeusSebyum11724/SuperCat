package com.supercat.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 * Dessin vectoriel d'un chat assis, anime.
 *
 * Trois pelages sont fournis : Nora la rousse (le personnage principal),
 * Suie le chat noir aux yeux d'or, et Givre le chat blanc et gris. Le meme
 * code de dessin sert pour les trois ; seule la palette change. L'animation
 * (battement de queue, clignement des yeux, respiration) est pilotee par un
 * temps continu exprime en secondes.
 */
public final class CatArt {

    private CatArt() {
        // classe utilitaire
    }

    /** Palette d'un chat : couleurs du pelage, du ventre, des yeux, des taches. */
    public static final class Pelage {
        final Color body;
        final Color shade;
        final Color belly;
        final Color innerEar;
        final Color eye;
        final Color patch;     // taches d'un pelage bicolore (peut etre null)

        Pelage(Color body, Color shade, Color belly, Color innerEar, Color eye, Color patch) {
            this.body = body;
            this.shade = shade;
            this.belly = belly;
            this.innerEar = innerEar;
            this.eye = eye;
            this.patch = patch;
        }
    }

    /** Nora, la chatte rousse -- personnage principal du jeu. */
    public static final Pelage NORA = new Pelage(
            Color.web("#F4A340"), Color.web("#DD8B2A"), Color.web("#FFF1DD"),
            Color.web("#F4A9A0"), Color.web("#6E9F5C"), null);

    /** Suie, le chat noir aux yeux d'or. */
    public static final Pelage SUIE = new Pelage(
            Color.web("#3B3442"), Color.web("#2A242F"), Color.web("#5C5462"),
            Color.web("#7E6E7D"), Color.web("#F2C84B"), null);

    /** Givre, le chat blanc et gris. */
    public static final Pelage GIVRE = new Pelage(
            Color.web("#F4F0E9"), Color.web("#D6CFC4"), Color.web("#FCFBF7"),
            Color.web("#EFC6BE"), Color.web("#7E96AA"), Color.web("#B3ABA0"));

    /**
     * Dessine un chat assis. Le point (cx, feetY) repose au sol, au centre des
     * pattes ; size correspond a la hauteur totale approximative du chat ;
     * t est un temps en secondes pilotant l'animation continue.
     */
    public static void draw(GraphicsContext g, Pelage cat, double cx, double feetY,
                            double size, double t) {
        double breath = 1.0 + Math.sin(t * 1.6) * 0.018;
        double bodyW = size * 0.58;
        double bodyH = size * 0.54 * breath;
        double bodyTop = feetY - bodyH;
        double hr = size * 0.235;
        double headCy = bodyTop - hr * 0.40 - Math.sin(t * 1.6) * size * 0.006;

        // ombre douce au sol
        g.setFill(Color.rgb(60, 56, 66, 0.12));
        g.fillOval(cx - bodyW * 0.66, feetY - size * 0.035, bodyW * 1.32, size * 0.09);

        // queue, qui se balance lentement
        double sway = Math.sin(t * 1.5) * size * 0.085;
        double tipX = cx + bodyW * 0.50 + sway;
        double tipY = bodyTop - size * 0.12;
        g.setLineCap(StrokeLineCap.ROUND);
        g.setStroke(cat.body);
        g.setLineWidth(size * 0.135);
        g.beginPath();
        g.moveTo(cx + bodyW * 0.32, feetY - bodyH * 0.06);
        g.quadraticCurveTo(cx + bodyW * 1.02, feetY - bodyH * 0.34, tipX, tipY);
        g.stroke();
        if (cat.patch != null) {                     // bout de queue grise (Givre)
            g.setStroke(cat.patch);
            g.beginPath();
            g.moveTo(cx + bodyW * 0.80, feetY - bodyH * 0.30);
            g.quadraticCurveTo(cx + bodyW * 0.92, feetY - bodyH * 0.56, tipX, tipY);
            g.stroke();
        }

        // corps
        g.setFill(cat.body);
        g.fillOval(cx - bodyW / 2, bodyTop, bodyW, bodyH);

        // pattes avant
        double pawW = bodyW * 0.30;
        double pawH = size * 0.12;
        g.fillOval(cx - bodyW * 0.30, feetY - pawH, pawW, pawH);
        g.fillOval(cx + bodyW * 0.30 - pawW, feetY - pawH, pawW, pawH);

        // ventre clair
        g.setFill(cat.belly);
        g.fillOval(cx - bodyW * 0.27, feetY - bodyH * 0.66, bodyW * 0.54, bodyH * 0.60);

        // tache de pelage sur le corps (Givre)
        if (cat.patch != null) {
            g.setFill(cat.patch);
            g.fillOval(cx - bodyW * 0.02, bodyTop + bodyH * 0.07, bodyW * 0.38, bodyH * 0.40);
        }

        // tete
        g.setFill(cat.body);
        g.fillOval(cx - hr, headCy - hr, hr * 2, hr * 2);

        drawEar(g, cat, cx, headCy, hr, true);
        drawEar(g, cat, cx, headCy, hr, false);

        // tache de pelage sur la tete (Givre)
        if (cat.patch != null) {
            g.setFill(cat.patch);
            g.fillOval(cx - hr * 0.84, headCy - hr * 0.84, hr * 0.90, hr * 0.78);
        }

        drawFace(g, cat, cx, headCy, hr, t);
    }

    private static void drawEar(GraphicsContext g, Pelage cat, double cx, double cy,
                                double hr, boolean left) {
        double s = left ? -1 : 1;
        g.setFill((cat.patch != null && left) ? cat.patch : cat.body);
        g.fillPolygon(
                new double[]{cx + s * hr * 0.70, cx + s * hr * 0.12, cx + s * hr * 0.96},
                new double[]{cy - hr * 0.46, cy - hr * 0.88, cy - hr * 1.46}, 3);
        g.setFill(cat.innerEar);
        g.fillPolygon(
                new double[]{cx + s * hr * 0.62, cx + s * hr * 0.30, cx + s * hr * 0.80},
                new double[]{cy - hr * 0.56, cy - hr * 0.84, cy - hr * 1.18}, 3);
    }

    private static void drawFace(GraphicsContext g, Pelage cat, double cx, double cy,
                                 double hr, double t) {
        double eyeY = cy - hr * 0.04;
        double eyeDx = hr * 0.42;
        double eyeW = hr * 0.40;
        double eyeH = hr * 0.52;

        // clignement : un bref battement toutes les 4,2 secondes
        boolean closed = (t % 4.2) < 0.16;
        if (closed) {
            g.setStroke(cat.shade);
            g.setLineWidth(hr * 0.10);
            g.setLineCap(StrokeLineCap.ROUND);
            g.strokeLine(cx - eyeDx - eyeW * 0.4, eyeY, cx - eyeDx + eyeW * 0.4, eyeY);
            g.strokeLine(cx + eyeDx - eyeW * 0.4, eyeY, cx + eyeDx + eyeW * 0.4, eyeY);
        } else {
            for (int i = -1; i <= 1; i += 2) {
                double ex = cx + i * eyeDx;
                g.setFill(cat.eye);
                g.fillOval(ex - eyeW / 2, eyeY - eyeH / 2, eyeW, eyeH);
                g.setFill(Color.web("#2A2630"));
                g.fillOval(ex - eyeW * 0.20, eyeY - eyeH * 0.34, eyeW * 0.40, eyeH * 0.68);
                g.setFill(Color.color(1, 1, 1, 0.92));
                g.fillOval(ex - eyeW * 0.04, eyeY - eyeH * 0.30, eyeW * 0.20, eyeH * 0.22);
            }
        }

        // nez
        g.setFill(cat.innerEar);
        g.fillPolygon(
                new double[]{cx - hr * 0.14, cx + hr * 0.14, cx},
                new double[]{cy + hr * 0.34, cy + hr * 0.34, cy + hr * 0.50}, 3);

        // bouche
        g.setStroke(cat.shade);
        g.setLineWidth(hr * 0.055);
        g.setLineCap(StrokeLineCap.ROUND);
        g.strokeLine(cx, cy + hr * 0.50, cx, cy + hr * 0.60);
        g.beginPath();
        g.moveTo(cx - hr * 0.16, cy + hr * 0.68);
        g.quadraticCurveTo(cx, cy + hr * 0.74, cx, cy + hr * 0.60);
        g.quadraticCurveTo(cx, cy + hr * 0.74, cx + hr * 0.16, cy + hr * 0.68);
        g.stroke();

        // moustaches
        g.setStroke(Color.rgb(120, 112, 110, 0.55));
        g.setLineWidth(hr * 0.035);
        for (int i = -1; i <= 1; i += 2) {
            double sx = cx + i * hr * 0.22;
            g.strokeLine(sx, cy + hr * 0.42, cx + i * hr * 1.15, cy + hr * 0.28);
            g.strokeLine(sx, cy + hr * 0.52, cx + i * hr * 1.15, cy + hr * 0.56);
        }
    }
}
