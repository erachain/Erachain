	package gui.items.statement;

	import java.awt.Dimension;
	import javax.swing.JTable;
	import javax.swing.ListSelectionModel;
	import javax.swing.event.DocumentEvent;
	import javax.swing.event.DocumentListener;
	import javax.swing.event.ListSelectionEvent;
	import javax.swing.event.ListSelectionListener;
import core.transaction.Transaction;
	import gui.Split_Panel;
import gui.library.MTable;
	import lang.Lang;


	public class Statements_My_SplitPanel extends Split_Panel{
		private static final long serialVersionUID = 2717571093561259483L;

		
	
	// для прозрачности
	     int alpha =255;
	     int alpha_int;
	     Statements_Table_Model_My my_Statements_Model;
		
		
	public Statements_My_SplitPanel(){
		super("Statements_My_SplitPanel");
	
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
		// EVENTS on CURSOR
			this.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new My_Tab_Listener());
//			 Dimension size = MainFrame.getInstance().desktopPane.getSize();
//			 this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
			// jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
	}
	// set favorine My
		void favorite_my(JTable table){
			int row = table.getSelectedRow();
			row = table.convertRowIndexToModel(row);
		}
		class My_Tab_Listener implements ListSelectionListener {
			
			//@SuppressWarnings("deprecation")
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				
				 
				Transaction statement = null;
				if (jTable_jScrollPanel_LeftPanel.getSelectedRow() >= 0 )
					statement =  my_Statements_Model.get_Statement(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
				
				if (statement == null)	return;
				Statement_Info info_panel = new Statement_Info(statement);
				info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,jScrollPane_jPanel_RightPanel.getSize().height-50));
				jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
			//	jSplitPanel.setRightComponent(info_panel);
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
	
			}
		}
		
		
	
	}




