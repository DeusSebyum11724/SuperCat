package com.supercat.engine;

/**
 * Contenu narratif du mode Histoire de SuperCat.
 *
 * L'histoire suit Nora, une petite chatte rousse errante. L'hiver venu, une
 * tempete a disperse les poissons d'or -- ces lueurs qui gardent les
 * maisons au chaud -- a travers tout le pays enneige, jusqu'au vieux
 * chateau dont le Grand Foyer s'est eteint.
 *
 * Nora entreprend la traversee. En chemin elle rencontre deux compagnons :
 * Suie, un chat noir aux yeux d'or qui connait les recoins sombres, et
 * Givre, un chat blanc et gris qui connait les sentiers glaces. Ensemble,
 * ils rejoignent le chateau pour rallumer le Grand Foyer.
 *
 * Chaque chapitre se joue comme un labyrinthe ; le texte narratif est
 * presente sur la carte du voyage avant chaque etape.
 */
public final class Story {

    private Story() {
        // donnees uniquement
    }

    /** Lieux traverses, etape par etape. */
    private static final String[] ROOMS = {
            "La Chaumiere",
            "Le Hameau",
            "Le Vieux Moulin",
            "Le Lac Gele",
            "La Foret Blanche",
            "Le Pont de Pierre",
            "La Tour de Guet",
            "Les Portes du Chateau",
            "Le Grand Foyer"
    };

    /** Type de lieu, pour l'affichage sur la carte (maison, etape, chateau). */
    public enum Place { COTTAGE, WAYPOINT, CASTLE }

    private static final Place[] PLACES = {
            Place.COTTAGE, Place.COTTAGE, Place.WAYPOINT, Place.WAYPOINT,
            Place.WAYPOINT, Place.WAYPOINT, Place.WAYPOINT, Place.CASTLE, Place.CASTLE
    };

    /** Texte narratif de chaque chapitre (court, calme). */
    private static final String[] CHAPTERS = {
            "La tempete est passee. Dans sa petite chaumiere, Nora la chatte "
                    + "rousse ne sent plus aucune chaleur : les poissons d'or se "
                    + "sont envoles. Elle resserre son courage et ouvre la porte "
                    + "sur la neige.",
            "Le hameau voisin est silencieux, ses maisons sombres et froides. "
                    + "Les lueurs dorees ont roule de seuil en seuil, toujours plus "
                    + "loin vers les collines.",
            "Pres du vieux moulin, deux yeux d'or brillent dans l'ombre : c'est "
                    + "Suie, un chat noir prudent. Il connait les recoins sombres et "
                    + "decide d'accompagner Nora.",
            "Le lac est entierement gele. Suie ouvre la voie sur la glace, et "
                    + "Nora le suit, pas a pas, vers les lueurs prises dans le "
                    + "givre.",
            "Dans la foret blanche, une silhouette pale attend sous un sapin : "
                    + "Givre, un chat blanc et gris, calme et sur. Il connait les "
                    + "sentiers et rejoint le voyage.",
            "Tous les trois franchissent le vieux pont de pierre. En contrebas, "
                    + "la riviere dort sous la glace et renvoie l'eclat des "
                    + "poissons d'or.",
            "La tour de guet veille sur la vallee. Ses escaliers en spirale "
                    + "gardent jalousement les dernieres lueurs egarees.",
            "Voici les grandes portes du chateau. Le froid y est mordant, mais "
                    + "le Grand Foyer n'est plus tres loin.",
            "Au coeur du chateau, le Grand Foyer attend sa derniere lueur. Nora, "
                    + "Suie et Givre avancent ensemble pour le rallumer."
    };

    /** Texte affiche une fois l'histoire terminee. */
    private static final String ENDING =
            "La derniere lueur retrouve le Grand Foyer, et une chaleur douce "
            + "se repand du chateau jusqu'aux plus petites chaumieres. Nora, "
            + "Suie et Givre se posent devant les flammes. Dehors, la neige "
            + "tombe sans bruit ; l'hiver, desormais, sera doux.";

    public static int chapterCount() {
        return ROOMS.length;
    }

    public static String room(int chapter) {
        return ROOMS[clamp(chapter)];
    }

    public static String narrative(int chapter) {
        return CHAPTERS[clamp(chapter)];
    }

    public static Place place(int chapter) {
        return PLACES[clamp(chapter)];
    }

    public static String ending() {
        return ENDING;
    }

    private static int clamp(int chapter) {
        return Math.max(0, Math.min(ROOMS.length - 1, chapter));
    }
}
