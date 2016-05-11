package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Date;
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
import core.item.ItemCls;
import core.item.statuses.StatusCls;
import core.transaction.R_SetStatusToItem;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class SetStatusToItemDetailsFrame extends Rec_DetailsFrame
{
	public SetStatusToItemDetailsFrame(R_SetStatusToItem setStatusToItem)
	{
		super(setStatusToItem);
		
		ItemCls item = setStatusToItem.getItem();
		//NAME
		long status_key = setStatusToItem.getKey();
		StatusCls status = Controller.getInstance().getItemStatus(status_key);
		
		// STATUS
		//LABEL NAME
		++labelGBC.gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Status Name") + ":");
		this.add(nameLabel, labelGBC);

		////// STATUS
		++detailGBC.gridy;
		JTextField statusName = new JTextField(status.getName());
		statusName.setEditable(false);
		MenuPopupUtil.installContextMenu(statusName);
		this.add(statusName, detailGBC);		
		
		//LABEL DESCRIPTION
		++labelGBC.gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Status Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		++detailGBC.gridy;
		JTextArea txtAreaDescription = new JTextArea(status.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(statusName.getBorder());
		txtAreaDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaDescription);
		this.add(txtAreaDescription, detailGBC);		

		// //// ITEM
		//LABEL NAME
		++labelGBC.gridy;
		JLabel itemNameLabel = new JLabel(Lang.getInstance().translate("Item Name") + ":");
		this.add(itemNameLabel, labelGBC);
		
		//NAME
		++detailGBC.gridy;
		JTextField itemName = new JTextField(item.getItemTypeStr() + " - " + item.getItemSubType()
		+ ": " + item.getName());
		itemName.setEditable(false);
		MenuPopupUtil.installContextMenu(itemName);
		this.add(itemName, detailGBC);		
		
		//LABEL DESCRIPTION
		++labelGBC.gridy;
		JLabel itemDescriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(itemDescriptionLabel, labelGBC);
				
		//DESCRIPTION
		++detailGBC.gridy;
		JTextArea txtAreaItemDescription = new JTextArea(item.getDescription());
		txtAreaItemDescription.setRows(4);
		txtAreaItemDescription.setBorder(statusName.getBorder());
		txtAreaItemDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaItemDescription);
		this.add(txtAreaItemDescription, detailGBC);		
						           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
