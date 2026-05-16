package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.Story;
import com.supercat.model.User;
import com.supercat.service.Settings;
import com.supercat.ui.CatArt;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Random;

/**
 * Ecran du mode Histoire : une carte du voyage de Nora.
 *
 * La carte represente le pays enneige, de la chaumiere de Nora jusqu'au
 * chateau. Les neuf etapes de l'aventure y figurent comme des jalons relies
 * par un sentier ; les etapes franchies sont marquees, l'etape courante
 * accueille Nora. Le texte du chapitre est presente sous la carte avant le
 * lancement du labyrinthe correspondant.
 */
public class StoryController {

    private static final double MAP_W = 700;
    private static final double MAP_H = 250;

    /** Position des neuf etapes, de la chaumiere (en bas) au chateau (en haut). */
    private static final double[][] NODES = {
            { 54, 212}, {140, 170}, {226, 204}, {314, 150},
            {396, 192}, {474, 140}, {542, 178}, {602, 116}, {648, 60}
    };

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();

    public StoryController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        User user = sceneManager.getCurrentUser();
        String username = (user != null) ? user.getUsername() : "";
        int total = Story.chapterCount();
        int progress = Math.min(db.getStoryProgress(username), total);
        boolean finished = progress >= total;
        int chapter = finished ? total - 1 : progress;

        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(764);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setSpacing(14);
        card.setPadding(new Insets(22, 32, 20, 32));

