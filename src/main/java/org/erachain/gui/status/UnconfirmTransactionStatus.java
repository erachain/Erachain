package org.erachain.gui.status;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionMap;
import org.erachain.gui.items.records.UnconfirmedTransactionsPanel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

public class UnconfirmTransactionStatus extends JLabel implements Observer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnconfirmTransactionStatus.class);

    private TransactionMap map;
    private int counter;
    private boolean needUpdate;


    public UnconfirmTransactionStatus() {
        super("| " + Lang.getInstance().translate("Unconfirmed Records") + ": 0 0/usec");

        map = DCSet.getInstance().getTransactionMap();
        counter = map.size();

        map.addObserver(this);

        DCSet.getInstance().getBlockMap().addObserver(this);
        Controller.getInstance().guiTimer.addObserver(this);

        refresh();

        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub
                if (counter == 0)
                    return;

                // MainPanel.getInstance().ccase1(
                // Lang.getInstance().translate("My Records"),
                // MyTransactionsSplitPanel.getInstance());

                MainPanel.getInstance().insertTab(Lang.getInstance().translate("Unconfirmed Records"),
                        UnconfirmedTransactionsPanel.getInstance());
            }

        });
    }

    @Override
    public void update(Observable arg0, Object arg1) {

        ObserverMessage message = (ObserverMessage) arg1;

        switch (message.getType()) {
            case ObserverMessage.ADD_UNC_TRANSACTION_TYPE:
                counter++;
                needUpdate = true;
                return;
            case ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE:
                counter--;
                needUpdate = true;
                return;
            case ObserverMessage.CHAIN_ADD_BLOCK_TYPE:
                needUpdate = true;
                return;
            case ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE:
                needUpdate = true;
                return;
            case ObserverMessage.CHAIN_RESET_BLOCK_TYPE:
                counter = 0;
                needUpdate = false;
                refresh();
                return;
            case ObserverMessage.GUI_REPAINT:
                if (needUpdate) {
                    needUpdate = false;
                    refresh();
                }
                return;
        }
    }

    private void refresh() {

        String mess;
        if (counter > 0) {
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));
            mess = "<HTML>| <A href = ' '>" + Lang.getInstance().translate("Unconfirmed Records") + ": " + counter
                    + "</a>";
        } else {

            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            mess = "| " + Lang.getInstance().translate("Unconfirmed Records") + ": 0";
        }

        if (BlockChain.DEVELOP_USE) {
            // MISSED TELEGRAMS
            long missedMessagesTmp = Controller.getInstance().network.missedTelegrams.get();
            if (missedMessagesTmp > 0)
                mess += " " + missedMessagesTmp + "-tg";

            // MISSED TRANSACTIONS
            missedMessagesTmp = Controller.getInstance().network.missedTransactions.get();
            if (missedMessagesTmp > 0)
                mess += " " + missedMessagesTmp + "-tx";

            // MISSED WIN BLOCKS
            missedMessagesTmp = Controller.getInstance().network.missedWinBlocks.get();
            if (missedMessagesTmp > 0)
                mess += " " + missedMessagesTmp + "-wb";

            // MISSED MESSAGES
            missedMessagesTmp = Controller.getInstance().network.missedMessages.get();
            if (missedMessagesTmp > 0)
                mess += " " + missedMessagesTmp + "-me";

            // MISSED SENDS
            missedMessagesTmp = Controller.getInstance().network.missedSendes.get();
            if (missedMessagesTmp > 0)
                mess += " " + missedMessagesTmp + "-sd";
        }

        long timing = Controller.getInstance().network.telegramer.messageTimingAverage;
        if (timing > 0) {
            mess += " " + 1000000000l / timing + "tlg/s";
        }

        timing = Controller.getInstance().getUnconfigmedMessageTimingAverage();
        if (timing > 0) {
            mess += " " + 1000000l / timing + "utx/s";
        }

        timing = Controller.getInstance().getBlockChain().transactionWinnedTimingAverage;
        if (timing > 0) {
            mess += " " + 1000000l / timing + "wtx/s";
        }

        timing = Controller.getInstance().getTransactionMakeTimingAverage();
        if (timing > 0) {
            mess += " " + 1000000l / timing + "mtx/s";
        }
        timing = Controller.getInstance().getBlockChain().transactionValidateTimingAverage;
        if (timing > 0) {
            mess += " " + 1000000l / timing + "vtx/s";
        }

        timing = Controller.getInstance().getBlockChain().transactionProcessTimingAverage;
        if (timing > 0) {
            mess += " " + 1000000l / timing + "ctx/s";
        }

        setText(mess + " |");

    }

}
