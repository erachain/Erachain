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
public class SetStatusToItemDetailsFrame extends JFrame
{
	public SetStatusToItemDetailsFrame(R_SetStatusToItem setStatusToItem)
	{
		super(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Transaction Details"));
		
		ItemCls item = setStatusToItem.getItem();
		//NAME
		long status_key = setStatusToItem.getKey();
		StatusCls status = Controller.getInstance().getItemStatus(status_key);
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//DETAIL GBC
		GridBagConstraints detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 5, 0);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;  
		detailGBC.anchor = GridBagConstraints.NORTHWEST;
		detailGBC.weightx = 1;	
		detailGBC.gridwidth = 2;
		detailGBC.gridx = 1;		
		
		int gridy = 0;
		//LABEL TYPE
		labelGBC.gridy = gridy;
		JLabel typeLabel = new JLabel(Lang.getInstance().translate("Type") + ":");
		this.add(typeLabel, labelGBC);
						
		//TYPE
		detailGBC.gridy = gridy;
		JLabel type = new JLabel(Lang.getInstance().translate("Set Status to Item"));
		this.add(type, detailGBC);
		
		//LABEL SIGNATURE
		labelGBC.gridy = ++gridy;
		JLabel signatureLabel = new JLabel(Lang.getInstance().translate("Signature") + ":");
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = gridy;
		JTextField signature = new JTextField(Base58.encode(setStatusToItem.getSignature()));
		signature.setEditable(false);
		MenuPopupUtil.installContextMenu(signature);
		this.add(signature, detailGBC);
		
		//LABEL REFERENCE
		labelGBC.gridy = ++gridy;
		JLabel referenceLabel = new JLabel(Lang.getInstance().translate("Reference") + ":");
		this.add(referenceLabel, labelGBC);
						
		//REFERENCE
		detailGBC.gridy = gridy;
		JTextField reference = new JTextField(Base58.encode(setStatusToItem.getReference()));
		reference.setEditable(false);
		MenuPopupUtil.installContextMenu(reference);
		this.add(reference, detailGBC);
		
		//LABEL TIMESTAMP
		labelGBC.gridy = ++gridy;
		JLabel timestampLabel = new JLabel(Lang.getInstance().translate("Timestamp") + ":");
		this.add(timestampLabel, labelGBC);
						
		//TIMESTAMP
		detailGBC.gridy = gridy;
		JTextField timestamp = new JTextField(DateTimeFormat.timestamptoString(setStatusToItem.getTimestamp()));
		timestamp.setEditable(false);
		MenuPopupUtil.installContextMenu(timestamp);
		this.add(timestamp, detailGBC);
		
		//LABEL CREATOR
		labelGBC.gridy = ++gridy;
		JLabel creatorLabel = new JLabel(Lang.getInstance().translate("Creator") + ":");
		this.add(creatorLabel, labelGBC);
		
		//CREATOR
		detailGBC.gridy = gridy;
		JTextField creator = new JTextField(setStatusToItem.getCreator().getAddress());
		creator.setEditable(false);
		MenuPopupUtil.installContextMenu(creator);
		this.add(creator, detailGBC);

		// STATUS
		//LABEL NAME
		labelGBC.gridy = ++gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Status Name") + ":");
		this.add(nameLabel, labelGBC);

		////// STATUS
		detailGBC.gridy = gridy;
		JTextField statusName = new JTextField(status.getName());
		statusName.setEditable(false);
		MenuPopupUtil.installContextMenu(statusName);
		this.add(statusName, detailGBC);		
		
		//LABEL DESCRIPTION
		labelGBC.gridy = ++gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Status Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		detailGBC.gridy = gridy;
		JTextArea txtAreaDescription = new JTextArea(status.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(statusName.getBorder());
		txtAreaDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaDescription);
		this.add(txtAreaDescription, detailGBC);		

		// //// ITEM
		//LABEL NAME
		labelGBC.gridy = ++gridy;
		JLabel itemNameLabel = new JLabel(Lang.getInstance().translate("Item Name") + ":");
		this.add(itemNameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = gridy;
		JTextField itemName = new JTextField(item.getItemTypeStr() + " - " + item.getItemSubType()
		+ ": " + item.getName());
		itemName.setEditable(false);
		MenuPopupUtil.installContextMenu(itemName);
		this.add(itemName, detailGBC);		
		
		//LABEL DESCRIPTION
		labelGBC.gridy = ++gridy;
		JLabel itemDescriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(itemDescriptionLabel, labelGBC);
				
		//DESCRIPTION
		detailGBC.gridy = gridy;
		JTextArea txtAreaItemDescription = new JTextArea(item.getDescription());
		txtAreaItemDescription.setRows(4);
		txtAreaItemDescription.setBorder(statusName.getBorder());
		txtAreaItemDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaItemDescription);
		this.add(txtAreaItemDescription, detailGBC);		
				
		//LABEL FEE
		labelGBC.gridy = ++gridy;
		JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
		this.add(feeLabel, labelGBC);
						
		//FEE
		detailGBC.gridy = gridy;
		JTextField fee = new JTextField("("+setStatusToItem.getFeePow() + ")="+setStatusToItem.getFee().toPlainString());
		fee.setEditable(false);
		MenuPopupUtil.installContextMenu(fee);
		this.add(fee, detailGBC);	
		
		//LABEL CONFIRMATIONS
		labelGBC.gridy = ++gridy;
		JLabel confirmationsLabel = new JLabel(Lang.getInstance().translate("Confirmations") + ":");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = gridy;
		JLabel confirmations = new JLabel(String.valueOf(setStatusToItem.getConfirmations()));
		this.add(confirmations, detailGBC);	

		//LABEL SIZE
		labelGBC.gridy = ++gridy;
		JLabel sizeLabel = new JLabel(Lang.getInstance().translate("Size") + ":");
		this.add(sizeLabel, labelGBC);
								
		//SIZE
		detailGBC.gridy = gridy;
		JLabel size = new JLabel(String.valueOf(setStatusToItem.getDataLength(false)));
		this.add(size, detailGBC);	
		           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
