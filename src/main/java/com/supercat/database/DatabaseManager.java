package com.supercat.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.supercat.model.ScoreEntry;
import com.supercat.model.User;
import com.supercat.service.Config;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Gestionnaire de la base de donnees MongoDB (patron Singleton).
 *
 * Toute la persistance passe par deux collections :
 *  - "users"  : comptes utilisateurs (pseudo, mot de passe hache BCrypt,
 *               e-mail, role, statut de verification) ;
 *  - "scores" : scores des parties terminees.
 *
 * Les mots de passe ne sont jamais stockes en clair : ils sont haches avec
 * BCrypt (regle metier RM1).
 */
public class DatabaseManager {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final SecureRandom RANDOM = new SecureRandom();

    private static DatabaseManager instance;

    private MongoClient client;
    private MongoCollection<Document> users;
    private MongoCollection<Document> scores;

    private DatabaseManager() {
        connect();
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
    //  Connexion
    // =====================================================================
    private void connect() {
        String uri = Config.getMongoUri();
        if (uri.isBlank()) {
            throw new IllegalStateException("Configuration MongoDB absente : creez le "
                    + "fichier config.properties (modele : config.properties.example).");
        }
        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .applyToClusterSettings(b -> b.serverSelectionTimeout(8, TimeUnit.SECONDS))
                    .build();
            client = MongoClients.create(settings);
            MongoDatabase database = client.getDatabase("supercat");
            users = database.getCollection("users");
            scores = database.getCollection("scores");
            // verifie immediatement la connexion (echoue vite si injoignable)
            database.runCommand(new Document("ping", 1));
            users.createIndex(Indexes.ascending("username"), new IndexOptions().unique(true));
            System.out.println("[SuperCat] Connexion MongoDB reussie (base 'supercat').");
        } catch (RuntimeException e) {
            throw new IllegalStateException("Connexion a la base MongoDB impossible. "
                    + "Verifiez votre connexion Internet et l'autorisation d'acces (IP) "
                    + "sur MongoDB Atlas.\nDetail : " + e.getMessage(), e);
        }
    }

    /** Cree le compte administrateur par defaut au premier lancement. */
    private void seedAdmin() {
        if (users.find(Filters.eq("username", "admin")).first() == null) {
            users.insertOne(new Document("username", "admin")
                    .append("password", BCrypt.hashpw("admin123", BCrypt.gensalt()))
                    .append("email", "admin@supercat.com")
                    .append("role", "admin")
                    .append("verified", true));
        }
    }

    // =====================================================================
    //  Comptes utilisateurs (collection "users")
    // =====================================================================

    /**
     * Authentifie un utilisateur : le mot de passe fourni est compare au hash
     * BCrypt stocke. Retourne le User en cas de succes, sinon null.
     */
    public User authenticate(String username, String password) {
        Document doc = users.find(Filters.eq("username", username)).first();
        if (doc != null && BCrypt.checkpw(password, doc.getString("password"))) {
            return toUser(doc);
        }
        return null;
    }

    /**
     * Inscrit un nouveau joueur (compte non verifie). Retourne le code de
     * verification a envoyer par e-mail, ou null si le pseudo est deja pris.
     */
    public String registerUser(String username, String password, String email) {
        if (usernameExists(username)) {
            return null;
        }
        String code = generateCode();
        users.insertOne(new Document("username", username)
                .append("password", BCrypt.hashpw(password, BCrypt.gensalt()))
                .append("email", email)
                .append("role", "joueur")
                .append("verified", false)
                .append("verificationCode", code));
        return code;
    }

    /**
     * Verifie un compte a l'aide du code recu par e-mail. En cas de succes,
     * le compte est marque comme verifie.
     */
    public boolean verifyAccount(String username, String code) {
        Document doc = users.find(Filters.eq("username", username)).first();
        if (doc == null) {
            return false;
        }
        String stored = doc.getString("verificationCode");
        if (stored != null && stored.equals(code.trim())) {
            users.updateOne(Filters.eq("username", username),
                    Updates.combine(Updates.set("verified", true),
                            Updates.unset("verificationCode")));
            return true;
        }
        return false;
    }

    /** Genere un nouveau code de verification pour un compte (re-envoi). */
    public String regenerateCode(String username) {
        Document doc = users.find(Filters.eq("username", username)).first();
        if (doc == null) {
            return null;
        }
        String code = generateCode();
        users.updateOne(Filters.eq("username", username),
                Updates.set("verificationCode", code));
        return code;
    }

    /** Vrai si un compte porte deja ce pseudo. */
    public boolean usernameExists(String username) {
        return users.find(Filters.eq("username", username)).first() != null;
    }

    /** Retourne un utilisateur a partir de son pseudo (null si introuvable). */
    public User getUserByUsername(String username) {
        Document doc = users.find(Filters.eq("username", username)).first();
        return (doc == null) ? null : toUser(doc);
    }

