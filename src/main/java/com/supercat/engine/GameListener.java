package com.supercat.engine;

/**
 * Interface d'observation du moteur de jeu. Le GameController l'implemente
 * pour reagir aux evenements importants : rafraichir l'affichage (HUD),
 * afficher les ecrans de fin de niveau, de Game Over ou de victoire.
 *
 * Ce mecanisme decouple le moteur (logique) de l'interface (affichage).
 */
public interface GameListener {

    /** Appele a chaque frame : permet de rafraichir le HUD (score, temps). */
    void onTick();

    /** Appele lorsqu'un niveau est termine et qu'un niveau suivant existe. */
    void onLevelComplete();

    /** Appele lors d'un Game Over (collision avec un chien ou temps ecoule). */
    void onGameOver();

    /** Appele lorsque tous les niveaux ont ete termines (victoire finale). */
    void onGameWon();
}
