package org.erachain.gui.transaction;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.R_SertifyPubKeys;
import org.erachain.lang.Lang;
import org.apache.log4j.Logger;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class SertifyPubKeysDetailsFrame extends Rec_DetailsFrame {
    private static final Logger LOGGER = Logger.getLogger(SertifyPubKeysDetailsFrame.class);
    private JTextField messageText;

    public SertifyPubKeysDetailsFrame(final R_SertifyPubKeys sertifyPubKeysRecord) {
        super(sertifyPubKeysRecord);

        //LABEL PERSON
        ++labelGBC.gridy;
        JLabel personLabel = new JLabel(Lang.getInstance().translate("Person") + ":");
        this.add(personLabel, labelGBC);

        // PERSON
        ++detailGBC.gridy;
        JTextField person;
        PersonCls personItem = Controller.getInstance().getPerson(sertifyPubKeysRecord.getKey());
        if (personItem == null)
            person = new JTextField("not found");
        else
            person = new JTextField(personItem.toString());

        person.setEditable(false);
        MenuPopupUtil.installContextMenu(person);
        this.add(person, detailGBC);

        //LABEL to DAYS
        ++labelGBC.gridy;
        JLabel amountLabel = new JLabel(Lang.getInstance().translate("End Days") + ":");
        this.add(amountLabel, labelGBC);

        //ens DAYS
        ++detailGBC.gridy;
        JTextField amount = new JTextField("" + sertifyPubKeysRecord.getAddDay());
        amount.setEditable(false);
        MenuPopupUtil.installContextMenu(amount);
        this.add(amount, detailGBC);

        int i = 0;
        for (String address : sertifyPubKeysRecord.getSertifiedPublicKeysB58()) {
            ++labelGBC.gridy;
            JLabel lbl = new JLabel(++i + " :");
            this.add(lbl, labelGBC);

            ++detailGBC.gridy;
            JTextField txt = new JTextField(address);
            txt.setEditable(false);
            MenuPopupUtil.installContextMenu(txt);
            this.add(txt, detailGBC);

        }
        //PACK
//		this.pack();
        //       this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
