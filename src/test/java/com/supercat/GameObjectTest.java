package com.supercat;

import com.supercat.model.Bonus;
import com.supercat.model.Cat;
import com.supercat.model.Dog;
import com.supercat.model.Fish;
import com.supercat.model.Wall;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires des objets du jeu : deplacement du chat, mouvement
 * autonome des chiens, collecte des poissons, valeur des bonus.
 */
class GameObjectTest {

    @Test
    void chat_prendLaBonneDirection() {
        Cat cat = new Cat(100, 100);
        cat.clearVelocity();
        assertEquals(0, cat.getVx());
        assertEquals(0, cat.getVy());

        cat.moveRight();
        assertTrue(cat.getVx() > 0, "moveRight doit donner une vitesse positive");

        cat.clearVelocity();
        cat.moveUp();
        assertTrue(cat.getVy() < 0, "moveUp doit donner une vitesse negative");
    }

    @Test
    void chien_seDeplaceDeFaconAutonome() {
        Dog dog = new Dog(100, 100, 9999);
        dog.setHorizontal();
        double startX = dog.getX();
        dog.update();
        assertNotEquals(startX, dog.getX(),
                "Le chien doit se deplacer tout seul lors de update()");
    }

    @Test
    void chien_peutFaireDemiTour() {
        Dog dog = new Dog(100, 100, 9999);
        dog.setHorizontal();
        double vitesseAvant = dog.getVx();
        dog.reverse();
        assertEquals(-vitesseAvant, dog.getVx(), 0.0001,
                "reverse() doit inverser la direction du chien");
    }

    @Test
    void poisson_peutEtreCollecte_etVaut100Points() {
        Fish fish = new Fish(50, 50);
        assertFalse(fish.isCollected());
        assertEquals(100, fish.getValue(), "Un poisson vaut 100 points (regle RM3)");
        fish.collect();
        assertTrue(fish.isCollected());
    }

    @Test
    void bonus_aLaBonneValeurSelonSonType() {
        Bonus points = new Bonus(0, 0, Bonus.Type.POINTS);
        Bonus temps = new Bonus(0, 0, Bonus.Type.TIME);
        assertEquals(250, points.getValue(), "Le bonus etoile rapporte 250 points");
        assertEquals(10, temps.getValue(), "Le bonus horloge ajoute 10 secondes");
    }

    @Test
    void getBounds_correspondAuxDimensionsDeLObjet() {
        Wall wall = new Wall(80, 120, 40);
        Rectangle2D bounds = wall.getBounds();
        assertEquals(80, bounds.getMinX(), 0.001);
        assertEquals(120, bounds.getMinY(), 0.001);
        assertEquals(40, bounds.getWidth(), 0.001);
        assertEquals(40, bounds.getHeight(), 0.001);
    }
}
