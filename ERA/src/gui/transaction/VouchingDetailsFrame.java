package gui.transaction;

import core.transaction.R_Vouch;
import core.transaction.Transaction;
import datachain.DCSet;
import lang.Lang;
import utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class VouchingDetailsFrame extends Rec_DetailsFrame {
    public VouchingDetailsFrame(R_Vouch vouchRecord) {
        super(vouchRecord);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Height-seq.") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(vouchRecord.getVouchHeight() + "-" + vouchRecord.getVouchSeqNo());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        Transaction record = DCSet.getInstance().getTransactionFinalMap().
                getBySignature(vouchRecord.getVouchHeight(), vouchRecord.getVouchSeqNo());

        String message = "<div>";
        if (record == null) {
            message += "NULL</div>";
        } else {
            message += ", time: " + record.viewTimestamp() + "</div>";
            message += "<div> type: <b>" + record.viewFullTypeName() + "</b>, size: " + record.viewSize(Transaction.FOR_NETWORK) + ", fee:" + record.viewFee() + "</div>";

            //message += "<div>REF: <font size='2'>" + record.viewReference() + "</font></div>";
            message += "<div>SIGN: <font size='2'>" + record.viewSignature() + "</font></div>";

            message += "<div>Creator: <font size='4'>" + record.viewCreator() + "</font></div>";
            message += "<div>Item: <font size='4'>" + record.viewItemName() + "</font></div>";
            message += "<div>Amount: <font size='4'>" + record.viewAmount() + "</font></div>";
            message += "<div>Recipient: <font size='4'>" + record.viewRecipient() + "</font></div>";
        }


        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++detailGBC.gridy;
        JTextPane txtAreaDescription = new JTextPane();
        txtAreaDescription.setContentType("text/html");
        //	txtAreaDescription.setBackground(MainFrame.getFrames()[0].getBackground());

        txtAreaDescription.setText(message);
		/*
		txtAreaDescription.setRows(4);
		txtAreaDescription.setColumns(4);
		*/
        txtAreaDescription.setBorder(name.getBorder());
        txtAreaDescription.setEditable(false);
        //MenuPopupUtil.installContextMenu(txtAreaDescription);
        this.add(txtAreaDescription, detailGBC);

        //PACK
//		this.pack();
//        this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
