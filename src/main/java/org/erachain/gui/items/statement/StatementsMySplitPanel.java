package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.gui.MainFrame;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.WalletTableRenderer;
import org.erachain.gui.items.records.MyTransactionsSplitPanel;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
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
import java.util.Timer;
import java.util.TimerTask;


public class StatementsMySplitPanel extends SplitPanel {

    public static String NAME = "StatementsMySplitPanel";
    public static String TITLE = "My Documents";

    private static final long serialVersionUID = 2717571093561259483L;

    // для прозрачности
    int alpha = 255;
    int alpha_int;
    MyStatementsTableModel my_Statements_Model;
    Wallet wallet = Controller.getInstance().wallet;


    private TableRowSorter search_Sorter;


    public StatementsMySplitPanel() {
        super(NAME, TITLE);

        // not show buttons
        this.button1ToolBarLeftPanel.setVisible(false);
        this.button2ToolBarLeftPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.jButton2_jToolBar_RightPanel.setVisible(false);


        // not show My filter
        this.searchMyJCheckBoxLeftPanel.setVisible(false);

        //TABLE


        my_Statements_Model = new MyStatementsTableModel();
        //	my_Statements_table = new JTable(my_Statements_Model);// new Statements_Table_Model();

        //	my_Statements_table.setTableHeader(null);
        //	my_Statements_table.setEditingColumn(0);
        //	my_Statements_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			
		/*	
			TableColumnModel columnModel = my_Statements_table.getColumnModel(); // read column model
			columnModel.getColumn(0).setMaxWidth((100));
			
			//Custom renderer for the String column;
			my_Statements_table.setDefaultRenderer(Long.class, new RendererRight()); // set renderer
			my_Statements_table.setDefaultRenderer(String.class, new RendererLeft()); // set renderer
					
					
			my_Sorter = new TableRowSorter(my_PersonsModel);
			my_Statements_table.setRowSorter(my_Sorter);
			my_Statements_table.getRowSorter();
			if (my_PersonsModel.getRowCount() > 0) my_PersonsModel.fireTableDataChanged();
			
			//CHECKBOX FOR CONFIRMED
			TableColumn confirmedColumn = my_Statements_table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_CONFIRMED);
			// confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			confirmedColumn.setCellRenderer(new RendererBoolean());
			confirmedColumn.setMinWidth(50);
			confirmedColumn.setMaxWidth(50);
			confirmedColumn.setPreferredWidth(50);//.setWidth(30);
			
			
			//CHECKBOX FOR FAVORITE
			TableColumn favoriteColumn = my_Statements_table.getColumnModel().getColumn(WalletItemPersonsTableModel.COLUMN_FAVORITE);
			//favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			favoriteColumn.setCellRenderer(new RendererBoolean());
			favoriteColumn.setMinWidth(50);
			favoriteColumn.setMaxWidth(50);
			favoriteColumn.setPreferredWidth(50);//.setWidth(30);
	
			// UPDATE FILTER ON TEXT CHANGE
			this.searchTextFieldSearchToolBarLeftPanelDocument.getDocument().addDocumentListener(new My_Search());
			*/        // SET VIDEO
        //this.jTableJScrollPanelLeftPanel.setModel(my_PersonsModel);
        this.jTableJScrollPanelLeftPanel = new MTable(my_Statements_Model); //my_Statements_table;
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Object.class, new WalletTableRenderer());
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Boolean.class, new WalletTableRenderer());


        TableColumnModel columnModel = jTableJScrollPanelLeftPanel.getColumnModel();
        columnModel.getColumn(my_Statements_Model.COLUMN_SEQNO).setPreferredWidth(150);
        columnModel.getColumn(my_Statements_Model.COLUMN_SEQNO).setMaxWidth(150);
        columnModel.getColumn(my_Statements_Model.COLUMN_FAVORITE).setPreferredWidth(70);
        columnModel.getColumn(my_Statements_Model.COLUMN_FAVORITE).setMaxWidth(100);

        //this.jTableJScrollPanelLeftPanel.setTableHeader(null);
        // sorter
        search_Sorter = new TableRowSorter(my_Statements_Model);


        // hand cursor for Favorite column
        jTableJScrollPanelLeftPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                if (jTableJScrollPanelLeftPanel.columnAtPoint(e.getPoint()) == StatementsMySplitPanel.this.my_Statements_Model.COLUMN_FAVORITE) {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    jTableJScrollPanelLeftPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        // mouse from favorine column
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

                        Transaction itemTableSelected = getItem(row);

                        if (e.getClickCount() == 2) {
                            //tableMouse2Click(itemTableSelected);
                        }

                        if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                            if (jTableJScrollPanelLeftPanel.getSelectedColumn() == StatementsMySplitPanel.this.my_Statements_Model.COLUMN_FAVORITE) {
                                favoriteSet(itemTableSelected);
                            }
                        }
                    }
                }, 10);
            }
        });

        ArrayList<SortKey> keys = new ArrayList<RowSorter.SortKey>();
        keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        search_Sorter.setSortKeys(keys);
        ((DefaultRowSorter<?, ?>) search_Sorter).setSortsOnUpdates(true);
        this.jTableJScrollPanelLeftPanel.setRowSorter(search_Sorter);
        this.jTableJScrollPanelLeftPanel.setEditingColumn(0);
        this.jTableJScrollPanelLeftPanel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.jTableJScrollPanelLeftPanel.setAutoCreateRowSorter(true);
        this.jScrollPanelLeftPanel.setViewportView(this.jTableJScrollPanelLeftPanel);
        // EVENTS on CURSOR
        this.jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new My_Tab_Listener());
