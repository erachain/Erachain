package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.persons.ItemsPersonsTableModel;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.MenuPopupUtil;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;

public class SearchStatementsSplitPanel extends SplitPanel {

    public static String NAME = "SearchStatementsSplitPanel";
    public static String TITLE = "Search Documents";

    private static final long serialVersionUID = 2717571093561259483L;
    private SearchStatementsTableModel search_Table_Model;
    private RowSorter<ItemsPersonsTableModel> search_Sorter;
    private int selected_Item;
    private JTextField key_Item;
    Wallet wallet = Controller.getInstance().wallet;

    public SearchStatementsSplitPanel() {
        super(NAME, TITLE);

        this.searchToolBar_LeftPanel.setVisible(true);
        this.searchFavoriteJCheckBoxLeftPanel.setVisible(false);

        // not show buttons
        jToolBarRightPanel.setVisible(false);
        toolBarLeftPanel.setVisible(false);
        this.toolBarLeftPanel.add(new JLabel("  " + Lang.T("Find Key") + ":"));
        key_Item = new JTextField();
        key_Item.setToolTipText("");
        key_Item.setAlignmentX(1.0F);
        key_Item.setMinimumSize(new Dimension(100, (int) (UIManager.getFont("Label.font").getSize() * 1.4)));
        key_Item.setMinimumSize(new Dimension(100, (int) (UIManager.getFont("Label.font").getSize() * 1.4)));
        key_Item.setPreferredSize(new Dimension(100, (int) (UIManager.getFont("Label.font").getSize() * 1.4)));

        MenuPopupUtil.installContextMenu(key_Item);

        this.toolBarLeftPanel.add(key_Item);


        key_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                searchTextFieldSearchToolBarLeftPanelDocument.setText("");
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
        search_Table_Model = new SearchStatementsTableModel();
        jTableJScrollPanelLeftPanel = new MTable(this.search_Table_Model);

        // CHECKBOX FOR FAVORITE
        TableColumnModel columnModel = jTableJScrollPanelLeftPanel.getColumnModel();
        columnModel.getColumn(search_Table_Model.COLUMN_SEQNO).setPreferredWidth(150);
        columnModel.getColumn(search_Table_Model.COLUMN_SEQNO).setMaxWidth(150);
        columnModel.getColumn(search_Table_Model.COLUMN_FAVORITE).setPreferredWidth(70);
        columnModel.getColumn(search_Table_Model.COLUMN_FAVORITE).setMaxWidth(100);

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
        searchTextFieldSearchToolBarLeftPanelDocument.addActionListener(new ActionListener() {


            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                // GET VALUE
                String search = searchTextFieldSearchToolBarLeftPanelDocument.getText();
                jScrollPanelLeftPanel.setViewportView(null);
                jScrollPaneJPanelRightPanel.setViewportView(null);

                if (search.equals("")) {
                    search_Table_Model.clear();
                    Label_search_Info_Panel.setText(Lang.T("Fill field Search"));
                    jScrollPanelLeftPanel.setViewportView(search_Info_Panel);

                    return;
                }
                // if (search.length()<3) return;
                key_Item.setText("");
                // show message
                // jTableJScrollPanelLeftPanel.setVisible(false);//
                Label_search_Info_Panel.setText(Lang.T("Waiting..."));
                jScrollPanelLeftPanel.setViewportView(search_Info_Panel);

                new Thread() {
                    @Override
                    public void run() {
                        search_Table_Model.setFilterByName(search, null);
                        if (search_Table_Model.getRowCount() < 1) {
                            Label_search_Info_Panel.setText(Lang.T("Not Found Documents"));
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

        JMenuItem vouch_Item = new JMenuItem(Lang.T("Sign / Vouch"));

        vouch_Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0)
                    return;

                Transaction statement = search_Table_Model.getItem(jTableJScrollPanelLeftPanel
                        .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
                if (statement == null)
                    return;

                new toSignRecordDialog(statement.getBlockHeight(), statement.getSeqNo());

            }
        });

        menu.add(vouch_Item);

        JMenuItem linkMenu = new JMenuItem(Lang.T("Append Document"));
        linkMenu.addActionListener(e -> {
            Transaction transaction = search_Table_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            MainPanel.getInstance().insertNewTab(
                    Lang.T("For # для") + " " + transaction.viewHeightSeq(),
                    new IssueDocumentPanel(null, ExData.LINK_APPENDIX_TYPE, transaction.viewHeightSeq(), null));

        });
        menu.add(linkMenu);

        JMenu menuSaveCopy = new JMenu(Lang.T("Save / Copy"));
        menu.add(menuSaveCopy);

        JMenuItem copyNumber = new JMenuItem(Lang.T("Copy Number"));
        copyNumber.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) search_Table_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            StringSelection stringSelection = new StringSelection(transaction.viewHeightSeq());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Number of the '%1' has been copy to buffer")
                            .replace("%1", transaction.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyNumber);

        JMenuItem copySourceText = new JMenuItem(Lang.T("Copy Source Message"));
        copySourceText.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            RSignNote transaction = (RSignNote) search_Table_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            StringSelection stringSelection = new StringSelection(transaction.getMessage());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Source Message of the '%1' has been copy to buffer")
                            .replace("%1", transaction.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copySourceText);

        JMenuItem copySign = new JMenuItem(Lang.T("Copy Signature"));
        copySign.addActionListener(e -> {
            Transaction transaction = (Transaction) search_Table_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            StringSelection stringSelection = new StringSelection(transaction.viewSignature());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Signature '%1' has been copy to buffer")
                            .replace("%1", transaction.viewSignature())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copySign);

        JMenuItem copyJson = new JMenuItem(Lang.T("Copy JSON"));
        copyJson.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) search_Table_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            StringSelection stringSelection = new StringSelection(transaction.toJson().toJSONString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("JSON of the '%1' has been copy to buffer")
                            .replace("%1", transaction.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyJson);

        JMenuItem copyRAW = new JMenuItem(Lang.T("Copy RAW (bytecode) as Base58"));
        copyRAW.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) search_Table_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            StringSelection stringSelection = new StringSelection(Base58.encode(transaction.toBytes(Transaction.FOR_NETWORK, true)));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Bytecode of the '%1' has been copy to buffer")
                            .replace("%1", transaction.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyRAW);

        JMenuItem copyRAW64 = new JMenuItem(Lang.T("Copy RAW (bytecode) as Base64"));
        copyRAW64.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) search_Table_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            StringSelection stringSelection = new StringSelection(Base64.getEncoder().encodeToString(transaction.toBytes(Transaction.FOR_NETWORK, true)));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Bytecode of the '%1' has been copy to buffer")
                            .replace("%1", transaction.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyRAW64);

        JMenuItem saveJson = new JMenuItem(Lang.T("Save as JSON"));
        saveJson.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) search_Table_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            Library.saveJSONtoFileSystem(this, transaction, "tx" + transaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveJson);

