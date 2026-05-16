package com.supercat;

import com.supercat.engine.Level;
import com.supercat.engine.LevelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires du chargement des niveaux. Verifie que les 3 labyrinthes
 * (fonctionnalite avancee de Type A) sont correctement construits et que la
 * difficulte est progressive.
 */
class LevelLoaderTest {

    @Test
    void leJeu_proposeTroisNiveaux() {
        assertEquals(3, LevelLoader.getLevelCount());
    }

    @Test
    void chaqueNiveau_estCorrectementConstruit() {
        for (int i = 0; i < LevelLoader.getLevelCount(); i++) {
            Level level = LevelLoader.load(i);
            assertNotNull(level.getCat(), "Le niveau " + i + " doit avoir un chat");
            assertNotNull(level.getExit(), "Le niveau " + i + " doit avoir une sortie");
            assertFalse(level.getWalls().isEmpty(), "Le niveau " + i + " doit avoir des murs");
            assertFalse(level.getFish().isEmpty(), "Le niveau " + i + " doit avoir des poissons");
            assertTrue(level.getTimeLimit() > 0, "Le niveau " + i + " doit avoir un temps limite");
        }
    }

    @Test
    void niveau1_contientSixPoissons() {
        Level niveau1 = LevelLoader.load(0);
        assertEquals(6, niveau1.getFish().size());
    }

    @Test
    void laDifficulte_estProgressive() {
        Level niveau1 = LevelLoader.load(0);
        Level niveau3 = LevelLoader.load(2);
        assertTrue(niveau3.getFish().size() > niveau1.getFish().size(),
                "Le niveau 3 doit contenir plus de poissons que le niveau 1");
        assertTrue(niveau3.getDogs().size() > niveau1.getDogs().size(),
                "Le niveau 3 doit contenir plus de chiens que le niveau 1");
    }

    @Test
    void laSortie_estVerrouillee_auDebutDuNiveau() {
        Level niveau = LevelLoader.load(0);
        assertTrue(niveau.getExit().isLocked(),
                "La sortie doit etre verrouillee tant que les poissons ne sont pas collectes");
    }
}
