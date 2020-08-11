package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.WalletTableRenderer;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.RendererBigDecimals;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.gui.records.VouchRecordDialog;
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
import java.awt.event.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    // Variables declaration - do not modify
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
    Wallet wallet = Controller.getInstance().wallet;

    /**
     * Creates new form НовыйJPanel
     */
    public AccountsRightPanel() {
        wTxMap = Controller.getInstance().wallet.database.getTransactionMap();
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
/*
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        */
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
        viewInfo = new JMenuItem(Lang.getInstance().translate("View Transaction"));
        viewInfo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                AccountsTransactionsTableModel.Trans transaction = tableModel.getItem(th.row);
                IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction.transaction, (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Lang.getInstance().translate("Transaction"));
                dd.setLocationRelativeTo(th);
                dd.setVisible(true);
            }

        });
        mainMenu.add(viewInfo);

        JMenuItem vouch_menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                AccountsTransactionsTableModel.Trans transaction = tableModel.getItem(th.row);
                new VouchRecordDialog(transaction.transaction.getBlockHeight(), transaction.transaction.getSeqNo());

            }
        });

        mainMenu.add(vouch_menu);

        // save jsot transactions
        JMenuItem item_Save = new JMenuItem(Lang.getInstance().translate("Save"));
        item_Save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                AccountsTransactionsTableModel.Trans transaction = tableModel.getItem(th.row);
                // save
                Library.saveTransactionJSONtoFileSystem(getParent(), transaction.transaction);
            }


        });

        mainMenu.add(item_Save);

        mainMenu.addSeparator();
        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

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

        //   jTable1.setComponentPopupMenu(mainMenu);
        TableMenuPopupUtil.installContextMenu(jTable1, mainMenu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON


        // SELECT
        jTable1.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    Point p = e.getPoint();
                    th.row = jTable1.rowAtPoint(p);
                    //jTable1.setRowSelectionInterval(th.row, th.row);
                    //if (((WTransactionMap) table_Model.getMap()).isUnViewed(table_Model.getItem(th.row).walletKey)) {
                    //    jTable1.setSelectionForeground(Color.white);
                    //    jTable1.setSelectionBackground(Color.red);
                    //}

                    AccountsTransactionsTableModel.Trans rowItem = tableModel.getItem(th.row);
                    Transaction transaction = rowItem.transaction;
                    rowItem.isUnViewed = false;
                    ((WTransactionMap) tableModel.getMap()).clearUnViewed(transaction);
                    //table_Model.fireTableCellUpdated(th.row);

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
            int showConfirmDialog = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.getInstance().translate("Delete from favorite") + "?", Lang.getInstance().translate("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);
            if (showConfirmDialog == 0) {
                wallet.removeTransactionFavorite(transaction);
            }
        } else {
            wallet.addTransactionFavorite(transaction);
        }
        ((TimerTableModelCls) jTable1.getModel()).fireTableDataChanged();

    }

}
