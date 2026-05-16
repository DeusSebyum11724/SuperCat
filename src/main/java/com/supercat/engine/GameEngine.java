package com.supercat.engine;

import com.supercat.model.Bonus;
import com.supercat.model.Cat;
import com.supercat.model.Dog;
import com.supercat.model.Exit;
import com.supercat.model.Fish;
import com.supercat.model.GameObject;
import com.supercat.model.Wall;
import com.supercat.ui.Theme;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Moteur du jeu SuperCat : coeur de l'application.
 *
 * Il pilote la boucle de jeu (frame par frame, ~60 images/seconde) :
 *  - lecture du clavier et deplacement du chat ;
 *  - deplacement autonome des chiens ;
 *  - detection des collisions (poissons, bonus, chiens, sortie) ;
 *  - gestion du chronometre et du score ;
 *  - rendu de la scene sur le canvas.
 *
 * Conforme au diagramme de classes : possede un AnimationTimer, un score,
 * un temps restant, un etat de jeu, et expose start(), stop(),
 * checkCollisions() et updateGameLogic().
 */
public class GameEngine {

    private final GraphicsContext gc;
    private final Set<KeyCode> activeKeys;
    private final GameListener listener;
    private final AnimationTimer timer;

    private GameState state = GameState.PLAYING;
    private int score = 0;
    private int timeLeft = 0;
    private int levelIndex = 0;

    private Level level;
    private Cat cat;
    private Exit exit;
    private List<Wall> walls;
    private List<Fish> fishList;
    private List<Dog> dogs;
    private List<Bonus> bonuses;

    private int fishTotal;
    private int fishCollected;

    private final List<FloatingText> effects = new ArrayList<>();

    private long lastNano = 0;
    private double secondAccumulator = 0;

