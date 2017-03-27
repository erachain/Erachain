package gui.items.persons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.item.persons.PersonCls;
import utils.ObserverMessage;
import database.DBSet;
import database.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelPersons extends TableModelCls<Tuple2<String, String>, PersonCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_BORN = 2;
	public static final int COLUMN_PUBLISHER = 3;
	public static final int COLUMN_FAVORITE = 4;

//	private SortableList<Long, PersonCls> persons;
	private SortableList<Tuple2<String, String>, PersonCls> persons;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Birthday", "Publisher"});//, "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false};
	
	public TableModelPersons()
	{
		Controller.getInstance().addObserver(this);
		//PersonCls ss = DBSet.getInstance().getItemPersonMap().get_Indexes("v");
		//String sss = ss!=null?ss.getName():"--";	
	}
	
//	@Override
	//public SortableList<Long, PersonCls> getSortableList() 
//	{
//		return this.persons;
//	}
	
	
	@Override
	public SortableList<Tuple2<String, String>, PersonCls> getSortableList() {
		return this.persons;
	}
	
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?null:o.getClass();
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
		
		case COLUMN_PUBLISHER:
			
			return person.getOwner().getPersonAsString();
			
		case COLUMN_FAVORITE:
			
			return person.isFavorite();
		
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
			
				this.persons = (SortableList<Tuple2<String, String>, PersonCls>) message.getValue();
				this.persons.addFilterField("name");
				this.persons.registerObserver();
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_PERSON_TYPE || message.getType() == ObserverMessage.REMOVE_PERSON_TYPE || message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
		{
	//		this.persons = (SortableList<Tuple2<String, String>, PersonCls>) message.getValue();
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.persons.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
