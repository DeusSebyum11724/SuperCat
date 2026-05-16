package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.LevelLoader;
import com.supercat.engine.Story;
import com.supercat.model.User;
import com.supercat.service.Settings;
import com.supercat.ui.CatArt;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.util.Map;
import java.util.Random;

/**
 * Page d'accueil du joueur.
 *
 * La page presente une scene animee ou se retrouvent les trois chats de
 * l'aventure -- Nora la rousse, Suie le noir et Givre le blanc et gris --
 * puis trois portes d'entree vers le jeu : le mode Histoire, la campagne et
 * le mode sans fin.
 */
public class HomeController {

    private static final double HERO_W = 682;
    private static final double HERO_H = 196;

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();

    public HomeController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        User user = sceneManager.getCurrentUser();
        String username = (user != null) ? user.getUsername() : "joueur";

        Map<Integer, Integer> bests = db.getLevelBests(username);
        int endlessBest = db.getEndlessBest(username);
        int storyProgress = db.getStoryProgress(username);
        int completed = bests.size();
        boolean endlessUnlocked = completed >= 3;
        int campaignCount = LevelLoader.getCampaignCount();

        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(742);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setSpacing(16);
        card.setPadding(new Insets(24, 30, 22, 30));

        // --- en-tete ---
        Label heading = UIFactory.title("SuperCat");
        Label who = new Label("Bonjour, " + username);
        who.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        VBox header = new VBox(2, heading, who);
        header.setAlignment(Pos.CENTER);

        // --- scene animee des trois chats ---
        Canvas hero = buildHero();

        // --- les trois modes de jeu ---
        String storyLine = (storyProgress >= Story.chapterCount())
                ? "Aventure terminee"
                : "Chapitre " + (storyProgress + 1) + " sur " + Story.chapterCount();
        VBox storyCard = modeCard(0, "Mode Histoire", storyLine, true,
                sceneManager::showStory);
        VBox campaignCard = modeCard(1, "La Campagne",
                completed + " / " + campaignCount + " niveaux", true,
                sceneManager::showCampaign);
        VBox endlessCard = modeCard(2, "Mode sans fin",
                endlessUnlocked ? "Record : " + endlessBest + " salles"
                                : "Apres 3 niveaux",
                endlessUnlocked, sceneManager::showEndless);
        HBox modes = new HBox(14, storyCard, campaignCard, endlessCard);
        modes.setAlignment(Pos.CENTER);

        // --- pied de page ---
        Button profile = UIFactory.secondaryButton("Profil");
        profile.setOnAction(e -> sceneManager.showProfile());
        Button leaderboard = UIFactory.secondaryButton("Classement");
        leaderboard.setOnAction(e -> sceneManager.showLeaderboard());
        Button settings = UIFactory.secondaryButton("Parametres");
        settings.setOnAction(e -> sceneManager.showSettings());
        Button logout = UIFactory.secondaryButton("Deconnexion");
        logout.setOnAction(e -> sceneManager.logout());
        HBox footer = new HBox(9, profile, leaderboard, settings, logout);
        footer.setAlignment(Pos.CENTER);

        card.getChildren().setAll(header, hero, modes, footer);
        root.getChildren().add(card);

