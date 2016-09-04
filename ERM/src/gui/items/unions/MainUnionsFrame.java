package gui.items.unions;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import javax.swing.DefaultRowSorter;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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

import core.item.unions.UnionCls;
import gui.MainFrame;
import gui.Main_Internal_Frame;
import gui.Split_Panel;
import gui.items.statuses.IssueStatusPanel;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;
import gui.models.WalletItemUnionsTableModel;
import lang.Lang;

public class MainUnionsFrame extends Main_Internal_Frame{

	private static final long serialVersionUID = 1L;
	private TableModelUnions tableModelUnions;

public MainUnionsFrame (){
// not show buttons main Toolbar
		this.setTitle(Lang.getInstance().translate("Unions"));
		 this.jButton2_jToolBar.setVisible(false);
		 this.jButton3_jToolBar.setVisible(false);
// buttun1
		 this.jButton1_jToolBar.setText(Lang.getInstance().translate("Issue Union"));
// status panel
		 this.jLabel_status_jPanel.setText(Lang.getInstance().translate("Work with unions"));
		 
		 this.jButton1_jToolBar.addActionListener(new ActionListener()
			{
			    public void actionPerformed(ActionEvent e)
			    {
			    //	 Menu.selectOrAdd( new IssueUnionFrame(), MainFrame.desktopPane.getAllFrames());
			    	 new IssueUnionDialog();
			    }

				
		});	
		 
		 this.jToolBar.setVisible(false);

// all unions 
		Split_Panel search_Union_SplitPanel = new Split_Panel();
		search_Union_SplitPanel.setName(Lang.getInstance().translate("Search Unions"));
		search_Union_SplitPanel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
// not show buttons
		search_Union_SplitPanel.button1_ToolBar_LeftPanel.setVisible(false);
		search_Union_SplitPanel.button2_ToolBar_LeftPanel.setVisible(false);
		search_Union_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
		search_Union_SplitPanel.jButton2_jToolBar_RightPanel.setVisible(false);
			
			
//CREATE TABLE
		this.tableModelUnions = new TableModelUnions();
		final JTable unionsTable = new JTable(this.tableModelUnions);
		TableColumnModel columnModel = unionsTable.getColumnModel(); // read column model
		columnModel.getColumn(0).setMaxWidth((100));
//Custom renderer for the String column;
		unionsTable.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		unionsTable.setDefaultRenderer(String.class, new Renderer_Left(unionsTable.getFontMetrics(unionsTable.getFont()))); // set renderer
//CHECKBOX FOR FAVORITE
		TableColumn favoriteColumn = unionsTable.getColumnModel().getColumn(TableModelUnions.COLUMN_FAVORITE);
		favoriteColumn.setCellRenderer(new Renderer_Boolean());
		favoriteColumn.setMinWidth(50);
		favoriteColumn.setMaxWidth(50);
		favoriteColumn.setPreferredWidth(50);
//Sorter
		RowSorter<TableModelUnions> sorter =   new TableRowSorter<TableModelUnions>(this.tableModelUnions);
		unionsTable.setRowSorter(sorter);	
// UPDATE FILTER ON TEXT CHANGE
		search_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}
			public void removeUpdate(DocumentEvent e) {
				onChange();
			}
			public void insertUpdate(DocumentEvent e) {
				onChange();
			}
			@SuppressWarnings("unchecked")
			public void onChange() {
// GET VALUE
				String search = search_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();
// SET FILTER
				tableModelUnions.fireTableDataChanged();
				@SuppressWarnings("rawtypes")
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter<TableModelUnions, ?>) sorter).setRowFilter(filter);
				tableModelUnions.fireTableDataChanged();
			}
		});
// set show			
		search_Union_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(this.tableModelUnions);
		search_Union_SplitPanel.jTable_jScrollPanel_LeftPanel = unionsTable;
		search_Union_SplitPanel.jScrollPanel_LeftPanel.setViewportView(search_Union_SplitPanel.jTable_jScrollPanel_LeftPanel);
				
		Union_Info info = new Union_Info();
	
// обработка изменения положения курсора в таблице
		search_Union_SplitPanel.jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
		@SuppressWarnings({ "unused" })
		@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String dateAlive;
				String date_birthday;
				String message;
// устанавливаем формат даты
				SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
//создаем объект персоны
				UnionCls union;
				if (unionsTable.getSelectedRow() >= 0 ){
					union = tableModelUnions.getUnion(unionsTable.convertRowIndexToModel(unionsTable.getSelectedRow()));
					info.show_Union_001(union);
					search_Union_SplitPanel.jSplitPanel.setDividerLocation(search_Union_SplitPanel.jSplitPanel.getDividerLocation());	
					search_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
				}
			}
		});				
		search_Union_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info);
						
// MENU			
		JPopupMenu all_Unions_Table_menu = new JPopupMenu();
		JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
		confirm_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
// открываем диалоговое окно ввода данных для подтверждения персоны 
				UnionCls union = tableModelUnions.getUnion(unionsTable.convertRowIndexToModel(unionsTable.getSelectedRow()));
				new UnionConfirmDialog(MainUnionsFrame.this, union);
			}
		});
		all_Unions_Table_menu.add(confirm_Menu);

		JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
		setStatus_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UnionCls union = tableModelUnions.getUnion(unionsTable.convertRowIndexToModel(unionsTable.getSelectedRow()));
				new UnionSetStatusDialog(MainUnionsFrame.this, union);		
			}
		});
		all_Unions_Table_menu.add(setStatus_Menu);
		unionsTable.setComponentPopupMenu(all_Unions_Table_menu);
							
		unionsTable.addMouseListener(new MouseAdapter() {
		@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = unionsTable.rowAtPoint(p);
				unionsTable.setRowSelectionInterval(row, row);
				if(e.getClickCount() == 2)
				{
					row = unionsTable.convertRowIndexToModel(row);
				}
			}
	});
						 
						 
