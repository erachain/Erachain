package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.persons.ItemsPersonsTableModel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class FavoriteStatementsSplitPanel extends SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;
    // для прозрачности
    int alpha = 255;
    int alpha_int;
    private StatementsTableModelFavorite search_Table_Model;
    //	private MTable search_Table;
    private RowSorter<ItemsPersonsTableModel> search_Sorter;

    public FavoriteStatementsSplitPanel() {
        super("FavoriteStatementsSplitPanel");
        setName(Lang.getInstance().translate("Favorite Documents"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");

        // not show buttons
        jToolBarRightPanel.setVisible(false);
        toolBarLeftPanel.setVisible(false);

        // not show My filter
        searchMyJCheckBoxLeftPanel.setVisible(false);

        //CREATE TABLE
        search_Table_Model = new StatementsTableModelFavorite();
        //	search_Table = new MTable(this.search_Table_Model);
        //	TableColumnModel columnModel = search_Table.getColumnModel(); // read column model
        //		columnModel.getColumn(0).setMaxWidth((100));

        //Custom renderer for the String column;

        //	 this.search_Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
		
	/*		
		//CHECKBOX FOR FAVORITE
				TableColumn favoriteColumn = search_Table.getColumnModel().getColumn(ItemsPersonsTableModel.COLUMN_FAVORITE);
				favoriteColumn.setCellRenderer(new RendererBoolean());
				favoriteColumn.setMinWidth(50);
				favoriteColumn.setMaxWidth(50);
				favoriteColumn.setPreferredWidth(50);
	*/
        //Sorter
        //			 search_Sorter = new TableRowSorter<ItemsPersonsTableModel>(this.search_Table_Model);
        //			search_Table.setRowSorter(search_Sorter);

        // UPDATE FILTER ON TEXT CHANGE
        searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new search_tab_filter());
        // SET VIDEO
        jTableJScrollPanelLeftPanel = new MTable(this.search_Table_Model);
        //	jTableJScrollPanelLeftPanel = search_Table;
        //sorter from 0 column
        search_Sorter = new TableRowSorter(search_Table_Model);
        ArrayList<SortKey> keys = new ArrayList<RowSorter.SortKey>();
        keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        search_Sorter.setSortKeys(keys);
        ((DefaultRowSorter<?, ?>) search_Sorter).setSortsOnUpdates(true);
        this.jTableJScrollPanelLeftPanel.setRowSorter(search_Sorter);
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
        //	setRowHeightFormat(true);
        // Event LISTENER
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

        JPopupMenu menu = new JPopupMenu();

        JMenuItem set_Status_Item = new JMenuItem(Lang.getInstance().translate("Set status"));

        set_Status_Item.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //	  	@SuppressWarnings("unused")
                        //	PersonSetStatusDialog fm = new PersonSetStatusDialog( search_Table_Model.get_Statement(search_Table.convertRowIndexToModel(search_Table.getSelectedRow())));

                    }
                });

        //	menu.add(set_Status_Item);

        JMenuItem vouch_Item = new JMenuItem(Lang.getInstance().translate("Vouch"));

        vouch_Item.addActionListener(e -> {

            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;


            Transaction statement = search_Table_Model.get_Statement(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (statement == null) return;
            new VouchRecordDialog(statement.getBlockHeight(), statement.getSeqNo());
        });

        menu.add(vouch_Item);

        TableMenuPopupUtil.installContextMenu(jTableJScrollPanelLeftPanel, menu);

        jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTableJScrollPanelLeftPanel.rowAtPoint(p);
                jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);


                if (e.getClickCount() == 1 & e.getButton() == MouseEvent.BUTTON1) {

                    if (jTableJScrollPanelLeftPanel.getSelectedColumn() == StatementsTableModelFavorite.COLUMN_FAVORITE) {
                        //	row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                        //	 PersonCls asset = search_Table_Model.getPerson(row);
                        favorite_set(jTableJScrollPanelLeftPanel);


                    }


                }
            }
        });


    }

    @Override
    public void onClose() {
        // delete observer left panel
        search_Table_Model.removeObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        if (c1 instanceof StatementInfo) ((StatementInfo) c1).delay_on_Close();

    }

    public void favorite_set(JTable personsTable) {


        int row = personsTable.getSelectedRow();
        row = personsTable.convertRowIndexToModel(row);

        Transaction person = search_Table_Model.get_Statement(row);
        //new AssetPairSelect(asset.getKey());


        //CHECK IF FAVORITES
        if (((RSignNote) person).isFavorite()) {
            row = personsTable.getSelectedRow();
            Controller.getInstance().wallet.database.getDocumentFavoritesSet().delete(person);
            if (search_Table_Model.getRowCount() == 0) return;
            if (row > 0) personsTable.addRowSelectionInterval(row - 1, row - 1);
            else personsTable.addRowSelectionInterval(0, 0);
        } else {

            Controller.getInstance().wallet.database.getDocumentFavoritesSet().add(person);
        }


        personsTable.repaint();


    }

    // filter search
    class search_tab_filter implements DocumentListener {

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
            String search = searchTextField_SearchToolBar_LeftPanel.getText();

            // SET FILTER
            //tableModelPersons.getSortableList().setFilter(search);
            search_Table_Model.fireTableDataChanged();

            RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
            ((DefaultRowSorter) search_Sorter).setRowFilter(filter);

            search_Table_Model.fireTableDataChanged();

        }
    }

    // listener select row
    class search_listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0)
                return;

            Transaction statement = search_Table_Model.get_Statement(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            StatementInfo info_panel = new StatementInfo(statement);
            info_panel.setPreferredSize(new Dimension(jScrollPaneJPanelRightPanel.getSize().width - 50, jScrollPaneJPanelRightPanel.getSize().height - 50));
            jScrollPaneJPanelRightPanel.setViewportView(info_panel);
            //	jSplitPanel.setRightComponent(info_panel);
        }
    }

}
