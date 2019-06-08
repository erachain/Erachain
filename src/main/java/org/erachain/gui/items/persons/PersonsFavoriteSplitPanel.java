package org.erachain.gui.items.persons;

import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.ItemSplitPanel;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PersonsFavoriteSplitPanel extends ItemSplitPanel {
    private static final long serialVersionUID = 2717571093561259483L;
    private PersonsFavoriteSplitPanel th;

    public PersonsFavoriteSplitPanel() {
        super(new FavoritePersonsTableModel(), "PersonsFavoriteSplitPanel");
        this.setName(Lang.getInstance().translate("Favorite Persons"));

        th = this;

        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.getInstance().translate("Send asset"));

        vsend_Coins_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertTab(new AccountAssetSendPanel(null, TransactionAmount.ACTION_SEND,
                        null, null, (PersonCls) th.itemMenu, null));

            }
        });

        // th.menuTable.add(vsend_Coins_Item);
        JMenuItem send_Mail_Item = new JMenuItem(Lang.getInstance().translate("Send mail"));
        send_Mail_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainPanel.getInstance().insertTab(new MailSendPanel(null, null, null, (PersonCls) th.itemMenu));
            }
        });

        //   th.menuTable.add(send_Mail_Item);
//      add items in menu


        JMenuItem set_Status_Item = new JMenuItem(Lang.getInstance().translate("Set status to person"));

        set_Status_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                PersonSetStatusDialog fm = new PersonSetStatusDialog((PersonCls) th.itemMenu);

            }
        });
        this.menuTable.add(set_Status_Item);

        JMenuItem attestPubKey_Item = new JMenuItem(Lang.getInstance().translate("Attest public key for person"));

        attestPubKey_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                PersonConfirmDialog fm = new PersonConfirmDialog((PersonCls) th.itemMenu, th.itemMenu.getOwner());

            }
        });
        this.menuTable.add(attestPubKey_Item);

        JMenuItem vouchPerson_Item = new JMenuItem(Lang.getInstance().translate("Vouch the person info"));
        vouchPerson_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                PersonCls per = (PersonCls) th.itemMenu;
                byte[] ref = per.getReference();
                Transaction transaction = Transaction.findByDBRef(DCSet.getInstance(), ref);
                int blockNo = transaction.getBlockHeight();
                int recNo = transaction.getSeqNo();
                new VouchRecordDialog(blockNo, recNo);

            }
        });
        this.menuTable.add(vouchPerson_Item);

    }

    // show details
    @Override
    public Component getShow(ItemCls item) {
        return new PersonInfo002((PersonCls) item, true);
    }

}
