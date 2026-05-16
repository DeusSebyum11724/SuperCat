package com.supercat;

import com.supercat.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires du modele User : roles, meilleur score, profil.
 */
class UserTest {

    @Test
    void unJoueur_nEstPasAdministrateur() {
        User joueur = new User(1, "alice", "joueur");
        assertFalse(joueur.isAdmin());
        assertEquals("alice", joueur.getUsername());
    }

    @Test
    void unAdministrateur_estReconnuCommeTel() {
        User admin = new User(2, "admin", "admin");
        assertTrue(admin.isAdmin());
    }

    @Test
    void leMeilleurScore_estModifiable() {
        User user = new User(3, "bob", "bob@mail.com", "joueur", 500);
        assertEquals(500, user.getHighScore());
        user.setHighScore(1200);
        assertEquals(1200, user.getHighScore());
    }

    @Test
    void lEmailDuProfil_estModifiable() {
        User user = new User(4, "carol", "joueur");
        user.setEmail("carol@supercat.com");
        assertEquals("carol@supercat.com", user.getEmail());
    }
}
