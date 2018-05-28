package gui.items.polls;

import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.polls.PollCls;
import datachain.DCSet;
import gui.items.Item_Search_SplitPanel;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Polls_Search_SplitPanel extends Item_Search_SplitPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static TableModelPolls tableModelPolls = new TableModelPolls();
    private Polls_Search_SplitPanel th;

    public Polls_Search_SplitPanel() {
        super(tableModelPolls, "Search_Popll_Tab", "Search_Poll_Tab");
        th = this;

        // ADD MENU ITEMS
        JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
        confirm_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //			new UnionConfirmDialog(th, (UnionCls) item_Menu);
            }
        });
        this.menu_Table.add(confirm_Menu);

        JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
        setStatus_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //			new UnionSetStatusDialog(th, (UnionCls) item_Menu);
            }
        });
        this.menu_Table.add(setStatus_Menu);


        JMenuItem setVote_Menu = new JMenuItem(Lang.getInstance().translate("Voting"));
        setVote_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//			new UnionSetStatusDialog(th, (UnionCls) item_Menu);

                PollCls poll = (PollCls) (item_Table_Selected);
                AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
                new Polls_Dialog(poll, 0, AssetCls);
            }
        });
        this.menu_Table.add(setVote_Menu);

    }
	
	
	

	/*
	// show details
	public void onVoteClick() {
		// GET SELECTED OPTION
		int option = votingDetailsPanel.pollTabPane.pollDetailPanel.optionsTable.getSelectedRow();
		if (option >= 0) {
			option = votingDetailsPanel.pollTabPane.pollDetailPanel.optionsTable.convertRowIndexToModel(option);
		}
		
		//this.pollOptionsTableModel;

		PollCls poll = null;
		if (allVotingsPanel.pollsTable.getSelectedRow() >= 0)
			poll = allVotingsPanel.pollsTableModel.getPoll(
					allVotingsPanel.pollsTable.convertRowIndexToModel(allVotingsPanel.pollsTable.getSelectedRow()));
		
		new Polls_Dialog(poll, option, (AssetCls) allVotingsPanel.cbxAssets.getSelectedItem());
	}
	*/

    @Override
    public Component get_show(ItemCls item) {
        AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
        PollsDetailPanel pollInfo = new PollsDetailPanel((PollCls) item, AssetCls);

        return pollInfo;

    }

}
