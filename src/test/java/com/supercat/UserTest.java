package com.supercat;

import com.supercat.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires du modele User : roles, meilleur score, profil, statut
 * de verification du compte.
 */
class UserTest {

    @Test
    void unJoueur_nEstPasAdministrateur() {
        User joueur = new User("alice", "joueur");
        assertFalse(joueur.isAdmin());
        assertEquals("alice", joueur.getUsername());
    }

    @Test
    void unAdministrateur_estReconnuCommeTel() {
        User admin = new User("admin", "admin");
        assertTrue(admin.isAdmin());
    }

    @Test
    void leMeilleurScore_estModifiable() {
        User user = new User("id1", "bob", "bob@mail.com", "joueur", 500, true);
        assertEquals(500, user.getHighScore());
        user.setHighScore(1200);
        assertEquals(1200, user.getHighScore());
    }

    @Test
    void lEmailDuProfil_estModifiable() {
        User user = new User("carol", "joueur");
        user.setEmail("carol@supercat.com");
        assertEquals("carol@supercat.com", user.getEmail());
    }

    @Test
    void leStatutDeVerification_estGere() {
        User user = new User("id2", "dave", "dave@mail.com", "joueur", 0, false);
        assertFalse(user.isVerified());
        assertEquals("Non", user.getVerifiedLabel());

        user.setVerified(true);
        assertTrue(user.isVerified());
        assertEquals("Oui", user.getVerifiedLabel());
    }
}
