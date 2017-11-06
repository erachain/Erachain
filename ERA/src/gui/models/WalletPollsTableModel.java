package gui.models;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import javax.validation.constraints.Null;

import org.mapdb.Fun.Tuple2;

import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import core.block.Block;
import core.voting.Poll;
import database.wallet.PollMap;
import datachain.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public class WalletPollsTableModel extends TableModelCls<Tuple2<String, String>, Poll> implements Observer
{
	public static final int COLUMN_NAME = 0;
	public static final int COLUMN_ADDRESS = 1;
	public static final int COLUMN_TOTAL_VOTES = 2;
	private static final int COLUMN_CONFIRMED = 3;
	
	private SortableList<Tuple2<String, String>, Poll> polls;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Name", "Creator", "Total Votes", "Confirmed"});
	
	public WalletPollsTableModel()
	{
		
		addObservers();
	}
	
	public Class<? extends Object> getColumnClass(int c) {     // set column type
		Object o = getValueAt(0, c);
		return o==null?Null.class:o.getClass();
    }
	
	@Override
	public SortableList<Tuple2<String, String>, Poll> getSortableList() {
		return polls;
	}
	
	public Poll getPoll(int row)
	{
		return polls.get(row).getB();
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
		 return this.polls.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.polls == null || row > this.polls.size() - 1 )
		{
			return null;
		}
		
		Pair<Tuple2<String, String>, Poll> data = this.polls.get(row);
		
		if (data == null || data.getB() == null) {
			return -1;
		}
		Poll poll = data.getB();
		
		switch(column)
		{
		case COLUMN_NAME:
			
			return poll.getName();
		
		case COLUMN_ADDRESS:
			
			return poll.getCreator().getPersonAsString();
			
		case COLUMN_TOTAL_VOTES:
			
			BigDecimal amo = poll.getTotalVotes();
			if (amo == null)
				return BigDecimal.ZERO;
			return amo;
		
			
		case COLUMN_CONFIRMED:
			
			return poll.isConfirmed();
			
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
		if(message.getType() == ObserverMessage.LIST_POLL_TYPE)
		{
			if(this.polls == null)
			{
				this.polls = (SortableList<Tuple2<String, String>, Poll>) message.getValue();
				this.polls.registerObserver();
				this.polls.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_POLL_TYPE || message.getType() == ObserverMessage.REMOVE_POLL_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}
	public void removeObservers(){
		
		Controller.getInstance().deleteObserver(this);
		
	}
	public void addObservers(){
		Controller.getInstance().addWalletListener(this);
	}
}
