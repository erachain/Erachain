package gui.items.records;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.mapdb.Fun.Tuple2;
import core.transaction.Transaction;
import database.DBSet;
import gui.Split_Panel;
import gui.items.mails.Mail_Info;
import gui.library.Voush_Library_Panel;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;

public class Records_My_SplitPanel extends Split_Panel {

	private static final long serialVersionUID = 2717571093561259483L;

	JScrollPane jScrollPane4;

	All_Records_Panel allVotingsPanel;
	// для прозрачности
	int alpha = 255;
	int alpha_int;
	private JPanel records_Info_Panel;
	// VotingDetailPanel votingDetailsPanel ;

	public Voush_Library_Panel voush_Library_Panel;

	public Records_My_SplitPanel() {
		super("Records_My_SplitPanel");
		this.leftPanel.setVisible(false);
		allVotingsPanel = new All_Records_Panel();
		this.jSplitPanel.setLeftComponent(allVotingsPanel);

		setName(Lang.getInstance().translate("My Records"));

		// searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search")
		// +": ");

		jScrollPane4 = new JScrollPane();

		// not show buttons
		jToolBar_RightPanel.setVisible(false);
		// toolBar_LeftPanel.setVisible(false);
		jButton1_jToolBar_RightPanel.setText("<HTML><B> " + Lang.getInstance().translate("Record") + "</></> ");
		jButton1_jToolBar_RightPanel.setBorderPainted(true);
		jButton1_jToolBar_RightPanel.setFocusable(true);
		jButton1_jToolBar_RightPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
				javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
		jButton1_jToolBar_RightPanel.setSize(120, 30);
		jButton1_jToolBar_RightPanel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClick();
			}
		});

		jButton2_jToolBar_RightPanel.setVisible(false);
		allVotingsPanel.records_Table.getSelectionModel().addListSelectionListener(new search_listener());
	}

	// listener select row
	class search_listener implements ListSelectionListener {
		

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			Transaction voting = null;
			if (allVotingsPanel.records_Table.getSelectedRow() >= 0) {
				voting = (Transaction) allVotingsPanel.records_model.getItem(allVotingsPanel.records_Table
						.convertRowIndexToModel(allVotingsPanel.records_Table.getSelectedRow()));

				records_Info_Panel = new JPanel();
				records_Info_Panel.setLayout(new GridBagLayout());

				// TABLE GBC
				GridBagConstraints tableGBC = new GridBagConstraints();
				tableGBC.fill = GridBagConstraints.BOTH;
				tableGBC.anchor = GridBagConstraints.FIRST_LINE_START;
				tableGBC.weightx = 1;
				tableGBC.weighty = 1;
				tableGBC.gridx = 0;
				tableGBC.gridy = 0;
				records_Info_Panel.add(TransactionDetailsFactory.getInstance().createTransactionDetail(voting), tableGBC);

				Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs = DBSet.getInstance().getVouchRecordMap()
						.get(voting.getBlockHeight(DBSet.getInstance()), voting.getSeqNo(DBSet.getInstance()));
				GridBagConstraints gridBagConstraints = null;
				if (signs != null) {

					JLabel jLabelTitlt_Table_Sign = new JLabel(Lang.getInstance().translate("Signatures") + ":");
					gridBagConstraints = new java.awt.GridBagConstraints();
					gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
					gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
					gridBagConstraints.weightx = 0.1;
					gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 11);
					gridBagConstraints.gridx = 0;
					gridBagConstraints.gridy = 1;
					records_Info_Panel.add(jLabelTitlt_Table_Sign, gridBagConstraints);
					gridBagConstraints = new java.awt.GridBagConstraints();
					gridBagConstraints.gridx = 0;
					gridBagConstraints.gridy = 2;
					gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
					gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
					gridBagConstraints.weightx = 1.0;
					gridBagConstraints.weighty = 1.0;
					voush_Library_Panel = new Voush_Library_Panel(voting);
					records_Info_Panel.add(voush_Library_Panel, gridBagConstraints);

				}

				jScrollPane_jPanel_RightPanel.setViewportView(records_Info_Panel);

			}
		}
	}

	public void onClick() {
		// GET SELECTED OPTION
		int row = allVotingsPanel.records_Table.getSelectedRow();
		if (row == -1) {
			row = 0;
		}
		row = allVotingsPanel.records_Table.convertRowIndexToModel(row);

		if (allVotingsPanel.records_Table.getSelectedRow() >= 0) {
		}
	}
	@Override
	public void delay_on_close(){
		// delete observer left panel
		allVotingsPanel.records_model.removeObservers();
		// get component from right panel
	//	Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
		// if Person_Info 002 delay on close
	//	  if (c1.getClass() == this.records_Info_Panel.getClass()) voush_Library_Panel.delay_on_close();
		
	}

}
