package gui.items.records;

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

	import controller.Controller;
	import core.item.assets.AssetCls;
	import core.item.persons.PersonCls;
	import gui.MainFrame;
	import gui.Main_Internal_Frame;
	import gui.RunMenu;
	import gui.Split_Panel;
	import gui.items.assets.IssueAssetPanel;
	import gui.items.assets.TableModelItemAssets;
	import gui.models.Renderer_Boolean;
	import gui.models.Renderer_Left;
	import gui.models.Renderer_Right;
	import gui.models.WalletItemAssetsTableModel;
	import gui.models.WalletItemPersonsTableModel;
	import lang.Lang;


	public class Persons_Search_SplitPanel extends Split_Panel{
	
		private static final long serialVersionUID = 2717571093561259483L;

		private TableModelPersons search_Table_Model;
		private JTable search_Table;
		private RowSorter<TableModelPersons> search_Sorter;
		private RunMenu Search_run_menu;
// для прозрачности
	     int alpha =255;
	     int alpha_int;
		
		
		public Persons_Search_SplitPanel(){
		
			
			
			setName(Lang.getInstance().translate("Search Persons"));
			searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
			
		// not show buttons
			jToolBar_RightPanel.setVisible(false);
			toolBar_LeftPanel.setVisible(false);
			
	// not show My filter
			searth_My_JCheckBox_LeftPanel.setVisible(false);
			
	//CREATE TABLE
			search_Table_Model = new TableModelPersons();
			search_Table = new JTable(this.search_Table_Model);
			TableColumnModel columnModel = search_Table.getColumnModel(); // read column model
			columnModel.getColumn(0).setMaxWidth((100));
		
	//Custom renderer for the String column;
			search_Table.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
			search_Table.setDefaultRenderer(String.class, new Renderer_Left(search_Table.getFontMetrics(search_Table.getFont()),search_Table_Model.get_Column_AutoHeight())); // set renderer
		
	//CHECKBOX FOR FAVORITE
			TableColumn favoriteColumn = search_Table.getColumnModel().getColumn(TableModelPersons.COLUMN_FAVORITE);	
			favoriteColumn.setCellRenderer(new Renderer_Boolean()); 
			favoriteColumn.setMinWidth(50);
			favoriteColumn.setMaxWidth(50);
			favoriteColumn.setPreferredWidth(50);
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
		
			search_Table.addMouseListener( new search_Mouse());
				
			
			Timer timer = new Timer( 200, new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					
					
					if (alpha <50) {
						
						Search_run_menu.setVisible(false);
						alpha = 50;
					}
			//	Search_run_menu.setBackground(new Color(0,204,102,alpha));	
			//		Search_run_menu.jButton1.setForeground(new Color(0,0,0,alpha));
			//		Search_run_menu.jButton2.setForeground(new Color(0,0,0,alpha));
			//		Search_run_menu.jButton3.setForeground(new Color(0,0,0,alpha));
			//		Search_run_menu.jButton1.setBackground( new Color(212,208,200,alpha));
					alpha = alpha - alpha_int;
					
					
					
					
				}
				
			});
				   

				timer.start();
			
			
			
				 

			Search_run_menu  = new RunMenu();
			
			Search_run_menu.setUndecorated(true);
		//	Search_run_menu.setBackground(new Color(0,204,102,255));
		//	Dimension dim = new Dimension(180,70);
	    //	Search_run_menu.setSize(dim);
	    	Search_run_menu.setPreferredSize(new Dimension(180,70));
	    	Search_run_menu.setVisible(false);
	    	Search_run_menu.jButton1.setText(Lang.getInstance().translate("Set Status"));
	   // 	aaa.jButton1.setBorderPainted(false);
	  //  	Search_run_menu.jButton1.setFocusPainted(true);
	 //  	Search_run_menu.jButton1.setFocusCycleRoot(true);
		Search_run_menu.jButton1.setContentAreaFilled(false);
		Search_run_menu.jButton1.setOpaque(false);
