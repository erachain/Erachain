package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.exLink.ExLinkAddress;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.gui.models.FavoriteComboBoxModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Саша
 */
public class IssueAssetCopyPanel extends IssueAssetPanelCls {

    public static String NAME = "IssueAssetCopyPanel";
    public static String TITLE = "Issue Series";

    public JCheckBox hasOriginal = new JCheckBox(Lang.T("Use Original Asset") + ":");
    byte[] origAssetTXSign;
    public javax.swing.JComboBox<ItemCls> jComboBox_Asset;


    public IssueAssetCopyPanel() {
        super(NAME, TITLE, "IssueAssetCopyPanel.titleDescription", null,
                true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE,
                true, true);

        quantityJLabel.setText(Lang.T("Series Size") + ":");
        // всегда NFT - для правильного выбора Роялти по умолчанию
        assetTypeJComboBox.setSelectedItem(new AssetType(AssetCls.AS_NON_FUNGIBLE));

        initComponents();

    }

    protected void initComponents() {

        super.initComponents();
        addImageLabel.label.setText(Lang.T("Add a frame"));

        int gridy = initTopArea(true);

        // favorite combo box
        jComboBox_Asset = new javax.swing.JComboBox<>();
        jComboBox_Asset.setModel(new ComboBoxAssetsNFTModel());
        jComboBox_Asset.setRenderer(new FavoriteComboBoxModel.IconListRenderer());
        jComboBox_Asset.setEditable(false);
        //this.jComboBox_Asset.setEnabled(assetIn != null);

        labelGBC.gridy = gridy;
        jPanelAdd.add(hasOriginal, labelGBC);

        fieldGBC.gridy = gridy;
        jComboBox_Asset.setEnabled(false);
        jPanelAdd.add(jComboBox_Asset, fieldGBC);

        JLabel hasOriginalTip = new JLabel(Lang.T("hasOriginalTip"));
        hasOriginalTip.setVisible(false);
        fieldGBC.gridy = ++gridy;
        jPanelAdd.add(hasOriginalTip, fieldGBC);

        labelGBC.gridy = ++gridy;
        jPanelAdd.add(quantityJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        textQuantity.setText("10");
        jPanelAdd.add(textQuantity, fieldGBC);

        hasOriginal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jComboBox_Asset.setEnabled(hasOriginal.isSelected());
                hasOriginalTip.setVisible(hasOriginal.isSelected());
            }
        });

        // вывод подвала
        super.initBottom(gridy);
    }

    protected boolean checkValues() {

        assetType = AssetCls.AS_NON_FUNGIBLE;

        if (hasOriginal.isSelected()) {
            AssetCls asset = (AssetCls) this.jComboBox_Asset.getSelectedItem();
            if (asset == null) {
                JOptionPane.showMessageDialog(MainFrame.getInstance(),
                        Lang.T("Empty Original! Add NTF asset to Favorites first"), Lang.T("Error"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            origAssetTXSign = asset.getReference();
        } else {
            origAssetTXSign = null;
        }

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