// My unions		
	Split_Panel my_Union_SplitPanel = new Split_Panel();
	my_Union_SplitPanel.setName(Lang.getInstance().translate("My Unions"));
	my_Union_SplitPanel.searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
// not show buttons
	my_Union_SplitPanel.button1_ToolBar_LeftPanel.setVisible(false);
	my_Union_SplitPanel.button2_ToolBar_LeftPanel.setVisible(false);
	my_Union_SplitPanel.jButton1_jToolBar_RightPanel.setVisible(false);
	my_Union_SplitPanel.jButton2_jToolBar_RightPanel.setVisible(false);
//TABLE
	final WalletItemUnionsTableModel unionsModel = new WalletItemUnionsTableModel();
	final JTable tableUnion = new JTable(unionsModel);
	columnModel = tableUnion.getColumnModel(); // read column model
	columnModel.getColumn(0).setMaxWidth((100));
//Custom renderer for the String column;
	tableUnion.setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
	tableUnion.setDefaultRenderer(String.class, new Renderer_Left(tableUnion.getFontMetrics(tableUnion.getFont()))); // set renderer
	TableRowSorter<WalletItemUnionsTableModel> sorter1 = new TableRowSorter<WalletItemUnionsTableModel>(unionsModel);
	tableUnion.setRowSorter(sorter1);
	tableUnion.getRowSorter();
	//unionsModel.fireTableDataChanged();
//CHECKBOX FOR CONFIRMED
	TableColumn confirmedColumn = tableUnion.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_CONFIRMED);
	confirmedColumn.setCellRenderer(new Renderer_Boolean()); //unionsTable.getDefaultRenderer(Boolean.class));
	confirmedColumn.setMinWidth(50);
	confirmedColumn.setMaxWidth(50);
	confirmedColumn.setPreferredWidth(50);
//CHECKBOX FOR FAVORITE
	favoriteColumn = tableUnion.getColumnModel().getColumn(WalletItemUnionsTableModel.COLUMN_FAVORITE);
//favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
	favoriteColumn.setCellRenderer(new Renderer_Boolean()); //unionsTable.getDefaultRenderer(Boolean.class));
	favoriteColumn.setMinWidth(50);
	favoriteColumn.setMaxWidth(50);
	favoriteColumn.setPreferredWidth(50);//.setWidth(30);
	
//Sorter
			RowSorter sorter11 =   new TableRowSorter(unionsModel);
			tableUnion.setRowSorter(sorter11);	
	
	
//CREATE SEARCH FIELD
// UPDATE FILTER ON TEXT CHANGE
	my_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}

			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			@SuppressWarnings("unchecked")
			public void onChange() {
// GET VALUE
				String search = my_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.getText();
// SET FILTER
				unionsModel.fireTableDataChanged();
				@SuppressWarnings("rawtypes")
				RowFilter filter1 = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter<WalletItemUnionsTableModel, ?>) sorter11).setRowFilter(filter1);
				unionsModel.fireTableDataChanged();
			
				
			
			
			
			}
		});
// set show		
		my_Union_SplitPanel.jTable_jScrollPanel_LeftPanel.setModel(unionsModel);
		my_Union_SplitPanel.jTable_jScrollPanel_LeftPanel = tableUnion;
		my_Union_SplitPanel.jScrollPanel_LeftPanel.setViewportView(my_Union_SplitPanel.jTable_jScrollPanel_LeftPanel);		
// new info panel
		Union_Info info1 = new Union_Info();
// обработка изменения положения курсора в таблице
		tableUnion.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
// устанавливаем формат даты
				SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
//создаем объект персоны
				UnionCls union;
				if (tableUnion.getSelectedRow() >= 0 ){
// select person
					union = unionsModel.getItem(tableUnion.convertRowIndexToModel(tableUnion.getSelectedRow()));
					info1.show_Union_002(union);
					my_Union_SplitPanel.jSplitPanel.setDividerLocation(my_Union_SplitPanel.jSplitPanel.getDividerLocation());	
					my_Union_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
				}
			}
		});
		my_Union_SplitPanel.jScrollPane_jPanel_RightPanel.setViewportView(info1);
		
		
// issiue panel
		
		
		 JPanel issuePanel = new IssueUnionPanel();
		 issuePanel.setName(Lang.getInstance().translate("Issue Union"));	
		
		
							
		this.jTabbedPane.add(my_Union_SplitPanel);
		this.jTabbedPane.add(search_Union_SplitPanel);
		this.jTabbedPane.add(issuePanel);

		this.pack();
//			this.setSize(800,600);
		this.setMaximizable(true);
		this.setClosable(true);
		this.setResizable(true);
//			this.setSize(new Dimension( (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
		this.setLocation(20, 20);
//			this.setIconImages(icons);
			//CLOSE
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		 this.setResizable(true);
	    this.setVisible(true);
		Dimension size = MainFrame.desktopPane.getSize();
		this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));

		search_Union_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));
		my_Union_SplitPanel.jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));


	}
}
