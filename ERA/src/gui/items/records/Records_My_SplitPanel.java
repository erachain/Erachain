package gui.items.records;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.mapdb.Fun.Tuple2;

import controller.Controller;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.MainFrame;
import gui.Split_Panel;
import gui.items.mails.Mail_Info;
import gui.library.Voush_Library_Panel;
import gui.transaction.TransactionDetailsFactory;
import lang.Lang;
import utils.TableMenuPopupUtil;

public class Records_My_SplitPanel extends Split_Panel {

	private static final long serialVersionUID = 2717571093561259483L;

	private static Records_My_SplitPanel instance;

	JScrollPane jScrollPane4;

	All_Records_Panel my_Records_Panel;
	// для прозрачности
	int alpha = 255;
	int alpha_int;
	private JPanel records_Info_Panel;
	// VotingDetailPanel votingDetailsPanel ;

	public Voush_Library_Panel voush_Library_Panel;

	private JPopupMenu menu;

	private JMenuItem item_Delete;

	private JMenuItem item_Rebroadcast;

	protected Transaction trans;

	
	public static Records_My_SplitPanel getInstance(){
		
		if(instance == null)
		{
			instance = new Records_My_SplitPanel();
		}
		
		return instance;
		
		
	}
	
	private Records_My_SplitPanel() {
		super("Records_My_SplitPanel");
		this.leftPanel.setVisible(false);
		my_Records_Panel = new All_Records_Panel();
		this.jSplitPanel.setLeftComponent(my_Records_Panel);

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
		my_Records_Panel.records_Table.getSelectionModel().addListSelectionListener(new search_listener());
	
		 menu = new JPopupMenu();
		 menu.addAncestorListener(new AncestorListener(){

				@Override
				public void ancestorAdded(AncestorEvent event) {
					// TODO Auto-generated method stub
					int row = my_Records_Panel.records_Table.getSelectedRow();
					row = my_Records_Panel.records_Table.convertRowIndexToModel(row);
					if (row < 0) return;
					trans = (Transaction) my_Records_Panel.records_model.getItem(row);
				}

				@Override
				public void ancestorMoved(AncestorEvent event) {
					// TODO Auto-generated method stub
					// TODO Auto-generated method stub
					
				}

				@Override
				public void ancestorRemoved(AncestorEvent event) {
					// TODO Auto-generated method stub
					int row = my_Records_Panel.records_Table.getSelectedRow();
					row = my_Records_Panel.records_Table.convertRowIndexToModel(row);
					if (row < 0) return;
					trans = (Transaction) my_Records_Panel.records_model.getItem(row);
				
				}
				
				
				
				
			});
    	
    	 item_Rebroadcast= new JMenuItem(Lang.getInstance().translate("Rebroadcast"));
    
    	item_Rebroadcast.addActionListener(new ActionListener(){
  		@Override
    	public void actionPerformed(ActionEvent e) {
  			// code Rebroadcast
			
  			if (trans == null) return;
			// DBSet db = DBSet.getInstance();
  			Controller.getInstance().broadcastTransaction(trans);
  						
  		}	
  		});
    	
    	menu.add(item_Rebroadcast);
    	 item_Delete= new JMenuItem(Lang.getInstance().translate("Delete"));
    	item_Delete.addActionListener(new ActionListener(){
  		@Override
    	public void actionPerformed(ActionEvent e) {
   
  			// code delete
			//int row = my_Records_Panel.records_Table.getSelectedRow();
			//row = my_Records_Panel.records_Table.convertRowIndexToModel(row);
			//Transaction trans = (Transaction) my_Records_Panel.records_model.getItem(row);
			if (trans == null) return;
				DCSet.getInstance().getTransactionMap().delete(trans);
  			
			}});
    	
    	menu.add(item_Delete);
   	TableMenuPopupUtil.installContextMenu(my_Records_Panel.records_Table, menu);
	menu.addAncestorListener(new AncestorListener(){

		@Override
		public void ancestorAdded(AncestorEvent event) {
			// TODO Auto-generated method stub
		//	int row = my_Records_Panel.records_Table.getSelectedRow();
		//	row = my_Records_Panel.records_Table.convertRowIndexToModel(row);
		//	Transaction trans = (Transaction) my_Records_Panel.records_model.getItem(row);
			if (trans == null) return;
			item_Delete.setEnabled(true);
			item_Rebroadcast.setEnabled(true);
			if (trans.isConfirmed(DCSet.getInstance())) {
				item_Delete.setEnabled(false);
				item_Rebroadcast.setEnabled(false);
				
			}
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			// TODO Auto-generated method stub
			
		}
		
		
	});
	
	
	
	
	
	}

	// listener select row
	class search_listener implements ListSelectionListener {
		

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			Transaction trans = null;
			if (my_Records_Panel.records_Table.getSelectedRow() >= 0) {
				trans = (Transaction) my_Records_Panel.records_model.getItem(my_Records_Panel.records_Table
						.convertRowIndexToModel(my_Records_Panel.records_Table.getSelectedRow()));

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
				records_Info_Panel.add(TransactionDetailsFactory.getInstance().createTransactionDetail(trans), tableGBC);

				Tuple2<BigDecimal, List<Tuple2<Integer, Integer>>> signs = DCSet.getInstance().getVouchRecordMap()
						.get(trans.getBlockHeight(DCSet.getInstance()), trans.getSeqNo(DCSet.getInstance()));
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
					voush_Library_Panel = new Voush_Library_Panel(trans);
					records_Info_Panel.add(voush_Library_Panel, gridBagConstraints);

				}

				jScrollPane_jPanel_RightPanel.setViewportView(records_Info_Panel);

			}
		}
	}

	public void onClick() {
		// GET SELECTED OPTION
		int row = my_Records_Panel.records_Table.getSelectedRow();
		if (row == -1) {
			row = 0;
		}
		row = my_Records_Panel.records_Table.convertRowIndexToModel(row);

		if (my_Records_Panel.records_Table.getSelectedRow() >= 0) {
		}
	}
	@Override
	public void delay_on_close(){
		// delete observer left panel
		my_Records_Panel.records_model.removeObservers();
		// get component from right panel
	//	Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
		// if Person_Info 002 delay on close
	//	  if (c1.getClass() == this.records_Info_Panel.getClass()) voush_Library_Panel.delay_on_close();
		
	}

}
