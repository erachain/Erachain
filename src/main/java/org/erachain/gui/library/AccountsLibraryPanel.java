package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.gui.items.accounts.AccountSendDialog;
import org.erachain.gui.items.accounts.AccountSetNameDialog;
import org.erachain.gui.items.mails.MailSendDialog;
import org.erachain.gui.models.PersonAccountsModel;
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

public class AccountsLibraryPanel extends JPanel {

    public PersonAccountsModel person_Accounts_Model;
    protected int row;
    private JTable jTable_Accounts;
    private JScrollPane jScrollPane_Tab_Accounts;
    private GridBagConstraints gridBagConstraints;

    public AccountsLibraryPanel(PersonCls person) {

        this.setName(Lang.getInstance().translate("Accounts"));

        person_Accounts_Model = new PersonAccountsModel(person.getKey());
        jTable_Accounts = new MTable(person_Accounts_Model);

        //	int row = jTable_Accounts.getSelectedRow();
        //	row = jTable_Accounts.convertRowIndexToModel(row);
        //	selected = person_Accounts_Model.getAccount(row);

        TableColumn to_Date_Column = jTable_Accounts.getColumnModel().getColumn(PersonAccountsModel.COLUMN_TO_DATE);
        int rr = (int) (getFontMetrics(UIManager.getFont("Table.font")).stringWidth("0022-22-2222"));
        to_Date_Column.setMinWidth(rr + 1);
        to_Date_Column.setMaxWidth(rr * 10);
        to_Date_Column.setPreferredWidth(rr + 5);//.setWidth(30);


        jScrollPane_Tab_Accounts = new JScrollPane();
        jScrollPane_Tab_Accounts.setViewportView(jTable_Accounts);
        this.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        this.add(jScrollPane_Tab_Accounts, gridBagConstraints);

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
                int row1 = jTable_Accounts.getSelectedRow();
                if (row1 < 0) return;

                row = jTable_Accounts.convertRowIndexToModel(row1);


            }
        });


        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Copy Account"));
        copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Account account = person_Accounts_Model.getAccount(row);
                StringSelection value = new StringSelection(account.getAddress());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copyAddress);

        JMenuItem menu_copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
        menu_copyPublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                Account account = person_Accounts_Model.getAccount(row);
                byte[] publick_Key = Controller.getInstance().getPublicKeyByAddress(account.getAddress());
                PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
                StringSelection value = new StringSelection(public_Account.getBase58());
                clipboard.setContents(value, null);
            }
        });
        menu.add(menu_copyPublicKey);

        JMenuItem menu_copyName = new JMenuItem(Lang.getInstance().translate("Copy verifier name"));
        menu_copyName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                @SuppressWarnings("static-access")
                StringSelection value = new StringSelection((String) person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_CREATOR_NAME));
                clipboard.setContents(value, null);

            }
        });
        menu.add(menu_copyName);

        JMenuItem copy_Creator_Address = new JMenuItem(Lang.getInstance().translate("Copy account verifier"));
        copy_Creator_Address.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(person_Accounts_Model.getCreator(row));
                clipboard.setContents(value, null);
            }
        });
        menu.add(copy_Creator_Address);

        JMenuItem menu_copy_Creator_PublicKey = new JMenuItem(Lang.getInstance().translate("Copy verifier public key"));
        menu_copy_Creator_PublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                byte[] publick_Key = Controller.getInstance()
                        .getPublicKeyByAddress(person_Accounts_Model.getCreator(row));
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
                StringSelection value = new StringSelection(person_Accounts_Model.get_No_Trancaction(row));
                clipboard.setContents(value, null);
            }
        });
        menu.add(menu_copy_Block_PublicKey);


        JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send asset"));
        Send_Coins_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Account account = person_Accounts_Model.getAccount(row);
                new AccountSendDialog(null, null, account, null);
                ;

            }
        });
        menu.add(Send_Coins_item_Menu);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send mail"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Account account = person_Accounts_Model.getAccount(row);

                new MailSendDialog(null, null, account, null);

            }
        });
        menu.add(Send_Mail_item_Menu);

        JMenuItem setName = new JMenuItem(Lang.getInstance().translate("Set name"));
        setName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Account account = person_Accounts_Model.getAccount(row);

                new AccountSetNameDialog(account.getAddress());
                jTable_Accounts.repaint();

            }
        });
        menu.add(setName);

        ////////////////////
        TableMenuPopupUtil.installContextMenu(jTable_Accounts, menu); // SELECT
        // ROW
        // ON
        // WHICH
        // CLICKED
        // RIGHT
        // BUTTON

    }

    public void delay_on_close() {

        person_Accounts_Model.removeObservers();

    }

}
