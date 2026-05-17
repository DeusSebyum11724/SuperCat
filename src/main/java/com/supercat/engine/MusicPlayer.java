package com.supercat.engine;

import com.supercat.service.Settings;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Lecteur de la musique d'ambiance du jeu.
 *
 * Deux pistes, entierement synthetisees (aucun fichier audio externe) :
 *
 *  - une berceuse douce pour l'interface : melodie sinusoidale lente posee
 *    sur un tapis d'accords qui "respire" et une basse chaleureuse ;
 *  - une melodie chiptune plus entrainante pendant les parties.
 *
 * Un seul lecteur existe pour toute l'application. La piste change en
 * entrant ou en quittant une partie ; a l'interieur de l'interface (ou a
 * l'interieur d'une partie) la musique se poursuit sans interruption.
 */
public final class MusicPlayer {

    private static final MusicPlayer INSTANCE = new MusicPlayer();

    private static final float SAMPLE_RATE = 44100f;
    private static final double TWO_PI = 2 * Math.PI;

    private enum Track { UI, GAME }

    // --- piste de l'interface : berceuse douce ---
    private static final double COZY_STEP = 0.46;
    private static final int[] COZY_MELODY = {
            79, 0, 76, 0, 72, 0, 74, 76,    // accord de Do
            72, 0, 69, 0, 71, 0, 72, 69,    // accord de La mineur
            77, 0, 72, 0, 69, 0, 72, 74,    // accord de Fa
            74, 0, 71, 0, 67, 0, 69, 67     // accord de Sol
    };
    private static final int[][] COZY_CHORDS = {
            {60, 64, 67}, {57, 60, 64}, {53, 57, 60}, {55, 59, 62}
    };
    private static final int[] COZY_BASS = {48, 45, 41, 43};

    // --- piste en jeu : melodie chiptune entrainante ---
    private static final double GAME_STEP = 0.15;
    private static final int[] GAME_MELODY = {
            72, 76, 79, 76,  74, 77, 74, 72,
            71, 74, 79, 77,  76, 72, 74, 72,
            72, 79, 76, 72,  77, 74, 71, 74,
            76, 79, 84, 79,  77, 76, 74, 72
    };
    private static final int[] GAME_BASS = {48, 43, 45, 41, 48, 43, 41, 43};

    private byte[] uiPcm;
    private byte[] gamePcm;
    private volatile boolean synthesized;
    private volatile boolean synthesizing;

    private volatile Clip clip;
    private Track currentTrack;
    private Track wantedTrack = Track.UI;
    private boolean muted;

    private MusicPlayer() {
        // instance unique
    }

    /** Le lecteur de musique partage par toute l'application. */
    public static MusicPlayer instance() {
        return INSTANCE;
    }

    /** Demande la berceuse de l'interface. */
    public void playUi() {
        request(Track.UI);
    }

    /** Demande la melodie de jeu (chiptune). */
    public void playGame() {
        request(Track.GAME);
    }

    private synchronized void request(Track track) {
        wantedTrack = track;
        if (synthesized) {
            applyWanted();
        } else {
            beginSynthesis();
        }
    }

    /** Lance la synthese des deux pistes sur un fil d'arriere-plan. */
    private void beginSynthesis() {
        if (synthesizing) {
            return;
        }
        synthesizing = true;
        Thread thread = new Thread(() -> {
            byte[] ui = synthesizeCozy();
            byte[] game = synthesizeGame();
            synchronized (this) {
                uiPcm = ui;
                gamePcm = game;
                synthesized = true;
                applyWanted();
            }
        }, "supercat-music");
        thread.setDaemon(true);
        thread.start();
    }

