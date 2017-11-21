package gui.items.unions;

import java.awt.Component;
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
import gui.items.Item_Search_SplitPanel;
import gui.library.MTable;
import lang.Lang;

public class Search_Union_Tab extends Item_Search_SplitPanel{
	private static TableModelUnions tableModelUnions = new TableModelUnions();
	private Search_Union_Tab th;

	public Search_Union_Tab(){
		super(tableModelUnions, "Search_Union_Tab", "Search_Union_Tab");
		th = this;

		
	
						
// MENU			
		JPopupMenu all_Unions_Table_menu = new JPopupMenu();
		JMenuItem confirm_Menu = new JMenuItem(Lang.getInstance().translate("Confirm"));
		confirm_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
// открываем диалоговое окно ввода данных для подтверждения персоны 
				UnionCls union = (UnionCls) tableModelUnions.getItem(th.search_Table.convertRowIndexToModel(th.search_Table.getSelectedRow()));
				new UnionConfirmDialog(th, union);
			}
		});
		all_Unions_Table_menu.add(confirm_Menu);

		JMenuItem setStatus_Menu = new JMenuItem(Lang.getInstance().translate("Set Status"));
		setStatus_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UnionCls union = (UnionCls) tableModelUnions.getItem(th.search_Table.convertRowIndexToModel(th.search_Table.getSelectedRow()));
				new UnionSetStatusDialog(th, union);		
			}
		});
		all_Unions_Table_menu.add(setStatus_Menu);
		th.search_Table.setComponentPopupMenu(all_Unions_Table_menu);
							
		th.search_Table.addMouseListener(new MouseAdapter() {
		@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = th.search_Table.rowAtPoint(p);
				th.search_Table.setRowSelectionInterval(row, row);
				if(e.getClickCount() == 2)
				{
					row = th.search_Table.convertRowIndexToModel(row);
				}
			}
	});
						 
	}

	}
