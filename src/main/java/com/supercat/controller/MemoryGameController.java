package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.engine.MemoryDeck;
import com.supercat.ui.Theme;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 * Mini-jeu du Vieux Moulin et du Grand Foyer : le jeu des paires.
 *
 * Des cartes sont posees face cachee. Le joueur en retourne deux : si les
 * symboles correspondent, la paire reste decouverte ; sinon les cartes se
 * retournent. Il faut retrouver toutes les paires avant la fin du temps.
 * Le plateau est fourni par {@link MemoryDeck} (distribution deterministe).
 */
public class MemoryGameController extends StoryMiniGame {

    private static final double CARD = 100;
    private static final double GAP = 16;

    private MemoryDeck deck;
    private Canvas canvas;
    private GraphicsContext g;
    private AnimationTimer timer;

    private double originX;
    private double originY;

    private boolean[] matched;
    private double[] flip;          // progres de retournement, lisse (0 cache, 1 face)
    private int first;
    private int second;
    private double mismatchTimer;   // > 0 : on montre une paire ratee
    private int pairsFound;

    private double timeLeft;
    private long lastNano;

    public MemoryGameController(SceneManager sceneManager, int chapter) {
        super(sceneManager, chapter);
    }

    @Override
    protected Node createContent() {
        deck = MemoryDeck.forChapter(chapter);
        canvas = new Canvas(GAME_W, GAME_H);
        g = canvas.getGraphicsContext2D();
        canvas.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
        layoutBoard();
        resetState();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frame(now);
            }
        };
        timer.start();
        return canvas;
    }

    @Override
    protected void stopLoop() {
        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    protected void restartGame() {
        deck = MemoryDeck.forChapter(chapter);
        layoutBoard();
        resetState();
    }

    private void layoutBoard() {
        double gridW = deck.getCols() * CARD + (deck.getCols() - 1) * GAP;
        double gridH = deck.getRows() * CARD + (deck.getRows() - 1) * GAP;
        originX = (GAME_W - gridW) / 2;
        originY = (GAME_H - gridH) / 2;
    }

    private void resetState() {
        matched = new boolean[deck.getCardCount()];
        flip = new double[deck.getCardCount()];
        first = -1;
        second = -1;
        mismatchTimer = 0;
        pairsFound = 0;
        timeLeft = timeLimit();
        lastNano = 0;
        updateGoal();
        updateTimer(timeLimit(), timeLimit());
        if (g != null) {
            render();
        }
    }

    private int timeLimit() {
        return 45 + deck.getPairCount() * 7;
    }

    // =====================================================================
    //  Boucle de jeu
    // =====================================================================
    private void frame(long now) {
        if (!isActive()) {
            lastNano = 0;
            updateFlips();
            render();
            return;
        }
        if (lastNano == 0) {
            lastNano = now;
            render();
            return;
        }
        double dt = Math.min(0.05, (now - lastNano) / 1_000_000_000.0);
        lastNano = now;

        timeLeft -= dt;
        if (timeLeft <= 0) {
            timeLeft = 0;
            updateTimer(0, timeLimit());
            render();
            lose("Le temps presse : les lueurs se sont eteintes.");
            return;
        }
        if (mismatchTimer > 0) {
            mismatchTimer -= dt;
            if (mismatchTimer <= 0) {
                mismatchTimer = 0;
                first = -1;
                second = -1;
            }
        }
        updateFlips();
        updateTimer((int) Math.ceil(timeLeft), timeLimit());
        render();
    }

    private void updateFlips() {
        for (int i = 0; i < flip.length; i++) {
            double target = (matched[i] || i == first || i == second) ? 1 : 0;
            flip[i] += (target - flip[i]) * 0.20;
        }
    }

    // =====================================================================
    //  Interaction
    // =====================================================================
    private void handleClick(double mx, double my) {
        if (!isActive() || mismatchTimer > 0) {
            return;
        }
        int card = cardAt(mx, my);
        if (card < 0 || matched[card] || card == first) {
            return;
        }
        if (first < 0) {
            first = card;
            return;
        }
        second = card;
        if (deck.symbolAt(first) == deck.symbolAt(second)) {
            matched[first] = true;
            matched[second] = true;
            first = -1;
            second = -1;
            pairsFound++;
            updateGoal();
            if (pairsFound >= deck.getPairCount()) {
                win();
            }
        } else {
            mismatchTimer = 0.85;   // les deux cartes restent visibles un instant
        }
    }

    private int cardAt(double mx, double my) {
        for (int i = 0; i < deck.getCardCount(); i++) {
            double x = cardX(i);
            double y = cardY(i);
            if (mx >= x && mx <= x + CARD && my >= y && my <= y + CARD) {
                return i;
            }
        }
        return -1;
    }

    private void updateGoal() {
        setGoal("Paires " + pairsFound + " / " + deck.getPairCount());
    }

    private double cardX(int index) {
        return originX + (index % deck.getCols()) * (CARD + GAP);
    }

    private double cardY(int index) {
        return originY + (index / deck.getCols()) * (CARD + GAP);
    }

    // =====================================================================
    //  Rendu
    // =====================================================================
    private void render() {
        g.setFill(Color.web("#ECE5D7"));
        g.fillRect(0, 0, GAME_W, GAME_H);

        double gridW = deck.getCols() * CARD + (deck.getCols() - 1) * GAP;
        double gridH = deck.getRows() * CARD + (deck.getRows() - 1) * GAP;
        g.setFill(Color.web("#E5DCC9"));
        g.fillRoundRect(originX - 24, originY - 24, gridW + 48, gridH + 48, 22, 22);

        for (int i = 0; i < deck.getCardCount(); i++) {
            drawCard(i);
        }
    }

    private void drawCard(int index) {
        double x = cardX(index);
        double y = cardY(index);
        double t = flip[index];
        double squish = Math.max(0.03, Math.abs(t - 0.5) * 2);

        // ombre douce
        g.setFill(Color.web("#3F3B42", 0.10));
        g.fillRoundRect(x + 3, y + 5, CARD, CARD, 16, 16);

        g.save();
        g.translate(x + CARD / 2, y + CARD / 2);
        g.scale(squish, 1);
        g.translate(-CARD / 2, -CARD / 2);
        if (t < 0.5) {
            drawBack();
        } else {
            drawFront(deck.symbolAt(index), matched[index]);
        }
        g.restore();
    }

    private void drawBack() {
        g.setFill(Color.web("#D9C3A6"));
        g.fillRoundRect(0, 0, CARD, CARD, 16, 16);
        g.setFill(Color.web("#CBB291"));
        g.fillRoundRect(8, 8, CARD - 16, CARD - 16, 12, 12);
        // empreinte de patte discrete
        drawPaw(CARD / 2, CARD / 2 + 4, 34, Color.web("#D9C3A6"));
    }

    private void drawFront(int symbol, boolean done) {
        g.setFill(Color.web(done ? "#DDE9DC" : "#FBF7EF"));
        g.fillRoundRect(0, 0, CARD, CARD, 16, 16);
        g.setStroke(Color.web(done ? Theme.SUCCESS : "#E6DDCB"));
        g.setLineWidth(2);
        g.strokeRoundRect(1, 1, CARD - 2, CARD - 2, 15, 15);
        drawSymbol(symbol, CARD / 2, CARD / 2, 50, Color.web(done ? "#DDE9DC" : "#FBF7EF"));
    }

    /** Huit symboles distincts (voir MemoryDeck.SYMBOL_COUNT). */
    private void drawSymbol(int id, double cx, double cy, double size, Color bg) {
        switch (id) {
            case 0 -> {                                  // poisson
                g.setFill(Theme.FISH_DARK);
                g.fillPolygon(new double[]{cx - size * 0.10, cx - size * 0.34, cx - size * 0.34},
                        new double[]{cy, cy - size * 0.22, cy + size * 0.22}, 3);
                g.setFill(Theme.FISH_BODY);
                g.fillOval(cx - size * 0.24, cy - size * 0.20, size * 0.56, size * 0.40);
                g.setFill(Color.web("#2B2B2B"));
                g.fillOval(cx + size * 0.10, cy - size * 0.08, size * 0.08, size * 0.08);
            }
            case 1 -> {                                  // etoile
                double[] xs = new double[10];
                double[] ys = new double[10];
                for (int k = 0; k < 10; k++) {
                    double ang = -Math.PI / 2 + k * Math.PI / 5;
                    double rad = (k % 2 == 0) ? size * 0.5 : size * 0.21;
                    xs[k] = cx + Math.cos(ang) * rad;
                    ys[k] = cy + Math.sin(ang) * rad;
                }
                g.setFill(Theme.BONUS_STAR);
                g.fillPolygon(xs, ys, 10);
            }
            case 2 -> {                                  // flocon
                g.setStroke(Color.web(Theme.SECONDARY));
                g.setLineWidth(size * 0.085);
                g.setLineCap(StrokeLineCap.ROUND);
                for (int k = 0; k < 3; k++) {
                    double ang = k * Math.PI / 3;
                    double dx = Math.cos(ang) * size * 0.42;
                    double dy = Math.sin(ang) * size * 0.42;
                    g.strokeLine(cx - dx, cy - dy, cx + dx, cy + dy);
                }
            }
            case 3 -> drawPaw(cx, cy, size, bg);         // patte
            case 4 -> {                                  // feuille
                g.setFill(Color.web(Theme.SUCCESS));
                g.beginPath();
                g.moveTo(cx, cy - size * 0.42);
                g.quadraticCurveTo(cx + size * 0.32, cy, cx, cy + size * 0.42);
                g.quadraticCurveTo(cx - size * 0.32, cy, cx, cy - size * 0.42);
                g.closePath();
                g.fill();
            }
            case 5 -> {                                  // cloche
                g.setFill(Color.web(Theme.GOLD));
                g.beginPath();
                g.moveTo(cx - size * 0.30, cy + size * 0.22);
                g.quadraticCurveTo(cx - size * 0.30, cy - size * 0.34, cx, cy - size * 0.34);
                g.quadraticCurveTo(cx + size * 0.30, cy - size * 0.34, cx + size * 0.30,
                        cy + size * 0.22);
                g.closePath();
                g.fill();
                g.fillRoundRect(cx - size * 0.36, cy + size * 0.18, size * 0.72,
                        size * 0.12, 4, 4);
                g.fillOval(cx - size * 0.08, cy + size * 0.28, size * 0.16, size * 0.16);
            }
            case 6 -> {                                  // lune
                g.setFill(Color.web("#8A86B8"));
                g.fillOval(cx - size * 0.36, cy - size * 0.36, size * 0.72, size * 0.72);
                g.setFill(bg);
                g.fillOval(cx - size * 0.12, cy - size * 0.46, size * 0.62, size * 0.62);
            }
            default -> {                                 // coeur
                g.setFill(Color.web(Theme.DANGER));
                g.fillOval(cx - size * 0.30, cy - size * 0.30, size * 0.34, size * 0.34);
                g.fillOval(cx - size * 0.04, cy - size * 0.30, size * 0.34, size * 0.34);
                g.fillPolygon(new double[]{cx - size * 0.32, cx + size * 0.32, cx},
                        new double[]{cy - size * 0.04, cy - size * 0.04, cy + size * 0.36}, 3);
            }
        }
    }

    private void drawPaw(double cx, double cy, double size, Color bg) {
        g.setFill(Color.web(Theme.ACCENT));
        g.fillOval(cx - size * 0.24, cy - size * 0.04, size * 0.48, size * 0.42);
        double[] toes = {-0.30, -0.10, 0.12, 0.30};
        for (int k = 0; k < toes.length; k++) {
            double ty = (k == 0 || k == 3) ? cy - size * 0.18 : cy - size * 0.34;
            g.fillOval(cx + toes[k] * size - size * 0.09, ty, size * 0.18, size * 0.20);
        }
    }
}