    public GameEngine(GraphicsContext gc, Set<KeyCode> activeKeys, GameListener listener) {
        this.gc = gc;
        this.activeKeys = activeKeys;
        this.listener = listener;
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };
    }

    // =====================================================================
    //  Cycle de vie de la partie
    // =====================================================================

    /** Demarre une nouvelle partie complete depuis le niveau 1. */
    public void startNewGame() {
        levelIndex = 0;
        score = 0;
        loadLevel(0);
        state = GameState.PLAYING;
    }

    /** Charge un niveau et reinitialise les compteurs associes. */
    private void loadLevel(int index) {
        level = LevelLoader.load(index);
        cat = level.getCat();
        exit = level.getExit();
        walls = level.getWalls();
        fishList = level.getFish();
        dogs = level.getDogs();
        bonuses = level.getBonuses();
        fishTotal = fishList.size();
        fishCollected = 0;
        timeLeft = level.getTimeLimit();
        effects.clear();
        lastNano = 0;
        secondAccumulator = 0;
    }

    /** Demarre la boucle d'animation. */
    public void start() {
        timer.start();
    }

    /** Arrete la boucle d'animation (quand on quitte l'ecran de jeu). */
    public void stop() {
        timer.stop();
    }

    /** Passe au niveau suivant en conservant le score acquis. */
    public void nextLevel() {
        levelIndex++;
        loadLevel(levelIndex);
        state = GameState.PLAYING;
    }

    /** Recommence toute la partie apres un Game Over ou une victoire. */
    public void restart() {
        startNewGame();
    }

    public void pause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
        }
    }

    public void resume() {
        if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            lastNano = 0;
        }
    }

    // =====================================================================
    //  Boucle de jeu (appelee a chaque frame)
    // =====================================================================
    private void tick(long now) {
        if (state == GameState.PLAYING) {
            updateTimer(now);
            if (state == GameState.PLAYING) {
                updateGameLogic();
                checkCollisions();
            }
        } else {
            lastNano = 0;
        }
        render();
        listener.onTick();
    }

    /** Met a jour le compte a rebours. Le temps ecoule provoque un Game Over. */
    private void updateTimer(long now) {
        if (lastNano == 0) {
            lastNano = now;
            return;
        }
        double dt = (now - lastNano) / 1_000_000_000.0;
        lastNano = now;
        if (dt > 0.25) {
            dt = 0.25;   // limite les sauts (lag ou reprise apres une pause)
        }
        secondAccumulator += dt;
        while (secondAccumulator >= 1.0) {
            secondAccumulator -= 1.0;
            timeLeft--;
            if (timeLeft <= 0) {
                timeLeft = 0;
                triggerGameOver();
                return;
            }
        }
    }

    /**
     * Met a jour la logique du jeu : deplacement du chat selon le clavier,
     * deplacement autonome des chiens, animations des objets.
     */
    public void updateGameLogic() {
        // --- deplacement du chat selon les touches enfoncees ---
        // Fleches + WASD (clavier QWERTY) + ZQSD (clavier AZERTY)
        cat.clearVelocity();
        if (activeKeys.contains(KeyCode.LEFT) || activeKeys.contains(KeyCode.A)
                || activeKeys.contains(KeyCode.Q)) {
            cat.moveLeft();
        }
        if (activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D)) {
            cat.moveRight();
        }
        if (activeKeys.contains(KeyCode.UP) || activeKeys.contains(KeyCode.W)
                || activeKeys.contains(KeyCode.Z)) {
            cat.moveUp();
        }
        if (activeKeys.contains(KeyCode.DOWN) || activeKeys.contains(KeyCode.S)) {
            cat.moveDown();
        }

        // deplacement axe par axe : en cas de collision avec un mur on annule
        // seulement l'axe concerne, ce qui permet au chat de glisser le long
        // des murs.
        double oldX = cat.getX();
        cat.setX(cat.getX() + cat.getVx());
        if (hitsWall(cat)) {
            cat.setX(oldX);
        }
        double oldY = cat.getY();
        cat.setY(cat.getY() + cat.getVy());
        if (hitsWall(cat)) {
            cat.setY(oldY);
        }
        cat.update();

        // --- deplacement autonome des chiens (IA de patrouille) ---
        for (Dog dog : dogs) {
            double dx = dog.getX();
            double dy = dog.getY();
            dog.update();
            if (hitsWall(dog)) {
                dog.setX(dx);
                dog.setY(dy);
                dog.reverse();
            }
        }

        // --- animations des objets ---
        for (Fish f : fishList) {
            if (!f.isCollected()) {
                f.update();
            }
        }
        for (Bonus b : bonuses) {
            if (!b.isCollected()) {
                b.update();
            }
        }
        exit.update();
        effects.removeIf(FloatingText::isDead);
        for (FloatingText ft : effects) {
            ft.update();
        }
    }

    private boolean hitsWall(GameObject obj) {
        return CollisionManager.collidesAny(obj, walls);
    }

    /**
     * Verifie toutes les collisions du chat : poissons collectes, bonus
     * ramasses, collision mortelle avec un chien (RM9), sortie atteinte.
     */
    public void checkCollisions() {
        // poissons d'or : +100 points chacun (RM3)
        for (Fish f : fishList) {
            if (!f.isCollected() && CollisionManager.collide(cat, f)) {
                f.collect();
                score += f.getValue();
                fishCollected++;
                effects.add(new FloatingText(f.getCenterX(), f.getY(), "+100", Theme.FISH_DARK));
                if (fishCollected >= fishTotal) {
                    exit.unlock();   // tous les poissons collectes : sortie ouverte (RM2)
                }
            }
        }

        // objets bonus speciaux
        for (Bonus b : bonuses) {
            if (!b.isCollected() && CollisionManager.collide(cat, b)) {
                b.collect();
                if (b.getType() == Bonus.Type.POINTS) {
                    score += b.getValue();
                    effects.add(new FloatingText(b.getCenterX(), b.getY(),
                            "+" + b.getValue(), Theme.BONUS_STAR));
                } else {
                    timeLeft += b.getValue();
                    effects.add(new FloatingText(b.getCenterX(), b.getY(),
                            "+" + b.getValue() + "s", Theme.BONUS_CLOCK));
                }
            }
        }

        // collision avec un chien : Game Over immediat (RM9)
        for (Dog dog : dogs) {
            if (CollisionManager.collide(cat, dog)) {
                triggerGameOver();
                return;
            }
        }

        // sortie atteinte (uniquement si elle est deverrouillee)
        if (!exit.isLocked() && CollisionManager.collide(cat, exit)) {
            triggerLevelComplete();
        }
    }

    private void triggerLevelComplete() {
        score += timeLeft * 5;   // bonus de temps pour avoir fini en avance (RM3)
        if (levelIndex >= LevelLoader.getLevelCount() - 1) {
            state = GameState.GAME_WON;
            listener.onGameWon();
        } else {
            state = GameState.LEVEL_COMPLETE;
            listener.onLevelComplete();
        }
    }

    private void triggerGameOver() {
        state = GameState.GAME_OVER;
        listener.onGameOver();
    }

    // =====================================================================
    //  Rendu graphique
    // =====================================================================
    public void render() {
        // sol en damier (deux teintes proches)
        for (int r = 0; r < Theme.ROWS; r++) {
            for (int c = 0; c < Theme.COLS; c++) {
                gc.setFill((r + c) % 2 == 0 ? Theme.FLOOR : Theme.FLOOR_ALT);
                gc.fillRect(c * Theme.TILE, r * Theme.TILE, Theme.TILE, Theme.TILE);
            }
        }
        for (Wall w : walls) {
            w.render(gc);
        }
        exit.render(gc);
        for (Fish f : fishList) {
            f.render(gc);
        }
        for (Bonus b : bonuses) {
            b.render(gc);
        }
        for (Dog d : dogs) {
            d.render(gc);
        }
        cat.render(gc);
        for (FloatingText ft : effects) {
            ft.render(gc);
        }

        // assombrissement de l'ecran en pause
        if (state == GameState.PAUSED) {
            gc.setFill(Color.rgb(0, 0, 0, 0.45));
            gc.fillRect(0, 0, Theme.CANVAS_WIDTH, Theme.CANVAS_HEIGHT);
        }
    }

    // =====================================================================
    //  Accesseurs (utilises par le GameController pour le HUD)
    // =====================================================================
    public GameState getState() { return state; }
    public int getScore() { return score; }
    public int getTimeLeft() { return timeLeft; }
    public int getLevelNumber() { return levelIndex + 1; }
    public int getTotalLevels() { return LevelLoader.getLevelCount(); }
    public String getLevelName() { return level != null ? level.getName() : ""; }
    public int getFishCollected() { return fishCollected; }
    public int getFishTotal() { return fishTotal; }
    public boolean isExitOpen() { return exit != null && !exit.isLocked(); }
}
