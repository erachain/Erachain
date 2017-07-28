package gui.items.persons;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.validation.constraints.Null;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.item.ItemCls;
import core.item.persons.PersonCls;
import utils.ObserverMessage;
import database.DBSet;
import database.ItemPersonMap;
import database.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class TableModelPersons extends TableModelCls<Tuple2<String, String>, PersonCls> implements Observer {
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_BORN = 2;
	public static final int COLUMN_PUBLISHER = 3;
	public static final int COLUMN_FAVORITE = 4;

	// private SortableList<Long, PersonCls> persons;
	private SortableList<Tuple2<String, String>, PersonCls> persons;

	private String[] columnNames = Lang.getInstance()
			.translate(new String[] { "Key", "Name", "Birthday", "Publisher", "Favorite" });// ,
																							// "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[] { false, true, true, false };
	private ItemPersonMap db;
	private List<ItemCls> list;
	private String filter_Name = "";
	private long key_filter =0;

	public TableModelPersons() {
		db = DBSet.getInstance().getItemPersonMap();
		// addObservers() ;
		// PersonCls ss =
		// DBSet.getInstance().getItemPersonMap().get_Indexes("v");
		// String sss = ss!=null?ss.getName():"--";
	}

	// @Override
	// public SortableList<Long, PersonCls> getSortableList()
	// {
	// return this.persons;
	// }
	public void set_Filter_By_Name(String str) {
		filter_Name = str;
		list = db.get_By_Name(filter_Name, false);
		this.fireTableDataChanged();

	}
	public void clear(){
		list =new ArrayList<ItemCls>();
		this.fireTableDataChanged();
		
	}

	@Override
	public SortableList<Tuple2<String, String>, PersonCls> getSortableList() {
		return this.persons;
	}

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

	public PersonCls getPerson(int row) {
		return (PersonCls) this.list.get(row);
	}

	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public String getColumnName(int index) {
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() {
		if (this.list == null)
			return 0;
		;
		return this.list.size();

	}

	@Override
	public Object getValueAt(int row, int column) {
		if (list == null || row > this.list.size() - 1) {
			return null;
		}

		PersonCls person = (PersonCls) list.get(row);

		switch (column) {
		case COLUMN_KEY:

			return person.getKey();

		case COLUMN_NAME:

			return person.getName();

		case COLUMN_PUBLISHER:

			return person.getOwner().getPersonAsString();

		case COLUMN_FAVORITE:

			return person.isFavorite();

		case COLUMN_BORN:

			// DateFormat f = new DateFormat("DD-MM-YYYY");
			// SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
			// return dateFormat.format( new Date(person.getBirthday()));
			return person.getBirthdayStr();

		}

		return null;
	}

	@Override
	public void update(Observable o, Object arg) {
		try {
			this.syncUpdate(o, arg);
		} catch (Exception e) {
			// GUI ERROR
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg) {
		ObserverMessage message = (ObserverMessage) arg;
		if (key_filter >0){
			
		return;	
		}
		
		// CHECK IF NEW LIST
		if (message.getType() == ObserverMessage.LIST_PERSON_TYPE) {
			if (this.list == null && !filter_Name.equals("")) {
				list = db.get_By_Name(filter_Name, false);
				this.fireTableDataChanged();
				// this.persons = (SortableList<Tuple2<String, String>,
				// PersonCls>) message.getValue();
				// this.persons.addFilterField("name");
				// this.persons.registerObserver();
			}

		}

		// CHECK IF LIST UPDATED
		if (message.getType() == ObserverMessage.ADD_PERSON_TYPE) {
			PersonCls ppp = (PersonCls) message.getValue();
			if (ppp.getName().contains(filter_Name))
				list.add(ppp);
			// list = db.getPerson_By_Name(filter_Name);

			this.fireTableDataChanged();
		}

		// CHECK IF LIST UPDATED
		if (message.getType() == ObserverMessage.REMOVE_PERSON_TYPE) {
			PersonCls ppp = (PersonCls) message.getValue();
			if (ppp.getName().contains(filter_Name))
				list.remove(ppp);
			// list = db.getPerson_By_Name(filter_Name);

			this.fireTableDataChanged();
		}
	}

	public void addObservers() {

		// Controller.getInstance()..addObserver(this);
		DBSet.getInstance().getItemPersonMap().addObserver(this);
	}

	public void removeObservers() {

		// Controller.getInstance().deleteObserver(this);
		DBSet.getInstance().getItemPersonMap().deleteObserver(this);
	}

	public void Find_item_from_key(String text) {
		// TODO Auto-generated method stub
		if (text.equals("") || text == null) return;
		if (!text.matches("[0-9]*"))return;
			key_filter = new Long(text);
			list =new ArrayList<ItemCls>();
			PersonCls pers = Controller.getInstance().getPerson(key_filter);
			if ( pers == null) return;
			list.add(pers);
			this.fireTableDataChanged();
		
		
	}
}
