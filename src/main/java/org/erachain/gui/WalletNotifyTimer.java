package org.erachain.gui;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.erachain.utils.PlaySound;
import org.erachain.utils.SysTray;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletNotifyTimer<U> implements Observer {

    public Object playEvent;

    protected Logger logger;
    Controller contr = Controller.getInstance();
    Settings settings = Settings.getInstance();
    SysTray sysTray = SysTray.getInstance();
    PlaySound playSound = PlaySound.getInstance();
    Lang lang = Lang.getInstance();
    private List<Integer> transactionsAlreadyPlayed;

    public WalletNotifyTimer() {
        transactionsAlreadyPlayed = new ArrayList<>();

        logger = LoggerFactory.getLogger(this.getClass());
        Controller.getInstance().guiTimer.addObserver(this); // обработка repaintGUI

    }

    public void playEvent(Object playEvent) {
        this.playEvent = playEvent;
    }

    public void update(Observable o, Object arg) {

        if (!contr.doesWalletKeysExists() || contr.getWallet().synchronizeBodyUsed
                || !settings.isTrayEventEnabled())
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

            String sound = null;
            String head = null;
            String message = null;
            TrayIcon.MessageType type = TrayIcon.MessageType.NONE;

            if (event instanceof Transaction) {

                Transaction transaction = (Transaction) event;
                Account creator = transaction.getCreator();

                settings.isSoundReceiveMessageEnabled();


                if (transaction instanceof RSend) {
                    RSend rSend = (RSend) transaction;
                    if (rSend.hasAmount()) {
                        // TRANSFER
                        String amount = (rSend.hasPacket() ? "package" : rSend.getAmount().toPlainString() + " [" + rSend.getAbsKey() + "]") + "\n";
                        if (contr.getWallet().accountExists(creator)) {
                            if (settings.isSoundNewTransactionEnabled())
                                sound = "send.wav";

                            head = Lang.T("Payment send");
                            message = rSend.getCreator().getPersonAsString() + " -> \n "
                                    + amount
                                    + rSend.getRecipient().getPersonAsString() + "\n"
                                    + (rSend.getTitle() != null ? "\n" + rSend.getTitle() : "");
                        } else {

                            if (settings.isSoundReceivePaymentEnabled())
                                sound = "receivepayment.wav";

                            head = Lang.T("Payment received");
                            message = rSend.getRecipient().getPersonAsString() + " <- \n "
                                    + amount
                                    + rSend.getCreator().getPersonAsString() + "\n"
                                    + (rSend.getTitle() != null ? "\n" + rSend.getTitle() : "");
                        }
                    } else {
                        // MAIL
                        if (contr.getWallet().accountExists(rSend.getCreator())) {
                            if (settings.isSoundNewTransactionEnabled())
                                sound = "send.wav";

                            head = Lang.T("Mail send");
                            message = rSend.getCreator().getPersonAsString() + " -> \n "
                                    //+ rSend.getAmount().toPlainString() + "[" + rSend.getAbsKey() + "]\n "
                                    + rSend.getRecipient().getPersonAsString() + "\n"
                                    + (rSend.getTitle() != null ? "\n" + rSend.getTitle() : "");
                        } else {
                            if (settings.isSoundReceiveMessageEnabled())
                                sound = "receivemail.wav";

                            head = Lang.T("Mail received");
                            message = rSend.getRecipient().getPersonAsString() + " <- \n "
                                    //+ rSend.getAmount().toPlainString() + "[" + rSend.getAbsKey() + "]\n "
                                    + rSend.getCreator().getPersonAsString() + "\n"
                                    + (rSend.getTitle() != null ? "\n" + rSend.getTitle() : "");
                        }

                    }

                } else {

                    if (contr.getWallet().accountExists(transaction.getCreator())) {
                        if (settings.isSoundNewTransactionEnabled())
                            sound = "outcometransaction.wav";

                        head = Lang.T("Outcome transaction") + ": " + Lang.T(transaction.viewFullTypeName());
                        message = transaction.getTitle();
                    } else {
                        if (settings.isSoundNewTransactionEnabled())
                            sound = "incometransaction.wav";
                        head = Lang.T("Income transaction") + ": " + Lang.T(transaction.viewFullTypeName());
                        message = transaction.getTitle();
                    }
                }
            } else if (event instanceof Block) {

                Block.BlockHead blockHead = ((Block) event).blockHead;
                if (blockHead.heightBlock == 1) {
                    return;
                }
                Fun.Tuple3<Integer, Integer, Integer> forgingPoint = blockHead.creator.getForgingData(DCSet.getInstance(), blockHead.heightBlock);
                if (forgingPoint == null)
                    return;

                if (settings.isSoundForgedBlockEnabled()) {
                    sound = "blockforge.wav";
                }

                head = Lang.T("Forging Block %d").replace("%d", "" + blockHead.heightBlock);
                message = Lang.T("Forging Fee") + ": " + blockHead.viewFeeAsBigDecimal();

                int diff = blockHead.heightBlock - forgingPoint.a;
                if (diff < 300) {
                    head = null;
                    sound = null;
                } else if (diff < 1000) {
                    sound = null;
                }
            } else if (event instanceof Pair) {
                Object value = ((Pair<?, ?>) event).getB();
                if (value instanceof Order) {
                    Order order = (Order) value;
                    head = Lang.T("Order") + " - " + Lang.T(order.viewStatus());
                    message = order.toString();
                    int status = order.getStatus();
                    if (status == Order.FULFILLED || status == Order.COMPLETED) {
                        sound = "receivepayment.wav";
                    } else {
                        sound = "receivemail.wav";
                    }
                } else {
                    head = Lang.T("EVENT");
                    sound = "receivemail.wav";
                    message = value.toString();
                }
            } else {
                head = Lang.T("EVENT");
                sound = "receivemail.wav";
                message = event.toString();
            }

            if (sound != null)
                playSound.playSound(sound);

            if (head != null) {
                sysTray.sendMessage(head, message, type);
            }

        }
    }

}
