package com.supercat.model;

/**
 * Represente un compte utilisateur de la plateforme SuperCat.
 *
 * Deux roles existent (cf. regles metier RM1 et RM5) :
 *  - "joueur" : peut jouer, gerer son profil et consulter le classement ;
 *  - "admin"  : peut superviser et supprimer des comptes joueurs.
 */
public class User {

    private final int id;
    private final String username;
    private String email;
    private final String role;
    private int highScore;

    /** Constructeur court (conforme au diagramme de classes). */
    public User(int id, String username, String role) {
        this(id, username, "", role, 0);
    }

    /** Constructeur complet utilise lors de la lecture en base de donnees. */
    public User(int id, String username, String email, String role, int highScore) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.highScore = highScore;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public int getHighScore() { return highScore; }

    public void setEmail(String email) { this.email = email; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    /** Indique si l'utilisateur dispose des droits d'administration. */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return username;
    }
}
