package org.erachain.gui.items.records;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.WalletTableRenderer;
import org.erachain.gui.items.statement.IssueDocumentPanel;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.SetIntervalPanel;
import org.erachain.gui.library.SignLibraryPanel;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.gui.models.WalletTransactionsTableModel;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;
import org.mapdb.Fun.Tuple2;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyTransactionsSplitPanel extends SplitPanel {

    public static String NAME = "MyTransactionsSplitPanel";
    public static String TITLE = "My Transactions";

    private static final long serialVersionUID = 2717571093561259483L;
    private static MyTransactionsSplitPanel instance;
    public SignLibraryPanel voush_Library_Panel;
    protected Tuple2<Long, Long> selectedTransactionKey;
    protected Transaction selectedTransaction;
    private JPanel records_Info_Panel;
    private JPopupMenu menu;
    private JMenuItem item_Delete;
    private JMenuItem item_Rebroadcast;
    private JMenuItem copyNumber;
    private JMenuItem vouchMenu;
    public WalletTransactionsTableModel recordsModel;
    public SetIntervalPanel setIntervalPanel;

    private JMenuItem item_Save;
    Wallet wallet = Controller.getInstance().getWallet();

    private MyTransactionsSplitPanel() {
        super(NAME, TITLE);
        this.leftPanel.setVisible(true);

        //CREATE TABLE
        this.recordsModel = new WalletTransactionsTableModel();
        this.jTableJScrollPanelLeftPanel = new MTable(this.recordsModel);
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Object.class, new WalletTableRenderer());
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Boolean.class, new WalletTableRenderer());
        //jTableJScrollPanelLeftPanel.setDefaultRenderer(Number.class, new WalletTableRenderer());

        this.jScrollPanelLeftPanel.setViewportView(this.jTableJScrollPanelLeftPanel);

        TableColumnModel columnModel = jTableJScrollPanelLeftPanel.getColumnModel();
        columnModel.getColumn(recordsModel.COLUMN_FAVORITE).setMaxWidth((100));
        columnModel.getColumn(recordsModel.COLUMN_FAVORITE).setMaxWidth((100));

        // not show buttons
        jToolBarRightPanel.setVisible(true);
        // toolBarLeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setText("<HTML><B> " + Lang.T("Record") + "</></> ");
        jButton1_jToolBar_RightPanel.setBorderPainted(true);
        jButton1_jToolBar_RightPanel.setFocusable(true);
        jButton1_jToolBar_RightPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
                javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        jButton1_jToolBar_RightPanel.setSize(120, 30);

        jButton1_jToolBar_RightPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClick();
            }
        });

        // mouse from favorite column
        jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                Point point = e.getPoint();
                java.util.Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        int row = jTableJScrollPanelLeftPanel.rowAtPoint(point);
                        jTableJScrollPanelLeftPanel.setRowSelectionInterval(row, row);
                        row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);

                        Transaction itemTableSelected = recordsModel.getItem(row).b;

                        if (e.getClickCount() == 2) {
                            //tableMouse2Click(itemTableSelected);
                        }

                        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                            if (jTableJScrollPanelLeftPanel.getSelectedColumn() == WalletTransactionsTableModel.COLUMN_FAVORITE) {
                                favoriteSet(itemTableSelected);
                            }
                        }
                    }
                }, 10);
            }
        });


        this.jToolBarRightPanel.setVisible(false);

        //  left panel tool bat visivle
        jButton2_jToolBar_RightPanel.setVisible(false);

        // button 1 in left tool bar
        toolBarLeftPanel.setVisible(true);

        button1ToolBarLeftPanel.setText(Lang.T("Reset Unread"));
        button1ToolBarLeftPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recordsModel.clearAllOnlyUndead();
            }
        });


        // button 2 in left tool bar
        button2ToolBarLeftPanel.setText(Lang.T("Unread Only"));
        button2ToolBarLeftPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button2ToolBarLeftPanel.setText((Lang.T(button2ToolBarLeftPanel
                        .isSelected() ? "See All" : "Unread Only")));

                recordsModel.setOnlyUndead();
            }
        });

        // set interval panel
        setIntervalPanel = new SetIntervalPanel(wallet.dwSet.getTransactionMap());
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        leftPanel.add(setIntervalPanel, gridBagConstraints);

        setIntervalPanel.jButtonSetInterval.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                setInterval();
            }
        });

        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

        menu = new JPopupMenu();
        menu.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                // TODO Auto-generated method stub
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // TODO Auto-generated method stub
                // TODO Auto-generated method stub

            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // TODO Auto-generated method stub
                int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                if (row < 0) {
                    selectedTransaction = null;
                    return;
                }
                row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                if (row < 0) {
                    selectedTransaction = null;
                    return;
                }
                selectedTransaction = recordsModel.getItem(row).b;

            }
        });

        JMenuItem itemCheckTX = new JMenuItem(Lang.T("Validate"));
        itemCheckTX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MyTransactionsSplitPanel.validate(selectedTransaction);
            }
        });
        menu.add(itemCheckTX);

        item_Rebroadcast = new JMenuItem(Lang.T("Rebroadcast"));
        item_Rebroadcast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // code Rebroadcast

                if (selectedTransaction == null) return;
                Controller.getInstance().broadcastTransaction(selectedTransaction);

            }
        });
        menu.add(item_Rebroadcast);

        item_Delete = new JMenuItem(Lang.T("Delete"));
        item_Delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (selectedTransaction == null)
                    return;

                // code delete
                Controller.getInstance().getWallet().dwSet.getTransactionMap()
                        .delete(selectedTransaction);

            }
        });
        menu.add(item_Delete);

        vouchMenu = new JMenuItem(Lang.T("Sign / Vouch"));
        vouchMenu.addActionListener(e -> {
            new toSignRecordDialog(selectedTransaction.getBlockHeight(), selectedTransaction.getSeqNo());

        });
        menu.add(vouchMenu);

        JMenuItem linkMenu = new JMenuItem(Lang.T("Append Document"));
        linkMenu.addActionListener(e -> {
            MainPanel.getInstance().insertNewTab(
                    Lang.T("For # для") + " " + selectedTransaction.viewHeightSeq(),
                    new IssueDocumentPanel(null, ExData.LINK_APPENDIX_TYPE, selectedTransaction.viewHeightSeq(), null));

        });
        menu.add(linkMenu);

        JMenu menuSaveCopy = new JMenu(Lang.T("Save / Copy"));
        menu.add(menuSaveCopy);

        copyNumber = new JMenuItem(Lang.T("Copy Number"));
        copyNumber.addActionListener(e -> {
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
            Library.saveJSONtoFileSystem(this, selectedTransaction, "tx" + selectedTransaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveJson);

        JMenuItem saveRAW = new JMenuItem(Lang.T("Save RAW (bytecode) as Base58"));
        saveRAW.addActionListener(e -> {
            Library.saveAsBase58FileSystem(this, selectedTransaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + selectedTransaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW);

        JMenuItem saveRAW64 = new JMenuItem(Lang.T("Save RAW (bytecode) as Base64"));
        saveRAW64.addActionListener(e -> {
            Library.saveAsBase64FileSystem(this, selectedTransaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + selectedTransaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW64);


        menu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (selectedTransaction == null) {
                    return;
                }

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?tx=" + selectedTransaction.viewHeightSeq()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });
        menu.add(setSeeInBlockexplorer);

        TableMenuPopupUtil.installContextMenu(jTableJScrollPanelLeftPanel, menu);
        menu.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                // TODO Auto-generated method stub
                if (selectedTransaction == null) return;

                boolean isConfirmed = selectedTransaction.isConfirmed(DCSet.getInstance());

                item_Delete.setEnabled(!isConfirmed);
                item_Rebroadcast.setEnabled(!isConfirmed);
                vouchMenu.setEnabled(isConfirmed);
                copyNumber.setEnabled(isConfirmed);

            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                // TODO Auto-generated method stub

            }

        });

        jTableJScrollPanelLeftPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {

                if (jTableJScrollPanelLeftPanel.columnAtPoint(e.getPoint()) == recordsModel.COLUMN_FAVORITE) {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

                } else {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            public void mouseDragged(MouseEvent e) {
            }
        });

    }

    public static MyTransactionsSplitPanel getInstance() {

        if (instance == null) {
            instance = new MyTransactionsSplitPanel();
        } else {
            // восстановим наблюдения
            instance.recordsModel.addObservers();
            instance.setIntervalPanel.addObservers();
        }

        return instance;

    }

    public static void validate(Transaction transaction) {
        if (transaction == null) return;

        DCSet dcSet = DCSet.getInstance();
        if (!transaction.isSignatureValid(dcSet)) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Signature Invalid") + "!",
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        transaction.setDC(dcSet, false);
        if (transaction.getConfirmations(dcSet) <= 0) {
            Long dbRef = dcSet.getTransactionFinalMapSigns().get(transaction.getSignature());
            if (dbRef == null) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.T("Transaction unconfirmed") + "!",
                        Lang.T("Wrong"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            transaction.setHeightSeq(dbRef);
        }

        Controller.getInstance().getWallet().processTransaction(transaction);
        JOptionPane.showMessageDialog(new JFrame(),
                Lang.T("Transaction confirmed") + "!",
                Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

    }


    public void onClick() {
    }

    //@Override
    public void onClose() {
        // delete observer left panel
        this.recordsModel.deleteObservers();
        this.setIntervalPanel.deleteObservers();
        // get component from right panel
        //	Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        //	  if (c1.getClass() == this.records_Info_Panel.getClass()) voush_Library_Panel.onClose();

    }

    // listener select row
    class search_listener implements ListSelectionListener {


        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() >= 0 && jTableJScrollPanelLeftPanel.getSelectedRow() < recordsModel.getRowCount()) {
                selectedTransaction = recordsModel
                        .getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;

                ((WTransactionMap) recordsModel.getMap()).clearUnViewed(selectedTransaction);

                records_Info_Panel = new JPanel();
                records_Info_Panel.setLayout(new GridBagLayout());

                // TABLE GBC
                GridBagConstraints tableGBC = new GridBagConstraints();
                tableGBC.fill = GridBagConstraints.BOTH;
                tableGBC.anchor = GridBagConstraints.FIRST_LINE_START;
                tableGBC.weightx = 1;
                tableGBC.weighty = 1;
                tableGBC.gridx = 0;
                tableGBC.gridy = 0;
                records_Info_Panel.add(TransactionDetailsFactory.getInstance().createTransactionDetail(selectedTransaction), tableGBC);

                Tuple2<BigDecimal, List<Long>> keys = DCSet.getInstance().getVouchRecordMap()
                        .get(Transaction.makeDBRef(selectedTransaction.getBlockHeight(), selectedTransaction.getSeqNo()));
                GridBagConstraints gridBagConstraints = null;
                if (keys != null) {

                    JLabel jLabelTitlt_Table_Sign = new JLabel(Lang.T("Signatures") + ":");
                    gridBagConstraints = new java.awt.GridBagConstraints();
                    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
                    gridBagConstraints.weightx = 0.1;
                    gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 11);
                    gridBagConstraints.gridx = 0;
                    gridBagConstraints.gridy = 1;
                    records_Info_Panel.add(jLabelTitlt_Table_Sign, gridBagConstraints);
                    gridBagConstraints = new java.awt.GridBagConstraints();
                    gridBagConstraints.gridx = 0;
                    gridBagConstraints.gridy = 2;
                    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
                    gridBagConstraints.weightx = 1.0;
                    gridBagConstraints.weighty = 1.0;
                    voush_Library_Panel = new SignLibraryPanel(selectedTransaction);
                    records_Info_Panel.add(voush_Library_Panel, gridBagConstraints);

                }

                jScrollPaneJPanelRightPanel.setViewportView(records_Info_Panel);

            }
        }
    }

    public void setInterval() {
        Integer start = 0;
        try {
            start = Integer.valueOf(setIntervalPanel.jTextFieldStart.getText());
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            return;
        }
        Integer end = 0;
        try {
            end = Integer.valueOf(setIntervalPanel.jTextFieldEnd.getText());
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            return;
        }
        if (end > start) {
            int step = end - start;
            this.recordsModel.setInterval(start);
            this.recordsModel.fireTableDataChanged();
        }
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
        ((TimerTableModelCls) jTableJScrollPanelLeftPanel.getModel()).fireTableDataChanged();

    }

}