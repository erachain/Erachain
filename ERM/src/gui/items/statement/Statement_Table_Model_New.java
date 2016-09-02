package gui.items.statement;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import core.item.assets.AssetCls;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;
import lang.Lang;
import utils.NumberAsString;

public class Statement_Table_Model_New extends AbstractTableModel {

	List<Transaction> transactions;

	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_AMOUNT = 2;
	public static final int COLUMN_FEE = 3;
	
//	private SortableList<byte[], Transaction> transactions;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Timestamp", "Type", "Amount", AssetCls.FEE_NAME});
	
	
	public Statement_Table_Model_New(){
		transactions = new ArrayList<Transaction>();
		transactions.addAll(DBSet.getInstance().getTransactionMap().getTransactions());
		int a = 1;
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
	//		LOGGER.error(e.getMessage(),e);
			return null;
		}
	}

}
