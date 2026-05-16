package com.supercat.engine;

/**
 * Contenu narratif du mode Histoire de SuperCat.
 *
 * Le mode Histoire suit Nora, une petite chatte errante qui a trouve refuge
 * dans une vieille maison pour l'hiver. Les poissons d'or -- ces lueurs qui
 * gardent la maison au chaud -- se sont disperses dans toutes les pieces.
 * Nora doit les rassembler, piece apres piece, avant que le froid ne gagne,
 * en evitant les vieux chiens de garde.
 *
 * Chaque chapitre se joue comme un labyrinthe ; le texte narratif est
 * affiche avant chaque chapitre.
 */
public final class Story {

    private Story() {
        // donnees uniquement
    }

    /** Nom des pieces traversees, chapitre par chapitre. */
    private static final String[] ROOMS = {
            "Le Grenier",
            "L'Escalier",
            "Le Grand Salon",
            "La Bibliotheque",
            "La Cuisine",
            "La Cave",
            "Le Foyer"
    };

    /** Texte narratif de chaque chapitre (court, calme). */
    private static final String[] CHAPTERS = {
            "L'hiver s'est installe sur la vieille maison, ou Nora, une petite "
                    + "chatte errante, a trouve refuge. Les poissons d'or, ces lueurs "
                    + "qui gardent la maison au chaud, se sont disperses. Tout commence "
                    + "ici, sous les combles glaces.",
            "Nora descend l'escalier derobe, marche apres marche. Les lueurs "
                    + "dorees ont roule plus bas, au coeur de la maison endormie.",
            "Le grand salon dort sous la poussiere. La lune entre par les hautes "
                    + "fenetres et se pose, douce, sur les poissons d'or egares.",
            "Des couloirs entiers de livres oublies. Un vieux chien sommeille "
                    + "entre les rayonnages : Nora avance sans le moindre bruit.",
            "La cuisine est froide et silencieuse. Sur le carrelage pale, les "
                    + "poissons d'or brillent comme de petites braises.",
            "Voici les pieces les plus profondes, les plus sombres. Nora "
                    + "frissonne, mais la chaleur de toute la maison en depend.",
            "Il ne manque plus qu'une lueur. Nora la rapporte jusqu'au grand "
                    + "foyer, tout au coeur de la maison."
    };

    /** Texte affiche une fois l'histoire terminee. */
    private static final String ENDING =
            "Une a une, les lueurs dorees sont revenues. La vieille maison "
            + "rayonne d'une chaleur douce, et Nora, enfin, n'a plus froid. "
            + "Dehors, la neige tombe sans bruit ; dedans, tout est calme.";

    public static int chapterCount() {
        return ROOMS.length;
    }

    public static String room(int chapter) {
        return ROOMS[Math.max(0, Math.min(ROOMS.length - 1, chapter))];
    }

    public static String narrative(int chapter) {
        return CHAPTERS[Math.max(0, Math.min(CHAPTERS.length - 1, chapter))];
    }

    public static String ending() {
        return ENDING;
    }
}
