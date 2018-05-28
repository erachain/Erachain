package gui.items.persons;

import core.item.persons.PersonCls;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import gui.library.MTable;
import lang.Lang;
import utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Person_Owner_Panel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected int row;
    TableModelOwnerPersons person_Accounts_Model;
    private JTable jTable_My_Persons;
    private JScrollPane jScrollPane_Tab_My_Persons;
    private GridBagConstraints gridBagConstraints;

    @SuppressWarnings("rawtypes")
    public Person_Owner_Panel(PersonCls person) {

        this.setName(Lang.getInstance().translate("Created person"));

        person_Accounts_Model = new TableModelOwnerPersons(person.getKey());
        jTable_My_Persons = new MTable(person_Accounts_Model);

        jScrollPane_Tab_My_Persons = new JScrollPane();
        jScrollPane_Tab_My_Persons.setViewportView(jTable_My_Persons);
        this.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        this.add(jScrollPane_Tab_My_Persons, gridBagConstraints);

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
                int row1 = jTable_My_Persons.getSelectedRow();
                if (row1 < 0) return;

                row = jTable_My_Persons.convertRowIndexToModel(row1);


            }
        });


        JMenuItem copyKey = new JMenuItem(Lang.getInstance().translate("Copy Key"));
        copyKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Object a = person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_KEY).toString();
                StringSelection value = new StringSelection(person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_KEY).toString());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copyKey);

        JMenuItem menu_copyName = new JMenuItem(Lang.getInstance().translate("Copy Name"));
        menu_copyName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                @SuppressWarnings("static-access")
                StringSelection value = new StringSelection((String) person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_NAME));
                clipboard.setContents(value, null);

            }
        });
        menu.add(menu_copyName);


        JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Asset"));
        Send_Coins_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Account_Send_Dialog(null, null, null, person_Accounts_Model.getPerson(row)).show();
                ;

            }
        });
        menu.add(Send_Coins_item_Menu);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Mail"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Mail_Send_Dialog(null, null, null, person_Accounts_Model.getPerson(row));

            }
        });
        menu.add(Send_Mail_item_Menu);

        ////////////////////
        TableMenuPopupUtil.installContextMenu(jTable_My_Persons, menu); // SELECT
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
