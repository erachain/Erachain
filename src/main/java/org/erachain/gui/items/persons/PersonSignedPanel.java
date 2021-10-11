package org.erachain.gui.items.persons;

import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.items.statement.StatementsVouchTableModel;
import org.erachain.gui.library.MTable;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PersonSignedPanel extends JPanel {

    /**
     * view VOUSH PANEL
     */
    private static final long serialVersionUID = 1L;
    protected int row;
    PersonVouchFromTableModel model;
    private JScrollPane jScrollPane_Tab_Vouches;
    private GridBagConstraints gridBagConstraints;

    public PersonSignedPanel(PersonCls person) {

        this.setName(Lang.T("Vouched for"));
        model = new PersonVouchFromTableModel(person);
        JTable jTable_Vouches = new MTable(model);
        TableColumnModel column_mod = jTable_Vouches.getColumnModel();
        TableColumn col_data = column_mod.getColumn(StatementsVouchTableModel.COLUMN_TIMESTAMP);
        col_data.setMinWidth(50);
        col_data.setMaxWidth(200);
        col_data.setPreferredWidth(120);// .setWidth(30);


        TableColumn Date_Column = jTable_Vouches.getColumnModel().getColumn(StatementsVouchTableModel.COLUMN_TIMESTAMP);
        int rr = (int) (getFontMetrics(UIManager.getFont("Table.font")).stringWidth("0022-22-2222"));
        Date_Column.setMinWidth(rr + 1);
        Date_Column.setMaxWidth(rr * 10);
        Date_Column.setPreferredWidth(rr + 5);//.setWidth(30);

        jTable_Vouches.setAutoCreateRowSorter(true);

        TableRowSorter sorter = new TableRowSorter(model); //Создаем сортировщик
        if (model.getRowCount() > 0 && sorter.isSortable(StatementsVouchTableModel.COLUMN_TIMESTAMP)) {
            sorter.toggleSortOrder(StatementsVouchTableModel.COLUMN_TIMESTAMP); //Сортируем первую колонку
        }
        sorter.setSortsOnUpdates(true);                         //Указываем автоматически сортировать
        //при изменении модели данных
        jTable_Vouches.setRowSorter(sorter);

        jScrollPane_Tab_Vouches = new javax.swing.JScrollPane();

        this.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Tab_Vouches.setViewportView(jTable_Vouches);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        this.add(jScrollPane_Tab_Vouches, gridBagConstraints);


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
                int row1 = jTable_Vouches.getSelectedRow();
                if (row1 < 0) return;

                row = jTable_Vouches.convertRowIndexToModel(row1);


            }
        });


        JMenuItem copy_Creator_Address = new JMenuItem(Lang.T("Copy Account"));
        copy_Creator_Address.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(model.getPublicKey(row).getAddress());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copy_Creator_Address);

        JMenuItem menu_copy_Creator_PublicKey = new JMenuItem(Lang.T("Copy Public Key"));
        menu_copy_Creator_PublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                PublicKeyAccount public_Account = model.getPublicKey(row);
                StringSelection value = new StringSelection(public_Account.getBase58());
                clipboard.setContents(value, null);
            }
        });
        menu.add(menu_copy_Creator_PublicKey);


        JMenuItem menu_copy_Block_PublicKey = new JMenuItem(Lang.T("Copy no. transaction"));
        menu_copy_Block_PublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection value = new StringSelection(model.getHeightSeq(row));
                clipboard.setContents(value, null);
            }
        });
        menu.add(menu_copy_Block_PublicKey);


        JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.T("Send Asset to Person"));
        Send_Coins_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Account accountTo = (Account) model.getPublicKey(row);
                MainPanel.getInstance().insertNewTab(Lang.T("Send Asset to Person"), new AccountAssetSendPanel(null,
                        null, accountTo, person, null, false));
            }
        });
        menu.add(Send_Coins_item_Menu);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.T("Send Mail to Person"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Account account = (Account) model.getPublicKey(row);

                MainPanel.getInstance().insertNewTab(Lang.T("Send Mail to Person"), new MailSendPanel(null, account, null));

            }
        });
        menu.add(Send_Mail_item_Menu);

        ////////////////////
        TableMenuPopupUtil.installContextMenu(jTable_Vouches, menu); // SELECT

    }

    public void delay_on_close() {
        model.removeObservers();
    }

}
