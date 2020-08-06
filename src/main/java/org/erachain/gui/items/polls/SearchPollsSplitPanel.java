package org.erachain.gui.items.polls;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.SearchItemSplitPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class SearchPollsSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "SearchPollsSplitPanel";
    public static String TITLE = "Search Polls";

    private static final long serialVersionUID = 1L;
    private static PollsItemsTableModel tableModelPolls = new PollsItemsTableModel();

    public SearchPollsSplitPanel() {
        super(tableModelPolls, NAME, TITLE);

        jTableJScrollPanelLeftPanel.getColumnModel().getColumn(3).setMaxWidth(200);
        jTableJScrollPanelLeftPanel.getColumnModel().getColumn(3).setPreferredWidth(100);
        jTableJScrollPanelLeftPanel.getColumnModel().getColumn(4).setMaxWidth(200);
        jTableJScrollPanelLeftPanel.getColumnModel().getColumn(4).setPreferredWidth(100);

        JMenuItem setVote_Menu = new JMenuItem(Lang.getInstance().translate("To Vote"));
        setVote_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//			new UnionSetStatusDialog(th, (UnionCls) itemMenu);

                PollCls poll = (PollCls) (itemTableSelected);
                AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
                new PollsDialog(poll, 0, AssetCls);
            }
        });
        this.menuTable.add(setVote_Menu);

        menuTable.addSeparator();

        JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set status"));
        setStatus_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //			new UnionSetStatusDialog(th, (UnionCls) itemMenu);
            }
        });
        this.menuTable.add(setStatus_Menu);

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?poll=" + itemTableSelected.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });
        menuTable.add(setSeeInBlockexplorer);

        // ADD MENU ITEMS
        JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        confirm_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //			new UnionConfirmDialog(th, (UnionCls) itemMenu);
            }
        });
        this.menuTable.add(confirm_Menu);

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
		
		new PollsDialog(poll, option, (AssetCls) allVotingsPanel.cbxAssets.getSelectedItem());
	}
	*/

    @Override
    public Component getShow(ItemCls item) {
        AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
        PollsDetailPanel pollInfo = new PollsDetailPanel((PollCls) item, AssetCls);

        return pollInfo;

    }
}