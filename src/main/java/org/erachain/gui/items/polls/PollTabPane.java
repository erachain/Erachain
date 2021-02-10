package org.erachain.gui.items.polls;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.gui.Gui;
import org.erachain.gui.models.ItemPollsTableModel;
import org.erachain.gui.models.VotesTableModel;
import org.erachain.lang.Lang;
import org.erachain.utils.BigDecimalStringComparator;

import javax.swing.*;
import javax.swing.table.TableRowSorter;

public class PollTabPane extends JTabbedPane {

    private static final long serialVersionUID = 2717571093561259483L;

    PollDetailPanel pollDetailPanel;
    JTable allVotesTable;
    private ItemPollsTableModel myVotesTableModel;
    private ItemPollsTableModel allVotesTableModel;

    @SuppressWarnings("unchecked")
    public PollTabPane(PollCls poll, ItemCls asset) {
        super();

        //POLL DETAILS
        this.pollDetailPanel = new PollDetailPanel(poll, (AssetCls) asset);
        this.addTab(Lang.T("Poll Details"), this.pollDetailPanel);

        //ALL VOTES
        allVotesTableModel = new ItemPollsTableModel();
        //(Long)poll.getKey(DCSet.getInstance()), asset);
        allVotesTable = Gui.createSortableTable(allVotesTableModel, 0);

        TableRowSorter<VotesTableModel> sorter = (TableRowSorter<VotesTableModel>) allVotesTable.getRowSorter();
        sorter.setComparator(VotesTableModel.COLUMN_VOTES, new BigDecimalStringComparator());

        this.addTab(Lang.T("All Votes"), new JScrollPane(allVotesTable));

        //MY VOTES
        myVotesTableModel = new ItemPollsTableModel();
        //poll.getVotes(DCSet.getInstance(), Controller.getInstance().getAccounts()), asset);
        final JTable myVotesTable = Gui.createSortableTable(myVotesTableModel, 0);

        sorter = (TableRowSorter<VotesTableModel>) myVotesTable.getRowSorter();
        sorter.setComparator(VotesTableModel.COLUMN_VOTES, new BigDecimalStringComparator());

        this.addTab(Lang.T("My Votes"), new JScrollPane(myVotesTable));


    }

    public void setAsset(AssetCls asset) {
        pollDetailPanel.setAsset(asset);
        allVotesTableModel.setAsset(asset);
        myVotesTableModel.setAsset(asset);
    }

    public void close() {
        //REMOVE OBSERVERS/HANLDERS
    }

}
