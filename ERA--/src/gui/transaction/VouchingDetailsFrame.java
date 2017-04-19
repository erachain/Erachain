package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import core.crypto.Base58;
import core.transaction.Transaction;
import core.transaction.R_Vouch;
import database.DBSet;
import gui.MainFrame;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class VouchingDetailsFrame extends Rec_DetailsFrame
{
	public VouchingDetailsFrame(R_Vouch vouchRecord)
	{
		super(vouchRecord);
				
		//LABEL NAME
		++labelGBC.gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("height-seq.") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		++detailGBC.gridy;
		JTextField name = new JTextField(vouchRecord.getVouchHeight() + "-" + vouchRecord.getVouchSeq());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);
		
		Transaction record = DBSet.getInstance().getTransactionFinalMap().
				getTransaction(vouchRecord.getVouchHeight(), vouchRecord.getVouchSeq());
		
		String message = "<div>"
				+ ", time: " + record.viewTimestamp() + "</div>";
			message += "<div> type: <b>" + record.viewFullTypeName() + "</b>, size: " + record.viewSize(false) + ", fee:" + record.viewFee() + "</div>";
		
			message += "<div>REF: <font size='2'>" + record.viewReference() + "</font></div>";
			message += "<div>SIGN: <font size='2'>" + record.viewSignature() + "</font></div>";

			message += "<div>Creator: <font size='4'>" + record.viewCreator() + "</font></div>";
			message += "<div>Item: <font size='4'>" + record.viewItemName() + "</font></div>";
			message += "<div>Amount: <font size='4'>" + record.viewAmount() + "</font></div>";
			message += "<div>Recipient: <font size='4'>" + record.viewRecipient() + "</font></div>";

		//LABEL DESCRIPTION
		++labelGBC.gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		++detailGBC.gridy;
		JTextPane txtAreaDescription = new JTextPane();
		txtAreaDescription.setContentType("text/html");
		txtAreaDescription.setBackground(MainFrame.getFrames()[0].getBackground());

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
