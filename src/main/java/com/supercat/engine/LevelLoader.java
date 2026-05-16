package com.supercat.engine;

import com.supercat.model.Bonus;
import com.supercat.model.Cat;
import com.supercat.model.Dog;
import com.supercat.model.Exit;
import com.supercat.model.Fish;
import com.supercat.model.Wall;
import com.supercat.ui.Theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generateur procedural de niveaux.
 *
 * Chaque niveau est genere automatiquement a partir de son indice : le meme
 * indice produit toujours exactement le meme labyrinthe (generation
 * deterministe), ce qui permet de rejouer un niveau et d'ameliorer son score.
 *
 *  - Indices 0 a 11  : les 12 niveaux de la campagne.
 *  - Indices 12 et + : le mode sans fin (genere a l'infini, difficulte
 *    croissante).
 *
 * La construction garantit que le labyrinthe est toujours entierement
 * connecte : couloirs (lignes ou colonnes ouvertes) relies par des passages,
 * avec au moins un passage par mur. Le chat peut donc toujours atteindre
 * tous les poissons et la sortie.
 */
public final class LevelLoader {

    private LevelLoader() {
        // classe utilitaire
    }

    private static final double T = Theme.TILE;
    private static final int COLS = Theme.COLS;
    private static final int ROWS = Theme.ROWS;
    private static final int CAMPAIGN_COUNT = 12;

    private static final String[] NAMES = {
            "Le Jardin", "Les Galeries", "Le Vieux Grenier", "La Citerne",
            "Les Cloitres", "Le Dedale", "La Cave Oubliee", "Les Toits",
            "Le Sanctuaire", "Les Catacombes", "La Tour", "Le Palais"
    };

    /** Nombre de niveaux de la campagne. */
    public static int getCampaignCount() {
        return CAMPAIGN_COUNT;
    }

    /** Nom d'un niveau. */
    public static String getLevelName(int index) {
        if (index >= 0 && index < CAMPAIGN_COUNT) {
            return NAMES[index];
        }
        return "Salle sans fin " + (index - CAMPAIGN_COUNT + 1);
    }

    /** Etiquette de difficulte d'un niveau. */
    public static String getDifficultyLabel(int index) {
        if (index <= 2) {
            return "Facile";
        }
        if (index <= 5) {
            return "Moyen";
        }
        if (index <= 8) {
            return "Difficile";
        }
        if (index <= 11) {
            return "Expert";
        }
        return "Extreme";
    }

    /** Genere (de facon deterministe) le niveau d'indice donne. */
    public static Level load(int index) {
        Random rng = new Random(index * 2654435761L + 12345L);
        boolean horizontal = (index % 2 == 0);
        int diff = index;

        int fishCount = clamp(5 + diff, 5, 18);
        int dogCount = clamp(1 + (diff * 2) / 3, 1, 11);
        double dogSpeed = Math.min(1.4 + diff * 0.13, 3.2);
        int passagesPerWall = clamp(4 - diff / 3, 1, 4);
        int bonusCount = clamp(1 + diff / 4, 1, 3);
        int timeLimit = Math.min(55 + fishCount * 5 + dogCount * 3, 140);

        boolean[][] grid = horizontal
                ? buildHorizontal(rng, passagesPerWall)
                : buildVertical(rng, passagesPerWall);

        int catR = 1;
        int catC = 1;
        int exitR = 11;
        int exitC = 17;

        // cellules de couloir disponibles (hors chat et sortie), melangees
        List<int[]> pool = new ArrayList<>();
        for (int[] cell : corridorCells(horizontal)) {
            boolean isCat = (cell[0] == catR && cell[1] == catC);
            boolean isExit = (cell[0] == exitR && cell[1] == exitC);
            if (!isCat && !isExit) {
                pool.add(cell);
            }
        }
        Collections.shuffle(pool, rng);

        int p = 0;
        List<int[]> fishCells = new ArrayList<>();
        while (fishCells.size() < fishCount && p < pool.size()) {
            fishCells.add(pool.get(p++));
        }
        List<int[]> dogCells = new ArrayList<>();
        while (dogCells.size() < dogCount && p < pool.size()) {
            int[] cell = pool.get(p++);
            // un chien ne doit pas apparaitre trop pres du chat (depart equitable)
            if (Math.abs(cell[0] - catR) + Math.abs(cell[1] - catC) >= 4) {
                dogCells.add(cell);
            }
        }
        List<int[]> bonusCells = new ArrayList<>();
        while (bonusCells.size() < bonusCount && p < pool.size()) {
            bonusCells.add(pool.get(p++));
        }

        return assemble(index, getLevelName(index), getDifficultyLabel(index), timeLimit,
                dogSpeed, horizontal, grid, catR, catC, exitR, exitC,
                fishCells, dogCells, bonusCells);
    }

