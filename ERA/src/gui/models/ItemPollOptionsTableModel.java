package gui.models;

import java.math.BigDecimal;

import javax.swing.table.AbstractTableModel;

import core.item.assets.AssetCls;
import core.item.polls.PollCls;
import datachain.DCSet;
import lang.Lang;
import utils.NumberAsString;

@SuppressWarnings("serial")
public class ItemPollOptionsTableModel extends AbstractTableModel
{
	private static final int COLUMN_NAME = 0;
	public static final int COLUMN_VOTES = 1;
	public static final int COLUMN_PERCENTAGE = 2;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Name", "Votes", "% of Total"});
	private PollCls poll;
	private AssetCls asset;
	
	public ItemPollOptionsTableModel(PollCls poll, AssetCls asset)
	{
		this.poll = poll;
		this.asset = asset;
	}
	
	public String getPollOption(int row)
	{
		return this.poll.getOptions().get(row);
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
		 return this.poll.getOptions().size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.poll.getOptions() == null || row > this.poll.getOptions().size() - 1 )
		{
			return null;
		}
				
		switch(column)
		{
		case COLUMN_NAME:
			
			return this.poll.viewOption(row);
		
		case COLUMN_VOTES:
			
			//return NumberAsString.formatAsString(poll.getVotes(this.asset.getKey(DCSet.getInstance())));
			return NumberAsString.formatAsString(poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance())));
			
		case COLUMN_PERCENTAGE:
			
			BigDecimal total = this.poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance()));
			BigDecimal votes = this.poll.getTotalVotes(DCSet.getInstance(), this.asset.getKey(DCSet.getInstance()), row);
			
			if(votes.compareTo(BigDecimal.ZERO) == 0)
			{
				return "0 %";
			}
			
			return votes.divide(total, BigDecimal.ROUND_UP).multiply(BigDecimal.valueOf(100)).toPlainString() + " %";
			
		}
		
		return null;
	}
	
	public void setAsset(AssetCls asset)
	{
		this.asset = asset;
		this.fireTableDataChanged();
	}
}