    /** Charge et joue la piste demandee. A appeler en detenant le verrou. */
    private void applyWanted() {
        if (!synthesized || (currentTrack == wantedTrack && clip != null)) {
            return;
        }
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        try {
            byte[] pcm = (wantedTrack == Track.UI) ? uiPcm : gamePcm;
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            Clip loaded = AudioSystem.getClip();
            loaded.open(format, pcm, 0, pcm.length);
            clip = loaded;
            currentTrack = wantedTrack;
            muted = !Settings.isMusicEnabled();
            applyVolume();
            if (!muted) {
                loaded.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Musique indisponible : " + e.getMessage());
            clip = null;
        }
    }

    /** Arrete la musique et libere les ressources audio. */
    public synchronized void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        currentTrack = null;
    }

    /** Active ou desactive le son. */
    public synchronized void setMuted(boolean muted) {
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

    /** Reapplique le volume des preferences a la musique en cours. */
    public synchronized void refreshVolume() {
        applyVolume();
    }

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
    //  Synthese de la berceuse (interface)
    // =====================================================================
    private byte[] synthesizeCozy() {
        int stepSamples = (int) (COZY_STEP * SAMPLE_RATE);
        int total = COZY_MELODY.length * stepSamples;
        int regionSamples = total / COZY_CHORDS.length;
        short[] samples = new short[total];

        for (int i = 0; i < total; i++) {
            double time = i / (double) SAMPLE_RATE;
            int step = i / stepSamples;
            int region = Math.min(COZY_CHORDS.length - 1, i / regionSamples);
            double swell = regionSwell(i % regionSamples, regionSamples);
            double value = 0;

            int note = COZY_MELODY[step];
            if (note > 0) {
                double env = softEnvelope(i % stepSamples, stepSamples);
                double f = midiToFreq(note);
                value += (Math.sin(TWO_PI * f * time) * 0.82
                        + Math.sin(TWO_PI * 2 * f * time) * 0.18) * 0.15 * env;
            }
            for (int padNote : COZY_CHORDS[region]) {
                double f = midiToFreq(padNote + 12);
                value += Math.sin(TWO_PI * f * time) * 0.03 * swell;
            }
            double bassFreq = midiToFreq(COZY_BASS[region]);
            value += (Math.sin(TWO_PI * bassFreq * time) * 0.8
                    + Math.sin(TWO_PI * 2 * bassFreq * time) * 0.2) * 0.11 * swell;

            value = Math.max(-1.0, Math.min(1.0, value));
            samples[i] = (short) (value * 32767);
        }
        return toBytes(samples);
    }

    // =====================================================================
    //  Synthese de la melodie chiptune (en jeu)
    // =====================================================================
    private byte[] synthesizeGame() {
        int stepSamples = (int) (GAME_STEP * SAMPLE_RATE);
        int total = GAME_MELODY.length * stepSamples;
        int bassStepSamples = total / GAME_BASS.length;
        short[] samples = new short[total];

        for (int i = 0; i < total; i++) {
            double time = i / (double) SAMPLE_RATE;
            double value = 0;

            int melodyNote = GAME_MELODY[i / stepSamples];
            if (melodyNote > 0) {
                double env = chipEnvelope(i % stepSamples, stepSamples);
                value += triangle(time * midiToFreq(melodyNote)) * 0.22 * env;
            }

            int bassIndex = Math.min(i / bassStepSamples, GAME_BASS.length - 1);
            int bassNote = GAME_BASS[bassIndex];
            if (bassNote > 0) {
                double env = chipEnvelope(i % bassStepSamples, bassStepSamples);
                value += triangle(time * midiToFreq(bassNote)) * 0.16 * env;
            }

            value = Math.max(-1.0, Math.min(1.0, value));
            samples[i] = (short) (value * 32767);
        }
        return toBytes(samples);
    }

    // =====================================================================
    //  Outils de synthese
    // =====================================================================
    private static byte[] toBytes(short[] samples) {
        byte[] pcm = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
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

    /** Enveloppe breve et nette de la melodie chiptune. */
    private static double chipEnvelope(int position, int length) {
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

    /** Enveloppe ronde et douce de la berceuse. */
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
