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
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
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
import gui.items.assets.ExchangeFrame;
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
import utils.MenuPopupUtil;
import utils.TableMenuPopupUtil;


public class Persons_Search_SplitPanel extends Split_Panel{

	private static final long serialVersionUID = 2717571093561259483L;

	private TableModelPersons search_Table_Model;
	private MTable search_Table;
	private RowSorter<TableModelPersons> search_Sorter;
	private int selected_Item;
	
// для прозрачности
     int alpha =255;
     int alpha_int;

	private JTextField key_Item;

	protected int row;
	public Persons_Search_SplitPanel(){
		super("Persons_Search_SplitPanel");
		setName(Lang.getInstance().translate("Search Persons"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
	// not show buttons
		jToolBar_RightPanel.setVisible(false);
		toolBar_LeftPanel.setVisible(true);
		button1_ToolBar_LeftPanel.setVisible(false);
		button2_ToolBar_LeftPanel.setVisible(false);
		
		this.searchToolBar_LeftPanel.setVisible(true);
		this.toolBar_LeftPanel.add(new JLabel(Lang.getInstance().translate("Find Key")+":"));
    	key_Item = new JTextField();
    	key_Item.setToolTipText("");
    	key_Item.setAlignmentX(1.0F);
    	key_Item.setMinimumSize(new java.awt.Dimension(100, 20));
    	key_Item.setName(""); // NOI18N
    	key_Item.setPreferredSize(new java.awt.Dimension(100, 20));
    	key_Item.setMaximumSize(new java.awt.Dimension(2000, 20));
       	
    	MenuPopupUtil.installContextMenu(key_Item);
    	
    	this.toolBar_LeftPanel.add(key_Item);
    	key_Item.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				searchTextField_SearchToolBar_LeftPanel.setText("");
				
				
				Label_search_Info_Panel.setText(Lang.getInstance().translate("Waiting..."));
				jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
				
				
				
				
			
				
				new Thread() {
					@Override
					public void run() {
						search_Table_Model.Find_item_from_key(key_Item.getText());	
						if (search_Table_Model.getRowCount() < 1) {
							Label_search_Info_Panel.setText(Lang.getInstance().translate("Not Found Persons"));
							jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
							return;
						}
						jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(0, 0);
						// ddd.dispose();
						jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
					}
				}.start();
				
				
			}
    		
    	});
		
		
		
// not show My filter
		searth_My_JCheckBox_LeftPanel.setVisible(false);
		searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
	
//CREATE TABLE
		search_Table_Model = new TableModelPersons();
		search_Table = new MTable(this.search_Table_Model);
		TableColumnModel columnModel = search_Table.getColumnModel(); // read column model
		columnModel.getColumn(0).setMaxWidth((100));
//CHECKBOX FOR FAVORITE	
		TableColumn favorite_Column = search_Table.getColumnModel().getColumn(TableModelPersons.COLUMN_FAVORITE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
		favorite_Column.setMinWidth(50);
		favorite_Column.setMaxWidth(1000);
		favorite_Column.setPreferredWidth(50);
		// hand cursor  for Favorite column
		search_Table.addMouseMotionListener(new MouseMotionListener() {
		    public void mouseMoved(MouseEvent e) {
		       
		        if(search_Table.columnAtPoint(e.getPoint())==TableModelPersons.COLUMN_FAVORITE)
		        {
		     
		        	search_Table.setCursor(new Cursor(Cursor.HAND_CURSOR));
		        } else {
		        	search_Table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		        }
		    }

		    public void mouseDragged(MouseEvent e) {
		    }
		});
		
		search_Table_Model.addTableModelListener(new TableModelListener(){

			@Override
			public void tableChanged(TableModelEvent arg0) {
				// TODO Auto-generated method stub
				
				if (search_Table_Model.getRowCount() ==0){
					jScrollPane_jPanel_RightPanel.setViewportView(null);
					return;
				}
				if (selected_Item == 0 ) return;
				if (selected_Item + 1 > search_Table_Model.getRowCount()) selected_Item = search_Table_Model.getRowCount()-1;
				search_Table.setRowSelectionInterval(selected_Item, selected_Item);
				
			}
			
			
		});

//		TableColumn favoriteColumn = search_Table.getColumnModel().getColumn(search_Table_Model.COLUMN_BORN);	
//		favoriteColumn.setCellRenderer(new Renderer_Boolean()); 
	//	 int ss = search_Table_Model.getColumnName(search_Table_Model.COLUMN_BORN).length();
//		int rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth(search_Table_Model.getColumnName(search_Table_Model.COLUMN_BORN)));	
//		favoriteColumn.setMinWidth(rr+1);
//		favoriteColumn.setMaxWidth(rr*10);
//		favoriteColumn.setPreferredWidth(rr+5);
		//Sorter
		 search_Sorter = new TableRowSorter<TableModelPersons>(this.search_Table_Model);
		search_Table.setRowSorter(search_Sorter);	
	
// UPDATE FILTER ON TEXT CHANGE
	
		searchTextField_SearchToolBar_LeftPanel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				// GET VALUE
					String search = searchTextField_SearchToolBar_LeftPanel.getText();
					if (search.equals("")){jScrollPane_jPanel_RightPanel.setViewportView(null);
					search_Table_Model.clear();
					 Label_search_Info_Panel.setText(Lang.getInstance().translate("Enter more  2 characters"));
					 jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
					return;
				}
					if (search.length()<3) {
						Label_search_Info_Panel.setText(Lang.getInstance().translate("Enter more  2 characters"));
						 jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
						
						
						
						return;
					}
					key_Item.setText("");
					
					Label_search_Info_Panel.setText(Lang.getInstance().translate("Waiting..."));
					jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
					
					
					
					
					search_Table_Model.set_Filter_By_Name(search);
					
					new Thread() {
						@Override
						public void run() {
							search_Table_Model.set_Filter_By_Name(search);
							if (search_Table_Model.getRowCount() < 1) {
								Label_search_Info_Panel.setText(Lang.getInstance().translate("Not Found Persons"));
								jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
								return;
							}
							jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(0, 0);
							// ddd.dispose();
							jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
						}
					}.start();
			
			
					
					
					
				
					
					
					
				//	jScrollPanel_LeftPanel.setViewportView(null);
				//	jScrollPane_jPanel_RightPanel.setViewportView(null);
					
					
					// if (search.length()<3) return;
				
					// show message
					// jTable_jScrollPanel_LeftPanel.setVisible(false);//
					

					
			
			
			}
    		
    	});
		
