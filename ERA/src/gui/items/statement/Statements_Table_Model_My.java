package gui.items.statement;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.ItemCls;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import database.DBSet;
import lang.Lang;
import utils.ObserverMessage;

public class Statements_Table_Model_My extends AbstractTableModel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	

	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_CREATOR = 1;
	public static final int COLUMN_NOTE = 2;
	public static final int COLUMN_BODY = 3;
	List<Transaction> transactions;
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Creator", "Template", "Statement"});//, AssetCls.FEE_NAME});
	private Boolean[] column_AutuHeight = new Boolean[]{true,true,true,false};
	Object[] collection;

	
	
	public Statements_Table_Model_My(){
		transactions = new ArrayList<Transaction>();
		addObserver();
		transactions = read_Statement();
	
	}
	
	// set class
	
		public Class<? extends Object> getColumnClass(int c) {     // set column type
			Object o = getValueAt(0, c);
			return o==null?Null.class:o.getClass();
		}
	
		// читаем колонки которые изменяем высоту	   
		public Boolean[] get_Column_AutoHeight(){
			
			return this.column_AutuHeight;
		}
	// устанавливаем колонки которым изменить высоту	
		public void set_get_Column_AutoHeight( Boolean[] arg0){
			this.column_AutuHeight = arg0;	
		}
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return this.columnNames.length;
	}
	
	public Transaction get_Statement(int row){
		
		if (this.collection == null || this.collection.length <= row) {
			return null;
		}

		Transaction transaction = (Transaction) this.collection[row];
		if (transaction == null)
			return null;

		return transaction;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return collection.length;//transactions.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		// TODO Auto-generated method stub
		try
		{
			if(this.collection == null || this.collection.length == 0)
			{
				return null;
			}
			Transaction trans =(Transaction) collection[row];
			if (trans == null)
				return null;
			
			R_SignNote record = (R_SignNote)trans;
			
			PublicKeyAccount creator;
			switch(column)
			{
			case COLUMN_TIMESTAMP:
				
				//return DateTimeFormat.timestamptoString(transaction.getTimestamp()) + " " + transaction.getTimestamp();
				return record.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;

			case COLUMN_NOTE:
				
				ItemCls item = ItemCls.getItem(DBSet.getInstance(), ItemCls.NOTE_TYPE, record.getKey());
				return item==null?null:item.toString();
				
			case COLUMN_BODY:				
				
				if (record.getData() == null)
					return null;
				
				String str = "";
				 try {
					JSONObject data = (JSONObject) JSONValue.parseWithException(new String(record.getData(), Charset.forName("UTF-8")));
					str =  (String) data.get("!!&_Title");
					if (str == null) str = (String) data.get("Title");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					
					 str = new String( record.getData() , Charset.forName("UTF-8") );
				}	
				 if (str == null) return "";
				 if (str.length()>50) return str.substring(0,50)+"...";
					return str ;//transaction.viewReference();//.viewProperies();
	
			case COLUMN_CREATOR:
				
				creator = record.getCreator();
				
				return creator==null?null:creator.getPersonAsString();
			}
			
			return null;
			
		} catch (Exception e) {
		//	LOGGER.error(e.getMessage(),e);
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
	
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_STATEMENT_TYPE)
		{
			if(this.transactions == null)
			{
				transactions = read_Statement();
			}
			
			this.fireTableDataChanged();
		}
		
		
		//CHECK IF LIST UPDATED
		if(	message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE 
				)
		{
			Transaction trans = (Transaction) message.getValue();
			if (trans.getType() != Transaction.SIGN_NOTE_TRANSACTION) return;
			transactions.add(trans);
			HashSet<Transaction> col = new HashSet<Transaction>(transactions);	
			  collection = col.toArray();
			
			this.fireTableDataChanged();
		}	
	}	
	
	
	private List<Transaction> read_Statement() {
		List<Transaction> tran;
		ArrayList<Transaction> db_transactions;
		db_transactions = new ArrayList<Transaction>();
		tran = new ArrayList<Transaction>();
		transactions.clear();
		// база данных	
		for (Transaction transaction : Controller.getInstance().getUnconfirmedTransactions()) {
			if(transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION)
			{
				transactions.add(transaction);
			}
		}
		
		for (Account account : Controller.getInstance().getAccounts()) {
			transactions.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(account.getAddress(), Transaction.SIGN_NOTE_TRANSACTION,0));//.SEND_ASSET_TRANSACTION, 0));	
		}
		
			HashSet<Transaction> col = new HashSet<Transaction>(transactions);	
		  collection = col.toArray();
		 
		return transactions;
	
	}

	public void removeObserver(){
		
		Controller.getInstance().deleteObserver(this);	
		
	}
	public void addObserver(){
		Controller.getInstance().addObserver(this);	
	}
	
}
