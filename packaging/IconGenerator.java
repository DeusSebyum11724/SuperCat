import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Generateur de l'icone macOS de SuperCat.
 *
 * Dessine en Java2D un jeu d'images PNG (16 a 1024 px) dans un dossier
 * ".iconset", que l'outil macOS "iconutil" transforme ensuite en .icns.
 * L'icone reprend l'univers du jeu : Nora la chatte rousse sur un fond de
 * nuit d'hiver, avec un poisson d'or.
 *
 * Lancement : java IconGenerator.java <dossier-iconset>
 */
public class IconGenerator {

    // palette (cohérente avec Theme du jeu)
    private static final Color BODY = new Color(0xF4, 0xA3, 0x40);
    private static final Color SHADE = new Color(0xD6, 0x7E, 0x1A);
    private static final Color BELLY = new Color(0xFF, 0xF1, 0xDD);
    private static final Color PINK = new Color(0xF2, 0xA0, 0x9C);
    private static final Color EYE = new Color(0x5E, 0x8C, 0x4E);
    private static final Color INK = new Color(0x2A, 0x26, 0x2C);
    private static final Color FISH = new Color(0xFF, 0xD2, 0x3F);
    private static final Color FISH_DARK = new Color(0xF0, 0xA0, 0x18);

    public static void main(String[] args) throws Exception {
        String outDir = (args.length > 0) ? args[0] : "SuperCat.iconset";
        File dir = new File(outDir);
        dir.mkdirs();
        int[] bases = {16, 32, 128, 256, 512};
        for (int base : bases) {
            ImageIO.write(render(base), "png",
                    new File(dir, "icon_" + base + "x" + base + ".png"));
            ImageIO.write(render(base * 2), "png",
                    new File(dir, "icon_" + base + "x" + base + "@2x.png"));
        }
        System.out.println("Iconset genere dans " + dir.getAbsolutePath());
    }

    private static BufferedImage render(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        draw(g, s);
        g.dispose();
        return img;
    }

