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

import core.crypto.Base58;
import core.transaction.GenesisIssueAssetTransaction;
import datachain.DCSet;
import core.item.assets.AssetCls;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class GenesisIssueAssetDetailsFrame extends RecGenesis_DetailsFrame
{
	public GenesisIssueAssetDetailsFrame(GenesisIssueAssetTransaction assetIssue)
	{
		super(assetIssue);
		
		AssetCls asset = (AssetCls)assetIssue.getItem();
		
		//LABEL NAME
		++labelGBC.gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		++detailGBC.gridy;
		JTextField name = new JTextField(asset.getName());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		
		
		//LABEL DESCRIPTION
		++labelGBC.gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		++detailGBC.gridy;
		JTextArea txtAreaDescription = new JTextArea(asset.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaDescription);
		this.add(txtAreaDescription, detailGBC);		
		
		//LABEL QUANTITY
		++labelGBC.gridy;
		JLabel quantityLabel = new JLabel(Lang.getInstance().translate("Quantity") + ":");
		this.add(quantityLabel, labelGBC);
				
		//QUANTITY
		++detailGBC.gridy;
		JTextField quantity = new JTextField(asset.getQuantity(DCSet.getInstance()).toString());
		quantity.setEditable(false);
		MenuPopupUtil.installContextMenu(quantity);
		this.add(quantity, detailGBC);	
		
		//LABEL DIVISIBLE
		++labelGBC.gridy;
		JLabel divisibleLabel = new JLabel(Lang.getInstance().translate("Divisible") + ":");
		this.add(divisibleLabel, labelGBC);
				
		//DIVISIBLE
		++detailGBC.gridy;
		JCheckBox divisible = new JCheckBox();
		divisible.setSelected(asset.isDivisible());
		divisible.setEnabled(false);
		this.add(divisible, detailGBC);	
						           
        //PACK
	//	this.pack();
    //    this.setResizable(false);
    //    this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
