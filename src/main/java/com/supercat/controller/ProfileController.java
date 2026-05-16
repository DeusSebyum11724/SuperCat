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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Ecran de profil : l'utilisateur consulte ses informations, modifie son
 * adresse e-mail et change son mot de passe. Mise en page compacte tenant
 * entierement dans la hauteur de l'ecran.
 */
public class ProfileController {

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();
    private Label message;

    public ProfileController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        User user = sceneManager.getCurrentUser();

        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(420);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setSpacing(13);
        card.setPadding(new javafx.geometry.Insets(24));

        message = UIFactory.error("");
        message.setMinHeight(16);
        message.setMaxWidth(Double.MAX_VALUE);
        message.setAlignment(Pos.CENTER);

        VBox infoBox = new VBox(4,
                infoLine("Pseudo", user.getUsername()),
                infoLine("Role", user.getRole()),
                infoLine("Score total", user.getHighScore() + " points"));
        infoBox.setStyle("-fx-background-color: #F2ECE1; -fx-background-radius: 12; "
                + "-fx-padding: 12 14 12 14;");

        TextField emailField = UIFactory.textField("Adresse e-mail");
        emailField.setText(user.getEmail() == null ? "" : user.getEmail());
        Button saveEmail = UIFactory.primaryButton("Enregistrer l'e-mail");
        saveEmail.setMaxWidth(Double.MAX_VALUE);
        saveEmail.setOnAction(e -> onSaveEmail(user, emailField.getText().trim()));
        VBox emailSection = new VBox(7, sectionLabel("Adresse e-mail"), emailField, saveEmail);
        emailSection.setFillWidth(true);

        PasswordField currentPw = UIFactory.passwordField("Mot de passe actuel");
        PasswordField newPw = UIFactory.passwordField("Nouveau mot de passe");
        PasswordField confirmPw = UIFactory.passwordField("Confirmer le mot de passe");
        Button changePw = UIFactory.primaryButton("Changer le mot de passe");
        changePw.setMaxWidth(Double.MAX_VALUE);
        changePw.setOnAction(e -> onChangePassword(user, currentPw, newPw, confirmPw));
        VBox pwSection = new VBox(7, sectionLabel("Mot de passe"),
                currentPw, newPw, confirmPw, changePw);
        pwSection.setFillWidth(true);

        Button back = UIFactory.secondaryButton("Retour a l'accueil");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(e -> sceneManager.showHome());

        VBox content = new VBox(13, infoBox, emailSection, pwSection, message, back);
        content.setFillWidth(true);

        card.getChildren().setAll(UIFactory.heading("Mon profil"), content);
        root.getChildren().add(card);
        return root;
    }

    // ----- Actions -----
    private void onSaveEmail(User user, String email) {
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            setMessage("Adresse e-mail invalide.", true);
            return;
        }
        db.updateEmail(user.getUsername(), email);
        user.setEmail(email);
        setMessage("Adresse e-mail mise a jour.", false);
    }

    private void onChangePassword(User user, PasswordField currentPw,
                                  PasswordField newPw, PasswordField confirmPw) {
        String current = currentPw.getText();
        String fresh = newPw.getText();
        String confirm = confirmPw.getText();

        if (current.isEmpty() || fresh.isEmpty()) {
            setMessage("Remplis les champs de mot de passe.", true);
            return;
        }
        if (db.authenticate(user.getUsername(), current) == null) {
            setMessage("Mot de passe actuel incorrect.", true);
            return;
        }
        if (fresh.length() < 4) {
            setMessage("Le nouveau mot de passe doit faire 4 caracteres minimum.", true);
            return;
        }
        if (!fresh.equals(confirm)) {
            setMessage("Les nouveaux mots de passe ne correspondent pas.", true);
            return;
        }
        db.changePassword(user.getUsername(), fresh);
        currentPw.clear();
        newPw.clear();
        confirmPw.clear();
        setMessage("Mot de passe modifie.", false);
    }

    // ----- Utilitaires -----
    private HBox infoLine(String label, String value) {
        Label l = UIFactory.subtitle(label);
        Label v = UIFactory.body(value);
        v.setStyle(v.getStyle() + " -fx-font-weight: 600;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(l, spacer, v);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: " + Theme.ACCENT + ";");
        return l;
    }

    private void setMessage(String text, boolean isError) {
        message.setText(text);
        message.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: "
                + (isError ? Theme.DANGER : Theme.SUCCESS) + ";");
    }
}
