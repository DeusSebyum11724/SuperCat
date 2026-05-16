package com.supercat.service;

import java.util.prefs.Preferences;

/**
 * Preferences de l'application, conservees entre deux lancements.
 *
 * Les valeurs sont stockees via l'API Preferences de Java (propre a
 * l'utilisateur du systeme) : affichage plein ecran, musique, volume,
 * animations reduites.
 */
public final class Settings {

    private static final Preferences PREFS = Preferences.userRoot().node("com/supercat/game");

    private Settings() {
        // classe utilitaire
    }

    public static boolean isFullscreen() {
        return PREFS.getBoolean("fullscreen", true);
    }

    public static void setFullscreen(boolean value) {
        PREFS.putBoolean("fullscreen", value);
    }

    public static boolean isMusicEnabled() {
        return PREFS.getBoolean("musicEnabled", true);
    }

    public static void setMusicEnabled(boolean value) {
        PREFS.putBoolean("musicEnabled", value);
    }

    /** Volume de la musique, entre 0.0 et 1.0. */
    public static double getMusicVolume() {
        return PREFS.getDouble("musicVolume", 0.65);
    }

    public static void setMusicVolume(double value) {
        PREFS.putDouble("musicVolume", Math.max(0.0, Math.min(1.0, value)));
    }

    public static boolean isReducedMotion() {
        return PREFS.getBoolean("reducedMotion", false);
    }

    public static void setReducedMotion(boolean value) {
        PREFS.putBoolean("reducedMotion", value);
    }
}
