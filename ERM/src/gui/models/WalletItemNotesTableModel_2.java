package gui.models;

import java.util.Observable;
//import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import qora.item.ItemCls;
import utils.ObserverMessage;
//import controller.Controller;
import database.SortableList;
//import lang.Lang;

@SuppressWarnings("serial")
public class WalletItemNotesTableModel_2 extends WalletItem_TableModel
{
	
	public WalletItemNotesTableModel_2()
	{
		super( ObserverMessage.ADD_NOTE_TYPE,  ObserverMessage.REMOVE_NOTE_TYPE,  ObserverMessage.LIST_NOTE_TYPE);
	}

	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF NEW LIST
		if(message.getType() == this.observer_list)
		{
			if(this.items == null)
			{
				this.items = (SortableList<Tuple2<String, String>, ItemCls>) message.getValue();
				this.items.registerObserver();
				//this.items.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == this.observer_add || message.getType() == this.observer_remove)
		{
			this.fireTableDataChanged();
		}	
	}

}
