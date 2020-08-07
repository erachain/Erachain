package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.PlaySound;
import org.erachain.utils.SysTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletTimer<U> implements Observer {

    public Object playEvent;

    protected Logger logger;
    Controller contr = Controller.getInstance();
    SysTray sysTray = SysTray.getInstance();
    PlaySound playSound = PlaySound.getInstance();
    Lang lang = Lang.getInstance();
    private List<Integer> transactionsAlreadyPlayed;

    boolean muteSound;
    boolean muteTray;

    public WalletTimer() {
        transactionsAlreadyPlayed = new ArrayList<>();

        logger = LoggerFactory.getLogger(this.getClass());
        Controller.getInstance().guiTimer.addObserver(this); // обработка repaintGUI
        muteSound = (Boolean) Settings.getInstance().getJSONObject().getOrDefault("muteTraySound", false);
        muteTray = (Boolean) Settings.getInstance().getJSONObject().getOrDefault("muteTrayMessage", false);

    }

    public void playEvent(Object playEvent) {
        this.playEvent = playEvent;
    }

    public void update(Observable o, Object arg) {

        if (!contr.doesWalletExists() || contr.wallet.synchronizeBodyUsed)
            return;

        ObserverMessage messageObs = (ObserverMessage) arg;

        if (messageObs.getType() == ObserverMessage.GUI_REPAINT && playEvent != null) {
            Object event = playEvent;
            playEvent = null;

            if (transactionsAlreadyPlayed.contains(event.hashCode()))
                return;

            transactionsAlreadyPlayed.add(event.hashCode());
            while (transactionsAlreadyPlayed.size() > 100) {
                transactionsAlreadyPlayed.remove(0);
            }

            String sound = "newtransaction.wav";
            String head = "";
            String message = "";
            TrayIcon.MessageType type = TrayIcon.MessageType.NONE;

            if (event instanceof Transaction) {

                Transaction transaction = (Transaction) event;
                Account creator = transaction.getCreator();

                if (transaction instanceof RSend) {
                    RSend rSend = (RSend) transaction;
                    if (rSend.hasAmount()) {
                        // TRANSFER
                        if (creator != null && contr.wallet.accountExists(creator.getAddress())) {
                            sound = "send.wav";
                            head = lang.translate("Payment send");
                            message = rSend.getCreator().getPersonAsString() + " -> \n "
                                    + rSend.getAmount().toPlainString() + " [" + rSend.getAbsKey() + "]\n "
                                    + rSend.getRecipient().getPersonAsString() + "\n"
                                    + (rSend.getHead() != null ? "\n" + rSend.getHead() : "");
                        } else {
                            sound = "receivepayment.wav";
                            head = lang.translate("Payment received");
                            message = rSend.getRecipient().getPersonAsString() + " <- \n "
                                    + rSend.getAmount().toPlainString() + " [" + rSend.getAbsKey() + "]\n "
                                    + rSend.getCreator().getPersonAsString() + "\n"
                                    + (rSend.getHead() != null ? "\n" + rSend.getHead() : "");
                        }
                    } else {
                        // MAIL
                        if (creator != null && contr.wallet.accountExists(rSend.getCreator().getAddress())) {
                            sound = "send.wav";
                            head = lang.translate("Mail send");
                            message = rSend.getCreator().getPersonAsString() + " -> \n "
                                    //+ rSend.getAmount().toPlainString() + "[" + rSend.getAbsKey() + "]\n "
                                    + rSend.getRecipient().getPersonAsString() + "\n"
                                    + (rSend.getHead() != null ? "\n" + rSend.getHead() : "");
                        } else {
                            sound = "receivemail.wav";
                            head = lang.translate("Mail received");
                            message = rSend.getRecipient().getPersonAsString() + " <- \n "
                                    //+ rSend.getAmount().toPlainString() + "[" + rSend.getAbsKey() + "]\n "
                                    + rSend.getCreator().getPersonAsString() + "\n"
                                    + (rSend.getHead() != null ? "\n" + rSend.getHead() : "");
                        }

                    }

                } else {
                    if (creator != null && contr.wallet.accountExists(transaction.getCreator().getAddress())) {
                        sound = "outcometransaction.wav";
                        head = lang.translate("Outcome transaction") + ": " + transaction.viewFullTypeName();
                        message = transaction.getTitle();
                    } else {
                        sound = "incometransaction.wav";
                        head = lang.translate("Income transaction") + ": " + transaction.viewFullTypeName();
                        message = transaction.getTitle();
                    }
                }
            } else if (event instanceof Block) {

                Block block = (Block) event;

                sound = "blockforge.wav";
                head = lang.translate("Forging Block %d").replace("%d", "" + block.heightBlock);
                message = lang.translate("Forging Fee") + ": " + block.viewFeeAsBigDecimal();

            } else {
                head = lang.translate("EVENT");
                message = event.toString();
            }

            playSound.playSound(sound);

            sysTray.sendMessage(head, message, type);

        }
    }

}
