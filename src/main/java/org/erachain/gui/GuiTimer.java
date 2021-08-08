package org.erachain.gui;

import org.erachain.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observer;

/**
 * только запускает событие - "обновиться"
 * Используется в gui.Library.SetIntervalPanel#syncUpdate(java.util.Observable, java.lang.Object) и других
 */
public class GuiTimer extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuiTimer.class);

    private GuiObserver observer;

    public GuiTimer() {
        observer = new GuiObserver();
        this.setName("GuiTimer");
        this.start();
    }

    public void run() {

        Controller cnt = Controller.getInstance();

        while (!cnt.isOnStopping()) {
            try {
                observer.repaintGUI();
            } catch (java.lang.OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(2056);
                return;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            long timer = cnt.useGui? (cnt.isDynamicGUI()? 500 : 2500) : 60000;
            try {
                Thread.sleep(timer);
            } catch (InterruptedException e) {
                break;
            }
        }

    }

    public void addObserver(Observer o) {
        observer.addObserver(o);
    }

    public void deleteObserver(Observer o) {
        observer.deleteObserver(o);
    }

}
