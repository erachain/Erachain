package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.exLink.ExLinkAddress;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.gui.models.FavoriteComboBoxModel;
import org.erachain.lang.Lang;

import javax.swing.*;

/**
 * @author Саша
 */
public class IssueAssetCopyPanel extends IssueAssetPanelCls {

    public static String NAME = "IssueAssetCopyPanel";
    public static String TITLE = "Issue Series";

    public JTextField assetRefField = new JTextField("");
    byte[] origAssetTXSign;
    public javax.swing.JComboBox<ItemCls> jComboBox_Asset;


    public IssueAssetCopyPanel() {
        super(NAME, TITLE, "Asset series issue has been sent!", true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE,
                true, true);

        quantityJLabel.setText(Lang.T("Series Size") + ":");
        initComponents();

    }

    protected void initComponents() {

        super.initComponents();

        int gridy = initTopArea(true);

        // favorite combo box
        jComboBox_Asset = new javax.swing.JComboBox<>();
        jComboBox_Asset.setModel(new ComboBoxAssetsNFTModel());
        jComboBox_Asset.setRenderer(new FavoriteComboBoxModel.IconListRenderer());
        jComboBox_Asset.setEditable(false);
        //this.jComboBox_Asset.setEnabled(assetIn != null);

        JLabel signLabel = new JLabel(Lang.T("Original Asset") + ":");
        labelGBC.gridy = gridy;
        jPanelAdd.add(signLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(jComboBox_Asset, fieldGBC);

        labelGBC.gridy = ++gridy;
        jPanelAdd.add(quantityJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(textQuantity, fieldGBC);

        // вывод подвала
        super.initBottom(gridy);
    }

    protected boolean checkValues() {

        assetType = AssetCls.AS_NON_FUNGIBLE;

        AssetCls asset = (AssetCls) this.jComboBox_Asset.getSelectedItem();
        origAssetTXSign = asset.getReference();

        int parseStep = 0;
        try {

            // READ QUANTITY
            parseStep++;
            quantity = Long.parseLong(textQuantity.getText());

        } catch (Exception e) {
            switch (parseStep) {
                case 1:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.T("Invalid quantity!"), Lang.T("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
            }
            return false;
        }

        return true;
    }

    @Override
    protected void makeTransaction() {

        // PROTOTYPE ASSET
        AssetVenture prototypeAsset = new AssetVenture(itemAppData, creator, nameField.getText(), addIconLabel.getMediaBytes(),
                addImageLabel.getMediaBytes(), textAreaDescription.getText(),
                assetType, 0, quantity);

        transaction = Controller.getInstance().issueAssetSeries(creator, exLink, feePow, origAssetTXSign, prototypeAsset);

    }

    @Override
    protected String makeBodyView() {

        String out = super.makeBodyView();
        AssetCls asset = (AssetCls) item;

        out += Lang.T("Asset Class") + ":&nbsp;"
                + Lang.T(asset.getItemSubType() + "") + "<br>"
                + Lang.T("Asset Type") + ":&nbsp;"
                + "<b>" + asset.charAssetType() + asset.viewAssetTypeAbbrev() + "</b>:" + Lang.T(asset.viewAssetTypeFull() + "") + "<br>"
                + Lang.T("Quantity") + ":&nbsp;" + asset.getQuantity() + ", "
                + Lang.T("Scale") + ":&nbsp;" + asset.getScale() + "<br>";

        if (asset.getDEXAwards() != null) {
            out += Lang.T("DEX Awards" + ":");
            for (ExLinkAddress award : asset.getDEXAwards()) {
                out += "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + award.getAccount().getPersonAsString() + " <b>" + award.getValue1() * 0.001d + "%</b>"
                        + (award.getMemo() == null || award.getMemo().isEmpty() ? "" : " - " + award.getMemo());
            }
            out += "<br>";
        }

        return out;

    }

}
