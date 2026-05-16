package com.supercat.model;

/**
 * Une ligne du classement mondial (leaderboard, cf. RM8) : le rang d'un
 * joueur, son pseudo, son meilleur score et la date de la performance.
 */
public record ScoreEntry(int rank, String username, int score, String date) {
}
