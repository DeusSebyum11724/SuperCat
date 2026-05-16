package com.supercat.engine;

/**
 * Interface d'observation du moteur de jeu. Le GameController l'implemente
 * pour reagir aux evenements importants : rafraichir le HUD, afficher les
 * ecrans de fin de niveau ou de Game Over.
 *
 * Ce mecanisme decouple le moteur (logique) de l'interface (affichage).
 */
public interface GameListener {

    /** Appele a chaque frame : permet de rafraichir le HUD (score, temps). */
    void onTick();

    /** Appele lorsque le niveau est reussi (sortie atteinte). */
    void onLevelComplete();

    /** Appele lors d'un Game Over (collision avec un chien ou temps ecoule). */
    void onGameOver();
}
