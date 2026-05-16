package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.LevelLoader;
import com.supercat.model.User;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Map;

/**
 * Page d'accueil du joueur : selection des niveaux.
 *
 * Les niveaux forment une ligne de stations (esthetique "Mini Metro") qui
 * apparait de maniere echelonnee. Chaque station indique sa difficulte et le
 * meilleur score du joueur. Le niveau a jouer "respire" doucement.
 */
public class HomeController {

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();

    public HomeController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        User user = sceneManager.getCurrentUser();
        String username = (user != null) ? user.getUsername() : "joueur";

        Map<Integer, Integer> bests = db.getLevelBests(username);
        int endlessBest = db.getEndlessBest(username);
        int completed = bests.size();
        int totalScore = 0;
        for (int value : bests.values()) {
            totalScore += value;
        }
        boolean endlessUnlocked = completed >= 3;
        int campaignCount = LevelLoader.getCampaignCount();

        // niveau a mettre en avant : le premier deverrouille non termine
        int current = -1;
        for (int i = 0; i < campaignCount; i++) {
            boolean done = bests.containsKey(i);
            boolean unlocked = (i == 0) || bests.containsKey(i - 1);
            if (unlocked && !done) {
                current = i;
                break;
            }
        }

        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(640);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setPadding(new Insets(26, 30, 24, 30));
        card.setSpacing(16);

        // --- en-tete sobre ---
        Label heading = UIFactory.heading("SuperCat");
        Label who = new Label(username);
        who.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        VBox header = new VBox(1, heading, who);
        header.setAlignment(Pos.CENTER);

        // --- bandeau de statistiques ---
        HBox stats = new HBox(10,
                statChip(completed + " / " + campaignCount, "TERMINES"),
                statChip(String.valueOf(totalScore), "SCORE"),
                statChip(String.valueOf(endlessBest), "SANS FIN"));
        stats.setAlignment(Pos.CENTER);

        // --- ligne des niveaux ---
        VBox route = new VBox(0);
        for (int i = 0; i < campaignCount; i++) {
            boolean done = bests.containsKey(i);
            boolean unlocked = (i == 0) || bests.containsKey(i - 1);
            int best = done ? bests.get(i) : 0;
            route.getChildren().add(levelStation(i, unlocked, done, best, i == current));
        }
        route.getChildren().add(endlessStation(endlessUnlocked, endlessBest));

        ScrollPane scroll = new ScrollPane(route);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(338);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // --- pied de page ---
        Button profile = UIFactory.secondaryButton("Profil");
        profile.setOnAction(e -> sceneManager.showProfile());
        Button leaderboard = UIFactory.secondaryButton("Classement");
        leaderboard.setOnAction(e -> sceneManager.showLeaderboard());
        Button logout = UIFactory.secondaryButton("Deconnexion");
        logout.setOnAction(e -> sceneManager.logout());
        HBox footer = new HBox(10, profile, leaderboard, logout);
        footer.setAlignment(Pos.CENTER);

        card.getChildren().setAll(header, stats, scroll, footer);
        root.getChildren().add(card);

