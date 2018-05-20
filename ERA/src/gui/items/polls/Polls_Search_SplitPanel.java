package gui.items.polls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import core.item.assets.AssetCls;
import core.item.polls.PollCls;
import gui.Split_Panel;
import lang.Lang;

public class Polls_Search_SplitPanel extends Split_Panel {

	private static final long serialVersionUID = 2717571093561259483L;
	All_Polls_Panel allVotingsPanel;
	// для прозрачности
	int alpha = 255;
	int alpha_int;
	PollsDetailPanel votingDetailsPanel;

	public Polls_Search_SplitPanel() {
		super("Votings_Search_SplitPanel");
		this.leftPanel.setVisible(false);
		allVotingsPanel = new All_Polls_Panel();
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
			PollCls poll = null;
			if (allVotingsPanel.pollsTable.getSelectedRow() >= 0)
				poll = allVotingsPanel.pollsTableModel.getPoll(
						allVotingsPanel.pollsTable.convertRowIndexToModel(allVotingsPanel.pollsTable.getSelectedRow()));
			if (poll == null)
				return;
			votingDetailsPanel = new PollsDetailPanel(poll, (AssetCls) allVotingsPanel.cbxAssets.getSelectedItem());
			jScrollPane_jPanel_RightPanel.setViewportView(votingDetailsPanel);
		}
	}

	public void onVoteClick() {
		// GET SELECTED OPTION
		int row = allVotingsPanel.pollsTable.getSelectedRow();
		if (row == -1) {
			row = 0;
		}
		PollCls poll = null;
		if (allVotingsPanel.pollsTable.getSelectedRow() >= 0)
			poll = allVotingsPanel.pollsTableModel.getPoll(
					allVotingsPanel.pollsTable.convertRowIndexToModel(allVotingsPanel.pollsTable.getSelectedRow()));
		new Polls_Dialog(poll, 0, (AssetCls) allVotingsPanel.cbxAssets.getSelectedItem());
	}

	@Override
	public void delay_on_close() {
		// delete observer left panel
		allVotingsPanel.pollsTableModel.removeObservers();
		// get component from right panel
		// Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
		// if Person_Info 002 delay on close
		// if (c1 instanceof Statement_Info) (
		// (Statement_Info)c1).delay_on_Close();

	}

}
