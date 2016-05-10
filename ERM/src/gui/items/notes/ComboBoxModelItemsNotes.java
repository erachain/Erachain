package gui.items.notes;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.DefaultComboBoxModel;

import utils.ObserverMessage;
import controller.Controller;
import core.item.notes.NoteCls;

@SuppressWarnings("serial")
public class ComboBoxModelItemsNotes extends DefaultComboBoxModel<NoteCls> implements Observer {
	Lock lock = new ReentrantLock();
	
	public ComboBoxModelItemsNotes()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public void update(Observable o, Object arg) 
	{
		try
		{
			if (lock.tryLock()) {
				try {
					this.syncUpdate(o, arg);
				}
				finally {
					lock.unlock();
				}
			}
			
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
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.LIST_NOTE_FAVORITES_TYPE)
		{
			//GET SELECTED ITEM
			NoteCls selected = (NoteCls) this.getSelectedItem();
						
			//EMPTY LIST
			this.removeAllElements();
				
			//INSERT ALL ACCOUNTS
			Set<Long> keys = (Set<Long>) message.getValue();
			List<NoteCls> notes = new ArrayList<NoteCls>();
			for(Long key: keys)
			{				
				//GET NOTE
				NoteCls note = Controller.getInstance().getItemNote(key);
				notes.add(note);
				
				//ADD
				this.addElement(note);
			}
				
			//RESET SELECTED ITEM
			if(this.getIndexOf(selected) != -1)
			{
				for(NoteCls note: notes)
				{
					if(note.getKey() == selected.getKey())
					{
						this.setSelectedItem(note);
						return;
					}
				}
			}
		}
	}
}
