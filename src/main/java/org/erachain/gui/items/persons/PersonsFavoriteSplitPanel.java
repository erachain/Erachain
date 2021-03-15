package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PersonsFavoriteSplitPanel extends ItemSplitPanel {

    public static String NAME = "PersonsFavoriteSplitPanel";
    public static String TITLE = "Favorite Persons";

    private static final long serialVersionUID = 2717571093561259483L;
    //private PersonsFavoriteSplitPanel th;

    public PersonsFavoriteSplitPanel() {
        super(new FavoritePersonsTableModel(), NAME, TITLE);

        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.T("Send asset"));

        vsend_Coins_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertNewTab(Lang.T("Send asset"), new AccountAssetSendPanel(null,
                        null, null, (PersonCls) itemTableSelected, null, false));

            }
        });

        // th.menuTable.add(vsend_Coins_Item);
        JMenuItem send_Mail_Item = new JMenuItem(Lang.T("Send mail"));
        send_Mail_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertNewTab(Lang.T("Send Mail"), new MailSendPanel(null, null, (PersonCls) itemTableSelected));
            }
        });

        //   th.menuTable.add(send_Mail_Item);
//      add items in menu


        JMenuItem set_Status_Item = new JMenuItem(Lang.T("Set status to person"));

        set_Status_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                PersonSetStatusDialog fm = new PersonSetStatusDialog((PersonCls) itemTableSelected);

            }
        });
        this.menuTable.add(set_Status_Item);

        JMenuItem attestPubKey_Item = new JMenuItem(Lang.T("Certify Public Key for Person"));

        attestPubKey_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                PersonCertifyPubKeysDialog fm = new PersonCertifyPubKeysDialog((PersonCls) itemTableSelected, itemTableSelected.getMaker());

            }
        });
        this.menuTable.add(attestPubKey_Item);

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new PersonInfo002((PersonCls) item, true);
    }

}
