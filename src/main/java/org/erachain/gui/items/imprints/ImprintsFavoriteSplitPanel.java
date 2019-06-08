package org.erachain.gui.items.imprints;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.items.accounts.AccountActionSendPanel;
import org.erachain.gui.items.accounts.AccountSendDialog;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImprintsFavoriteSplitPanel extends ItemSplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    private ImprintsFavoriteSplitPanel th;

    public ImprintsFavoriteSplitPanel() {
        super(new FavoriteImprintsTableModel(), "PersonsFavoriteSplitPanel");
        this.setName(Lang.getInstance().translate("Favorite Persons"));
        th = this;
        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.getInstance().translate("Send asset"));

        vsend_Coins_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //new AccountSendDialog(null, null, null, (PersonCls) th.itemMenu);
                MainPanel.getInstance().insertTab(new AccountActionSendPanel(null, TransactionAmount.ACTION_SEND,
                        null, null, (PersonCls) th.itemMenu, null));

            }
        });

        th.menuTable.add(vsend_Coins_Item);
        JMenuItem send_Mail_Item = new JMenuItem(Lang.getInstance().translate("Send Mail"));
        send_Mail_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertTab(new MailSendPanel(null, null, null, (PersonCls) th.itemMenu));
            }
        });

        th.menuTable.add(send_Mail_Item);
    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new ImprintsInfoPanel((ImprintCls) item);
    }

}
