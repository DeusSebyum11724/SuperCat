package com.supercat.ui;

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
 * Le style s'inspire de "Monument Valley" (douceur, minimalisme, formes
 * arrondies) et de "Mini Metro" (badges geometriques epures).
 */
public final class UIFactory {

    private UIFactory() {
        // classe utilitaire
    }

    // ----- Libelles -----
    public static Label title(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 36px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        return l;
    }

    public static Label heading(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        return l;
    }

    public static Label subtitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
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
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + Theme.DANGER + ";");
        return l;
    }

    /** Petit badge arrondi (etiquette de difficulte, statut...). */
    public static Label tag(String text, String backgroundColor, String textColor) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor + "; "
                + "-fx-background-radius: 11; -fx-padding: 3 12 3 12; "
                + "-fx-font-size: 11px; -fx-font-weight: bold;");
        return l;
    }

    // ----- Boutons (forme arrondie "pilule") -----
    public static Button primaryButton(String text) {
        return pill(text, Theme.ACCENT, Theme.ACCENT_DARK, "white");
    }

    public static Button secondaryButton(String text) {
        return pill(text, "#EBE4D9", "#DDD4C5", Theme.TEXT_DARK);
    }

    public static Button dangerButton(String text) {
        return pill(text, Theme.DANGER, "#B5685B", "white");
    }

    public static Button successButton(String text) {
        return pill(text, Theme.SUCCESS, "#688F7D", "white");
    }

    private static Button pill(String text, String bg, String bgHover, String fg) {
        Button b = new Button(text);
        String shape = "-fx-background-radius: 24; -fx-cursor: hand; -fx-font-size: 15px; "
                + "-fx-font-weight: bold; -fx-padding: 12 26 12 26; -fx-text-fill: " + fg + ";";
        b.setStyle("-fx-background-color: " + bg + ";" + shape);
        b.setOnMouseEntered(e -> {
            b.setStyle("-fx-background-color: " + bgHover + ";" + shape);
            b.setScaleX(1.03);
            b.setScaleY(1.03);
        });
        b.setOnMouseExited(e -> {
            b.setStyle("-fx-background-color: " + bg + ";" + shape);
            b.setScaleX(1.0);
            b.setScaleY(1.0);
        });
        return b;
    }

    /** Bouton plat ressemblant a un lien hypertexte. */
    public static Button linkButton(String text) {
        Button b = new Button(text);
        String base = "-fx-background-color: transparent; -fx-cursor: hand; -fx-underline: true; "
                + "-fx-font-size: 13px; -fx-padding: 4; -fx-text-fill: ";
        b.setStyle(base + Theme.ACCENT + ";");
        b.setOnMouseEntered(e -> b.setStyle(base + Theme.ACCENT_DARK + ";"));
        b.setOnMouseExited(e -> b.setStyle(base + Theme.ACCENT + ";"));
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
        field.setStyle("-fx-background-radius: 13; -fx-border-radius: 13; -fx-font-size: 14px; "
                + "-fx-padding: 11; -fx-border-color: #DDD3C4; -fx-border-width: 1.5; "
                + "-fx-background-color: #F3EDE2;");
        field.setPrefHeight(44);
    }

    // ----- Conteneurs -----

    /** Carte creme arrondie avec une ombre douce. */
    public static VBox card() {
        VBox v = new VBox(14);
        v.setStyle(Theme.CARD_STYLE);
        v.setPadding(new Insets(32));
        v.setAlignment(Pos.CENTER);
        return v;
    }

    /** Conteneur racine d'un ecran : fond degrade serein plein cadre. */
    public static StackPane screen() {
        StackPane root = new StackPane();
        root.setStyle(Theme.BG_GRADIENT);
        root.setPrefSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        return root;
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

    // ----- Animations -----

    /** Anime l'apparition d'un noeud : fondu accompagne d'un leger glissement. */
    public static void fadeInUp(Node node, double delayMillis) {
        node.setOpacity(0);
        node.setTranslateY(18);
        FadeTransition fade = new FadeTransition(Duration.millis(460), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(460), node);
        slide.setFromY(18);
        slide.setToY(0);
        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.setDelay(Duration.millis(delayMillis));
        animation.setInterpolator(Interpolator.EASE_OUT);
        animation.play();
    }

    /** Anime un noeud en respiration continue (battement d'echelle discret). */
    public static void breathe(Node node, double maxScale, double durationMillis) {
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
}
