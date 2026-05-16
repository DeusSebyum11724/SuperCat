package com.supercat.engine;

import com.supercat.model.Bonus;
import com.supercat.model.Cat;
import com.supercat.model.Dog;
import com.supercat.model.Exit;
import com.supercat.model.Fish;
import com.supercat.model.Wall;
import com.supercat.ui.Theme;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructeur des niveaux du jeu. Le jeu comporte 3 labyrinthes (plusieurs
 * niveaux = fonctionnalite avancee de Type A).
 *
 * Les labyrinthes sont generes de facon structurelle plutot que saisis
 * caractere par caractere : un labyrinthe est compose de couloirs (lignes ou
 * colonnes entierement ouvertes) relies par des passages perces dans les
 * murs. Cette construction GARANTIT que le labyrinthe est toujours
 * entierement connecte : le chat peut atteindre tous les poissons et la
 * sortie, quel que soit le niveau.
 */
public final class LevelLoader {

    private LevelLoader() {
        // classe utilitaire
    }

    private static final double T = Theme.TILE;
    private static final int COLS = Theme.COLS;
    private static final int ROWS = Theme.ROWS;
    private static final int LEVEL_COUNT = 3;

    /** Nombre total de niveaux disponibles. */
    public static int getLevelCount() {
        return LEVEL_COUNT;
    }

    /** Charge et instancie le niveau d'indice donne (0, 1 ou 2). */
    public static Level load(int index) {
        return switch (index) {
            case 0 -> level1();
            case 1 -> level2();
            default -> level3();
        };
    }

    // =====================================================================
    //  NIVEAU 1 - "Le Grenier" : couloirs horizontaux, peu de chiens (facile)
    // =====================================================================
    private static Level level1() {
        int[][] passages = {
                {3, 9, 15},        // rangee-mur 2
                {1, 7, 13, 17},    // rangee-mur 4
                {5, 11, 15},       // rangee-mur 6
                {1, 9, 13, 17},    // rangee-mur 8
                {3, 7, 15}         // rangee-mur 10
        };
        boolean[][] grid = horizontalGrid(passages);
        int[][] fish   = {{1, 13}, {3, 5}, {5, 15}, {7, 3}, {9, 11}, {11, 5}};
        int[][] dogs   = {{5, 9, 0}, {9, 7, 0}};   // 0 = patrouille horizontale
        int[][] stars  = {};
        int[][] clocks = {{7, 15}};
        return assemble(0, "Le Grenier", 75, 1.5, grid, 1, 1, 11, 17,
                fish, dogs, stars, clocks);
    }

    // =====================================================================
    //  NIVEAU 2 - "La Cave" : couloirs verticaux, 3 chiens (intermediaire)
    // =====================================================================
    private static Level level2() {
        int[][] passages = {
                {3, 9},        // colonne-mur 2
                {1, 7, 11},    // colonne-mur 4
                {5, 9},        // colonne-mur 6
                {3, 7, 11},    // colonne-mur 8
                {1, 5, 9},     // colonne-mur 10
                {3, 7, 11},    // colonne-mur 12
                {5, 9},        // colonne-mur 14
                {1, 7, 11}     // colonne-mur 16
        };
        boolean[][] grid = verticalGrid(passages);
        int[][] fish   = {{3, 3}, {1, 5}, {9, 5}, {5, 7}, {11, 9}, {3, 11}, {7, 13}, {5, 15}};
        int[][] dogs   = {{5, 3, 1}, {7, 9, 1}, {9, 15, 1}};   // 1 = patrouille verticale
        int[][] stars  = {{7, 7}};
        int[][] clocks = {{9, 11}};
        return assemble(1, "La Cave", 85, 1.9, grid, 1, 1, 11, 17,
                fish, dogs, stars, clocks);
    }