    private static void draw(Graphics2D g, double s) {
        double m = s * 0.055;
        double inner = s - 2 * m;
        double arc = inner * 0.45;
        RoundRectangle2D bg = new RoundRectangle2D.Double(m, m, inner, inner, arc, arc);

        // fond : degrade nuit d'hiver, du crepuscule a la rose chaude
        g.setPaint(new GradientPaint(0, (float) m, new Color(0x4C, 0x43, 0x68),
                0, (float) (s - m), new Color(0xA8, 0x8C, 0x90)));
        g.fill(bg);

        Shape clip = g.getClip();
        g.setClip(bg);

        // reflet doux en haut (effet verre)
        g.setPaint(new GradientPaint(0, (float) m, new Color(255, 255, 255, 38),
                0, (float) (m + inner * 0.4f), new Color(255, 255, 255, 0)));
        g.fill(bg);

        double cx = s * 0.5;
        double cy = s * 0.45;

        // halo lumineux derriere le chat
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy),
                (float) (s * 0.44), new float[]{0f, 1f},
                new Color[]{new Color(255, 232, 198, 165), new Color(255, 232, 198, 0)}));
        g.fill(new Ellipse2D.Double(cx - s * 0.44, cy - s * 0.44, s * 0.88, s * 0.88));

        // flocons de neige
        g.setColor(new Color(255, 255, 255, 170));
        double[][] snow = {
                {0.19, 0.21, 0.012}, {0.83, 0.17, 0.014}, {0.72, 0.33, 0.009},
                {0.15, 0.52, 0.011}, {0.87, 0.58, 0.013}, {0.27, 0.74, 0.010},
                {0.80, 0.80, 0.012}, {0.50, 0.13, 0.009}
        };
        for (double[] f : snow) {
            double r = f[2] * s;
            g.fill(new Ellipse2D.Double(f[0] * s - r, f[1] * s - r, 2 * r, 2 * r));
        }

        drawCat(g, cx, cy, s * 0.255);
        drawFish(g, s * 0.5, s * 0.832, s * 0.156);

        g.setClip(clip);
    }

    private static void drawCat(Graphics2D g, double cx, double cy, double r) {
        // oreilles
        g.setColor(BODY);
        g.fill(tri(cx - r * 0.34, cy - r * 0.74, cx - r * 0.98, cy - r * 0.50,
                cx - r * 0.76, cy - r * 1.48));
        g.fill(tri(cx + r * 0.34, cy - r * 0.74, cx + r * 0.98, cy - r * 0.50,
                cx + r * 0.76, cy - r * 1.48));
        g.setColor(PINK);
        g.fill(tri(cx - r * 0.42, cy - r * 0.70, cx - r * 0.80, cy - r * 0.54,
                cx - r * 0.68, cy - r * 1.18));
        g.fill(tri(cx + r * 0.42, cy - r * 0.70, cx + r * 0.80, cy - r * 0.54,
                cx + r * 0.68, cy - r * 1.18));

        // tete
        double hw = r * 1.10;
        double hh = r * 1.02;
        g.setColor(BODY);
        g.fill(new Ellipse2D.Double(cx - hw, cy - hh, 2 * hw, 2 * hh));

        // museau clair
        g.setColor(BELLY);
        g.fill(new Ellipse2D.Double(cx - r * 0.64, cy - r * 0.02, r * 1.28, r * 0.94));

        // rayures du front
        g.setColor(SHADE);
        g.setStroke(new BasicStroke((float) (r * 0.135), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(cx, cy - r * 0.94, cx, cy - r * 0.62));
        g.draw(new Line2D.Double(cx - r * 0.28, cy - r * 0.88, cx - r * 0.21, cy - r * 0.58));
        g.draw(new Line2D.Double(cx + r * 0.28, cy - r * 0.88, cx + r * 0.21, cy - r * 0.58));

        // yeux
        double eyeDx = r * 0.45;
        double eyeY = cy - r * 0.03;
        double eyeW = r * 0.44;
        double eyeH = r * 0.52;
        for (int i = -1; i <= 1; i += 2) {
            double ex = cx + i * eyeDx;
            g.setColor(EYE);
            g.fill(new Ellipse2D.Double(ex - eyeW / 2, eyeY - eyeH / 2, eyeW, eyeH));
            g.setColor(INK);
            g.fill(new Ellipse2D.Double(ex - eyeW * 0.22, eyeY - eyeH * 0.40,
                    eyeW * 0.44, eyeH * 0.80));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(ex + eyeW * 0.01, eyeY - eyeH * 0.34,
                    eyeW * 0.24, eyeH * 0.26));
        }

        // nez
        g.setColor(PINK);
        g.fill(tri(cx - r * 0.13, cy + r * 0.30, cx + r * 0.13, cy + r * 0.30,
                cx, cy + r * 0.47));

        // bouche
        g.setColor(SHADE);
        g.setStroke(new BasicStroke((float) (r * 0.072), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(cx, cy + r * 0.47, cx, cy + r * 0.60));
        g.draw(new QuadCurve2D.Double(cx, cy + r * 0.60, cx - r * 0.16, cy + r * 0.76,
                cx - r * 0.28, cy + r * 0.58));
        g.draw(new QuadCurve2D.Double(cx, cy + r * 0.60, cx + r * 0.16, cy + r * 0.76,
                cx + r * 0.28, cy + r * 0.58));

        // moustaches
        g.setColor(new Color(255, 255, 255, 205));
        g.setStroke(new BasicStroke((float) (r * 0.05), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        for (int i = -1; i <= 1; i += 2) {
            double sx = cx + i * r * 0.32;
            g.draw(new Line2D.Double(sx, cy + r * 0.30, cx + i * r * 1.34, cy + r * 0.12));
            g.draw(new Line2D.Double(sx, cy + r * 0.44, cx + i * r * 1.36, cy + r * 0.44));
            g.draw(new Line2D.Double(sx, cy + r * 0.58, cx + i * r * 1.32, cy + r * 0.74));
        }
    }

    private static void drawFish(Graphics2D g, double fx, double fy, double r) {
        g.setColor(new Color(255, 210, 63, 70));
        g.fill(new Ellipse2D.Double(fx - r * 1.05, fy - r * 1.05, r * 2.1, r * 2.1));
        g.setColor(FISH_DARK);
        g.fill(tri(fx - r * 0.28, fy, fx - r * 0.82, fy - r * 0.46,
                fx - r * 0.82, fy + r * 0.46));
        g.setColor(FISH);
        g.fill(new Ellipse2D.Double(fx - r * 0.58, fy - r * 0.42, r * 1.22, r * 0.84));
        g.setColor(FISH_DARK);
        g.fill(tri(fx - r * 0.08, fy - r * 0.40, fx + r * 0.26, fy - r * 0.40,
                fx + r * 0.09, fy - r * 0.70));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(fx + r * 0.20, fy - r * 0.20, r * 0.30, r * 0.30));
        g.setColor(INK);
        g.fill(new Ellipse2D.Double(fx + r * 0.30, fy - r * 0.11, r * 0.14, r * 0.14));
    }

    private static Path2D tri(double x1, double y1, double x2, double y2,
                              double x3, double y3) {
        Path2D path = new Path2D.Double();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.closePath();
        return path;
    }
}
