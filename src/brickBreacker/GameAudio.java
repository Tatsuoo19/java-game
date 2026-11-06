package brickBreacker;

import javax.sound.sampled.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Generates original music and effects; audio failure leaves gameplay usable.
 */
public class GameAudio implements AutoCloseable {
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audio");
        t.setDaemon(true);
        return t;
    });
    private final Map<String, Clip> effects = new HashMap<>();
    private Clip music;
    private Clip fallback;
    private final Map<Level, Clip> tracks = new EnumMap<>(Level.class);
    private Level current;
    private boolean enabled = true;

    public GameAudio() {
        worker.execute(() -> {
            try {
                music = clip(new double[] { 262, 330, 392, 330, 294, 349, 440, 349, 330, 392, 494, 392, 294,
                        349, 392, 294 }, .24, .10);
                fallback = music;
                effects.put("hit", clip(new double[] { 550, 350 }, .04, .18));
                effects.put("bounce", clip(new double[] { 330 }, .05, .14));
                effects.put("bonus", clip(new double[] { 523, 659, 784 }, .09, .16));
                effects.put("lose", clip(new double[] { 330, 262, 196 }, .15, .16));
                effects.put("win", clip(new double[] { 392, 523, 659, 784 }, .18, .16));
            } catch (LineUnavailableException | IllegalArgumentException | SecurityException failure) {
                System.err.println("Audio unavailable; continuing silently: " + failure.getMessage());
                release();
            }
        });
    }

    private Clip clip(double[] notes, double duration, double volume) throws LineUnavailableException {
        int rate = 22050, per = (int) (rate * duration);
        byte[] data = new byte[per * notes.length * 2];
        for (int n = 0; n < notes.length; n++)
            for (int i = 0; i < per; i++) {
                double envelope = Math.min(1, i / (rate * .01)) * Math.min(1, (per - i) / (rate * .04));
                short v = (short) (Math.sin(2 * Math.PI * notes[n] * i / rate) * 32767 * volume * envelope);
                int at = (n * per + i) * 2;
                data[at] = (byte) v;
                data[at + 1] = (byte) (v >> 8);
            }
        Clip c = AudioSystem.getClip();
        try {
            c.open(new AudioFormat(rate, 16, 1, true, false), data, 0, data.length);
        } catch (RuntimeException | LineUnavailableException e) {
            c.close();
            throw e;
        }
        return c;
    }

    private Clip track(Level level) {
        if (tracks.containsKey(level))
            return tracks.get(level);
        Clip loaded = null;
        String path = "assets/music/"
                + (level == Level.BOSS ? "boss" : level.name().toLowerCase(java.util.Locale.ROOT)) + ".wav";
        try {
            java.io.InputStream resource = GameAudio.class.getResourceAsStream("/" + path);
            try (AudioInputStream stream = resource == null
                    ? AudioSystem.getAudioInputStream(new java.io.File(path))
                    : AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(resource))) {
                loaded = AudioSystem.getClip();
                loaded.open(stream);
            }
        } catch (java.io.IOException | UnsupportedAudioFileException | LineUnavailableException
                | IllegalArgumentException | SecurityException e) {
            if (loaded != null)
                loaded.close();
            loaded = fallback;
            System.err.println("Stage music unavailable (" + path + "); using fallback: " + e.getMessage());
        }
        tracks.put(level, loaded);
        return loaded;
    }

    public void sync(boolean enabled, boolean playing) {
        sync(enabled, playing, Level.WINTER);
    }

    public void sync(boolean enabled, boolean playing, Level level) {
        worker.execute(() -> {
            this.enabled = enabled;
            if (current != level) {
                if (music != null)
                    music.stop();
                music = track(level);
                current = level;
                if (music != null)
                    music.setFramePosition(0);
            }
            if (music != null) {
                if (enabled && playing && !music.isRunning())
                    music.loop(Clip.LOOP_CONTINUOUSLY);
                else if (!enabled || !playing)
                    music.stop();
            }
            if (!enabled || !playing)
                effects.values().forEach(Clip::stop);
        });
    }

    public void play(String name) {
        worker.execute(() -> {
            Clip c = effects.get(name);
            if (enabled && c != null) {
                c.stop();
                c.setFramePosition(0);
                c.start();
            }
        });
    }

    private void release() {
        for (Clip c : new java.util.HashSet<>(tracks.values()))
            if (c != null)
                c.close();
        tracks.clear();
        if (fallback != null)
            fallback.close();
        if (music != null) {
            music.close();
            music = null;
        }
        effects.values().forEach(Clip::close);
        effects.clear();
    }

    public void close() {
        worker.execute(this::release);
        worker.shutdown();
    }
}
