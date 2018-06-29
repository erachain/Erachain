package gui.items.persons;

import core.item.ItemCls;
import core.item.persons.PersonCls;
import gui.items.Item_SplitPanel;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Persons_Favorite_SplitPanel extends Item_SplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    private static Persons_Favorite_TableModel table_Model = new Persons_Favorite_TableModel();
    private Persons_Favorite_SplitPanel th;

    public Persons_Favorite_SplitPanel() {
        super(table_Model, "Persons_Favorite_SplitPanel");
        this.setName(Lang.getInstance().translate("Favorite Persons"));
        th = this;
        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.getInstance().translate("Send Asset"));

        vsend_Coins_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Account_Send_Dialog(null, null, null, (PersonCls) th.item_Menu);
            }
        });

        th.menu_Table.add(vsend_Coins_Item);
        JMenuItem send_Mail_Item = new JMenuItem(Lang.getInstance().translate("Send Mail"));
        send_Mail_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Mail_Send_Dialog(null, null, null, (PersonCls) th.item_Menu);
            }
        });

        th.menu_Table.add(send_Mail_Item);
    }

    // show details
    @Override
    public Component get_show(ItemCls item) {
        return new Person_Info_002((PersonCls) item, true);
    }

    @Override
    protected void splitClose() {
        table_Model.removeObservers();

    }
}
