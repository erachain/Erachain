package org.erachain.gui.status;

import org.erachain.controller.Controller;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionMap;
import org.erachain.gui.items.records.Records_UnConfirmed_Panel;
import org.erachain.gui2.Main_Panel;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
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

    public UnconfirmTransactionStatus() {
        super("| " + Lang.getInstance().translate("Unconfirmed Records") + ": 0 0/usec");

        map = DCSet.getInstance().getTransactionMap();
        map.addObserver(this);
        DCSet.getInstance().getBlockMap().addObserver(this);
        counter = map.size();
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

                // Main_Panel.getInstance().ccase1(
                // Lang.getInstance().translate("My Records"),
                // Records_My_SplitPanel.getInstance());
                Main_Panel.getInstance().insertTab(Lang.getInstance().translate("Unconfirmed Records"),
                        Records_UnConfirmed_Panel.getInstance());
            }

        });
    }

    private static long lastUpdate;
    @Override
    public void update(Observable arg0, Object arg1) {

        // TODO Auto-generated method stub
        // if (Controller.getInstance().needUpToDate())
        // return;

        ObserverMessage message = (ObserverMessage) arg1;

        switch (message.getType()) {
            case ObserverMessage.ADD_UNC_TRANSACTION_TYPE:
                counter++;
                if (NTP.getTime() - lastUpdate > 2000) {
                    refresh();
                }
                return;
            case ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE:
                counter--;
                if (NTP.getTime() - lastUpdate > 2000) {
                    refresh();
                }
                return;
            case ObserverMessage.CHAIN_ADD_BLOCK_TYPE:
                if (NTP.getTime() - lastUpdate > 2000) {
                    counter = map.size();
                    refresh();
                }
                return;
            case ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE:
                if (NTP.getTime() - lastUpdate > 2000) {
                    counter = map.size();
                    refresh();
                }
                return;

        }
    }

    private void refresh() {

        lastUpdate = NTP.getTime();

        String mess;
        if (counter > 0) {
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));
            mess = "<HTML>| <A href = ' '>" + Lang.getInstance().translate("Unconfirmed Records") + ": " + counter
                    + "</a>";
        } else {

            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            mess = "| " + Lang.getInstance().translate("Unconfirmed Records") + ": 0";
        }

        long timing = Controller.getInstance().getUnconfigmedMessageTimingAverage();
        if (timing > 0) {
            mess += " " + 1000000 / timing + "utx/s";
        }

        timing = Controller.getInstance().getBlockChain().transactionWinnedTimingAverage;
        if (timing > 0) {
            mess += " " + 1000000 / timing + "wtx/s";
        }

        timing = Controller.getInstance().getTransactionMakeTimingAverage();
        if (timing > 0) {
            mess += " " + 1000000 / timing + "mtx/s";
        }
        timing = Controller.getInstance().getBlockChain().transactionValidateTimingAverage;
        if (timing > 0) {
            mess += " " + 1000000 / timing + "vtx/s";
        }

        timing = Controller.getInstance().getBlockChain().transactionProcessTimingAverage;
        if (timing > 0) {
            mess += " " + 1000000 / timing + "ctx/s";
        }

        setText(mess + " |");

    }

}