// SET VIDEO			
		jTable_jScrollPanel_LeftPanel.setModel(this.search_Table_Model);
		jTable_jScrollPanel_LeftPanel = search_Table;
		jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
//		setRowHeightFormat(true);
// Event LISTENER		
		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
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
							row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
							 PersonCls asset = search_Table_Model.getPerson(row);
							favorite_set( jTable_jScrollPanel_LeftPanel);	
							
							
							
						}
						
						
					}
			     }
			});
			
		
		
		JPopupMenu menu = new JPopupMenu();
		menu.addAncestorListener(new AncestorListener(){

			

			@Override
			public void ancestorAdded(AncestorEvent arg0) {
				// TODO Auto-generated method stub
				row = search_Table.getSelectedRow();
				if (row < 1 ) {
				menu.disable();
			}
			
			row = search_Table.convertRowIndexToModel(row);
				
				
			}

			@Override
			public void ancestorMoved(AncestorEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void ancestorRemoved(AncestorEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
			
		});

		JMenuItem favorite = new JMenuItem(Lang.getInstance().translate("&&"));
		favorite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				favorite_set( jTable_jScrollPanel_LeftPanel);
				
			}
		});
	
    	    	
    	    	JMenuItem vsend_Coins_Item= new JMenuItem(Lang.getInstance().translate("Send Asset"));
    	    
    	    	vsend_Coins_Item.addActionListener(new ActionListener(){
    	  		@Override
    	    	public void actionPerformed(ActionEvent e) {
    	  			PersonCls person = search_Table_Model.getPerson(row);
    	  			Account_Send_Dialog fm = new Account_Send_Dialog(null,null,null, person);				
    				}});
    	    	
    	    	menu.add(vsend_Coins_Item);
    	    	JMenuItem send_Mail_Item= new JMenuItem(Lang.getInstance().translate("Send Mail"));
    	    	send_Mail_Item.addActionListener(new ActionListener(){
    	  		@Override
    	    	public void actionPerformed(ActionEvent e) {
    	   PersonCls person = search_Table_Model.getPerson(row);
    	  				Mail_Send_Dialog fm = new Mail_Send_Dialog(null,null,null, person);
    				}});
    	    	
    	    	menu.add(send_Mail_Item);
    	    	
    	    	
    	    	menu.addPopupMenuListener(new PopupMenuListener(){

    	    		@Override
    	    		public void popupMenuCanceled(PopupMenuEvent arg0) {
    	    			// TODO Auto-generated method stub
    	    			
    	    		}

    	    		@Override
    	    		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
    	    			// TODO Auto-generated method stub
    	    			
    	    		}

    	    		@Override
    	    		public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
    	    			// TODO Auto-generated method stub
    	    			
    	    			row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
    	    			row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
    	    			 PersonCls person = search_Table_Model.getPerson(row);
    	    			
    	    			//IF ASSET CONFIRMED AND NOT ERM
    	    			
    	    				favorite.setVisible(true);
    	    				//CHECK IF FAVORITES
    	    				if(Controller.getInstance().isItemFavorite(person))
    	    				{
    	    					favorite.setText(Lang.getInstance().translate("Remove Favorite"));
    	    				}
    	    				else
    	    				{
    	    					favorite.setText(Lang.getInstance().translate("Add Favorite"));
    	    				}
    	    				/*	
    	    				//this.favoritesButton.setPreferredSize(new Dimension(200, 25));
    	    				this.favoritesButton.addActionListener(new ActionListener()
    	    				{
    	    					public void actionPerformed(ActionEvent e)
    	    					{
    	    						onFavoriteClick();
    	    					}
    	    				});	
    	    				this.add(this.favoritesButton, labelGBC);
    	    				*/
    	    			
    	    		
    	    		
    	    		
    	    		
    	    		}
    	    		
    	    	}
    	    	
    	    	);
    	    	
    	    	
    	    	menu.add(favorite);
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
   	    	TableMenuPopupUtil.installContextMenu(jTable_jScrollPanel_LeftPanel, menu);
	}
// set favorite Search	
	void favorite_all(JTable personsTable){
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

		
		
	
// listener select row	 
	 class search_listener implements ListSelectionListener  {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				PersonCls person = null;
				if (search_Table.getSelectedRow() >= 0 ) person = search_Table_Model.getPerson(search_Table.convertRowIndexToModel(search_Table.getSelectedRow()));
				if (person == null) return;
					selected_Item = search_Table.getSelectedRow();
					Person_Info_002 info_panel = new Person_Info_002(person, true);
					info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
					jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
				
			}
		}

	 @Override
		public void delay_on_close(){
			// delete observer left panel
			// get component from right panel
			Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
			// if Person_Info 002 delay on close
			  if (c1 instanceof Person_Info_002) ( (Person_Info_002)c1).delay_on_Close();
			
		}
	 
	 public void favorite_set(JTable personsTable){


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
	

}
