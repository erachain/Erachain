package org.erachain.gui.models;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("serial")
/**
 * не перерисовыется по событиям - статичная таблица при поиске
 */
public class SearchTransactionsTableModel extends TimerTableModelCls<Transaction> implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_BLOCK = 1;
    public static final int COLUMN_SEQ_NO = 2;
    public static final int COLUMN_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_FEE = 5;

    Integer block_No;

    public SearchTransactionsTableModel() {
        super(new String[]{"Timestamp", "Block", "Seq_no", "Type", "Amount", AssetCls.FEE_NAME}, false);
        LOGGER = LoggerFactory.getLogger(SearchTransactionsTableModel.class.getName());
    }

    public void setBlockNumber(String string) {

        try {
            block_No = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            list = new ArrayList<>();
            Transaction transaction = DCSet.getInstance().getTransactionFinalMap().getRecord(string);
            if (transaction != null) {
                transaction.setDC(DCSet.getInstance());
                list.add(transaction);
            }
            needUpdate = true;
            return;
        }

        list = (List<Transaction>) DCSet.getInstance().getTransactionFinalMap().getTransactionsByBlock(block_No);
        needUpdate = true;

    }

    public void Find_Transactions_from_Address(String address) {

        if (address == null || address.equals("")) return;
        Tuple2<Account, String> accountResult = Account.tryMakeAccount(address);
        Account account = accountResult.a;

        if (account != null) {

            list = new ArrayList();
            list.addAll(DCSet.getInstance().getTransactionFinalMap().getTransactionsByAddress(account.getAddress()));//.findTransactions(address, sender=address, recipient=address, minHeight=0, maxHeight=0, type=0, service=0, desc=false, offset=0, limit=0);//.getTransactionsByBlock(block_No);

            needUpdate = true;

        }

    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (this.list == null || this.list.size() - 1 < row) {
                return null;
            }

            Transaction transaction = list.get(row);
            if (transaction == null) {
                return null;
            }


            switch (column) {
                case COLUMN_TIMESTAMP:

                    return DateTimeFormat.timestamptoString(transaction.getTimestamp());
                //return transaction.viewTimestamp() + " " + transaction.getTimestamp() / 1000;

                case COLUMN_TYPE:

                    //return Lang.transactionTypes[transaction.getType()];
                    return Lang.getInstance().translate(transaction.viewTypeName());

                case COLUMN_AMOUNT:

                    return transaction.getAmount();//.getAmount(transaction.getCreator()));

                case COLUMN_FEE:

                    return transaction.getFee();

                case COLUMN_BLOCK:

                    return transaction.getBlockHeight();

                case COLUMN_SEQ_NO:
                    return transaction.getSeqNo();

                //		case COLUMN_NO:
                //			return row;
            }


            return null;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {

        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.GUI_REPAINT) {
            needUpdate = false;
            this.fireTableDataChanged();
        }
    }

}
