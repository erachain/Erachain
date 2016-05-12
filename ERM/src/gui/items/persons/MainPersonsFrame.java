package gui.items.persons;

import java.awt.Dimension;

import javax.swing.DefaultRowSorter;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import core.item.persons.PersonCls;
import gui.Frame_All;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import gui.models.Renderer_Right;
import lang.Lang;

public class MainPersonsFrame extends Main_Internal_Frame{ //Frame_All{ //JInternalFrame {
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private TableModelPersons tableModelPersons;

public MainPersonsFrame(){

	Split_Panel search_Person_SplitPanel = new Split_Panel();
	search_Person_SplitPanel.setName("Search Persons");
	
	
	Split_Panel my_Person_SplitPanel = new Split_Panel();
	my_Person_SplitPanel.setName("My Persons");
	
	this.jTabbedPane.add(search_Person_SplitPanel);
	this.jTabbedPane.add(my_Person_SplitPanel);
	//Frame_All frame = new Frame_All();
/*	
	this.setTitle(Lang.getInstance().translate("Persons"));
	this.search_Label_Panel1_Tabbed_Panel_Left_Panel.setText(Lang.getInstance().translate("Search") +":"); 
	this.search_Label_Panel2_Tabbed_Panel_Left_Panel.setText(Lang.getInstance().translate("Search") +":");
	this.Search_jLabel.setText(Lang.getInstance().translate("Search") +":");
	

	
	
	
	//CREATE TABLE
	this.tableModelPersons = new TableModelPersons();
	final JTable personsTable = new JTable(this.tableModelPersons);
	TableColumnModel columnModel = personsTable.getColumnModel(); // read column model
	columnModel.getColumn(0).setMaxWidth((100));
//Custom renderer for the String column;
	personsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
	personsTable.setDefaultRenderer(String.class, new Renderer_Right()); // set renderer
//CHECKBOX FOR FAVORITE
	TableColumn favoriteColumn = personsTable.getColumnModel().getColumn(TableModelPersons.COLUMN_FAVORITE);
	favoriteColumn.setCellRenderer(personsTable.getDefaultRenderer(Boolean.class));
//Sorter
	RowSorter sorter =   new TableRowSorter(this.tableModelPersons);
	personsTable.setRowSorter(sorter);	
	// UPDATE FILTER ON TEXT CHANGE
			this.search_TextField_Panel2_Tabbed_Panel_Left_Panel.getDocument().addDocumentListener(new DocumentListener() {
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
					String search = search_TextField_Panel2_Tabbed_Panel_Left_Panel.getText();

				 	// SET FILTER
			//		tableModelPersons.getSortableList().setFilter(search);
					tableModelPersons.fireTableDataChanged();
					
					RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
					((DefaultRowSorter) sorter).setRowFilter(filter);
					
					tableModelPersons.fireTableDataChanged();
					
				}
			});
	
	
	
	

// set video			
			jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setModel(this.tableModelPersons);
			jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel = personsTable;
			jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.setViewportView(jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel); // personsTable; 
// select row table persons
			 Person_Info info = new Person_Info(); 
	//		
// обработка изменения положения курсора в таблице
			 jTable_jScrollPanel_Panel2_Tabbed_Panel_Left_Panel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
				@SuppressWarnings("deprecation")
				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					PersonCls person = null;
					if (personsTable.getSelectedRow() >= 0 ) person = tableModelPersons.getPerson(personsTable.convertRowIndexToModel(personsTable.getSelectedRow()));
					info.show_001(person);
					
					jSplitPane2.setDividerLocation(jSplitPane2.getDividerLocation());	
				}
			});
	
	
	
	
//	Person_Info info = new Person_Info();
//	info.show_001(null);
	//this.Panel_Right_Panel.add(info);
	 this.jScrollPane_Panel_Right_Panel.setViewportView(info);
	
	
	
	
	
	
	
	this.jSplitPane2.setDividerLocation(700);
*/	
	this.pack();
	this.setSize(800,600);
	this.setMaximizable(true);
	
	this.setClosable(true);
	this.setResizable(true);
//	this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
	this.setLocation(20, 20);
//	this.setIconImages(icons);
	//CLOSE
	setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    this.setResizable(true);
//    splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
    //my_person_panel.requestFocusInWindow();
    this.setVisible(true);
	
}

}
