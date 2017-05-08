package gui.items.unions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;

import javax.swing.DefaultRowSorter;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
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
import gui.Split_Panel;
import gui.library.MTable;
import lang.Lang;

public class Search_Union_Tab extends Split_Panel{
	private TableModelUnions tableModelUnions;
	private Search_Union_Tab sUT;

	public Search_Union_Tab(){
		sUT = this;
		
		setName(Lang.getInstance().translate("Search_Union_Tab"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") +":  ");
// not show buttons
		button1_ToolBar_LeftPanel.setVisible(false);
		button2_ToolBar_LeftPanel.setVisible(false);
		jButton1_jToolBar_RightPanel.setVisible(false);
		jButton2_jToolBar_RightPanel.setVisible(false);
			
			
//CREATE TABLE
		this.tableModelUnions = new TableModelUnions();
		final MTable unionsTable = new MTable(this.tableModelUnions);
		TableColumnModel columnModel = unionsTable.getColumnModel(); // read column model
		columnModel.getColumn(0).setMaxWidth((100));
//CHECKBOX FOR FAVORITE
		TableColumn favoriteColumn = unionsTable.getColumnModel().getColumn(TableModelUnions.COLUMN_FAVORITE);
	
		favoriteColumn.setMinWidth(50);
		favoriteColumn.setMaxWidth(50);
		favoriteColumn.setPreferredWidth(50);
//Sorter
		RowSorter<TableModelUnions> sorter =   new TableRowSorter<TableModelUnions>(this.tableModelUnions);
		unionsTable.setRowSorter(sorter);	
// UPDATE FILTER ON TEXT CHANGE
		searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new DocumentListener() {
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
				String search = searchTextField_SearchToolBar_LeftPanel.getText();
// SET FILTER
				tableModelUnions.fireTableDataChanged();
				@SuppressWarnings("rawtypes")
				RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
				((DefaultRowSorter<TableModelUnions, ?>) sorter).setRowFilter(filter);
				tableModelUnions.fireTableDataChanged();
			}
		});
// set show			
		jTable_jScrollPanel_LeftPanel.setModel(this.tableModelUnions);
		jTable_jScrollPanel_LeftPanel = unionsTable;
		jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
				
		Union_Info info = new Union_Info();
	
// обработка изменения положения курсора в таблице
		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
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
					jSplitPanel.setDividerLocation(jSplitPanel.getDividerLocation());	
					searchTextField_SearchToolBar_LeftPanel.setEnabled(true);
				}
			}
		});				
		jScrollPane_jPanel_RightPanel.setViewportView(info);
						
// MENU			
		JPopupMenu all_Unions_Table_menu = new JPopupMenu();
		JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
		confirm_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
// открываем диалоговое окно ввода данных для подтверждения персоны 
				UnionCls union = tableModelUnions.getUnion(unionsTable.convertRowIndexToModel(unionsTable.getSelectedRow()));
				new UnionConfirmDialog(sUT, union);
			}
		});
		all_Unions_Table_menu.add(confirm_Menu);

		JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
		setStatus_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UnionCls union = tableModelUnions.getUnion(unionsTable.convertRowIndexToModel(unionsTable.getSelectedRow()));
				new UnionSetStatusDialog(sUT, union);		
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
						 
		
		
		
		
		
	}

}
