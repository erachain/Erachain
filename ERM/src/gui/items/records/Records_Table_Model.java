package gui.items.records;

import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.block.Block;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import utils.ObserverMessage;
import database.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class Records_Table_Model extends TableModelCls<byte[], Transaction> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_FAVORITE = 3;

//	private SortableList<Long, PersonCls> persons;
//	private SortableList<Tuple2<String, String>, PersonCls> transactions;
	private SortableList<byte[], Transaction> transactions;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Creator", "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false};
	
	public Records_Table_Model()
	{
		Controller.getInstance().addObserver(this);
	}
	
//	@Override
	//public SortableList<Long, PersonCls> getSortableList() 
//	{
//		return this.persons;
//	}
	
	
	@Override
	public SortableList<byte[], Transaction> getSortableList() {
		return this.transactions;
	}
	
	
//	public Class<? extends Object> getColumnClass(int c) {     // set column type
//		Object o = getValueAt(0, c);
//		return o==null?null:o.getClass();
//     }
	
	// читаем колонки которые изменяем высоту	   
		public Boolean[] get_Column_AutoHeight(){
			
			return this.column_AutuHeight;
		}
	// устанавливаем колонки которым изменить высоту	
		public void set_get_Column_AutoHeight( Boolean[] arg0){
			this.column_AutuHeight = arg0;	
		}
	
	public Transaction getTrabsaction(int row)
	{
		return this.transactions.get(row).getB();
	}
	
	@Override
	public int getColumnCount() 
	{
		return this.columnNames.length;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		return this.transactions.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.transactions == null || row > this.transactions.size() - 1 )
		{
			return null;
		}
		
		 Transaction transaction = this.transactions.get(row).getB(); 
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return transaction.getKey();
		
		case COLUMN_NAME:
			
			return transaction.getSeqNo(null);
		
		case COLUMN_ADDRESS:
			
			return transaction.getCreator().asPerson();
			
		case COLUMN_FAVORITE:
			
			return transaction.isReferenced();

		}
		
		return null;
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
		if(message.getType() == ObserverMessage.LIST_PERSON_TYPE)
		{			
			if(this.transactions == null)
			{
			
				this.transactions = (SortableList<byte[], Transaction>) message.getValue();
				this.transactions.addFilterField("name");
				this.transactions.registerObserver();
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_PERSON_TYPE || message.getType() == ObserverMessage.REMOVE_PERSON_TYPE || message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
		{
			this.transactions = (SortableList<byte[], Transaction>) message.getValue();
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.transactions.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