    /**
     * Supprime un compte (action reservee a l'administrateur, RM5). Les
     * comptes administrateur sont proteges. Les scores du joueur sont aussi
     * supprimes.
     */
    public boolean deleteUser(String username) {
        Document doc = users.find(Filters.eq("username", username)).first();
        if (doc == null || "admin".equals(doc.getString("role"))) {
            return false;
        }
        users.deleteOne(Filters.eq("username", username));
        scores.deleteMany(Filters.eq("username", username));
        return true;
    }

    /** Retourne tous les comptes (utilise par l'espace d'administration). */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        for (Document doc : users.find().sort(Sorts.ascending("role", "username"))) {
            list.add(toUser(doc));
        }
        return list;
    }

    /** Met a jour l'adresse e-mail d'un profil. */
    public boolean updateEmail(String username, String email) {
        return users.updateOne(Filters.eq("username", username),
                Updates.set("email", email)).getMatchedCount() > 0;
    }

    /** Change le mot de passe d'un compte (re-hache avec BCrypt). */
    public boolean changePassword(String username, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        return users.updateOne(Filters.eq("username", username),
                Updates.set("password", hash)).getMatchedCount() > 0;
    }

    /** Verifie l'identite pour la recuperation de mot de passe (pseudo + e-mail). */
    public boolean verifyRecovery(String username, String email) {
        Document doc = users.find(Filters.eq("username", username)).first();
        return doc != null && email.equalsIgnoreCase(doc.getString("email"));
    }

    /** Reinitialise le mot de passe d'un compte identifie par son pseudo. */
    public boolean resetPassword(String username, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        return users.updateOne(Filters.eq("username", username),
                Updates.set("password", hash)).getMatchedCount() > 0;
    }

    // =====================================================================
    //  Scores (collection "scores")
    // =====================================================================

    /**
     * Enregistre le score d'une partie et indique s'il s'agit d'un nouveau
     * record personnel (regles RM3 et RM8).
     */
    public boolean updateHighScore(String username, int score) {
        int previousBest = getHighScore(username);
        scores.insertOne(new Document("username", username)
                .append("value", score)
                .append("date", LocalDateTime.now().format(DATE_FORMAT)));
        return score > previousBest;
    }

    /** Retourne le meilleur score d'un joueur (0 s'il n'a jamais joue). */
    public int getHighScore(String username) {
        Document top = scores.find(Filters.eq("username", username))
                .sort(Sorts.descending("value")).first();
        return (top == null) ? 0 : top.getInteger("value", 0);
    }

    /** Reinitialise tous les scores d'un joueur (action administrateur, RM5). */
    public boolean resetUserScores(String username) {
        scores.deleteMany(Filters.eq("username", username));
        return true;
    }

    /** Retourne le classement mondial trie par meilleur score (RM8). */
    public List<ScoreEntry> getLeaderboard() {
        List<ScoreEntry> board = new ArrayList<>();
        List<Bson> pipeline = List.of(
                Aggregates.group("$username",
                        Accumulators.max("best", "$value"),
                        Accumulators.max("lastdate", "$date")),
                Aggregates.sort(Sorts.descending("best")));
        int rank = 1;
        for (Document doc : scores.aggregate(pipeline)) {
            String date = doc.getString("lastdate");
            board.add(new ScoreEntry(rank++, doc.getString("_id"),
                    doc.getInteger("best", 0), (date == null) ? "" : date));
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
        int totalUsers = (int) users.countDocuments();
        int totalPlayers = (int) users.countDocuments(Filters.eq("role", "joueur"));
        int gamesPlayed = (int) scores.countDocuments();

        Document top = scores.find().sort(Sorts.descending("value")).first();
        int topScore = (top == null) ? 0 : top.getInteger("value", 0);

        int averageScore = 0;
        Document avg = scores.aggregate(List.of(
                Aggregates.group(null, Accumulators.avg("avg", "$value")))).first();
        if (avg != null && avg.get("avg") instanceof Number number) {
            averageScore = (int) Math.round(number.doubleValue());
        }
        return new Stats(totalUsers, totalPlayers, gamesPlayed, topScore, averageScore);
    }

    // =====================================================================
    //  Utilitaires
    // =====================================================================
    private User toUser(Document doc) {
        String id = (doc.getObjectId("_id") != null)
                ? doc.getObjectId("_id").toHexString() : "";
        String email = doc.getString("email");
        return new User(id, doc.getString("username"),
                (email == null) ? "" : email,
                doc.getString("role"),
                getHighScore(doc.getString("username")),
                doc.getBoolean("verified", false));
    }

    /** Genere un code de verification a 6 chiffres. */
    private String generateCode() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }

    /** Ferme la connexion a la base de donnees (a l'arret de l'application). */
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
