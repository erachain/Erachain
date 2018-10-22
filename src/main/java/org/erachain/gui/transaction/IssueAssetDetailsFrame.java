package org.erachain.gui.transaction;

import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.gui.items.assets.Asset_Info;
import org.erachain.lang.Lang;

import javax.swing.*;

@SuppressWarnings("serial")
public class IssueAssetDetailsFrame extends Rec_DetailsFrame {
    public IssueAssetDetailsFrame(IssueAssetTransaction assetIssue) {
        super(assetIssue);

        Asset_Info as_info = new Asset_Info((AssetCls) assetIssue.getItem());
        //LABEL NAME
        ++labelGBC.gridy;
        labelGBC.gridwidth = 4;
        labelGBC.fill = labelGBC.BOTH;
        labelGBC.weightx = 0.1;
        labelGBC.weightx = 0.1;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(as_info, labelGBC);
		
	/*	
		//LABEL NAME
		++labelGBC.gridy;
		JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
		this.add(nameLabel, labelGBC);
		
		//NAME
		++detailGBC.gridy;
		JTextField name = new JTextField(assetIssue.getItem().viewName());
		name.setEditable(false);
		MenuPopupUtil.installContextMenu(name);
		this.add(name, detailGBC);		
		
		//LABEL DESCRIPTION
		++labelGBC.gridy;
		JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		++detailGBC.gridy;
		JTextArea txtAreaDescription = new JTextArea(assetIssue.getItem().getDescription());
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
		JTextField quantity = new JTextField(((AssetCls)assetIssue.getItem()).getQuantity().toString());
		quantity.setEditable(false);
		MenuPopupUtil.installContextMenu(quantity);
		this.add(quantity, detailGBC);	
		
		//LABEL DIVISIBLE
		++labelGBC.gridy;
		JLabel divisibleLabel = new JLabel(Lang.getInstance().translate("Divisible") + ":");
		this.add(divisibleLabel, labelGBC);
				
		//QUANTITY
		++detailGBC.gridy;
		JCheckBox divisible = new JCheckBox();
		divisible.setSelected(((AssetCls)assetIssue.getItem()).isDivisible());
		divisible.setEnabled(false);
		this.add(divisible, detailGBC);	
		
		//LABEL movable
				++labelGBC.gridy;
				JLabel mavableLabel = new JLabel(Lang.getInstance().translate("Movable") + ":");
				this.add(mavableLabel, labelGBC);
						
				//QUANTITY
				++detailGBC.gridy;
				JCheckBox movable = new JCheckBox();
				movable.setSelected(((AssetCls)assetIssue.getItem()).isMovable());
				movable.setEnabled(false);
				this.add(movable, detailGBC);	
*/
        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);

    }
}