//			 Dimension size = MainFrame.getInstance().desktopPane.getSize();
//			 this.setSize(new Dimension((int)size.getWidth()-100,(int)size.getHeight()-100));
        // jSplitPanel.setDividerLocation((int)(size.getWidth()/1.618));

        JPopupMenu menu = new JPopupMenu();

        JMenuItem itemCheckTX = new JMenuItem(Lang.T("Validate"));
        itemCheckTX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Transaction selectedTransaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                        .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
                MyTransactionsSplitPanel.validate(selectedTransaction);
            }
        });
        menu.add(itemCheckTX);

        JMenuItem vouch_menu = new JMenuItem(Lang.T("Sign / Vouch"));
        vouch_menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                        .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
                new toSignRecordDialog(transaction.getBlockHeight(), transaction.getSeqNo());

            }
        });

        menu.add(vouch_menu);

        JMenuItem linkMenu = new JMenuItem(Lang.T("Append Document"));
        linkMenu.addActionListener(e -> {
            Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
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
            Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
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
            RSignNote transaction = (RSignNote) my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
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
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
            if (transaction == null) return;
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
            Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
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
            Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
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
            Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
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
            Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
            if (transaction == null) return;
            Library.saveJSONtoFileSystem(this, transaction, "tx" + transaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveJson);

        JMenuItem saveRAW = new JMenuItem(Lang.T("Save RAW (bytecode) as Base58"));
        saveRAW.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
            if (transaction == null) return;
            Library.saveAsBase58FileSystem(this, transaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + transaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW);

        JMenuItem saveRAW64 = new JMenuItem(Lang.T("Save RAW (bytecode) as Base64"));
        saveRAW64.addActionListener(e -> {
            if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) return;
            Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                    .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
            if (transaction == null) return;
            Library.saveAsBase64FileSystem(this, transaction.toBytes(Transaction.FOR_NETWORK, true),
                    "tx" + transaction.viewHeightSeq());

        });
        menuSaveCopy.add(saveRAW64);


        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) {
                    return;
                }

                Transaction transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel
                        .convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;
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

    }

    @Override
    public void onClose() {
        // delete observer left panel
        my_Statements_Model.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        //if (c1 instanceof RNoteInfo) ((RNoteInfo) c1).delay_on_Close();

    }

    class My_Tab_Listener implements ListSelectionListener {

        //@SuppressWarnings("deprecation")
        @Override
        public void valueChanged(ListSelectionEvent arg0) {


            Transaction transaction = null;
            if (jTableJScrollPanelLeftPanel.getSelectedRow() >= 0)
                transaction = my_Statements_Model.getItem(jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow())).b;

            if (transaction == null) return;

            ((WTransactionMap) my_Statements_Model.getMap()).clearUnViewed(transaction);

            JPanel info_panel = TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);

            info_panel.setPreferredSize(new Dimension(jScrollPaneJPanelRightPanel.getSize().width - 50, jScrollPaneJPanelRightPanel.getSize().height - 50));
            jScrollPaneJPanelRightPanel.setViewportView(info_panel);
            //	jSplitPanel.setRightComponent(info_panel);
        }

    }

    private Transaction getItem(int row) {
        int crow = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
        return my_Statements_Model.getItem(crow).b;
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