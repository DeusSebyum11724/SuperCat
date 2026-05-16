package com.supercat.ui;

import javafx.scene.paint.Color;

/**
 * Constantes visuelles centralisees du jeu SuperCat.
 *
 * L'interface (menus, ecrans, HUD) suit une charte calme et minimaliste :
 * fond neutre et clair a faible contraste, palette restreinte, accents
 * doux. Les couleurs du labyrinthe et des personnages restent inchangees :
 * elles appartiennent au jeu lui-meme, pas a l'interface.
 */
public final class Theme {

    private Theme() {
        // classe utilitaire : pas d'instanciation
    }

    // ----- Fenetre / scenes -----
    public static final double SCENE_WIDTH = 860;
    public static final double SCENE_HEIGHT = 660;

    // =====================================================================
    //  INTERFACE  (calme, minimaliste, premium)
    // =====================================================================

    /** Fond des ecrans : creme tres pale, faible contraste, presque plat. */
    public static final String BG_GRADIENT =
            "-fx-background-color: linear-gradient(to bottom, #F3EEE5 0%, #EBE4D6 100%);";

    /** Panneau : champ de couleur creme, coins doux, ombre tres discrete. */
    public static final String CARD_STYLE =
            "-fx-background-color: #FCFAF5; -fx-background-radius: 18; "
            + "-fx-effect: dropshadow(gaussian, rgba(63,59,66,0.10), 22, 0, 0, 7);";

    public static final String ACCENT = "#DB8B6B";        // corail doux
    public static final String ACCENT_DARK = "#C8785A";
    public static final String SECONDARY = "#8B9FB0";     // bleu-gris poudre
    public static final String SECONDARY_DARK = "#76899A";
    public static final String SUCCESS = "#8AAA8E";       // vert sauge doux
    public static final String DANGER = "#D98E6E";        // corail chaud (jamais rouge vif)
    public static final String GOLD = "#D6B271";          // or pale
    public static final String LOCKED = "#C4BFB5";        // gris brumeux
    public static final String TEXT_DARK = "#423E45";     // encre douce (jamais noir pur)
    public static final String TEXT_MUTED = "#9C968B";    // gris brumeux

    /** Bandeau de jeu (HUD) : clair, translucide, discret. */
    public static final String HUD_BG = "rgba(252,250,245,0.94)";

    // =====================================================================
    //  JEU  (labyrinthe et personnages -- inchanges)
    // =====================================================================

    public static final double TILE = 40;
    public static final int COLS = 19;
    public static final int ROWS = 13;
    public static final double CANVAS_WIDTH = TILE * COLS;    // 760
    public static final double CANVAS_HEIGHT = TILE * ROWS;   // 520

    public static final Color FLOOR = Color.web("#EFE6D6");
    public static final Color FLOOR_ALT = Color.web("#E7DBC6");
    public static final Color WALL = Color.web("#8C7CA6");
    public static final Color WALL_LIGHT = Color.web("#A596BC");
    public static final Color WALL_DARK = Color.web("#6E6088");

    public static final Color CAT_BODY = Color.web("#F4A340");
    public static final Color CAT_STRIPE = Color.web("#D67E1A");
    public static final Color CAT_BELLY = Color.web("#FFF1DD");
    public static final Color CAT_PINK = Color.web("#FF9AA2");

    public static final Color DOG_BODY = Color.web("#9B7B52");
    public static final Color DOG_DARK = Color.web("#6B4F2A");
    public static final Color DOG_BELLY = Color.web("#E8D9C0");

    public static final Color FISH_BODY = Color.web("#FFD23F");
    public static final Color FISH_DARK = Color.web("#F0A018");

    public static final Color EXIT_LOCKED = Color.web("#C97A6D");
    public static final Color EXIT_OPEN = Color.web("#7BA793");

    public static final Color BONUS_STAR = Color.web("#9C6FB0");
    public static final Color BONUS_CLOCK = Color.web("#6E9BC4");
}
