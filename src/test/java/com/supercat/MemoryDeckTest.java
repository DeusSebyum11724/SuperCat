package com.supercat;

import com.supercat.engine.MemoryDeck;
import com.supercat.engine.Story;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests du plateau du jeu des paires (mini-jeu "memory" du mode Histoire).
 */
class MemoryDeckTest {

    @Test
    void chaqueSymbole_apparaitExactementDeuxFois() {
        for (int chapter = 0; chapter < Story.chapterCount(); chapter++) {
            MemoryDeck deck = MemoryDeck.forChapter(chapter);
            int[] occurrences = new int[MemoryDeck.SYMBOL_COUNT];
            for (int i = 0; i < deck.getCardCount(); i++) {
                int symbol = deck.symbolAt(i);
                assertTrue(symbol >= 0 && symbol < MemoryDeck.SYMBOL_COUNT,
                        "Symbole hors limites au chapitre " + chapter);
                occurrences[symbol]++;
            }
            for (int symbol = 0; symbol < deck.getPairCount(); symbol++) {
                assertEquals(2, occurrences[symbol],
                        "Le symbole " + symbol + " doit former une paire (chapitre "
                                + chapter + ")");
            }
        }
    }

    @Test
    void lesDimensions_duPlateau_sontCoherentes() {
        for (int chapter = 0; chapter < Story.chapterCount(); chapter++) {
            MemoryDeck deck = MemoryDeck.forChapter(chapter);
            assertEquals(deck.getRows() * deck.getCols(), deck.getCardCount(),
                    "Cartes = lignes x colonnes (chapitre " + chapter + ")");
            assertEquals(deck.getCardCount() / 2, deck.getPairCount(),
                    "Paires = cartes / 2 (chapitre " + chapter + ")");
            assertEquals(0, deck.getCardCount() % 2,
                    "Le nombre de cartes doit etre pair (chapitre " + chapter + ")");
        }
    }

    @Test
    void lePlateau_estDeterministe() {
        for (int chapter = 0; chapter < Story.chapterCount(); chapter++) {
            MemoryDeck first = MemoryDeck.forChapter(chapter);
            MemoryDeck second = MemoryDeck.forChapter(chapter);
            assertEquals(first.getCardCount(), second.getCardCount());
            for (int i = 0; i < first.getCardCount(); i++) {
                assertEquals(first.symbolAt(i), second.symbolAt(i),
                        "Le plateau du chapitre " + chapter + " doit etre identique a chaque fois");
            }
        }
    }
}
