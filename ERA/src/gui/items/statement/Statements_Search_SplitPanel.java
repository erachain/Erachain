package gui.items.statement;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.ColorModel;
import java.util.ArrayList;

import javax.swing.Timer;
import java.awt.*;

import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.account.PublicKeyAccount;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import database.DBSet;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import gui.items.assets.IssueAssetPanel;
import gui.items.assets.TableModelItemAssets;
import gui.items.persons.PersonConfirmDialog;
import gui.items.persons.PersonSetStatusDialog;
import gui.items.persons.TableModelPersons;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemAssetsTableModel;
import gui.models.WalletItemPersonsTableModel;
import gui.records.VouchRecordDialog;
import lang.Lang;
import utils.TableMenuPopupUtil;


public class Statements_Search_SplitPanel extends Split_Panel {

	private static final long serialVersionUID = 2717571093561259483L;

	private Statements_Table_Model_Search search_Table_Model;
//	private MTable search_Table;
	private RowSorter<TableModelPersons> search_Sorter;
	
	// для прозрачности
	int alpha =255;
	int alpha_int;

	public Statements_Search_SplitPanel() {
		super("Statements_Search_SplitPanel");
		setName(Lang.getInstance().translate("Search Statements"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
		
	// not show buttons
		jToolBar_RightPanel.setVisible(false);
		toolBar_LeftPanel.setVisible(false);
		
	// not show My filter
		searth_My_JCheckBox_LeftPanel.setVisible(false);
		
	//CREATE TABLE
		search_Table_Model = new Statements_Table_Model_Search();
	//	search_Table = new MTable(this.search_Table_Model);
	//	TableColumnModel columnModel = search_Table.getColumnModel(); // read column model
	//		columnModel.getColumn(0).setMaxWidth((100));
	
	//Custom renderer for the String column;
		
	//	 this.search_Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
		
	/*		
		//CHECKBOX FOR FAVORITE
				TableColumn favoriteColumn = search_Table.getColumnModel().getColumn(TableModelPersons.COLUMN_FAVORITE);	
				favoriteColumn.setCellRenderer(new Renderer_Boolean()); 
				favoriteColumn.setMinWidth(50);
				favoriteColumn.setMaxWidth(50);
				favoriteColumn.setPreferredWidth(50);
	*/
	//Sorter
	//			 search_Sorter = new TableRowSorter<TableModelPersons>(this.search_Table_Model);
	//			search_Table.setRowSorter(search_Sorter);	
	
	// UPDATE FILTER ON TEXT CHANGE
		searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener( new search_tab_filter());
	// SET VIDEO			
		jTable_jScrollPanel_LeftPanel = new MTable(this.search_Table_Model);
	//	jTable_jScrollPanel_LeftPanel = search_Table;
		//sorter from 0 column
		search_Sorter = new TableRowSorter(search_Table_Model);
		ArrayList<SortKey> keys = new ArrayList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING)); 
		search_Sorter.setSortKeys(keys);
		((DefaultRowSorter<?, ?>) search_Sorter).setSortsOnUpdates(true);
		this.jTable_jScrollPanel_LeftPanel.setRowSorter(search_Sorter);
		jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
	//	setRowHeightFormat(true);
	// Event LISTENER		
		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
	
		JPopupMenu menu =new JPopupMenu();
		
		JMenuItem set_Status_Item= new JMenuItem(Lang.getInstance().translate("Set Status"));
	    
		set_Status_Item.addActionListener(
				new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		//	  	@SuppressWarnings("unused")
						//	PersonSetStatusDialog fm = new PersonSetStatusDialog( search_Table_Model.get_Statement(search_Table.convertRowIndexToModel(search_Table.getSelectedRow())));	
				
		}});
		
	//	menu.add(set_Status_Item);
		
		JMenuItem vouch_Item= new JMenuItem(Lang.getInstance().translate("Vouch"));
	    
		vouch_Item.addActionListener(new ActionListener(){
		
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (jTable_jScrollPanel_LeftPanel.getSelectedRow() < 0 )	return;
					
				
				Transaction statement = search_Table_Model.get_Statement(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
				if (statement == null) return;
				VouchRecordDialog vouch_panel = new VouchRecordDialog(statement.getBlockHeight(DBSet.getInstance()),statement.getSeqNo(DBSet.getInstance()));
			
			}
		});
		
		menu.add(vouch_Item);
		
		TableMenuPopupUtil.installContextMenu(jTable_jScrollPanel_LeftPanel, menu);
	
		jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = jTable_jScrollPanel_LeftPanel.rowAtPoint(p);
				jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(row, row);
				
				
				if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
				{
					
					if (jTable_jScrollPanel_LeftPanel.getSelectedColumn() == TableModelPersons.COLUMN_FAVORITE){
					//	row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
					//	 PersonCls asset = search_Table_Model.getPerson(row);
						favorite_set( jTable_jScrollPanel_LeftPanel);	
						
						
						
					}
					
					
				}
		     }
		});
	
	
	
	
	}

	// set favorite Search	
	void favorite_all(JTable personsTable){
		int row = personsTable.getSelectedRow();
		row = personsTable.convertRowIndexToModel(row);
/*
	PersonCls person = search_Table_Model.getPerson(row);
	//new AssetPairSelect(asset.getKey());

	
		//CHECK IF FAVORITES
		if(Controller.getInstance().isItemFavorite(person))
		{
			
			Controller.getInstance().removeItemFavorite(person);
		}
		else
		{
			
			Controller.getInstance().addItemFavorite(person);
		}
			

		personsTable.repaint();
*/
	}

	// filter search
	 class search_tab_filter implements DocumentListener {
			
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
			//tableModelPersons.getSortableList().setFilter(search);
			search_Table_Model.fireTableDataChanged();
			
			RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
			((DefaultRowSorter) search_Sorter).setRowFilter(filter);
			
			search_Table_Model.fireTableDataChanged();
			
		}
	}
	
	// listener select row	 
	class search_listener implements ListSelectionListener  {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
		
			if (jTable_jScrollPanel_LeftPanel.getSelectedRow() < 0 )
				return;
			
			Transaction statement = search_Table_Model.get_Statement(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
			Statement_Info info_panel = new Statement_Info(statement);
			info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
			jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
		//	jSplitPanel.setRightComponent(info_panel);
		}
	}
	@Override
	public void delay_on_close(){
		// delete observer left panel
		search_Table_Model.removeObservers();
		// get component from right panel
		Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
		// if Person_Info 002 delay on close
		  if (c1 instanceof Statement_Info) ( (Statement_Info)c1).delay_on_Close();
		
	}
	 public void favorite_set(JTable personsTable){


		 int row = personsTable.getSelectedRow();
		 row = personsTable.convertRowIndexToModel(row);

		    Transaction person = search_Table_Model.get_Statement(row);
		 //new AssetPairSelect(asset.getKey());

		
		 	//CHECK IF FAVORITES
		 	if(((R_SignNote) person).isFavorite())
		 	{
		 		
		 		Controller.getInstance().wallet.database.getDocumentFavoritesSet().delete(person);
		 	}
		 	else
		 	{
		 		
		 		Controller.getInstance().wallet.database.getDocumentFavoritesSet().add(person);
		 	}
		 		

		 	personsTable.repaint();

		 
		 }

}
