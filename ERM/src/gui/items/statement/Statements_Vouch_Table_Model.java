package gui.items.statement;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

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
import database.DBMap;
import database.DBSet;
import database.SortableList;
import database.TransactionFinalMap;
import database.TransactionMap;
import lang.Lang;
import utils.DateTimeFormat;
import utils.ObserverMessage;
import utils.Pair;

public class Statements_Vouch_Table_Model extends AbstractTableModel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int COLUMN_TIMESTAMP = 0;
	// public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_CREATOR = 1;
	// public static final int COLUMN_BODY = 2;
	// public static final int COLUMN_AMOUNT = 2;
	 public static final int COLUMN_HEIGHT = 2;
	 public static final int COLUMN_CREATOR_NAME =30;
	List<Transaction> transactions;

	// private SortableList<byte[], Transaction> transactions;

	private String[] columnNames = Lang.getInstance().translate(new String[] { "Timestamp", "Creator", "Height" });// ,
																											// AssetCls.FEE_NAME});
	private Boolean[] column_AutuHeight = new Boolean[] { true, true };
	// private Map<byte[], BlockingQueue<Block>> blocks;
	// private Transaction transaction;
	private int blockNo;
	private int recNo;

	TransactionFinalMap table;

	private ObserverMessage message;

	private String sss;

	public Statements_Vouch_Table_Model(Transaction transaction) {
		table = DBSet.getInstance().getTransactionFinalMap();
		blockNo = transaction.getBlockHeight(DBSet.getInstance());
		recNo = transaction.getSeqNo(DBSet.getInstance());
		transactions = new ArrayList<Transaction>();
		// transactions = read_Sign_Accoutns();
		DBSet.getInstance().getTransactionFinalMap().addObserver(this);
		DBSet.getInstance().getTransactionMap().addObserver(this);
		DBSet.getInstance().getVouchRecordMap().addObserver(this);

	}

	public Class<? extends Object> getColumnClass(int c) { // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
	}

	// читаем колонки которые изменяем высоту
	public Boolean[] get_Column_AutoHeight() {

		return this.column_AutuHeight;
	}

	// устанавливаем колонки которым изменить высоту
	public void set_get_Column_AutoHeight(Boolean[] arg0) {
		this.column_AutuHeight = arg0;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return this.columnNames.length;
	}

	@Override
	public String getColumnName(int index) {
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		if (transactions == null)
			return 0;
		
		return transactions.size();
	}
	
	public String get_No_Trancaction(int row) {
		
		if (this.transactions == null || this.transactions.size() <= row) {
			return null;
		}

		Transaction transaction = this.transactions.get(row);
		if (transaction == null)
			return null;

		return transaction.viewHeightSeq(DBSet.getInstance());
		
	}

	public Transaction getTrancaction(int row) {
		
		if (this.transactions == null || this.transactions.size() <= row) {
			return null;
		}

		return this.transactions.get(row);
		
	}

	@Override
	public Object getValueAt(int row, int column) {
		// TODO Auto-generated method stub
		try {
			if (this.transactions == null || this.transactions.size() <= row) {
				return null;
			}

			Transaction transaction = this.transactions.get(row);
			if (transaction == null)
				return null;

			// R_Vouch i;
			switch (column) {
			case COLUMN_TIMESTAMP:

				// return
				// DateTimeFormat.timestamptoString(transaction.getTimestamp())
				// + " " + transaction.getTimestamp();
				return DateTimeFormat.timestamptoString(transaction.getTimestamp());//.viewTimestamp(); // + " " +
													// transaction.getTimestamp()
													// / 1000;

			/*
			 * case COLUMN_TYPE:
			 * 
			 * //return Lang.transactionTypes[transaction.getType()]; return
			 * Lang.getInstance().translate(transaction.viewTypeName());
			 */

			// case COLUMN_BODY:

			// i = (R_Vouch)transaction;

			// return new String( i.getData(), Charset.forName("UTF-8") )
			// ;//transaction.viewReference();//.viewProperies();

			// case COLUMN_AMOUNT:

			// return
			// NumberAsString.getInstance().numberAsString(transaction.getAmount(transaction.getCreator()));

			// case COLUMN_FEE:

			// return
			// NumberAsString.getInstance().numberAsString(transaction.getFee());

			case COLUMN_CREATOR:

				return transaction.getCreator().getPersonAsString();
				
			case COLUMN_HEIGHT:
				
				return (int)(transaction.getBlockHeight(DBSet.getInstance()));
				
			case COLUMN_CREATOR_NAME:
				return	((Account)transaction.getCreator()).getPerson().b.getName();
			}

			return null;

		} catch (Exception e) {
			// LOGGER.error(e.getMessage(),e);
			return null;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// try
		// {
		this.syncUpdate(o, arg);
		// }
		// catch(Exception e)
		// {
		// GUI ERROR
		// }
	}

	public synchronized void syncUpdate(Observable o, Object arg) {
		message = (ObserverMessage) arg;

		if (message.getType() == ObserverMessage.LIST_VOUCH_TYPE) {
			// CHECK IF NEW LIST
			if (this.transactions == null || this.transactions.size() == 0) {
				transactions = read_Sign_Accoutns();
				this.fireTableDataChanged();
			}
		} 
		
		if (message.getType() == ObserverMessage.ADD_VOUCH_TYPE) {
			// CHECK IF NEW LIST
		
				transactions = read_Sign_Accoutns();
				this.fireTableDataChanged();
			
		} 
		
		if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE)
		{
			//CHECK IF NEW LIST
			
			SortableList<byte[], Transaction> ss = (SortableList<byte[], Transaction>) message.getValue();
			Iterator<Pair<byte[], Transaction>> s = ss.iterator();
			
			boolean fire = false;
			while (s.hasNext()){
				Pair<byte[], Transaction> a = s.next();
				Transaction t = a.getB();
				if (t.getType()== Transaction.VOUCH_TRANSACTION ){
					R_Vouch tt = (R_Vouch)t;
					if (tt.getVouchHeight() == blockNo && tt.getVouchSeq() == recNo) {
						if (!this.transactions.contains(tt)){
							this.transactions.add(tt);
							fire = true;
						}
					}
				}
			}
			
			if (fire)
				this.fireTableDataChanged();
		}
		
		if (message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE
		// || message.getType() == ObserverMessage.REMOVE_VOUCH_TYPE
		// || message.getType() == ObserverMessage.LIST_STATEMENT_TYPE
		// || message.getType() == ObserverMessage.REMOVE_STATEMENT_TYPE
				) {
			Transaction ss = (Transaction) message.getValue();
			if (ss.getType() == Transaction.VOUCH_TRANSACTION) {
				R_Vouch ss1 = (R_Vouch) ss;
				if (ss1.getVouchHeight() == blockNo	&& ss1.getVouchSeq() == recNo) {	
					if (!this.transactions.contains(ss)){
						this.transactions.add(ss);
						this.fireTableDataChanged();
					}
				}
			}
		}
	}

	private List<Transaction> read_Sign_Accoutns() {
		List<Transaction> trans = new ArrayList<Transaction>();
		// ArrayList<Transaction> db_transactions;
		// db_transactions = new ArrayList<Transaction>();
		// tran = new ArrayList<Transaction>();
		// база данных
		// DBSet dbSet = DBSet.getInstance();

		/*
		 * Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs =
		 * DBSet.getInstance().getVouchRecordMap().get(blockNo, recNo);
		 * 
		 * 
		 * if (signs == null) return null; for(Tuple2<Integer, Integer> seq:
		 * signs.b) {
		 * 
		 * Transaction kk = table.getTransaction(seq.a, seq.b); if
		 * (!tran.contains(kk)) tran.add(kk); }
		 */

		@SuppressWarnings("unchecked")
		SortableList<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>> rec = (SortableList<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>) message
				.getValue();

		Iterator<Pair<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>> ss = rec
				.iterator();
		while (ss.hasNext()) {
			Pair<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>> a = (Pair<Tuple2<Integer, Integer>, Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>>>) ss
					.next();
			// block
			if (a.getA().a == blockNo && a.getA().b == recNo) {
				List<Tuple2<Integer, Integer>> ff = a.getB().b;

				for (Tuple2<Integer, Integer> ll : ff) {
					Integer bl = ll.a;
					Integer seg = ll.b;

					Transaction kk = table.getTransaction(bl, seg);
					if (!trans.contains(kk))
						trans.add(kk);
				}
			}

		}
		return trans;
	}

}
