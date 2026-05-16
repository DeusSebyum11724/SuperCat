package com.supercat.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Fabrique de composants graphiques deja stylises. Centraliser la creation
 * des boutons, champs et libelles garantit une interface coherente et evite
 * la repetition de code CSS dans tous les ecrans.
 */
public final class UIFactory {

    private UIFactory() {
        // classe utilitaire
    }

    // ----- Libelles -----
    public static Label title(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 40px; -fx-font-weight: 900; "
                + "-fx-text-fill: " + Theme.ACCENT + ";");
        return l;
    }

    public static Label heading(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 22px; -fx-font-weight: bold; "
                + "-fx-text-fill: " + Theme.TEXT_DARK + ";");
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

    // ----- Boutons -----
    public static Button primaryButton(String text) {
        return styledButton(text, Theme.ACCENT, Theme.ACCENT_DARK, "white");
    }

    public static Button secondaryButton(String text) {
        return styledButton(text, "#E6EAED", "#D2D9DD", Theme.TEXT_DARK);
    }

    public static Button dangerButton(String text) {
        return styledButton(text, Theme.DANGER, "#A93226", "white");
    }

    public static Button successButton(String text) {
        return styledButton(text, Theme.SUCCESS, "#1E8449", "white");
    }

    private static Button styledButton(String text, String bg, String bgHover, String fg) {
        Button b = new Button(text);
        String shape = "-fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 15px; "
                + "-fx-font-weight: bold; -fx-padding: 11 24 11 24; -fx-text-fill: " + fg + ";";
        b.setStyle("-fx-background-color: " + bg + ";" + shape);
        // effet de survol : changement de teinte + leger agrandissement (transition)
        b.setOnMouseEntered(e -> {
            b.setStyle("-fx-background-color: " + bgHover + ";" + shape);
            b.setScaleX(1.04);
            b.setScaleY(1.04);
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
        field.setStyle("-fx-background-radius: 9; -fx-border-radius: 9; -fx-font-size: 14px; "
                + "-fx-padding: 10; -fx-border-color: #D4DADE; -fx-border-width: 1.5; "
                + "-fx-background-color: #F7F9FA;");
        field.setPrefHeight(42);
    }

    // ----- Conteneurs -----

    /** Carte blanche arrondie avec ombre portee. */
    public static VBox card() {
        VBox v = new VBox(14);
        v.setStyle(Theme.CARD_STYLE);
        v.setPadding(new Insets(30));
        v.setAlignment(Pos.CENTER);
        return v;
    }

    /** Conteneur racine d'un ecran : fond degrade plein cadre. */
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

        // oreilles
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

        // tete
        g.setFill(Theme.CAT_BODY);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setFill(Theme.CAT_BELLY);
        g.fillOval(cx - r * 0.85, cy - r * 0.15, r * 1.7, r * 1.05);

        // yeux
        g.setFill(Color.web("#2B2B2B"));
        g.fillOval(cx - r * 0.52, cy - r * 0.4, r * 0.34, r * 0.46);
        g.fillOval(cx + r * 0.18, cy - r * 0.4, r * 0.34, r * 0.46);
        g.setFill(Color.WHITE);
        g.fillOval(cx - r * 0.44, cy - r * 0.34, r * 0.13, r * 0.13);
        g.fillOval(cx + r * 0.26, cy - r * 0.34, r * 0.13, r * 0.13);

        // nez
        g.setFill(Theme.CAT_PINK);
        g.fillPolygon(new double[]{cx - r * 0.14, cx + r * 0.14, cx},
                      new double[]{cy + r * 0.15, cy + r * 0.15, cy + r * 0.34}, 3);

        // moustaches
        g.setStroke(Color.web("#2B2B2B"));
        g.setLineWidth(Math.max(1, size * 0.013));
        g.strokeLine(cx - r * 0.25, cy + r * 0.28, cx - r * 1.2, cy + r * 0.08);
        g.strokeLine(cx - r * 0.25, cy + r * 0.4, cx - r * 1.2, cy + r * 0.45);
        g.strokeLine(cx + r * 0.25, cy + r * 0.28, cx + r * 1.2, cy + r * 0.08);
        g.strokeLine(cx + r * 0.25, cy + r * 0.4, cx + r * 1.2, cy + r * 0.45);
        return canvas;
    }
}
