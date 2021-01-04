package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.gui.items.SearchItemSplitPanel;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.URLViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class SearchPersonsSplitPanel extends SearchItemSplitPanel {

    public static String NAME = "SearchPersonsSplitPanel";
    public static String TITLE = "Search Persons";

    private static final long serialVersionUID = 2717571093561259483L;

    public SearchPersonsSplitPanel() {
        super(new ItemsPersonsTableModel(), NAME, TITLE);

        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.getInstance().translate("Send asset"));

        vsend_Coins_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.getInstance().translate("Send asset"), new AccountAssetSendPanel(null,
                        null, null, (PersonCls) itemTableSelected, null, false));

            }
        });

        //  this.menuTable.add(vsend_Coins_Item);


        JMenuItem send_Mail_Item = new JMenuItem(Lang.getInstance().translate("Send Mail"));
        send_Mail_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.getInstance().translate("Send Mail"), new MailSendPanel(null, null, (PersonCls) itemTableSelected));
            }
        });

        //   this.menuTable.add(send_Mail_Item);

//    add items in menu

        JMenuItem set_Status_Item = new JMenuItem(Lang.getInstance().translate("Set status to person"));

        set_Status_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                PersonSetStatusDialog fm = new PersonSetStatusDialog((PersonCls) itemTableSelected);

            }
        });
        this.menuTable.add(set_Status_Item);

        JMenuItem attestPubKey_Item = new JMenuItem(Lang.getInstance().translate("Certify Public Key for Person"));

        attestPubKey_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                @SuppressWarnings("unused")
                PersonCertifyPubKeysDialog fm = new PersonCertifyPubKeysDialog((PersonCls) itemTableSelected, itemTableSelected.getOwner());

            }
        });
        this.menuTable.add(attestPubKey_Item);

        menuTable.addSeparator();

        JMenuItem setSeeInBlockexplorer = new JMenuItem(Lang.getInstance().translate("Check in Blockexplorer"));

        setSeeInBlockexplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    URLViewer.openWebpage(new URL(Settings.getInstance().getBlockexplorerURL()
                            + "/index/blockexplorer.html"
                            + "?person=" + itemTableSelected.getKey()));
                } catch (MalformedURLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        });

        menuTable.add(setSeeInBlockexplorer);

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {

        return new PersonInfo002((PersonCls) item, true);

    }

}
