package gui.transaction;

import core.item.assets.AssetCls;
import core.transaction.GenesisIssueAssetTransaction;
import lang.Lang;
import utils.MenuPopupUtil;

import javax.swing.*;

@SuppressWarnings("serial")
public class GenesisIssueAssetDetailsFrame extends RecGenesis_DetailsFrame {
    public GenesisIssueAssetDetailsFrame(GenesisIssueAssetTransaction assetIssue) {
        super(assetIssue);

        AssetCls asset = (AssetCls) assetIssue.getItem();

        //LABEL NAME
        ++labelGBC.gridy;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        this.add(nameLabel, labelGBC);

        //NAME
        ++detailGBC.gridy;
        JTextField name = new JTextField(asset.viewName());
        name.setEditable(false);
        MenuPopupUtil.installContextMenu(name);
        this.add(name, detailGBC);

        //LABEL DESCRIPTION
        ++labelGBC.gridy;
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        this.add(descriptionLabel, labelGBC);

        //DESCRIPTION
        ++detailGBC.gridy;
        JTextArea txtAreaDescription = new JTextArea(Lang.getInstance().translate(asset.viewDescription()));
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
        JTextField quantity = new JTextField(asset.getQuantity().toString());
        quantity.setEditable(false);
        MenuPopupUtil.installContextMenu(quantity);
        this.add(quantity, detailGBC);

        //LABEL DIVISIBLE
        ++labelGBC.gridy;
        JLabel divisibleLabel = new JLabel(Lang.getInstance().translate("Asset Type") + ":");
        this.add(divisibleLabel, labelGBC);

        //DIVISIBLE
        ++detailGBC.gridy;
        //	JCheckBox divisible = new JCheckBox();
        JTextField textType = new JTextField(Lang.getInstance().translate(asset.viewAssetType()));
        //	= asset.viewAssetType();
        //	divisible.setSelected(asset.isDivisible()); //
        textType.setEnabled(false);
        this.add(textType, detailGBC);

        //PACK
        //	this.pack();
        //    this.setResizable(false);
        //    this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
