package gui.items.statuses;

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
import core.item.statuses.StatusCls;

@SuppressWarnings("serial")
public class ComboBoxModelItemsStatuses extends DefaultComboBoxModel<StatusCls> implements Observer {
	Lock lock = new ReentrantLock();
	
	public ComboBoxModelItemsStatuses()
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
		if(message.getType() == ObserverMessage.LIST_STATUS_FAVORITES_TYPE)
		{
			//GET SELECTED ITEM
			StatusCls selected = (StatusCls) this.getSelectedItem();
						
			//EMPTY LIST
			this.removeAllElements();
				
			//INSERT ALL ACCOUNTS
			Set<Long> keys = (Set<Long>) message.getValue();
			List<StatusCls> statuses = new ArrayList<StatusCls>();
			for(Long key: keys)
			{				
				//GET STATUS
				StatusCls status = Controller.getInstance().getItemStatus(key);
				statuses.add(status);
				
				//ADD
				this.addElement(status);
			}
				
			//RESET SELECTED ITEM
			if(this.getIndexOf(selected) != -1)
			{
				for(StatusCls status: statuses)
				{
					if(status.getKey() == selected.getKey())
					{
						this.setSelectedItem(status);
						return;
					}
				}
			}
		}
	}
}
