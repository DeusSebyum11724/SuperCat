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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Gestionnaire de la base de donnees MongoDB (patron Singleton).
 *
 * Deux collections :
 *  - "users"  : comptes (pseudo, mot de passe hache BCrypt, e-mail, role,
 *               statut de verification) ;
 *  - "scores" : scores des parties. Le champ "level" vaut 0..11 pour les
 *               niveaux de la campagne, et -1 pour le mode sans fin
 *               (la valeur represente alors le nombre de salles franchies).
 *
 * Les mots de passe ne sont jamais stockes en clair : ils sont haches avec
 * BCrypt (regle metier RM1).
 */
public class DatabaseManager {

    /** Marqueur de niveau pour les scores du mode sans fin. */
    public static final int ENDLESS_LEVEL = -1;

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
            database.runCommand(new Document("ping", 1));
            users.createIndex(Indexes.ascending("username"), new IndexOptions().unique(true));
            System.out.println("[SuperCat] Connexion MongoDB reussie (base 'supercat').");
        } catch (RuntimeException e) {
            throw new IllegalStateException("Connexion a la base MongoDB impossible. "
                    + "Verifiez votre connexion Internet et l'autorisation d'acces (IP) "
                    + "sur MongoDB Atlas.\nDetail : " + e.getMessage(), e);
        }
    }

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

    /** Verifie un compte a l'aide du code recu par e-mail. */
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

    /** Genere un nouveau code de verification (re-envoi). */
    public String regenerateCode(String username) {
        Document doc = users.find(Filters.eq("username", username)).first();
        if (doc == null) {
            return null;
        }
        String code = generateCode();
        users.updateOne(Filters.eq("username", username), Updates.set("verificationCode", code));
        return code;
    }

    public boolean usernameExists(String username) {
        return users.find(Filters.eq("username", username)).first() != null;
    }

    public User getUserByUsername(String username) {
        Document doc = users.find(Filters.eq("username", username)).first();
        return (doc == null) ? null : toUser(doc);
    }

    /** Supprime un compte joueur et tous ses scores (action admin, RM5). */
    public boolean deleteUser(String username) {
        Document doc = users.find(Filters.eq("username", username)).first();
        if (doc == null || "admin".equals(doc.getString("role"))) {
            return false;
        }
        users.deleteOne(Filters.eq("username", username));
        scores.deleteMany(Filters.eq("username", username));
        return true;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        for (Document doc : users.find().sort(Sorts.ascending("role", "username"))) {
            list.add(toUser(doc));
        }
        return list;
    }

    public boolean updateEmail(String username, String email) {
        return users.updateOne(Filters.eq("username", username),
                Updates.set("email", email)).getMatchedCount() > 0;
    }

    public boolean changePassword(String username, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        return users.updateOne(Filters.eq("username", username),
                Updates.set("password", hash)).getMatchedCount() > 0;
    }

    public boolean verifyRecovery(String username, String email) {
        Document doc = users.find(Filters.eq("username", username)).first();
        return doc != null && email.equalsIgnoreCase(doc.getString("email"));
    }

    public boolean resetPassword(String username, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        return users.updateOne(Filters.eq("username", username),
                Updates.set("password", hash)).getMatchedCount() > 0;
    }

    // =====================================================================
    //  Scores (collection "scores")
    // =====================================================================

    /**
     * Enregistre le score obtenu sur un niveau et indique s'il s'agit d'un
     * nouveau record personnel pour ce niveau (regles RM3 et RM8).
     */
    public boolean saveLevelScore(String username, int level, int value) {
        int previousBest = getLevelBest(username, level);
        scores.insertOne(new Document("username", username)
                .append("level", level)
                .append("value", value)
                .append("date", LocalDateTime.now().format(DATE_FORMAT)));
        return value > previousBest;
    }

    /** Meilleur score d'un joueur sur un niveau donne (0 si jamais joue). */
    public int getLevelBest(String username, int level) {
        Document top = scores.find(Filters.and(Filters.eq("username", username),
                        Filters.eq("level", level)))
                .sort(Sorts.descending("value")).first();
        return (top == null) ? 0 : top.getInteger("value", 0);
    }

    /** Meilleurs scores de la campagne d'un joueur : indice de niveau -> score. */
    public Map<Integer, Integer> getLevelBests(String username) {
        Map<Integer, Integer> bests = new HashMap<>();
        List<Bson> pipeline = List.of(
                Aggregates.match(Filters.and(Filters.eq("username", username),
                        Filters.gte("level", 0))),
                Aggregates.group("$level", Accumulators.max("best", "$value")));
        for (Document doc : scores.aggregate(pipeline)) {
            bests.put(doc.getInteger("_id"), doc.getInteger("best", 0));
        }
        return bests;
    }

    /** Score total de la campagne (somme des meilleurs scores par niveau). */
    public int getTotalScore(String username) {
        int total = 0;
        for (int best : getLevelBests(username).values()) {
            total += best;
        }
        return total;
    }

    /** Enregistre un resultat du mode sans fin (nombre de salles franchies). */
    public boolean saveEndlessResult(String username, int depth) {
        return saveLevelScore(username, ENDLESS_LEVEL, depth);
    }

    /** Meilleur resultat du mode sans fin (salles franchies). */
    public int getEndlessBest(String username) {
        return getLevelBest(username, ENDLESS_LEVEL);
    }

    /** Marqueur de niveau pour la progression du mode Histoire. */
    private static final int STORY_LEVEL = -2;

    /** Enregistre la progression du mode Histoire (nombre de chapitres termines). */
    public boolean saveStoryProgress(String username, int chaptersCompleted) {
        return saveLevelScore(username, STORY_LEVEL, chaptersCompleted);
    }

    /** Nombre de chapitres du mode Histoire termines par le joueur. */
    public int getStoryProgress(String username) {
        return getLevelBest(username, STORY_LEVEL);
    }

    /** Remet la progression du mode Histoire a zero. */
    public void resetStory(String username) {
        scores.deleteMany(Filters.and(Filters.eq("username", username),
                Filters.eq("level", STORY_LEVEL)));
    }

    /** Reinitialise tous les scores d'un joueur (action administrateur, RM5). */
    public boolean resetUserScores(String username) {
        scores.deleteMany(Filters.eq("username", username));
        return true;
    }

    /** Classement mondial : score total de campagne par joueur (RM8). */
    public List<ScoreEntry> getLeaderboard() {
        List<ScoreEntry> board = new ArrayList<>();
        List<Bson> pipeline = List.of(
                Aggregates.match(Filters.gte("level", 0)),
                Aggregates.group(new Document("u", "$username").append("l", "$level"),
                        Accumulators.max("best", "$value")),
                Aggregates.group("$_id.u", Accumulators.sum("total", "$best")),
                Aggregates.sort(Sorts.descending("total")));
        int rank = 1;
        for (Document doc : scores.aggregate(pipeline)) {
            board.add(new ScoreEntry(rank++, doc.getString("_id"),
                    doc.getInteger("total", 0), ""));
        }
        return board;
    }

    // =====================================================================
    //  Statistiques (espace administrateur, RM10)
    // =====================================================================
    public record Stats(int totalUsers, int totalPlayers, int gamesPlayed,
                         int topScore, int averageScore) {
    }

    public Stats getStatistics() {
        int totalUsers = (int) users.countDocuments();
        int totalPlayers = (int) users.countDocuments(Filters.eq("role", "joueur"));
        int gamesPlayed = (int) scores.countDocuments();

        Document top = scores.find(Filters.gte("level", 0))
                .sort(Sorts.descending("value")).first();
        int topScore = (top == null) ? 0 : top.getInteger("value", 0);

        int averageScore = 0;
        Document avg = scores.aggregate(List.of(
                Aggregates.match(Filters.gte("level", 0)),
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
                getTotalScore(doc.getString("username")),
                doc.getBoolean("verified", false));
    }

    private String generateCode() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
