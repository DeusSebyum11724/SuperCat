package com.supercat.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Platform;

import java.util.Properties;
import java.util.function.Consumer;

/**
 * Service d'envoi d'e-mails via le serveur SMTP de Gmail, utilise pour la
 * verification des comptes : a l'inscription, un code a 6 chiffres est
 * envoye a l'adresse e-mail du joueur.
 *
 * Remarque : l'enonce fournissait une configuration "nodemailer", qui est
 * une bibliotheque Node.js. L'application etant ecrite en Java, on utilise
 * ici Jakarta Mail, qui se connecte au meme serveur SMTP Gmail
 * (smtp.gmail.com, port 587) avec le meme compte d'envoi.
 */
public final class EmailService {

    private EmailService() {
        // classe utilitaire
    }

    /**
     * Envoie le code de verification a l'adresse indiquee (appel bloquant).
     *
     * @throws MessagingException si l'envoi echoue (SMTP, identifiants, reseau)
     */
    public static void sendVerificationCode(String toEmail, String username, String code)
            throws MessagingException {

        final String gmailUser = Config.getGmailUser();
        // un mot de passe d'application Gmail s'utilise sans les espaces
        final String gmailPass = Config.getGmailPassword().replace(" ", "");

        if (gmailUser.isBlank() || gmailPass.isBlank()) {
            throw new MessagingException("Configuration Gmail absente (config.properties).");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(gmailUser, gmailPass);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(gmailUser));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("SuperCat - Code de verification de ton compte");
        message.setText("Bonjour " + username + ",\n\n"
                + "Bienvenue dans SuperCat !\n\n"
                + "Ton code de verification est : " + code + "\n\n"
                + "Saisis ce code dans l'application pour activer ton compte et "
                + "commencer a jouer.\n\n"
                + "-- L'equipe SuperCat");

        Transport.send(message);
    }

    /**
     * Envoie le code de verification de maniere asynchrone (sur un thread
     * separe) afin de ne pas bloquer l'interface graphique. Les callbacks
     * sont executes sur le thread JavaFX.
     */
    public static void sendVerificationCodeAsync(String toEmail, String username, String code,
                                                 Runnable onSuccess, Consumer<String> onError) {
        Thread thread = new Thread(() -> {
            try {
                sendVerificationCode(toEmail, username, code);
                Platform.runLater(onSuccess);
            } catch (Exception e) {
                String reason = (e.getMessage() == null) ? e.toString() : e.getMessage();
                Platform.runLater(() -> onError.accept(reason));
            }
        }, "email-sender");
        thread.setDaemon(true);
        thread.start();
    }
}
