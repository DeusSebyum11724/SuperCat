package com.supercat.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Charge la configuration sensible de l'application (identifiants MongoDB et
 * Gmail) sans la stocker dans le code source.
 *
 * Ordre de priorite :
 *  1. variables d'environnement (MONGODB_URI_CATALOG, GMAIL_USER,
 *     GMAIL_APP_PASSWORD) ;
 *  2. fichier "config.properties" a la racine du projet.
 *
 * Le fichier config.properties est exclu de Git : les identifiants ne sont
 * donc jamais publies sur le depot public.
 */
public final class Config {

    private static final Properties FILE_PROPS = new Properties();

    static {
        Path path = Path.of("config.properties");
        if (Files.exists(path)) {
            try (InputStream in = new FileInputStream(path.toFile())) {
                FILE_PROPS.load(in);
            } catch (IOException e) {
                System.err.println("Lecture de config.properties impossible : " + e.getMessage());
            }
        }
    }

    private Config() {
        // classe utilitaire
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
