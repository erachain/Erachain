package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.exdata.exLink.ExLinkAddress;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.lang.Lang;

import javax.swing.*;

/**
 * @author Саша
 */
public class IssueAssetPanel extends IssueAssetPanelCls {

    public static String NAME = "IssueAssetPanel";
    public static String TITLE = "Issue Asset";

    protected final JLabel scaleJLabel = new JLabel(Lang.T("Scale") + ":");
    protected final JComboBox<String> textScale = new JComboBox<>();

    int scale;

    public IssueAssetPanel() {
        super(NAME, TITLE, null, null, true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE,
                true, true);

        textScale.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(24)));
        textScale.setSelectedIndex(8);

        assetTypesComboBoxModel = new AssetTypesComboBoxModel();
        assetTypeJComboBox.setModel(assetTypesComboBoxModel);
        //assetTypeJComboBox.setRenderer(new RenderComboBoxAssetActions());

        initComponents();

        // select combobox Asset type
        assetTypeJComboBox.addActionListener(e -> {
            JComboBox source = (JComboBox) e.getSource();
            refreshLabels((AssetType) source.getSelectedItem());
        });

        // set start text area asset type
        refreshLabels((AssetType) assetTypesComboBoxModel.getSelectedItem());

    }

    protected void initComponents() {

        super.initComponents();

        // вывод верхней панели
        int gridy = super.initTopArea(true);

        labelGBC.gridy = gridy;
        jPanelAdd.add(typeJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(assetTypeJComboBox, fieldGBC);

        textAreasAssetTypeDescription = new JTextPane();
        textAreasAssetTypeDescription.setEditable(false);
        textAreasAssetTypeDescription.setBackground(this.getBackground());
        textAreasAssetTypeDescription.setContentType("text/html");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = fieldGBC.gridx;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.gridwidth = fieldGBC.gridwidth;
        gridBagConstraints.anchor = fieldGBC.anchor;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelAdd.add(textAreasAssetTypeDescription, gridBagConstraints);

        ////
        labelGBC.gridy = ++gridy;
        jPanelAdd.add(quantityJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(textQuantity, fieldGBC);

        labelGBC.gridy = gridy;
        jPanelAdd.add(scaleJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(textScale, fieldGBC);

        // вывод подвала
        super.initBottom(gridy);
    }

    private void refreshLabels(AssetType assetType) {
        int fontSize = textScale.getFontMetrics(textScale.getFont()).getHeight();
        String fontStyle = textScale.getFont().getFontName();
        fontStyle = "<body style='font: " + (fontSize - 2) + "pt " + fontStyle + "'>";

        textAreasAssetTypeDescription.setText(fontStyle + assetType.getDescription());

        if (AssetCls.isTypeUnique(assetType.getId(), 0)) {
            textQuantity.setVisible(false);
            quantityJLabel.setVisible(false);
            textScale.setVisible(false);
            scaleJLabel.setVisible(false);
        } else {
            textQuantity.setVisible(!AssetCls.isAccounting(assetType.getId()));
            quantityJLabel.setVisible(!AssetCls.isAccounting(assetType.getId()));
            textScale.setVisible(true);
            scaleJLabel.setVisible(true);
        }

    }

    @Override
    protected boolean checkValues() {

        assetType = ((AssetType) assetTypesComboBoxModel.getSelectedItem()).getId();

        int parseStep = 0;
        try {

            if (!AssetCls.isTypeUnique(assetType, quantity)) {
                // READ SCALE
                scale = Byte.parseByte((String) textScale.getSelectedItem());

                // READ QUANTITY
                parseStep++;
                quantity = Long.parseLong(textQuantity.getText());
            }

        } catch (Exception e) {
            switch (parseStep) {
                case 0:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.T("Invalid Scale!"), Lang.T("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
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

        AssetCls asset;
        if (AssetCls.isTypeUnique(assetType, quantity)) {
            asset = new AssetUnique(itemAppData, creator, nameField.getText(), addIconLabel.getMediaBytes(),
                    addImageLabel.getMediaBytes(), textAreaDescription.getText(),
                    assetType);
        } else {
            asset = new AssetVenture(itemAppData, creator, nameField.getText(), addIconLabel.getMediaBytes(),
                    addImageLabel.getMediaBytes(), textAreaDescription.getText(),
                    assetType, scale, quantity);
        }
        transaction = Controller.getInstance().issueAsset(
                creator, exLink, feePow, asset);

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
