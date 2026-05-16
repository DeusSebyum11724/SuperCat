package com.supercat;

import com.supercat.engine.Level;
import com.supercat.engine.LevelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de la generation procedurale des niveaux : campagne de
 * 12 niveaux et mode sans fin, avec une difficulte progressive.
 */
class LevelLoaderTest {

    @Test
    void laCampagne_comporteDouzeNiveaux() {
        assertEquals(12, LevelLoader.getCampaignCount());
    }

    @Test
    void chaqueNiveauDeCampagne_estCorrectementGenere() {
        for (int i = 0; i < LevelLoader.getCampaignCount(); i++) {
            Level level = LevelLoader.load(i);
            assertNotNull(level.getCat(), "Niveau " + i + " : chat manquant");
            assertNotNull(level.getExit(), "Niveau " + i + " : sortie manquante");
            assertFalse(level.getWalls().isEmpty(), "Niveau " + i + " : murs manquants");
            assertFalse(level.getFish().isEmpty(), "Niveau " + i + " : poissons manquants");
            assertTrue(level.getTimeLimit() > 0, "Niveau " + i + " : temps invalide");
            assertNotNull(level.getDifficultyLabel());
        }
    }

    @Test
    void laGeneration_estDeterministe() {
        // le meme indice doit toujours produire exactement le meme niveau
        Level first = LevelLoader.load(5);
        Level second = LevelLoader.load(5);
        assertEquals(first.getFish().size(), second.getFish().size());
        assertEquals(first.getWalls().size(), second.getWalls().size());
        assertEquals(first.getDogs().size(), second.getDogs().size());
    }

    @Test
    void laDifficulte_estProgressive() {
        Level facile = LevelLoader.load(0);
        Level expert = LevelLoader.load(11);
        assertTrue(expert.getFish().size() > facile.getFish().size(),
                "Un niveau avance doit contenir plus de poissons");
        assertTrue(expert.getDogs().size() > facile.getDogs().size(),
                "Un niveau avance doit contenir plus de chiens");
    }

    @Test
    void leModeSansFin_genereDesNiveauxValides() {
        Level endless1 = LevelLoader.load(12);
        Level endless2 = LevelLoader.load(25);
        assertNotNull(endless1.getCat());
        assertFalse(endless1.getFish().isEmpty());
        assertNotNull(endless2.getExit());
        assertFalse(endless2.getWalls().isEmpty());
    }

    @Test
    void laSortie_estVerrouillee_auDebutDuNiveau() {
        assertTrue(LevelLoader.load(0).getExit().isLocked(),
                "La sortie doit etre verrouillee tant que les poissons ne sont pas collectes");
    }

    @Test
    void chaqueNiveau_aUnLibelleDeDifficulte() {
        assertEquals("Facile", LevelLoader.getDifficultyLabel(0));
        assertEquals("Expert", LevelLoader.getDifficultyLabel(10));
        assertEquals("Extreme", LevelLoader.getDifficultyLabel(15));
    }
}
