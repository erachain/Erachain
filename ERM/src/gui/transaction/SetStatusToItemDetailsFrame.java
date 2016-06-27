package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.nio.charset.Charset;
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
		
		//ItemCls item = ItemCls.setStatusToItem.getItemType();
		//ItemCls item = db.getItem_Map(this.itemType).get(this.itemKey);
		ItemCls item = Controller.getInstance().getItem(setStatusToItem.getItemType(), setStatusToItem.getItemKey());

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

		// FROM - TO DATE
		++labelGBC.gridy;
		this.add(new JLabel(Lang.getInstance().translate("From - To") + ":"), labelGBC);						
		++detailGBC.gridy;
		long beginDate = setStatusToItem.getBeginDate();
		long endDate = setStatusToItem.getEndDate();
		
		JTextField fromToDate = new JTextField((beginDate == Long.MIN_VALUE?"?":DateTimeFormat.timestamptoString(beginDate))
				+ " - " + (endDate == Long.MAX_VALUE? "?":DateTimeFormat.timestamptoString(endDate)));
		fromToDate.setEditable(false);
		MenuPopupUtil.installContextMenu(fromToDate);
		this.add(fromToDate, detailGBC);

		if (setStatusToItem.getValue1() != 0) {
			//LABEL VALUE 1
			++labelGBC.gridy;
			this.add(new JLabel(Lang.getInstance().translate("Value") + " 1:"), labelGBC);
			//VALUE 1
			++detailGBC.gridy;
			JTextField statusValue1 = new JTextField("" + setStatusToItem.getValue1());
			statusValue1.setEditable(false);
			MenuPopupUtil.installContextMenu(statusValue1);
			this.add(statusValue1, detailGBC);
		}

		if (setStatusToItem.getValue2() != 0) {
			//LABEL VALUE 2
			++labelGBC.gridy;
			this.add(new JLabel(Lang.getInstance().translate("Value") + " 2:"), labelGBC);
			//VALUE 2
			++detailGBC.gridy;
			JTextField statusValue2 = new JTextField("" + setStatusToItem.getValue2());
			statusValue2.setEditable(false);
			MenuPopupUtil.installContextMenu(statusValue2);
			this.add(statusValue2, detailGBC);
		}

		if (setStatusToItem.getData() != null) {

			//LABEL ADDITION DATA
			++labelGBC.gridy;
			this.add(new JLabel(Lang.getInstance().translate("DATA") + ":"), labelGBC);
			//DATA
			++detailGBC.gridy;
			JTextField statusAData = new JTextField(new String(setStatusToItem.getData(), Charset.forName("UTF-8")));
			statusAData.setEditable(false);
			MenuPopupUtil.installContextMenu(statusAData);
			this.add(statusAData, detailGBC);
		}

		if (setStatusToItem.getRefParent() != 0l) {

			//LABEL PARENT
			++labelGBC.gridy;
			this.add(new JLabel(Lang.getInstance().translate("Parent") + ":"), labelGBC);
			//DATA
			++detailGBC.gridy;
			JTextField statusRefParent = new JTextField("" + setStatusToItem.viewRefParent());
			statusRefParent.setEditable(false);
			MenuPopupUtil.installContextMenu(statusRefParent);
			this.add(statusRefParent, detailGBC);
		}

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
