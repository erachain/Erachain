package org.erachain.gui;

import org.erachain.controller.Controller;
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
        muteSound = (Boolean) Settings.getInstance().getJSONObject().getOrDefault("muteSound", false);
        muteTray = (Boolean) Settings.getInstance().getJSONObject().getOrDefault("muteTray", false);

    }

    public void playEvent(Object playEvent) {
        this.playEvent = playEvent;
    }

    public void update(Observable o, Object arg) {

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
            TrayIcon.MessageType type = TrayIcon.MessageType.INFO;

            if (event instanceof Transaction) {

                Transaction transaction = (Transaction) event;

                if (transaction instanceof RSend) {
                    RSend rSend = (RSend) transaction;
                    if (rSend.hasAmount()) {
                        sound = "receivepayment.wav";
                        head = lang.translate("Payment received");
                        message = rSend.getCreator().getPersonAsString() + "\nTo: " + rSend.getRecipient().getPersonAsString() + "\n"
                                + "Asset Key" + ": " + rSend.getAbsKey() + ", " + lang.translate("Amount") + ": "
                                + rSend.getAmount().toPlainString()
                                + (rSend.getHead() != null ? "\n Title" + ":" + rSend.getHead() : "");
                    } else {
                        sound = "receivemail.wav";
                        head = lang.translate("Mail received");
                        message = rSend.getCreator().getPersonAsString() + "\nTo: " + rSend.getRecipient().getPersonAsString() + "\n"
                                + (rSend.getHead() != null ? "\n Title" + ":" + rSend.getHead() : "");

                    }
                } else {
                    sound = "newtransaction.wav";
                    head = lang.translate("New transaction");
                    message = transaction.getTitle();
                }
            } else if (event instanceof Block) {
                Block block = (Block) event;

                sound = "blockforge.wav";
                head = lang.translate("Block %d is forged").replace("%d", "" + block.heightBlock);
                message = lang.translate("Forged Fee") + ": " + block.viewFeeAsBigDecimal();

            } else {
                head = lang.translate("EVENT");
                message = event.toString();
            }

            playSound.playSound(sound);

            sysTray.sendMessage(head, message, type);

        }
    }

}
