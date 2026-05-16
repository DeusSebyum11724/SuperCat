package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.Story;
import com.supercat.model.User;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Ecran du mode Histoire : presente le texte narratif du chapitre courant
 * avant de lancer le labyrinthe correspondant. L'histoire est lineaire et
 * la progression du joueur est conservee entre deux sessions.
 */
public class StoryController {

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();

    public StoryController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        User user = sceneManager.getCurrentUser();
        String username = (user != null) ? user.getUsername() : "";
        int progress = db.getStoryProgress(username);   // chapitres deja termines
        int total = Story.chapterCount();

        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(440);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setSpacing(16);

        if (progress >= total) {
            buildEnding(card, username);
        } else {
            buildChapter(card, progress, total);
        }

        root.getChildren().add(card);
        return root;
    }

    private void buildChapter(VBox card, int chapter, int total) {
        Label caption = new Label("CHAPITRE " + (chapter + 1) + " / " + total);
        caption.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.ACCENT + ";");
        VBox header = new VBox(4, caption, UIFactory.heading(Story.room(chapter)));
        header.setAlignment(Pos.CENTER);

        Label text = UIFactory.body(Story.narrative(chapter));
        text.setWrapText(true);
        text.setMaxWidth(360);
        text.setStyle(text.getStyle() + " -fx-text-alignment: center; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");

        Button play = UIFactory.primaryButton("Jouer le chapitre");
        play.setOnAction(e -> sceneManager.showStoryLevel(chapter));
        Button home = UIFactory.secondaryButton("Retour a l'accueil");
        home.setOnAction(e -> sceneManager.showHome());
        play.setMaxWidth(Double.MAX_VALUE);
        home.setMaxWidth(Double.MAX_VALUE);
        VBox actions = new VBox(9, play, home);
        actions.setFillWidth(true);

        card.getChildren().setAll(UIFactory.catFace(72), header, text, actions);
    }

    private void buildEnding(VBox card, String username) {
        Label text = UIFactory.body(Story.ending());
        text.setWrapText(true);
        text.setMaxWidth(360);
        text.setStyle(text.getStyle() + " -fx-text-alignment: center; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");

        Button replay = UIFactory.primaryButton("Recommencer l'histoire");
        replay.setOnAction(e -> {
            db.resetStory(username);
            sceneManager.showStory();
        });
        Button home = UIFactory.secondaryButton("Retour a l'accueil");
        home.setOnAction(e -> sceneManager.showHome());
        replay.setMaxWidth(Double.MAX_VALUE);
        home.setMaxWidth(Double.MAX_VALUE);
        VBox actions = new VBox(9, replay, home);
        actions.setFillWidth(true);

        card.getChildren().setAll(UIFactory.catFace(72),
                UIFactory.heading("Histoire terminee"), text, actions);
    }
}
