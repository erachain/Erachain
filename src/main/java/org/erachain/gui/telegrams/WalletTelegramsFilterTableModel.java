package org.erachain.gui.telegrams;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
// in list of org.erachain.records in wallet
public class WalletTelegramsFilterTableModel extends DefaultTableModel implements Observer {

    private boolean needUpdate = false;
    private long timeUpdate = 0;
    private String sender;
    private String reciever;

    public static final int COLUMN_MESSAGE = 0;
  //  public static final int COLUMN_DATE = 0;
    static Logger LOGGER = LoggerFactory.getLogger(WalletTelegramsFilterTableModel.class);
    // ItemAssetMap dbItemAssetMap;
    private SortableList<String, Transaction> transactions;
    private String[] columnNames = Lang.getInstance().translate(new String[] { "Message" });
    private Boolean[] column_AutuHeight = new Boolean[] { true, true, true, true, true, true, true, false, false };
    ArrayList<Tuple3<String,String,Transaction>> ttt;

    public WalletTelegramsFilterTableModel() {
        ttt = new ArrayList<Tuple3<String,String,Transaction>>();
        addObservers();

        // dbItemAssetMap = DLSet.getInstance().getItemAssetMap();

    }

    public SortableList<String, Transaction> getSortableList() {
        return this.transactions;
    }

    public void setAsset(AssetCls asset) {

    }

    public Object getItem(int row) {
        return this.ttt.get(row);
    }

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

    // читаем колонки которые изменяем высоту
    public Boolean[] get_Column_AutoHeight() {

        return this.column_AutuHeight;
    }

    // устанавливаем колонки которым изменить высоту
    public void set_get_Column_AutoHeight(Boolean[] arg0) {
        this.column_AutuHeight = arg0;
    }

    public Transaction getTelegramMessage(int row) {
        return this.ttt.get(row).c;

    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return columnNames[index];
    }

    @Override
    public int getRowCount() {
        if (this.ttt == null) {
            return 0;
        }

        return this.ttt.size();
    }

    
    @Override
    public Object getValueAt(int row, int column) {
        // try
        // {
        if (this.ttt == null || this.ttt.size() == 0) {
            return null;
        }

        RSend transaction = (RSend) ttt.get(row).c;
        if (transaction == null)
            return null;

        switch (column) {
        case 0:

            return ttt.get(row);

       
        }

        return null;

        // } catch (Exception e) {
        // GUI ERROR
        // logger.error(e.getMessage(),e);
        // return null;
        // }

    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            // GUI ERROR
            String mess = e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        // CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_TELEGRAM_TYPE) {
            if (this.transactions == null) {
                this.transactions = (SortableList<String, Transaction>) message.getValue();
                //this.transactions.registerObserver();

            }
            filter();
            this.fireTableDataChanged();

        }
        if (message.getType() == ObserverMessage.WALLET_ADD_TELEGRAM_TYPE 
                || message.getType() == ObserverMessage.WALLET_RESET_TELEGRAM_TYPE){
            Object mm = message.getValue();
            filter();
            this.fireTableDataChanged();
        }
        
        if ( message.getType() == ObserverMessage.WALLET_REMOVE_TELEGRAM_TYPE) {
           Object mm = message.getValue();
            filter();
            this.fireTableDataChanged();
        }
        if ( message.getType() == ObserverMessage.WALLET_STATUS) {
            Object mm = message.getValue();
             filter();
             this.fireTableDataChanged();
         }
        
    }

    private void filter() {
        ttt.clear();
        for (Pair<String, Transaction> transaction : transactions) {
            HashSet<Account> recipients = transaction.getB().getRecipientAccounts();
            if (reciever != null) {
                
                if (transaction.getB().getCreator().getAddress().equals(sender)) {
                     for (Account pecipient : recipients) {
                        if (pecipient.getAddress().equals(reciever)) {
                            ttt.add(new Tuple3(sender,reciever,transaction.getB()));
                            continue;
                        }

                    }

                }
                if (transaction.getB().getCreator().getAddress().equals(reciever)) {
                    for (Account pecipient : recipients) {
                       if (pecipient.getAddress().equals(sender)) {
                           Tuple3 tt = new Tuple3(reciever,sender,transaction.getB());
                           if (!ttt.contains(tt)) 
                               ttt.add(tt);
                           continue;
                       }

                   }

               }
                
               
                
            } else {
                // add all recipients
              
                if (transaction.getB().getCreator().getAddress().equals(sender)) {
                   
                    for (Account recipient : recipients) {
                        ttt.add(new Tuple3(sender,recipient.getAddress(),transaction.getB()));
                    }
                }else
                {
                    // add recipient = sender
                    for (Account pecipient : recipients) {
                        if (pecipient.getAddress().equals(sender)) {
                            ttt.add(new Tuple3(transaction.getB().getCreator().getAddress(), sender, transaction.getB()));
                            continue;
                        }

                    }
                }
            }
        }

    }

    public void addObservers() {

        // REGISTER ON WALLET TRANSACTIONS
        Controller.getInstance().getWallet().database.getTelegramsMap().addObserver(this);
        // for UNCONFIRMEDs
        DCSet.getInstance().getTransactionTab().addObserver(this);
        // for ??
        /// Controller.getInstance().wallet.database.getPersonMap().addObserver(transactions);

    }

    public void removeObservers() {

        Controller.getInstance().getWallet().database.getTelegramsMap().deleteObserver(this);
        DCSet.getInstance().getTransactionTab().addObserver(this);
        /// ???
        /// Controller.getInstance().wallet.database.getPersonMap().deleteObserver(transactions);
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