    // =====================================================================
    //  Construction de la grille de murs
    // =====================================================================
    private static boolean[][] buildHorizontal(Random rng, int passagesPerWall) {
        boolean[][] wall = new boolean[ROWS][COLS];
        addBorders(wall);
        int[] oddCols = {1, 3, 5, 7, 9, 11, 13, 15, 17};
        for (int r = 2; r <= ROWS - 2; r += 2) {          // rangees-mur 2,4,6,8,10
            for (int c = 1; c <= COLS - 2; c++) {
                wall[r][c] = true;
            }
            for (int col : pickDistinct(rng, oddCols, passagesPerWall)) {
                wall[r][col] = false;
            }
        }
        return wall;
    }

    private static boolean[][] buildVertical(Random rng, int passagesPerWall) {
        boolean[][] wall = new boolean[ROWS][COLS];
        addBorders(wall);
        int[] oddRows = {1, 3, 5, 7, 9, 11};
        for (int c = 2; c <= COLS - 2; c += 2) {          // colonnes-mur 2,4,...,16
            for (int r = 1; r <= ROWS - 2; r++) {
                wall[r][c] = true;
            }
            for (int row : pickDistinct(rng, oddRows, passagesPerWall)) {
                wall[row][c] = false;
            }
        }
        return wall;
    }

    private static void addBorders(boolean[][] wall) {
        for (int r = 0; r < ROWS; r++) {
            wall[r][0] = true;
            wall[r][COLS - 1] = true;
        }
        for (int c = 0; c < COLS; c++) {
            wall[0][c] = true;
            wall[ROWS - 1][c] = true;
        }
    }

    /** Liste des cellules de couloir (toutes connectees entre elles). */
    private static List<int[]> corridorCells(boolean horizontal) {
        List<int[]> cells = new ArrayList<>();
        if (horizontal) {
            for (int r = 1; r <= ROWS - 2; r += 2) {
                for (int c = 1; c <= COLS - 2; c++) {
                    cells.add(new int[]{r, c});
                }
            }
        } else {
            for (int c = 1; c <= COLS - 2; c += 2) {
                for (int r = 1; r <= ROWS - 2; r++) {
                    cells.add(new int[]{r, c});
                }
            }
        }
        return cells;
    }

    private static int[] pickDistinct(Random rng, int[] options, int count) {
        List<Integer> list = new ArrayList<>();
        for (int option : options) {
            list.add(option);
        }
        Collections.shuffle(list, rng);
        int n = Math.min(count, list.size());
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    // =====================================================================
    //  Instanciation des objets du niveau
    // =====================================================================
    private static Level assemble(int index, String name, String difficulty, int timeLimit,
                                  double dogSpeed, boolean horizontal, boolean[][] grid,
                                  int catR, int catC, int exitR, int exitC,
                                  List<int[]> fishCells, List<int[]> dogCells,
                                  List<int[]> bonusCells) {

        List<Wall> walls = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c]) {
                    walls.add(new Wall(c * T, r * T, T));
                }
            }
        }

        Cat cat = new Cat(centered(catC, Cat.SIZE), centered(catR, Cat.SIZE));
        Exit exit = new Exit(centered(exitC, Exit.SIZE), centered(exitR, Exit.SIZE));

        List<Fish> fish = new ArrayList<>();
        for (int[] cell : fishCells) {
            fish.add(new Fish(centered(cell[1], Fish.SIZE), centered(cell[0], Fish.SIZE)));
        }

        List<Dog> dogs = new ArrayList<>();
        for (int[] cell : dogCells) {
            Dog dog = new Dog(centered(cell[1], Dog.SIZE), centered(cell[0], Dog.SIZE), COLS * T);
            dog.setSpeed(dogSpeed);
            if (horizontal) {
                dog.setHorizontal();
            } else {
                dog.setVertical();
            }
            dogs.add(dog);
        }

        List<Bonus> bonuses = new ArrayList<>();
        for (int i = 0; i < bonusCells.size(); i++) {
            int[] cell = bonusCells.get(i);
            Bonus.Type type = (i % 2 == 0) ? Bonus.Type.POINTS : Bonus.Type.TIME;
            bonuses.add(new Bonus(centered(cell[1], Bonus.SIZE), centered(cell[0], Bonus.SIZE), type));
        }

        return new Level(index, name, difficulty, timeLimit, cat, exit, walls, fish, dogs, bonuses);
    }

    private static double centered(int cell, double objectSize) {
        return cell * T + (T - objectSize) / 2.0;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
