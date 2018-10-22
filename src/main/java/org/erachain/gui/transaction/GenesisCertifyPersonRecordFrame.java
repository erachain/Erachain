package org.erachain.gui.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.GenesisCertifyPersonRecord;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class GenesisCertifyPersonRecordFrame extends RecGenesis_DetailsFrame {
    public GenesisCertifyPersonRecordFrame(GenesisCertifyPersonRecord record) {
        super(record);

        //LABEL RECIPIENT
        ++labelGBC.gridy;
        JLabel recipientLabel = new JLabel(Lang.getInstance().translate("Recipient") + ":");
        this.add(recipientLabel, labelGBC);

        //RECIPIENT
        ++detailGBC.gridy;
        JTextField recipient = new JTextField(record.viewRecipient());
        recipient.setEditable(false);
        MenuPopupUtil.installContextMenu(recipient);
        this.add(recipient, detailGBC);

        //LABEL PERSON
        ++labelGBC.gridy;
        JLabel assetLabel = new JLabel(Lang.getInstance().translate("Person") + ":");
        this.add(assetLabel, labelGBC);

        //PERSON
        ++detailGBC.gridy;
        JTextField asset = new JTextField(String.valueOf(Controller.getInstance().getPerson(record.getKey()).toString()));
        asset.setEditable(false);
        MenuPopupUtil.installContextMenu(asset);
        this.add(asset, detailGBC);

        //PACK
//		this.pack();
        //       this.setResizable(false);
        //       this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
