package gui.items.polls;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.polls.PollCls;
import gui.Gui;
import gui.models.ItemPollsTableModel;
import gui.models.VotesTableModel;
import lang.Lang;
import utils.BigDecimalStringComparator;

public class PollTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private PollDetailPanel pollDetailPanel;
	private ItemPollsTableModel myVotesTableModel;	
	private ItemPollsTableModel allVotesTableModel;
	
	@SuppressWarnings("unchecked")
	public PollTabPane(PollCls poll, ItemCls asset)
	{
		super();
			
		//POLL DETAILS
		this.pollDetailPanel = new PollDetailPanel(poll, (AssetCls)asset);
		this.addTab(Lang.getInstance().translate("Poll Details"), this.pollDetailPanel);
		
		//ALL VOTES
		allVotesTableModel = new ItemPollsTableModel();
				//(Long)poll.getKey(DCSet.getInstance()), asset);
		final JTable allVotesTable = Gui.createSortableTable(allVotesTableModel, 0);
		
		TableRowSorter<VotesTableModel> sorter =  (TableRowSorter<VotesTableModel>) allVotesTable.getRowSorter();
		sorter.setComparator(VotesTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
		
		this.addTab(Lang.getInstance().translate("All Votes"), new JScrollPane(allVotesTable));
		
		//MY VOTES
		myVotesTableModel = new ItemPollsTableModel();
				//poll.getVotes(DCSet.getInstance(), Controller.getInstance().getAccounts()), asset);
		final JTable myVotesTable = Gui.createSortableTable(myVotesTableModel, 0);
		
		sorter = (TableRowSorter<VotesTableModel>) myVotesTable.getRowSorter();
		sorter.setComparator(VotesTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
		
		this.addTab(Lang.getInstance().translate("My Votes"), new JScrollPane(myVotesTable));
		
		
	}

	public void setAsset(AssetCls asset)
	{
		pollDetailPanel.setAsset(asset);
		allVotesTableModel.setAsset(asset);
		myVotesTableModel.setAsset(asset);
	}
	
	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
	}
	
}
