package com.supercat.engine;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Lecteur de la musique d'ambiance du jeu.
 *
 * La musique n'est pas chargee depuis un fichier : elle est entierement
 * synthetisee par le programme (melodie de style "chiptune" en ondes
 * triangulaires). Cela evite toute dependance a un fichier audio externe
 * et toute question de droits d'auteur.
 */
public class MusicPlayer {

    private static final float SAMPLE_RATE = 44100f;
    private static final double STEP_DURATION = 0.15;   // duree d'une note (s)

    /** Melodie principale (numeros de notes MIDI ; 0 = silence). */
    private static final int[] MELODY = {
            72, 76, 79, 76,  74, 77, 74, 72,
            71, 74, 79, 77,  76, 72, 74, 72,
            72, 79, 76, 72,  77, 74, 71, 74,
            76, 79, 84, 79,  77, 76, 74, 72
    };

    /** Ligne de basse (une note pour 4 notes de la melodie). */
    private static final int[] BASS = {48, 43, 45, 41, 48, 43, 41, 43};

    private Clip clip;
    private boolean muted = false;

    /** Demarre la musique en boucle (sans effet si elle joue deja). */
    public void start() {
        if (clip != null && clip.isRunning()) {
            return;
        }
        try {
            byte[] pcm = synthesizeLoop();
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            clip = AudioSystem.getClip();
            clip.open(format, pcm, 0, pcm.length);
            if (!muted) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            // l'absence de musique ne doit jamais empecher de jouer
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
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public boolean isMuted() {
        return muted;
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

            // voix de la melodie
            int melodyNote = MELODY[i / stepSamples];
            if (melodyNote > 0) {
                double env = envelope(i % stepSamples, stepSamples);
                value += triangle(time * midiToFreq(melodyNote)) * 0.22 * env;
            }

            // voix de la basse
            int bassIndex = Math.min(i / bassStepSamples, BASS.length - 1);
            int bassNote = BASS[bassIndex];
            if (bassNote > 0) {
                double env = envelope(i % bassStepSamples, bassStepSamples);
                value += triangle(time * midiToFreq(bassNote)) * 0.16 * env;
            }

            value = Math.max(-1.0, Math.min(1.0, value));
            samples[i] = (short) (value * 32767);
        }

        // conversion en octets (16 bits, little-endian)
        byte[] pcm = new byte[total * 2];
        for (int i = 0; i < total; i++) {
            pcm[2 * i] = (byte) (samples[i] & 0xFF);
            pcm[2 * i + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }
        return pcm;
    }

    /** Convertit un numero de note MIDI en frequence (Hz). */
    private static double midiToFreq(int midiNote) {
        return 440.0 * Math.pow(2.0, (midiNote - 69) / 12.0);
    }

    /** Onde triangulaire (son plus doux qu'une onde carree) entre -1 et 1. */
    private static double triangle(double phase) {
        double frac = phase - Math.floor(phase);
        return 4.0 * Math.abs(frac - 0.5) - 1.0;
    }

    /** Enveloppe attaque/relachement pour eviter les craquements entre notes. */
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
