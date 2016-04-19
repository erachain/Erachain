package gui.items.imprints;

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
import core.item.imprints.ImprintCls;

@SuppressWarnings("serial")
public class ComboBoxModelItemsImprints_NONE extends DefaultComboBoxModel<ImprintCls> implements Observer {
	Lock lock = new ReentrantLock();
	
	public ComboBoxModelItemsImprints_NONE()
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
		if(false) //message.getType() == ObserverMessage.LIST_IMPRINT_FAVORITES_TYPE)
		{
			//GET SELECTED ITEM
			ImprintCls selected = (ImprintCls) this.getSelectedItem();
						
			//EMPTY LIST
			this.removeAllElements();
				
			//INSERT ALL ACCOUNTS
			Set<Long> keys = (Set<Long>) message.getValue();
			List<ImprintCls> imprints = new ArrayList<ImprintCls>();
			for(Long key: keys)
			{				
				//GET IMPRINT
				ImprintCls imprint = Controller.getInstance().getImprint(key);
				imprints.add(imprint);
				
				//ADD
				this.addElement(imprint);
			}
				
			//RESET SELECTED ITEM
			if(this.getIndexOf(selected) != -1)
			{
				for(ImprintCls imprint: imprints)
				{
					if(imprint.getKey() == selected.getKey())
					{
						this.setSelectedItem(imprint);
						return;
					}
				}
			}
		}
	}
}
