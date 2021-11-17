package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.WalletTableRenderer;
import org.erachain.gui.items.records.MyTransactionsSplitPanel;
import org.erachain.gui.items.statement.IssueDocumentPanel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.RendererBigDecimals;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.gui.records.VouchTransactionDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

public class AccountsRightPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public AccountsTransactionsTableModel tableModel;
    @SuppressWarnings("rawtypes")
    public MTable jTable1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    public javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private JPopupMenu mainMenu;
    private JMenuItem viewInfo;
    private AccountsRightPanel th;
    protected int row;

    WTransactionMap wTxMap;
    Wallet wallet = Controller.getInstance().getWallet();

    /**
     * Creates new form НовыйJPanel
     */
    public AccountsRightPanel() {
        wTxMap = wallet.dwSet.getTransactionMap();
        initComponents();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        th = this;
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();
        jToggleButton1 = new javax.swing.JToggleButton();
        new javax.swing.JPopupMenu();
        jMenu5 = new javax.swing.JMenu();
        jToggleButton2 = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableModel = new AccountsTransactionsTableModel();
        jTable1 = new MTable(tableModel);

        jTable1.setDefaultRenderer(Object.class, new WalletTableRenderer());
        jTable1.setDefaultRenderer(Boolean.class, new WalletTableRenderer());
        TableColumnModel columnModel = jTable1.getColumnModel();
        //columnModel.getColumn(0).setMaxWidth((100));
        columnModel.getColumn(tableModel.COLUMN_SEQNO).setPreferredWidth(100);
        columnModel.getColumn(tableModel.COLUMN_SEQNO).setMaxWidth(150);
        columnModel.getColumn(tableModel.COLUMN_AMOUNT).setPreferredWidth(150);
        columnModel.getColumn(tableModel.COLUMN_AMOUNT).setMaxWidth(200);
        columnModel.getColumn(tableModel.COLUMN_FAVORITE).setPreferredWidth(100);
        columnModel.getColumn(tableModel.COLUMN_FAVORITE).setMaxWidth(150);

        columnModel.getColumn(tableModel.COLUMN_ITEM_CLS).setCellRenderer(new WalletTableRenderer());


        if (false) {
            // не правильная сортировка - по существующим только и не дает неподтвержденные сюда внести
            // и при этом еще у записей Номера блоков обновляет и присваивает для неподтвержденных как будто они включенв в +1 блок верхний
            // темболее что сейчас основная сортировка в кошельке - по времени что для не подтвержденных так же правильно

            // sort from column
            @SuppressWarnings("unchecked")
            TableRowSorter t = new TableRowSorter(tableModel);
            // comparator
            t.setComparator(tableModel.COLUMN_SEQNO, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    BigDecimal transaction1 = Library.getBlockSegToBigInteger(DCSet.getInstance().getTransactionFinalMap().getRecord(o1));
                    BigDecimal transaction2 = Library.getBlockSegToBigInteger(DCSet.getInstance().getTransactionFinalMap().getRecord(o2));
                    return transaction1.compareTo(transaction2);
                }
            });

            // sort list  - AUTO sort
            List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
            sortKeys.add(new RowSorter.SortKey(tableModel.COLUMN_SEQNO, SortOrder.DESCENDING));
            t.setSortKeys(sortKeys);
            // sort table
            jTable1.setRowSorter(t);

        }

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jMenu3.setText("File");
        jMenuBar2.add(jMenu3);

        jMenu4.setText("Edit");
        jMenuBar2.add(jMenu4);

        jToggleButton1.setText("jToggleButton1");

        jMenu5.setText("jMenu5");

        jToggleButton2.setText("jToggleButton2");

        setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(jTable1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(jScrollPane1, gridBagConstraints);

        mainMenu = new JPopupMenu();
        mainMenu.addPopupMenuListener(new PopupMenuListener() {

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
                int row1 = jTable1.getSelectedRow();
                if (row1 < 0) return;

                row = jTable1.convertRowIndexToModel(row1);

            }
        });

        JMenuItem itemCheckTX = new JMenuItem(Lang.T("Validate"));
        itemCheckTX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Transaction selectedTransaction = tableModel.getItem(th.row).transaction;
                MyTransactionsSplitPanel.validate(selectedTransaction);
            }
        });
        mainMenu.add(itemCheckTX);

        viewInfo = new JMenuItem(Lang.T("See Details"));
        viewInfo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                AccountsTransactionsTableModel.Trans transaction = tableModel.getItem(th.row);
                IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction.transaction, (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Lang.T("Transaction"));
                dd.setLocationRelativeTo(th);
                dd.setVisible(true);
            }

        });
        mainMenu.add(viewInfo);

        JMenuItem vouch_menu = new JMenuItem(Lang.T("Sign / Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                AccountsTransactionsTableModel.Trans transaction = tableModel.getItem(th.row);
                new VouchTransactionDialog(transaction.transaction.getBlockHeight(), transaction.transaction.getSeqNo());

            }
        });
        mainMenu.add(vouch_menu);

        JMenuItem issueNote = new JMenuItem(Lang.T("Append Document"));
        issueNote.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AccountsTransactionsTableModel.Trans transaction = tableModel.getItem(th.row);
                MainPanel.getInstance().insertNewTab(
                        Lang.T("For # для") + " " + transaction.transaction.viewHeightSeq(),
                        new IssueDocumentPanel(null, ExData.LINK_APPENDIX_TYPE, transaction.transaction.viewHeightSeq(), null));
            }
        });
        mainMenu.add(issueNote);

        JMenu menuSaveCopy = new JMenu(Lang.T("Save / Copy"));
        mainMenu.add(menuSaveCopy);

        JMenuItem copyNumber = new JMenuItem(Lang.T("Copy Number"));
        copyNumber.addActionListener(e -> {
            Transaction selectedTransaction = tableModel.getItem(th.row).transaction;
            StringSelection stringSelection = new StringSelection(selectedTransaction.viewHeightSeq());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Number of the '%1' has been copy to buffer")
                            .replace("%1", selectedTransaction.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyNumber);

        JMenuItem copySign = new JMenuItem(Lang.T("Copy Signature"));
        copySign.addActionListener(e -> {
            Transaction selectedTransaction = tableModel.getItem(th.row).transaction;
            StringSelection stringSelection = new StringSelection(selectedTransaction.viewSignature());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Signature '%1' has been copy to buffer")
                            .replace("%1", selectedTransaction.viewSignature())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copySign);

        JMenuItem copyJson = new JMenuItem(Lang.T("Copy JSON"));
        copyJson.addActionListener(e -> {
            Transaction selectedTransaction = tableModel.getItem(th.row).transaction;
            StringSelection stringSelection = new StringSelection(selectedTransaction.toJson().toJSONString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("JSON of the '%1' has been copy to buffer")
                            .replace("%1", selectedTransaction.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyJson);

        JMenuItem copyRAW = new JMenuItem(Lang.T("Copy RAW (bytecode) as Base58"));
        copyRAW.addActionListener(e -> {
            Transaction selectedTransaction = tableModel.getItem(th.row).transaction;
            StringSelection stringSelection = new StringSelection(Base58.encode(selectedTransaction.toBytes(Transaction.FOR_NETWORK, true)));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Bytecode of the '%1' has been copy to buffer")
                            .replace("%1", selectedTransaction.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyRAW);

        JMenuItem copyRAW64 = new JMenuItem(Lang.T("Copy RAW (bytecode) as Base64"));
        copyRAW64.addActionListener(e -> {
            Transaction selectedTransaction = tableModel.getItem(th.row).transaction;
            StringSelection stringSelection = new StringSelection(Base64.getEncoder().encodeToString(selectedTransaction.toBytes(Transaction.FOR_NETWORK, true)));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Bytecode of the '%1' has been copy to buffer")
                            .replace("%1", selectedTransaction.viewHeightSeq())
                            + ".",
                    Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

        });
        menuSaveCopy.add(copyRAW64);

        JMenuItem saveJson = new JMenuItem(Lang.T("Save as JSON"));
        saveJson.addActionListener(e -> {
            Transaction selectedTransaction = tableModel.getItem(th.row).transaction;
            Library.saveJSONtoFileSystem(this, selectedTransaction, "tx" + selectedTransaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveJson);

        JMenuItem saveRAW = new JMenuItem(Lang.T("Save RAW (bytecode) as Base58"));
        saveRAW.addActionListener(e -> {
            Transaction selectedTransaction = tableModel.getItem(th.row).transaction;
            Library.saveAsBase58FileSystem(this, selectedTransaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + selectedTransaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW);

        JMenuItem saveRAW64 = new JMenuItem(Lang.T("Save RAW (bytecode) as Base64"));
        saveRAW64.addActionListener(e -> {
            Transaction selectedTransaction = tableModel.getItem(th.row).transaction;
            Library.saveAsBase64FileSystem(this, selectedTransaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + selectedTransaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW64);


        mainMenu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));
        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                AccountsTransactionsTableModel.Trans transaction = tableModel.getItem(th.row);

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?tx=" + transaction.transaction.viewHeightSeq()));
                } catch (MalformedURLException e1) {
                }
            }
        });
        mainMenu.add(setSeeInBlockexplorer);

        TableMenuPopupUtil.installContextMenu(jTable1, mainMenu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        // SELECT
        jTable1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    Point p = e.getPoint();
                    th.row = jTable1.rowAtPoint(p);

                    AccountsTransactionsTableModel.Trans rowItem = tableModel.getItem(th.row);
                    Transaction transaction = rowItem.transaction;
                    rowItem.isUnViewed = false;
                    ((WTransactionMap) tableModel.getMap()).clearUnViewed(transaction);

                    if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                        if (jTable1.getSelectedColumn() == AccountsTransactionsTableModel.COLUMN_FAVORITE) {
                            favoriteSet(rowItem.transaction);
                        }
                    }

                }
            }
        });

        jTable1.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {

                if (jTable1.columnAtPoint(e.getPoint()) == tableModel.COLUMN_FAVORITE) {
                    jTable1.setCursor(new Cursor(Cursor.HAND_CURSOR));

                } else {
                    jTable1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            public void mouseDragged(MouseEvent e) {
            }
        });

    }// </editor-fold>

    public void setAsset(AssetCls asset) {
        tableModel.setAsset(asset);
        tableModel.getInterval();
        tableModel.fireTableDataChanged();
        jTable1.setDefaultRenderer(BigDecimal.class, new RendererBigDecimals(asset.getScale()));
    }

    private void favoriteSet(Transaction transaction) {
        // CHECK IF FAVORITES
        if (wallet.isTransactionFavorite(transaction)) {
            int showConfirmDialog = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.T("Delete from favorite") + "?", Lang.T("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);
            if (showConfirmDialog == 0) {
                wallet.removeTransactionFavorite(transaction);
            }
        } else {
            wallet.addTransactionFavorite(transaction);
        }
        ((TimerTableModelCls) jTable1.getModel()).fireTableDataChanged();

    }

}
