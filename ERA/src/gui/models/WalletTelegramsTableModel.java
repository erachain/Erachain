package gui.models;

import controller.Controller;
import core.account.Account;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.transaction.*;
import database.wallet.TransactionMap;
import datachain.DCSet;
import datachain.SortableList;
import gui.Gui;
import lang.Lang;
import network.message.TelegramMessage;
import ntp.NTP;
import org.apache.log4j.Logger;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import settings.Settings;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import utils.Pair;
import utils.PlaySound;

import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
// in list of records in wallet
public class WalletTelegramsTableModel extends TableModelCls<String, Transaction> implements Observer {

    private boolean needUpdate = false;
    private long timeUpdate = 0;

    public static final int COLUMN_CONFIRMATIONS = 0;
    public static final int COLUMN_TIMESTAMP = 1;
    public static final int COLUMN_TYPE = 2;
    public static final int COLUMN_CREATOR = 3;
    public static final int COLUMN_ITEM = 4;
    public static final int COLUMN_AMOUNT = 5;
    public static final int COLUMN_RECIPIENT = 6;
    public static final int COLUMN_FEE = 7;
    public static final int COLUMN_SIZE = 8;
    static Logger LOGGER = Logger.getLogger(WalletTelegramsTableModel.class.getName());
    //ItemAssetMap dbItemAssetMap;
    private SortableList< String, Transaction> transactions;
    private String[] columnNames = Lang.getInstance().translate(new String[]{
            "Confirmations", "Timestamp", "Type", "Creator", "Item", "Amount", "Recipient", "Fee", "Size"});
    private Boolean[] column_AutuHeight = new Boolean[]{true, true, true, true, true, true, true, false, false};
    private int start =0;
    private int step =100;
   

    public WalletTelegramsTableModel() {
        addObservers();

        //dbItemAssetMap = DBSet.getInstance().getItemAssetMap();

    }

    @Override
    public SortableList< String, Transaction> getSortableList() {
        return this.transactions;
    }

    public void setAsset(AssetCls asset) {


    }


    public Object getItem(int row) {
        return this.transactions.get(row).getB();
    }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
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
        Pair<String, Transaction> data = this.transactions.get(row);
        if (data == null || data.getB() == null) {
            return null;
        }
        return data.getB();
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
        //try
        //{
        if (this.transactions == null ) {
            return null;
        }

       R_Send transaction = (R_Send) transactions.get(row).getB();
        if (transaction == null)
            return null;

        switch (column) {
            case COLUMN_CONFIRMATIONS:

                return transaction.getConfirmations(DCSet.getInstance());

            case COLUMN_TIMESTAMP:


                return DateTimeFormat.timestamptoString(transaction.getTimestamp());//.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;

            case COLUMN_TYPE:

                return Lang.getInstance().translate(transaction.viewFullTypeName());

            case COLUMN_CREATOR:

                return transaction.viewCreator();

            case COLUMN_ITEM:
                return transaction.getHead();

            case COLUMN_AMOUNT:

                BigDecimal amo = transaction.getAmount();
                if (amo == null)
                    return BigDecimal.ZERO;
                return amo;

            case COLUMN_RECIPIENT:

                try {
                    return transaction.viewRecipient();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                    return "";
                }

            case COLUMN_FEE:

                return transaction.getFee();

            case COLUMN_SIZE:
                return transaction.viewSize(Transaction.FOR_NETWORK);
        }

        return null;

        //} catch (Exception e) {
        //GUI ERROR
        //	LOGGER.error(e.getMessage(),e);
        //	return null;
        //}

    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
            String mess = e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;

        //CHECK IF NEW LIST
        if (message.getType() == ObserverMessage.WALLET_LIST_TELEGRAM_TYPE) {
            if (this.transactions == null) {
                this.transactions = (SortableList<String, Transaction>)message.getValue();
                this.transactions.registerObserver();
    //            this.transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
            }

            this.fireTableDataChanged();

        
        } 
        if (message.getType() ==ObserverMessage.WALLET_ADD_TELEGRAM_TYPE || 
                message.getType() ==ObserverMessage.WALLET_RESET_TELEGRAM_TYPE 
                || message.getType() ==ObserverMessage.WALLET_REMOVE_TELEGRAM_TYPE)
                                        this.fireTableDataChanged();

        
    }

    public void addObservers() {

        //REGISTER ON WALLET TRANSACTIONS
        Controller.getInstance().getWallet().database.getTelegramsMap().addObserver(this);
        // for UNCONFIRMEDs
        DCSet.getInstance().getTransactionMap().addObserver(this);
        // for ??
        ///Controller.getInstance().wallet.database.getPersonMap().addObserver(transactions);

    }


    public void removeObservers() {

        Controller.getInstance().getWallet().database.getTelegramsMap().deleteObserver(this);
        DCSet.getInstance().getTransactionMap().addObserver(this);
        /// ??? Controller.getInstance().wallet.database.getPersonMap().deleteObserver(transactions);
    }
    
  
}
