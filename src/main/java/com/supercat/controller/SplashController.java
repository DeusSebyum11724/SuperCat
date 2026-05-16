package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Ecran-titre anime affiche au lancement de l'application.
 *
 * Inspire de l'esthetique de "Monument Valley" : un fond ou derivent
 * lentement des formes geometriques translucides, et une apparition en
 * fondu enchaine de la mascotte, du logo et de l'invite. Un clic ou une
 * touche fait passer a l'ecran de connexion.
 */
public class SplashController {

    private final SceneManager sceneManager;
    private final StackPane root;
    private AnimationTimer backdrop;
    private boolean advancing = false;

    public SplashController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private StackPane build() {
        StackPane screen = UIFactory.screen();

        // --- fond anime : formes geometriques en derive lente ---
        Canvas canvas = new Canvas(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        startBackdrop(canvas.getGraphicsContext2D());

        // --- mascotte ---
        Canvas mascot = UIFactory.catFace(150);

        // --- logo bicolore "SuperCat" ---
        Label superPart = new Label("Super");
        superPart.setStyle("-fx-font-size: 58px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        Label catPart = new Label("Cat");
        catPart.setStyle("-fx-font-size: 58px; -fx-font-weight: 800; -fx-text-fill: " + Theme.ACCENT + ";");
        HBox wordmark = new HBox(superPart, catPart);
        wordmark.setAlignment(Pos.CENTER);

        // --- trait sous le logo ---
        Region line = new Region();
        line.setMinSize(150, 3);
        line.setMaxSize(150, 3);
        line.setStyle("-fx-background-color: " + Theme.ACCENT + "; -fx-background-radius: 2;");

        // --- sous-titre discret ---
        Label tagline = new Label("Le labyrinthe du chat");
        tagline.setStyle("-fx-font-size: 14px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        VBox content = new VBox(16, mascot, wordmark, line, tagline);
        content.setAlignment(Pos.CENTER);

        // --- invite clignotante ---
        Label prompt = new Label("Appuyer pour commencer");
        prompt.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        StackPane.setAlignment(prompt, Pos.BOTTOM_CENTER);
        StackPane.setMargin(prompt, new Insets(0, 0, 46, 0));

        screen.getChildren().addAll(canvas, content, prompt);
        playIntro(mascot, wordmark, line, tagline, prompt);

        // clic ou touche : passage a la connexion
        screen.setOnMouseClicked(e -> advance());
        sceneManager.getScene().setOnKeyPressed(e -> advance());
        PauseTransition autoSkip = new PauseTransition(Duration.seconds(9));
        autoSkip.setOnFinished(e -> advance());
        autoSkip.play();
        return screen;
    }

    /** Orchestration de l'apparition des elements du titre. */
    private void playIntro(Canvas mascot, HBox wordmark, Region line,
                           Label tagline, Label prompt) {
        mascot.setOpacity(0);
        mascot.setScaleX(0.7);
        mascot.setScaleY(0.7);
        FadeTransition mascotFade = new FadeTransition(Duration.millis(640), mascot);
        mascotFade.setToValue(1);
        ScaleTransition mascotScale = new ScaleTransition(Duration.millis(640), mascot);
        mascotScale.setToX(1);
        mascotScale.setToY(1);
        ParallelTransition mascotIn = new ParallelTransition(mascotFade, mascotScale);
        mascotIn.setDelay(Duration.millis(160));
        mascotIn.setInterpolator(Interpolator.EASE_OUT);
        mascotIn.setOnFinished(e -> UIFactory.breathe(mascot, 1.05, 2300));
        mascotIn.play();

        UIFactory.fadeInUp(wordmark, 480);
        UIFactory.fadeInUp(tagline, 1080);

        line.setScaleX(0);
        ScaleTransition lineGrow = new ScaleTransition(Duration.millis(520), line);
        lineGrow.setToX(1);
        lineGrow.setDelay(Duration.millis(820));
        lineGrow.setInterpolator(Interpolator.EASE_OUT);
        lineGrow.play();

        prompt.setOpacity(0);
        FadeTransition promptIn = new FadeTransition(Duration.millis(520), prompt);
        promptIn.setToValue(1);
        promptIn.setDelay(Duration.millis(1500));
        promptIn.setOnFinished(e -> {
            FadeTransition blink = new FadeTransition(Duration.millis(1100), prompt);
            blink.setFromValue(1);
            blink.setToValue(0.32);
            blink.setAutoReverse(true);
            blink.setCycleCount(Animation.INDEFINITE);
            blink.play();
        });
        promptIn.play();
    }

    /** Passe a l'ecran de connexion (en fondu), une seule fois. */
    private void advance() {
        if (advancing) {
            return;
        }
        advancing = true;
        if (backdrop != null) {
            backdrop.stop();
        }
        FadeTransition out = new FadeTransition(Duration.millis(340), root);
        out.setToValue(0);
        out.setOnFinished(e -> sceneManager.showLogin());
        out.play();
    }

    // =====================================================================
    //  Fond anime : formes geometriques translucides en derive lente
    // =====================================================================
    private void startBackdrop(GraphicsContext gc) {
        final int n = 7;
        final double[] x = new double[n];
        final double[] y = new double[n];
        final double[] size = new double[n];
        final double[] speed = new double[n];
        final double[] phase = new double[n];
        final double[] rot = new double[n];
        final double[] rotSpeed = new double[n];
        final int[] type = new int[n];
        final Color[] color = new Color[n];
        final Color[] palette = {
                Color.web("#E08A6F"), Color.web("#7E9DB6"),
                Color.web("#B7A6CE"), Color.web("#E0B25C")
        };
        for (int i = 0; i < n; i++) {
            x[i] = Math.random() * Theme.SCENE_WIDTH;
            y[i] = Math.random() * Theme.SCENE_HEIGHT;
            size[i] = 70 + Math.random() * 130;
            speed[i] = 0.12 + Math.random() * 0.34;
            phase[i] = Math.random() * Math.PI * 2;
            rot[i] = Math.random() * Math.PI * 2;
            rotSpeed[i] = (Math.random() - 0.5) * 0.006;
            type[i] = (Math.random() < 0.5) ? 0 : 1;
            color[i] = palette[(int) (Math.random() * palette.length)];
        }
        backdrop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
                for (int i = 0; i < n; i++) {
                    y[i] -= speed[i];
                    phase[i] += 0.01;
                    rot[i] += rotSpeed[i];
                    if (y[i] < -size[i]) {
                        y[i] = Theme.SCENE_HEIGHT + size[i];
                        x[i] = Math.random() * Theme.SCENE_WIDTH;
                    }
                    double drawX = x[i] + Math.sin(phase[i]) * 22;
                    gc.setFill(Color.color(color[i].getRed(), color[i].getGreen(),
                            color[i].getBlue(), 0.085));
                    if (type[i] == 0) {
                        gc.fillOval(drawX - size[i] / 2, y[i] - size[i] / 2, size[i], size[i]);
                    } else {
                        double[] px = new double[3];
                        double[] py = new double[3];
                        for (int k = 0; k < 3; k++) {
                            double a = rot[i] + k * 2 * Math.PI / 3;
                            px[k] = drawX + Math.cos(a) * size[i] / 2;
                            py[k] = y[i] + Math.sin(a) * size[i] / 2;
                        }
                        gc.fillPolygon(px, py, 3);
                    }
                }
            }
        };
        backdrop.start();
    }
}
