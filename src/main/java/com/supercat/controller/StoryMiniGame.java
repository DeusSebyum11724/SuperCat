package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.MusicPlayer;
import com.supercat.engine.Story;
import com.supercat.model.User;
import com.supercat.service.Settings;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;

/**
 * Cadre commun aux mini-jeux du mode Histoire.
 *
 * Cette classe fournit tout ce que les mini-jeux ont en commun : le bandeau
 * du haut (controles, chapitre, chronometre), le panneau superpose de
 * victoire / d'echec / de pause, la musique, la sauvegarde de la progression
 * et la navigation. Chaque mini-jeu concret n'a plus qu'a dessiner et animer
 * son epreuve, puis appeler {@link #win()} ou {@link #lose(String)}.
 */
public abstract class StoryMiniGame {

    /** Dimensions de la zone de jeu, identiques a celles du labyrinthe. */
    protected static final double GAME_W = Theme.CANVAS_WIDTH;
    protected static final double GAME_H = Theme.CANVAS_HEIGHT;

    protected final SceneManager sceneManager;
    protected final DatabaseManager db = DatabaseManager.getInstance();
    protected final int chapter;

    private final MusicPlayer music = MusicPlayer.instance();
    private BorderPane root;
    private StackPane overlay;
    private boolean paused;
    private boolean finished;

    private final Label goalLabel = new Label();
    private final Region timerFill = new Region();
    private final Label timeLabel = new Label();
    private Button soundBtn;

    protected StoryMiniGame(SceneManager sceneManager, int chapter) {
        this.sceneManager = sceneManager;
        this.chapter = chapter;
    }

    public final Parent getView() {
        overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(48,44,54,0.46);");
        overlay.setVisible(false);

        Node content = createContent();
        StackPane center = new StackPane(content, overlay);
        center.setMinSize(GAME_W, GAME_H);
        center.setMaxSize(GAME_W, GAME_H);

        root = new BorderPane();
        root.setStyle(Theme.BG_GRADIENT);
        root.setMinSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        root.setPrefSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        root.setMaxSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        root.setTop(buildBar());
        root.setCenter(center);
        BorderPane.setAlignment(center, Pos.CENTER);

        Scene scene = sceneManager.getScene();
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(e -> onKeyReleased(e.getCode()));

        music.start();
        return root;
    }

    // =====================================================================
    //  A implementer par chaque mini-jeu
    // =====================================================================

    /** Construit et demarre le contenu du mini-jeu (un Canvas, en general). */
    protected abstract Node createContent();

    /** Arrete la boucle d'animation du mini-jeu (appele avant de quitter). */
    protected abstract void stopLoop();

    /** Reinitialise le mini-jeu pour une nouvelle tentative. */
    protected abstract void restartGame();

    /** Touche enfoncee (hors pause). Sans effet par defaut. */
    protected void onKeyPressed(KeyCode code) { }

    /** Touche relachee. Sans effet par defaut. */
    protected void onKeyReleased(KeyCode code) { }

    // =====================================================================
    //  Services offerts aux mini-jeux
    // =====================================================================

    /** Le mini-jeu est-il en cours (ni en pause, ni termine) ? */
    protected final boolean isActive() {
        return !paused && !finished;
    }

    /** Met a jour le texte d'objectif affiche dans le bandeau. */
    protected final void setGoal(String text) {
        goalLabel.setText(text);
    }

    /** Met a jour la barre de temps du bandeau. */
    protected final void updateTimer(int secondsLeft, int secondsLimit) {
        double fraction = Math.min(1.0, secondsLeft / (double) Math.max(1, secondsLimit));
        double width = fraction * 120;
        timerFill.setMinWidth(width);
        timerFill.setPrefWidth(width);
        timerFill.setMaxWidth(width);
        timerFill.setStyle("-fx-background-radius: 4; -fx-background-color: "
                + (secondsLeft <= 10 ? Theme.DANGER : Theme.ACCENT) + ";");
        timeLabel.setText(String.format("%d:%02d", secondsLeft / 60, secondsLeft % 60));
    }

