package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.gui.library.MultipleRoyaltyPanel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

/**
 * @author Саша
 */
public class IssueAssetPanel extends IssueItemPanel {

    public static String NAME = "IssueAssetPanel";
    public static String TITLE = "Issue Asset";

    private JLabel scaleJLabel = new JLabel(Lang.T("Scale") + ":");
    private JLabel quantityJLabel = new JLabel(Lang.T("Quantity") + ":");
    private JLabel typeJLabel = new JLabel(Lang.T("Type") + ":");

    private JComboBox<AssetType> assetTypeJComboBox = new JComboBox();
    private JComboBox<String> textScale = new JComboBox<>();

    private JTextPane textareasAssetTypeDescription;
    private MDecimalFormatedTextField textQuantity = new MDecimalFormatedTextField();

    private AssetTypesComboBoxModel assetTypesComboBoxModel;

    private MultipleRoyaltyPanel multipleRoyaltyPanel = new MultipleRoyaltyPanel(fromJComboBox);

    public IssueAssetPanel() {
        super(NAME, TITLE, "Asset issue has been sent!", true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE, true, true);

        assetTypesComboBoxModel = new AssetTypesComboBoxModel();
        assetTypeJComboBox.setModel(assetTypesComboBoxModel);
        //assetTypeJComboBox.setRenderer(new RenderComboBoxAssetActions());

        textScale.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(24)));
        textScale.setSelectedIndex(8);

        initComponents();

        textQuantity.setText("0");

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
        int gridy = super.initTopArea();

        labelGBC.gridy = gridy;
        jPanelAdd.add(typeJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(assetTypeJComboBox, fieldGBC);

        textareasAssetTypeDescription = new JTextPane();
        textareasAssetTypeDescription.setEditable(false);
        textareasAssetTypeDescription.setBackground(this.getBackground());
        textareasAssetTypeDescription.setContentType("text/html");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = fieldGBC.gridx;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.gridwidth = fieldGBC.gridwidth;
        gridBagConstraints.anchor = fieldGBC.anchor;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = fieldGBC.insets;
        jPanelAdd.add(textareasAssetTypeDescription, gridBagConstraints);

        ////
        labelGBC.gridy = ++gridy;
        jPanelAdd.add(quantityJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(textQuantity, fieldGBC);

        labelGBC.gridy = gridy;
        jPanelAdd.add(scaleJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(textScale, fieldGBC);

        fieldGBC.gridy = gridy++;
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = fieldGBC.gridy;
        gridBagConstraints.gridwidth = 9;
        //gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelAdd.add(multipleRoyaltyPanel, gridBagConstraints);


        // вывод подвала
        super.initBottom(gridy);
    }

    int scale;
    long quantity;
    int assetType;

    private void refreshLabels(AssetType assetType) {
        int fontSize = textScale.getFontMetrics(textScale.getFont()).getHeight();
        String fontStyle = textScale.getFont().getFontName();
        fontStyle = "<body style='font: " + (fontSize - 2) + "pt " + fontStyle + "'>";

        textareasAssetTypeDescription.setText(fontStyle + assetType.getDescription());

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

        multipleRoyaltyPanel.setVisible(false && assetType.getId() == AssetCls.AS_NON_FUNGIBLE);

    }

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
    protected void makeAppData() {
        itemAppData = AssetCls.makeAppData(!addIconLabel.isInternalMedia(), addIconLabel.getMediaType(),
                !addImageLabel.isInternalMedia(), addImageLabel.getMediaType(),
                multipleRoyaltyPanel.recipientsTableModel.getRecipients());

    }

    protected void makeTransaction() {

        AssetCls asset;
        if (AssetCls.isTypeUnique(assetType, quantity)) {
            asset = new AssetUnique(itemAppData, creator, textName.getText(), addIconLabel.getMediaBytes(),
                    addImageLabel.getMediaBytes(), textAreaDescription.getText(),
                    assetType);
        } else {
            asset = new AssetVenture(itemAppData, creator, textName.getText(), addIconLabel.getMediaBytes(),
                    addImageLabel.getMediaBytes(), textAreaDescription.getText(),
                    assetType, scale, quantity);
        }
        transaction = (IssueAssetTransaction) Controller.getInstance().issueAsset(
                creator, exLink, feePow, asset);

    }

    protected String makeTransactionView() {

        AssetCls asset = (AssetCls) transaction.getItem();

        String text = "<body><h2>";
        text += Lang.T("Confirmation Transaction") + ":&nbsp;"
                + Lang.T("Issue Asset") + "</h2>"
                + makeHeadView("Name")
                + Lang.T("Asset Class") + ":&nbsp;"
                + Lang.T(asset.getItemSubType() + "") + "<br>"
                + Lang.T("Asset Type") + ":&nbsp;"
                + "<b>" + asset.charAssetType() + asset.viewAssetTypeAbbrev() + "</b>:" + Lang.T(asset.viewAssetTypeFull() + "") + "<br>"
                + Lang.T("Quantity") + ":&nbsp;" + asset.getQuantity() + ", "
                + Lang.T("Scale") + ":&nbsp;" + asset.getScale() + "<br>"
                + Lang.T("Description") + ":<br>";
        if (asset.getKey() > 0 && asset.getKey() < 1000) {
            text += Library.to_HTML(Lang.T(asset.viewDescription())) + "<br>";
        } else {
            text += Library.to_HTML(asset.viewDescription()) + "<br>";
        }

        return text;

    }

}
