package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

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
        super(NAME, TITLE);
// init
        assetTypesComboBoxModel = new AssetTypesComboBoxModel();
        assetTypeJComboBox.setModel(assetTypesComboBoxModel);
        textScale.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(24)));
        textScale.setSelectedIndex(8);
//

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

        // insert asset issue info
        // grid x - 4...26
        // y -  3 ....29
        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(typeJLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(assetTypeJComboBox, gridBagConstraints);

        JScrollPane scrollPaneAssetTypeDescription = new JScrollPane();

        textareasAssetTypeDescription.setColumns(20);
        textareasAssetTypeDescription.setRows(5);
        textareasAssetTypeDescription.setLineWrap(true);
        scrollPaneAssetTypeDescription.setViewportView(textareasAssetTypeDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(textareasAssetTypeDescription, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(quantityJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jPanelMain.add(textQuantity, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 17;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelMain.add(scaleJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 8);
        jPanelMain.add(textScale, gridBagConstraints);


    }

    public void onIssueClick() {
        // DISABLE
        issueJButton.setEnabled(false);
        if (checkWalletUnlock(issueJButton)) {
            return;
        }

        // READ CREATOR
        Account sender = (Account) fromJComboBox.getSelectedItem();

        int parsestep = 0;
        try {

            // READ FEE POW
            int feePow = Integer.parseInt((String) textFeePow.getSelectedItem());

            // READ SCALE
            parsestep++;
            byte scale = Byte.parseByte((String) textScale.getSelectedItem());


            // READ QUANTITY
            parsestep++;
            long quantity = Long.parseLong(textQuantity.getText());
            int forDeal = Transaction.FOR_NETWORK;

            // CREATE ASSET
            parsestep++;
            // SCALE, ASSET_TYPE, QUANTITY
            PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
            if (creator == null) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }


            // if (currency_unmovabl_chk.isSelected()) assetType = 1;
            // if (claim_right_obligation_chk.isSelected()) assetType = 2;
            parsestep++;
            int assetType = ((AssetType) assetTypesComboBoxModel.getSelectedItem()).getId();

            IssueAssetTransaction issueAssetTransaction = (IssueAssetTransaction) Controller.getInstance().issueAsset(
                    creator, null, textName.getText(), textAreaDescription.getText(), addLogoIconLabel.getImgBytes(),
                    addImageLabel.getImgBytes(), scale, assetType, quantity, feePow);

            AssetCls asset = (AssetCls) issueAssetTransaction.getItem();

            String text = "<HTML><body>";
            text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                    + Lang.getInstance().translate("Issue Asset") + "<br><br><br>"
                    + Lang.getInstance().translate("Creator") + ":&nbsp;" + issueAssetTransaction.getCreator() + "<br>"
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
            String statusText = "";

            IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(),
                    true, issueAssetTransaction,
                    text, (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), statusText,
                    Lang.getInstance().translate("Confirmation Transaction"));
            confirmDialog.setLocationRelativeTo(this);
            confirmDialog.setVisible(true);

            // JOptionPane.OK_OPTION
            if (!confirmDialog.isConfirm) {
                issueJButton.setEnabled(true);
                return;
            }

            // VALIDATE AND PROCESS
            int result = Controller.getInstance().getTransactionCreator().afterCreate(issueAssetTransaction, forDeal);

            // CHECK VALIDATE MESSAGE
            switch (result) {
                case Transaction.VALIDATE_OK:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Asset issue has been sent!"),
                            Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                    break;
                case Transaction.INVALID_QUANTITY:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case Transaction.NOT_ENOUGH_FEE:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Not enough %fee% balance!").replace("%fee%",
                                    AssetCls.FEE_NAME),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
                case Transaction.INVALID_NAME_LENGTH_MIN:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Name must be more then %val characters!")
                                    .replace("%val", "" + issueAssetTransaction.getItem().getMinNameLen()),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
                case Transaction.INVALID_NAME_LENGTH_MAX:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Name must be less then %val characters!")
                                    .replace("%val", "" + ItemCls.MAX_NAME_LENGTH),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
                case Transaction.INVALID_DESCRIPTION_LENGTH_MAX:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Description must be between 1 and 1000 characters!"),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
                case Transaction.INVALID_PAYMENTS_LENGTH:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case Transaction.CREATOR_NOT_PERSONALIZED:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Issuer account not personalized!"),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.getInstance().translate(OnDealClick.resultMess(result)),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
            }
        } catch (Exception e) {
            switch (parsestep) {
                case 0:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Invalid Fee Power!"), Lang.getInstance().translate("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case 1:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Invalid Scale!"), Lang.getInstance().translate("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case 2:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case 4:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Invalid Type!"), Lang.getInstance().translate("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case 3:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Invalid Asset!"), Lang.getInstance().translate("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
            }
        }

        // ENABLE
        issueJButton.setEnabled(true);
    }

}
