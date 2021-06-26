package org.erachain.gui.library;

import org.erachain.controller.Controller;

public class WalletImportButton extends WalletButton {

    public WalletImportButton() {
        super("Import", "Import private key");
    }

    void action() {
        new Thread() {
            @Override
            public void run() {
                Controller.getInstance().getWallet().synchronizeFull();
            }
        }.start();
    }
}
