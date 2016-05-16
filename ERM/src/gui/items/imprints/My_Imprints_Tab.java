package gui.items.imprints;

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
import javax.swing.table.TableColumn;

import core.item.imprints.ImprintCls;
import gui.CoreRowSorter;
import gui.Split_Panel;
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
	final JTable tableImprints = new JTable(imprintsModel);
//POLLS SORTER
	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
	CoreRowSorter sorter = new CoreRowSorter(imprintsModel, indexes);
	tableImprints.setRowSorter(sorter);
//CHECKBOX FOR CONFIRMED
	TableColumn confirmedColumn = tableImprints.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_CONFIRMED);
	confirmedColumn.setCellRenderer(new Renderer_Boolean()); 
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
// set show			
	jTable_jScrollPanel_LeftPanel.setModel(imprintsModel);
	jTable_jScrollPanel_LeftPanel = tableImprints;
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
			
		}	
	
	
	
	
	
}




