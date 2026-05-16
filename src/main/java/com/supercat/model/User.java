package com.supercat.model;

/**
 * Represente un compte utilisateur de la plateforme SuperCat.
 *
 * Deux roles existent (cf. regles metier RM1 et RM5) :
 *  - "joueur" : peut jouer, gerer son profil et consulter le classement ;
 *  - "admin"  : peut superviser et supprimer des comptes joueurs.
 *
 * Le champ "verified" indique si le compte a ete active par e-mail :
 * un joueur doit verifier son adresse avant de pouvoir se connecter.
 */
public class User {

    private final String id;
    private final String username;
    private String email;
    private final String role;
    private int highScore;
    private boolean verified;

    /** Constructeur court (usage interne et tests). */
    public User(String username, String role) {
        this("", username, "", role, 0, true);
    }

    /** Constructeur complet utilise lors de la lecture en base de donnees. */
    public User(String id, String username, String email, String role,
                int highScore, boolean verified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.highScore = highScore;
        this.verified = verified;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public int getHighScore() { return highScore; }
    public boolean isVerified() { return verified; }

    public void setEmail(String email) { this.email = email; }
    public void setHighScore(int highScore) { this.highScore = highScore; }
    public void setVerified(boolean verified) { this.verified = verified; }

    /** Indique si l'utilisateur dispose des droits d'administration. */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    /** Libelle "Oui"/"Non" affiche dans le tableau d'administration. */
    public String getVerifiedLabel() {
        return verified ? "Oui" : "Non";
    }

    @Override
    public String toString() {
        return username;
    }
}
