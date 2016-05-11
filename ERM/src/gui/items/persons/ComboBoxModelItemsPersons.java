package gui.items.persons;

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
import core.item.persons.PersonCls;

@SuppressWarnings("serial")
public class ComboBoxModelItemsPersons extends DefaultComboBoxModel<PersonCls> implements Observer {
	Lock lock = new ReentrantLock();
	
	public ComboBoxModelItemsPersons()
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
		if(message.getType() == ObserverMessage.LIST_PERSON_FAVORITES_TYPE)
		{
			//GET SELECTED ITEM
			PersonCls selected = (PersonCls) this.getSelectedItem();
						
			//EMPTY LIST
			this.removeAllElements();
				
			//INSERT ALL ACCOUNTS
			Set<Long> keys = (Set<Long>) message.getValue();
			List<PersonCls> persons = new ArrayList<PersonCls>();
			for(Long key: keys)
			{				
				//GET PERSON
				PersonCls person = Controller.getInstance().getItemPerson(key);
				persons.add(person);
				
				//ADD
				this.addElement(person);
			}
				
			//RESET SELECTED ITEM
			if(this.getIndexOf(selected) != -1)
			{
				for(PersonCls person: persons)
				{
					if(person.getKey() == selected.getKey())
					{
						this.setSelectedItem(person);
						return;
					}
				}
			}
		}
	}
}
