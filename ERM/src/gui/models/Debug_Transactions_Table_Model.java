


	package gui.models;

	import java.util.Observable;
	import java.util.Observer;

	import org.apache.log4j.Logger;

	import core.transaction.Transaction;
	import utils.DateTimeFormat;
	import utils.NumberAsString;
	import utils.ObserverMessage;
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

	
	
	

