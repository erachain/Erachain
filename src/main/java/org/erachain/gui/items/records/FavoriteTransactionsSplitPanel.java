package org.erachain.gui.items.records;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.WalletTableRenderer;
import org.erachain.gui.items.persons.ItemsPersonsTableModel;
import org.erachain.gui.items.statement.IssueDocumentPanel;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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


public class FavoriteTransactionsSplitPanel extends SplitPanel {

    public static String NAME = "FavoriteTransactionsSplitPanel";
    public static String TITLE = "Favorite Transactions";

    private static final long serialVersionUID = 2717571093561259483L;
    private FavoriteTransactionTableModel favotitesTable;
    private RowSorter<ItemsPersonsTableModel> search_Sorter;

    public FavoriteTransactionsSplitPanel() {
        super(NAME, TITLE);
        iconName = "favorite.png";

        // not show buttons
        jToolBarRightPanel.setVisible(false);
        toolBarLeftPanel.setVisible(false);

        // not show My filter
        searchMyJCheckBoxLeftPanel.setVisible(false);

        //CREATE TABLE
        //search_Table_Model = new StatementsTableModelFavorite();
        favotitesTable = new FavoriteTransactionTableModel();

        // UPDATE FILTER ON TEXT CHANGE
        searchTextField2.getDocument().addDocumentListener(new search_tab_filter());
        // SET VIDEO
        jTableJScrollPanelLeftPanel = new MTable(this.favotitesTable);
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Object.class, new WalletTableRenderer());
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Boolean.class, new WalletTableRenderer());

        TableColumnModel columnModel = jTableJScrollPanelLeftPanel.getColumnModel();
        columnModel.getColumn(favotitesTable.COLUMN_SEQNO).setPreferredWidth((150));
        columnModel.getColumn(favotitesTable.COLUMN_SEQNO).setMaxWidth((150));
        columnModel.getColumn(favotitesTable.COLUMN_FAVORITE).setPreferredWidth((70));
        columnModel.getColumn(favotitesTable.COLUMN_FAVORITE).setMaxWidth((100));

        //	jTableJScrollPanelLeftPanel = search_Table;
        //sorter from 0 column
        search_Sorter = new TableRowSorter(favotitesTable);
        ArrayList<SortKey> keys = new ArrayList<SortKey>();
        keys.add(new SortKey(0, SortOrder.DESCENDING));
        search_Sorter.setSortKeys(keys);
        ((DefaultRowSorter<?, ?>) search_Sorter).setSortsOnUpdates(true);
        this.jTableJScrollPanelLeftPanel.setRowSorter(search_Sorter);
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
        //	setRowHeightFormat(true);
        // Event LISTENER
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

        JPopupMenu menu = new JPopupMenu();

        JMenuItem vouchItem = new JMenuItem(Lang.T("Sign / Vouch"));

        vouchItem.addActionListener(e -> {

            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            new toSignRecordDialog(transaction.getBlockHeight(), transaction.getSeqNo());
        });

        menu.add(vouchItem);

        JMenuItem linkMenu = new JMenuItem(Lang.T("Append Document"));
        linkMenu.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
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
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
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

        JMenuItem copySign = new JMenuItem(Lang.T("Copy Signature"));
        copySign.addActionListener(e -> {
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
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
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
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
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
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
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
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
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            Library.saveJSONtoFileSystem(this, transaction, "tx" + transaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveJson);

        JMenuItem saveRAW = new JMenuItem(Lang.T("Save RAW (bytecode) as Base58"));
        saveRAW.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            if (transaction == null) return;
            Library.saveAsBase58FileSystem(this, transaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + transaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW);

        JMenuItem saveRAW64 = new JMenuItem(Lang.T("Save RAW (bytecode) as Base64"));
        saveRAW64.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
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

                Transaction statement = (Transaction) favotitesTable.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
                if (statement == null) {
                    return;
                }

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?tx=" + statement.viewHeightSeq()));
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
                jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);

                if (e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {

                    if (jTableJScrollPanelLeftPanel.getSelectedColumn() == favotitesTable.COLUMN_FAVORITE) {

                        row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                        Transaction transaction = (Transaction) favotitesTable.getItem(row);
                        favorite_set(transaction);

                    }
                }
            }
        });

        jTableJScrollPanelLeftPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {

                if (jTableJScrollPanelLeftPanel.columnAtPoint(e.getPoint()) == favotitesTable.COLUMN_FAVORITE) {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

                } else {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        //if (c1 instanceof RNoteInfo) ((RNoteInfo) c1).delay_on_Close();

    }

    public void favorite_set(Transaction transaction) {

        // CHECK IF FAVORITES
        if (Controller.getInstance().isTransactionFavorite(transaction)) {
            int dd = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.T("Delete from favorite") + "?", Lang.T("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);

            if (dd == 0) Controller.getInstance().removeTransactionFavorite(transaction);
        } else {

            Controller.getInstance().addTransactionFavorite(transaction);
        }
        jTableJScrollPanelLeftPanel.repaint();

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
            String search = searchTextField2.getText();

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

            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0)
                return;

            Transaction transaction = (Transaction) favotitesTable.getItem(
                    jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));

            JPanel info_panel = TransactionDetailsFactory.createTransactionDetail(transaction);
            info_panel.setPreferredSize(new Dimension(jScrollPaneJPanelRightPanel.getSize().width - 50, jScrollPaneJPanelRightPanel.getSize().height - 50));
            jScrollPaneJPanelRightPanel.setViewportView(info_panel);
            //	jSplitPanel.setRightComponent(info_panel);
        }
    }
}