package com.supercat;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaires de la securite des mots de passe. La regle metier RM1
 * impose que les mots de passe soient haches (BCrypt) et jamais stockes en
 * clair en base de donnees.
 */
class PasswordSecurityTest {

    @Test
    void leMotDePasse_estHache_jamaisEnClair() {
        String enClair = "monMotDePasse123";
        String hache = BCrypt.hashpw(enClair, BCrypt.gensalt());
        assertNotEquals(enClair, hache,
                "Le hash ne doit jamais etre identique au mot de passe en clair");
        assertTrue(hache.length() > 20, "Un hash BCrypt est une longue chaine");
    }

    @Test
    void leBonMotDePasse_estAccepte() {
        String hache = BCrypt.hashpw("secret2024", BCrypt.gensalt());
        assertTrue(BCrypt.checkpw("secret2024", hache),
                "Le bon mot de passe doit etre verifie avec succes");
    }

    @Test
    void unMauvaisMotDePasse_estRejete() {
        String hache = BCrypt.hashpw("bonMotDePasse", BCrypt.gensalt());
        assertFalse(BCrypt.checkpw("mauvaisMotDePasse", hache),
                "Un mauvais mot de passe doit etre rejete");
    }

    @Test
    void deuxHachages_duMemeMotDePasse_sontDifferents() {
        // grace au sel aleatoire de BCrypt
        String motDePasse = "identique";
        String hash1 = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
        String hash2 = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
        assertNotEquals(hash1, hash2,
                "Deux hachages du meme mot de passe doivent differer (sel aleatoire)");
    }
}
