package gui.models;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;
////////
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import core.item.imprints.ImprintCls;
import database.DBSet;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public  class PersonAccountsModel extends  AbstractTableModel implements Observer
{
	public static final int COLUMN_TO_DATE = 1;
	//public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 0;
//	public static final int COLUMN_CONFIRMED = 3;
	
	
	TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> addresses; //= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Address","To Date"}); //, "Data"});
	SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	
	public PersonAccountsModel(long person_Key)
	{
		Controller.getInstance().addWalletListener(this);
		addresses = DBSet.getInstance().getPersonAddressMap().getItems(person_Key);
	}

	
	//@Override
	//public SortableList<Tuple2<String, String>, ImprintCls> getSortableList() {
	//	return this.imprints;
	//}
	

// set class
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		       return getValueAt(0, c).getClass();
		   }
		   

	/*
	public ImprintCls getItem(int row)
	{
		return this.address.get(row).getB();
	}
	*/
	
	
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
		
		TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> a = addresses;
		return  addresses.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if( addresses == null || row >  addresses.size() - 1 )
		{
			return null;
		}
		
		//Map.Entry<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> entry  =  records.entrySet();
		String addrses_key_value = "-";
		int i = 0;
		for (String addrses_key: addresses.keySet()) {
			if (i == row)
			{
				addrses_key_value = addrses_key;
				break;
			}
		}
		Stack<Tuple3<Integer, Integer, Integer>> entry = addresses.get(addrses_key_value);
		if (entry == null || entry.isEmpty() ) return "-";
		
		Tuple3<Integer, Integer, Integer> value = entry.peek();
		
		switch(column)
		{
		
		case COLUMN_ADDRESS:
			
			return  addrses_key_value;
									
		case COLUMN_TO_DATE:
			
			return  formatDate.format( new Date(value.a)).toString();
			
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
		/*
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.l.LIST_IMPRINT_TYPE)
		{
			if(this.imprints == null)
			{
				this.imprints = (SortableList<Tuple2<String, String>, ImprintCls>) message.getValue();
				this.imprints.registerObserver();
				//this.imprints.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		*/
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE || message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}
	
/*
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
	*/
}
