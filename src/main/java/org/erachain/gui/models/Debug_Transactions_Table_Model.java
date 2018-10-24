package org.erachain.gui.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Fun.Tuple2;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.SortableList;
import org.erachain.datachain.TransactionMap;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;

@SuppressWarnings("serial")
public class Debug_Transactions_Table_Model extends AbstractTableModel implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_FEE = 2;
    private static final int MAX_ROWS = 1000;
    private static final Logger LOGGER = LoggerFactory            .getLogger(Debug_Transactions_Table_Model.class);
    private List<Transaction> transactions;
    SortableList <byte[],Transaction> list;
    private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Fee"});

    public Debug_Transactions_Table_Model() {
        DCSet.getInstance().getTransactionMap().addObserver(this);
  }

    public Class<? extends Object> getColumnClass(int c) {     // set column type
        Object o = getValueAt(0, c);
        return o == null ? Null.class : o.getClass();
    }
	
	/*
	@Override
	public SortableList<byte[], Transaction> getSortableList() 
	{
		return this.transactions;
	}
	*/

    public Transaction getTransaction(int row) {
        if (list == null
                || row >= list.size())
            return null;

        return list.get(row).getB();
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
        if (this.list == null) {
            return 0;
        }

        return this.list.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            if (this.list == null || this.list.size() <= row) {
                return null;
            }

            Transaction transaction = this.list.get(row).getB();
            if (transaction == null)
                return null;

            switch (column) {
                case COLUMN_TIMESTAMP:

                    return DateTimeFormat.timestamptoString(transaction.getTimestamp());

                case COLUMN_TYPE:

                    return Lang.getInstance().translate(transaction.viewTypeName());

                case COLUMN_FEE:

                    return NumberAsString.formatAsString(transaction.getFee());
            }

            return null;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            this.syncUpdate(o, arg);
        } catch (Exception e) {
            //GUI ERROR
            LOGGER.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void syncUpdate(Observable o, Object arg) {
        ObserverMessage message = (ObserverMessage) arg;
        int type = message.getType();

        if (type == ObserverMessage.LIST_UNC_TRANSACTION_TYPE) {
            //CHECK IF NEW LIST
    //        LOGGER.error("gui.models.Debug_Transactions_Table_Model.syncUpdate - LIST_UNC_TRANSACTION_TYPE");
            if (this.list == null) {
                this.list = (SortableList<byte[], Transaction>) message.getValue();
                this.list.registerObserver();
            }
            this.fireTableDataChanged();
        } else if (type == ObserverMessage.ADD_UNC_TRANSACTION_TYPE) {
              this.fireTableDataChanged();
        } else if (type == ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE) {
               this.fireTableDataChanged();
        }

    }

   

    public void removeObservers() {
        //this.transactions.removeObserver();
        DCSet.getInstance().getTransactionMap().deleteObserver(this);
        this.list.removeObserver();
    }
}
