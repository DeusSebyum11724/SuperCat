package com.supercat;

import com.supercat.engine.CollisionManager;
import com.supercat.model.Cat;
import com.supercat.model.Fish;
import com.supercat.model.Wall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de la detection de collisions, mecanisme central du jeu
 * (collecte des poissons, collision mortelle avec un chien, blocage par les
 * murs).
 */
class CollisionManagerTest {

    @Test
    void deuxObjetsQuiSeChevauchent_sontEnCollision() {
        Fish fish = new Fish(100, 100);
        Wall wall = new Wall(105, 105, 40);
        assertTrue(CollisionManager.collide(fish, wall),
                "Deux objets qui se chevauchent doivent etre detectes en collision");
    }

    @Test
    void deuxObjetsEloignes_neSontPasEnCollision() {
        Fish fish = new Fish(0, 0);
        Wall wall = new Wall(500, 500, 40);
        assertFalse(CollisionManager.collide(fish, wall),
                "Deux objets eloignes ne doivent pas etre detectes en collision");
    }

    @Test
    void collidesAny_detecteUneCollisionDansUneListe() {
        Cat cat = new Cat(200, 200);
        List<Wall> walls = List.of(
                new Wall(0, 0, 40),
                new Wall(195, 195, 40),   // celui-ci chevauche le chat
                new Wall(600, 600, 40));
        assertTrue(CollisionManager.collidesAny(cat, walls));
    }

    @Test
    void collidesAny_estFaux_quandAucuneCollision() {
        Cat cat = new Cat(200, 200);
        List<Wall> walls = List.of(new Wall(0, 0, 40), new Wall(600, 600, 40));
        assertFalse(CollisionManager.collidesAny(cat, walls));
    }
}
