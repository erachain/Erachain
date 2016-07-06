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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.Controller;
import core.crypto.Base58;
import core.transaction.GenesisCertifyPersonRecord;
import core.item.assets.AssetCls;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class GenesisCertifyPersonRecordFrame extends RecGenesis_DetailsFrame
{
	public GenesisCertifyPersonRecordFrame(GenesisCertifyPersonRecord record)
	{
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
