package org.erachain.gui.transaction;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.transaction.RCalculated;
import org.erachain.gui.library.MAccoutnTextField;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class RCalculatedDetailsFrame extends RecDetailsFrame {

    public RCalculatedDetailsFrame(final RCalculated r_Calc) {
        super(r_Calc, true);
        //LABEL RECIPIENT
        ++labelGBC.gridy;
        JLabel recipientLabel = new JLabel(Lang.T("Recipient") + ":");
        this.add(recipientLabel, labelGBC);

        //RECIPIENT
        ++fieldGBC.gridy;
        MAccoutnTextField recipient = new MAccoutnTextField(r_Calc.getRecipient());
        recipient.setEditable(false);
        this.add(recipient, fieldGBC);

        if (r_Calc.getMessage() != null) {
            //LABEL MESSAGE
            ++labelGBC.gridy;
            JLabel title_Label = new JLabel(Lang.T("Message") + ":");
            this.add(title_Label, labelGBC);

            // ISTEXT
            ++fieldGBC.gridy;
            fieldGBC.gridwidth = 2;
            JTextField head_Text = new JTextField(r_Calc.getMessage());
            head_Text.setEditable(false);
            MenuPopupUtil.installContextMenu(head_Text);
            this.add(head_Text, fieldGBC);
        }

        if (r_Calc.getAmount() != null) {

            String sendType = Lang.T(r_Calc.viewFullTypeName());
            //LABEL AMOUNT
            ++labelGBC.gridy;
            JLabel amountLabel = new JLabel(sendType + ":");
            this.add(amountLabel, labelGBC);

            //AMOUNT
            fieldGBC.gridy = labelGBC.gridy;
            fieldGBC.gridwidth = 2;
            JTextField amount = new JTextField(r_Calc.getAmount().toPlainString());
            amount.setEditable(false);
            MenuPopupUtil.installContextMenu(amount);
            this.add(amount, fieldGBC);

            //ASSET
            long assetKey = r_Calc.getAbsKey();
            if (assetKey > 0) {
                fieldGBC.gridx = 3;
                fieldGBC.gridwidth = 1;
                JTextField asset = new JTextField(Controller.getInstance().getAsset(assetKey).toString());
                asset.setEditable(false);
                MenuPopupUtil.installContextMenu(asset);
                this.add(asset, fieldGBC);
                fieldGBC.gridx = 1;
                fieldGBC.gridwidth = 3;
            }
        }

        this.setVisible(true);
    }
}
