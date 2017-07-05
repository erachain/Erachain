	package gui.items.persons;

	import java.awt.Component;
	import java.awt.Cursor;
	import java.awt.Dimension;
	import java.awt.Point;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	import java.awt.event.MouseAdapter;
	import java.awt.event.MouseEvent;
	import java.awt.event.MouseMotionListener;
	import javax.swing.JDialog;
	import javax.swing.JMenuItem;
	import javax.swing.JPopupMenu;
	import javax.swing.JTable;
	import javax.swing.RowSorter;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
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
	import core.item.persons.PersonCls;
import gui.MainFrame;
import gui.Split_Panel;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import gui.library.MTable;
import lang.Lang;
import utils.TableMenuPopupUtil;


	public class Persons_Favorite_SplitPanel extends Split_Panel{
		private static final long serialVersionUID = 2717571093561259483L;

		
		private Persons_Favorite_TableModel search_Table_Model;
		private MTable<?, ?> search_Table;
	
		private RowSorter<Persons_Favorite_TableModel> search_Sorter;
		
	// для прозрачности
	     int alpha =255;
	     int alpha_int;


		protected int row;
		
		
	public Persons_Favorite_SplitPanel(){
		super("Persons_Favorite_SplitPanel");
	
		this.setName(Lang.getInstance().translate("Favorite Persons"));
			this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
			// not show buttons
		

			
		
			
			
		
			// not show buttons
				jToolBar_RightPanel.setVisible(false);
				toolBar_LeftPanel.setVisible(true);
				button2_ToolBar_LeftPanel.setVisible(false);
				button1_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Search Persons"));
				button1_ToolBar_LeftPanel.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						JDialog dd = new JDialog(MainFrame.getInstance());
						dd.setModal(true);
						dd.add(new Persons_Search_SplitPanel());
						dd.setPreferredSize(new Dimension(MainFrame.getInstance().getWidth()-100, MainFrame.getInstance().getHeight()-100));
					
						dd.pack();
						//	this.setSize( size.width-(size.width/8), size.height-(size.width/8));
							
							dd.setResizable(true);
							dd.setSize(MainFrame.getInstance().getWidth()-300, MainFrame.getInstance().getHeight()-300);
							dd.setLocationRelativeTo(MainFrame.getInstance());
							dd.setVisible(true);
					
					
					}
					
					
				});
				
				
			
			
		// not show My filter
				searth_My_JCheckBox_LeftPanel.setVisible(false);
				searth_Favorite_JCheckBox_LeftPanel.setVisible(false);
			
		//CREATE TABLE
				search_Table_Model = new Persons_Favorite_TableModel();
				search_Table = new MTable(this.search_Table_Model);
				TableColumnModel columnModel = search_Table.getColumnModel(); // read column model
				columnModel.getColumn(0).setMaxWidth((100));
		//CHECKBOX FOR FAVORITE	
				TableColumn favorite_Column = search_Table.getColumnModel().getColumn(Persons_Favorite_TableModel.COLUMN_FAVORITE);//.COLUMN_KEY);//.COLUMN_CONFIRMED);
				favorite_Column.setMinWidth(50);
				favorite_Column.setMaxWidth(1000);
				favorite_Column.setPreferredWidth(50);
				// hand cursor  for Favorite column
				search_Table.addMouseMotionListener(new MouseMotionListener() {
				    public void mouseMoved(MouseEvent e) {
				       
				        if(search_Table.columnAtPoint(e.getPoint())==Persons_Favorite_TableModel.COLUMN_FAVORITE)
				        {
				     
				        	search_Table.setCursor(new Cursor(Cursor.HAND_CURSOR));
				        } else {
				        	search_Table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				        }
				    }

				    public void mouseDragged(MouseEvent e) {
				    }
				});
				
				

//				TableColumn favoriteColumn = search_Table.getColumnModel().getColumn(search_Table_Model.COLUMN_BORN);	
//				favoriteColumn.setCellRenderer(new Renderer_Boolean()); 
			//	 int ss = search_Table_Model.getColumnName(search_Table_Model.COLUMN_BORN).length();
