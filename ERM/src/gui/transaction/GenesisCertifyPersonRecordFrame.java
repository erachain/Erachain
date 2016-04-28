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
import core.transaction.GenesisCertifyPersonRecord;
import core.item.assets.AssetCls;
import lang.Lang;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class GenesisCertifyPersonRecordFrame extends JFrame
{
	public GenesisCertifyPersonRecordFrame(GenesisCertifyPersonRecord record)
	{
		super(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Transaction Details"));
		
		AssetCls asset = null; //(AssetCls)record.getItem();
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
		
		//LABEL TYPE
		labelGBC.gridy = 0;
		JLabel typeLabel = new JLabel(Lang.getInstance().translate("Type") + ":");
		this.add(typeLabel, labelGBC);
						
		//TYPE
		detailGBC.gridy = 0;
		JLabel type = new JLabel(Lang.getInstance().translate("Issue Asset Transaction"));
		this.add(type, detailGBC);
		
		//LABEL SIGNATURE
		labelGBC.gridy = 1;
		JLabel signatureLabel = new JLabel(Lang.getInstance().translate("Signature") + ":");
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = 1;
		JTextField signature = new JTextField(Base58.encode(record.getSignature()));
		signature.setEditable(false);
		MenuPopupUtil.installContextMenu(signature);
		this.add(signature, detailGBC);
		
		/*
		//LABEL REFERENCE
		labelGBC.gridy = 2;
		JLabel referenceLabel = new JLabel(Lang.getInstance().translate("Reference") + ":");
		this.add(referenceLabel, labelGBC);
						
		//REFERENCE
		detailGBC.gridy = 2;
		JTextField reference = new JTextField(Base58.encode(record.getReference()));
		reference.setEditable(false);
		MenuPopupUtil.installContextMenu(reference);
		this.add(reference, detailGBC);
		
		//LABEL OWNER
		labelGBC.gridy = 5;
		JLabel ownerLabel = new JLabel(Lang.getInstance().translate("Owner") + ":");
		this.add(ownerLabel, labelGBC);
				
		//OWNER
		detailGBC.gridy = 5;
		JTextField owner = new JTextField("genesis");
		owner.setEditable(false);
		MenuPopupUtil.installContextMenu(owner);
		this.add(owner, detailGBC);
		*/
		
		//LABEL NAME
		labelGBC.gridy = 6;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = 6;
		JTextField name = new JTextField(asset.getName());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		
		
		/*
		//LABEL DESCRIPTION
		labelGBC.gridy = 7;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		detailGBC.gridy = 7;
		JTextArea txtAreaDescription = new JTextArea(asset.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		MenuPopupUtil.installContextMenu(txtAreaDescription);
		this.add(txtAreaDescription, detailGBC);		
		
		//LABEL QUANTITY
		labelGBC.gridy = 8;
		JLabel quantityLabel = new JLabel(Lang.getInstance().translate("Quantity") + ":");
		this.add(quantityLabel, labelGBC);
				
		//QUANTITY
		detailGBC.gridy = 8;
		JTextField quantity = new JTextField(asset.getQuantity().toString());
		quantity.setEditable(false);
		MenuPopupUtil.installContextMenu(quantity);
		this.add(quantity, detailGBC);	
		
		//LABEL DIVISIBLE
		labelGBC.gridy = 9;
		JLabel divisibleLabel = new JLabel(Lang.getInstance().translate("Divisible") + ":");
		this.add(divisibleLabel, labelGBC);
				
		//QUANTITY
		detailGBC.gridy = 9;
		JCheckBox divisible = new JCheckBox();
		divisible.setSelected(asset.isDivisible());
		divisible.setEnabled(false);
		this.add(divisible, detailGBC);	
		*/
				
		//LABEL CONFIRMATIONS
		labelGBC.gridy = 11;
		JLabel confirmationsLabel = new JLabel(Lang.getInstance().translate("Confirmations") + ":");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = 11;
		JLabel confirmations = new JLabel(String.valueOf(record.getConfirmations()));
		this.add(confirmations, detailGBC);	
		           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
