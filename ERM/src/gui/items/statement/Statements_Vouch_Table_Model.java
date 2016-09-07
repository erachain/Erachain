package gui.items.statement;

import java.math.BigDecimal;
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
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.item.assets.AssetCls;
import core.transaction.R_SignNote;
//import core.transaction.R_SignStatement_old;
import core.transaction.R_Vouch;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;
import database.TransactionFinalMap;
import database.VouchRecordMap;
import gui.items.statement.Statements_Table_Model.MessageBuf;
import lang.Lang;
import network.Peer;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;

public class Statements_Vouch_Table_Model extends AbstractTableModel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	

	public static final int COLUMN_TIMESTAMP = 0;
//	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_CREATOR = 1;
	public static final int COLUMN_BODY = 2;
//	public static final int COLUMN_AMOUNT = 2;
//	public static final int COLUMN_FEE = 3;
	List<Transaction> transactions;
	
//	private SortableList<byte[], Transaction> transactions;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Signatures"});//, AssetCls.FEE_NAME});
	private Boolean[] column_AutuHeight = new Boolean[]{true,true};
//	private Map<byte[], BlockingQueue<Block>> blocks;
	//private Transaction transaction;
	private int blockNo;
	private int recNo;
	
	
	
	public Statements_Vouch_Table_Model(Transaction transaction){
	//	transactions = new ArrayList<Transaction>();
		
		//transaction = transaction;
		blockNo = transaction.getBlockHeight(DBSet.getInstance());
		recNo = transaction.getSeqNo(DBSet.getInstance());

		//Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs = DBSet.getInstance().getVouchRecordMap().get(),));

		
	
		
	//	for (Account account : Controller.getInstance().getAccounts()) {
	//		transactions.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(account.getAddress(), Transaction.SIGN_NOTE_TRANSACTION,0));//.SEND_ASSET_TRANSACTION, 0));	
	//	}
		
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
		transactions = read_Sign_Accoutns();	
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
	
	
	}
	
	// set class
	
	

		public Class<? extends Object> getColumnClass(int c) {     // set column type
			       return getValueAt(0, c).getClass();
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
	
	public String get_Account (){
	
		
		
		
		
		return null;
		
		
		
	}

	@Override
	public Object getValueAt(int row, int column) {
		// TODO Auto-generated method stub
		try
		{
			if(this.transactions == null || this.transactions.size() -1 < row)
			{
				return null;
			}
			
			Transaction transaction = this.transactions.get(row);
			
			
	//		R_Vouch i;
			switch(column)
			{
			case COLUMN_TIMESTAMP:
				
				//return DateTimeFormat.timestamptoString(transaction.getTimestamp()) + " " + transaction.getTimestamp();
				return transaction.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;
				
		/*	case COLUMN_TYPE:
				
				//return Lang.transactionTypes[transaction.getType()];
				return Lang.getInstance().translate(transaction.viewTypeName());
		*/
				
				
				
	//		case	 COLUMN_BODY:
				
				
				
	//			 i = (R_Vouch)transaction;
				
	//			return new String( i.getData(), Charset.forName("UTF-8") ) ;//transaction.viewReference();//.viewProperies();
				
				
	//		case COLUMN_AMOUNT:
				
	//			return NumberAsString.getInstance().numberAsString(transaction.getAmount(transaction.getCreator()));
				
	//		case COLUMN_FEE:
				
	//			return NumberAsString.getInstance().numberAsString(transaction.getFee());	
			
			case COLUMN_CREATOR:
				
				
				return (transaction.getCreator().toString());
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
		if(message.getType() == ObserverMessage.ADD_STATEMENT_TYPE)
		{
			if(this.transactions == null)
			{
			//	this.statuses = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>) message.getValue();
			//	this.statusesMap .registerObserver();
				//this.imprints.sort(PollMap.NAME_INDEX);
				transactions = read_Sign_Accoutns();
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
			transactions = read_Sign_Accoutns();
			this.fireTableDataChanged();
		}	
	}	
	
	
	private List<Transaction> read_Sign_Accoutns(){
		List<Transaction> tran = new ArrayList<Transaction>();
		//ArrayList<Transaction> db_transactions;
		//db_transactions = new ArrayList<Transaction>();
		//tran = new ArrayList<Transaction>();
		// база данных	
		//DBSet dbSet = DBSet.getInstance();
		TransactionFinalMap table = DBSet.getInstance().getTransactionFinalMap();
		
		Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs = DBSet.getInstance().getVouchRecordMap().get(blockNo, recNo);

		for(Tuple2<Integer, Integer> seq: signs.b)
		{
			 tran.add(table.getTransaction(seq.a, seq.b));
		}
		return tran;
	
	}	

}
