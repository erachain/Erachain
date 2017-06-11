package gui.models;

import java.util.Observable;
import java.util.Observer;

import javax.validation.constraints.Null;

import org.apache.log4j.Logger;

import core.transaction.Transaction;
import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import database.SortableList;
import database.TransactionMap;
import lang.Lang;

@SuppressWarnings("serial")
public class Debug_Transactions_Table_Model extends TableModelCls<byte[], Transaction> implements Observer {
	
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_FEE = 2;
	
	private static final Logger LOGGER = Logger
			.getLogger(Debug_Transactions_Table_Model.class);
	private SortableList<byte[], Transaction> transactions;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Fee"});

	public Debug_Transactions_Table_Model()
	{
		Controller.getInstance().addObserver(this);
	}
	
	public Class<? extends Object> getColumnClass(int c)
	{     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
    }
	
	@Override
	public SortableList<byte[], Transaction> getSortableList() 
	{
		return this.transactions;
	}
	
	public Transaction getTransaction(int row)
	{
		if (transactions == null
				|| row >= transactions.size())
			return null;
		
		Pair<byte[], Transaction> record = transactions.get(row);
		if (record == null)
			return null;
		
		return record.getB();
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
			
			Pair<byte[], Transaction> record = this.transactions.get(row);
			if (record == null)
				return null;
			
			Transaction transaction = record.getB();
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
		}
	}
	
	@SuppressWarnings("unchecked")	
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE)
		{
			//CHECK IF NEW LIST
			if(this.transactions == null)
			{
				this.transactions = (SortableList<byte[], Transaction>) message.getValue();
				this.transactions.registerObserver();
				this.transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
			}
			
			this.fireTableDataChanged();
		}
		else if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
		{
			//CHECK IF LIST UPDATED
//			Pair<byte[], Transaction> value = (Pair<byte[], Transaction>) message.getValue();
//			this.transactions.add(value);
			this.fireTableDataChanged();
		}	
		
		else if( message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{
			//CHECK IF LIST UPDATED
//			Pair<byte[], Transaction> value = (Pair<byte[], Transaction>) message.getValue();
//			this.transactions.remove(value);
			this.fireTableDataChanged();
		}	
		
	}

	public void removeObservers() 
	{
		this.transactions.removeObserver();
		Controller.getInstance().deleteObserver(this);		
	}
}
