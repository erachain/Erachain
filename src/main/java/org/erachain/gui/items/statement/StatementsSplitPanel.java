package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.WalletTableRenderer;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.gui.models.WalletTableModel;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;
import org.mapdb.Fun;

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


public abstract class StatementsSplitPanel<T> extends SplitPanel {

    private static final long serialVersionUID = 2717571093561259483L;

    Wallet wallet = Controller.getInstance().getWallet();
    private T currentItem;
    private RSignNote rNote;
    JMenuItem getPasswordMenuItems = new JMenuItem(Lang.T("Retrieve Password"));
    JMenuItem favoriteMenuItems = new JMenuItem(Lang.T("Remove Favorite"));

    private WalletTableModel tableModel;
    private RowSorter<WalletTableModel> tableSorter;

    public StatementsSplitPanel(String name, String title, WalletTableModel tableModel, int seqNoCol, boolean isFavorite) {
        super(name, title);

        // not show buttons
        jToolBarRightPanel.setVisible(false);
        toolBarLeftPanel.setVisible(false);

        // not show My filter
        searchMyJCheckBoxLeftPanel.setVisible(false);

        //CREATE TABLE
        //search_Table_Model = new StatementsTableModelFavorite();
        this.tableModel = tableModel;

        // UPDATE FILTER ON TEXT CHANGE
        searchTextFieldSearchToolBarLeftPanelDocument.getDocument().addDocumentListener(new search_tab_filter());
        // SET VIDEO
        jTableJScrollPanelLeftPanel = new MTable(this.tableModel);
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Object.class, new WalletTableRenderer());
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Boolean.class, new WalletTableRenderer());

        TableColumnModel columnModel = jTableJScrollPanelLeftPanel.getColumnModel();
        columnModel.getColumn(seqNoCol).setPreferredWidth(150);
        columnModel.getColumn(seqNoCol).setMaxWidth(150);
        columnModel.getColumn(this.tableModel.COLUMN_FAVORITE).setPreferredWidth(70);
        columnModel.getColumn(this.tableModel.COLUMN_FAVORITE).setMaxWidth(100);

        //	jTableJScrollPanelLeftPanel = search_Table;
        //sorter from 0 column
        tableSorter = new TableRowSorter(this.tableModel);
        ArrayList<SortKey> keys = new ArrayList<SortKey>();
        keys.add(new SortKey(0, SortOrder.DESCENDING));
        tableSorter.setSortKeys(keys);
        ((DefaultRowSorter<?, ?>) tableSorter).setSortsOnUpdates(true);
        this.jTableJScrollPanelLeftPanel.setRowSorter(tableSorter);
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);
        //	setRowHeightFormat(true);
        // Event LISTENER
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new selectRowListener());

        JPopupMenu menu = new JPopupMenu();

        // favorite menu
        favoriteMenuItems.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (rNote == null) return;
                favoriteSet(rNote);
            }
        });
        menu.add(favoriteMenuItems);

        menu.addSeparator();

        JMenuItem vouch_Item = new JMenuItem(Lang.T("Sign / Vouch"));

        vouch_Item.addActionListener(e -> {
            if (rNote == null) return;
            new toSignRecordDialog(rNote.getBlockHeight(), rNote.getSeqNo());
        });

        menu.add(vouch_Item);

        JMenuItem linkMenu = new JMenuItem(Lang.T("Append Document"));
        linkMenu.addActionListener(e -> {
            if (rNote == null)
                return;
            MainPanel.getInstance().insertNewTab(
                    Lang.T("For # для") + " " + rNote.viewHeightSeq(),
                    new IssueDocumentPanel(null, ExData.LINK_APPENDIX_TYPE, rNote.viewHeightSeq(), null));

        });
        menu.add(linkMenu);

        JMenu menuSaveCopy = new JMenu(Lang.T("Save / Copy"));
        menu.add(menuSaveCopy);

        JMenuItem copyNumber = new JMenuItem(Lang.T("Copy Number"));
        copyNumber.addActionListener(e -> {
            if (rNote == null)
                return;
            StringSelection stringSelection = new StringSelection(rNote.viewHeightSeq());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Number of the '%1' has been copy to buffer")
                            .replace("%1", rNote.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyNumber);

        JMenuItem copySourceText = new JMenuItem(Lang.T("Copy Source Message"));
        copySourceText.addActionListener(e -> {
            if (rNote == null)
                return;
            StringSelection stringSelection = new StringSelection(rNote.getMessage());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Source Message of the '%1' has been copy to buffer")
                            .replace("%1", rNote.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copySourceText);

        JMenuItem copySign = new JMenuItem(Lang.T("Copy Signature"));
        copySign.addActionListener(e -> {
            if (rNote == null)
                return;
            StringSelection stringSelection = new StringSelection(rNote.viewSignature());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Signature '%1' has been copy to buffer")
                            .replace("%1", rNote.viewSignature())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copySign);

        JMenuItem copyJson = new JMenuItem(Lang.T("Copy JSON"));
        copyJson.addActionListener(e -> {
            if (rNote == null)
                return;
            StringSelection stringSelection = new StringSelection(rNote.toJson().toJSONString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("JSON of the '%1' has been copy to buffer")
                            .replace("%1", rNote.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyJson);

        JMenuItem copyRAW = new JMenuItem(Lang.T("Copy RAW (bytecode) as Base58"));
        copyRAW.addActionListener(e -> {
            if (rNote == null)
                return;
            StringSelection stringSelection = new StringSelection(Base58.encode(rNote.toBytes(Transaction.FOR_NETWORK, true)));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Bytecode of the '%1' has been copy to buffer")
                            .replace("%1", rNote.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyRAW);

        JMenuItem copyRAW64 = new JMenuItem(Lang.T("Copy RAW (bytecode) as Base64"));
        copyRAW64.addActionListener(e -> {
            if (rNote == null)
                return;
            StringSelection stringSelection = new StringSelection(Base64.getEncoder().encodeToString(rNote.toBytes(Transaction.FOR_NETWORK, true)));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Bytecode of the '%1' has been copy to buffer")
                            .replace("%1", rNote.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyRAW64);

        JMenuItem saveJson = new JMenuItem(Lang.T("Save as JSON"));
        saveJson.addActionListener(e -> {
            if (rNote == null)
                return;
            Library.saveJSONtoFileSystem(this, rNote, "tx" + rNote.viewHeightSeq());

        });
        menuSaveCopy.add(saveJson);

        JMenuItem saveRAW = new JMenuItem(Lang.T("Save RAW (bytecode) as Base58"));
        saveRAW.addActionListener(e -> {
            if (rNote == null)
                return;
            Library.saveAsBase58FileSystem(this, rNote.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + rNote.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW);

        JMenuItem saveRAW64 = new JMenuItem(Lang.T("Save RAW (bytecode) as Base64"));
        saveRAW64.addActionListener(e -> {
            if (rNote == null)
                return;
            Library.saveAsBase64FileSystem(this, rNote.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + rNote.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW64);

        menu.addSeparator();

        // favorite menu
        getPasswordMenuItems.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (rNote == null)
                    return;

                if (rNote.isEncrypted()) {

                    Controller cntr = Controller.getInstance();
                    if (!cntr.isWalletUnlocked()) {
                        //ASK FOR PASSWORD
                        String password = PasswordPane.showUnlockWalletDialog(null);
                        if (!cntr.unlockWallet(password)) {
                            //WRONG PASSWORD
                            JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    Account account = cntr.getInvolvedAccount(rNote);
                    Fun.Tuple3<Integer, String, byte[]> result = rNote.getPassword(account);
                    if (result.a < 0) {
                        JOptionPane.showMessageDialog(null,
                                Lang.T(result.b == null ? "Not exists Account access" : result.b),
                                Lang.T("Not decrypted"), JOptionPane.ERROR_MESSAGE);
                        return;

                    } else if (result.b != null) {
                        JOptionPane.showMessageDialog(null,
                                Lang.T(" In pos: " + result.a + " - " + result.b),
                                Lang.T("Not decrypted"), JOptionPane.ERROR_MESSAGE);
                        return;

                    }

                    StringSelection stringSelection = new StringSelection(Base58.encode(result.c));
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.T("Password of the '%1' has been copy to buffer")
                                    .replace("%1", rNote.viewHeightSeq())
                                    + ".",
                            Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

                }
            }
        });
        menu.add(getPasswordMenuItems);

        menu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) {
                    return;
                }

                if (rNote == null)
                    return;

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?tx=" + rNote.viewHeightSeq()));
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
                if (e.getClickCount() == 1 & e.getButton() == e.BUTTON1) {
                    if (jTableJScrollPanelLeftPanel.getSelectedColumn() == StatementsSplitPanel.this.tableModel.COLUMN_FAVORITE) {
                        favoriteSet(rNote);
                        int row = jTableJScrollPanelLeftPanel.rowAtPoint(p);
                        if (row >= jTableJScrollPanelLeftPanel.getRowCount())
                            row--;
                        if (row < 0)
                            return;

                        jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);
                    }
                }
            }
        });

        jTableJScrollPanelLeftPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {

                if (jTableJScrollPanelLeftPanel.columnAtPoint(e.getPoint()) == StatementsSplitPanel.this.tableModel.COLUMN_FAVORITE) {
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
        tableModel.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        //if (c1 instanceof RNoteInfo) ((RNoteInfo) c1).delay_on_Close();

    }

    abstract RSignNote getTransaction(T rNote);

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
            String search = searchTextFieldSearchToolBarLeftPanelDocument.getText();

            // SET FILTER
            //tableModelPersons.getSortableList().setFilter(search);
            tableModel.fireTableDataChanged();

            RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
            ((DefaultRowSorter) tableSorter).setRowFilter(filter);

            tableModel.fireTableDataChanged();

        }
    }

    // listener select row
    class selectRowListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0
                    || jTableJScrollPanelLeftPanel.getSelectedRow() >= jTableJScrollPanelLeftPanel.getRowCount()) {
                rNote = null;
                return;
            }

            currentItem = (T) tableModel.getItem(jTableJScrollPanelLeftPanel.
                    convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));

            rNote = getTransaction(currentItem);
            favoriteMenuItems.setText(Lang.T(wallet.isDocumentFavorite(rNote) ? "Remove Favorite" : "Add Favorite"));

            getPasswordMenuItems.setEnabled(rNote.isEncrypted());

            JPanel info_panel = TransactionDetailsFactory.getInstance().createTransactionDetail(rNote);
            RNoteInfo rNoteInfo = new RNoteInfo(rNote); // here load all values and calc FEE

            info_panel.setPreferredSize(new Dimension(jScrollPaneJPanelRightPanel.getSize().width - 50, jScrollPaneJPanelRightPanel.getSize().height - 50));
            jScrollPaneJPanelRightPanel.setViewportView(rNoteInfo);
            //	jSplitPanel.setRightComponent(info_panel);
        }
    }

    private void favoriteSet(Transaction transaction) {
        // CHECK IF FAVORITES
        if (wallet.isDocumentFavorite(transaction)) {
            int showConfirmDialog = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.T("Delete from favorite") + "?", Lang.T("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);
            if (showConfirmDialog == 0) {
                wallet.removeDocumentFavorite(transaction);
            }
        } else {
            wallet.addDocumentFavorite(transaction);
        }
        ((TimerTableModelCls) jTableJScrollPanelLeftPanel.getModel()).fireTableDataChanged();

    }

}
