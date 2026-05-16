package com.supercat.ui;

import com.supercat.service.Settings;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Fabrique de composants graphiques deja stylises.
 *
 * Charte calme et minimaliste : typographie sobre, boutons doux et
 * tactiles, panneaux clairs, animations discretes. Centraliser la creation
 * des composants garantit une interface coherente sur tous les ecrans.
 */
public final class UIFactory {

    private UIFactory() {
        // classe utilitaire
    }

    // ----- Libelles -----
    public static Label title(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 34px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        return l;
    }

    public static Label heading(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        return l;
    }

    public static Label subtitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        return l;
    }

    public static Label body(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        return l;
    }

    public static Label error(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + Theme.DANGER + ";");
        return l;
    }

    /** Petite etiquette arrondie (difficulte, statut...). */
    public static Label tag(String text, String backgroundColor, String textColor) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor + "; "
                + "-fx-background-radius: 10; -fx-padding: 3 11 3 11; "
                + "-fx-font-size: 11px; -fx-font-weight: 600;");
        return l;
    }

    // ----- Boutons (doux, tactiles) -----
    public static Button primaryButton(String text) {
        return pill(text, Theme.ACCENT, "#E69D81", "white");
    }

    public static Button secondaryButton(String text) {
        return pill(text, "#ECE5D9", "#F3EEE4", Theme.TEXT_DARK);
    }

    public static Button dangerButton(String text) {
        return pill(text, Theme.DANGER, "#E29E80", "white");
    }

    public static Button successButton(String text) {
        return pill(text, Theme.SUCCESS, "#99B79C", "white");
    }

    private static Button pill(String text, String background, String backgroundHover, String foreground) {
        Button b = new Button(text);
        String shape = "-fx-background-radius: 21; -fx-cursor: hand; -fx-font-size: 14px; "
                + "-fx-font-weight: 600; -fx-padding: 11 24 11 24; -fx-text-fill: " + foreground + ";";
        String idle = "-fx-background-color: " + background + ";" + shape;
        String hovered = "-fx-background-color: " + backgroundHover + ";" + shape
                + "-fx-effect: dropshadow(gaussian, rgba(63,59,66,0.18), 12, 0, 0, 3);";
        b.setStyle(idle);
        // survol / pression : mise a l'echelle douce (1.02 / 0.97)
        b.setOnMouseEntered(e -> {
            b.setStyle(hovered);
            scaleTo(b, 1.02, 150);
        });
        b.setOnMouseExited(e -> {
            b.setStyle(idle);
            scaleTo(b, 1.0, 170);
        });
        b.setOnMousePressed(e -> scaleTo(b, 0.97, 90));
        b.setOnMouseReleased(e -> scaleTo(b, b.isHover() ? 1.02 : 1.0, 150));
        return b;
    }

    /** Bouton plat, discret, ressemblant a un lien. */
    public static Button linkButton(String text) {
        Button b = new Button(text);
        String base = "-fx-background-color: transparent; -fx-cursor: hand; "
                + "-fx-font-size: 13px; -fx-padding: 4 6 4 6; -fx-text-fill: ";
        b.setStyle(base + Theme.TEXT_MUTED + ";");
        b.setOnMouseEntered(e -> b.setStyle(base + Theme.ACCENT + ";"));
        b.setOnMouseExited(e -> b.setStyle(base + Theme.TEXT_MUTED + ";"));
        return b;
    }

    // ----- Champs de saisie -----
    public static TextField textField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        styleField(tf);
        return tf;
    }

    public static PasswordField passwordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        styleField(pf);
        return pf;
    }

    private static void styleField(TextField field) {
        field.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-font-size: 14px; "
                + "-fx-padding: 11; -fx-border-color: #E1D9CA; -fx-border-width: 1.4; "
                + "-fx-background-color: #F4EFE6; -fx-prompt-text-fill: #B0AA9E;");
        field.setPrefHeight(44);
    }

    // ----- Conteneurs -----

    /** Panneau creme arrondi, ombre douce. */
    public static VBox card() {
        VBox v = new VBox(14);
        v.setStyle(Theme.CARD_STYLE);
        v.setPadding(new Insets(32));
        v.setAlignment(Pos.CENTER);
        return v;
    }

    /** Conteneur racine d'un ecran (taille de conception fixe : 860 x 660). */
    public static StackPane screen() {
        StackPane root = new StackPane();
        root.setStyle(Theme.BG_GRADIENT);
        root.setMinSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        root.setPrefSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        root.setMaxSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        return root;
    }

    // ----- Animations -----

    /** Apparition d'un element : fondu, leger glissement et mise a l'echelle. */
    public static void fadeInUp(Node node, double delayMillis) {
        if (Settings.isReducedMotion()) {
            node.setOpacity(1);
            node.setTranslateY(0);
            return;
        }
        node.setOpacity(0);
        node.setTranslateY(14);
        node.setScaleX(0.985);
        node.setScaleY(0.985);
        FadeTransition fade = new FadeTransition(Duration.millis(380), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(380), node);
        slide.setFromY(14);
        slide.setToY(0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(380), node);
        scale.setFromX(0.985);
        scale.setFromY(0.985);
        scale.setToX(1.0);
        scale.setToY(1.0);
        ParallelTransition animation = new ParallelTransition(fade, slide, scale);
        animation.setDelay(Duration.millis(delayMillis));
        animation.setInterpolator(Interpolator.EASE_OUT);
        animation.play();
    }

    /** Respiration continue : battement d'echelle tres discret. */
    public static void breathe(Node node, double maxScale, double durationMillis) {
        if (Settings.isReducedMotion()) {
            return;
        }
        ScaleTransition pulse = new ScaleTransition(Duration.millis(durationMillis), node);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(maxScale);
        pulse.setToY(maxScale);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.play();
    }

    private static void scaleTo(Node node, double target, double millis) {
        ScaleTransition st = new ScaleTransition(Duration.millis(millis), node);
        st.setToX(target);
        st.setToY(target);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.play();
    }

    /** Petit visage de chat dessine (mascotte de l'application). */
    public static Canvas catFace(double size) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext g = canvas.getGraphicsContext2D();
        double cx = size / 2.0;
        double cy = size * 0.55;
        double r = size * 0.33;

        g.setFill(Theme.CAT_BODY);
        g.fillPolygon(new double[]{cx - r * 0.95, cx - r * 0.15, cx - r * 1.05},
                      new double[]{cy - r * 0.5, cy - r * 0.8, cy - r * 1.55}, 3);
        g.fillPolygon(new double[]{cx + r * 0.95, cx + r * 0.15, cx + r * 1.05},
                      new double[]{cy - r * 0.5, cy - r * 0.8, cy - r * 1.55}, 3);
        g.setFill(Theme.CAT_PINK);
        g.fillPolygon(new double[]{cx - r * 0.78, cx - r * 0.35, cx - r * 0.88},
                      new double[]{cy - r * 0.62, cy - r * 0.82, cy - r * 1.2}, 3);
        g.fillPolygon(new double[]{cx + r * 0.78, cx + r * 0.35, cx + r * 0.88},
                      new double[]{cy - r * 0.62, cy - r * 0.82, cy - r * 1.2}, 3);

        g.setFill(Theme.CAT_BODY);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setFill(Theme.CAT_BELLY);
        g.fillOval(cx - r * 0.85, cy - r * 0.15, r * 1.7, r * 1.05);

        g.setFill(Color.web("#2B2B2B"));
        g.fillOval(cx - r * 0.52, cy - r * 0.4, r * 0.34, r * 0.46);
        g.fillOval(cx + r * 0.18, cy - r * 0.4, r * 0.34, r * 0.46);
        g.setFill(Color.WHITE);
        g.fillOval(cx - r * 0.44, cy - r * 0.34, r * 0.13, r * 0.13);
        g.fillOval(cx + r * 0.26, cy - r * 0.34, r * 0.13, r * 0.13);

        g.setFill(Theme.CAT_PINK);
        g.fillPolygon(new double[]{cx - r * 0.14, cx + r * 0.14, cx},
                      new double[]{cy + r * 0.15, cy + r * 0.15, cy + r * 0.34}, 3);

        g.setStroke(Color.web("#2B2B2B"));
        g.setLineWidth(Math.max(1, size * 0.013));
        g.strokeLine(cx - r * 0.25, cy + r * 0.28, cx - r * 1.2, cy + r * 0.08);
        g.strokeLine(cx - r * 0.25, cy + r * 0.4, cx - r * 1.2, cy + r * 0.45);
        g.strokeLine(cx + r * 0.25, cy + r * 0.28, cx + r * 1.2, cy + r * 0.08);
        g.strokeLine(cx + r * 0.25, cy + r * 0.4, cx + r * 1.2, cy + r * 0.45);
        return canvas;
    }
}
