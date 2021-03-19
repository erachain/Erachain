package org.erachain.gui.items.persons;

import org.erachain.core.item.persons.PersonCls;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.library.MTable;
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

public class PersonMakerPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected int row;
    TableModelMakerPersons person_Accounts_Model;
    private JTable jTable_My_Persons;
    private JScrollPane jScrollPane_Tab_My_Persons;
    private GridBagConstraints gridBagConstraints;

    @SuppressWarnings("rawtypes")
    public PersonMakerPanel(PersonCls person) {

        this.setName(Lang.T("Created person"));

        person_Accounts_Model = new TableModelMakerPersons(person.getKey());
        jTable_My_Persons = new MTable(person_Accounts_Model);

        //почему-то перестает показывать вообще всю инфо если включить тут TableColumn favorite_Column = jTable_My_Persons.getColumnModel().getColumn(TableModelMakerPersons.COLUMN_FAVORITE);
        //favorite_Column.setCellRenderer(jTable_My_Persons.getDefaultRenderer(Boolean.class));
        //favorite_Column.setMinWidth(50);
        //favorite_Column.setMaxWidth(150);
        //favorite_Column.setPreferredWidth(100);

        TableColumn height_Column = jTable_My_Persons.getColumnModel().getColumn(TableModelMakerPersons.COLUMN_KEY);
        //favoriteColumn.setCellRenderer(new RendererBoolean()); //personsTable.getDefaultRenderer(Boolean.class));
        int rr = (int) (getFontMetrics(UIManager.getFont("Table.font")).stringWidth("0000222"));
        height_Column.setMinWidth(rr + 1);
        height_Column.setMaxWidth(rr * 10);
        height_Column.setPreferredWidth(rr + 5);


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


        JMenuItem copyKey = new JMenuItem(Lang.T("Copy Key"));
        copyKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Object a = person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_KEY).toString();
                StringSelection value = new StringSelection(person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_KEY).toString());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copyKey);

        JMenuItem menu_copyName = new JMenuItem(Lang.T("Copy Name"));
        menu_copyName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                @SuppressWarnings("static-access")
                StringSelection value = new StringSelection((String) person_Accounts_Model.getValueAt(row, person_Accounts_Model.COLUMN_NAME));
                clipboard.setContents(value, null);

            }
        });
        menu.add(menu_copyName);


        JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.T("Send asset"));
        Send_Coins_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertNewTab(Lang.T("Send asset"), new AccountAssetSendPanel(null,
                        null, null, person_Accounts_Model.getItem(row), null, false));

            }
        });
        menu.add(Send_Coins_item_Menu);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.T("Send Mail"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertNewTab(Lang.T("Send Mail"), new MailSendPanel(null, null, person_Accounts_Model.getItem(row)));

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

        person_Accounts_Model.deleteObservers();


    }

}
