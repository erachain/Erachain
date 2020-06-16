package org.erachain.gui.telegrams;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.wallet.TelegramsMap;
import org.erachain.gui.models.TimerTableModelCls;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Observer;

@SuppressWarnings("serial")
public class WalletTelegramsFilterTableModel extends TimerTableModelCls<Transaction> implements Observer {

    private boolean needUpdate = false;
    private long timeUpdate = 0;
    private String sender;
    private String reciever;

    static Logger LOGGER = LoggerFactory.getLogger(WalletTelegramsFilterTableModel.class);

    public WalletTelegramsFilterTableModel() {
        super(Controller.getInstance().getWallet().database.getTelegramsMap(), new String[]{"Message"},
                new Boolean[]{true, true, true, true, true, true, true, false, false}, false);

        addObservers();

    }

    @Override
    public Object getValueAt(int row, int column) {

        if (this.list == null || this.list.size() == 0) {
            return null;
        }

        RSend transaction = (RSend) list.get(row);
        if (transaction == null)
            return null;

        switch (column) {
            case 0:

                return list.get(row);

        }

        return null;

    }

    private void filter() {
        list.clear();
        for (Transaction transaction : ((TelegramsMap) map).values()) {
            HashSet<Account> recipients = transaction.getRecipientAccounts();
            if (reciever != null) {

                if (transaction.getCreator().getAddress().equals(sender)) {
                    for (Account pecipient : recipients) {
                        if (pecipient.getAddress().equals(reciever)) {
                            //list.add(new Tuple3(sender,reciever,transaction));
                            continue;
                        }

                    }

                }
                if (transaction.getCreator().getAddress().equals(reciever)) {
                    for (Account pecipient : recipients) {
                        if (pecipient.getAddress().equals(sender)) {
                            Tuple3 tt = new Tuple3(reciever, sender, transaction);
                            if (!list.contains(tt))
                                //list.add(tt);
                                continue;
                        }

                    }

               }

                
            } else {
                // add all recipients

                if (transaction.getCreator().getAddress().equals(sender)) {

                    for (Account recipient : recipients) {
                        //ttt.add(new Tuple3(sender,recipient.getAddress(),transaction));
                    }
                } else {
                    // add recipient = sender
                    for (Account pecipient : recipients) {
                        if (pecipient.getAddress().equals(sender)) {
                            //ttt.add(new Tuple3(transaction.getCreator().getAddress(), sender, transaction));
                            continue;
                        }

                    }
                }
            }
        }

    }

    /**
     * @param sender
     *            the sender to set
     */
    public void setSender(String sender) {
        this.sender = sender;
        filter();
        this.fireTableDataChanged();
    }

    /**
     * @param reciever
     *            the reciever to set
     */
    public void setReciever(String reciever) {
        this.reciever = reciever;
        filter();
        this.fireTableDataChanged();
    }

    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return the reciever
     */
    public String getReciever() {
        return reciever;
    }

}
