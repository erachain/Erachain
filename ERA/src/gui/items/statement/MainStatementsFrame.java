package gui.items.statement;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.DefaultRowSorter;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.item.persons.PersonCls;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import gui.models.WalletItemPersonsTableModel;
import lang.Lang;


public class MainStatementsFrame extends Main_Internal_Frame{
	private static final long serialVersionUID = 2717571093561259483L;

//	private TableModelPersons tableModelPersons;
	private WalletItemPersonsTableModel personsModel;
	Split_Panel search_Statements_SplitPanel;
	JTable personsTable;
	RowSorter<?> search_Sorter;
	JTable table_My;
	TableRowSorter<?> sorter_My;
	
	Split_Panel my_Statements_SplitPanel;
	
// для всплывающего меню	
	 private JPopupMenu mouseMenu;
     private JPanel imagePanel;
     private GridLayout grid;
     
   
// для прозрачности
     int alpha =255;
     int alpha_int;

	private Issue_Document_Panel iss2;
	
	
	public MainStatementsFrame(){
	
		// not show buttons main Toolbar
		this.setTitle(Lang.getInstance().translate("Statements"));
		this.jToolBar.setVisible(false);
	
		//this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with Statements"));
		///////////////////////
		// ALL StatementS
		///////////////////////
		
		search_Statements_SplitPanel = new Statements_Search_SplitPanel();
		
	 
		//////////////////////////////////////	
		// MY StatementS
		//////////////////////////////////////
		my_Statements_SplitPanel = new Statements_My_SplitPanel();
	
		
// issue Person
		 Issue_Statement_Panel Issue_Statement_Panel = new Issue_Statement_Panel(null, null);
		  Issue_Statement_Panel.setName(Lang.getInstance().translate("Issue Statement"));
		  
	
// issue2
		  iss2 = new Issue_Document_Panel();
		  iss2.setName(Lang.getInstance().translate("Issue Statement"));
///////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
		
		this.jTabbedPane.add(my_Statements_SplitPanel);
		
		this.jTabbedPane.add(search_Statements_SplitPanel);
		this.jTabbedPane.add(Issue_Statement_Panel);
		this.jTabbedPane.add(iss2);
		
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
	    Dimension size = MainFrame.getInstance().desktopPane.getSize();
	    this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
	    search_Statements_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	 	my_Statements_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/2));
	 	iss2.sp_pan.setDividerLocation((int)(size.getWidth()/2));
	 	
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

	
	class My_Tab_Listener implements ListSelectionListener {
		
		//@SuppressWarnings("deprecation")
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			
			if (table_My.getSelectedRow() >= 0 )
			 {
			}
			
			// PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
			//my_Person_SplitPanel.jSplitPanel.setDividerLocation(my_Person_SplitPanel.jSplitPanel.getDividerLocation());	
			////my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
			
	//		Person_info_panel_001 info_panel = new Person_info_panel_001(person, false);
	//		info_panel.setPreferredSize(new Dimension(search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.getSize().width-50,search_Person_SplitPanel.jScrollPane_jPanel_RightPanel.getSize().height-50));
	//		my_Person_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
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
			String search = my_Statements_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();
			// SET FILTER
			personsModel.fireTableDataChanged();
		
			RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
			((DefaultRowSorter)  sorter_My).setRowFilter(filter);
				
			personsModel.fireTableDataChanged();

		}
	}
	
	
	
	
}


