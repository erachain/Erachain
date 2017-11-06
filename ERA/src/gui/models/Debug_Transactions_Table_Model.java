package gui.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

import org.apache.log4j.Logger;
import org.mapdb.Fun.Tuple2;

import core.block.Block;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.SortableList;
import datachain.TransactionMap;
import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import lang.Lang;

@SuppressWarnings("serial")
public class Debug_Transactions_Table_Model extends AbstractTableModel implements Observer {
	
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_FEE = 2;
	
	private static final Logger LOGGER = Logger
			.getLogger(Debug_Transactions_Table_Model.class);
	private List<Transaction> transactions;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Fee"});

	public Debug_Transactions_Table_Model()
	{
		DCSet.getInstance().getTransactionMap().addObserver(this);

		resetRows();
	}
	
	public Class<? extends Object> getColumnClass(int c)
	{     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
    }
	
	/*
	@Override
	public SortableList<byte[], Transaction> getSortableList() 
	{
		return this.transactions;
	}
	*/
	
	public Transaction getTransaction(int row)
	{
		if (transactions == null
				|| row >= transactions.size())
			return null;
		
		return transactions.get(row);
	}
	
	@Override
	public int getColumnCount() 
	{
		return columnNames.length;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		if(this.transactions == null)
		{
			return 0;
		}
		
		return this.transactions.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		try
		{
			if(this.transactions == null || this.transactions.size() <= row)
			{
				return null;
			}
			
			Transaction transaction = this.transactions.get(row);
			if (transaction == null)
				return null;

			switch(column)
			{
			case COLUMN_TIMESTAMP:
				
				return DateTimeFormat.timestamptoString(transaction.getTimestamp());
				
			case COLUMN_TYPE:
				
				return Lang.getInstance().translate( transaction.viewTypeName());
				
			case COLUMN_FEE:
				
				return NumberAsString.getInstance().numberAsString(transaction.getFee());		
			}
			
			return null;
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			return null;
		}
	}

	@Override
	public void update(Observable o, Object arg) 
	{
		try
		{
			this.syncUpdate(o, arg);
		}
		catch(Exception e)
		{
			//GUI ERROR
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	@SuppressWarnings("unchecked")	
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		int type = message.getType();
		
		if(type == ObserverMessage.LIST_UNC_TRANSACTION_TYPE)
		{
			//CHECK IF NEW LIST

			LOGGER.error("gui.models.Debug_Transactions_Table_Model.syncUpdate - LIST_UNC_TRANSACTION_TYPE");

			this.resetRows();
			this.fireTableDataChanged();
		}
		else if(type == ObserverMessage.ADD_UNC_TRANSACTION_TYPE)
		{
			//CHECK IF LIST UPDATED
			this.transactions.add((Transaction)message.getValue());
			this.fireTableRowsInserted(0, 0);			
			if (this.transactions.size() > 100) {
				this.transactions.remove(100);
				this.fireTableDataChanged();							
			}
		}		
		else if( message.getType() == ObserverMessage.REMOVE_UNC_TRANSACTION_TYPE)
		{
			//CHECK IF LIST UPDATED
			Transaction transaction = (Transaction)message.getValue();
			this.transactions.remove(transaction);
			this.fireTableDataChanged();			
		}	
		
	}

	public void resetRows() {
		this.transactions = new ArrayList<Transaction>();
		TransactionMap map = DCSet.getInstance().getTransactionMap();
		byte[] key;
		Iterator<byte[]> iterator = map.getIterator(0, true);
		int i = 0;
		while (iterator.hasNext() && i++ < 100) {
			
			key = iterator.next();
			this.transactions.add(map.get(key));
		}
	}

	public void removeObservers() 
	{
		//this.transactions.removeObserver();
		DCSet.getInstance().getTransactionMap().deleteObserver(this);		
	}
}
