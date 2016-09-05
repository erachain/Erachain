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
import org.mapdb.Fun.Tuple5;

import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import core.item.imprints.ImprintCls;
import database.DBSet;
import database.ItemStatusMap;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public  class PersonStatusesModel extends  AbstractTableModel implements Observer
{
	public static final int COLUMN_TO_DATE = 1;
	//public static final int COLUMN_NAME = 1;
	public static final int COLUMN_STATUS = 0;
//	public static final int COLUMN_CONFIRMED = 3;
	
	TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> statuses;
	
	SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	//TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> addresses; //= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Status","To Date"}); //, "Data"});
	private Boolean[] column_AutuHeight = new Boolean[]{true,false};
	String from_date_str;
	String to_date_str;
	Long dte;
	ItemStatusMap statusesMap;
	long status_this;
	
	public PersonStatusesModel(long person_Key)
	{
		status_this = person_Key;
		Controller.getInstance().addWalletListener(this);
	//	addresses = DBSet.getInstance().getPersonAddressMap().getItems(person_Key);
		statuses= DBSet.getInstance().getPersonStatusMap().get(status_this);
		statusesMap = DBSet.getInstance().getItemStatusMap();
	}

	
	public TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> getSortableList() {
		return statuses;
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
		
		 TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> a = statuses;
		return  statuses.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if( statuses == null || row >  statuses.size() - 1 )
		{
			return null;
		}
		
		//Map.Entry<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> entry  =  records.entrySet();
		Long status_key_value = 0l;
		int i = 0;
		for ( Long status_key: statuses.keySet()) {
			if (i++ == row)
			{
				status_key_value = status_key;
				break;
			}
		}
		 Stack<Tuple5<Long, Long, byte[], Integer, Integer>> entry = statuses.get(status_key_value);
		if (entry == null || entry.isEmpty() ) return 0;
		
		 Tuple5<Long, Long, byte[], Integer, Integer> value = entry.peek();
	//	 if (value == null ) return 0;
		
		 switch(column)
		{
		
		case COLUMN_STATUS:
			
			return statusesMap.get(status_key_value).toString(DBSet.getInstance(), value.c);//addrses_key_value;
									
		case COLUMN_TO_DATE:
			
			
			dte = value.a;
			if (dte == null || dte == Long.MIN_VALUE) from_date_str = " ? ";
			else from_date_str = formatDate.format( new Date(dte));
			
			dte = value.b;
			if (dte == null || dte == Long.MAX_VALUE) to_date_str = " ? ";
			else to_date_str = formatDate.format( new Date(dte));
			
			return from_date_str + " - " + to_date_str;
			
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
		if(message.getType() == ObserverMessage.LIST_STATUS_TYPE)
		{
			if(this.statuses == null)
			{
				this.statuses = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>) message.getValue();
			//	this.statusesMap .registerObserver();
				//this.imprints.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_STATUS_TYPE
				|| message.getType() == ObserverMessage.REMOVE_STATUS_TYPE
				|| message.getType() == ObserverMessage.ADD_PERSON_STATUS_TYPE
				|| message.getType() == ObserverMessage.REMOVE_PERSON_STATUS_TYPE
				|| message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
		{
			//this.statuses = (TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>>) message.getValue();
			statuses= DBSet.getInstance().getPersonStatusMap().get(status_this);
			this.fireTableDataChanged();
		}	
	}
	

//	@Override
//	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
//	}
}
