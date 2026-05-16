package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.model.User;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controleur de l'espace d'administration (cas d'utilisation UC6, UC7, UC8).
 *
 * Permet a l'administrateur de consulter et rechercher les comptes, de
 * supprimer un compte joueur, de reinitialiser les scores d'un joueur
 * (regle RM5) et de consulter les statistiques de la plateforme (RM10).
 */
public class AdminController {

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();

    private final ObservableList<User> masterData = FXCollections.observableArrayList();
    private TableView<User> table;

    private final Label statUsers = bigStat();
    private final Label statPlayers = bigStat();
    private final Label statGames = bigStat();
    private final Label statTop = bigStat();
    private final Label statAvg = bigStat();

    public AdminController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.setStyle(Theme.BG_GRADIENT);
        root.setTop(buildHeader());

        HBox center = new HBox(16, buildUserPanel(), buildStatsPanel());
        center.setPadding(new Insets(16));
        root.setCenter(center);

        refresh();
        return root;
    }

    // ----- En-tete -----
    private HBox buildHeader() {
        User admin = sceneManager.getCurrentUser();
        Label title = new Label("Espace Administrateur");
        title.setStyle("-fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label who = new Label("Connecte en tant que : " + (admin != null ? admin.getUsername() : ""));
        who.setStyle("-fx-text-fill: #B5C1CC; -fx-font-size: 13px;");
        VBox titleBox = new VBox(2, title, who);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logout = UIFactory.dangerButton("Se deconnecter");
        logout.setOnAction(e -> sceneManager.logout());

        HBox header = new HBox(14, titleBox, spacer, logout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color: #243140;");
        return header;
    }

    // ----- Panneau de gestion des joueurs -----
    private VBox buildUserPanel() {
        TextField search = UIFactory.textField("Rechercher un joueur par pseudo...");
        FilteredList<User> filtered = new FilteredList<>(masterData, u -> true);
        search.textProperty().addListener((obs, oldVal, val) -> {
            String query = (val == null) ? "" : val.trim().toLowerCase();
            filtered.setPredicate(u -> query.isEmpty()
                    || u.getUsername().toLowerCase().contains(query));
        });

        table = new TableView<>();
        table.setItems(filtered);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Aucun compte a afficher."));
        table.getColumns().add(col("Pseudo", "username", 120));
        table.getColumns().add(col("E-mail", "email", 175));
        table.getColumns().add(col("Role", "role", 70));
        table.getColumns().add(col("Verifie", "verifiedLabel", 70));
        table.getColumns().add(col("Score total", "highScore", 105));
        VBox.setVgrow(table, Priority.ALWAYS);

        Button delete = UIFactory.dangerButton("Supprimer le compte");
        delete.setOnAction(e -> onDelete());
        Button reset = UIFactory.secondaryButton("Reinitialiser les scores");
        reset.setOnAction(e -> onResetScores());
        Button refresh = UIFactory.secondaryButton("Actualiser");
        refresh.setOnAction(e -> refresh());
        HBox actions = new HBox(10, delete, reset, refresh);

        VBox panel = new VBox(12, UIFactory.heading("Gestion des joueurs"),
                search, table, actions);
        panel.setPadding(new Insets(18));
        panel.setStyle(Theme.CARD_STYLE);
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private TableColumn<User, ?> col(String title, String property, double width) {
        TableColumn<User, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    // ----- Panneau des statistiques -----
    private VBox buildStatsPanel() {
        VBox panel = new VBox(10, UIFactory.heading("Statistiques"),
                statRow("Comptes au total", statUsers),
                statRow("Joueurs inscrits", statPlayers),
                statRow("Parties jouees", statGames),
                statRow("Meilleur score global", statTop),
                statRow("Score moyen", statAvg));
        panel.setPadding(new Insets(18));
        panel.setStyle(Theme.CARD_STYLE);
        panel.setMinWidth(235);
        panel.setMaxWidth(235);
        return panel;
    }

    private VBox statRow(String caption, Label value) {
        Label c = new Label(caption);
        c.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        VBox box = new VBox(2, c, value);
        box.setStyle("-fx-background-color: #F4F6F7; -fx-background-radius: 10; -fx-padding: 10 12 10 12;");
        return box;
    }

    private static Label bigStat() {
        Label l = new Label("-");
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + Theme.ACCENT + ";");
        return l;
    }

    // ----- Actions -----
    private void onDelete() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Selectionne d'abord un compte dans la liste.");
            return;
        }
        if (selected.isAdmin()) {
            info("Les comptes administrateur sont proteges : suppression impossible.");
            return;
        }
        User current = sceneManager.getCurrentUser();
        if (current != null && selected.getUsername().equals(current.getUsername())) {
            info("Tu ne peux pas supprimer ton propre compte.");
            return;
        }
        if (confirm("Supprimer definitivement le compte \"" + selected.getUsername()
                + "\" ainsi que tous ses scores ?")) {
            db.deleteUser(selected.getUsername());
            refresh();
        }
    }

    private void onResetScores() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Selectionne d'abord un compte dans la liste.");
            return;
        }
        if (confirm("Reinitialiser tous les scores du joueur \"" + selected.getUsername() + "\" ?")) {
            db.resetUserScores(selected.getUsername());
            refresh();
        }
    }

    /** Recharge la liste des comptes et les statistiques depuis la base. */
    private void refresh() {
        masterData.setAll(db.getAllUsers());
        DatabaseManager.Stats stats = db.getStatistics();
        statUsers.setText(String.valueOf(stats.totalUsers()));
        statPlayers.setText(String.valueOf(stats.totalPlayers()));
        statGames.setText(String.valueOf(stats.gamesPlayed()));
        statTop.setText(stats.topScore() + " pts");
        statAvg.setText(stats.averageScore() + " pts");
    }

    // ----- Boites de dialogue -----
    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
