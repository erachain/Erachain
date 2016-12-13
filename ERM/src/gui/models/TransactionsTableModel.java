package gui.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;

import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import core.block.Block;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;
import database.TransactionFinalMap;
import database.TransactionMap;
import lang.Lang;

@SuppressWarnings("serial")
// IN gui.DebugTabPane used
public class TransactionsTableModel extends TableModelCls<byte[], Transaction> implements Observer {
	
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_AMOUNT = 2;
	public static final int COLUMN_FEE = 3;
	
	//private SortableList<byte[], Transaction> transactions;
	private Integer blockNo = null;
	Long block_Height;
	List<Transaction> transactions;
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Amount", AssetCls.FEE_NAME});
	//private String[] transactionTypes = Lang.getInstance().translate(new String[]{"", "Genesis", "Payment", "Name Registration", "Name Update", "Name Sale", "Cancel Name Sale", "Name Purchase", "Poll Creation", "Poll Vote", "Arbitrary Transaction", "Check Issue", "Check Transfer", "Order Creation", "Cancel Order", "Multi Payment", "Deploy AT", "Message Transaction","Accounting Transaction"});

	static Logger LOGGER = Logger.getLogger(TransactionsTableModel.class.getName());

	public TransactionsTableModel()
	{
		Controller.getInstance().addObserver(this);
//		SortableList<byte[], Transaction> a = this.transactions;
		
	//	TransactionFinalMap table = DBSet.getInstance().getTransactionFinalMap();
		
		
		
	//	 byte[] block_key = DBSet.getInstance().getBlockHeightsMap().get((long) blockNo);
	//	 Block block = DBSet.getInstance().getBlockMap().get(block_key);
	//	 transactions = block.getTransactions();
	//	 Transaction signs = DBSet.getInstance().getTransactionFinalMap().getTransaction(21452, 1);

//		for(Tuple2<Integer, Integer> seq: signs.b)
//		{
			// tran.add(table.getTransaction(seq.a, seq.b));
//		}
		
	}
	
//	@Override
//	public SortableList<byte[], Transaction> getSortableList() 
//	{
//		return this.transactions;
//	}
	
	public void Set_Block_Namber(String string){
		
		 byte[] block_key = DBSet.getInstance().getBlockHeightsMap().get(Long.parseLong(string));
		 Block block = DBSet.getInstance().getBlockMap().get(block_key);
		 transactions = block.getTransactions();	
		 this.fireTableDataChanged();
		
	}
	
	
	public Transaction getTransaction(int row)
	{
		Transaction data = transactions.get(row);
		if (data == null ) {
			return null;
		}
		return data;
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
			
			Transaction data = transactions.get(row);
			if (data == null)  {
				return null;
			}

			Transaction transaction = data;
			
			switch(column)
			{
			case COLUMN_TIMESTAMP:
				
				//return DateTimeFormat.timestamptoString(transaction.getTimestamp()) + " " + transaction.getTimestamp();
				return transaction.viewTimestamp() + " " + transaction.getTimestamp() / 1000;
				
			case COLUMN_TYPE:
				
				//return Lang.transactionTypes[transaction.getType()];
				return Lang.getInstance().translate(transaction.viewTypeName());
				
			case COLUMN_AMOUNT:
				
				return NumberAsString.getInstance().numberAsString(transaction.getAmount(transaction.getCreator()));
				
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
			//	this.transactions = (SortableList<byte[], Transaction>) message.getValue();
			//	this.transactions.registerObserver();
			//	this.transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
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
	//	this.transactions.removeObserver();
		Controller.getInstance().deleteObserver(this);		
	}

	@Override
	public SortableList<byte[], Transaction> getSortableList() {
		// TODO Auto-generated method stub
		return null;
	}
}
