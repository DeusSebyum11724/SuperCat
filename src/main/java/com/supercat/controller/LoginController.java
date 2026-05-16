package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.model.User;
import com.supercat.service.EmailService;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controleur de l'ecran d'authentification (cas d'utilisation UC1) :
 * connexion, creation de compte, verification par e-mail et recuperation
 * du mot de passe.
 */
public class LoginController {

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();

    private final StackPane root;
    private final VBox card;
    private Label message;

    private String pendingUsername = "";
    private String pendingEmail = "";

    public LoginController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.root = UIFactory.screen();
        this.card = UIFactory.card();
        this.card.setMaxWidth(394);
        this.card.setMaxHeight(Region.USE_PREF_SIZE);
        this.root.getChildren().add(card);
        showLoginForm();
    }

    public Parent getView() {
        return root;
    }

    // ===================== Connexion =====================
    private void showLoginForm() {
        TextField txtUser = UIFactory.textField("Pseudo");
        PasswordField txtPass = UIFactory.passwordField("Mot de passe");
        message = freshMessage();

        Button btnLogin = UIFactory.primaryButton("Se connecter");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> onLogin(txtUser.getText().trim(), txtPass.getText()));
        txtPass.setOnAction(e -> onLogin(txtUser.getText().trim(), txtPass.getText()));

        Button toRegister = UIFactory.linkButton("Creer un compte");
        toRegister.setOnAction(e -> showRegisterForm());
        Button toRecover = UIFactory.linkButton("Mot de passe oublie");
        toRecover.setOnAction(e -> showRecoverForm());
        HBox links = new HBox(18, toRegister, toRecover);
        links.setAlignment(Pos.CENTER);

        Label hint = new Label("admin / admin123  pour l'espace administrateur");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        VBox form = new VBox(12, txtUser, txtPass, message, btnLogin, links, hint);
        rebuildCard("Connexion", form);
    }

    // ===================== Inscription =====================
    private void showRegisterForm() {
        TextField txtUser = UIFactory.textField("Pseudo (3 caracteres minimum)");
        TextField txtEmail = UIFactory.textField("Adresse e-mail");
        PasswordField txtPass = UIFactory.passwordField("Mot de passe (4 caracteres minimum)");
        PasswordField txtConfirm = UIFactory.passwordField("Confirmer le mot de passe");
        message = freshMessage();

        Button btnRegister = UIFactory.primaryButton("Creer le compte");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setOnAction(e -> onRegister(txtUser.getText().trim(),
                txtEmail.getText().trim(), txtPass.getText(), txtConfirm.getText()));

        Button back = UIFactory.linkButton("Retour");
        back.setOnAction(e -> showLoginForm());

        VBox form = new VBox(12, txtUser, txtEmail, txtPass, txtConfirm,
                message, btnRegister, back);
        rebuildCard("Creer un compte", form);
    }

    // ===================== Verification =====================
    private void showVerifyForm() {
        TextField txtCode = UIFactory.textField("Code a 6 chiffres");
        message = freshMessage();

        Label info = new Label("Code envoye a\n" + pendingEmail);
        info.setWrapText(true);
        info.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED
                + "; -fx-text-alignment: center;");
        info.setMaxWidth(Double.MAX_VALUE);
        info.setAlignment(Pos.CENTER);

        Button btnVerify = UIFactory.primaryButton("Verifier");
        btnVerify.setMaxWidth(Double.MAX_VALUE);
        btnVerify.setOnAction(e -> onVerify(txtCode.getText().trim()));
        txtCode.setOnAction(e -> onVerify(txtCode.getText().trim()));

        Button resend = UIFactory.linkButton("Renvoyer le code");
        resend.setOnAction(e -> onResendCode());
        Button back = UIFactory.linkButton("Retour");
        back.setOnAction(e -> showLoginForm());
        HBox links = new HBox(18, resend, back);
        links.setAlignment(Pos.CENTER);

        VBox form = new VBox(12, info, txtCode, message, btnVerify, links);
        rebuildCard("Verification du compte", form);
    }

    // ===================== Recuperation =====================
    private void showRecoverForm() {
        TextField txtUser = UIFactory.textField("Pseudo");
        TextField txtEmail = UIFactory.textField("Adresse e-mail du compte");
        PasswordField txtPass = UIFactory.passwordField("Nouveau mot de passe");
        PasswordField txtConfirm = UIFactory.passwordField("Confirmer le mot de passe");
        message = freshMessage();

        Button btnReset = UIFactory.primaryButton("Reinitialiser");
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setOnAction(e -> onRecover(txtUser.getText().trim(),
                txtEmail.getText().trim(), txtPass.getText(), txtConfirm.getText()));

        Button back = UIFactory.linkButton("Retour");
        back.setOnAction(e -> showLoginForm());

        VBox form = new VBox(12, txtUser, txtEmail, txtPass, txtConfirm,
                message, btnReset, back);
        rebuildCard("Mot de passe oublie", form);
    }

    // ===================== Actions =====================
    private void onLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("Renseigne tous les champs.");
            return;
        }
        User user = db.authenticate(username, password);
        if (user == null) {
            showErrorMessage("Pseudo ou mot de passe incorrect.");
            return;
        }
        if (!user.isVerified()) {
            pendingUsername = user.getUsername();
            pendingEmail = user.getEmail();
            showVerifyForm();
            setMessage("Compte non verifie. Saisis le code recu par e-mail.", true);
            return;
        }
        sceneManager.setCurrentUser(user);
        if (user.isAdmin()) {
            sceneManager.showAdmin();
        } else {
            sceneManager.showHome();
        }
    }

    private void onRegister(String username, String email, String password, String confirm) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showErrorMessage("Renseigne tous les champs.");
            return;
        }
        if (username.length() < 3) {
            showErrorMessage("Le pseudo doit contenir au moins 3 caracteres.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showErrorMessage("Adresse e-mail invalide.");
            return;
        }
        if (password.length() < 4) {
            showErrorMessage("Le mot de passe doit contenir au moins 4 caracteres.");
            return;
        }
        if (!password.equals(confirm)) {
            showErrorMessage("Les mots de passe ne correspondent pas.");
            return;
        }
        String code = db.registerUser(username, password, email);
        if (code == null) {
            showErrorMessage("Ce pseudo est deja utilise.");
            return;
        }
        pendingUsername = username;
        pendingEmail = email;
        showVerifyForm();
        setMessage("Envoi du code en cours...", false);
        EmailService.sendVerificationCodeAsync(email, username, code,
                () -> setMessage("Code envoye a " + email, false),
                err -> setMessage("Envoi impossible. Utilise \"Renvoyer le code\".", true));
    }

    private void onVerify(String code) {
        if (code.isEmpty()) {
            showErrorMessage("Saisis le code recu par e-mail.");
            return;
        }
        if (db.verifyAccount(pendingUsername, code)) {
            showLoginForm();
            setMessage("Compte verifie. Connecte-toi.", false);
        } else {
            showErrorMessage("Code incorrect.");
        }
    }

    private void onResendCode() {
        String code = db.regenerateCode(pendingUsername);
        if (code == null) {
            showErrorMessage("Compte introuvable.");
            return;
        }
        setMessage("Envoi d'un nouveau code...", false);
        EmailService.sendVerificationCodeAsync(pendingEmail, pendingUsername, code,
                () -> setMessage("Nouveau code envoye a " + pendingEmail, false),
                err -> setMessage("Envoi impossible : " + err, true));
    }

    private void onRecover(String username, String email, String password, String confirm) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showErrorMessage("Renseigne tous les champs.");
            return;
        }
        if (password.length() < 4) {
            showErrorMessage("Le mot de passe doit contenir au moins 4 caracteres.");
            return;
        }
        if (!password.equals(confirm)) {
            showErrorMessage("Les mots de passe ne correspondent pas.");
            return;
        }
        if (!db.verifyRecovery(username, email)) {
            showErrorMessage("Aucun compte ne correspond a ce pseudo et cet e-mail.");
            return;
        }
        db.resetPassword(username, password);
        showLoginForm();
        setMessage("Mot de passe reinitialise.", false);
    }

    // ===================== Utilitaires d'affichage =====================
    private void rebuildCard(String headingText, VBox form) {
        form.setAlignment(Pos.CENTER);
        form.setFillWidth(true);
        card.getChildren().setAll(UIFactory.catFace(66), UIFactory.heading(headingText), form);
        UIFactory.fadeInUp(card, 40);
    }

    private Label freshMessage() {
        Label l = UIFactory.error("");
        l.setMinHeight(18);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    /** Affiche un message d'erreur (operation prevue par le diagramme de classes). */
    public void showErrorMessage(String msg) {
        setMessage(msg, true);
    }

    private void setMessage(String text, boolean isError) {
        if (message == null) {
            return;
        }
        message.setText(text);
        message.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                + (isError ? Theme.DANGER : Theme.SUCCESS) + ";");
    }
}
