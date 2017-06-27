package gui.items.statement;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.DefaultRowSorter;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.transaction.R_SignNote;
import core.transaction.Transaction;
import database.DBSet;
import gui.MainFrame;
import gui.Split_Panel;
import gui.items.persons.TableModelPersons;
import gui.library.Issue_Confirm_Dialog;
import gui.library.MTable;
import gui.records.VouchRecordDialog;
import lang.Lang;
import utils.MenuPopupUtil;
import utils.TableMenuPopupUtil;

public class Statements_Search_SplitPanel extends Split_Panel {

	private static final long serialVersionUID = 2717571093561259483L;

	private Statements_Table_Model_Search search_Table_Model;
	// private MTable search_Table;
	private RowSorter<TableModelPersons> search_Sorter;
	private int selected_Item;

	// для прозрачности
	int alpha = 255;
	int alpha_int;

	private JTextField key_Item;

	protected Issue_Confirm_Dialog ddd;

	public Statements_Search_SplitPanel() {
		super("Statements_Search_SplitPanel");
		setName(Lang.getInstance().translate("Search Statements"));
		searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
		this.searchToolBar_LeftPanel.setVisible(true);
		this.searth_Favorite_JCheckBox_LeftPanel.setVisible(false);

		// not show buttons
		jToolBar_RightPanel.setVisible(false);
		toolBar_LeftPanel.setVisible(false);
		this.toolBar_LeftPanel.add(new JLabel(Lang.getInstance().translate("Find Key") + ":"));
		key_Item = new JTextField();
		key_Item.setToolTipText("");
		key_Item.setAlignmentX(1.0F);
		key_Item.setMinimumSize(new java.awt.Dimension(100, 20));
		key_Item.setName(""); // NOI18N
		key_Item.setPreferredSize(new java.awt.Dimension(100, 20));
		key_Item.setMaximumSize(new java.awt.Dimension(2000, 20));

		MenuPopupUtil.installContextMenu(key_Item);

		this.toolBar_LeftPanel.add(key_Item);
		key_Item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				searchTextField_SearchToolBar_LeftPanel.setText("");
				search_Table_Model.Find_item_from_key(key_Item.getText());
				if (search_Table_Model.getRowCount() < 1)
					return;
				selected_Item = 0;
				jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(selected_Item, selected_Item);

			}

		});

		// not show My filter
		searth_My_JCheckBox_LeftPanel.setVisible(false);

		// CREATE TABLE
		search_Table_Model = new Statements_Table_Model_Search();
		jTable_jScrollPanel_LeftPanel = new MTable(this.search_Table_Model);
		// search_Table = new MTable(this.search_Table_Model);
		TableColumnModel columnModel = jTable_jScrollPanel_LeftPanel.getColumnModel(); // read
																						// column
																						// model
		// columnModel.getColumn(0).setMaxWidth((100));

		// Custom renderer for the String column;

		// this.search_Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION
		// );

		// CHECKBOX FOR FAVORITE
		TableColumn favoriteColumn = jTable_jScrollPanel_LeftPanel.getColumnModel()
				.getColumn(Statements_Table_Model_Search.COLUMN_FAVORITE);
		// favoriteColumn.setCellRenderer(new Renderer_Boolean());
		favoriteColumn.setMinWidth(50);
		favoriteColumn.setMaxWidth(50);
		favoriteColumn.setPreferredWidth(50);

		// hand cursor for Favorite column
		jTable_jScrollPanel_LeftPanel.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {

				if (jTable_jScrollPanel_LeftPanel
						.columnAtPoint(e.getPoint()) == Statements_Table_Model_Search.COLUMN_FAVORITE) {

					jTable_jScrollPanel_LeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					jTable_jScrollPanel_LeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}

			public void mouseDragged(MouseEvent e) {
			}
		});

		// Sorter
		// search_Sorter = new
		// TableRowSorter<TableModelPersons>(this.search_Table_Model);
		// search_Table.setRowSorter(search_Sorter);

		// UPDATE FILTER ON TEXT CHANGE
		searchTextField_SearchToolBar_LeftPanel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				// GET VALUE
				String search = searchTextField_SearchToolBar_LeftPanel.getText();
				jScrollPanel_LeftPanel.setViewportView(null);
				jScrollPane_jPanel_RightPanel.setViewportView(null);
				if (search.equals("")) {
					search_Table_Model.clear();
					jScrollPanel_LeftPanel.setViewportView(new JLabel("Edit search Field..."));
					return;
				}
				// if (search.length()<3) return;
				key_Item.setText("");
				// show message
				// jTable_jScrollPanel_LeftPanel.setVisible(false);//
				jScrollPanel_LeftPanel.setViewportView(new JLabel("Wait..."));

				new Thread() {
					@Override
					public void run() {
						search_Table_Model.set_Filter_By_Name(search);
						if (search_Table_Model.getRowCount() < 1) {
							jScrollPanel_LeftPanel.setViewportView(
									new JLabel("Not Found Documents contains In title '" + search + "'"));
							return;
						}
						jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(0, 0);
						// ddd.dispose();
						jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
					}
				}.start();

			}

		});
		// SET VIDEO

		// jTable_jScrollPanel_LeftPanel = search_Table;
		// sorter from 0 column
		search_Sorter = new TableRowSorter(search_Table_Model);
		ArrayList<SortKey> keys = new ArrayList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
		search_Sorter.setSortKeys(keys);
		((DefaultRowSorter<?, ?>) search_Sorter).setSortsOnUpdates(true);
		this.jTable_jScrollPanel_LeftPanel.setRowSorter(search_Sorter);
		jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
		// setRowHeightFormat(true);
		// Event LISTENER
		jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

		JPopupMenu menu = new JPopupMenu();

		JMenuItem set_Status_Item = new JMenuItem(Lang.getInstance().translate("Set Status"));

		set_Status_Item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// @SuppressWarnings("unused")
				// PersonSetStatusDialog fm = new PersonSetStatusDialog(
				// search_Table_Model.get_Statement(search_Table.convertRowIndexToModel(search_Table.getSelectedRow())));

			}
		});

		// menu.add(set_Status_Item);

		JMenuItem vouch_Item = new JMenuItem(Lang.getInstance().translate("Vouch"));

		vouch_Item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (jTable_jScrollPanel_LeftPanel.getSelectedRow() < 0)
					return;

				Transaction statement = search_Table_Model.get_Statement(jTable_jScrollPanel_LeftPanel
						.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
				if (statement == null)
					return;
				VouchRecordDialog vouch_panel = new VouchRecordDialog(statement.getBlockHeight(DBSet.getInstance()),
						statement.getSeqNo(DBSet.getInstance()));

			}
		});

		menu.add(vouch_Item);

		TableMenuPopupUtil.installContextMenu(jTable_jScrollPanel_LeftPanel, menu);

		jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = jTable_jScrollPanel_LeftPanel.rowAtPoint(p);
				jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(row, row);

				if (e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {

					if (jTable_jScrollPanel_LeftPanel
							.getSelectedColumn() == Statements_Table_Model_Search.COLUMN_FAVORITE) {
						// row =
						// jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
						// PersonCls asset = search_Table_Model.getPerson(row);
						favorite_set(jTable_jScrollPanel_LeftPanel);

					}

				}
			}
		});

	}

	// set favorite Search
	void favorite_all(JTable personsTable) {
		int row = personsTable.getSelectedRow();
		row = personsTable.convertRowIndexToModel(row);
		/*
		 * PersonCls person = search_Table_Model.getPerson(row); //new
		 * AssetPairSelect(asset.getKey());
		 * 
		 * 
		 * //CHECK IF FAVORITES
		 * if(Controller.getInstance().isItemFavorite(person)) {
		 * 
		 * Controller.getInstance().removeItemFavorite(person); } else {
		 * 
		 * Controller.getInstance().addItemFavorite(person); }
		 * 
		 * 
		 * personsTable.repaint();
		 */
	}

	// listener select row
	class search_listener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {

			if (jTable_jScrollPanel_LeftPanel.getSelectedRow() < 0)
				return;

			Transaction statement = search_Table_Model.get_Statement(jTable_jScrollPanel_LeftPanel
					.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
			Statement_Info info_panel = new Statement_Info(statement);
			info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width - 50,
					jScrollPane_jPanel_RightPanel.getSize().height - 50));
			jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
			// jSplitPanel.setRightComponent(info_panel);
		}
	}

	@Override
	public void delay_on_close() {
		// delete observer left panel
		search_Table_Model.removeObservers();
		// get component from right panel
		Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
		// if Person_Info 002 delay on close
		if (c1 instanceof Statement_Info)
			((Statement_Info) c1).delay_on_Close();

	}

	public void favorite_set(JTable personsTable) {

		int row = personsTable.getSelectedRow();
		row = personsTable.convertRowIndexToModel(row);

		Transaction person = search_Table_Model.get_Statement(row);
		// new AssetPairSelect(asset.getKey());

		// CHECK IF FAVORITES
		if (((R_SignNote) person).isFavorite()) {

			Controller.getInstance().wallet.database.getDocumentFavoritesSet().delete(person);
		} else {

			Controller.getInstance().wallet.database.getDocumentFavoritesSet().add(person);
		}

		personsTable.repaint();

	}

}
