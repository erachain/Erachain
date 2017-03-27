package gui.items.statement;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

import javax.swing.table.AbstractTableModel;

import org.json.simple.JSONArray;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.transaction.R_SignNote;
//import core.transaction.R_SignStatement_old;
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
//	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_CREATOR = 1;
	public static final int COLUMN_NOTE = 2;
	public static final int COLUMN_BODY = 3;
	//public static final int COLUMN_SIGNATURE = 3;
//	public static final int COLUMN_FEE = 3;
	List<Transaction> transactions;
	
//	private SortableList<byte[], Transaction> transactions;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Creator", "Template", "Statement"});//, AssetCls.FEE_NAME});
	private Boolean[] column_AutuHeight = new Boolean[]{true,true,true,false};
//	private Map<byte[], BlockingQueue<Block>> blocks;
	
	
	public Statements_Table_Model_My(){
	//	transactions = new ArrayList<Transaction>();
		
	
/*
		for (Transaction transaction : Controller.getInstance().getUnconfirmedTransactions()) {
			if(transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION);
			{
				transactions.add(transaction);
			}
		}
		
		for (Account account : Controller.getInstance().getAccounts()) {
			transactions.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(account.getAddress(), Transaction.SIGN_NOTE_TRANSACTION,0));//.SEND_ASSET_TRANSACTION, 0));	
		}
	*/	
	//	Pair<Block, List<Transaction>> result = Controller.getInstance().scanTransactions(null, 0, 0, 0, 0, null);
			
	
	
//	GenesisBlock block = new GenesisBlock();
			
		//FOR ALL TRANSACTIONS IN BLOCK
//	List<Transaction> transactions = block.getTransactions();
	
	//BlockChain blockChain = new BlockChain(null);
	//Block lastBlock = blockChain.getLastBlock();
	
	
	//blockChain.getBlock(0)
	
	//	private DBSet dbSet;
		
			
			//CREATE GENESIS BLOCK
		//	genesisBlock = new GenesisBlock();
		//	genesisTimestamp = genesisBlock.getTimestamp(null);
		transactions = new ArrayList<Transaction>();
		Controller.getInstance().addObserver(this);	
		transactions = read_Statement();	
/*		// база данных	
				DBSet dbSet = DBSet.getInstance();
		// читаем все блоки
			SortableList<byte[], Block> lists = dbSet.getBlockMap().getList();
		// проходим по блокам
			for(Pair<byte[], Block> list: lists)
			{
				
		// читаем транзакции из блока
				db_transactions = (ArrayList<Transaction>) list.getB().getTransactions();
		// проходим по транзакциям
				for (Transaction transaction:db_transactions){
		// если ноте то пишем в transactions			
				 if(transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION)	transactions.add(transaction);	
				
				}
			}
*/				
		
	//	this.blocks = new HashMap<byte[], BlockingQueue<Block>>();
	
	//
		
		
		
	//	List<Pair<Account, Block>> blocks = Controller.getInstance().getLastBlocks();
	//	JSONArray array = new JSONArray();
	/*	
		for(Pair<Account, Block> block: blocks)
		{
			array.add(block.getB());
			List<Transaction> transactions = block.getTransactions()
		}
		
	
		
		for (Transaction transaction : Controller.getInstance().getUnconfirmedTransactions()) {
			if(transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION);
			{
				transactions.add(transaction);
			}
		}
		
		for (Account account : Controller.getInstance().getAccounts()) {
			transactions.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(null, Transaction.SIGN_NOTE_TRANSACTION,0));//.SEND_ASSET_TRANSACTION, 0));	
		}
		
		
	*/		
		
		int a = 10;
	
	}
	
	// set class
	
		public Class<? extends Object> getColumnClass(int c) {     // set column type
			Object o = getValueAt(0, c);
			return o==null?null:o.getClass();
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
		
		if (this.transactions == null || this.transactions.size() <= row) {
			return null;
		}

		Transaction transaction = this.transactions.get(row);
		if (transaction == null)
			return null;

		return transactions.get(row);
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return transactions.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		// TODO Auto-generated method stub
		try
		{
			if(this.transactions == null || this.transactions.size() < 0
					|| this.transactions.size() -1 < row)
			{
				return null;
			}
			
			Transaction trans = this.transactions.get(row);
			if (trans == null)
				return null;
			
			R_SignNote record = (R_SignNote)trans;
			
			PublicKeyAccount creator;
			switch(column)
			{
			case COLUMN_TIMESTAMP:
				
				//return DateTimeFormat.timestamptoString(transaction.getTimestamp()) + " " + transaction.getTimestamp();
				return record.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;
				
		/*	case COLUMN_TYPE:
				
				//return Lang.transactionTypes[transaction.getType()];
				return Lang.getInstance().translate(transaction.viewTypeName());
		*/

			case COLUMN_NOTE:
				
				ItemCls item = ItemCls.getItem(DBSet.getInstance(), ItemCls.NOTE_TYPE, record.getKey());
				return item==null?null:item.toString();
				
			case COLUMN_BODY:				
				
				if (record.getData() == null)
					return null;
				
				return new String( record.getData() , Charset.forName("UTF-8") ) ;//transaction.viewReference();//.viewProperies();
				
				
	//		case COLUMN_AMOUNT:
				
	//			return NumberAsString.getInstance().numberAsString(transaction.getAmount(transaction.getCreator()));
				
	//		case COLUMN_FEE:
				
	//			return NumberAsString.getInstance().numberAsString(transaction.getFee());	
			
			case COLUMN_CREATOR:
				
				creator = record.getCreator();
				
				return creator==null?null:creator.getPersonAsString();
			}
			
			return null;
			
		} catch (Exception e) {
	//		LOGGER.error(e.getMessage(),e);
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
//		System.out.println( message.getType());
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_STATEMENT_TYPE)
		{
			if(this.transactions == null)
			{
			//	this.statuses = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>) message.getValue();
			//	this.statusesMap .registerObserver();
				//this.imprints.sort(PollMap.NAME_INDEX);
				transactions = read_Statement();
			}
			
			this.fireTableDataChanged();
		}
		
		
		//CHECK IF LIST UPDATED
		if( //message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE 
			//	||
				message.getType() == ObserverMessage.ADD_BLOCK_TYPE 
		//		|| message.getType() == ObserverMessage.LIST_STATEMENT_FAVORITES_TYPE
		//		|| message.getType() == ObserverMessage.LIST_STATEMENT_TYPE
		//		|| message.getType() == ObserverMessage.REMOVE_STATEMENT_TYPE
				
				)
		{
			//this.statuses = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>) message.getValue();
			transactions = read_Statement();
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
				
		return transactions;
	
	}

}
