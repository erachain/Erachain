package gui.items.imprints;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import core.item.imprints.ImprintCls;
import gui.CoreRowSorter;
import gui.Split_Panel;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemImprintsTableModel;
import lang.Lang;

public class My_Imprints_Tab extends Split_Panel{


	private static final long serialVersionUID = 1L;

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
	final MTable tableImprints = new MTable(imprintsModel);
//POLLS SORTER
	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
	CoreRowSorter sorter = new CoreRowSorter(imprintsModel, indexes);
	tableImprints.setRowSorter(sorter);
//CHECKBOX FOR CONFIRMED
	TableColumn confirmedColumn = tableImprints.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_CONFIRMED);
	
	confirmedColumn.setMinWidth(50);
	confirmedColumn.setMaxWidth(100);
	confirmedColumn.setPreferredWidth(50);//.setWidth(30);
// column #1
	TableColumn column1 = tableImprints.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_KEY);//.COLUMN_CONFIRMED);
	column1.setMinWidth(1);
	column1.setMaxWidth(1000);
	column1.setPreferredWidth(50);
// set show			
	jTable_jScrollPanel_LeftPanel.setModel(imprintsModel);
	jTable_jScrollPanel_LeftPanel = tableImprints;
	jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
	
	// Event LISTENER		
		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						ImprintCls imprint = null;
						if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 ) imprint = imprintsModel.getItem(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
						
						
						
					//	info.show_001(person);
						
					//	search_Person_SplitPanel.jSplitPanel.setDividerLocation(search_Person_SplitPanel.jSplitPanel.getDividerLocation());	
					//	search_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
						 Imprints_Info_Panel info_panel = new Imprints_Info_Panel(imprint);
						info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
						jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
					}
				});
		
	
/*	
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
	*/	
	}
		
		public void onIssueClick()
		{
			new IssueImprintDialog();
		}
		
		public void onAllClick()
		{
			new AllImprintsFrame();
		}
		
		public void onMyOrdersClick()
		{
			
		}	
	
	
	
	
	
}




