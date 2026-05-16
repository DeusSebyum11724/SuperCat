package com.supercat.ui;

import javafx.scene.paint.Color;

/**
 * Constantes visuelles centralisees du jeu SuperCat : dimensions de la
 * grille du labyrinthe, couleurs de l'interface et couleurs des objets
 * animes. Regrouper ces valeurs ici garantit une charte graphique coherente.
 */
public final class Theme {

    private Theme() {
        // classe utilitaire : pas d'instanciation
    }

    // ----- Fenetre / scenes -----
    public static final double SCENE_WIDTH = 820;
    public static final double SCENE_HEIGHT = 640;

    // ----- Styles interface (CSS en ligne) -----
    public static final String BG_GRADIENT =
            "-fx-background-color: linear-gradient(to bottom right, #2C3E50, #46637E);";
    public static final String CARD_STYLE =
            "-fx-background-color: white; -fx-background-radius: 18; "
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 22, 0.2, 0, 8);";

    public static final String ACCENT = "#E8821E";       // orange (couleur du chat)
    public static final String ACCENT_DARK = "#C76A12";
    public static final String SUCCESS = "#27AE60";
    public static final String DANGER = "#C0392B";
    public static final String TEXT_DARK = "#2C3E50";
    public static final String TEXT_MUTED = "#8A98A5";

    // ----- Grille du labyrinthe -----
    public static final double TILE = 40;
    public static final int COLS = 19;
    public static final int ROWS = 13;
    public static final double CANVAS_WIDTH = TILE * COLS;    // 760
    public static final double CANVAS_HEIGHT = TILE * ROWS;   // 520

    // ----- Couleurs du sol et des murs -----
    public static final Color FLOOR = Color.web("#FBE9D0");
    public static final Color FLOOR_ALT = Color.web("#F3DBBC");
    public static final Color WALL = Color.web("#3A5068");
    public static final Color WALL_LIGHT = Color.web("#54718F");
    public static final Color WALL_DARK = Color.web("#27374A");

    // ----- Couleurs du chat (joueur) -----
    public static final Color CAT_BODY = Color.web("#F4A340");
    public static final Color CAT_STRIPE = Color.web("#D67E1A");
    public static final Color CAT_BELLY = Color.web("#FFF1DD");
    public static final Color CAT_PINK = Color.web("#FF9AA2");

    // ----- Couleurs des chiens (ennemis) -----
    public static final Color DOG_BODY = Color.web("#9B7B52");
    public static final Color DOG_DARK = Color.web("#6B4F2A");
    public static final Color DOG_BELLY = Color.web("#E8D9C0");

    // ----- Couleurs des poissons d'or -----
    public static final Color FISH_BODY = Color.web("#FFD23F");
    public static final Color FISH_DARK = Color.web("#FF9F1C");

    // ----- Couleurs de la sortie -----
    public static final Color EXIT_LOCKED = Color.web("#C0392B");
    public static final Color EXIT_OPEN = Color.web("#27AE60");

    // ----- Couleurs des bonus -----
    public static final Color BONUS_STAR = Color.web("#9B59B6");
    public static final Color BONUS_CLOCK = Color.web("#2E86DE");
}
