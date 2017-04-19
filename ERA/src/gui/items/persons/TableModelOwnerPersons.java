package gui.items.persons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;
import javax.validation.constraints.Null;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import utils.ObserverMessage;
import utils.Pair;
import database.DBSet;
import database.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelOwnerPersons<U, T> extends AbstractTableModel implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_BORN = 2;
	public static final int COLUMN_ADDRESS = 3;
	public static final int COLUMN_FAVORITE = 4;

//	private SortableList<Long, PersonCls> persons;
	private  List<Pair<U,PersonCls>> persons;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Birthday"});//, "Publisher", "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false};
	private SortableList<Tuple2<String, String>, PersonCls> persons_S_List;
	
	
	private TreeMap<String, Stack<Tuple3<Integer, Integer, Integer>>> addresses;
	
	public TableModelOwnerPersons(Long key)
	{
		addresses = DBSet.getInstance().getPersonAddressMap().getItems(key);
		Controller.getInstance().addObserver(this);
		
		//PersonCls ss = DBSet.getInstance().getItemPersonMap().get_Indexes("v");
		//String sss = ss!=null?ss.getName():"--";	
	}
	
//	@Override
	//public SortableList<Long, PersonCls> getSortableList() 
//	{
//		return this.persons;
//	}
	
	
	public  List<Pair<U, PersonCls>> getSortableList() {
		return this.persons;
	}
	
	
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
	
	public PersonCls getPerson(int row)
	{
		return this.persons.get(row).getB();
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
		return this.persons.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.persons == null || row > this.persons.size() - 1 )
		{
			return null;
		}
		
		PersonCls person = this.persons.get(row).getB();
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return person.getKey();
		
		case COLUMN_NAME:
			
			return person.getName();
		
	//	case COLUMN_ADDRESS:
			
	//		return person.getOwner().getPersonAsString();
			
		
		
		case COLUMN_BORN:
			
		//	DateFormat f = new DateFormat("DD-MM-YYYY");
			//SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
			//return  dateFormat.format( new Date(person.getBirthday()));
			return person.getBirthdayStr();

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
			if(this.persons == null)
			{
				persons = new ArrayList<Pair<U, PersonCls>>();
				this.persons_S_List = (SortableList<Tuple2<String, String>, PersonCls>) message.getValue();
				//	this.persons_S_List.addFilterField("name");
				this.persons_S_List.registerObserver();
				get_List();
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_PERSON_TYPE || message.getType() == ObserverMessage.REMOVE_PERSON_TYPE)
		{
			get_List();
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.persons_S_List.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
	
	@SuppressWarnings("unchecked")
	private void get_List(){
		persons.clear();
		
		for(Pair<Tuple2<String, String>, PersonCls> pp1:persons_S_List){
			String creator = pp1.getB().getOwner().getAddress();
			for (String ad:addresses.keySet()){
				if (ad.equals(creator)) persons.add((Pair<U, PersonCls>) pp1);
				
			}
			
		}
		
	}
	
}
