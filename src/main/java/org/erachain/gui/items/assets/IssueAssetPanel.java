package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import javax.swing.*;

/**
 * @author Саша
 */
public class IssueAssetPanel extends IssueItemPanel {

    public static String NAME = "IssueAssetPanel";
    public static String TITLE = "Issue Asset";

    private JLabel scaleJLabel = new JLabel(Lang.getInstance().translate("Scale") + ":");
    private JLabel quantityJLabel = new JLabel(Lang.getInstance().translate("Quantity") + ":");
    private JLabel typeJLabel = new JLabel(Lang.getInstance().translate("Type") + ":");

    private JComboBox<AssetType> assetTypeJComboBox = new JComboBox();
    private JComboBox<String> textScale = new JComboBox<>();

    private JTextArea textareasAssetTypeDescription = new JTextArea();
    private MDecimalFormatedTextField textQuantity = new MDecimalFormatedTextField();

    private AssetTypesComboBoxModel assetTypesComboBoxModel;


    public IssueAssetPanel() {
        super(NAME, TITLE, "Asset issue has been sent!");

        assetTypesComboBoxModel = new AssetTypesComboBoxModel();
        assetTypeJComboBox.setModel(assetTypesComboBoxModel);
        textScale.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(24)));
        textScale.setSelectedIndex(8);

        initComponents();
        textQuantity.setMaskType(textQuantity.maskLong);
        textQuantity.setText("0");

        // select combobox Asset type
        assetTypeJComboBox.addActionListener(e -> {
            JComboBox source = (JComboBox) e.getSource();
            AssetType assetType = (AssetType) source.getSelectedItem();
            textareasAssetTypeDescription.setText(assetType.getDescription());
        });

        // set start text area asset type
        textareasAssetTypeDescription.setText(((AssetType) assetTypesComboBoxModel.getSelectedItem()).getDescription());

    }

    protected void initComponents() {
        super.initComponents();

        // вывод верхней панели
        int gridy = super.initTopArea();

        labelGBC.gridy = gridy;
        jPanelAdd.add(typeJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(assetTypeJComboBox, fieldGBC);

        JScrollPane scrollPaneAssetTypeDescription = new JScrollPane();

        textareasAssetTypeDescription.setColumns(20);
        textareasAssetTypeDescription.setRows(5);
        textareasAssetTypeDescription.setLineWrap(true);
        scrollPaneAssetTypeDescription.setViewportView(textareasAssetTypeDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelAdd.add(textareasAssetTypeDescription, gridBagConstraints);

        ////
        labelGBC.gridy = gridy;
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

    int scale;
    long quantity;
    int assetType;

    protected boolean checkValues() {

        int parseStep = 0;
        try {

            // READ SCALE
            scale = Byte.parseByte((String) textScale.getSelectedItem());

            // READ QUANTITY
            parseStep++;
            quantity = Long.parseLong(textQuantity.getText());

        } catch (Exception e) {
            switch (parseStep) {
                case 0:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Invalid Scale!"), Lang.getInstance().translate("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case 1:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
            }
            return false;
        }

        assetType = ((AssetType) assetTypesComboBoxModel.getSelectedItem()).getId();

        return true;
    }

    protected void makeTransaction() {

        transaction = (IssueAssetTransaction) Controller.getInstance().issueAsset(
                creator, exLink, textName.getText(), textAreaDescription.getText(),
                addLogoIconLabel.getImgBytes(), addImageLabel.getImgBytes(),
                scale, assetType, quantity, feePow);

        // TEST
        JSONObject json = transaction.copy().toJson();

    }

    protected String makeTransactionView() {

        AssetCls asset = (AssetCls) transaction.getItem();

        String text = "<HTML><body><h2>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Issue Asset") + "</h2>"
                + Lang.getInstance().translate("Creator") + ":&nbsp;<b>" + transaction.getCreator() + "</b><br>"
                + (exLink == null ? "" : Lang.getInstance().translate("Append to") + ":&nbsp;<b>" + exLink.viewRef() + "</b><br>")
                + "[" + asset.getKey() + "]" + Lang.getInstance().translate("Name") + ":&nbsp;" + asset.viewName() + "<br>"
                + Lang.getInstance().translate("Quantity") + ":&nbsp;" + asset.getQuantity() + "<br>"
                + Lang.getInstance().translate("Asset Type") + ":&nbsp;"
                + Lang.getInstance().translate(asset.viewAssetTypeFull() + "") + "<br>"
                + Lang.getInstance().translate("Scale") + ":&nbsp;" + asset.getScale() + "<br>"
                + Lang.getInstance().translate("Description") + ":<br>";
        if (asset.getKey() > 0 && asset.getKey() < 1000) {
            text += Library.to_HTML(Lang.getInstance().translate(asset.viewDescription())) + "<br>";
        } else {
            text += Library.to_HTML(asset.viewDescription()) + "<br>";
        }

        return text;

    }

}
