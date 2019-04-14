package org.erachain.gui.items.records;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.persons.ItemsPersonsTableModel;
import org.erachain.gui.items.statement.StatementInfo;
import org.erachain.gui.library.MTable;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui.transaction.TransactionDetailsFactory;
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
import java.awt.event.*;
import java.util.ArrayList;


public class FavoriteTransactionsSplitPanel extends SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;
    // для прозрачности
    int alpha = 255;
    int alpha_int;
    // private StatementsTableModelFavorite search_Table_Model;
    private FavoriteTransactionTableModel favotitesTable;
    //	private MTable search_Table;
    private RowSorter<ItemsPersonsTableModel> search_Sorter;

    public FavoriteTransactionsSplitPanel() {
        super("FavoriteStatementsSplitPanel");
        setName(Lang.getInstance().translate("Favorite Transactions"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");

        // not show buttons
        jToolBar_RightPanel.setVisible(false);
        toolBar_LeftPanel.setVisible(false);

        // not show My filter
        searth_My_JCheckBox_LeftPanel.setVisible(false);

        //CREATE TABLE
        //search_Table_Model = new StatementsTableModelFavorite();
        favotitesTable = new FavoriteTransactionTableModel();

        // UPDATE FILTER ON TEXT CHANGE
        searchTextField_SearchToolBar_LeftPanel.getDocument().addDocumentListener(new search_tab_filter());
        // SET VIDEO
        jTable_jScrollPanel_LeftPanel = new MTable(this.favotitesTable);
        //	jTable_jScrollPanel_LeftPanel = search_Table;
        //sorter from 0 column
        search_Sorter = new TableRowSorter(favotitesTable);
        ArrayList<SortKey> keys = new ArrayList<SortKey>();
        keys.add(new SortKey(0, SortOrder.DESCENDING));
        search_Sorter.setSortKeys(keys);
        ((DefaultRowSorter<?, ?>) search_Sorter).setSortsOnUpdates(true);
        this.jTable_jScrollPanel_LeftPanel.setRowSorter(search_Sorter);
        jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);
        //	setRowHeightFormat(true);
        // Event LISTENER
        jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

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

            if (jTable_jScrollPanel_LeftPanel.getSelectedRow() < 0) return;


            Transaction statement = (Transaction) favotitesTable.getItem(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
            if (statement == null) return;
            new VouchRecordDialog(statement.getBlockHeight(), statement.getSeqNo());
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

                    if (jTable_jScrollPanel_LeftPanel.getSelectedColumn() == favotitesTable.COLUMN_FAVORITE) {

                        row = jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(row);
                        Transaction transaction = (Transaction) favotitesTable.getItem(row);
                        favorite_set(transaction);

                    }
                }
            }
        });

        jTable_jScrollPanel_LeftPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {

                if (jTable_jScrollPanel_LeftPanel.columnAtPoint(e.getPoint()) == favotitesTable.COLUMN_FAVORITE) {
                    jTable_jScrollPanel_LeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

                } else {
                    jTable_jScrollPanel_LeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            public void mouseDragged(MouseEvent e) {
            }
        });

    }

    @Override
    public void onClose() {
        // delete observer left panel
        favotitesTable.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        if (c1 instanceof StatementInfo) ((StatementInfo) c1).delay_on_Close();

    }

    public void favorite_set(Transaction transaction) {

        // CHECK IF FAVORITES
        if (Controller.getInstance().isTransactionFavorite(transaction)) {
            int dd = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.getInstance().translate("Delete from favorite") + "?", Lang.getInstance().translate("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);

            if (dd == 0) Controller.getInstance().removeTransactionFavorite(transaction);
        } else {

            Controller.getInstance().addTransactionFavorite(transaction);
        }
        jTable_jScrollPanel_LeftPanel.repaint();

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
            favotitesTable.fireTableDataChanged();

            RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
            ((DefaultRowSorter) search_Sorter).setRowFilter(filter);

            favotitesTable.fireTableDataChanged();

        }
    }

    // listener select row
    class search_listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            if (jTable_jScrollPanel_LeftPanel.getSelectedRow() < 0)
                return;

            Transaction transaction = (Transaction)favotitesTable.getItem(jTable_jScrollPanel_LeftPanel.convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));

            JPanel info_panel = TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
            info_panel.setPreferredSize(new Dimension(jScrollPane_jPanel_RightPanel.getSize().width - 50, jScrollPane_jPanel_RightPanel.getSize().height - 50));
            jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
            //	jSplitPanel.setRightComponent(info_panel);
        }
    }

}
