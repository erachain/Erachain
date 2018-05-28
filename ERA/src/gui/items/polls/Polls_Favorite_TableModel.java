package gui.items.polls;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import javax.validation.constraints.Null;
import org.mapdb.Fun.Tuple2;
import utils.ObserverMessage;
import controller.Controller;
import core.item.persons.PersonCls;
import core.item.polls.PollCls;
import core.item.templates.TemplateCls;
import datachain.DCSet;
import datachain.SortableList;
import gui.models.TableModelCls;
import lang.Lang;

@SuppressWarnings("serial")
public class Polls_Favorite_TableModel extends TableModelCls<Tuple2<String, String>, PersonCls> implements Observer
{
	public static final int COLUMN_KEY = 0;
	public static final int COLUMN_NAME = 1;
	public static final int COLUMN_ADDRESS = 2;
	public static final int COLUMN_CONFIRMED = 3;
	public static final int COLUMN_FAVORITE = 4;
	
	private List<PollCls> templates;
	
	
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Key", "Name", "Publisher", "Confirmed", "Favorite"});
	private Boolean[] column_AutuHeight = new Boolean[]{false,true,true,false,false};
	
	public Polls_Favorite_TableModel()
	{
		super.COLUMN_FAVORITE = COLUMN_FAVORITE;
		addObservers();
	}
	
	@Override
	public SortableList<Tuple2<String, String>, PersonCls> getSortableList() {
		return null;
	}
	// читаем колонки которые изменяем высоту	   
		public Boolean[] get_Column_AutoHeight(){
			
			return this.column_AutuHeight;
		}
	// устанавливаем колонки которым изменить высоту	
		public void set_get_Column_AutoHeight( Boolean[] arg0){
			this.column_AutuHeight = arg0;	
		}
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
    }
	
	public PollCls getItem(int row)
	{
		return this.templates.get(row);
		
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
		if (templates == null) return 0;
		return this.templates.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.templates == null || row > this.templates.size() - 1 )
		{
			return null;
		}
		
		PollCls status = this.templates.get(row);
		if (status == null)
			return null;
		
		
		
		switch(column)
		{
		case COLUMN_KEY:
			
			return status.getKey(DCSet.getInstance());
		
		case COLUMN_NAME:
			
			return status.viewName();
		
		case COLUMN_ADDRESS:
			
			return status.getOwner().getPersonAsString();
						
		case COLUMN_CONFIRMED:
			
			return status.isConfirmed();
			
		case COLUMN_FAVORITE:
			
			return status.isFavorite();
			
		}
		
		return null;
	}

	@Override
	public void update(Observable o, Object arg) 
	{	
	//	try
	//	{
			this.syncUpdate(o, arg);
	//	}
	//	catch(Exception e)
	//	{
			//GUI ERROR
	//	}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.WALLET_LIST_POLL_FAVORITES_TYPE && templates==null)
		{
			templates = new ArrayList<PollCls>();
			fill((Set<Long>) message.getValue());
			fireTableDataChanged();
			}
		if(message.getType() == ObserverMessage.WALLET_ADD_POLL_FAVORITES_TYPE){
			templates.add(Controller.getInstance().getPoll((long) message.getValue()));
			fireTableDataChanged();
			}
		if(message.getType() == ObserverMessage.WALLET_DELETE_POLL_FAVORITE_TYPE){
			templates.remove(Controller.getInstance().getPoll((long) message.getValue()));
			fireTableDataChanged();
			}
	}
		
	public void fill(Set<Long> set){
		for(Long s:set){
				templates.add ( Controller.getInstance().getPoll(s));
		}
	}
	
	public void removeObservers() 
	{
		Controller.getInstance().wallet.database.getPollFavoritesSet().deleteObserver(this);
	}
	public void addObservers(){
		
		Controller.getInstance().wallet.database.getPollFavoritesSet().addObserver(this);
	}

	
}
