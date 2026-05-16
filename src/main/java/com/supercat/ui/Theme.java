package com.supercat.ui;

import javafx.scene.paint.Color;

/**
 * Constantes visuelles centralisees du jeu SuperCat.
 *
 * La charte graphique s'inspire de l'esthetique des jeux "Monument Valley"
 * (degrades pastel, douceur, minimalisme) et "Mini Metro" (formes
 * geometriques epurees) pour l'interface utilisateur.
 */
public final class Theme {

    private Theme() {
        // classe utilitaire : pas d'instanciation
    }

    // ----- Fenetre / scenes -----
    public static final double SCENE_WIDTH = 860;
    public static final double SCENE_HEIGHT = 660;

    // ----- Interface : degrade de fond serein (lavande -> rose -> peche) -----
    public static final String BG_GRADIENT =
            "-fx-background-color: linear-gradient(to bottom, "
            + "#B7A6CE 0%, #D6B7C2 55%, #F0CDB6 100%);";

    // ----- Carte (panneau) : creme doux, coins tres arrondis, ombre legere -----
    public static final String CARD_STYLE =
            "-fx-background-color: #FBF7F0; -fx-background-radius: 24; "
            + "-fx-effect: dropshadow(gaussian, rgba(74,68,88,0.22), 26, 0.12, 0, 10);";

    // ----- Couleurs de l'interface -----
    public static final String ACCENT = "#E08A6F";        // corail doux
    public static final String ACCENT_DARK = "#CC7159";
    public static final String SECONDARY = "#7E9DB6";     // bleu poudre
    public static final String SECONDARY_DARK = "#6A8AA3";
    public static final String SUCCESS = "#7BA793";       // sauge douce
    public static final String DANGER = "#C97A6D";        // terracotta sombre
    public static final String GOLD = "#E0B25C";          // ambre doux
    public static final String LOCKED = "#BCB4C8";        // gris lavande (verrouille)
    public static final String TEXT_DARK = "#4A4458";     // prune sombre
    public static final String TEXT_MUTED = "#9A93A8";    // lavande grisee
    public static final String HUD_BG = "#3E3852";        // prune profond (bandeau de jeu)

    // ----- Grille du labyrinthe -----
    public static final double TILE = 40;
    public static final int COLS = 19;
    public static final int ROWS = 13;
    public static final double CANVAS_WIDTH = TILE * COLS;    // 760
    public static final double CANVAS_HEIGHT = TILE * ROWS;   // 520

    // ----- Couleurs du sol et des murs (teintes douces, harmonisees) -----
    public static final Color FLOOR = Color.web("#EFE6D6");
    public static final Color FLOOR_ALT = Color.web("#E7DBC6");
    public static final Color WALL = Color.web("#8C7CA6");
    public static final Color WALL_LIGHT = Color.web("#A596BC");
    public static final Color WALL_DARK = Color.web("#6E6088");

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
    public static final Color FISH_DARK = Color.web("#F0A018");

    // ----- Couleurs de la sortie -----
    public static final Color EXIT_LOCKED = Color.web("#C97A6D");
    public static final Color EXIT_OPEN = Color.web("#7BA793");

    // ----- Couleurs des bonus -----
    public static final Color BONUS_STAR = Color.web("#9C6FB0");
    public static final Color BONUS_CLOCK = Color.web("#6E9BC4");
}