//		Search_run_menu.jButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
	    	Search_run_menu.jButton1.addActionListener(new ActionListener(){
	  		@Override
	    	public void actionPerformed(ActionEvent e) {
	   
	  		  	@SuppressWarnings("unused")
				PersonSetStatusDialog fm = new PersonSetStatusDialog( search_Table_Model.getPerson(search_Table.convertRowIndexToModel(search_Table.getSelectedRow())));	
	    	}});
	    	   	
	    	
	    	Search_run_menu.jButton2.setText(Lang.getInstance().translate("Confirm"));
	    	Search_run_menu.jButton2.setContentAreaFilled(false);
	    	Search_run_menu.jButton2.setOpaque(false);
	    	Search_run_menu.getContentPane().add(Search_run_menu.jButton2);
	    	Search_run_menu.jButton2.addActionListener(new ActionListener(){
	  		@Override
	    	public void actionPerformed(ActionEvent e) {
	   
	  
	    		@SuppressWarnings("unused")
				PersonConfirmDialog fm = new PersonConfirmDialog(search_Table_Model.getPerson(search_Table.convertRowIndexToModel(search_Table.getSelectedRow())));		
	    		}});
	 
	    	Search_run_menu.jButton3.setContentAreaFilled(false);
	  //  	Search_run_menu.jButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
	    	Search_run_menu.jButton3.setOpaque(false);
	    	Search_run_menu.getContentPane().add(Search_run_menu.jButton3);
	    	Search_run_menu.jButton3.addActionListener(new  ActionListener(){
	// вычисляем устанавливаем\ сбрасываем флажек выбранные
				@Override
				public void actionPerformed(ActionEvent e) {
					favorite_all(search_Table);
					alpha = 200;
					int row = search_Table.getSelectedRow();
					row = search_Table.convertRowIndexToModel(row);
					PersonCls person = search_Table_Model.getPerson(row);	
					if(Controller.getInstance().isItemFavorite(person))
					{
						Search_run_menu.jButton3.setText(Lang.getInstance().translate("Remove Favorite"));
					}
					else
					{
						Search_run_menu.jButton3.setText(Lang.getInstance().translate("Add Favorite"));
					}
				
				
				}
	    	
	    	});
	   
	    	Search_run_menu.pack();
	    
	    	Search_run_menu.addWindowFocusListener( new run_Menu_Search_Focus_Listener());
	 
		 
			
	
		   
	
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
					PersonCls person = null;
					if (search_Table.getSelectedRow() >= 0 ) person = search_Table_Model.getPerson(search_Table.convertRowIndexToModel(search_Table.getSelectedRow()));
					Person_info_panel_001 info_panel = new Person_info_panel_001(person, false);
					info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
					jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
				}
			}
	// mouse listener		
		class  search_Mouse extends MouseAdapter {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = search_Table.rowAtPoint(p);
				if(e.getClickCount() == 2)
				{
		//			row = personsTable.convertRowIndexToModel(row);
		//			PersonCls person = tableModelPersons.getPerson(row);
		//			new PersonFrame(person);
					
				}
			
			//	if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
					if( e.getButton() == MouseEvent.BUTTON1)
				{
					
					
					row = search_Table.convertRowIndexToModel(row);
					PersonCls person = search_Table_Model.getPerson(row);	
	//выводим меню всплывающее
					if(Controller.getInstance().isItemFavorite(person))
					{
						Search_run_menu.jButton3.setText(Lang.getInstance().translate("Remove Favorite"));
					}
					else
					{
						Search_run_menu.jButton3.setText(Lang.getInstance().translate("Add Favorite"));
					}
		//			alpha = 255;
					alpha_int = 5;
					Search_run_menu.setBackground(new Color(1,204,102,255));		
				    Search_run_menu.setLocation(e.getXOnScreen(), e.getYOnScreen());
				    Search_run_menu.repaint();
			        Search_run_menu.setVisible(true);		
		    
			    
			
				}
			}
			}
	

	

		
		
		class run_Menu_Search_Focus_Listener implements WindowFocusListener{
			@Override
			public void windowGainedFocus(WindowEvent arg0) {
				alpha = 255;
			}
			@Override
			public void windowLostFocus(WindowEvent arg0) {
				Search_run_menu.setVisible(false);
			}
		};
		
	}




