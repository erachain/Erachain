package gui.items.accounts;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import database.DBMap;
import database.SortableList;
import utils.ObserverMessage;
import utils.Pair;

public class SortableList_Account_Transaction<T, U> extends SortableList<T, U> implements Observer {

	public SortableList_Account_Transaction(DBMap<T, U> db) {
		super(db);
		
		
		
		
		// TODO Auto-generated constructor stub
	}

	private DBMap<T, U> db;
	private int index;
	private boolean descending;
	private int position;
	private Iterator<T> iterator;
	private Pattern pattern;
	private int size;
	private Pair<T, U> lastValue;
	private Collection<T> keys;
	private List<String> additionalFilterFields;

	static Logger LOGGER = Logger.getLogger(SortableList.class.getName());

	
	
}
