package com.supercat.engine;

import com.supercat.model.GameObject;
import javafx.geometry.Rectangle2D;

import java.util.List;

/**
 * Gestion de la detection de collisions. Deux objets sont en collision
 * lorsque leurs rectangles englobants (getBounds) se chevauchent.
 *
 * Cette classe regroupe la logique de collision afin qu'elle soit
 * facilement testable de maniere unitaire (cf. CollisionManagerTest).
 */
public final class CollisionManager {

    private CollisionManager() {
        // classe utilitaire : pas d'instanciation
    }

    /** Vrai si les deux objets se chevauchent. */
    public static boolean collide(GameObject a, GameObject b) {
        return a.getBounds().intersects(b.getBounds());
    }

    /** Vrai si les deux rectangles se croisent. */
    public static boolean intersects(Rectangle2D a, Rectangle2D b) {
        return a.intersects(b);
    }

    /** Vrai si l'objet entre en collision avec au moins un objet de la liste. */
    public static boolean collidesAny(GameObject obj, List<? extends GameObject> others) {
        for (GameObject other : others) {
            if (collide(obj, other)) {
                return true;
            }
        }
        return false;
    }
}
