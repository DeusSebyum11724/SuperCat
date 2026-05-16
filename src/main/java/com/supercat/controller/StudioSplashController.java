package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.ui.Theme;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Ecran-titre du studio "Tsuki & Nora Studios", affiche au tout premier
 * lancement, avant l'ecran-titre du jeu.
 *
 * Ambiance volontairement cosy : une nuit douce, une lune chaude, des
 * etoiles qui scintillent et un petit chat assis qui la contemple.
 * ("Tsuki" = la lune, "Nora" = le chat errant, en japonais.)
 */
public class StudioSplashController {

    private final SceneManager sceneManager;
    private final StackPane root;
    private AnimationTimer scene;
    private boolean advancing = false;

    private final double[] starX = new double[30];
    private final double[] starY = new double[30];
    private final double[] starSize = new double[30];
    private final double[] starPhase = new double[30];
    private double time = 0;

    public StudioSplashController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private StackPane build() {
        StackPane screen = new StackPane();
        screen.setMinSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        screen.setPrefSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        screen.setMaxSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        screen.setStyle("-fx-background-color: linear-gradient(to bottom, "
                + "#2A2440 0%, #3E3550 55%, #574752 100%);");

        for (int i = 0; i < starX.length; i++) {
            starX[i] = 60 + Math.random() * (Theme.SCENE_WIDTH - 120);
            starY[i] = 50 + Math.random() * 360;
            starSize[i] = 1.4 + Math.random() * 2.4;
            starPhase[i] = Math.random() * Math.PI * 2;
        }

        Canvas canvas = new Canvas(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        startScene(canvas.getGraphicsContext2D());

        Label studio = new Label("Tsuki & Nora");
        studio.setStyle("-fx-font-size: 40px; -fx-font-weight: 800; -fx-text-fill: #F4E6CC;");
        Label suffix = new Label("S T U D I O S");
        suffix.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #C6A98F;");
        VBox name = new VBox(6, studio, suffix);
        name.setAlignment(Pos.CENTER);
        StackPane.setAlignment(name, Pos.BOTTOM_CENTER);
        StackPane.setMargin(name, new Insets(0, 0, 96, 0));

        screen.getChildren().addAll(canvas, name);

        name.setOpacity(0);
        FadeTransition nameIn = new FadeTransition(Duration.millis(900), name);
        nameIn.setFromValue(0);
        nameIn.setToValue(1);
        nameIn.setDelay(Duration.millis(650));
        nameIn.play();

        screen.setOnMouseClicked(e -> advance());
        sceneManager.getScene().setOnKeyPressed(e -> advance());
        PauseTransition autoSkip = new PauseTransition(Duration.seconds(4.0));
        autoSkip.setOnFinished(e -> advance());
        autoSkip.play();
        return screen;
    }

    private void advance() {
        if (advancing) {
            return;
        }
        advancing = true;
        if (scene != null) {
            scene.stop();
        }
        FadeTransition out = new FadeTransition(Duration.millis(420), root);
        out.setToValue(0);
        out.setOnFinished(e -> sceneManager.showSplash());
        out.play();
    }

    // =====================================================================
    //  Scene nocturne animee
    // =====================================================================
    private void startScene(GraphicsContext gc) {
        scene = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time += 0.016;
                gc.clearRect(0, 0, Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
                drawStars(gc);
                drawMoon(gc);
                drawCat(gc);
            }
        };
        scene.start();
    }

    private void drawStars(GraphicsContext gc) {
        for (int i = 0; i < starX.length; i++) {
            double twinkle = 0.5 + 0.5 * Math.sin(time * 2.4 + starPhase[i]);
            gc.setFill(Color.color(1.0, 0.97, 0.86, 0.25 + twinkle * 0.6));
            double s = starSize[i];
            gc.fillOval(starX[i] - s, starY[i] - s, s * 2, s * 2);
        }
    }

    private void drawMoon(GraphicsContext gc) {
        double mx = 642;
        double my = 168;
        double glow = 0.16 + 0.05 * Math.sin(time * 1.3);
        gc.setFill(Color.color(0.96, 0.90, 0.74, glow));
        gc.fillOval(mx - 96, my - 96, 192, 192);
        gc.setFill(Color.color(0.95, 0.89, 0.72, glow + 0.12));
        gc.fillOval(mx - 66, my - 66, 132, 132);

        gc.setFill(Color.web("#F4E4C1"));
        gc.fillOval(mx - 52, my - 52, 104, 104);
        gc.setFill(Color.web("#E7D2A8"));
        gc.fillOval(mx - 26, my - 30, 20, 20);
        gc.fillOval(mx + 12, my + 6, 28, 28);
        gc.fillOval(mx - 6, my + 26, 13, 13);
    }

    /** Petit chat assis qui contemple la lune (silhouette). */
    private void drawCat(GraphicsContext gc) {
        Color fur = Color.web("#221C30");
        double tailWag = Math.sin(time * 1.1) * 7;

        // queue enroulee
        gc.setStroke(fur);
        gc.setLineWidth(15);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.beginPath();
        gc.moveTo(330, 500);
        gc.quadraticCurveTo(286, 506, 300 + tailWag, 446);
        gc.stroke();

        gc.setFill(fur);
        // hanches (assises)
        gc.fillOval(322, 420, 122, 96);
        // corps qui s'eleve
        gc.fillOval(356, 332, 92, 130);
        // poitrine / pattes avant
        gc.fillOval(392, 414, 46, 96);
        // tete
        gc.fillOval(382, 300, 80, 78);
        // oreilles
        gc.fillPolygon(new double[]{392, 398, 426}, new double[]{318, 268, 312}, 3);
        gc.fillPolygon(new double[]{426, 456, 460}, new double[]{312, 266, 318}, 3);

        // yeux tournes vers la lune (deux petites lueurs chaudes)
        gc.setFill(Color.web("#F4E4C1"));
        gc.fillOval(420, 330, 7, 9);
        gc.fillOval(437, 329, 7, 9);
    }
}
