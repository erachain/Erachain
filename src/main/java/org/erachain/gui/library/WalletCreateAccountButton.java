package org.erachain.gui.library;

import org.erachain.controller.Controller;

;

public class WalletCreateAccountButton extends WalletButton {

    public WalletCreateAccountButton() {
        super("Create");
    }

    void action() {
        new Thread() {
            @Override
            public void run() {
                Controller.getInstance().getWallet().generateNewAccount();
            }
        }.start();
    }

}
