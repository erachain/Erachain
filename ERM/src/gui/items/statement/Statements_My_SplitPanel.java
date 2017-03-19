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
import core.transaction.Transaction;
import gui.MainFrame;
	import gui.Main_Internal_Frame;
	import gui.RunMenu;
	import gui.Split_Panel;
	import gui.items.assets.IssueAssetPanel;
	import gui.items.assets.TableModelItemAssets;
import gui.library.MTable;
import gui.models.Renderer_Boolean;
	import gui.models.Renderer_Left;
	import gui.models.Renderer_Right;
	import gui.models.WalletItemAssetsTableModel;
	import gui.models.WalletItemPersonsTableModel;
	import lang.Lang;


	public class Statements_My_SplitPanel extends Split_Panel{
		private static final long serialVersionUID = 2717571093561259483L;

		
	
	//	private JTable my_Statements_table;
		private TableRowSorter my_Sorter;
		private RunMenu my_run_menu;
	// для прозрачности
	     int alpha =255;
	     int alpha_int;
	     Statements_Table_Model_My my_Statements_Model;
		
		
	public Statements_My_SplitPanel(){
	
		this.setName(Lang.getInstance().translate("My Statements"));
			this.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
			// not show buttons
			this.button1_ToolBar_LeftPanel.setVisible(false);
			this.button2_ToolBar_LeftPanel.setVisible(false);
			this.jButton1_jToolBar_RightPanel.setVisible(false);
			this.jButton2_jToolBar_RightPanel.setVisible(false);
			

			// not show My filter
			this.searth_My_JCheckBox_LeftPanel.setVisible(false);
			
			//TABLE
			
			
			
			 my_Statements_Model = new Statements_Table_Model_My();
		//	my_Statements_table = new JTable(my_Statements_Model);// new Statements_Table_Model();
	    	
		//	my_Statements_table.setTableHeader(null);
		//	my_Statements_table.setEditingColumn(0);
		//	my_Statements_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			
		/*	
			TableColumnModel columnModel = my_Statements_table.getColumnModel(); // read column model
			columnModel.getColumn(0).setMaxWidth((100));
			
			//Custom renderer for the String column;
			my_Statements_table.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
			my_Statements_table.setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
					
					
			my_Sorter = new TableRowSorter(my_PersonsModel);
			my_Statements_table.setRowSorter(my_Sorter);
			my_Statements_table.getRowSorter();
			if (my_PersonsModel.getRowCount() > 0) my_PersonsModel.fireTableDataChanged();
			
			//CHECKBOX FOR CONFIRMED
			TableColumn confirmedColumn = my_Statements_table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_CONFIRMED);
			// confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			confirmedColumn.setCellRenderer(new Renderer_Boolean());
			confirmedColumn.setMinWidth(50);
			confirmedColumn.setMaxWidth(50);
			confirmedColumn.setPreferredWidth(50);//.setWidth(30);
			
			
			//CHECKBOX FOR FAVORITE
			TableColumn favoriteColumn = my_Statements_table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_FAVORITE);
			//favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			favoriteColumn.setCellRenderer(new Renderer_Boolean());
			favoriteColumn.setMinWidth(50);
			favoriteColumn.setMaxWidth(50);
			favoriteColumn.setPreferredWidth(50);//.setWidth(30);
	
			// UPDATE FILTER ON TEXT CHANGE
			this.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new My_Search());
			*/		// SET VIDEO			
			//this.jTable_jScrollPanel_LeftPanel.setModel(my_PersonsModel);
			this.jTable_jScrollPanel_LeftPanel = new MTable(my_Statements_Model); //my_Statements_table;
			//this.jTable_jScrollPanel_LeftPanel.setTableHeader(null);
	
			this.jTable_jScrollPanel_LeftPanel.setEditingColumn(0);
			this.jTable_jScrollPanel_LeftPanel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.jTable_jScrollPanel_LeftPanel.setAutoCreateRowSorter(true);
			this.jScrollPanel_LeftPanel.setViewportView(this.jTable_jScrollPanel_LeftPanel);		
	//		this.setRowHeightFormat(false);
			
			
			 
			// EVENTS on CURSOR
			this.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new My_Tab_Listener());
			
			this.jTable_jScrollPanel_LeftPanel.addMouseListener(new My_Mouse());
			my_run_menu  = new RunMenu();
			Dimension dim1 = new Dimension(180,50);
			my_run_menu.setSize(dim1);
			my_run_menu.setPreferredSize(dim1);
			my_run_menu.setVisible(false);
			my_run_menu.jButton1.setFocusPainted(true);
			my_run_menu.jButton1.setFocusCycleRoot(true);
			my_run_menu.jButton1.addActionListener(new My_run_menu_Button1_Action());
			my_run_menu.pack();
			my_run_menu.addWindowFocusListener(new My_run_Menu_Focus_Listener());

			 Dimension size = MainFrame.desktopPane.getSize();
			 this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
			 jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
			
		  
		    
		}
	

	
	
	// set favorine My
		void favorite_my(JTable table){
			int row = table.getSelectedRow();
			row = table.convertRowIndexToModel(row);
/*
			PersonCls person = my_PersonsModel.getItem(row);
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
					

				table.repaint();
*/
		}

		
	
	
	// listener search_tab run menu focus
		class My_run_Menu_Focus_Listener implements WindowFocusListener{
			@Override
			public void windowGainedFocus(WindowEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void windowLostFocus(WindowEvent arg0) {
				// TODO Auto-generated method stub
				my_run_menu.setVisible(false);
			}
		};

		class My_run_menu_Button1_Action implements ActionListener{
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
	/*			favorite_my(my_Statements_table);
				int row = my_Statements_table.getSelectedRow();
				row = my_Statements_table.convertRowIndexToModel(row);
				PersonCls person = my_PersonsModel.getItem(row);
				if(Controller.getInstance().isItemFavorite(person))
				{
					my_run_menu.jButton1.setText(Lang.getInstance().translate("Remove Favorite"));
				}
				else
				{
					my_run_menu.jButton1.setText(Lang.getInstance().translate("Add Favorite"));
				}
			
	*/		
			}
		
		};

		class My_Mouse extends MouseAdapter {
			@Override
			public void mousePressed(MouseEvent e) {
		/*		Point p = e.getPoint();
				int row = my_Statements_table.rowAtPoint(p);
				row = my_Statements_table.convertRowIndexToModel(row);
				PersonCls person = my_PersonsModel.getItem(row);
				if(e.getClickCount() == 2)
				{
					
				}
			
				if(e.getClickCount() == 1 & e.getButton() == e.BUTTON1)
				{
					if(Controller.getInstance().isItemFavorite(person))
					{
						my_run_menu.jButton1.setText(Lang.getInstance().translate("Remove Favorite"));
					}
					else
					{
						my_run_menu.jButton1.setText(Lang.getInstance().translate("Add Favorite"));
					}
					my_run_menu.setLocation(e.getXOnScreen(), e.getYOnScreen());
					my_run_menu.setVisible(true);	
				}
		*/	}
		}

		class My_Tab_Listener implements ListSelectionListener {
			
			//@SuppressWarnings("deprecation")
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				
				 
				Transaction statement = null;
				if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 )
					statement =  my_Statements_Model.get_Statement(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
				
				if (statement == null)
					return;
				
				Statement_Info info_panel = new Statement_Info(statement);
				info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
				jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
			}
			
		}
		
		class My_Search implements DocumentListener {
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
	/*			String search = searchTextField_SearchToolBar_LeftPanel.getText();
				// SET FILTER
				my_PersonsModel.fireTableDataChanged();
			
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter)  my_Sorter).setRowFilter(filter);
					
				my_PersonsModel.fireTableDataChanged();
*/
			}
		}
		
		
	
	}




