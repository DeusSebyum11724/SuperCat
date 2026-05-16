package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.model.User;
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
 * Controleur de l'ecran d'authentification (cas d'utilisation UC1).
 *
 * Gere trois modes affiches dans la meme carte :
 *  - connexion a un compte existant ;
 *  - creation d'un nouveau compte ;
 *  - recuperation (reinitialisation) du mot de passe.
 */
public class LoginController {

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();

    private final StackPane root;
    private final VBox card;
    private Label message;

    public LoginController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        this.root = UIFactory.screen();
        this.card = UIFactory.card();
        this.card.setMaxWidth(400);
        this.card.setMaxHeight(Region.USE_PREF_SIZE);
        this.root.getChildren().add(card);
        showLoginForm();
    }

    public Parent getView() {
        return root;
    }

    // ===================== Mode CONNEXION =====================
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
        Button toRecover = UIFactory.linkButton("Mot de passe oublie ?");
        toRecover.setOnAction(e -> showRecoverForm());
        HBox links = new HBox(18, toRegister, toRecover);
        links.setAlignment(Pos.CENTER);

        Label hint = UIFactory.subtitle("Compte admin de demonstration :  admin  /  admin123");
        hint.setStyle(hint.getStyle() + " -fx-font-size: 11px;");

        VBox form = new VBox(12, txtUser, txtPass, message, btnLogin, links, hint);
        rebuildCard("Bienvenue !", "Connecte-toi pour entrer dans le labyrinthe", form);
    }

    // ===================== Mode INSCRIPTION =====================
    private void showRegisterForm() {
        TextField txtUser = UIFactory.textField("Pseudo (3 caracteres minimum)");
        TextField txtEmail = UIFactory.textField("Adresse e-mail");
        PasswordField txtPass = UIFactory.passwordField("Mot de passe (4 caracteres minimum)");
        PasswordField txtConfirm = UIFactory.passwordField("Confirme le mot de passe");
        message = freshMessage();

        Button btnRegister = UIFactory.primaryButton("Creer le compte");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setOnAction(e -> onRegister(txtUser.getText().trim(),
                txtEmail.getText().trim(), txtPass.getText(), txtConfirm.getText()));

        Button back = UIFactory.linkButton("Retour a la connexion");
        back.setOnAction(e -> showLoginForm());

        VBox form = new VBox(12, txtUser, txtEmail, txtPass, txtConfirm,
                message, btnRegister, back);
        rebuildCard("Creer un compte", "Rejoins l'aventure SuperCat", form);
    }

    // ===================== Mode RECUPERATION =====================
    private void showRecoverForm() {
        TextField txtUser = UIFactory.textField("Pseudo");
        TextField txtEmail = UIFactory.textField("Adresse e-mail du compte");
        PasswordField txtPass = UIFactory.passwordField("Nouveau mot de passe");
        PasswordField txtConfirm = UIFactory.passwordField("Confirme le nouveau mot de passe");
        message = freshMessage();

        Button btnReset = UIFactory.primaryButton("Reinitialiser le mot de passe");
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setOnAction(e -> onRecover(txtUser.getText().trim(),
                txtEmail.getText().trim(), txtPass.getText(), txtConfirm.getText()));

        Button back = UIFactory.linkButton("Retour a la connexion");
        back.setOnAction(e -> showLoginForm());

        VBox form = new VBox(12, txtUser, txtEmail, txtPass, txtConfirm,
                message, btnReset, back);
        rebuildCard("Mot de passe oublie", "Verifie ton identite pour le reinitialiser", form);
    }

    // ===================== Actions =====================
    private void onLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("Veuillez remplir tous les champs.");
            return;
        }
        User user = db.authenticate(username, password);
        if (user == null) {
            showErrorMessage("Pseudo ou mot de passe incorrect.");
            return;
        }
        sceneManager.setCurrentUser(user);
        if (user.isAdmin()) {
            sceneManager.showAdmin();
        } else {
            sceneManager.showMenu();
        }
    }

    private void onRegister(String username, String email, String password, String confirm) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showErrorMessage("Veuillez remplir tous les champs.");
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
        if (!db.registerUser(username, password, email)) {
            showErrorMessage("Ce pseudo est deja utilise.");
            return;
        }
        showLoginForm();
        setMessage("Compte cree avec succes ! Tu peux maintenant te connecter.", false);
    }

    private void onRecover(String username, String email, String password, String confirm) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showErrorMessage("Veuillez remplir tous les champs.");
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
        setMessage("Mot de passe reinitialise ! Connecte-toi avec le nouveau.", false);
    }

    // ===================== Utilitaires d'affichage =====================
    private void rebuildCard(String headingText, String subtitleText, VBox form) {
        form.setAlignment(Pos.CENTER);
        form.setFillWidth(true);
        VBox header = new VBox(3, UIFactory.heading(headingText), UIFactory.subtitle(subtitleText));
        header.setAlignment(Pos.CENTER);
        card.getChildren().setAll(UIFactory.catFace(78), UIFactory.title("SuperCat"), header, form);
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
