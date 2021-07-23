package org.erachain.gui.items.mails;

import org.erachain.core.account.Account;
import org.erachain.core.exdata.ExData;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.wallet.WTransactionMap;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.WalletTableRenderer;
import org.erachain.gui.items.statement.IssueDocumentPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.TableMenuPopupUtil;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class IncomingMailsSplitPanel extends SplitPanel {

    public static String NAME = "IncomingMailsSplitPanel";
    public static String TITLE = "Incoming Mails";

    private static final long serialVersionUID = 2717571093561259483L;
    private TableModelMails incoming_Mails_Model;
    //private MTable jTableJScrollPanelLeftPanel;
    private TableRowSorter my_Sorter;

    public IncomingMailsSplitPanel() {
        super(NAME, TITLE);
        // not show buttons
        this.button1ToolBarLeftPanel.setVisible(false);
        this.button2ToolBarLeftPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.jButton2_jToolBar_RightPanel.setVisible(false);

        // not show My filter
        this.searchMyJCheckBoxLeftPanel.setVisible(false);

        // TABLE
        incoming_Mails_Model = new TableModelMails(true);
        jTableJScrollPanelLeftPanel = new MTable(incoming_Mails_Model);
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Object.class, new WalletTableRenderer());
        jTableJScrollPanelLeftPanel.setDefaultRenderer(Boolean.class, new WalletTableRenderer());

        TableColumnModel columnModel = jTableJScrollPanelLeftPanel.getColumnModel();
        columnModel.getColumn(TableModelMails.COLUMN_SEQNO).setPreferredWidth(150);
        columnModel.getColumn(TableModelMails.COLUMN_SEQNO).setMaxWidth(200);

        jTableJScrollPanelLeftPanel.setAutoCreateRowSorter(true);

        // MENU
        JPopupMenu menu = new JPopupMenu();

        JMenuItem copySender = new JMenuItem(Lang.T("Copy Sender Account"));
        copySender.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(
                        (incoming_Mails_Model.getItem(row)).getCreator().getAddress());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copySender);

        JMenuItem copyRecipient = new JMenuItem(Lang.T("Copy Recipient Account"));
        copyRecipient.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(
                        ((RSend) incoming_Mails_Model.getItem(row)).getRecipient().getAddress());
                clipboard.setContents(value, null);
            }
        });

        menu.add(copyRecipient);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.T("To Answer"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                RSend rSend = (RSend) incoming_Mails_Model.getItem(row);
                Account account = new Account(rSend.getCreator().getAddress());
                Account recipient = rSend.getRecipient();

                MainPanel.getInstance().insertNewTab(Lang.T("Send Mail"), new MailSendPanel(recipient, account, null));

            }
        });
        menu.add(Send_Mail_item_Menu);

        JMenuItem signMail_item_Menu = new JMenuItem(Lang.T("Sign / Vouch"));
        signMail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int row = jTableJScrollPanelLeftPanel.getSelectedRow();
                row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
                Transaction trans = incoming_Mails_Model.getItem(row);
                int blockNo = trans.getBlockHeight();
                int recNo = trans.getSeqNo();
                new toSignRecordDialog(blockNo, recNo, ((RSend) trans).getRecipient());

            }
        });
        menu.add(signMail_item_Menu);

        JMenuItem linkMenu = new JMenuItem(Lang.T("Append Document"));
        linkMenu.addActionListener(e -> {
            int row = jTableJScrollPanelLeftPanel.getSelectedRow();
            row = jTableJScrollPanelLeftPanel.convertRowIndexToModel(row);
            Transaction transaction = incoming_Mails_Model.getItem(row);
            MainPanel.getInstance().insertNewTab(
                    Lang.T("For # для") + " " + transaction.viewHeightSeq(),
                    new IssueDocumentPanel(null, ExData.LINK_APPENDIX_TYPE, transaction.viewHeightSeq(), null));

        });
        menu.add(linkMenu);

        menu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.T("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableJScrollPanelLeftPanel.getSelectedRow() < 0) {
                    return;
                }

                Transaction transaction = incoming_Mails_Model.getItem(jTableJScrollPanelLeftPanel
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

        TableMenuPopupUtil.installContextMenu(jTableJScrollPanelLeftPanel, menu); // SELECT
        // ROW
        // ON
        // WHICH
        // CLICKED
        // RIGHT
        // BUTTON

        /*
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         * //CHECKBOX FOR FAVORITE TableColumn favoriteColumn =
         * jTableJScrollPanelLeftPanel.getColumnModel().getColumn(
         * WalletItemPersonsTableModel.COLUMN_FAVORITE);
         * //favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.
         * class)); favoriteColumn.setCellRenderer(new RendererBoolean());
         * favoriteColumn.setMinWidth(50); favoriteColumn.setMaxWidth(50);
         * favoriteColumn.setPreferredWidth(50);//.setWidth(30);
         */
        // UPDATE FILTER ON TEXT CHANGE
        this.searchTextField2.getDocument().addDocumentListener(new My_Search());
        // SET VIDEO
        this.jTableJScrollPanelLeftPanel.setModel(incoming_Mails_Model);
        this.jTableJScrollPanelLeftPanel = jTableJScrollPanelLeftPanel;
        this.jScrollPanelLeftPanel.setViewportView(this.jTableJScrollPanelLeftPanel);
        // this.setRowHeightFormat(true);

        // EVENTS on CURSOR
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new My_Tab_Listener());

    }

    @Override
    public void onClose() {
        // delete observer left panel
        incoming_Mails_Model.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        if (c1 instanceof MailInfo) ((MailInfo) c1).delay_on_Close();

    }

    class My_Tab_Listener implements ListSelectionListener {

        // @SuppressWarnings("deprecation")
        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            RSend mail = null;
            if (jTableJScrollPanelLeftPanel.getSelectedRow() >= 0)
                mail = (RSend) incoming_Mails_Model.getItem(
                        jTableJScrollPanelLeftPanel.convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
            // info1.show_001(person);
            if (mail == null)
                return;

            ((WTransactionMap) incoming_Mails_Model.getMap()).clearUnViewed(mail);

            //((WTransactionMap) TableModelMails.getMap()).clearUnViewed(IncomingMailsSplitPanel.this.notifyAll(););
            MailInfo info_panel = new MailInfo(mail);

            jScrollPaneJPanelRightPanel.setViewportView(info_panel);

        }

    }

    class My_Search implements DocumentListener {
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
            incoming_Mails_Model.fireTableDataChanged();

            RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
            ((DefaultRowSorter) my_Sorter).setRowFilter(filter);
            incoming_Mails_Model.fireTableDataChanged();

        }
    }

}
