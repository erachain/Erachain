package gui.status;

import datachain.DCSet;
import gui.items.records.Records_UnConfirmed_Panel;
import gui2.Main_Panel;
import lang.Lang;
import org.apache.log4j.Logger;
import utils.ObserverMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

public class UnconfirmTransactionStatus extends JLabel implements Observer {

    private static final Logger LOGGER = Logger.getLogger(UnconfirmTransactionStatus.class);

    private datachain.TransactionMap map;
    private int counter;

    public UnconfirmTransactionStatus() {
        super("| " + Lang.getInstance().translate("Unconfirmed Records") + ": 0 |");

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

    @Override
    public void update(Observable arg0, Object arg1) {

        // TODO Auto-generated method stub
        // if (Controller.getInstance().needUpToDate())
        // return;

        ObserverMessage message = (ObserverMessage) arg1;

        /// LOGGER.error("update - type:" + message.getType());

        if (message.getType() == ObserverMessage.ADD_UNC_TRANSACTION_TYPE) {
            counter++;
            refresh();
        } else if (message.getType() == ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE) {
            counter--;
            refresh();
        } else if (message.getType() == ObserverMessage.CHAIN_ADD_BLOCK_TYPE
                || message.getType() == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE) {
            counter = map.size();
            refresh();
            // } else if (message.getType() ==
            // ObserverMessage.COUNT_UNC_TRANSACTION_TYPE) {
            // counter = (int) message.getValue();
            // refresh();
        }
    }

    private void refresh() {
        if (counter > 0) {
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));
            setText("<HTML>| <A href = ' '>" + Lang.getInstance().translate("Unconfirmed Records") + ": " + counter
                    + "</a> |");
            return;
        }

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setText("| " + Lang.getInstance().translate("Unconfirmed Records") + ": 0 |");

    }

}
