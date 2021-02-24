package org.erachain.gui.transaction;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RCertifyPubKeys;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

@SuppressWarnings("serial")
public class CertifyPubKeysDetailsFrame extends RecDetailsFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(CertifyPubKeysDetailsFrame.class);
    private JTextField messageText;

    public CertifyPubKeysDetailsFrame(final RCertifyPubKeys certifyPubKeysRecord) {
        super(certifyPubKeysRecord, true);

        //LABEL PERSON
        ++labelGBC.gridy;
        JLabel personLabel = new JLabel(Lang.T("Person") + ":");
        this.add(personLabel, labelGBC);

        // PERSON
        ++fieldGBC.gridy;
        JTextField person;
        PersonCls personItem = Controller.getInstance().getPerson(certifyPubKeysRecord.getKey());
        if (personItem == null)
            person = new JTextField("not found");
        else
            person = new JTextField(personItem.toString());

        person.setEditable(false);
        MenuPopupUtil.installContextMenu(person);
        this.add(person, fieldGBC);

        //LABEL to DAYS
        ++labelGBC.gridy;
        JLabel amountLabel = new JLabel(Lang.T("End Days") + ":");
        this.add(amountLabel, labelGBC);

        //ens DAYS
        ++fieldGBC.gridy;
        JTextField amount = new JTextField("" + certifyPubKeysRecord.getAddDay());
        amount.setEditable(false);
        MenuPopupUtil.installContextMenu(amount);
        this.add(amount, fieldGBC);

        int i = 0;
        for (String address : certifyPubKeysRecord.getCertifiedPublicKeysB58()) {
            ++labelGBC.gridy;
            JLabel lbl = new JLabel(++i + " :");
            this.add(lbl, labelGBC);

            ++fieldGBC.gridy;
            JTextField txt = new JTextField(address);
            txt.setEditable(false);
            MenuPopupUtil.installContextMenu(txt);
            this.add(txt, fieldGBC);

        }
        //PACK
//		this.pack();
        //       this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
