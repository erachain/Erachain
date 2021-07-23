package org.erachain.gui.items.records;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.WalletTableRenderer;
import org.erachain.gui.items.statement.IssueDocumentPanel;
import org.erachain.gui.items.statement.SearchStatementsTableModel;
import org.erachain.gui.library.ASMakeHashMenuItem;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.SignLibraryPanel;
import org.erachain.gui.models.SearchTableModelCls;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.MenuPopupUtil;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;
import org.mapdb.Fun.Tuple2;

import javax.swing.*;
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

/**
 * search transactions
 */
public abstract class SearchTransactionsSplitPanelClass<T> extends SplitPanel {

    Controller cnt = Controller.getInstance();
    public JPanel info_Panel;
    public JButton buttonGetLasts = new JButton(Lang.T("Get Last"));
    public SignLibraryPanel voush_Library_Panel;
    public SearchTableModelCls transactionsTableModel;
    public Transaction selectedTransaction;
    private JTextField searchString;
    private boolean forDocuments;

    public SearchTransactionsSplitPanelClass(String name, String title, SearchTableModelCls tableModel) {
        super(name, title);

        transactionsTableModel = tableModel;
        forDocuments = tableModel instanceof SearchStatementsTableModel;

        this.searchToolBar_LeftPanel.setVisible(true);

        this.searthLabel2.setText(Lang.T("Height or seqNo") + ": ");
        this.toolBarLeftPanel.add(new JLabel(Lang.T("Search") + ": "));

        searchString = new JTextField();
        searchString.setToolTipText("");

        searchString.setMinimumSize(new Dimension(350, UIManager.getFont("Label.font").getSize() + UIManager.getFont("Label.font").getSize() / 2));
        searchString.setName(""); // NOI18N
        searchString.setPreferredSize(new Dimension(350, UIManager.getFont("Label.font").getSize() + UIManager.getFont("Label.font").getSize() / 2));
        searchString.setMaximumSize(new Dimension(2000, UIManager.getFont("Label.font").getSize() + UIManager.getFont("Label.font").getSize() / 2));

        MenuPopupUtil.installContextMenu(searchString);

        this.toolBarLeftPanel.add(searchString);
        searchString.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                transactionsTableModel.clear();
                transactionsTableModel.find(searchString.getText(), null);

            }

        });

        this.toolBarLeftPanel.add(buttonGetLasts);
        buttonGetLasts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                transactionsTableModel.clear();
                transactionsTableModel.find(null, null);

            }

        });

        this.button1ToolBarLeftPanel.setVisible(false);
        this.button2ToolBarLeftPanel.setVisible(false);
        this.searchFavoriteJCheckBoxLeftPanel.setVisible(false);
        this.searchMyJCheckBoxLeftPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.jButton2_jToolBar_RightPanel.setVisible(false);

        // make hash item from popup menu
        ASMakeHashMenuItem makeHashButton = new ASMakeHashMenuItem(searchString);
        this.toolBarLeftPanel.add(makeHashButton);

        MenuPopupUtil.installContextMenu(searchString);
        MenuPopupUtil.installContextMenu(searchTextField2);


        this.searchTextField2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                //searchString.setText("");
                transactionsTableModel.clear();
                transactionsTableModel.setBlockNumber(searchTextField2.getText());
                if (transactionsTableModel.getRowCount() == 1)
                    jTableJScrollPanelLeftPanel.addRowSelectionInterval(0, 0);

            }

        });

        //TRANSACTIONS TABLE MODEL
        //this.transactionsTableModel = new SearchTransactionsTableModel();
        this.jTableJScrollPanelLeftPanel = new MTable(this.transactionsTableModel);
        TableColumnModel columnModel = jTableJScrollPanelLeftPanel.getColumnModel();
        columnModel.getColumn(transactionsTableModel.COLUMN_FAVORITE).setMaxWidth((100));
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Object.class, new WalletTableRenderer());
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Boolean.class, new WalletTableRenderer());

        this.jTableJScrollPanelLeftPanel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // MENU
        JPopupMenu mainMenu = new JPopupMenu();

        JMenuItem vouch_menu = new JMenuItem(Lang.T("Sign / Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (selectedTransaction == null) return;
                new toSignRecordDialog(selectedTransaction.getBlockHeight(), selectedTransaction.getSeqNo());

            }
        });

        mainMenu.add(vouch_menu);

        JMenuItem linkMenu = new JMenuItem(Lang.T("Append Document"));
        linkMenu.addActionListener(e -> {
            if (selectedTransaction == null) return;
            MainPanel.getInstance().insertNewTab(
                    Lang.T("For # для") + " " + selectedTransaction.viewHeightSeq(),
                    new IssueDocumentPanel(null, ExData.LINK_APPENDIX_TYPE, selectedTransaction.viewHeightSeq(), null));

        });
        mainMenu.add(linkMenu);

        JMenu menuSaveCopy = new JMenu(Lang.T("Save / Copy"));
        mainMenu.add(menuSaveCopy);

        JMenuItem copyNumber = new JMenuItem(Lang.T("Copy Number"));
        copyNumber.addActionListener(e -> {
            if (selectedTransaction == null) return;
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
            if (selectedTransaction == null) return;
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
            if (selectedTransaction == null) return;
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
            if (selectedTransaction == null) return;
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
            if (selectedTransaction == null) return;
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
            if (selectedTransaction == null) return;
            Library.saveJSONtoFileSystem(this, selectedTransaction, "tx" + selectedTransaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveJson);

        JMenuItem saveRAW = new JMenuItem(Lang.T("Save RAW (bytecode) as Base58"));
        saveRAW.addActionListener(e -> {
            if (selectedTransaction == null) return;
            Library.saveAsBase58FileSystem(this, selectedTransaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + selectedTransaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW);

        JMenuItem saveRAW64 = new JMenuItem(Lang.T("Save RAW (bytecode) as Base64"));
        saveRAW64.addActionListener(e -> {
            if (selectedTransaction == null) return;
            Library.saveAsBase64FileSystem(this, selectedTransaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + selectedTransaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW64);


        mainMenu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (selectedTransaction == null) return;

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?tx=" + selectedTransaction.viewHeightSeq()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });
        mainMenu.add(setSeeInBlockexplorer);

        TableMenuPopupUtil.installContextMenu(this.jTableJScrollPanelLeftPanel, mainMenu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        this.jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new search_listener());

        //TRANSACTION DETAILS
        this.jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    //GET ROW
                    if (selectedTransaction == null) return;

                    //SHOW DETAIL SCREEN OF TRANSACTION
                    TransactionDetailsFactory.createTransactionDetail(selectedTransaction);
                }
            }
        });

        jTableJScrollPanelLeftPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {

                if (jTableJScrollPanelLeftPanel
                        .columnAtPoint(e.getPoint()) == transactionsTableModel.COLUMN_FAVORITE) {

                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            public void mouseDragged(MouseEvent e) {
            }
        });

        jTableJScrollPanelLeftPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int row = jTableJScrollPanelLeftPanel.rowAtPoint(p);

                if (e.getClickCount() == 1 & e.getButton() == MouseEvent.BUTTON1) {

                    if (jTableJScrollPanelLeftPanel
                            .getSelectedColumn() == transactionsTableModel.COLUMN_FAVORITE) {
                        if (selectedTransaction == null) return;
                        favorite_set(selectedTransaction);
                    }
                }
            }
        });

        this.jScrollPanelLeftPanel.setViewportView(this.jTableJScrollPanelLeftPanel);

    }

    @Override
    public void onClose() {
    }

    public void listener() {
        transactionsTableModel.setBlockNumber(searchTextField2.getText());
        if (transactionsTableModel.getRowCount() == 1)
            jTableJScrollPanelLeftPanel.addRowSelectionInterval(0, 0);
    }

    // listener select row
    class search_listener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            if (jTableJScrollPanelLeftPanel.getSelectedRow() >= 0 && jTableJScrollPanelLeftPanel.getSelectedRow() < transactionsTableModel.getRowCount()) {
                selectedTransaction = transactionsTableModel
                        .getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));


                info_Panel = new JPanel();
                info_Panel.setLayout(new GridBagLayout());

                //TABLE GBC
                GridBagConstraints tableGBC = new GridBagConstraints();
                tableGBC.fill = GridBagConstraints.BOTH;
                tableGBC.anchor = GridBagConstraints.FIRST_LINE_START;
                tableGBC.weightx = 1;
                tableGBC.weighty = 1;
                tableGBC.gridx = 0;
                tableGBC.gridy = 0;
                info_Panel.add(TransactionDetailsFactory.createTransactionDetail(selectedTransaction), tableGBC);

                Tuple2<BigDecimal, List<Long>> keys = DCSet.getInstance().getVouchRecordMap().get(Transaction.makeDBRef(selectedTransaction.getBlockHeight(), selectedTransaction.getSeqNo()));
                GridBagConstraints gridBagConstraints = null;
                if (keys != null) {

                    JLabel jLabelTitlt_Table_Sign = new JLabel(Lang.T("Signatures") + ":");
                    gridBagConstraints = new GridBagConstraints();
                    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                    gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
                    gridBagConstraints.weightx = 0.1;
                    gridBagConstraints.insets = new Insets(12, 11, 0, 11);
                    gridBagConstraints.gridx = 0;
                    gridBagConstraints.gridy = 1;
                    info_Panel.add(jLabelTitlt_Table_Sign, gridBagConstraints);


                    gridBagConstraints = new GridBagConstraints();
                    gridBagConstraints.gridx = 0;
                    gridBagConstraints.gridy = 2;
                    gridBagConstraints.fill = GridBagConstraints.BOTH;
                    gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
                    gridBagConstraints.weightx = 1.0;
                    gridBagConstraints.weighty = 1.0;
                    voush_Library_Panel = new SignLibraryPanel(selectedTransaction);
                    info_Panel.add(voush_Library_Panel, gridBagConstraints);

                }

                jScrollPaneJPanelRightPanel.setViewportView(info_Panel);

            }
        }
    }

    public void favorite_set(Transaction transaction) {

        if (forDocuments) {
            // CHECK IF FAVORITES
            if (cnt.isDocumentFavorite(transaction)) {
                int dd = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.T("Delete from favorite") + "?", Lang.T("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);

                if (dd == 0) cnt.removeDocumentFavorite(transaction);
            } else {
                cnt.addDocumentFavorite(transaction);
            }

        } else {
            // CHECK IF FAVORITES
            if (cnt.isTransactionFavorite(transaction)) {
                int dd = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.T("Delete from favorite") + "?", Lang.T("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);

                if (dd == 0) cnt.removeTransactionFavorite(transaction);
            } else {
                cnt.addTransactionFavorite(transaction);
            }

        }
        jTableJScrollPanelLeftPanel.repaint();

    }
}