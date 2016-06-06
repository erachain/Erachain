package gui.items.assets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultRowSorter;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import core.item.assets.AssetCls;
import gui.CoreRowSorter;
import gui.Split_Panel;
import gui.items.unions.TableModelUnions;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemImprintsTableModel;
import lang.Lang;

public class Search_Assets_Tab extends Split_Panel {
	private TableModelItemAssets tableModelItemAssets;

	public Search_Assets_Tab(){
		
		
		setName(Lang.getInstance().translate("Search Assets"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
		// not show buttons
			button1_ToolBar_LeftPanel.setVisible(false);
			button2_ToolBar_LeftPanel.setVisible(false);
			jButton1_jToolBar_RightPanel.setVisible(false);
			jButton2_jToolBar_RightPanel.setVisible(false);
			

	//CREATE TABLE
	tableModelItemAssets = new TableModelItemAssets();
	final JTable assetsTable = new JTable(tableModelItemAssets);
	
	//CHECKBOX FOR DIVISIBLE
//	TableColumn divisibleColumn = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_DIVISIBLE);
//	divisibleColumn.setCellRenderer(assetsTable.getDefaultRenderer(Boolean.class));
	
	//CHECKBOX FOR FAVORITE
//	TableColumn favoriteColumn = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_FAVORITE);
//	favoriteColumn.setCellRenderer(assetsTable.getDefaultRenderer(Boolean.class));

	//ASSETS SORTER
//	Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
//	CoreRowSorter sorter = new CoreRowSorter(tableModelItemAssets, indexes);
//	assetsTable.setRowSorter(sorter);
	
	//Custom renderer for the String column;
	assetsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
	assetsTable.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
	assetsTable.setDefaultRenderer(Boolean.class, new Renderer_Boolean()); // set renderer
	// column #1
		TableColumn column1 = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_KEY);//.COLUMN_CONFIRMED);
		column1.setMinWidth(50);
		column1.setMaxWidth(1000);
		column1.setPreferredWidth(50);
		// column #1
				TableColumn column2 = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_DIVISIBLE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
				column2.setMinWidth(50);
				column2.setMaxWidth(1000);
				column2.setPreferredWidth(50);
				// column #1
				TableColumn column3 = assetsTable.getColumnModel().getColumn(TableModelItemAssets.COLUMN_FAVORITE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
				column3.setMinWidth(50);
				column3.setMaxWidth(1000);
				column3.setPreferredWidth(50);
	//Sorter
		RowSorter sorter =   new TableRowSorter(tableModelItemAssets);
		assetsTable.setRowSorter(sorter);	
	// UPDATE FILTER ON TEXT CHANGE
		searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}

			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			public void onChange() {

	// GET VALUE
				String search = searchTextField_SearchToolBar_LeftPanel.getText();

	// SET FILTER
				tableModelItemAssets.fireTableDataChanged();
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter) sorter).setRowFilter(filter);
				tableModelItemAssets.fireTableDataChanged();
								
			}
		});
				
	// set showvideo			
		jTable_jScrollPanel_LeftPanel.setModel(this.tableModelItemAssets);
		jTable_jScrollPanel_LeftPanel = assetsTable;
		jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
		
	// MENU
	JPopupMenu nameSalesMenu = new JPopupMenu();
	JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
	details.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int row = assetsTable.getSelectedRow();
			row = assetsTable.convertRowIndexToModel(row);

			AssetCls asset = tableModelItemAssets.getAsset(row);
			new AssetFrame(asset);
		}
	});
	nameSalesMenu.add(details);

	assetsTable.setComponentPopupMenu(nameSalesMenu);
	assetsTable.addMouseListener(new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			int row = assetsTable.rowAtPoint(p);
			assetsTable.setRowSelectionInterval(row, row);
			
			if(e.getClickCount() == 2)
			{
				row = assetsTable.convertRowIndexToModel(row);
				AssetCls asset = tableModelItemAssets.getAsset(row);
				new AssetFrame(asset);
			}
		}
	});

	
}

public void removeObservers() {
	this.tableModelItemAssets.removeObservers();
}
}
