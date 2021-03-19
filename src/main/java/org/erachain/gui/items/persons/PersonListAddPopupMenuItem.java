package org.erachain.gui.items.persons;

import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.records.toSignRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PersonListAddPopupMenuItem {

    public PersonListAddPopupMenuItem(PersonCls person, JPopupMenu menu) {
        JMenuItem vsend_Coins_Item = new JMenuItem(Lang.T("Send asset"));

        vsend_Coins_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //new AccountSendDialog(null, null, null, person);
                MainPanel.getInstance().insertNewTab(Lang.T("Send asset"), new AccountAssetSendPanel(null,
                        null, null, person, null, false));

            }
        });

        JMenuItem send_Mail_Item = new JMenuItem(Lang.T("Send Mail"));
        send_Mail_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                MainPanel.getInstance().insertNewTab(Lang.T("Send Mail"), new MailSendPanel(null, null, (PersonCls) person));
            }
        });

        //   menu.add(send_Mail_Item);
        JMenuItem set_Status_Item = new JMenuItem(Lang.T("Set Status to Person"));

        set_Status_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                @SuppressWarnings("unused")
                PersonSetStatusDialog fm = new PersonSetStatusDialog((PersonCls) person);

            }
        });
        menu.add(set_Status_Item);

        JMenuItem attestPubKey_Item = new JMenuItem(Lang.T("Certify Public Key for Person"));

        attestPubKey_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                @SuppressWarnings("unused")
                PersonCertifyPubKeysDialog fm = new PersonCertifyPubKeysDialog((PersonCls) person, person.getMaker());

            }
        });
        menu.add(attestPubKey_Item);

        JMenuItem vouchPerson_Item = new JMenuItem(Lang.T("Vouch the Person Info"));
        vouchPerson_Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                PersonCls per = person;
                byte[] ref = per.getReference();
                Transaction transaction = Transaction.findByDBRef(DCSet.getInstance(), ref);
                int blockNo = transaction.getBlockHeight();
                int recNo = transaction.getSeqNo();
                new toSignRecordDialog(blockNo, recNo);

            }
        });
        menu.add(vouchPerson_Item);

    }
}
