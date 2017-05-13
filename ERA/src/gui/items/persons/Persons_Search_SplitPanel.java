package gui.items.persons;

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
import java.util.Date;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.Timer;
import javax.swing.UIManager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

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
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.item.assets.AssetCls;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import database.DBSet;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.assets.IssueAssetPanel;
import gui.items.assets.TableModelItemAssets;
import gui.items.mails.Mail_Send_Dialog;
import gui.library.MButton;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemAssetsTableModel;
import gui.models.WalletItemPersonsTableModel;
import gui.records.VouchRecordDialog;
import lang.Lang;
import utils.TableMenuPopupUtil;


public class Persons_Search_SplitPanel extends Split_Panel{

	private static final long serialVersionUID = 2717571093561259483L;

	private TableModelPersons search_Table_Model;
	private MTable search_Table;
	private RowSorter<TableModelPersons> search_Sorter;
	
// для прозрачности
     int alpha =255;
     int alpha_int;
	public Persons_Search_SplitPanel(){
		super("Persons_Search_SplitPanel");
		setName(Lang.getInstance().translate("Search Persons"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	// not show buttons
		jToolBar_RightPanel.setVisible(false);
		toolBar_LeftPanel.setVisible(false);
		
		this.searchToolBar_LeftPanel.setVisible(true);
// not show My filter
		searth_My_JCheckBox_LeftPanel.setVisible(false);
		searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
	
//CREATE TABLE
		search_Table_Model = new TableModelPersons();
		search_Table = new MTable(this.search_Table_Model);
		TableColumnModel columnModel = search_Table.getColumnModel(); // read column model
		columnModel.getColumn(0).setMaxWidth((100));
	
	
//CHECKBOX FOR FAVORITE
		TableColumn favoriteColumn = search_Table.getColumnModel().getColumn(search_Table_Model.COLUMN_BORN);	
//		favoriteColumn.setCellRenderer(new Renderer_Boolean()); 
	//	 int ss = search_Table_Model.getColumnName(search_Table_Model.COLUMN_BORN).length();
		int rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth(search_Table_Model.getColumnName(search_Table_Model.COLUMN_BORN)));	
		favoriteColumn.setMinWidth(rr+1);
		favoriteColumn.setMaxWidth(rr*10);
		favoriteColumn.setPreferredWidth(rr+5);
		//Sorter
		 search_Sorter = new TableRowSorter<TableModelPersons>(this.search_Table_Model);
		search_Table.setRowSorter(search_Sorter);	
	
// UPDATE FILTER ON TEXT CHANGE
		searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener( new search_tab_filter());
// SET VIDEO			
		jTable_jScrollPanel_LeftPanel.setModel(this.search_Table_Model);
		jTable_jScrollPanel_LeftPanel = search_Table;
		jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
//		setRowHeightFormat(true);
// Event LISTENER		
		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
	
		
		JPopupMenu menu = new JPopupMenu();

	
    	    	
    	    	JMenuItem vsend_Coins_Item= new JMenuItem(Lang.getInstance().translate("Send Asset"));
    	    
    	    	vsend_Coins_Item.addActionListener(new ActionListener(){
    	  		@Override
    	    	public void actionPerformed(ActionEvent e) {
    	  			
    				
    	  			int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
    				row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
    	    		
    				PersonCls person = search_Table_Model.getPerson(row);
    	  			Account_Send_Dialog fm = new Account_Send_Dialog(null,null,null, person);				
    				}});
    	    	
    	    	menu.add(vsend_Coins_Item);
    	    	JMenuItem send_Mail_Item= new JMenuItem(Lang.getInstance().translate("Send Mail"));
    	    	send_Mail_Item.addActionListener(new ActionListener(){
    	  		@Override
    	    	public void actionPerformed(ActionEvent e) {
    	   

    	  			int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
    				row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
    	    		
    				PersonCls person = search_Table_Model.getPerson(row);
    	  				Mail_Send_Dialog fm = new Mail_Send_Dialog(null,null,null, person);
    				}});
    	    	
    	    	menu.add(send_Mail_Item);
   	    	TableMenuPopupUtil.installContextMenu(jTable_jScrollPanel_LeftPanel, menu);
	}
// set favorite Search	
	void favorite_all(JTable personsTable){
		int row = personsTable.getSelectedRow();
		row = personsTable.convertRowIndexToModel(row);

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
	//			search_Table_Model.getSortableList().setFilter(".*" + search + ".*");
	//			search_Table_Model.fireTableDataChanged();
				
				search_Table_Model.set_Filter_By_Name(search);
	//			RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
	//			((DefaultRowSorter) search_Sorter).setRowFilter(filter);
				
				search_Table_Model.fireTableDataChanged();
				
			}
		}
	
// listener select row	 
	 class search_listener implements ListSelectionListener  {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				PersonCls person = null;
				if (search_Table.getSelectedRow() >= 0 ) person = search_Table_Model.getPerson(search_Table.convertRowIndexToModel(search_Table.getSelectedRow()));
				if (person == null) return;
					Person_Info_002 info_panel = new Person_Info_002(person, true);
					info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
					jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
				
			}
		}


}
