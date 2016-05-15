package gui.items.imprints;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import core.item.imprints.ImprintCls;
import gui.CoreRowSorter;
import gui.Split_Panel;
import gui.items.unions.TableModelUnions;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemImprintsTableModel;
import lang.Lang;

public class My_Imprints_Tab extends Split_Panel{

public My_Imprints_Tab(){
	
	setName(Lang.getInstance().translate("My Imprints"));
	searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	// not show buttons
	button1_ToolBar_LeftPanel.setVisible(false);
	button2_ToolBar_LeftPanel.setVisible(false);
	jButton1_jToolBar_RightPanel.setVisible(false);
	jButton2_jToolBar_RightPanel.setVisible(false);
	
	
	//TABLE
			final WalletItemImprintsTableModel imprintsModel = new WalletItemImprintsTableModel();
			final JTable tableImprints = new JTable(imprintsModel);
			
			//POLLS SORTER
			Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
			CoreRowSorter sorter = new CoreRowSorter(imprintsModel, indexes);
			tableImprints.setRowSorter(sorter);

			/*
			//CHECKBOX FOR DIVISIBLE
			TableColumn divisibleColumn = table.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_DIVISIBLE);
			divisibleColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			*/
			
			//CHECKBOX FOR CONFIRMED
			TableColumn confirmedColumn = tableImprints.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_CONFIRMED);
			//confirmedColumn.setCellRenderer(tableImprints.getDefaultRenderer(Boolean.class));
			confirmedColumn.setCellRenderer(new Renderer_Boolean()); //unionsTable.getDefaultRenderer(Boolean.class));
			confirmedColumn.setMinWidth(50);
			confirmedColumn.setMaxWidth(100);
			confirmedColumn.setPreferredWidth(50);//.setWidth(30);
			// column #1
			TableColumn column1 = tableImprints.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_KEY);//.COLUMN_CONFIRMED);
			column1.setMinWidth(1);
			column1.setMaxWidth(1000);
			column1.setPreferredWidth(50);
			
			
			
			//Custom renderer for the String column;
			tableImprints.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
			tableImprints.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
		//CHECKBOX FOR FAVORITE
			//TableColumn favoriteColumn = unionsTable.getColumnModel().getColumn(TableModelUnions.COLUMN_FAVORITE);
			
		

			
			
			
			
			
			// set video			
			//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelUnions);
	jTable_jScrollPanel_LeftPanel.setModel(imprintsModel);
			//jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = unionsTable;
	jTable_jScrollPanel_LeftPanel = tableImprints;
			//jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // unionsTable; 
	jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
			
			
			
			
			
			
			
			
			
			
			
			
			//MENU
			JPopupMenu imprintsMenu = new JPopupMenu();
			JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
			details.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int row = tableImprints.getSelectedRow();
					row = tableImprints.convertRowIndexToModel(row);

					ImprintCls imprint = (ImprintCls)imprintsModel.getItem(row);
					new ImprintFrame(imprint);
				}
			});
			imprintsMenu.add(details);
			
			/*
			JMenuItem dividend = new JMenuItem(Lang.getInstance().translate("Pay dividend"));
			dividend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int row = table.getSelectedRow();
					row = table.convertRowIndexToModel(row);

					ImprintCls imprint = (ImprintCls)imprintsModel.getItem(row);	
					new PayDividendFrame(imprint);
				}
			});
			imprintsMenu.add(dividend);
			*/
			
			tableImprints.setComponentPopupMenu(imprintsMenu);
			
			//MOUSE ADAPTER
			tableImprints.addMouseListener(new MouseAdapter() 
			{
				@Override
				public void mousePressed(MouseEvent e) 
				{
					Point p = e.getPoint();
					int row = tableImprints.rowAtPoint(p);
					tableImprints.setRowSelectionInterval(row, row);
			     }
			});
			
			tableImprints.addMouseListener(new MouseAdapter() 
			{
				@Override
				public void mousePressed(MouseEvent e) 
				{
					Point p = e.getPoint();
					int row = tableImprints.rowAtPoint(p);
					tableImprints.setRowSelectionInterval(row, row);
					
					if(e.getClickCount() == 2)
					{
						row = tableImprints.convertRowIndexToModel(row);
						ImprintCls imprint = (ImprintCls)imprintsModel.getItem(row);
						new ImprintFrame(imprint);
					}
			     }
			});
		/*	
			//ADD NAMING SERVICE TABLE
	//		this.add(new JScrollPane(table), tableGBC);
			
			//ADD REGISTER BUTTON
			JButton issueButton = new JButton(Lang.getInstance().translate("Issue Imprint"));
			issueButton.setPreferredSize(new Dimension(120, 25));
			issueButton.addActionListener(new ActionListener()
			{
			    public void actionPerformed(ActionEvent e)
			    {
			        onIssueClick();
			    }
			});	
			this.add(issueButton, buttonGBC);
			
			//ADD ALL BUTTON
			buttonGBC.gridx = 1;
			JButton allButton = new JButton(Lang.getInstance().translate("All Imprints"));
			allButton.setPreferredSize(new Dimension(120, 25));
			allButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onAllClick();
				}
			});	
			this.add(allButton, buttonGBC);
			
			//ADD MY ORDERS BUTTON
			buttonGBC.gridx = 2;
			JButton myOrdersButton = new JButton(Lang.getInstance().translate("My Orders"));
			myOrdersButton.setPreferredSize(new Dimension(120, 25));
			myOrdersButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onMyOrdersClick();
				}
			});	
			this.add(myOrdersButton, buttonGBC);
			
			
			*/
		}
		
		public void onIssueClick()
		{
			new IssueImprintFrame();
		}
		
		public void onAllClick()
		{
			new AllImprintsFrame();
		}
		
		public void onMyOrdersClick()
		{
			//new MyOrdersFrame();
		}	
	
	
	
	
	
}