        // apparition echelonnee des elements
        UIFactory.fadeInUp(header, 60);
        UIFactory.fadeInUp(stats, 150);
        int delay = 260;
        for (Node station : route.getChildren()) {
            UIFactory.fadeInUp(station, delay);
            delay += 52;
        }
        UIFactory.fadeInUp(footer, delay + 60);
        return root;
    }

    // ----- Bandeau de statistiques -----
    private VBox statChip(String value, String caption) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: "
                + Theme.ACCENT + ";");
        Label captionLabel = new Label(caption);
        captionLabel.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");
        VBox chip = new VBox(2, valueLabel, captionLabel);
        chip.setAlignment(Pos.CENTER);
        chip.setMinWidth(168);
        chip.setStyle("-fx-background-color: #F2ECE1; -fx-background-radius: 14; "
                + "-fx-padding: 11 14 11 14;");
        return chip;
    }

    // ----- Station d'un niveau de campagne -----
    private HBox levelStation(int index, boolean unlocked, boolean done, int best, boolean current) {
        StackPane node = circleNode(String.valueOf(index + 1), unlocked, done, current);

        Label name = new Label(LevelLoader.getLevelName(index));
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        String difficulty = LevelLoader.getDifficultyLabel(index);
        Label diffTag = UIFactory.tag(difficulty, difficultyColor(difficulty), "white");

        Label score = new Label(done ? best + " pts" : "");
        score.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + Theme.ACCENT + ";");

        return station(node, name, diffTag, score, unlocked,
                () -> sceneManager.showCampaignLevel(index));
    }

    // ----- Station du mode sans fin -----
    private HBox endlessStation(boolean unlocked, int best) {
        StackPane node = squareNode(unlocked);

        Label name = new Label("Mode sans fin");
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        Label tag = UIFactory.tag("Infini", Theme.GOLD, "white");

        Label score = new Label(unlocked ? (best + " salles") : "3 niveaux requis");
        score.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                + (unlocked ? Theme.GOLD : Theme.TEXT_MUTED) + ";");

        return station(node, name, tag, score, unlocked, sceneManager::showEndless);
    }

    /** Assemble une station : noeud, nom, etiquette, score, interaction. */
    private HBox station(StackPane node, Label name, Label tag, Label score,
                         boolean unlocked, Runnable onClick) {
        HBox titleRow = new HBox(8, name, tag);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleRow, Priority.ALWAYS);

        HBox row = new HBox(12, node, titleRow, score);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 16, 0, 4));

        if (unlocked) {
            String idle = "-fx-background-color: transparent;";
            String hover = "-fx-background-color: rgba(224,138,111,0.12);";
            row.setStyle(idle);
            row.setOnMouseEntered(e -> {
                row.setStyle(hover);
                row.setScaleX(1.012);
                row.setScaleY(1.012);
            });
            row.setOnMouseExited(e -> {
                row.setStyle(idle);
                row.setScaleX(1.0);
                row.setScaleY(1.0);
            });
            row.setOnMouseClicked(e -> onClick.run());
            row.setStyle(idle + " -fx-cursor: hand;");
        } else {
            row.setOpacity(0.45);
        }
        return row;
    }

    // ----- Noeuds geometriques de la "ligne" -----
    private StackPane circleNode(String text, boolean unlocked, boolean done, boolean pulse) {
        Circle circle = new Circle(17);
        if (done) {
            circle.setFill(Color.web(Theme.ACCENT));
        } else if (unlocked) {
            circle.setFill(Color.web("#FBF7F0"));
            circle.setStroke(Color.web(Theme.ACCENT));
            circle.setStrokeWidth(2.5);
        } else {
            circle.setFill(Color.web(Theme.LOCKED));
        }
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                + (unlocked && !done ? Theme.ACCENT : "white") + ";");
        StackPane inner = new StackPane(circle, label);
        if (pulse) {
            UIFactory.breathe(inner, 1.14, 1000);
        }
        return assembleNode(inner);
    }

    private StackPane squareNode(boolean unlocked) {
        Region square = new Region();
        square.setMinSize(32, 32);
        square.setMaxSize(32, 32);
        square.setStyle("-fx-background-radius: 9; -fx-background-color: "
                + (unlocked ? Theme.GOLD : Theme.LOCKED) + ";");
        StackPane inner = new StackPane(square);
        return assembleNode(inner);
    }

    /** Pose un noeud sur un segment de "ligne" vertical (continuite du trace). */
    private StackPane assembleNode(Node inner) {
        Region line = new Region();
        line.setMinWidth(4);
        line.setMaxWidth(4);
        line.setMinHeight(66);
        line.setMaxHeight(Double.MAX_VALUE);
        line.setStyle("-fx-background-color: #D9CFC2;");

        StackPane node = new StackPane(line, inner);
        node.setMinWidth(50);
        node.setPrefWidth(50);
        return node;
    }

    private String difficultyColor(String difficulty) {
        return switch (difficulty) {
            case "Facile" -> Theme.SUCCESS;
            case "Moyen" -> Theme.SECONDARY;
            case "Difficile" -> Theme.GOLD;
            case "Expert" -> Theme.ACCENT;
            default -> Theme.DANGER;
        };
    }
}
