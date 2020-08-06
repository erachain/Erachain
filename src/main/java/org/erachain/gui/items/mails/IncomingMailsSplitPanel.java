package org.erachain.gui.items.mails;

import org.erachain.core.account.Account;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.records.VouchRecordDialog;
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
    private MTable inciming_Mail_Table;
    private TableRowSorter my_Sorter;

    public IncomingMailsSplitPanel() {
        super(NAME, TITLE);
        this.searthLabelSearchToolBarLeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        // not show buttons
        this.button1ToolBarLeftPanel.setVisible(false);
        this.button2ToolBarLeftPanel.setVisible(false);
        this.jButton1_jToolBar_RightPanel.setVisible(false);
        this.jButton2_jToolBar_RightPanel.setVisible(false);

        // not show My filter
        this.searchMyJCheckBoxLeftPanel.setVisible(false);

        // TABLE
        incoming_Mails_Model = new TableModelMails(true);
        inciming_Mail_Table = new MTable(incoming_Mails_Model);
        inciming_Mail_Table.setAutoCreateRowSorter(true);

        // MENU
        JPopupMenu menu = new JPopupMenu();

        JMenuItem copySender = new JMenuItem(Lang.getInstance().translate("Copy Sender Account"));
        copySender.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int row = inciming_Mail_Table.getSelectedRow();
                row = inciming_Mail_Table.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(
                        (incoming_Mails_Model.getItem(row)).getCreator().getAddress());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copySender);

        JMenuItem copyRecipient = new JMenuItem(Lang.getInstance().translate("Copy Recipient Account"));
        copyRecipient.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int row = inciming_Mail_Table.getSelectedRow();
                row = inciming_Mail_Table.convertRowIndexToModel(row);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(
                        ((RSend) incoming_Mails_Model.getItem(row)).getRecipient().getAddress());
                clipboard.setContents(value, null);
            }
        });

        menu.add(copyRecipient);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("To Answer"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int row = inciming_Mail_Table.getSelectedRow();
                row = inciming_Mail_Table.convertRowIndexToModel(row);
                RSend rSend = (RSend) incoming_Mails_Model.getItem(row);
                Account account = new Account(rSend.getCreator().getAddress());
                Account recipient = rSend.getRecipient();

                MainPanel.getInstance().insertNewTab(Lang.getInstance().translate("Send Mail"), new MailSendPanel(recipient, account, null));

            }
        });
        menu.add(Send_Mail_item_Menu);

        JMenuItem vouch_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Vouch"));
        vouch_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int row = inciming_Mail_Table.getSelectedRow();
                row = inciming_Mail_Table.convertRowIndexToModel(row);
                Transaction trans = incoming_Mails_Model.getItem(row);
                int blockNo = trans.getBlockHeight();
                int recNo = trans.getSeqNo();
                new VouchRecordDialog(blockNo, recNo, ((RSend) trans).getRecipient());

            }
        });
        menu.add(vouch_Mail_item_Menu);

        menu.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

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

        TableMenuPopupUtil.installContextMenu(inciming_Mail_Table, menu); // SELECT
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
         * inciming_Mail_Table.getColumnModel().getColumn(
         * WalletItemPersonsTableModel.COLUMN_FAVORITE);
         * //favoriteColumn.setCellRenderer(table.getDefaultRenderer(Boolean.
         * class)); favoriteColumn.setCellRenderer(new RendererBoolean());
         * favoriteColumn.setMinWidth(50); favoriteColumn.setMaxWidth(50);
         * favoriteColumn.setPreferredWidth(50);//.setWidth(30);
         */
        // UPDATE FILTER ON TEXT CHANGE
        this.searchTextFieldSearchToolBarLeftPanelDocument.getDocument().addDocumentListener(new My_Search());
        // SET VIDEO
        this.jTableJScrollPanelLeftPanel.setModel(incoming_Mails_Model);
        this.jTableJScrollPanelLeftPanel = inciming_Mail_Table;
        this.jScrollPanelLeftPanel.setViewportView(this.jTableJScrollPanelLeftPanel);
        // this.setRowHeightFormat(true);

        // EVENTS on CURSOR
        inciming_Mail_Table.getSelectionModel().addListSelectionListener(new My_Tab_Listener());

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
            if (inciming_Mail_Table.getSelectedRow() >= 0)
                mail = (RSend) incoming_Mails_Model.getItem(
                        inciming_Mail_Table.convertRowIndexToModel(inciming_Mail_Table.getSelectedRow()));
            // info1.show_001(person);
            if (mail == null)
                return;
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
            String search = searchTextFieldSearchToolBarLeftPanelDocument.getText();
            // SET FILTER
            incoming_Mails_Model.fireTableDataChanged();

            RowFilter filter = RowFilter.regexFilter(".*" + search + ".*", 1);
            ((DefaultRowSorter) my_Sorter).setRowFilter(filter);
            incoming_Mails_Model.fireTableDataChanged();

        }
    }

}
