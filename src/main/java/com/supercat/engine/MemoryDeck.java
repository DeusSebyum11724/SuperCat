package com.supercat.engine;

import java.util.Random;

/**
 * Plateau du jeu des paires (mini-jeu "memory" du mode Histoire).
 *
 * Le plateau contient des paires de symboles caches : chaque symbole apparait
 * exactement deux fois. La distribution est deterministe (melange a graine
 * fixe), si bien qu'un meme chapitre rejoue presente toujours le meme
 * plateau -- coherent avec le reste du jeu.
 */
public final class MemoryDeck {

    /** Nombre de symboles distincts dessinables (voir MemoryGameController). */
    public static final int SYMBOL_COUNT = 8;

    private final int rows;
    private final int cols;
    private final int[] symbols;   // identifiant de symbole de chaque carte

    private MemoryDeck(int rows, int cols, int[] symbols) {
        this.rows = rows;
        this.cols = cols;
        this.symbols = symbols;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getCardCount() { return symbols.length; }
    public int getPairCount() { return symbols.length / 2; }
    public int symbolAt(int index) { return symbols[index]; }

    /** Construit le plateau du chapitre donne. */
    public static MemoryDeck forChapter(int chapter) {
        int pairs = (chapter <= 4) ? 6 : 8;
        int cols = 4;
        int rows = pairs * 2 / cols;
        int[] deck = new int[pairs * 2];
        for (int i = 0; i < pairs; i++) {
            deck[2 * i] = i;
            deck[2 * i + 1] = i;
        }
        Random rng = new Random(chapter * 7919L + 1313L);
        for (int i = deck.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = deck[i];
            deck[i] = deck[j];
            deck[j] = tmp;
        }
        return new MemoryDeck(rows, cols, deck);
    }
}
