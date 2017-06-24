package gui.items.statement;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.block.Block;
import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;
import lang.Lang;
import utils.ObserverMessage;
import utils.Pair;

public class Statements_Table_Model_Search extends AbstractTableModel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_CREATOR = 1;
//	public static final int COLUMN_NOTE = 2;
	public static final int COLUMN_BODY = 2;
	public static final int COLUMN_FAVORITE = 3;
	List<R_SignNote> transactions;
	private String[] columnNames = new String[] { "Timestamp", "Creator"/*, "Template"*/, "Statement" , "Favorite"};// ,
																									// AssetCls.FEE_NAME});
	private Boolean[] column_AutuHeight = new Boolean[] { true, true, true, false };

	public Statements_Table_Model_Search() {

		//transactions = new ArrayList<Transaction>();
		//addObservers();
		//transactions = read_Statement("", (long) -1);
		clear();
	}

	// set class

	public Class<? extends Object> getColumnClass(int c) { // set column type
		Object o = getValueAt(0, c);
		return o == null ? Null.class : o.getClass();
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

	public Transaction get_Statement(int row) {

		if (transactions == null || row < 0 || transactions.size() <= row)
			return null;

		return transactions.get(row);
	}

	@Override
	public String getColumnName(int index) {
		return Lang.getInstance().translate(columnNames[index]);
	}

	public String getColumnNameNO_Translate(int index) {
		return columnNames[index];
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return transactions.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		// TODO Auto-generated method stub
	//	try {
			if (this.transactions == null || this.transactions.size() - 1 < row) {
				return null;
			}

			R_SignNote record = (R_SignNote) this.transactions.get(row);

			switch (column) {
			case COLUMN_TIMESTAMP:

				return record.viewTimestamp(); 
	/*			
			case COLUMN_NOTE:

				if (record.getVersion() ==2) {
					
					
					return " ";
				}
				//view version 1
				
				
				
				return ItemCls.getItem(DBSet.getInstance(), ItemCls.NOTE_TYPE, record.getKey()).toString();
			*/	

			case COLUMN_BODY:

				if (record.getData() == null)
					return "";
				
				if(record.getVersion() == 2){
					Tuple3<String, String, JSONObject> a;
					try {
						a = record.parse_Data_V2_Without_Files();
					
					return a.b;} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
							
				
				String str = "";
				try {
					JSONObject data = (JSONObject) JSONValue
							.parseWithException(new String(record.getData(), Charset.forName("UTF-8")));
					str = (String) data.get("!!&_Title");
					if (str == null)
						str = (String) data.get("Title");
				} catch (Exception e) {
					// TODO Auto-generated catch block

					str = new String(record.getData(), Charset.forName("UTF-8"));
				}
				if (str == null)
					return "";
				if (str.length() > 50)
					return str.substring(0, 50) + "...";
				return str;// transaction.viewReference();//.viewProperies();
			case COLUMN_CREATOR:

				return record.getCreator().getPersonAsString();
			case COLUMN_FAVORITE:
				return record.isFavorite();
			}

			return null;

		//} catch (Exception e) {
			// LOGGER.error(e.getMessage(),e);
		//	return null;
		//}
	}

	@Override
	public void update(Observable o, Object arg) {
		try {
			this.syncUpdate(o, arg);
		} catch (Exception e) {
			// GUI ERROR
		}
	}

	public synchronized void syncUpdate(Observable o, Object arg) {
		ObserverMessage message = (ObserverMessage) arg;
		// System.out.println( message.getType());

		// CHECK IF NEW LIST
		if (message.getType() == ObserverMessage.LIST_STATEMENT_TYPE) {
			if (this.transactions == null) {
				transactions = read_Statement("",(long) -1);
				this.fireTableDataChanged();
			}

			
		}

		// CHECK IF LIST UPDATED
		if (message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE) {
			Transaction trans = (Transaction) message.getValue();
			if (trans.getType() != Transaction.SIGN_NOTE_TRANSACTION)
				return;
			for (Transaction tr : transactions) {
				if (tr.viewSignature().equals(trans.viewSignature())) {
					transactions.remove(tr);
				}

			}
			transactions.add((R_SignNote) trans);

			this.fireTableDataChanged();
		}
	}

	private List<R_SignNote> read_Statement(String str, Long key) {
		List<R_SignNote> tran;
		ArrayList<Transaction> db_transactions;
		db_transactions = new ArrayList<Transaction>();
		tran = new ArrayList<R_SignNote>();
		// база данных
		DBSet dbSet = DBSet.getInstance();
		// читаем все блоки
		SortableList<byte[], Block> lists = dbSet.getBlockMap().getList();
		// проходим по блокам
		for (Pair<byte[], Block> list : lists) {

			// читаем транзакции из блока
			db_transactions = (ArrayList<Transaction>) list.getB().getTransactions();
			// проходим по транзакциям
			for (Transaction transaction : db_transactions) {
				// если ноте то пишем в transactions
				
				if (transaction.getType() == Transaction.SIGN_NOTE_TRANSACTION){
					R_SignNote statement = (R_SignNote) transaction;
					// filter Title
					statement = (R_SignNote) transaction;
					if (str != null && !str.equals("")){
						if (filter_str(str, statement ))	tran.add(statement);
					}
					if (key >0){
						if(statement.getKey() == key) tran.add(statement);
						
					}
				}
			}

		}
		
		// filter key
		if (key >0){
			
		}
		
		return tran;

	}

	public void removeObservers() {

		// Controller.getInstance().deleteObserver(this);
		DBSet.getInstance().getTransactionFinalMap().deleteObserver(this);
	}

	public void addObservers() {
		// Controller.getInstance().addObserver(this);
		DBSet.getInstance().getTransactionFinalMap().addObserver(this);
	}
	
	public void Find_item_from_key(String text) {
		// TODO Auto-generated method stub
		if (text.equals("") || text == null) return;
		if (!text.matches("[0-9]*"))return;
		if (new Long(text) < 1) return;
		transactions = read_Statement("", new Long (text));
		fireTableDataChanged();
		
		
	}
	public void clear(){
		transactions = new ArrayList<R_SignNote>();
		fireTableDataChanged();
		
		
	}
	public void set_Filter_By_Name(String str){
		transactions = read_Statement(str, (long) -1);
		fireTableDataChanged();
		
		
	}
	private boolean filter_str(String filter, R_SignNote record){
		if (record.getData() == null)
			return false;
		
		if(record.getVersion() == 2){
			Tuple3<String, String, JSONObject> a;
			try {
				a = record.parse_Data_V2_Without_Files();
			
				if (a.b.contains(filter)) return true;
				return false;
					} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
					
		
		String str = "";
		try {
			JSONObject data = (JSONObject) JSONValue
					.parseWithException(new String(record.getData(), Charset.forName("UTF-8")));
			str = (String) data.get("!!&_Title");
			if (str == null)
				str = (String) data.get("Title");
		} catch (Exception e) {
			// TODO Auto-generated catch block

			str = new String(record.getData(), Charset.forName("UTF-8"));
		}
		if (str == null)
			return false;
		if (str.contains(filter)) return true;
		return false;// t
		
		
		
	}

}
