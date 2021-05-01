package org.erachain.gui.transaction;

import org.erachain.core.transaction.RVouch;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class SigningDetailsFrame extends RecDetailsFrame {
    public SigningDetailsFrame(RVouch vouchRecord) {
        super(vouchRecord, true);

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.T("Height-seq.") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++fieldGBC.gridy;
        JTextField name = new JTextField(vouchRecord.getRefHeight() + "-" + vouchRecord.getRefSeqNo());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, fieldGBC);

        Transaction record = DCSet.getInstance().getTransactionFinalMap().
                get(vouchRecord.getRefHeight(), vouchRecord.getRefSeqNo());

        String message = "<div>";
        if (record == null) {
            message += "NULL</div>";
        } else {
            message += ", time: " + record.viewTimestamp() + "</div>";
            message += "<div> type: <b>" + Lang.T(record.viewFullTypeName()) + "</b>, size: " + record.viewSize(Transaction.FOR_NETWORK)
                    + ", fee: " + record.viewFeeAndFiat(UIManager.getFont("Label.font").getSize()) + "</div>";

            //message += "<div>REF: <font size='2'>" + record.viewReference() + "</font></div>";
            message += "<div>SIGN: <font size='2'>" + record.viewSignature() + "</font></div>";

            message += "<div>Creator: <font size='4'>" + record.viewCreator() + "</font></div>";
            message += "<div>Item: <font size='4'>" + record.viewItemName() + "</font></div>";
            message += "<div>Amount: <font size='4'>" + record.viewAmount() + "</font></div>";
            message += "<div>Recipient: <font size='4'>" + record.viewRecipient() + "</font></div>";
        }


        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.T("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++fieldGBC.gridy;
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
        this.add(txtAreaDescription, fieldGBC);

        //PACK
//		this.pack();
//        this.setResizable(false);
//        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
