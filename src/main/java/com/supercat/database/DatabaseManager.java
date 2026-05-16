package com.supercat.database;

import com.supercat.model.ScoreEntry;
import com.supercat.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de la base de donnees SQLite (patron de conception Singleton,
 * conforme au diagramme de classes UML).
 *
 * Responsable de toute la persistance : comptes utilisateurs (table USERS)
 * et scores (table SCORES). Les mots de passe ne sont jamais stockes en
 * clair : ils sont haches avec l'algorithme BCrypt (regle metier RM1).
 */
public class DatabaseManager {

    /** Fichier de base de donnees, cree automatiquement au premier lancement. */
    private static final String DB_URL = "jdbc:sqlite:supercat.db";

    private static DatabaseManager instance;

    private Connection connection;

    /** Constructeur prive : seul getInstance() peut creer l'unique instance. */
    private DatabaseManager() {
        connect();
        createTables();
        seedAdmin();
    }

    /** Retourne l'unique instance du gestionnaire de base de donnees. */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // =====================================================================
    //  Connexion et creation du schema
    // =====================================================================
    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'ouvrir la base SQLite", e);
        }
    }

    private void createTables() {
        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    email    TEXT,
                    role     TEXT NOT NULL DEFAULT 'joueur'
                )""";
        String scoresTable = """
                CREATE TABLE IF NOT EXISTS scores (
                    id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    value   INTEGER NOT NULL,
                    date    TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )""";
        try (Statement st = connection.createStatement()) {
            st.execute(usersTable);
            st.execute(scoresTable);
        } catch (SQLException e) {
            throw new IllegalStateException("Echec de la creation des tables", e);
        }
    }

    /** Cree le compte administrateur par defaut au premier lancement. */
    private void seedAdmin() {
        if (!usernameExists("admin")) {
            createAccount("admin", "admin123", "admin@supercat.com", "admin");
        }
    }

    // =====================================================================
    //  Comptes utilisateurs (table USERS)
    // =====================================================================

    /**
     * Authentifie un utilisateur. Le mot de passe fourni est compare au hash
     * BCrypt stocke en base. Retourne le User en cas de succes, sinon null.
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT id, username, password, email, role FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && BCrypt.checkpw(password, rs.getString("password"))) {
                    int id = rs.getInt("id");
                    return new User(id, rs.getString("username"), rs.getString("email"),
                            rs.getString("role"), getHighScore(id));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur authenticate : " + e.getMessage());
        }
        return null;
    }

    /** Inscrit un nouveau joueur. Retourne false si le pseudo est deja pris. */
    public boolean registerUser(String username, String password, String email) {
        return createAccount(username, password, email, "joueur");
    }

    private boolean createAccount(String username, String password, String email, String role) {
        if (usernameExists(username)) {
            return false;
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, email);
            ps.setString(4, role);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur registerUser : " + e.getMessage());
            return false;
        }
    }

    /** Vrai si un compte porte deja ce pseudo. */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erreur usernameExists : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un compte (action reservee a l'administrateur, RM5). Les
     * comptes administrateurs sont proteges et ne peuvent pas etre supprimes.
     * La suppression en cascade efface aussi les scores du joueur.
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ? AND role <> 'admin'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur deleteUser : " + e.getMessage());
            return false;
        }
    }

    /** Retourne tous les comptes (utilise par l'espace d'administration). */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email, role FROM users ORDER BY role, username";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                users.add(new User(id, rs.getString("username"), rs.getString("email"),
                        rs.getString("role"), getHighScore(id)));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAllUsers : " + e.getMessage());
        }
        return users;
    }

    /** Met a jour l'adresse e-mail d'un profil. */
    public boolean updateEmail(int userId, String email) {
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur updateEmail : " + e.getMessage());
            return false;
        }
    }

    /** Change le mot de passe d'un compte (re-hache avec BCrypt). */
    public boolean changePassword(int userId, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur changePassword : " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifie l'identite pour la recuperation de mot de passe : le pseudo et
     * l'e-mail doivent correspondre au meme compte.
     */
    public boolean verifyRecovery(String username, String email) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND lower(email) = lower(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erreur verifyRecovery : " + e.getMessage());
            return false;
        }
    }

    /** Reinitialise le mot de passe d'un compte identifie par son pseudo. */
    public boolean resetPassword(String username, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur resetPassword : " + e.getMessage());
            return false;
        }
    }

    // =====================================================================
    //  Scores (table SCORES)
    // =====================================================================

    /**
     * Enregistre le score d'une partie terminee et indique s'il s'agit d'un
     * nouveau record personnel (regles RM3 et RM8). Chaque partie est
     * conservee, ce qui permet l'analyse statistique (RM10).
     */
    public boolean updateHighScore(int userId, int score) {
        int previousBest = getHighScore(userId);
        String sql = "INSERT INTO scores (user_id, value) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, score);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur updateHighScore : " + e.getMessage());
            return false;
        }
        return score > previousBest;
    }

    /** Retourne le meilleur score d'un joueur (0 s'il n'a jamais joue). */
    public int getHighScore(int userId) {
        String sql = "SELECT COALESCE(MAX(value), 0) AS hs FROM scores WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("hs");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur getHighScore : " + e.getMessage());
        }
        return 0;
    }

    /** Reinitialise tous les scores d'un joueur (action administrateur, RM5). */
    public boolean resetUserScores(int userId) {
        String sql = "DELETE FROM scores WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur resetUserScores : " + e.getMessage());
            return false;
        }
    }

    /** Retourne le classement mondial trie par meilleur score (RM8). */
    public List<ScoreEntry> getLeaderboard() {
        List<ScoreEntry> board = new ArrayList<>();
        String sql = """
                SELECT u.username AS username,
                       MAX(s.value) AS best,
                       MAX(s.date)  AS lastdate
                FROM scores s
                JOIN users u ON u.id = s.user_id
                GROUP BY u.id
                ORDER BY best DESC, lastdate ASC""";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            int rank = 1;
            while (rs.next()) {
                board.add(new ScoreEntry(rank++, rs.getString("username"),
                        rs.getInt("best"), rs.getString("lastdate")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getLeaderboard : " + e.getMessage());
        }
        return board;
    }

    // =====================================================================
    //  Statistiques (espace administrateur, RM10)
    // =====================================================================

    /** Statistiques agregees de la plateforme. */
    public record Stats(int totalUsers, int totalPlayers, int gamesPlayed,
                         int topScore, int averageScore) {
    }

    public Stats getStatistics() {
        int users = queryInt("SELECT COUNT(*) FROM users");
        int players = queryInt("SELECT COUNT(*) FROM users WHERE role = 'joueur'");
        int games = queryInt("SELECT COUNT(*) FROM scores");
        int top = queryInt("SELECT COALESCE(MAX(value), 0) FROM scores");
        int avg = queryInt("SELECT COALESCE(ROUND(AVG(value)), 0) FROM scores");
        return new Stats(users, players, games, top, avg);
    }

    private int queryInt(String sql) {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur queryInt : " + e.getMessage());
        }
        return 0;
    }

    /** Ferme la connexion a la base de donnees (a l'arret de l'application). */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur close : " + e.getMessage());
        }
    }
}
