package gui.items.statement;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.swing.table.AbstractTableModel;

import org.json.simple.JSONArray;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.item.assets.AssetCls;
import core.transaction.R_SignNote;
import core.transaction.R_SignStatement;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;
import gui.items.statement.Statements_Table_Model.MessageBuf;
import lang.Lang;
import network.Peer;
import utils.NumberAsString;
import utils.Pair;

public class Statement_Table_Model_New extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<Transaction> transactions;
	ArrayList<Transaction> db_transactions;

	public static final int COLUMN_TIMESTAMP = 0;
//	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_CREATOR = 1;
	public static final int COLUMN_BODY = 2;
//	public static final int COLUMN_AMOUNT = 2;
//	public static final int COLUMN_FEE = 3;

	
//	private SortableList<byte[], Transaction> transactions;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Creator", "Statement"});//, AssetCls.FEE_NAME});
//	private Map<byte[], BlockingQueue<Block>> blocks;
	
	
	public Statement_Table_Model_New(){
	//	transactions = new ArrayList<Transaction>();
		
		db_transactions = new ArrayList<Transaction>();
		transactions = new ArrayList<Transaction>();
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
			
			
		// база данных	
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
			       return getValueAt(0, c).getClass();
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
			
			
			R_SignNote i;
			switch(column)
			{
			case COLUMN_TIMESTAMP:
				
				//return DateTimeFormat.timestamptoString(transaction.getTimestamp()) + " " + transaction.getTimestamp();
				return transaction.viewTimestamp(); // + " " + transaction.getTimestamp() / 1000;
				
		/*	case COLUMN_TYPE:
				
				//return Lang.transactionTypes[transaction.getType()];
				return Lang.getInstance().translate(transaction.viewTypeName());
		*/
				
				
				
			case	 COLUMN_BODY:
				
				
				
				 i = (R_SignNote)transaction;
				
				return new String( i.getData(), Charset.forName("UTF-8") ) ;//transaction.viewReference();//.viewProperies();
				
				
	//		case COLUMN_AMOUNT:
				
	//			return NumberAsString.getInstance().numberAsString(transaction.getAmount(transaction.getCreator()));
				
	//		case COLUMN_FEE:
				
	//			return NumberAsString.getInstance().numberAsString(transaction.getFee());	
			
			case COLUMN_CREATOR:
				
				
				return transaction.getCreator().toString();
			}
			
			return null;
			
		} catch (Exception e) {
	//		LOGGER.error(e.getMessage(),e);
			return null;
		}
	}

}