        // --- en-tete ---
        Label caption = new Label(finished ? "FIN DE L'AVENTURE"
                : "CHAPITRE " + (progress + 1) + " SUR " + total);
        caption.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; "
                + "-fx-text-fill: " + Theme.ACCENT + ";");
        Label heading = UIFactory.heading(finished ? "Le Grand Foyer rallume"
                : Story.room(chapter));
        VBox header = finished
                ? new VBox(3, caption, heading)
                : new VBox(5, caption, heading,
                        UIFactory.tag("Defi  ·  " + Story.gameLabel(chapter),
                                "#EFE7DA", Theme.TEXT_MUTED));
        header.setAlignment(Pos.CENTER);

        // --- carte du voyage ---
        Canvas map = buildMap(progress, total);

        // --- panneau narratif ---
        Label text = new Label(finished ? Story.ending() : Story.narrative(chapter));
        text.setWrapText(true);
        text.setMaxWidth(616);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        VBox panel = new VBox(text);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(660);
        panel.setPadding(new Insets(12, 18, 12, 18));
        panel.setStyle("-fx-background-color: #F2ECE1; -fx-background-radius: 14;");

        // --- etapes rejouables : chaque jalon franchi (et l'etape courante)
        //     se touche sur la carte pour relancer son mini-jeu ---
        int playable = Math.min(progress, total - 1);
        map.setOnMouseClicked(e -> {
            for (int i = 0; i <= playable; i++) {
                if (Math.hypot(e.getX() - NODES[i][0], e.getY() - NODES[i][1]) < 26) {
                    sceneManager.showStoryLevel(i);
                    return;
                }
            }
        });
        map.setOnMouseMoved(e -> {
            for (int i = 0; i <= playable; i++) {
                if (Math.hypot(e.getX() - NODES[i][0], e.getY() - NODES[i][1]) < 26) {
                    map.setCursor(Cursor.HAND);
                    return;
                }
            }
            map.setCursor(Cursor.DEFAULT);
        });

        // --- actions ---
        HBox actions;
        if (finished) {
            Button replay = UIFactory.primaryButton("Recommencer l'aventure");
            replay.setOnAction(e -> {
                db.resetStory(username);
                sceneManager.showStory();
            });
            Button home = UIFactory.secondaryButton("Retour a l'accueil");
            home.setOnAction(e -> sceneManager.showHome());
            actions = new HBox(10, replay, home);
        } else {
            Button play = UIFactory.primaryButton("Jouer ce chapitre");
            play.setOnAction(e -> sceneManager.showStoryLevel(progress));
            Button home = UIFactory.secondaryButton("Retour a l'accueil");
            home.setOnAction(e -> sceneManager.showHome());
            actions = new HBox(10, play, home);
        }
        actions.setAlignment(Pos.CENTER);

        card.getChildren().setAll(header, map, panel, actions);
        if (progress >= 1) {
            Label hint = new Label(
                    "Touche une etape deja franchie sur la carte pour la rejouer.");
            hint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
            card.getChildren().add(2, hint);
            UIFactory.fadeInUp(hint, 240);
        }
        root.getChildren().add(card);

        UIFactory.fadeInUp(header, 60);
        UIFactory.fadeInUp(map, 160);
        UIFactory.fadeInUp(panel, 280);
        UIFactory.fadeInUp(actions, 380);
        return root;
    }

    // ===================================================================
    //  Carte animee du voyage
    // ===================================================================
    private Canvas buildMap(int progress, int total) {
        Canvas canvas = new Canvas(MAP_W, MAP_H);
        GraphicsContext g = canvas.getGraphicsContext2D();
        double[][] snow = makeSnow(26, 71L);

        if (Settings.isReducedMotion()) {
            drawMap(g, snow, progress, total, 0);
            return canvas;
        }
        final long[] start = {0};
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (start[0] == 0) {
                    start[0] = now;
                }
                drawMap(g, snow, progress, total, (now - start[0]) / 1_000_000_000.0);
            }
        };
        timer.start();
        canvas.sceneProperty().addListener((obs, was, is) -> {
            if (is == null) {
                timer.stop();
            }
        });
        return canvas;
    }

    private void drawMap(GraphicsContext g, double[][] snow, int progress, int total, double t) {
        g.clearRect(0, 0, MAP_W, MAP_H);
        g.save();
        g.beginPath();
        g.appendSVGPath("M20,0 H680 A20,20 0 0 1 700,20 V230 "
                + "A20,20 0 0 1 680,250 H20 A20,20 0 0 1 0,230 V20 A20,20 0 0 1 20,0 Z");
        g.clip();

        // ciel et sol enneige
        g.setFill(Color.web("#DCDEE9"));
        g.fillRect(0, 0, MAP_W, MAP_H);
        g.setFill(Color.web("#EEF0F2"));
        g.beginPath();
        g.moveTo(0, MAP_H * 0.60);
        g.bezierCurveTo(MAP_W * 0.3, MAP_H * 0.46, MAP_W * 0.7, MAP_H * 0.72,
                MAP_W, MAP_H * 0.50);
        g.lineTo(MAP_W, MAP_H);
        g.lineTo(0, MAP_H);
        g.closePath();
        g.fill();
        g.setFill(Color.web("#F8F6F1"));
        g.beginPath();
        g.moveTo(0, MAP_H * 0.80);
        g.bezierCurveTo(MAP_W * 0.35, MAP_H * 0.70, MAP_W * 0.65, MAP_H * 0.92,
                MAP_W, MAP_H * 0.78);
        g.lineTo(MAP_W, MAP_H);
        g.lineTo(0, MAP_H);
        g.closePath();
        g.fill();

        // sapins enneiges
        double[][] pines = {
                {108, 230, 40}, {186, 244, 30}, {268, 246, 46}, {360, 228, 34},
                {432, 242, 42}, {520, 234, 36}, {588, 246, 44}
        };
        for (double[] p : pines) {
            drawPine(g, p[0], p[1], p[2]);
        }

        // chaumiere de Nora et chateau
        drawCottage(g, 56, 202, 23);
        drawCastle(g, 650, 100, 22, progress >= total);

        // sentier en pointilles entre les etapes
        for (int i = 0; i < NODES.length - 1; i++) {
            boolean traveled = i < progress;
            double[] a = NODES[i];
            double[] b = NODES[i + 1];
            double dist = Math.hypot(b[0] - a[0], b[1] - a[1]);
            int dots = Math.max(2, (int) Math.round(dist / 15));
            g.setFill(Color.web(traveled ? Theme.ACCENT : "#D3CCBE"));
            for (int d = 1; d < dots; d++) {
                double f = (double) d / dots;
                double x = a[0] + (b[0] - a[0]) * f;
                double y = a[1] + (b[1] - a[1]) * f;
                g.fillOval(x - 2.6, y - 2.6, 5.2, 5.2);
            }
        }

        // jalons des etapes
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.setFont(Font.font("System", FontWeight.BOLD, 12));
        for (int i = 0; i < NODES.length; i++) {
            double x = NODES[i][0];
            double y = NODES[i][1];
            boolean done = i < progress;
            boolean current = i == progress;
            if (done) {
                g.setFill(Color.web(Theme.ACCENT));
                g.fillOval(x - 13, y - 13, 26, 26);
                g.setStroke(Color.WHITE);
                g.setLineWidth(2.6);
                g.setLineCap(StrokeLineCap.ROUND);
                g.strokeLine(x - 4.5, y + 0.5, x - 1, y + 4.5);
                g.strokeLine(x - 1, y + 4.5, x + 5, y - 4);
            } else if (current) {
                double pr = 18 + Math.sin(t * 2.4) * 3;
                g.setStroke(Color.web(Theme.ACCENT, 0.40));
                g.setLineWidth(2.4);
                g.strokeOval(x - pr, y - pr, pr * 2, pr * 2);
                g.setFill(Color.web("#FCFAF5"));
                g.fillOval(x - 13, y - 13, 26, 26);
                g.setStroke(Color.web(Theme.ACCENT));
                g.setLineWidth(3);
                g.strokeOval(x - 13, y - 13, 26, 26);
            } else {
                g.setFill(Color.web("#CFC8BB"));
                g.fillOval(x - 12, y - 12, 24, 24);
                g.setFill(Color.web("#FCFAF5"));
                g.fillText(String.valueOf(i + 1), x, y + 1);
            }
        }

        // les chats : Nora a l'etape courante, les trois reunis a la fin
        if (progress >= total) {
            CatArt.draw(g, CatArt.SUIE, 614, 88, 34, t + 1.1);
            CatArt.draw(g, CatArt.GIVRE, 686, 88, 34, t + 2.3);
            CatArt.draw(g, CatArt.NORA, 650, 96, 44, t);
        } else {
            double[] node = NODES[progress];
            CatArt.draw(g, CatArt.NORA, node[0], node[1] + 1, 46, t);
        }

        // neige qui tombe
        g.setFill(Color.web("#FFFFFF", 0.85));
        for (double[] f : snow) {
            double y = (f[1] + t * f[3]) % (MAP_H + 16) - 8;
            double x = f[0] + Math.sin(t * f[4] + f[5]) * 9;
            g.fillOval(x, y, f[2], f[2]);
        }
        g.restore();
    }

    // ----- Elements de decor -----

    private void drawPine(GraphicsContext g, double cx, double baseY, double h) {
        g.setFill(Color.web("#8E7B5A"));
        g.fillRect(cx - h * 0.05, baseY - h * 0.15, h * 0.10, h * 0.15);
        for (int i = 0; i < 3; i++) {
            double w = h * (0.62 - i * 0.13);
            double ty = baseY - h * 0.12 - i * (h * 0.27);
            g.setFill(Color.web(i == 2 ? "#A6C0A2" : "#94B091"));
            g.fillPolygon(
                    new double[]{cx - w / 2, cx + w / 2, cx},
                    new double[]{ty, ty, ty - h * 0.42}, 3);
        }
        // cime enneigee
        g.setFill(Color.web("#FBFAF6"));
        double topY = baseY - h * 0.12 - 2 * (h * 0.27);
        g.fillPolygon(
                new double[]{cx - h * 0.10, cx + h * 0.10, cx},
                new double[]{topY - h * 0.30, topY - h * 0.30, topY - h * 0.42}, 3);
    }

    private void drawCottage(GraphicsContext g, double cx, double baseY, double s) {
        g.setFill(Color.web("#E7DBC4"));
        g.fillRect(cx - s * 0.78, baseY - s * 0.95, s * 1.56, s * 0.95);
        // toit enneige
        g.setFill(Color.web("#F6F2E9"));
        g.fillPolygon(
                new double[]{cx - s, cx + s, cx},
                new double[]{baseY - s * 0.88, baseY - s * 0.88, baseY - s * 1.78}, 3);
        // porte
        g.setFill(Color.web("#9A7350"));
        g.fillRect(cx - s * 0.18, baseY - s * 0.52, s * 0.36, s * 0.52);
        // fenetre eclairee
        g.setFill(Color.web("#F6C96B", 0.45));
        g.fillOval(cx + s * 0.10, baseY - s * 0.86, s * 0.62, s * 0.62);
        g.setFill(Color.web("#F6C96B"));
        g.fillRect(cx + s * 0.24, baseY - s * 0.72, s * 0.34, s * 0.34);
    }

    private void drawCastle(GraphicsContext g, double cx, double baseY, double s, boolean lit) {
        Color stone = Color.web("#A9A4B6");
        Color tower = Color.web("#8C7CA6");
        Color window = lit ? Color.web("#F6C96B") : Color.web("#6E6880");

        // tours laterales
        for (int side = -1; side <= 1; side += 2) {
            double tx = cx + side * s * 1.05;
            g.setFill(stone);
            g.fillRect(tx - s * 0.30, baseY - s * 1.85, s * 0.60, s * 1.85);
            g.setFill(tower);
            g.fillPolygon(
                    new double[]{tx - s * 0.38, tx + s * 0.38, tx},
                    new double[]{baseY - s * 1.85, baseY - s * 1.85, baseY - s * 2.45}, 3);
            g.setFill(Color.web("#FBFAF6"));
            g.fillPolygon(
                    new double[]{tx - s * 0.14, tx + s * 0.14, tx},
                    new double[]{baseY - s * 2.20, baseY - s * 2.20, baseY - s * 2.45}, 3);
        }

        // corps central et creneaux
        g.setFill(stone);
        g.fillRect(cx - s * 0.62, baseY - s * 2.15, s * 1.24, s * 2.15);
        for (int c = 0; c < 4; c++) {
            g.fillRect(cx - s * 0.62 + c * s * 0.40, baseY - s * 2.40, s * 0.26, s * 0.28);
        }
        g.setFill(Color.web("#FBFAF6"));
        g.fillRect(cx - s * 0.62, baseY - s * 2.18, s * 1.24, s * 0.07);

        // porte voutee
        g.setFill(Color.web("#5A5366"));
        g.fillRoundRect(cx - s * 0.26, baseY - s * 0.78, s * 0.52, s * 0.78, s * 0.5, s * 0.5);

        // fenetres
        if (lit) {
            g.setFill(Color.web("#F6C96B", 0.4));
            g.fillOval(cx - s * 0.48, baseY - s * 1.78, s * 0.96, s * 0.96);
        }
        g.setFill(window);
        g.fillRect(cx - s * 0.30, baseY - s * 1.55, s * 0.22, s * 0.40);
        g.fillRect(cx + s * 0.08, baseY - s * 1.55, s * 0.22, s * 0.40);

        // fanion
        g.setStroke(Color.web("#7A7286"));
        g.setLineWidth(2);
        g.strokeLine(cx, baseY - s * 2.40, cx, baseY - s * 3.00);
        g.setFill(Color.web(Theme.ACCENT));
        g.fillPolygon(
                new double[]{cx, cx + s * 0.55, cx},
                new double[]{baseY - s * 3.00, baseY - s * 2.82, baseY - s * 2.64}, 3);
    }

    private double[][] makeSnow(int count, long seed) {
        Random rng = new Random(seed);
        double[][] flakes = new double[count][6];
        for (int i = 0; i < count; i++) {
            flakes[i][0] = rng.nextDouble() * MAP_W;
            flakes[i][1] = rng.nextDouble() * MAP_H;
            flakes[i][2] = 2 + rng.nextDouble() * 3;
            flakes[i][3] = 9 + rng.nextDouble() * 16;
            flakes[i][4] = 0.5 + rng.nextDouble() * 0.8;
            flakes[i][5] = rng.nextDouble() * 6.28;
        }
        return flakes;
    }
}
