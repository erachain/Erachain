package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.items.accounts.AccountActionSendPanel;
import org.erachain.gui.items.accounts.AccountSendDialog;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.models.PersonStatusesModel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StatusesLibraryPanel extends JPanel {


    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public PersonStatusesModel statusModel;
    protected int row;
    private MTable jTable_Statuses;
    private JScrollPane jScrollPane_Tab_Status;
    private GridBagConstraints gridBagConstraints;

    public StatusesLibraryPanel(PersonCls person) {


        this.setName(Lang.getInstance().translate("Statuses"));
        this.setLayout(new java.awt.GridBagLayout());


        statusModel = new PersonStatusesModel(person.getKey());
        jTable_Statuses = new MTable(statusModel);

        //CHECKBOX FOR FAVORITE
        TableColumn to_Date_Column1 = jTable_Statuses.getColumnModel().getColumn(PersonStatusesModel.COLUMN_PERIOD);
        //favoriteColumn.setCellRenderer(new RendererBoolean()); //personsTable.getDefaultRenderer(Boolean.class));
        //   		int rr = (int) (getFontMetrics( UIManager.getFont("Table.font")).stringWidth("0022-22-2222"));
        to_Date_Column1.setMinWidth(80);
        // 		to_Date_Column1.setMaxWidth(200);
        to_Date_Column1.setPreferredWidth(120);//.setWidth(30);


        jScrollPane_Tab_Status = new javax.swing.JScrollPane();


        jScrollPane_Tab_Status.setViewportView(jTable_Statuses);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        this.add(jScrollPane_Tab_Status, gridBagConstraints);


        JPopupMenu menu = new JPopupMenu();


        menu.addPopupMenuListener(new PopupMenuListener() {

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
                int row1 = jTable_Statuses.getSelectedRow();
                if (row1 < 0) return;

                row = jTable_Statuses.convertRowIndexToModel(row1);


            }
        });


        JMenuItem menu_copyName = new JMenuItem(Lang.getInstance().translate("Copy creator name"));
        menu_copyName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                @SuppressWarnings("static-access")
                StringSelection value = new StringSelection((String) statusModel.getValueAt(row, statusModel.COLUMN_CREATOR_NAME));
                clipboard.setContents(value, null);

            }
        });
        menu.add(menu_copyName);


        JMenuItem copy_Creator_Address = new JMenuItem(Lang.getInstance().translate("Copy creator account"));
        copy_Creator_Address.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(statusModel.getCreator(row).getAddress());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copy_Creator_Address);

        JMenuItem menu_copy_Creator_PublicKey = new JMenuItem(Lang.getInstance().translate("Copy creator public key"));
        menu_copy_Creator_PublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                byte[] publick_Key = Controller.getInstance()
                        .getPublicKeyByAddress(statusModel.getCreator(row).getAddress());
                PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
                StringSelection value = new StringSelection(public_Account.getBase58());
                clipboard.setContents(value, null);
            }
        });
        menu.add(menu_copy_Creator_PublicKey);


        JMenuItem menu_copy_Block_PublicKey = new JMenuItem(Lang.getInstance().translate("Copy no. transaction"));
        menu_copy_Block_PublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(statusModel.getTransactionHeightSeqNo(row));
                clipboard.setContents(value, null);
            }
        });
        menu.add(menu_copy_Block_PublicKey);


        JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Asset to Creator"));
        Send_Coins_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Account account = statusModel.getCreator(row);
                //new AccountSendDialog(null, null, account, null);
                MainPanel.getInstance().insertTab(new AccountActionSendPanel(null, TransactionAmount.ACTION_SEND,
                        null, null, person, null));


            }
        });
        menu.add(Send_Coins_item_Menu);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send mail to creator"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Account account = statusModel.getCreator(row);
                MainPanel.getInstance().insertTab(new MailSendPanel(null, null, account, null));
                //new MailSendDialog(null, null, account, null);

            }
        });
        menu.add(Send_Mail_item_Menu);

        ////////////////////
        TableMenuPopupUtil.installContextMenu(jTable_Statuses, menu); // SELECT

    }

    public void delay_on_close() {

        statusModel.deleteObservers();

    }

}
