package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.utils.ObserverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;

public class GuiObserver extends Observable {

    public void repaintGUI() {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.GUI_REPAINT, null));
    }

}
