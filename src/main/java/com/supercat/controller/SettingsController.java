package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.engine.MusicPlayer;
import com.supercat.service.Settings;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Ecran des parametres : affichage (plein ecran), audio (musique, volume)
 * et accessibilite (animations reduites). Les choix sont conserves entre
 * deux lancements (classe Settings).
 */
public class SettingsController {

    private final SceneManager sceneManager;

    public SettingsController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(460);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setSpacing(15);

        VBox content = new VBox(11,
                section("Affichage"),
                row("Plein ecran", toggle(Settings.isFullscreen(), this::onFullscreen)),
                section("Audio"),
                row("Musique", toggle(Settings.isMusicEnabled(), enabled -> {
                    Settings.setMusicEnabled(enabled);
                    MusicPlayer.instance().setMuted(!enabled);
                })),
                row("Volume", volumeControl()),
                section("Accessibilite"),
                row("Animations reduites",
                        toggle(Settings.isReducedMotion(), Settings::setReducedMotion)));
        content.setFillWidth(true);

        Button quit = UIFactory.dangerButton("Quitter le jeu");
        quit.setMaxWidth(Double.MAX_VALUE);
        quit.setOnAction(e -> Platform.exit());
        Button back = UIFactory.secondaryButton("Retour");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(e -> sceneManager.showHome());

        card.getChildren().setAll(UIFactory.heading("Parametres"), content, quit, back);
        root.getChildren().add(card);
        return root;
    }

    private void onFullscreen(boolean value) {
        Settings.setFullscreen(value);
        Stage stage = sceneManager.getStage();
        stage.setFullScreen(value);
        if (!value) {
            stage.setWidth(900);
            stage.setHeight(740);
            stage.centerOnScreen();
        }
    }

    // ----- Composants -----
    private Label section(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + Theme.ACCENT + ";");
        return l;
    }

    private HBox row(String label, Node control) {
        Label name = new Label(label);
        name.setStyle("-fx-font-size: 14px; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(12, name, spacer, control);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /** Bouton bascule Active / Desactive. */
    private Button toggle(boolean initial, Consumer<Boolean> onChange) {
        Button button = new Button();
        boolean[] state = {initial};
        button.setMinWidth(116);
        Runnable render = () -> button.setStyle(
                "-fx-background-radius: 20; -fx-cursor: hand; -fx-font-size: 13px; "
                + "-fx-font-weight: bold; -fx-padding: 8 16 8 16; -fx-text-fill: white; "
                + "-fx-background-color: " + (state[0] ? Theme.SUCCESS : "#BCB3C6") + ";");
        button.setText(initial ? "Active" : "Desactive");
        render.run();
        button.setOnAction(e -> {
            state[0] = !state[0];
            button.setText(state[0] ? "Active" : "Desactive");
            render.run();
            onChange.accept(state[0]);
        });
        return button;
    }

    /** Controle de volume segmente (10 paliers cliquables). */
    private HBox volumeControl() {
        HBox box = new HBox(4);
        box.setAlignment(Pos.CENTER_RIGHT);
        Region[] segments = new Region[10];
        Runnable render = () -> {
            int level = (int) Math.round(Settings.getMusicVolume() * 10);
            for (int i = 0; i < segments.length; i++) {
                segments[i].setStyle("-fx-background-radius: 4; -fx-cursor: hand; "
                        + "-fx-background-color: " + (i < level ? Theme.ACCENT : "#E2DACB") + ";");
            }
        };
        for (int i = 0; i < segments.length; i++) {
            Region segment = new Region();
            segment.setMinSize(15, 22);
            segment.setMaxSize(15, 22);
            final int index = i;
            segment.setOnMouseClicked(e -> {
                Settings.setMusicVolume((index + 1) / 10.0);
                MusicPlayer.instance().refreshVolume();
                render.run();
            });
            segments[i] = segment;
            box.getChildren().add(segment);
        }
        render.run();
        return box;
    }
}
