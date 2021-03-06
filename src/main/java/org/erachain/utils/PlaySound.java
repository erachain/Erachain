package org.erachain.utils;
// 03/03

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class PlaySound {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaySound.class);
    private static PlaySound instance;


    public PlaySound() {
    }

    public static PlaySound getInstance() {
        if (instance == null) {
            instance = new PlaySound();
        }

        return instance;
    }

    private long timePoint;

    public void playSound(final String url) {

        if (System.currentTimeMillis() - timePoint < 5000 || url == null) {
            return;
        }

        timePoint = System.currentTimeMillis();

        new Thread(new Runnable() {
            public void run() {
                File soundFile = new File("sounds/" + url);
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);) {


                    Clip clip = AudioSystem.getClip();

                    clip.open(ais);

                    clip.setFramePosition(0);
                    clip.start();

                    Thread.sleep(clip.getMicrosecondLength() / 1000);
                    clip.stop();
                    clip.close();
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
                    LOGGER.error(exc.getMessage());
                } catch (InterruptedException exc) {
                    LOGGER.debug(exc.getMessage());
                } catch (Exception e) {
                }

            }
        }).start();

    }

}
