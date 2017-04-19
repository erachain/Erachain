package gui.items.voting;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import core.item.assets.AssetCls;
import core.voting.Poll;
import gui.Split_Panel;
import lang.Lang;

public class Votings_Search_SplitPanel extends Split_Panel {

	private static final long serialVersionUID = 2717571093561259483L;
	All_Votings_Panel allVotingsPanel;
	// для прозрачности
	int alpha = 255;
	int alpha_int;
	VotingDetailPanel votingDetailsPanel;

	public Votings_Search_SplitPanel() {
		this.leftPanel.setVisible(false);
		allVotingsPanel = new All_Votings_Panel();
		this.jSplitPanel.setLeftComponent(allVotingsPanel);
		setName(Lang.getInstance().translate("Search Votings"));
		jButton1_jToolBar_RightPanel.setText("<HTML><B> " + Lang.getInstance().translate("Vote") + "</></> ");
		jButton1_jToolBar_RightPanel.setBorderPainted(true);
		jButton1_jToolBar_RightPanel.setFocusable(true);
		jButton1_jToolBar_RightPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
				javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
		jButton1_jToolBar_RightPanel.setSize(120, 30);
		jButton1_jToolBar_RightPanel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onVoteClick();
			}
		});
		jButton2_jToolBar_RightPanel.setVisible(false);
		// Event LISTENER
		allVotingsPanel.pollsTable.getSelectionModel().addListSelectionListener(new search_listener());
	}

	// listener select row
	class search_listener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			Poll voting = null;
			if (allVotingsPanel.pollsTable.getSelectedRow() >= 0)
				voting = allVotingsPanel.pollsTableModel.getPoll(allVotingsPanel.pollsTable.convertRowIndexToModel(allVotingsPanel.pollsTable.getSelectedRow()));
			if (voting == null) return;
			votingDetailsPanel = new VotingDetailPanel(voting, (AssetCls) allVotingsPanel.cbxAssets.getSelectedItem());
			jScrollPane_jPanel_RightPanel.setViewportView(votingDetailsPanel);
		}
	}

	public void onVoteClick() {
		// GET SELECTED OPTION
		int row = allVotingsPanel.pollsTable.getSelectedRow();
		if (row == -1) {
			row = 0;
		}
		Poll voting = null;
		if (allVotingsPanel.pollsTable.getSelectedRow() >= 0)
			voting = allVotingsPanel.pollsTableModel.getPoll(
					allVotingsPanel.pollsTable.convertRowIndexToModel(allVotingsPanel.pollsTable.getSelectedRow()));
		new Voting_Dialog(voting, 0, (AssetCls) allVotingsPanel.cbxAssets.getSelectedItem());
	}

}
