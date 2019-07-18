package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.persons.ItemsPersonsTableModel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.MenuPopupUtil;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
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
        this.searchFavoriteJCheckBoxLeftPanel.setVisible(false);

        // not show buttons
        jToolBarRightPanel.setVisible(false);
        toolBarLeftPanel.setVisible(false);
        this.toolBarLeftPanel.add(new JLabel(Lang.getInstance().translate("Find Key") + ":"));
        key_Item = new JTextField();
        key_Item.setToolTipText("");
        key_Item.setAlignmentX(1.0F);
        key_Item.setMinimumSize(new java.awt.Dimension(100, 20));
        key_Item.setName(""); // NOI18N
        key_Item.setPreferredSize(new java.awt.Dimension(100, 20));
        key_Item.setMaximumSize(new java.awt.Dimension(2000, 20));

        MenuPopupUtil.installContextMenu(key_Item);

        this.toolBarLeftPanel.add(key_Item);


        key_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                searchTextField_SearchToolBar_LeftPanel.setText("");
                search_Table_Model.findByKey(key_Item.getText());
                if (search_Table_Model.getRowCount() < 1)
                    return;
                selected_Item = 0;
                jTableJScrollPanelLeftPanel.setRowSelectionInterval(selected_Item, selected_Item);

            }

        });

        // not show My filter
        searchMyJCheckBoxLeftPanel.setVisible(false);

        // CREATE TABLE
        search_Table_Model = new StatementsTableModelSearch();
        jTableJScrollPanelLeftPanel = new MTable(this.search_Table_Model);


        // Custom renderer for the String column;
        // this.search_Table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION
        // );

        // CHECKBOX FOR FAVORITE
        TableColumn favoriteColumn = jTableJScrollPanelLeftPanel.getColumnModel()
                .getColumn(search_Table_Model.COLUMN_FAVORITE);
        // favoriteColumn.setCellRenderer(new RendererBoolean());
        favoriteColumn.setMinWidth(150);
        favoriteColumn.setMaxWidth(300);
        favoriteColumn.setPreferredWidth(100);

        // hand cursor for Favorite column
        jTableJScrollPanelLeftPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {

                if (jTableJScrollPanelLeftPanel
                        .columnAtPoint(e.getPoint()) == search_Table_Model.COLUMN_FAVORITE) {

                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            public void mouseDragged(MouseEvent e) {
            }
        });

        // Sorter
        // searchSorter = new
        // TableRowSorter<ItemsPersonsTableModel>(this.search_Table_Model);
        // search_Table.setRowSorter(searchSorter);

        // UPDATE FILTER ON TEXT CHANGE
        searchTextField_SearchToolBar_LeftPanel.addActionListener(new ActionListener() {


            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                // GET VALUE
                String search = searchTextField_SearchToolBar_LeftPanel.getText();
                jScrollPanelLeftPanel.setViewportView(null);
                jScrollPaneJPanelRightPanel.setViewportView(null);

                if (search.equals("")) {
                    search_Table_Model.clear();
                    Label_search_Info_Panel.setText(Lang.getInstance().translate("Fill field Search"));
                    jScrollPanelLeftPanel.setViewportView(search_Info_Panel);

                    return;
                }
                // if (search.length()<3) return;
                key_Item.setText("");
                // show message
                // jTableJScrollPanelLeftPanel.setVisible(false);//
                Label_search_Info_Panel.setText(Lang.getInstance().translate("Waiting..."));
                jScrollPanelLeftPanel.setViewportView(search_Info_Panel);

                new Thread() {
                    @Override
                    public void run() {
                        search_Table_Model.setFilterByName(search);
                        if (search_Table_Model.getRowCount() < 1) {
                            Label_search_Info_Panel.setText(Lang.getInstance().translate("Not Found Documents"));
                            jScrollPanelLeftPanel.setViewportView(search_Info_Panel);
                            return;
                        }
                        jTableJScrollPanelLeftPanel.setRowSelectionInterval(0, 0);
                        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
                    }
                }.start();

            }

        });
        // SET VIDEO

        // jTableJScrollPanelLeftPanel = search_Table;
        // sorter from 0 column
        search_Sorter = new TableRowSorter(search_Table_Model);
        ArrayList<SortKey> keys = new ArrayList<RowSorter.SortKey>();
        keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        search_Sorter.setSortKeys(keys);
        ((DefaultRowSorter<?, ?>) search_Sorter).setSortsOnUpdates(true);
        this.jTableJScrollPanelLeftPanel.setRowSorter(search_Sorter);
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
        // setRowHeightFormat(true);
        // Event LISTENER
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

        JPopupMenu menu = new JPopupMenu();

        JMenuItem vouch_Item = new JMenuItem(Lang.getInstance().translate("Vouch"));

        vouch_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0)
                    return;

                Transaction statement = search_Table_Model.getItem(jTableJScrollPanelLeftPanel
                        .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
                if (statement == null)
                    return;

                new VouchRecordDialog(statement.getBlockHeight(), statement.getSeqNo());

            }
        });

        menu.add(vouch_Item);

        menu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) {
                    return;
                }

                Transaction transaction = search_Table_Model.getItem(jTableJScrollPanelLeftPanel
                        .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
                if (transaction == null) {
                    return;
                }

                try {
                    URLViewer.openWebpage(new URL("http://" + Settings.getInstance().getBlockexplorerURL()
                            + ":" + Settings.getInstance().getWebPort() + "/index/blockexplorer.html"
                            + "?tx=" + transaction.viewHeightSeq()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        menu.add(setSeeInBlockexplorer);

        TableMenuPopupUtil.installContextMenu(jTableJScrollPanelLeftPanel, menu);

        jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTableJScrollPanelLeftPanel.rowAtPoint(p);
                //	jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);

                if (e.getClickCount() == 1 & e.getButton() == MouseEvent.BUTTON1) {

                    if (jTableJScrollPanelLeftPanel
                            .getSelectedColumn() == search_Table_Model.COLUMN_FAVORITE) {
                        row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                        Transaction transaction = (Transaction) search_Table_Model.getItem(row);
                        favorite_set(transaction);
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
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        if (c1 instanceof StatementInfo)
            ((StatementInfo) c1).delay_on_Close();

    }

    public void favorite_set(Transaction transaction) {

        // CHECK IF FAVORITES
        if (Controller.getInstance().isTransactionFavorite(transaction)) {
            int dd = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.getInstance().translate("Delete from favorite") + "?", Lang.getInstance().translate("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);

            if (dd == 0) Controller.getInstance().removeTransactionFavorite(transaction);
        } else {

            Controller.getInstance().addTransactionFavorite(transaction);
        }
        jTableJScrollPanelLeftPanel.repaint();

    }

    // listener select row
    class search_listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0)
                return;

            Transaction transaction = search_Table_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            JPanel info_panel = TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);

            int infoPanelWidth = jScrollPaneJPanelRightPanel.getSize().width;
            int infoPanelHeight = jScrollPaneJPanelRightPanel.getSize().height;

            info_panel.setPreferredSize(new Dimension(infoPanelWidth, infoPanelHeight));
            info_panel.setMinimumSize(new Dimension(infoPanelWidth, infoPanelHeight));
            info_panel.setMaximumSize(new Dimension(infoPanelWidth, infoPanelHeight));

            jScrollPaneJPanelRightPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPaneJPanelRightPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            jScrollPaneJPanelRightPanel.setViewportView(info_panel);
            // jSplitPanel.setRightComponent(info_panel);
        }
    }
}
