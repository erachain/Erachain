package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.utils.ObserverMessage;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * только запускает событи - "обновиться"
 * Используется в gui.library.SetIntervalPanel#syncUpdate(java.util.Observable, java.lang.Object) и других
 */
public class GuiTimer extends Observable {

    public GuiTimer() {
        Timer timer = new Timer("GuiTimer");

        TimerTask action = new TimerTask() {
            public void run() {
                try {
                    onRepaint();
                } catch (Exception e) {
                }
            }
        };

        timer.schedule(action, 1000, 2000);

    }

    public void onRepaint() {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_REPAINT, this));
    }

    // for REPAINT timer
    public void addTimerObserver(Observer o) {
        this.addObserver(o);
    }
    public void removeTimerObserver(Observer o) {
        this.deleteObserver(o);
    }

}
