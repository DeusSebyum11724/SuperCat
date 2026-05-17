package com.supercat.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * Charge la configuration sensible de l'application (identifiants MongoDB et
 * Gmail) sans la stocker dans le code source.
 *
 * Ordre de priorite :
 *  1. variables d'environnement (MONGODB_URI_CATALOG, GMAIL_USER,
 *     GMAIL_APP_PASSWORD) ;
 *  2. premier fichier "config.properties" trouve parmi :
 *       - le repertoire courant (developpement : mvn javafx:run) ;
 *       - ~/Library/Application Support/SuperCat/ (application macOS) ;
 *       - ~/.supercat/ (autres systemes).
 *
 * Le fichier config.properties est exclu de Git : les identifiants ne sont
 * donc jamais publies sur le depot public.
 */
public final class Config {

    private static final Properties FILE_PROPS = new Properties();

    static {
        for (Path path : candidateFiles()) {
            if (Files.exists(path)) {
                try (InputStream in = new FileInputStream(path.toFile())) {
                    FILE_PROPS.load(in);
                    break;
                } catch (IOException e) {
                    System.err.println("Lecture de " + path + " impossible : " + e.getMessage());
                }
            }
        }
    }

    private Config() {
        // classe utilitaire
    }

    /**
     * Emplacements ou chercher config.properties, par ordre de priorite. Le
     * repertoire courant sert au developpement ; les emplacements dans le
     * dossier personnel servent a l'application installee, dont le repertoire
     * courant n'est pas celui du projet.
     */
    private static List<Path> candidateFiles() {
        String home = System.getProperty("user.home", ".");
        return List.of(
                Path.of("config.properties"),
                Path.of(home, "Library", "Application Support", "SuperCat",
                        "config.properties"),
                Path.of(home, ".supercat", "config.properties"));
    }

    private static String resolve(String envName, String propertyName) {
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return FILE_PROPS.getProperty(propertyName, "").trim();
    }

    /** Chaine de connexion MongoDB. */
    public static String getMongoUri() {
        return resolve("MONGODB_URI_CATALOG", "mongodb.uri");
    }

    /** Adresse Gmail utilisee pour l'envoi des e-mails. */
    public static String getGmailUser() {
        return resolve("GMAIL_USER", "gmail.user");
    }

    /** Mot de passe d'application Gmail. */
    public static String getGmailPassword() {
        return resolve("GMAIL_APP_PASSWORD", "gmail.password");
    }

    /** Vrai si la configuration MongoDB est disponible. */
    public static boolean hasDatabaseConfig() {
        return !getMongoUri().isBlank();
    }

    /** Vrai si la configuration Gmail (envoi d'e-mails) est disponible. */
    public static boolean hasEmailConfig() {
        return !getGmailUser().isBlank() && !getGmailPassword().isBlank();
    }
}
