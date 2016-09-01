package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.Controller;
import core.crypto.Base58;
import core.transaction.GenesisTransferAssetTransaction;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class GenesisTransferAssetDetailsFrame extends RecGenesis_DetailsFrame
{
	public GenesisTransferAssetDetailsFrame(GenesisTransferAssetTransaction assetTransfer)
	{
		super(assetTransfer);
				
		//LABEL RECIPIENT
		++labelGBC.gridy;
		JLabel recipientLabel = new JLabel(Lang.getInstance().translate("Recipient") + ":");
		this.add(recipientLabel, labelGBC);
		
		//RECIPIENT
		++detailGBC.gridy;
		JTextField recipient = new JTextField(assetTransfer.getRecipient().getAddress());
		recipient.setEditable(false);
		MenuPopupUtil.installContextMenu(recipient);
		this.add(recipient, detailGBC);		
		
		String personStr = assetTransfer.getRecipient().viewPerson();
		if (personStr.length()>0) {
			++labelGBC.gridy;
			++detailGBC.gridy;
			this.add(new JLabel(personStr), detailGBC);
		}

		//LABEL ASSET
		++labelGBC.gridy;
		JLabel assetLabel = new JLabel(Lang.getInstance().translate("Asset") + ":");
		this.add(assetLabel, labelGBC);
		
		//ASSET
		++detailGBC.gridy;
		JTextField asset = new JTextField(String.valueOf(Controller.getInstance()
				.getAsset(assetTransfer.getAbsKey()).toString()));
		asset.setEditable(false);
		MenuPopupUtil.installContextMenu(asset);
		this.add(asset, detailGBC);	
		
		//LABEL AMOUNT
		++labelGBC.gridy;
		JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount") + ":");
		this.add(amountLabel, labelGBC);
				
		//AMOUNT
		++detailGBC.gridy;
		JTextField amount = new JTextField(assetTransfer.getAmount().toPlainString());
		amount.setEditable(false);
		MenuPopupUtil.installContextMenu(amount);
		this.add(amount, detailGBC);	
						           
        //PACK
	//	this.pack();
  //      this.setResizable(false);
 //       this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