    // =====================================================================
    //  NIVEAU 3 - "Le Labyrinthe" : passages rares, 4 chiens rapides (difficile)
    // =====================================================================
    private static Level level3() {
        int[][] passages = {
                {5, 15},     // rangee-mur 2
                {3, 11},     // rangee-mur 4
                {9, 17},     // rangee-mur 6
                {1, 13},     // rangee-mur 8
                {7, 15}      // rangee-mur 10
        };
        boolean[][] grid = horizontalGrid(passages);
        int[][] fish   = {{1, 9}, {1, 17}, {3, 5}, {3, 15}, {5, 3},
                          {5, 13}, {7, 7}, {9, 1}, {9, 15}, {11, 5}};
        int[][] dogs   = {{3, 9, 0}, {5, 7, 0}, {7, 13, 0}, {9, 5, 0}};
        int[][] stars  = {{7, 17}};
        int[][] clocks = {{11, 15}};
        return assemble(2, "Le Labyrinthe", 100, 2.3, grid, 1, 1, 11, 9,
                fish, dogs, stars, clocks);
    }

    // =====================================================================
    //  Construction des grilles de murs
    // =====================================================================

    /**
     * Labyrinthe a couloirs HORIZONTAUX : les rangees impaires sont des
     * couloirs entierement ouverts, les rangees paires sont des murs perces
     * de passages. Chaque rangee-mur ayant au moins un passage, la grille est
     * forcement entierement connectee.
     */
    private static boolean[][] horizontalGrid(int[][] passagesByWallRow) {
        boolean[][] wall = new boolean[ROWS][COLS];
        addBorders(wall);
        for (int r = 2; r < ROWS - 1; r += 2) {
            for (int c = 1; c < COLS - 1; c++) {
                wall[r][c] = true;
            }
        }
        for (int i = 0; i < passagesByWallRow.length; i++) {
            int wallRow = 2 + 2 * i;
            for (int col : passagesByWallRow[i]) {
                wall[wallRow][col] = false;
            }
        }
        return wall;
    }

    /**
     * Labyrinthe a couloirs VERTICAUX : les colonnes impaires sont des
     * couloirs ouverts, les colonnes paires sont des murs perces de passages.
     */
    private static boolean[][] verticalGrid(int[][] passagesByWallCol) {
        boolean[][] wall = new boolean[ROWS][COLS];
        addBorders(wall);
        for (int c = 2; c < COLS - 1; c += 2) {
            for (int r = 1; r < ROWS - 1; r++) {
                wall[r][c] = true;
            }
        }
        for (int i = 0; i < passagesByWallCol.length; i++) {
            int wallCol = 2 + 2 * i;
            for (int row : passagesByWallCol[i]) {
                wall[row][wallCol] = false;
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

    // =====================================================================
    //  Instanciation des objets du niveau a partir de la grille
    // =====================================================================
    private static Level assemble(int index, String name, int timeLimit, double dogSpeed,
                                  boolean[][] grid, int catR, int catC, int exitR, int exitC,
                                  int[][] fishCells, int[][] dogCells,
                                  int[][] starCells, int[][] clockCells) {

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
            if (cell[2] == 0) {
                dog.setHorizontal();
            } else {
                dog.setVertical();
            }
            dogs.add(dog);
        }

        List<Bonus> bonuses = new ArrayList<>();
        for (int[] cell : starCells) {
            bonuses.add(new Bonus(centered(cell[1], Bonus.SIZE), centered(cell[0], Bonus.SIZE),
                    Bonus.Type.POINTS));
        }
        for (int[] cell : clockCells) {
            bonuses.add(new Bonus(centered(cell[1], Bonus.SIZE), centered(cell[0], Bonus.SIZE),
                    Bonus.Type.TIME));
        }

        return new Level(index, name, timeLimit, cat, exit, walls, fish, dogs, bonuses);
    }

    /** Calcule la coordonnee en pixels d'un objet centre dans une cellule. */
    private static double centered(int cell, double objectSize) {
        return cell * T + (T - objectSize) / 2.0;
    }
}
