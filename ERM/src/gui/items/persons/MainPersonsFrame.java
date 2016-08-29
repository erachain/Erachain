package gui.items.persons;

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


public class MainPersonsFrame extends Main_Internal_Frame{
	private static final long serialVersionUID = 2717571093561259483L;

	private TableModelPersons tableModelPersons;
	private WalletItemPersonsTableModel personsModel;
	Split_Panel search_Person_SplitPanel;
	JTable personsTable;
	RowSorter search_Sorter;
	JTable table_My;
	TableRowSorter sorter_My;
	
	Split_Panel my_Person_SplitPanel;
	
// для всплывающего меню	
	 private JPopupMenu mouseMenu;
     private JPanel imagePanel;
     private GridLayout grid;
     
     RunMenu Search_run_menu;
     RunMenu my_run_menu;
// для прозрачности
     int alpha =255;
     int alpha_int;
	
	
	public MainPersonsFrame(){
	
		// not show buttons main Toolbar
		this.setTitle(Lang.getInstance().translate("Persons"));
		this.jToolBar.setVisible(false);
	
	
		///////////////////////
		// ALL PERSONS
		///////////////////////
		
		search_Person_SplitPanel = new Persons_Search_SplitPanel();
		
	 
		//////////////////////////////////////	
		// MY PERSONS
		//////////////////////////////////////
		my_Person_SplitPanel = new Persons_My_SplitPanel();
	
		
// issue Person
		  JScrollPane Issue_Person_Panel = new JScrollPane();
		  Issue_Person_Panel.setName(Lang.getInstance().translate("Issue Person"));
		  Issue_Person_Panel.add(new IssuePersonPanel());
		  Issue_Person_Panel.setViewportView(new IssuePersonPanel());
	
///////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
		
		this.jTabbedPane.add(my_Person_SplitPanel);
		
		this.jTabbedPane.add(search_Person_SplitPanel);
		this.jTabbedPane.add(Issue_Person_Panel);
		
		this.pack();
		//	this.setSize(800,600);
		this.setMaximizable(true);
		
		this.setClosable(true);
		this.setResizable(true);
		this.setLocation(20, 20);
		//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
	    this.setResizable(true);
	    this.setVisible(true);
	    Dimension size = MainFrame.desktopPane.getSize();
	    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	    search_Person_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	 	my_Person_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	}

// set favorine My
	void favorite_my(JTable table){
		int row = table.getSelectedRow();
		row = table.convertRowIndexToModel(row);

		PersonCls person = personsModel.getItem(row);
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

	}
	


// listener my_tab run menu focus
	class run_Menu_My_Focus_Listener implements WindowFocusListener{
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

	class Search_run_menu_Button1_Action implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			favorite_my(table_My);
			int row = table_My.getSelectedRow();
			row = table_My.convertRowIndexToModel(row);
			PersonCls person = personsModel.getItem(row);
			if(Controller.getInstance().isItemFavorite(person))
			{
				my_run_menu.jButton1.setText(Lang.getInstance().translate("Remove Favorite"));
			}
			else
			{
				my_run_menu.jButton1.setText(Lang.getInstance().translate("Add Favorite"));
			}
		
		
		}
	
	};

	class My_Mouse extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			int row = table_My.rowAtPoint(p);
			row = table_My.convertRowIndexToModel(row);
			PersonCls person = personsModel.getItem(row);
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
		}
	}

	class My_Tab_Listener implements ListSelectionListener {
		
		//@SuppressWarnings("deprecation")
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			
			PersonCls person = null;
			if (table_My.getSelectedRow() >= 0 )person = personsModel.getItem(table_My.convertRowIndexToModel(table_My.getSelectedRow()));
			//info1.show_001(person);
			
			// PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
			//my_Person_SplitPanel.jSplitPanel.setDividerLocation(my_Person_SplitPanel.jSplitPanel.getDividerLocation());	
			////my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
			
			Person_info_panel_001 info_panel = new Person_info_panel_001(person, false);
			info_panel.setPreferredSize(new Dimension(search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.getSize().width-50,search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.getSize().height-50));
			my_Person_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
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
			String search = my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();
			// SET FILTER
			personsModel.fireTableDataChanged();
		
			RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
			((DefaultRowSorter)  sorter_My).setRowFilter(filter);
				
			personsModel.fireTableDataChanged();

		}
	}
	
	
	
	
}


