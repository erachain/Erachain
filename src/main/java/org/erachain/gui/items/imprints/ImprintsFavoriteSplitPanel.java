package org.erachain.gui.items.imprints;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImprintsFavoriteSplitPanel extends ItemSplitPanel {

    public static String NAME = "ImprintsFavoriteSplitPanel";
    public static String TITLE = "Favorite Unique Hashes";

    private static final long serialVersionUID = 2717571093561259483L;

    public ImprintsFavoriteSplitPanel() {
        super(new FavoriteImprintsTableModel(), NAME, TITLE);
        iconName = "favorite.png";

        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.T("Send asset"));

        vsend_Coins_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //new AccountSendDialog(null, null, null, (PersonCls) th.itemMenu);
                MainPanel.getInstance().insertNewTab(Lang.T("Send asset"),
                        new AccountAssetSendPanel(null, null,
                                null, itemTableSelected.getMaker(), null, null, false));

            }
        });

        menuTable.add(vsend_Coins_Item);
        JMenuItem send_Mail_Item = new JMenuItem(Lang.T("Send Mail"));
        send_Mail_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertNewTab(Lang.T("Send Mail"), new MailSendPanel(null, itemTableSelected.getMaker(), null));
            }
        });

        menuTable.add(send_Mail_Item);
    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new ImprintsInfoPanel((ImprintCls) item);
    }

}