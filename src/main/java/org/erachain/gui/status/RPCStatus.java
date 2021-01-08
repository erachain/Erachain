package org.erachain.gui.status;

import org.erachain.controller.Controller;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class RPCStatus extends JLabel implements Observer {

    String path = Settings.getInstance().getUserPath() + "images" + File.separator + "icons" + File.separator + "attentionIcon20_20.png";

    public RPCStatus(){

        Controller.getInstance().getRPCService().addObserver(this);

         }

    @Override
    public void update(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;
        boolean data = (boolean) ((ObserverMessage) arg).getValue();
        int type = message.getType();

        if (type == ObserverMessage.RPC_WORK_TYPE ) {
            if (data) {
                this.setIcon(new ImageIcon(path));
                this.setText(Lang.T("RPC Start"));
            } else{
                this.setIcon(null);
                this.setText("");

            }
        }
    }
}
