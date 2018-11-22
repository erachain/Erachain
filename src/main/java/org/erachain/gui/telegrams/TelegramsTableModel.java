package org.erachain.gui.telegrams;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.R_Send;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.SortableList;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.DefaultTableModel;
import javax.validation.constraints.Null;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
// in list of org.erachain.records in wallet
public class TelegramsTableModel extends DefaultTableModel implements Observer {

   

    public static final int COLUMN_DATE = 0;
    public static final int COLUMN_SENDER = 1;
    public static final int COLUMN_RECIEVER = 2;
    public static final int COLUMN_MESSAGE = 3;
    public static final int COLUMN_SIGNATURE = 4;
  //  public static final int COLUMN_DATE = 0;
    static Logger LOGGER = LoggerFactory.getLogger(TelegramsTableModel.class.getName());
    // ItemAssetMap dbItemAssetMap;
    private SortableList<String, Transaction> transactions;
    private String[] columnNames = Lang.getInstance().translate(new String[] {"Date", "Sender", "Recipient", "Message", "Signature" });
   
   

    public TelegramsTableModel() {
         addObservers();
    }

    public SortableList<String, Transaction> getSortableList() {
        return this.transactions;
    }

    public void setAsset(AssetCls asset) {

    }

    public Object getItem(int row) {
        return this.transactions.get(row);
    }

    public Class<? extends Object> getColumnClass(int c) { // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }

     public Transaction getTelegramMessage(int row) {
        return this.transactions.get(row).getB();

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
        if (this.transactions == null) {
            return 0;
        }

        return this.transactions.size();
    }

    
    @Override
    public Object getValueAt(int row, int column) {
        // try
        // {
        if (this.transactions == null || this.transactions.size() == 0) {
            return null;
        }

        R_Send transaction = (R_Send)this.transactions.get(row).getB();
        if (transaction == null)
            return null;

        switch (column) {
        case COLUMN_DATE:
            return transaction.viewTimestamp();
        case COLUMN_SENDER:
            return transaction.viewCreator();
        case COLUMN_RECIEVER:
            return transaction.viewRecipient();
        case COLUMN_MESSAGE:
            return transaction.viewData();
        case COLUMN_SIGNATURE:
            return transaction.viewSignature();
       
        }

        return null;

        // } catch (Exception e) {
        // GUI ERROR
        // LOGGER.error(e.getMessage(),e);
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
        if (message.getType() == ObserverMessage.ALL_TELEGRAMT_LIST_TYPE) {
            if (this.transactions == null) {
                this.transactions = (SortableList<String, Transaction>) message.getValue();
                this.transactions.registerObserver();

            }
           
            this.fireTableDataChanged();

        }
        if (message.getType() == ObserverMessage.ALL_TELEGRAMT_ADD_TYPE 
                || message.getType() == ObserverMessage.ALL_TELEGRAM_RESET_TYPE){
            Object mm = message.getValue();
            
            this.fireTableDataChanged();
        }
        
        if ( message.getType() == ObserverMessage.ALL_TELEGRAMT_REMOVE_TYPE) {
           Object mm = message.getValue();
            
            this.fireTableDataChanged();
        }
        if ( message.getType() == ObserverMessage.WALLET_STATUS) {
            Object mm = message.getValue();
            
             this.fireTableDataChanged();
         }
        
    }

   
    public void addObservers() {

        // REGISTER ON WALLET TRANSACTIONS
        Controller.getInstance().telegramStore.database.getTelegramsMap().addObserver(this);
        // for UNCONFIRMEDs
       // DCSet.getInstance().getTransactionMap().addObserver(this);
        // for ??
        /// Controller.getInstance().wallet.database.getPersonMap().addObserver(transactions);

    }

    public void removeObservers() {

        Controller.getInstance().telegramStore.database.getTelegramsMap().deleteObserver(this);
      // DCSet.getInstance().getTransactionMap().addObserver(this);
        /// ???
        /// Controller.getInstance().wallet.database.getPersonMap().deleteObserver(transactions);
    }

   

}
