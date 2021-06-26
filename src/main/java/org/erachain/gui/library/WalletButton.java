package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.BlockGenerator;
import org.erachain.datachain.DCSet;
import org.erachain.gui.PasswordPane;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

;

public abstract class WalletButton extends JButton implements Observer {

    private WalletButton th;

    public WalletButton(String label) {

        super(Lang.T(label));
        th = this;

        Controller.getInstance().addObserver(this);

        this.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // check synchronize Walet
                if (Controller.getInstance().getWallet().synchronizeBodyUsed) {
                    return;
                }

                //CHECK IF WALLET UNLOCKED
                if (!Controller.getInstance().isWalletUnlocked()) {
                    //ASK FOR PASSWORD
                    String password = PasswordPane.showUnlockWalletDialog(th);
                    if (!Controller.getInstance().unlockWallet(password)) {
                        //WRONG PASSWORD
                        JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                action();
            }
        });

    }

    abstract void action();

    @Override
    public void update(Observable arg0, Object arg1) {
        // TODO Auto-generated method stub
        ObserverMessage message = (ObserverMessage) arg1;
        int type = message.getType();
        if (type == ObserverMessage.WALLET_SYNC_STATUS) {
            int currentHeight = (int) message.getValue();
            if (currentHeight == 0 || currentHeight == DCSet.getInstance().getBlockMap().size()) {
                th.setEnabled(true);
                return;
            }
            th.setEnabled(false);
        } else if (message.getType() == ObserverMessage.FORGING_STATUS) {
            BlockGenerator.ForgingStatus status = (BlockGenerator.ForgingStatus) message.getValue();


            if (status == BlockGenerator.ForgingStatus.FORGING_WAIT || status == BlockGenerator.ForgingStatus.FORGING_ENABLED)
                th.setEnabled(false);
            else {
                th.setEnabled(true);
            }
        } else if (type == ObserverMessage.NETWORK_STATUS) {
            int status = (int) message.getValue();


            if (status == Controller.STATUS_SYNCHRONIZING) {

                th.setEnabled(false);
            } else {
                th.setEnabled(true);
            }
        }
    }
}
