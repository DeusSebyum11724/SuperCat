package com.supercat.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Genere et represente un casse-tete du Lac Gele (mini-jeu du mode Histoire).
 *
 * Sur la glace, Nora glisse en ligne droite jusqu'a heurter un rocher ou le
 * bord du lac : impossible de s'arreter au milieu. Le but est de passer sur
 * tous les poissons d'or puis de s'immobiliser sur la sortie.
 *
 * Chaque casse-tete est construit a partir d'une marche aleatoire de
 * glissades. La sequence exacte de cette marche constitue donc une solution :
 * le casse-tete est toujours resoluble par construction.
 */
public final class IcePuzzle {

    public static final int COLS = 15;
    public static final int ROWS = 10;

    private static final int[] DR = {-1, 1, 0, 0};
    private static final int[] DC = {0, 0, -1, 1};

    private final boolean[][] wall;
    private final int catRow;
    private final int catCol;
    private final int exitRow;
    private final int exitCol;
    private final List<int[]> fish;
    private final int timeLimit;

    private IcePuzzle(boolean[][] wall, int catRow, int catCol,
                      int exitRow, int exitCol, List<int[]> fish, int timeLimit) {
        this.wall = wall;
        this.catRow = catRow;
        this.catCol = catCol;
        this.exitRow = exitRow;
        this.exitCol = exitCol;
        this.fish = fish;
        this.timeLimit = timeLimit;
    }

    // ----- Accesseurs -----
    public boolean isWall(int r, int c) {
        return r < 0 || r >= ROWS || c < 0 || c >= COLS || wall[r][c];
    }

    public int getCatRow() { return catRow; }
    public int getCatCol() { return catCol; }
    public int getExitRow() { return exitRow; }
    public int getExitCol() { return exitCol; }
    public int getTimeLimit() { return timeLimit; }
    public int getFishCount() { return fish.size(); }

    /** Cellules portant un poisson (copie defensive). */
    public List<int[]> getFish() {
        List<int[]> copy = new ArrayList<>();
        for (int[] cell : fish) {
            copy.add(new int[]{cell[0], cell[1]});
        }
        return copy;
    }

    /** Cellule ou s'arrete une glissade depuis (r,c) dans la direction (dr,dc). */
    public int[] slideEnd(int r, int c, int dr, int dc) {
        while (!isWall(r + dr, c + dc)) {
            r += dr;
            c += dc;
        }
        return new int[]{r, c};
    }

    // =====================================================================
    //  Generation deterministe
    // =====================================================================

    /** Construit le casse-tete d'un chapitre (toujours le meme, toujours resoluble). */
    public static IcePuzzle generate(int chapter) {
        int fishCount = Math.min(3 + chapter / 2, 7);
        int timeLimit = 70 + fishCount * 10;
        for (int attempt = 0; attempt < 200; attempt++) {
            IcePuzzle puzzle = tryBuild(chapter, attempt, fishCount, timeLimit);
            if (puzzle != null) {
                return puzzle;
            }
        }
        return fallback(fishCount, timeLimit);
    }

    private static IcePuzzle tryBuild(int chapter, int attempt, int fishCount, int timeLimit) {
        Random rng = new Random(chapter * 9176L + attempt * 1099511628211L + 17L);

        boolean[][] wall = new boolean[ROWS][COLS];
        for (int c = 0; c < COLS; c++) {
            wall[0][c] = true;
            wall[ROWS - 1][c] = true;
        }
        for (int r = 0; r < ROWS; r++) {
            wall[r][0] = true;
            wall[r][COLS - 1] = true;
        }

        int catRow = 1 + rng.nextInt(2);
        int catCol = 1 + rng.nextInt(2);
        int blocks = 9 + chapter;
        for (int i = 0; i < blocks; i++) {
            int r = 1 + rng.nextInt(ROWS - 2);
            int c = 1 + rng.nextInt(COLS - 2);
            if (r != catRow || c != catCol) {
                wall[r][c] = true;
            }
        }

        IcePuzzle scratch = new IcePuzzle(wall, catRow, catCol, catRow, catCol,
                List.of(), timeLimit);

        // marche aleatoire de glissades : cette sequence est une solution
        List<int[]> path = new ArrayList<>();
        int r = catRow;
        int c = catCol;
        int steps = 7 + chapter;
        for (int s = 0; s < steps; s++) {
            int[] dirs = {0, 1, 2, 3};
            shuffle(dirs, rng);
            int[] move = null;
            for (int d : dirs) {
                int[] end = scratch.slideEnd(r, c, DR[d], DC[d]);
                if (end[0] != r || end[1] != c) {
                    move = end;
                    break;
                }
            }
            if (move == null) {
                break;
            }
            addLine(path, r, c, move[0], move[1]);
            r = move[0];
            c = move[1];
        }

        int exitRow = r;
        int exitCol = c;
        if (Math.abs(exitRow - catRow) + Math.abs(exitCol - catCol) < 6) {
            return null;
        }

        // poissons : cellules traversees par la marche (donc collectables)
        List<int[]> candidates = new ArrayList<>();
        boolean[][] seen = new boolean[ROWS][COLS];
        for (int[] cell : path) {
            if (seen[cell[0]][cell[1]]) {
                continue;
            }
            seen[cell[0]][cell[1]] = true;
            boolean isCat = cell[0] == catRow && cell[1] == catCol;
            boolean isExit = cell[0] == exitRow && cell[1] == exitCol;
            if (!isCat && !isExit) {
                candidates.add(cell);
            }
        }
        if (candidates.size() < fishCount) {
            return null;
        }
        shuffleCells(candidates, rng);
        List<int[]> fish = new ArrayList<>(candidates.subList(0, fishCount));
        return new IcePuzzle(wall, catRow, catCol, exitRow, exitCol, fish, timeLimit);
    }

    /** Disposition de secours : une solution simple en deux glissades. */
    private static IcePuzzle fallback(int fishCount, int timeLimit) {
        boolean[][] wall = new boolean[ROWS][COLS];
        for (int c = 0; c < COLS; c++) {
            wall[0][c] = true;
            wall[ROWS - 1][c] = true;
        }
        for (int r = 0; r < ROWS; r++) {
            wall[r][0] = true;
            wall[r][COLS - 1] = true;
        }
        List<int[]> fish = new ArrayList<>();
        for (int c = 2; c < COLS - 2 && fish.size() < fishCount; c++) {
            fish.add(new int[]{1, c});
        }
        for (int r = 2; r < ROWS - 2 && fish.size() < fishCount; r++) {
            fish.add(new int[]{r, COLS - 2});
        }
        return new IcePuzzle(wall, 1, 1, ROWS - 2, COLS - 2, fish, timeLimit);
    }

    private static void addLine(List<int[]> path, int r0, int c0, int r1, int c1) {
        int dr = Integer.signum(r1 - r0);
        int dc = Integer.signum(c1 - c0);
        int r = r0;
        int c = c0;
        path.add(new int[]{r, c});
        while (r != r1 || c != c1) {
            r += dr;
            c += dc;
            path.add(new int[]{r, c});
        }
    }

    private static void shuffle(int[] array, Random rng) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    private static void shuffleCells(List<int[]> list, Random rng) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int[] tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }
}