        JMenuItem saveRAW = new JMenuItem(Lang.T("Save RAW (bytecode) as Base58"));
        saveRAW.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) search_Table_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            Library.saveAsBase58FileSystem(this, transaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + transaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW);

        JMenuItem saveRAW64 = new JMenuItem(Lang.T("Save RAW (bytecode) as Base64"));
        saveRAW64.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) search_Table_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            Library.saveAsBase64FileSystem(this, transaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + transaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW64);


        menu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));

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
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
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
                        Transaction transaction = search_Table_Model.getItem(row);
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
        //if (c1 instanceof RNoteInfo) ((RNoteInfo) c1).delay_on_Close();

    }

    public void favorite_set(Transaction transaction) {

        // CHECK IF FAVORITES
        if (wallet.isDocumentFavorite(transaction)) {
            int dd = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.T("Delete from favorite") + "?", Lang.T("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);

            if (dd == 0) wallet.removeDocumentFavorite(transaction);
        } else {
            wallet.addDocumentFavorite(transaction);
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
            //info_panel.setMinimumSize(new Dimension(infoPanelWidth, infoPanelHeight));
            //info_panel.setMaximumSize(new Dimension(infoPanelWidth, infoPanelHeight));

            jScrollPaneJPanelRightPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPaneJPanelRightPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            jScrollPaneJPanelRightPanel.setViewportView(info_panel);
            // jSplitPanel.setRightComponent(info_panel);
        }
    }

}
