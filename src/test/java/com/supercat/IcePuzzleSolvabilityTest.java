package com.supercat;

import com.supercat.engine.IcePuzzle;
import com.supercat.engine.Story;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests garantissant que chaque casse-tete du Lac Gele est realisable.
 *
 * Un parcours en largeur explore tout l'espace d'etats du mini-jeu : un etat
 * associe la cellule ou le chat s'immobilise et l'ensemble des poissons deja
 * ramasses. Le casse-tete est resoluble s'il existe un chemin menant a l'etat
 * "sur la sortie, tous les poissons collectes".
 */
class IcePuzzleSolvabilityTest {

    private static final int COLS = IcePuzzle.COLS;

    @Test
    void lesChapitresGlissants_sontResolubles() {
        for (int chapter = 0; chapter < Story.chapterCount(); chapter++) {
            if (Story.game(chapter) == Story.Game.ICE) {
                assertTrue(isSolvable(IcePuzzle.generate(chapter)),
                        "Chapitre " + chapter + " (Lac Gele) : casse-tete insoluble");
            }
        }
    }

    @Test
    void leGenerateurDeGlace_estToujoursResoluble() {
        for (int chapter = 0; chapter <= 30; chapter++) {
            assertTrue(isSolvable(IcePuzzle.generate(chapter)),
                    "Generation " + chapter + " : casse-tete de glace insoluble");
        }
    }

    /** Vrai s'il existe une suite de glissades collectant tout puis sortant. */
    private boolean isSolvable(IcePuzzle puzzle) {
        List<int[]> fish = puzzle.getFish();
        int n = fish.size();
        int full = (1 << n) - 1;
        int cells = IcePuzzle.ROWS * COLS;
        int exit = puzzle.getExitRow() * COLS + puzzle.getExitCol();

        boolean[][] visited = new boolean[cells][full + 1];
        Deque<int[]> queue = new ArrayDeque<>();
        int start = puzzle.getCatRow() * COLS + puzzle.getCatCol();
        visited[start][0] = true;
        queue.add(new int[]{start, 0});
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            int[] state = queue.poll();
            int cell = state[0];
            int mask = state[1];
            if (cell == exit && mask == full) {
                return true;
            }
            int r = cell / COLS;
            int c = cell % COLS;
            for (int[] d : dirs) {
                int[] end = puzzle.slideEnd(r, c, d[0], d[1]);
                int newMask = mask | fishOnPath(fish, r, c, end[0], end[1]);
                int endCell = end[0] * COLS + end[1];
                if (!visited[endCell][newMask]) {
                    visited[endCell][newMask] = true;
                    queue.add(new int[]{endCell, newMask});
                }
            }
        }
        return false;
    }

    /** Masque des poissons situes sur la trajectoire (r0,c0) -> (r1,c1). */
    private int fishOnPath(List<int[]> fish, int r0, int c0, int r1, int c1) {
        int dr = Integer.signum(r1 - r0);
        int dc = Integer.signum(c1 - c0);
        int mask = 0;
        int r = r0;
        int c = c0;
        while (true) {
            for (int i = 0; i < fish.size(); i++) {
                if (fish.get(i)[0] == r && fish.get(i)[1] == c) {
                    mask |= (1 << i);
                }
            }
            if (r == r1 && c == c1) {
                break;
            }
            r += dr;
            c += dc;
        }
        return mask;
    }
}