        UIFactory.fadeInUp(header, 60);
        UIFactory.fadeInUp(hero, 160);
        UIFactory.fadeInUp(modes, 280);
        UIFactory.fadeInUp(footer, 400);
        return root;
    }

    // ===================================================================
    //  Scene animee des trois chats
    // ===================================================================
    private Canvas buildHero() {
        Canvas canvas = new Canvas(HERO_W, HERO_H);
        GraphicsContext g = canvas.getGraphicsContext2D();
        double[][] snow = makeSnow(22, HERO_W, HERO_H, 41L);

        if (Settings.isReducedMotion()) {
            drawHero(g, snow, 0);
            return canvas;
        }
        final long[] start = {0};
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (start[0] == 0) {
                    start[0] = now;
                }
                drawHero(g, snow, (now - start[0]) / 1_000_000_000.0);
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

    private void drawHero(GraphicsContext g, double[][] snow, double t) {
        g.clearRect(0, 0, HERO_W, HERO_H);

        // ciel d'hiver
        g.setFill(Color.web("#E9EAF0"));
        g.fillRoundRect(0, 0, HERO_W, HERO_H, 20, 20);
        g.setFill(Color.web("#EFE9DC"));
        g.fillRoundRect(0, HERO_H * 0.52, HERO_W, HERO_H * 0.48, 20, 20);

        // congere
        g.setFill(Color.web("#FBFAF6"));
        g.fillOval(HERO_W * 0.04, HERO_H * 0.66, HERO_W * 0.92, HERO_H * 0.9);

        // les trois chats
        CatArt.draw(g, CatArt.SUIE, HERO_W * 0.22, HERO_H * 0.92, 104, t + 1.3);
        CatArt.draw(g, CatArt.GIVRE, HERO_W * 0.78, HERO_H * 0.92, 104, t + 2.7);
        CatArt.draw(g, CatArt.NORA, HERO_W * 0.50, HERO_H * 0.99, 132, t);

        // neige qui tombe
        g.setFill(Color.web("#FFFFFF", 0.85));
        for (double[] f : snow) {
            double y = (f[1] + t * f[3]) % (HERO_H + 16) - 8;
            double x = f[0] + Math.sin(t * f[4] + f[5]) * 9;
            g.fillOval(x, y, f[2], f[2]);
        }
    }

    private double[][] makeSnow(int count, double w, double h, long seed) {
        Random rng = new Random(seed);
        double[][] flakes = new double[count][6];
        for (int i = 0; i < count; i++) {
            flakes[i][0] = rng.nextDouble() * w;             // x de base
            flakes[i][1] = rng.nextDouble() * h;             // y de base
            flakes[i][2] = 2 + rng.nextDouble() * 3;         // rayon
            flakes[i][3] = 9 + rng.nextDouble() * 16;        // vitesse de chute
            flakes[i][4] = 0.5 + rng.nextDouble() * 0.8;     // frequence de derive
            flakes[i][5] = rng.nextDouble() * 6.28;          // phase
        }
        return flakes;
    }

    // ===================================================================
    //  Cartes de mode
    // ===================================================================
    private VBox modeCard(int icon, String title, String subtitle,
                          boolean enabled, Runnable onClick) {
        Canvas glyph = modeIcon(icon, enabled);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: "
                + (enabled ? Theme.TEXT_DARK : Theme.TEXT_MUTED) + ";");
        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-font-size: 11.5px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        VBox box = new VBox(6, glyph, titleLabel, subLabel);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(206);
        box.setPrefWidth(206);
        box.setPadding(new Insets(16, 12, 16, 12));

        String idle = "-fx-background-color: #F2ECE1; -fx-background-radius: 16;";
        String hover = "-fx-background-color: #EBE3D4; -fx-background-radius: 16; -fx-cursor: hand;";
        if (enabled) {
            box.setStyle(idle + " -fx-cursor: hand;");
            box.setOnMouseEntered(e -> box.setStyle(hover));
            box.setOnMouseExited(e -> box.setStyle(idle + " -fx-cursor: hand;"));
            box.setOnMouseClicked(e -> onClick.run());
        } else {
            box.setStyle(idle);
            box.setOpacity(0.55);
        }
        return box;
    }

    /** Petit pictogramme au trait : 0 histoire, 1 campagne, 2 sans fin. */
    private Canvas modeIcon(int kind, boolean enabled) {
        Canvas canvas = new Canvas(40, 40);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setStroke(Color.web(enabled ? Theme.ACCENT : Theme.TEXT_MUTED));
        g.setLineWidth(2.3);
        g.setLineCap(StrokeLineCap.ROUND);
        if (kind == 0) {                       // livre ouvert (histoire)
            g.strokeLine(20, 11, 20, 31);
            g.beginPath();
            g.moveTo(20, 11);
            g.quadraticCurveTo(13, 7, 7, 12);
            g.lineTo(7, 30);
            g.quadraticCurveTo(13, 25, 20, 29);
            g.stroke();
            g.beginPath();
            g.moveTo(20, 11);
            g.quadraticCurveTo(27, 7, 33, 12);
            g.lineTo(33, 30);
            g.quadraticCurveTo(27, 25, 20, 29);
            g.stroke();
        } else if (kind == 1) {                // ligne de stations (campagne)
            g.strokeLine(8, 20, 32, 20);
            for (double x : new double[]{8, 20, 32}) {
                g.setFill(Color.web(enabled ? Theme.ACCENT : Theme.TEXT_MUTED));
                g.fillOval(x - 3.4, 16.6, 6.8, 6.8);
            }
        } else {                               // boucle infinie (sans fin)
            g.strokeOval(8, 14, 12, 12);
            g.strokeOval(20, 14, 12, 12);
        }
        return canvas;
    }
}
