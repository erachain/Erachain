package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.persons.ItemsPersonsTableModel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MTable;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui.transaction.RecDetailsFrame;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class SearchStatementsSplitPanel extends SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;
    // для прозрачности
    int alpha = 255;
    int alpha_int;
    private StatementsTableModelSearch search_Table_Model;
    // private MTable search_Table;
    private RowSorter<ItemsPersonsTableModel> search_Sorter;
    private int selected_Item;
    private JTextField key_Item;

    public SearchStatementsSplitPanel() {
        super("SearchStatementsSplitPanel");
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
                search_Table_Model.findByKey(key_Item.getText());
                if (search_Table_Model.getRowCount() < 1)
                    return;
                selected_Item = 0;
                jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(selected_Item, selected_Item);

            }

        });

        // not show My filter
        searth_My_JCheckBox_LeftPanel.setVisible(false);

        // CREATE TABLE
        search_Table_Model = new StatementsTableModelSearch();
        jTable_jScrollPanel_LeftPanel = new MTable(this.search_Table_Model);


        // Custom renderer for the String column;
        // this.search_Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION
        // );

        // CHECKBOX FOR FAVORITE
        TableColumn favoriteColumn = jTable_jScrollPanel_LeftPanel.getColumnModel()
                .getColumn(StatementsTableModelSearch.COLUMN_FAVORITE);
        // favoriteColumn.setCellRenderer(new RendererBoolean());
        favoriteColumn.setMinWidth(150);
        favoriteColumn.setMaxWidth(300);
        favoriteColumn.setPreferredWidth(100);

        // hand cursor for Favorite column
        jTable_jScrollPanel_LeftPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {

                if (jTable_jScrollPanel_LeftPanel
                        .columnAtPoint(e.getPoint()) == StatementsTableModelSearch.COLUMN_FAVORITE) {

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
        // TableRowSorter<ItemsPersonsTableModel>(this.search_Table_Model);
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
                    Label_search_Info_Panel.setText(Lang.getInstance().translate("Fill field Search"));
                    jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);

                    return;
                }
                // if (search.length()<3) return;
                key_Item.setText("");
                // show message
                // jTable_jScrollPanel_LeftPanel.setVisible(false);//
                Label_search_Info_Panel.setText(Lang.getInstance().translate("Waiting..."));
                jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);

                new Thread() {
                    @Override
                    public void run() {
                        search_Table_Model.setFilterByName(search);
                        if (search_Table_Model.getRowCount() < 1) {
                            Label_search_Info_Panel.setText(Lang.getInstance().translate("Not Found Documents"));
                            jScrollPanel_LeftPanel.setViewportView(search_Info_Panel);
                            return;
                        }
                        jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(0, 0);
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

        JMenuItem set_Status_Item = new JMenuItem(Lang.getInstance().translate("Set status"));

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

                Transaction statement = search_Table_Model.getItem(jTable_jScrollPanel_LeftPanel
                        .convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
                if (statement == null)
                    return;

                new VouchRecordDialog(statement.getBlockHeight(), statement.getSeqNo());

            }
        });

        menu.add(vouch_Item);

        TableMenuPopupUtil.installContextMenu(jTable_jScrollPanel_LeftPanel, menu);

        jTable_jScrollPanel_LeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTable_jScrollPanel_LeftPanel.rowAtPoint(p);
                //	jTable_jScrollPanel_LeftPanel.setRowSelectionInterval(row, row);

                if (e.getClickCount() == 1 & e.getButton() == MouseEvent.BUTTON1) {

                    if (jTable_jScrollPanel_LeftPanel
                            .getSelectedColumn() == StatementsTableModelSearch.COLUMN_FAVORITE) {
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

    @Override
    public void onClose() {
        // delete observer left panel
        // get component from right panel
        Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        if (c1 instanceof StatementInfo)
            ((StatementInfo) c1).delay_on_Close();

    }

    public void favorite_set(JTable personsTable) {

        int row = personsTable.getSelectedRow();
        row = personsTable.convertRowIndexToModel(row);

        Transaction transaction = (Transaction)search_Table_Model.getItem(row);

        // CHECK IF FAVORITES
        if (cnt.isTransactionFavorite(transaction)) {
            Controller.getInstance().wallet.database.getTransactionFavoritesSet().delete(transaction.getDBRef());
        } else {
            Controller.getInstance().wallet.database.getTransactionFavoritesSet().add(transaction.getDBRef());
        }

        personsTable.repaint();

    }

    // listener select row
    class search_listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            if (jTable_jScrollPanel_LeftPanel.getSelectedRow() < 0)
                return;

            Transaction transaction = search_Table_Model.getItem(jTable_jScrollPanel_LeftPanel
                    .convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
            JPanel info_panel = TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
            info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width - 50,
                    jScrollPane_jPanel_RightPanel.getSize().height - 50));
            jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
            // jSplitPanel.setRightComponent(info_panel);
        }
    }
}
