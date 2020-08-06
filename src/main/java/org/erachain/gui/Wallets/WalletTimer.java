package org.erachain.gui.Wallets;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.Transaction;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.PlaySound;
import org.erachain.utils.SysTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletTimer<U> implements Observer {

    public Object playEvent;

    protected Logger logger;

    public WalletTimer() {
        logger = LoggerFactory.getLogger(this.getClass());

        Controller.getInstance().guiTimer.addObserver(this); // обработка repaintGUI

    }

    public void playEvent(Object playEvent) {
        this.playEvent = playEvent;
    }

    public void update(Observable o, Object arg) {

        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.GUI_REPAINT && playEvent != null) {
            Object event = playEvent;
            playEvent = null;

            if (event instanceof Transaction) {
                byte[] signature = new byte[64];

                PlaySound.getInstance().playSound("receivepayment.wav", signature);

                SysTray.getInstance().sendMessage("Payment received",
                        "From: 123"
                        //+ r_Send.getCreator().getPersonAsString() + "\nTo: " + r_Send.getRecipient().getPersonAsString() + "\n"
                        //+ "Asset Key" + ": " + r_Send.getAbsKey() + ", " + "Amount" + ": "
                        //+ r_Send.getAmount().toPlainString()
                        //+ (r_Send.getHead() != null? "\n Title" + ":" + r_Send.getHead() : "")
                        ,
                        TrayIcon.MessageType.INFO);

            } else if (event instanceof Block) {
                Block block = (Block) event;


                PlaySound.getInstance().playSound("receivepayment.wav", block.getSignature());

                SysTray.getInstance().sendMessage("Block generated",
                        "Fee: " + block.viewFeeAsBigDecimal()
                        ,
                        TrayIcon.MessageType.INFO);


            } else {
                SysTray.getInstance().sendMessage("EVENT",
                        event.toString(),
                        TrayIcon.MessageType.INFO);
            }
        }
    }

}
