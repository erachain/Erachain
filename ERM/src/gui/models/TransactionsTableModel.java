package gui.models;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import controller.Controller;
import core.transaction.Transaction;
import database.SortableList;
import database.TransactionMap;
import lang.Lang;

@SuppressWarnings("serial")
// IN gui.DebugTabPane used
public class TransactionsTableModel extends TableModelCls<byte[], Transaction> implements Observer {
	
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_AMOUNT = 2;
	public static final int COLUMN_FEE = 3;
	
	private SortableList<byte[], Transaction> transactions;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Amount", "OIL"});
	//private String[] transactionTypes = Lang.getInstance().translate(new String[]{"", "Genesis", "Payment", "Name Registration", "Name Update", "Name Sale", "Cancel Name Sale", "Name Purchase", "Poll Creation", "Poll Vote", "Arbitrary Transaction", "Check Issue", "Check Transfer", "Order Creation", "Cancel Order", "Multi Payment", "Deploy AT", "Message Transaction","Accounting Transaction"});

	static Logger LOGGER = Logger.getLogger(TransactionsTableModel.class.getName());

	public TransactionsTableModel()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<byte[], Transaction> getSortableList() 
	{
		return this.transactions;
	}
	
	public Transaction getTransaction(int row)
	{
		return transactions.get(row).getB();
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
			if(this.transactions == null || this.transactions.size() -1 < row)
			{
				return null;
			}
			
			Transaction transaction = this.transactions.get(row).getB();
			
			switch(column)
			{
			case COLUMN_TIMESTAMP:
				
				return DateTimeFormat.timestamptoString(transaction.getTimestamp());
				
			case COLUMN_TYPE:
				
				//return Lang.transactionTypes[transaction.getType()];
				return Lang.getInstance().translate(transaction.viewTypeName());
				
			case COLUMN_AMOUNT:
				
				return NumberAsString.getInstance().numberAsString(transaction.viewAmount(transaction.getCreator()));
				
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
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE)
		{
			if(this.transactions == null)
			{
				this.transactions = (SortableList<byte[], Transaction>) message.getValue();
				this.transactions.registerObserver();
				this.transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}

	public void removeObservers() 
	{
		this.transactions.removeObserver();
		Controller.getInstance().deleteObserver(this);		
	}
}
