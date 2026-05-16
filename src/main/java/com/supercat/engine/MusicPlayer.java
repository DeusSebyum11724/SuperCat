package com.supercat.engine;

import com.supercat.service.Settings;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Lecteur de la musique d'ambiance du jeu.
 *
 * La musique est entierement synthetisee par le programme (melodie de style
 * "chiptune" en ondes triangulaires) : aucun fichier audio externe, aucune
 * question de droits d'auteur. Le volume et l'activation sont lus depuis les
 * preferences de l'application (Settings).
 */
public class MusicPlayer {

    private static final float SAMPLE_RATE = 44100f;
    private static final double STEP_DURATION = 0.15;

    private static final int[] MELODY = {
            72, 76, 79, 76,  74, 77, 74, 72,
            71, 74, 79, 77,  76, 72, 74, 72,
            72, 79, 76, 72,  77, 74, 71, 74,
            76, 79, 84, 79,  77, 76, 74, 72
    };

    private static final int[] BASS = {48, 43, 45, 41, 48, 43, 41, 43};

    private Clip clip;
    private boolean muted = false;

    /** Demarre la musique en boucle, selon les preferences. */
    public void start() {
        if (clip != null && clip.isRunning()) {
            return;
        }
        muted = !Settings.isMusicEnabled();
        try {
            byte[] pcm = synthesizeLoop();
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            clip = AudioSystem.getClip();
            clip.open(format, pcm, 0, pcm.length);
            applyVolume();
            if (!muted) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Musique indisponible : " + e.getMessage());
            clip = null;
        }
    }

    /** Arrete la musique et libere les ressources audio. */
    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    /** Active ou desactive le son. */
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (clip == null) {
            return;
        }
        if (muted) {
            clip.stop();
        } else {
            applyVolume();
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public boolean isMuted() {
        return muted;
    }

    /** Applique le volume defini dans les preferences au canal audio. */
    private void applyVolume() {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
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
            // controle de volume indisponible : on garde le volume par defaut
        }
    }

    // =====================================================================
    //  Synthese de la melodie
    // =====================================================================
    private byte[] synthesizeLoop() {
        int stepSamples = (int) (STEP_DURATION * SAMPLE_RATE);
        int total = MELODY.length * stepSamples;
        int bassStepSamples = total / BASS.length;
        short[] samples = new short[total];

        for (int i = 0; i < total; i++) {
            double time = i / (double) SAMPLE_RATE;
            double value = 0;

            int melodyNote = MELODY[i / stepSamples];
            if (melodyNote > 0) {
                double env = envelope(i % stepSamples, stepSamples);
                value += triangle(time * midiToFreq(melodyNote)) * 0.22 * env;
            }

            int bassIndex = Math.min(i / bassStepSamples, BASS.length - 1);
            int bassNote = BASS[bassIndex];
            if (bassNote > 0) {
                double env = envelope(i % bassStepSamples, bassStepSamples);
                value += triangle(time * midiToFreq(bassNote)) * 0.16 * env;
            }

            value = Math.max(-1.0, Math.min(1.0, value));
            samples[i] = (short) (value * 32767);
        }

        byte[] pcm = new byte[total * 2];
        for (int i = 0; i < total; i++) {
            pcm[2 * i] = (byte) (samples[i] & 0xFF);
            pcm[2 * i + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }
        return pcm;
    }

    private static double midiToFreq(int midiNote) {
        return 440.0 * Math.pow(2.0, (midiNote - 69) / 12.0);
    }

    private static double triangle(double phase) {
        double frac = phase - Math.floor(phase);
        return 4.0 * Math.abs(frac - 0.5) - 1.0;
    }

    private static double envelope(int position, int length) {
        int attack = (int) (0.008 * SAMPLE_RATE);
        int release = (int) (0.040 * SAMPLE_RATE);
        if (position < attack) {
            return position / (double) attack;
        }
        if (position > length - release) {
            return Math.max(0.0, (length - position) / (double) release);
        }
        return 1.0;
    }
}
