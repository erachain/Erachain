package org.erachain.gui.transaction;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.transaction.RCalculated;
import org.erachain.gui.library.MTextPane;
import org.erachain.gui.library.MAccoutnTextField;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

@SuppressWarnings("serial")
public class RCalculatedDetailsFrame extends RecDetailsFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(RCalculatedDetailsFrame.class);
    private JTextField messageText;
    private JScrollPane jScrollPane1;
    private MTextPane jTextArea_Messge;
    private RCalculatedDetailsFrame th;

    public RCalculatedDetailsFrame(final RCalculated r_Calc) {
        super(r_Calc);
        th = this;
        //LABEL RECIPIENT
        ++labelGBC.gridy;
        JLabel recipientLabel = new JLabel(Lang.getInstance().translate("Recipient") + ":");
        this.add(recipientLabel, labelGBC);

        //RECIPIENT
        ++detailGBC.gridy;
        MAccoutnTextField recipient = new MAccoutnTextField(r_Calc.getRecipient());
        //	JTextField recipient = new JTextField(r_Send.getRecipient().getAddress());
        recipient.setEditable(false);
        //	MenuPopupUtil.installContextMenu(recipient);
        this.add(recipient, detailGBC);

	/*	String personStr = r_Send.getRecipient().viewPerson();
		if (personStr.length()>0) {
			++labelGBC.gridy;
			++detailGBC.gridy;
			this.add(new JLabel(personStr), detailGBC);
		}
		*/

        if (r_Calc.getMessage() != null) {
            //LABEL MESSAGE
            ++labelGBC.gridy;
            JLabel title_Label = new JLabel(Lang.getInstance().translate("Message") + ":");
            this.add(title_Label, labelGBC);

            // ISTEXT
            ++detailGBC.gridy;
            detailGBC.gridwidth = 2;
            JTextField head_Text = new JTextField(r_Calc.getMessage());
            head_Text.setEditable(false);
            MenuPopupUtil.installContextMenu(head_Text);
            this.add(head_Text, detailGBC);
        }

        if (r_Calc.getAmount() != null) {

            String sendType = Lang.getInstance().translate(r_Calc.viewFullTypeName());
            //LABEL AMOUNT
            ++labelGBC.gridy;
            JLabel amountLabel = new JLabel(sendType + ":");
            this.add(amountLabel, labelGBC);

            //AMOUNT
            detailGBC.gridy = labelGBC.gridy;
            detailGBC.gridwidth = 2;
            JTextField amount = new JTextField(r_Calc.getAmount().toPlainString());
            amount.setEditable(false);
            MenuPopupUtil.installContextMenu(amount);
            this.add(amount, detailGBC);

            //ASSET
            long assetKey = r_Calc.getAbsKey();
            if (assetKey > 0) {
                detailGBC.gridx = 3;
                detailGBC.gridwidth = 1;
                JTextField asset = new JTextField(Controller.getInstance().getAsset(assetKey).toString());
                asset.setEditable(false);
                MenuPopupUtil.installContextMenu(asset);
                this.add(asset, detailGBC);
                detailGBC.gridx = 1;
                detailGBC.gridwidth = 3;
            }
        }

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
