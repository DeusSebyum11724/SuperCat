package com.supercat;

import com.supercat.engine.Level;
import com.supercat.engine.LevelLoader;
import com.supercat.model.Fish;
import com.supercat.model.Wall;
import com.supercat.ui.Theme;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires garantissant que tous les niveaux generes sont realisables.
 *
 * Un parcours en largeur (BFS) sur la grille du labyrinthe verifie que le
 * chat peut atteindre la sortie ET tous les poissons d'or. Comme le chat
 * (30 px) est plus petit qu'une cellule (40 px), l'accessibilite entre
 * cellules voisines correspond bien au deplacement reel du chat.
 *
 * La verification couvre les 12 niveaux de la campagne et un large
 * echantillon du mode sans fin : aucune salle generee n'est impossible.
 */
class LevelSolvabilityTest {

    private static final int ROWS = Theme.ROWS;
    private static final int COLS = Theme.COLS;
    private static final double TILE = Theme.TILE;

    @Test
    void tousLesNiveauxDeCampagne_sontResolubles() {
        for (int index = 0; index < LevelLoader.getCampaignCount(); index++) {
            assertSolvable(index);
        }
    }

    @Test
    void leModeSansFin_genereDesNiveauxToujoursResolubles() {
        for (int index = LevelLoader.getCampaignCount(); index <= 45; index++) {
            assertSolvable(index);
        }
    }

    /** Verifie que la sortie et tous les poissons sont accessibles au chat. */
    private void assertSolvable(int index) {
        Level level = LevelLoader.load(index);

        boolean[][] wall = new boolean[ROWS][COLS];
        for (Wall w : level.getWalls()) {
            wall[cell(w.getY())][cell(w.getX())] = true;
        }

        int catRow = cell(level.getCat().getCenterY() - TILE / 2);
        int catCol = cell(level.getCat().getCenterX() - TILE / 2);
        boolean[][] reachable = floodFill(wall, catRow, catCol);

        int exitRow = cell(level.getExit().getCenterY() - TILE / 2);
        int exitCol = cell(level.getExit().getCenterX() - TILE / 2);
        assertTrue(reachable[exitRow][exitCol],
                "Niveau " + index + " : la sortie est inaccessible");

        for (Fish fish : level.getFish()) {
            int row = cell(fish.getCenterY() - TILE / 2);
            int col = cell(fish.getCenterX() - TILE / 2);
            assertTrue(reachable[row][col],
                    "Niveau " + index + " : un poisson est inaccessible");
        }
    }

    private int cell(double pixel) {
        return (int) Math.round(pixel / TILE);
    }

    /** Parcours en largeur : cellules atteignables depuis (startRow, startCol). */
    private boolean[][] floodFill(boolean[][] wall, int startRow, int startCol) {
        boolean[][] visited = new boolean[ROWS][COLS];
        Queue<int[]> queue = new ArrayDeque<>();
        visited[startRow][startCol] = true;
        queue.add(new int[]{startRow, startCol});
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            for (int[] d : directions) {
                int nr = current[0] + d[0];
                int nc = current[1] + d[1];
                if (nr >= 0 && nr < ROWS && nc >= 0 && nc < COLS
                        && !visited[nr][nc] && !wall[nr][nc]) {
                    visited[nr][nc] = true;
                    queue.add(new int[]{nr, nc});
                }
            }
        }
        return visited;
    }
}
