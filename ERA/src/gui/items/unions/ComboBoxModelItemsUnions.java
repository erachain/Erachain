package gui.items.unions;

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
import core.item.unions.UnionCls;
import datachain.DCSet;

@SuppressWarnings("serial")
public class ComboBoxModelItemsUnions extends DefaultComboBoxModel<UnionCls> implements Observer {
	Lock lock = new ReentrantLock();
	
	public ComboBoxModelItemsUnions()
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
		if(message.getType() == ObserverMessage.LIST_UNION_FAVORITES_TYPE)
		{
			//GET SELECTED ITEM
			UnionCls selected = (UnionCls) this.getSelectedItem();
						
			//EMPTY LIST
			this.removeAllElements();
				
			//INSERT ALL ACCOUNTS
			Set<Long> keys = (Set<Long>) message.getValue();
			List<UnionCls> unions = new ArrayList<UnionCls>();
			for(Long key: keys)
			{				
				//GET UNION
				UnionCls union = Controller.getInstance().getItemUnion(key);
				unions.add(union);
				
				//ADD
				this.addElement(union);
			}
				
			//RESET SELECTED ITEM
			if(this.getIndexOf(selected) != -1)
			{
				for(UnionCls union: unions)
				{
					if(union.getKey() == selected.getKey(DCSet.getInstance()))
					{
						this.setSelectedItem(union);
						return;
					}
				}
			}
		}
	}
}
