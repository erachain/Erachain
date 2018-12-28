package org.erachain.gui.status;

import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.io.File;

public class RPCStatus extends JLabel {

    public RPCStatus(){
        if (Settings.getInstance().isRpcEnabled()) {
            String path = Settings.getInstance().getUserPath() + "images" + File.separator + "icons" + File.separator + "attentionIcon20_20.png";
            this.setIcon(new ImageIcon(path));
            this.setText(Lang.getInstance().translate("RPC Start"));
        }
    }
}