    /** Victoire : enregistre la progression et propose la suite. */
    protected final void win() {
        if (finished) {
            return;
        }
        finished = true;
        saveProgress();
        Button cont = UIFactory.primaryButton("Continuer l'histoire");
        cont.setOnAction(e -> exitToStory());
        Button replay = UIFactory.secondaryButton("Rejouer le chapitre");
        replay.setOnAction(e -> restart());
        Button home = UIFactory.secondaryButton("Accueil");
        home.setOnAction(e -> exitToHome());
        showOverlay("Chapitre termine",
                "Nora a releve le defi de cette etape.", cont, replay, home);
    }

    /** Echec : propose de recommencer. */
    protected final void lose(String reason) {
        if (finished) {
            return;
        }
        finished = true;
        Button retry = UIFactory.primaryButton("Reessayer");
        retry.setOnAction(e -> restart());
        Button back = UIFactory.secondaryButton("Mode Histoire");
        back.setOnAction(e -> exitToStory());
        Button home = UIFactory.secondaryButton("Accueil");
        home.setOnAction(e -> exitToHome());
        showOverlay("Reessaie", reason, retry, back, home);
    }

    // =====================================================================
    //  Bandeau du haut
    // =====================================================================
    private HBox buildBar() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(9, 16, 9, 12));
        bar.setStyle("-fx-background-color: " + Theme.HUD_BG + "; "
                + "-fx-effect: dropshadow(gaussian, rgba(63,59,66,0.12), 9, 0, 0, 3);");

        Button pauseBtn = iconButton("pause", this::togglePause);
        soundBtn = iconButton(Settings.isMusicEnabled() ? "sound" : "muted", null);
        soundBtn.setOnAction(e -> toggleSound());
        Button homeBtn = iconButton("home", this::exitToHome);

        Label caption = new Label("CHAPITRE " + (chapter + 1));
        caption.setStyle("-fx-font-size: 8.5px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");
        Label room = new Label(Story.room(chapter));
        room.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_DARK + ";");
        VBox chapterBlock = new VBox(1, caption, room);
        chapterBlock.setMinWidth(150);

        Label tag = new Label(Story.gameLabel(chapter));
        tag.setStyle("-fx-background-color: " + Theme.SECONDARY + "; -fx-text-fill: white; "
                + "-fx-background-radius: 9; -fx-padding: 2 10 2 10; "
                + "-fx-font-size: 11px; -fx-font-weight: 600;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        goalLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_DARK + ";");

        StackPane timerTrack = new StackPane(timerFill);
        timerTrack.setMinSize(120, 7);
        timerTrack.setMaxSize(120, 7);
        timerTrack.setStyle("-fx-background-color: #E4DCCC; -fx-background-radius: 4;");
        timerFill.setMinHeight(7);
        timerFill.setMaxHeight(7);
        StackPane.setAlignment(timerFill, Pos.CENTER_LEFT);
        timeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");
        Label timeCaption = new Label("TEMPS");
        timeCaption.setStyle("-fx-font-size: 8.5px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");
        HBox timerLine = new HBox(8, timerTrack, timeLabel);
        timerLine.setAlignment(Pos.CENTER_LEFT);
        VBox timeBlock = new VBox(3, timeCaption, timerLine);

        bar.getChildren().addAll(pauseBtn, soundBtn, homeBtn, chapterBlock, tag,
                spacer, goalLabel, timeBlock);
        return bar;
    }

    private void toggleSound() {
        boolean enabled = !Settings.isMusicEnabled();
        Settings.setMusicEnabled(enabled);
        music.setMuted(!enabled);
        soundBtn.setGraphic(icon(enabled ? "sound" : "muted"));
    }

    // =====================================================================
    //  Pause et navigation
    // =====================================================================
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.P || code == KeyCode.ESCAPE) {
            togglePause();
            return;
        }
        if (isActive()) {
            onKeyPressed(code);
        }
    }

    private void togglePause() {
        if (finished) {
            return;
        }
        if (!paused) {
            paused = true;
            Button resume = UIFactory.primaryButton("Reprendre");
            resume.setOnAction(e -> resumeGame());
            Button quit = UIFactory.secondaryButton("Quitter");
            quit.setOnAction(e -> exitToHome());
            showOverlay("Pause", "Appuie sur P pour reprendre.", resume, quit);
        } else {
            resumeGame();
        }
    }

    private void resumeGame() {
        paused = false;
        hideOverlay();
    }

    private void restart() {
        finished = false;
        paused = false;
        hideOverlay();
        restartGame();
    }

    private void exitToStory() {
        stopLoop();
        sceneManager.showStory();
    }

    private void exitToHome() {
        stopLoop();
        sceneManager.showHome();
    }

    private void saveProgress() {
        User user = sceneManager.getCurrentUser();
        if (user != null) {
            db.saveStoryProgress(user.getUsername(), chapter + 1);
        }
    }

    // =====================================================================
    //  Panneau superpose
    // =====================================================================
    private void showOverlay(String headingText, String detailText, Button... actions) {
        VBox box = UIFactory.card();
        box.setMaxWidth(360);
        box.setMaxHeight(Region.USE_PREF_SIZE);
        box.setSpacing(16);

        Label detail = UIFactory.body(detailText);
        detail.setWrapText(true);
        detail.setMaxWidth(290);
        detail.setStyle(detail.getStyle() + " -fx-text-alignment: center; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");

        VBox buttons = new VBox(9, actions);
        buttons.setFillWidth(true);
        buttons.setAlignment(Pos.CENTER);
        for (Button action : actions) {
            action.setMaxWidth(Double.MAX_VALUE);
        }

        box.getChildren().setAll(UIFactory.heading(headingText), detail, buttons);
        overlay.getChildren().setAll(box);
        overlay.setVisible(true);
        UIFactory.fadeInUp(box, 0);
    }

    private void hideOverlay() {
        overlay.setVisible(false);
        overlay.getChildren().clear();
    }

    // =====================================================================
    //  Icones du bandeau
    // =====================================================================
    private Button iconButton(String iconType, Runnable action) {
        Button button = new Button();
        button.setGraphic(icon(iconType));
        button.setMinSize(40, 40);
        button.setMaxSize(40, 40);
        String idle = "-fx-background-color: transparent; -fx-background-radius: 12; -fx-cursor: hand;";
        String hover = "-fx-background-color: #EBE4D7; -fx-background-radius: 12; -fx-cursor: hand;";
        button.setStyle(idle);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(idle));
        if (action != null) {
            button.setOnAction(e -> action.run());
        }
        return button;
    }

    private Canvas icon(String type) {
        Canvas canvas = new Canvas(22, 22);
        GraphicsContext g = canvas.getGraphicsContext2D();
        Color ink = Color.web(Theme.TEXT_DARK);
        g.setStroke(ink);
        g.setFill(ink);
        g.setLineWidth(2);
        g.setLineCap(StrokeLineCap.ROUND);
        switch (type) {
            case "pause" -> {
                g.fillRoundRect(6.5, 4, 3.6, 14, 2, 2);
                g.fillRoundRect(12, 4, 3.6, 14, 2, 2);
            }
            case "sound" -> {
                g.fillPolygon(new double[]{3, 8, 8, 3}, new double[]{8.5, 8.5, 13.5, 13.5}, 4);
                g.fillPolygon(new double[]{8, 13, 13}, new double[]{11, 5, 17}, 3);
                g.strokeArc(11.5, 6.5, 8, 9, -55, 110, ArcType.OPEN);
            }
            case "muted" -> {
                g.fillPolygon(new double[]{3, 8, 8, 3}, new double[]{8.5, 8.5, 13.5, 13.5}, 4);
                g.fillPolygon(new double[]{8, 13, 13}, new double[]{11, 5, 17}, 3);
                g.strokeLine(14, 7, 20, 15);
            }
            case "home" -> {
                g.strokeLine(4, 11, 11, 4.5);
                g.strokeLine(11, 4.5, 18, 11);
                g.strokePolygon(new double[]{6, 16, 16, 6}, new double[]{11, 11, 18, 18}, 4);
            }
            default -> { }
        }
        return canvas;
    }
}
