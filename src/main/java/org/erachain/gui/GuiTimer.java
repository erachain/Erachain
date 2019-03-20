package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.database.DBMap;
import org.erachain.utils.ObserverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * только запускает событи - "обновиться"
 * Используется в gui.library.SetIntervalPanel#syncUpdate(java.util.Observable, java.lang.Object) и других
 */
public class GuiTimer extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuiTimer.class);

    public GuiTimer() {
    }

    public void run() {

        Controller cnt = Controller.getInstance();

        while (!cnt.isOnStopping()) {
            try {
                cnt.repaintGUI();
            } catch (java.lang.OutOfMemoryError e) {
                Controller.getInstance().stopAll(56);
                return;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            try {
                Thread.sleep(cnt.isDynamicGUI()? 500 : 2500);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

}
