package org.erachain.gui.library;

import org.erachain.controller.Controller;

public class WalletSyncButton extends WalletButton {

    public WalletSyncButton() {
        super("Update", "Update accounts and synchronize wallet");
    }

    void action() {
        new Thread() {
            @Override
            public void run() {
                Controller.getInstance().getWallet().updateAccountsFromSecretKeys();
                Controller.getInstance().getWallet().synchronizeFull();
            }
        }.start();
    }
}
