package com.supercat.engine;

import com.supercat.service.Settings;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Lecteur de la musique d'ambiance du jeu.
 *
 * La musique est une berceuse douce, entierement synthetisee par le
 * programme : une melodie lente en ondes sinusoidales, posee sur un tapis
 * d'accords qui "respire" et une basse chaleureuse. Aucun fichier audio
 * externe, donc aucune question de droits d'auteur.
 *
 * Un seul lecteur existe pour toute l'application (instance unique) : la
 * musique demarre au lancement et se poursuit sans interruption d'un ecran
 * a l'autre, ce qui rend l'ambiance plus douce et continue.
 */
public final class MusicPlayer {

    private static final MusicPlayer INSTANCE = new MusicPlayer();

    private static final float SAMPLE_RATE = 44100f;
    private static final double TWO_PI = 2 * Math.PI;

    /** Duree d'un pas de melodie : tempo lent et calme. */
    private static final double STEP_DURATION = 0.46;

    /** Melodie (notes MIDI, 0 = silence) -- douce, beaucoup d'espace. */
    private static final int[] MELODY = {
            79, 0, 76, 0, 72, 0, 74, 76,    // accord de Do
            72, 0, 69, 0, 71, 0, 72, 69,    // accord de La mineur
            77, 0, 72, 0, 69, 0, 72, 74,    // accord de Fa
            74, 0, 71, 0, 67, 0, 69, 67     // accord de Sol
    };

    /** Triades des quatre accords, pour le tapis sonore (un accord par region). */
    private static final int[][] CHORDS = {
            {60, 64, 67},   // Do majeur
            {57, 60, 64},   // La mineur
            {53, 57, 60},   // Fa majeur
            {55, 59, 62}     // Sol majeur
    };

    /** Note de basse de chaque accord. */
    private static final int[] BASS = {48, 45, 41, 43};

    private volatile Clip clip;
    private volatile boolean building;
    private boolean muted = false;

    private MusicPlayer() {
        // instance unique
    }

    /** Le lecteur de musique partage par toute l'application. */
    public static MusicPlayer instance() {
        return INSTANCE;
    }

    /**
     * Demarre la musique en boucle (sans effet si elle tourne deja). La
     * synthese a lieu sur un fil d'arriere-plan pour ne pas retarder le
     * demarrage de l'application.
     */
    public void start() {
        if (clip != null || building) {
            return;
        }
        building = true;
        Thread thread = new Thread(this::buildAndPlay, "supercat-music");
        thread.setDaemon(true);
        thread.start();
    }

    private void buildAndPlay() {
        try {
            byte[] pcm = synthesizeLoop();
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            Clip built = AudioSystem.getClip();
            built.open(format, pcm, 0, pcm.length);
            clip = built;
            muted = !Settings.isMusicEnabled();
            applyVolume();
            if (!muted) {
                built.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Musique indisponible : " + e.getMessage());
            clip = null;
        } finally {
            building = false;
        }
    }

    /** Arrete la musique et libere les ressources audio. */
    public void stop() {
        Clip current = clip;
        if (current != null) {
            current.stop();
            current.close();
            clip = null;
        }
    }

    /** Active ou desactive le son. */
    public void setMuted(boolean muted) {
        this.muted = muted;
        Clip current = clip;
        if (current == null) {
            return;
        }
        if (muted) {
            current.stop();
        } else {
            applyVolume();
            current.setFramePosition(0);
            current.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public boolean isMuted() {
        return muted;
    }

    /** Reapplique le volume des preferences a la musique en cours. */
    public void refreshVolume() {
        applyVolume();
    }

    /** Applique le volume defini dans les preferences au canal audio. */
    private void applyVolume() {
        Clip current = clip;
        if (current == null || !current.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        try {
            FloatControl gain = (FloatControl) current.getControl(FloatControl.Type.MASTER_GAIN);
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
    //  Synthese de la berceuse
    // =====================================================================
    private byte[] synthesizeLoop() {
        int stepSamples = (int) (STEP_DURATION * SAMPLE_RATE);
        int total = MELODY.length * stepSamples;
        int regionSamples = total / CHORDS.length;
        short[] samples = new short[total];

        for (int i = 0; i < total; i++) {
            double time = i / (double) SAMPLE_RATE;
            int step = i / stepSamples;
            int region = Math.min(CHORDS.length - 1, i / regionSamples);
            double swell = regionSwell(i % regionSamples, regionSamples);
            double value = 0;

            // melodie : sinus doux, avec une touche de 2e harmonique
            int note = MELODY[step];
            if (note > 0) {
                double env = softEnvelope(i % stepSamples, stepSamples);
                double f = midiToFreq(note);
                value += (Math.sin(TWO_PI * f * time) * 0.82
                        + Math.sin(TWO_PI * 2 * f * time) * 0.18) * 0.15 * env;
            }

            // tapis d'accords : triade qui respire doucement
            for (int padNote : CHORDS[region]) {
                double f = midiToFreq(padNote + 12);
                value += Math.sin(TWO_PI * f * time) * 0.03 * swell;
            }

            // basse chaleureuse
            double bassFreq = midiToFreq(BASS[region]);
            value += (Math.sin(TWO_PI * bassFreq * time) * 0.8
                    + Math.sin(TWO_PI * 2 * bassFreq * time) * 0.2) * 0.11 * swell;

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

    /** Enveloppe d'une note : attaque et extinction longues, donc rondes. */
    private static double softEnvelope(int position, int length) {
        int attack = (int) (0.06 * SAMPLE_RATE);
        int release = (int) (0.14 * SAMPLE_RATE);
        if (position < attack) {
            return position / (double) attack;
        }
        if (position > length - release) {
            return Math.max(0.0, (length - position) / (double) release);
        }
        return 1.0;
    }

    /** Respiration du tapis d'accords : leger souffle a chaque accord. */
    private static double regionSwell(int position, int length) {
        double p = position / (double) length;
        if (p < 0.25) {
            return p / 0.25;
        }
        if (p > 0.78) {
            return Math.max(0.0, (1.0 - p) / 0.22);
        }
        return 1.0;
    }
}
