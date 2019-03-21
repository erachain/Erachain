package org.erachain.utils;
// 03/03

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class PlaySound {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaySound.class);
    private static PlaySound instance;
    private ArrayList<byte[]> transactionsAlreadyPlayed;

    public PlaySound() {
        transactionsAlreadyPlayed = new ArrayList<byte[]>();
    }

    public static PlaySound getInstance() {
        if (instance == null) {
            instance = new PlaySound();
        }

        return instance;
    }

    private long timePoint;
    public void playSound(final String url, byte[] signature) {

        if (System.currentTimeMillis() - timePoint > 10000) {
            return;
        }

        timePoint = System.currentTimeMillis();
        if (transactionsAlreadyPlayed.size() > 100) {
            transactionsAlreadyPlayed.clear();
        }

        boolean is = false;

        for (byte[] transactionSign : transactionsAlreadyPlayed) {
            if (Arrays.equals(transactionSign, signature)) {
                is = true;
                break;
            }
        }

        if (!is) {
            transactionsAlreadyPlayed.add(0, signature);

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

}