//				int rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth(search_Table_Model.getColumnName(search_Table_Model.COLUMN_BORN)));	
//				favoriteColumn.setMinWidth(rr+1);
//				favoriteColumn.setMaxWidth(rr*10);
//				favoriteColumn.setPreferredWidth(rr+5);
				//Sorter
				 search_Sorter = new TableRowSorter<Persons_Favorite_TableModel>(this.search_Table_Model);
				search_Table.setRowSorter(search_Sorter);	
			
		// UPDATE FILTER ON TEXT CHANGE
				searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener( new search_tab_filter());
		// SET VIDEO			
				jTable_jScrollPanel_LeftPanel.setModel(this.search_Table_Model);
				jTable_jScrollPanel_LeftPanel = search_Table;
				jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
//				setRowHeightFormat(true);
		// Event LISTENER		
				jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());
			
				
				jTable_jScrollPanel_LeftPanel.addAncestorListener(new AncestorListener(){

					@Override
					public void ancestorAdded(AncestorEvent arg0) {
						// TODO Auto-generated method stub
						search_Table_Model.addObservers();
					}

					@Override
					public void ancestorMoved(AncestorEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void ancestorRemoved(AncestorEvent arg0) {
						// TODO Auto-generated method stub
						search_Table_Model.removeObservers();
					}
					
					
					
				});
				
				jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() 
					{
						@Override
						public void mousePressed(MouseEvent e) 
						{
							Point p = e.getPoint();
							row = jTable_jScrollPanel_LeftPanel.rowAtPoint(p);
				//			jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(row, row);
							
							
							if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
							{
								
								if (jTable_jScrollPanel_LeftPanel.getSelectedColumn() == Persons_Favorite_TableModel.COLUMN_FAVORITE){
									row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
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
		    	  			PersonCls person = search_Table_Model.getItem(row);
		    	  			new Account_Send_Dialog(null,null,null, person);				
		    				}});
		    	    	
		    	    	menu.add(vsend_Coins_Item);
		    	    	JMenuItem send_Mail_Item= new JMenuItem(Lang.getInstance().translate("Send Mail"));
		    	    	send_Mail_Item.addActionListener(new ActionListener(){
		    	  		@Override
		    	    	public void actionPerformed(ActionEvent e) {
		    	  			PersonCls person = search_Table_Model.getItem(row);
		    	  				 new Mail_Send_Dialog(null,null,null, person);
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
		    	    			 PersonCls person = search_Table_Model.getItem(row);
		    	    			
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
				PersonCls person = search_Table_Model.getItem(row);
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
						
			//			search_Table_Model.set_Filter_By_Name(search);
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
						if (search_Table.getSelectedRow() >= 0 ) person = search_Table_Model.getItem(search_Table.convertRowIndexToModel(search_Table.getSelectedRow()));
						if (person == null) return;
							Person_Info_002 info_panel = new Person_Info_002(person, true);
							info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
							jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
						
					}
				}

			 @Override
				public void delay_on_close(){
					// delete observer left panel
				// search_Table_Model.removeObservers();
					// get component from right panel
					Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
					// if Person_Info 002 delay on close
					  if (c1 instanceof Person_Info_002) ( (Person_Info_002)c1).delay_on_Close();
					
				}
			 
			 public void favorite_set(JTable personsTable){

				 PersonCls person = search_Table_Model.getItem(row);
				 //new AssetPairSelect(asset.getKey());

				
				 	//CHECK IF FAVORITES
				 	if(Controller.getInstance().isItemFavorite(person))
				 	{
				 	//select row in table	
				 		row = personsTable.getSelectedRow();
				 		Controller.getInstance().removeItemFavorite(person);
				 		if (search_Table_Model.getRowCount() == 0)  return;
				 		if (row > 0)	personsTable.addRowSelectionInterval(row-1,row-1);
				 		else personsTable.addRowSelectionInterval(0,0);
				 		
				 	}
				 	else
				 	{
				 		
				 		Controller.getInstance().addItemFavorite(person);
				 	}
				 		

				 	personsTable.repaint();

				 
				 }
			

		}
