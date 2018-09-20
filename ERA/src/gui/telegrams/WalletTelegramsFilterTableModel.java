package gui.telegrams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import javax.validation.constraints.Null;

import org.apache.log4j.Logger;

import controller.Controller;
import core.account.Account;
import core.item.assets.AssetCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.SortableList;
import gui.models.TableModelCls;
import lang.Lang;
import utils.ObserverMessage;
import utils.Pair;

@SuppressWarnings("serial")
// in list of records in wallet
public class WalletTelegramsFilterTableModel extends TableModelCls<String, Transaction> implements Observer {

    private boolean needUpdate = false;
    private long timeUpdate = 0;
    private String sender;
    private String reciever;

   
    public static final int COLUMN_MESSAGE = 1;
    public static final int COLUMN_DATE = 0;
    static Logger LOGGER = Logger.getLogger(WalletTelegramsFilterTableModel.class.getName());
    //ItemAssetMap dbItemAssetMap;
    private SortableList< String, Transaction> transactions;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Date", "Message"});
    private Boolean[] column_AutuHeight = new Boolean[]{true, true, true, true, true, true, true, false, false};
    ArrayList<Transaction> ttt;
       

    public WalletTelegramsFilterTableModel() {
        ttt = new ArrayList<Transaction>();
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
        return this.ttt.get(row);
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
         return this.ttt.get(row);
        
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
        //try
        //{
        if (this.ttt == null || this.ttt.size()==0) {
            return null;
        }

       R_Send transaction = (R_Send) ttt.get(row);
        if (transaction == null)
            return null;

        switch (column) {
            case COLUMN_MESSAGE:

                return transaction.viewCreator() ;
                        
            case COLUMN_DATE:
                return transaction.viewTimestamp();
 }

        return null;

        //} catch (Exception e) {
        //GUI ERROR
        //  LOGGER.error(e.getMessage(),e);
        //  return null;
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
   
            }
            filter();
            this.fireTableDataChanged();

        
        } 
        if (message.getType() ==ObserverMessage.WALLET_ADD_TELEGRAM_TYPE || 
                message.getType() ==ObserverMessage.WALLET_RESET_TELEGRAM_TYPE 
                || message.getType() ==ObserverMessage.WALLET_REMOVE_TELEGRAM_TYPE){
            filter();
            this.fireTableDataChanged();
        }
                                       

        
    }

    private void filter(){
        ttt.clear();
        for (Pair<String, Transaction> tr:transactions){
           
            if(tr.getB().getCreator().getAddress().equals(sender) || tr.getB().getCreator().getAddress().equals(reciever)){
             HashSet<Account> rep = tr.getB().getRecipientAccounts();
             for(Account r:rep){
                 if( r.getAddress().equals(reciever) ||  r.getAddress().equals(sender)){
                    ttt.add(tr.getB());
                    continue;
                }
                
            }
                
            }
        }
        
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

    /**
     * @param sender the sender to set
     */
    public void setSender(String sender) {
        this.sender = sender;
        filter();
        this.fireTableDataChanged();
    }

    /**
     * @param reciever the reciever to set
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
