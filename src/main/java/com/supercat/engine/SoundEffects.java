package com.supercat.engine;

import com.supercat.service.Settings;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Effets sonores du jeu, entierement synthetises (aucun fichier externe).
 *
 * Pour l'instant un seul effet : le petit "miaou" joue lorsque le chat
 * attrape un poisson d'or. Le miaou est obtenu en faisant glisser la
 * hauteur d'un son riche en harmoniques (montee puis descente), avec un
 * leger creux au milieu qui evoque les deux syllabes "mi-aou".
 */
public final class SoundEffects {

    private static final float SAMPLE_RATE = 44100f;

    private static volatile Clip meowClip;
    private static boolean built;

    private SoundEffects() {
        // classe utilitaire
    }

    /** Prepare les effets sonores a l'avance (appele au demarrage). */
    public static void preload() {
        Thread thread = new Thread(SoundEffects::build, "supercat-sfx");
        thread.setDaemon(true);
        thread.start();
    }

    /** Joue le miaou du chat (sans effet si le son est coupe). */
    public static void meow() {
        if (!Settings.isMusicEnabled()) {
            return;
        }
        build();
        Clip clip = meowClip;
        if (clip == null) {
            return;
        }
        try {
            clip.stop();
            clip.setFramePosition(0);
            applyVolume(clip);
            clip.start();
        } catch (RuntimeException ignored) {
            // lecture impossible : on ignore silencieusement
        }
    }

    private static synchronized void build() {
        if (built) {
            return;
        }
        built = true;
        try {
            byte[] pcm = synthesizeMeow();
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            Clip clip = AudioSystem.getClip();
            clip.open(format, pcm, 0, pcm.length);
            meowClip = clip;
        } catch (Exception e) {
            System.err.println("Effets sonores indisponibles : " + e.getMessage());
            meowClip = null;
        }
    }

    private static void applyVolume(Clip clip) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            double volume = Settings.getMusicVolume();
            float decibels = (volume <= 0.0001)
                    ? gain.getMinimum()
                    : (float) (20.0 * Math.log10(volume));
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels)));
        } catch (RuntimeException ignored) {
            // controle de volume indisponible
        }
    }

    // =====================================================================
    //  Synthese du miaou
    // =====================================================================
    private static byte[] synthesizeMeow() {
        int length = (int) (0.5 * SAMPLE_RATE);
        short[] samples = new short[length];
        double phase = 0;

        for (int i = 0; i < length; i++) {
            double t = i / (double) length;            // progression 0..1
            double seconds = i / SAMPLE_RATE;

            // contour de hauteur : montee rapide puis longue descente
            double contour = (t < 0.22)
                    ? 1.0 + (t / 0.22) * 0.55
                    : 1.55 - ((t - 0.22) / 0.78) * 0.80;
            double f0 = 505 * contour * (1 + 0.022 * Math.sin(TWO_PI * 6.2 * seconds));
            phase += TWO_PI * f0 / SAMPLE_RATE;

            // timbre riche en harmoniques : un son "voix d'animal"
            double tone = 0;
            for (int h = 1; h <= 6; h++) {
                tone += Math.sin(h * phase) / h;
            }
            tone /= 2.3;

            double value = Math.max(-1.0, Math.min(1.0, tone * meowEnvelope(t) * 0.6));
            samples[i] = (short) (value * 32767);
        }

        byte[] pcm = new byte[length * 2];
        for (int i = 0; i < length; i++) {
            pcm[2 * i] = (byte) (samples[i] & 0xFF);
            pcm[2 * i + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }
        return pcm;
    }

    private static final double TWO_PI = 2 * Math.PI;

    /** Enveloppe d'amplitude du miaou, avec un creux pour les deux syllabes. */
    private static double meowEnvelope(double t) {
        double attack = (t < 0.05) ? t / 0.05 : 1.0;
        double release = (t > 0.62) ? Math.max(0.0, (1.0 - t) / 0.38) : 1.0;
        double dip = 1.0 - 0.32 * Math.exp(-Math.pow((t - 0.30) / 0.055, 2));
        return attack * release * dip;
    }
}
