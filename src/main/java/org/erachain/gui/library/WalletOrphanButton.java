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

public class WalletOrphanButton extends JButton implements Observer {

    private WalletOrphanButton th;

    public WalletOrphanButton() {

        super(Lang.T("Roll back blocks"));
        th = this;
        this.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                // TODO Auto-generated method stub
                // CHECK IF WALLET UNLOCKED
                if (!Controller.getInstance().isWalletUnlocked()) {
                    // ASK FOR PASSWORD
                    String password = PasswordPane.showUnlockWalletDialog(th);
                    if (!Controller.getInstance().unlockWallet(password)) {
                        // WRONG PASSWORD
                        JOptionPane.showMessageDialog(null, Lang.T("Invalid password"),
                                Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // GENERATE NEW ACCOUNT

                // newAccount_Button.setEnabled(false);
                // creane new thread
                new Thread("WalletOrphanButton") {
                    @Override
                    public void run() {

                        String message = Lang.T("Insert Quantity") + ":";
                        String retVal = JOptionPane.showInputDialog(null, message, "10");
                        if (retVal != null) {

                            Integer retValint = Integer.valueOf(retVal);
                            int hh = DCSet.getInstance().getBlockMap().size() - retValint;
                            if (hh > 1) {
                                Controller.getInstance().getWallet().synchronizeBodyUsed = false;
                                Controller.getInstance().setOrphanTo(hh);
                            }
                        }

                    }
                }.start();
            }
        });
        Controller.getInstance().addObserver(this);
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        ObserverMessage message = (ObserverMessage) arg1;
        int type = message.getType();
        if (type == ObserverMessage.WALLET_SYNC_STATUS) {
            int currentHeight = (int) message.getValue();
            if (currentHeight == 0 || currentHeight == DCSet.getInstance().getBlockMap().size()) {
                th.setEnabled(true);
                return;
            }
            th.setEnabled(false);
        } else if (type == ObserverMessage.FORGING_STATUS) {
            BlockGenerator.ForgingStatus status = (BlockGenerator.ForgingStatus) message.getValue();

            if (status == BlockGenerator.ForgingStatus.FORGING_ENABLED
                    && Controller.getInstance().isStatusOK()
                // || status == BlockGenerator.ForgingStatus.FORGING_WAIT
                    )
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
