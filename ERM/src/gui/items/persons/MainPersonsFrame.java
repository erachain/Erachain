package gui.items.persons;


import java.awt.Dimension;
import javax.swing.DefaultRowSorter;
import javax.swing.JInternalFrame;
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


public class MainPersonsFrame extends Main_Internal_Frame{
	private static final long serialVersionUID = 2717571093561259483L;

	private WalletItemPersonsTableModel personsModel;
	Split_Panel search_Person_SplitPanel;
	JTable personsTable;
	RowSorter<?> search_Sorter;
	JTable table_My;
	TableRowSorter<?> sorter_My;
	
	Split_Panel my_Person_SplitPanel;
	
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
		  
// insert Person
		  
		  JScrollPane insert_Person_Panel = new JScrollPane();
		  insert_Person_Panel.setName(Lang.getInstance().translate("Insert Person"));
		  insert_Person_Panel.add(new InsertPersonPanel());
		  insert_Person_Panel.setViewportView(new InsertPersonPanel());
		  
		  
		  
	
///////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
		
		this.jTabbedPane.add(my_Person_SplitPanel);
		
		this.jTabbedPane.add(search_Person_SplitPanel);
		this.jTabbedPane.add(Issue_Person_Panel);
		this.jTabbedPane.add(insert_Person_Panel);
		
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
	    search_Person_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/3));
	 	my_Person_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/3));
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
			
			PersonCls person = null;
			if (table_My.getSelectedRow() >= 0 )person = personsModel.getItem(table_My.convertRowIndexToModel(table_My.getSelectedRow()));
			//info1.show_001(person);
			
			// PersJSpline.setDividerLocation(PersJSpline.getDividerLocation());
			//my_Person_SplitPanel.jSplitPanel.setDividerLocation(my_Person_SplitPanel.jSplitPanel.getDividerLocation());	
			////my_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
			
			Person_Info_002 info_panel = new Person_Info_002(person, false);
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


