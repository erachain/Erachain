package gui.items;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.*;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import controller.Controller;
import core.item.ItemCls;
import gui.Split_Panel;
import gui.library.MTable;
import gui.models.TableModelCls;
import lang.Lang;
import utils.TableMenuPopupUtil;

public class Item_SplitPanel extends Split_Panel {

	private static final long serialVersionUID = 2717571093561259483L;
	protected TableModelCls search_Table_Model;
	protected JMenuItem favorite_menu_items;
	protected JPopupMenu menu_Table;
	protected ItemCls item_Menu;
	protected ItemCls item_Table_Selected = null;

	@SuppressWarnings("rawtypes")
	public Item_SplitPanel(TableModelCls search_Table_Model1, String gui_Name) {

		super(gui_Name);
		this.search_Table_Model = search_Table_Model1;
		
		

		// not show My filter
		searth_My_JCheckBox_LeftPanel.setVisible(false);
		searth_Favorite_JCheckBox_LeftPanel.setVisible(false);

		// CREATE TABLE
		jTable_jScrollPanel_LeftPanel = new MTable(this.search_Table_Model);
		TableColumnModel columnModel = jTable_jScrollPanel_LeftPanel.getColumnModel(); 
		columnModel.getColumn(0).setMaxWidth((100));
		// CHECKBOX FOR FAVORITE
		TableColumn favorite_Column = jTable_jScrollPanel_LeftPanel.getColumnModel()
				.getColumn(search_Table_Model.COLUMN_FAVORITE);
		favorite_Column.setMaxWidth(1000);
		favorite_Column.setPreferredWidth(50);
		// hand cursor for Favorite column
		jTable_jScrollPanel_LeftPanel.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {

				if (jTable_jScrollPanel_LeftPanel.columnAtPoint(e.getPoint()) == search_Table_Model.COLUMN_FAVORITE) {

					jTable_jScrollPanel_LeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					jTable_jScrollPanel_LeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}

			public void mouseDragged(MouseEvent e) {
			}
		});

		// select row
		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				
				if (jTable_jScrollPanel_LeftPanel.getSelectedRow() < 0) {
					jScrollPane_jPanel_RightPanel.setViewportView(null);
					jTable_jScrollPanel_LeftPanel.repaint();
					return;
				}
				item_Table_Selected = (ItemCls) search_Table_Model.getItem(jTable_jScrollPanel_LeftPanel
						.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
				if (item_Table_Selected == null)
					return;

				jScrollPane_jPanel_RightPanel.setViewportView(get_show(item_Table_Selected));
				item_Table_Selected = null;

			}

		});

		// UPDATE FILTER ON TEXT CHANGE

		
		// jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
		// mouse from favorine column
		jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("static-access")
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = jTable_jScrollPanel_LeftPanel.rowAtPoint(p);
				jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(row, row);
				row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
				ItemCls item = (ItemCls) search_Table_Model.getItem(row);
				
				if(e.getClickCount() == 2 )
				{
					table_mouse_2_Click(item);
				}
				
				if (e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {
					
					if (jTable_jScrollPanel_LeftPanel.getSelectedColumn() == search_Table_Model.COLUMN_FAVORITE) {
					favorite_set(item);
					}
				}
			}
		});

		menu_Table = new JPopupMenu();
		// favorite menu
		favorite_menu_items = new JMenuItem();
		favorite_menu_items.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = jTable_jScrollPanel_LeftPanel.getSelectedRow();
				row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
				favorite_set((ItemCls)search_Table_Model.getItem(row));

			}
		});

		menu_Table.addPopupMenuListener(new PopupMenuListener(){

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub
			
				// TODO Auto-generated method stub
				item_Menu = (ItemCls) search_Table_Model.getItem(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));

				// IF ASSET CONFIRMED AND NOT ERM

				favorite_menu_items.setVisible(true);
				// CHECK IF FAVORITES
				if (Controller.getInstance().isItemFavorite(item_Menu)) {
					favorite_menu_items.setText(Lang.getInstance().translate("Remove Favorite"));
				} else {
					favorite_menu_items.setText(Lang.getInstance().translate("Add Favorite"));
				}	
				
			}
			
			
			
		});
		
		menu_Table.add(favorite_menu_items);
		TableMenuPopupUtil.installContextMenu(jTable_jScrollPanel_LeftPanel, menu_Table);

	}

	@Override
	public void delay_on_close() {
		jTable_jScrollPanel_LeftPanel = null;
		search_Table_Model = null;
		favorite_menu_items = null;
		menu_Table= null;
		item_Menu =null;
		item_Table_Selected = null;
	}

	public void favorite_set(ItemCls itemCls) {
		
		// CHECK IF FAVORITES
		if (Controller.getInstance().isItemFavorite(itemCls)) {

			Controller.getInstance().removeItemFavorite(itemCls);
		} else {

			Controller.getInstance().addItemFavorite(itemCls);
		}
		jTable_jScrollPanel_LeftPanel.repaint();

	}

	protected Component get_show(ItemCls item) {
		return null;

	}
	protected void  table_mouse_2_Click(ItemCls item){
		
	};

}
