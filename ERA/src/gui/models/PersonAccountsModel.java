package gui.models;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashSet;
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
import core.account.Account;
import core.item.imprints.ImprintCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public  class PersonAccountsModel extends  AbstractTableModel implements Observer
{
	public static final int COLUMN_TO_DATE = 1;
	public static final int COLUMN_CREATOR = 2;
	public static final int COLUMN_ADDRESS = 0;
	public static final int COLUMN_CREATOR_NAME = 30;
//	public static final int COLUMN_CONFIRMED = 3;
	private static final HashSet<Account> Account = null;
	
	long key_person_table;
	TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> addresses; //= DBSet.getInstance().getPersonAddressMap().getItems(person.getKey());
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Account","To Date", "Creator"}); //, "Data"});
	private Boolean[] column_AutuHeight = new Boolean[]{true,true};
	SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
	
	public PersonAccountsModel(long person_Key)
	{
		addObservers();
		key_person_table = person_Key;
		addresses = DBSet.getInstance().getPersonAddressMap().getItems(key_person_table);
	}

	public String get_No_Trancaction(int row){
		
		
		//Map.Entry<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> entry  =  records.entrySet();
				String addrses_key_value = "-";
				int i = 0;
				for (String addrses_key: addresses.keySet()) {
					if (i == row)
					{
						addrses_key_value = addrses_key;
						break;
					}
					i++;
				}
		
		Stack<Tuple3<Integer, Integer, Integer>> entry = addresses.get(addrses_key_value);
		if (entry == null || entry.isEmpty() ) return "-";
		
		Tuple3<Integer, Integer, Integer> value = entry.peek();
		//int height = value.b;
		//int seq = value.c;
		return value.b +"-"+  value.c;
		
	}
	
	//@Override
	//public SortableList<Tuple2<String, String>, ImprintCls> getSortableList() {
	//	return this.imprints;
	//}
	

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
			i++;
		}
		Stack<Tuple3<Integer, Integer, Integer>> entry = addresses.get(addrses_key_value);
		if (entry == null || entry.isEmpty() ) return "-";
		
		Tuple3<Integer, Integer, Integer> value = entry.peek();
		int height = value.b;
		int seq = value.c;
		Transaction trans = DBSet.getInstance().getTransactionFinalMap().getTransaction(height, seq);
		switch(column)
		{
		
		case COLUMN_ADDRESS:
			
			return  addrses_key_value;
									
		case COLUMN_TO_DATE:
			
			//return  formatDate.format( new Date(value.a)).toString();
			return  utils.DateTimeFormat.timestamptoString(value.a);
		
		case COLUMN_CREATOR:
			
			
			
			if (trans == null)
				return null;
			
			return trans.getCreator().getPersonAsString();
			
		case 3:
			if (trans == null)
				return null;
			
			if (trans.getCreator().getPerson()== null) return null;
			return trans.getCreator().getPerson().b;
		
		case COLUMN_CREATOR_NAME:
			if (trans == null)
				return null;
			
			if (trans.getCreator().getPerson()== null) return null;
			return trans.getCreator().getPerson().b.getName();
		
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
		if( message.getType() == ObserverMessage.ADD_ALL_ACCOUNT_TYPE || message.getType() == ObserverMessage.REMOVE_ALL_ACCOUNT_TYPE || message.getType() == ObserverMessage.LIST_ALL_ACCOUNT_TYPE)
		{
			addresses = DBSet.getInstance().getPersonAddressMap().getItems(key_person_table);
			this.fireTableDataChanged();
		}	
	}


	public String getAccount_String(int row) {
		// TODO Auto-generated method stub
	
		return  (String) getValueAt( row, COLUMN_ADDRESS) ;
		
		
	}
	
	public Account getAccount(int row){
	
		//return  (String) getValueAt( row, COLUMN_ADDRESS) ;
				Stack<Tuple3<Integer, Integer, Integer>> entry = addresses.get((String) getValueAt( row, COLUMN_ADDRESS));
				if (entry == null || entry.isEmpty() ) return null;
				
				Tuple3<Integer, Integer, Integer> value = entry.peek();
				int height = value.b;
				int seq = value.c;
				Transaction trans = DBSet.getInstance().getTransactionFinalMap().getTransaction(height, seq);
				if (trans == null)
					return null;
				HashSet<core.account.Account> accounts = trans.getRecipientAccounts();
				
				for ( Account acc:accounts){
					
					String a = acc.getAddress();
					String b = getValueAt( row, COLUMN_ADDRESS).toString();
					
					
					if (acc.getAddress().equals( getValueAt( row, COLUMN_ADDRESS).toString()) ){
						
						return  acc;
						
					}
					
				}
				
				return  null;
				
		
		
	}
	
	
	public String get_Creator_Account(int row) {
		// TODO Auto-generated method stub
	
		//return  (String) getValueAt( row, COLUMN_ADDRESS) ;
		Stack<Tuple3<Integer, Integer, Integer>> entry = addresses.get((String) getValueAt( row, COLUMN_ADDRESS));
		if (entry == null || entry.isEmpty() ) return "-";
		
		Tuple3<Integer, Integer, Integer> value = entry.peek();
		int height = value.b;
		int seq = value.c;
		Transaction trans = DBSet.getInstance().getTransactionFinalMap().getTransaction(height, seq);
		if (trans == null)
			return null;
		
		return trans.getCreator().getAddress().toString();
		
	}
	
public void addObservers(){
		
	Controller.getInstance().addWalletListener(this);
		
	}
	
	
	public void removeObservers() 
	{
		
		Controller.getInstance().deleteObserver(this);
	}
}
