package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.gui.library.CloseDialog;

public class ClosingDialog  {
    CloseDialog aa;
   public ClosingDialog() {
       aa = new CloseDialog(MainFrame.getInstance());
       aa.setVisible(true);

        new Thread() {
            @Override
            public void run() {
                Controller.getInstance().deleteObservers();
                Controller.getInstance().addSingleObserver(aa);
                Controller.getInstance().stopAll(0);
                aa.setVisible(false);
            }
        }.start();
    }
}
