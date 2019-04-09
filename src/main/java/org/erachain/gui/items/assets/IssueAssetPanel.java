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
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.library;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

import static org.erachain.gui.items.utils.GUIConstants.*;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

/**
 * @author Саша
 */
public class IssueAssetPanel extends JPanel {

    private JLabel titleJLabel = new JLabel();
    private JLabel accountJLabel = new JLabel();
    private JLabel descriptionJLabel = new JLabel();
    private JLabel feeJLabel = new JLabel();
    private JLabel scaleJLabel = new JLabel();
    private JLabel nameJLabel = new JLabel();
    private JLabel quantityJLabel = new JLabel();
    private JLabel typeJLabel = new JLabel();

    private JComboBox<String> textFeePow = new JComboBox<>();
    private JComboBox<Account> fromJComboBox = new JComboBox<>(new AccountsComboBoxModel());
    private JComboBox<AssetType> assetTypeJComboBox = new JComboBox();
    private JComboBox<String> textScale = new JComboBox<>();
    private JButton issueJButton = new JButton();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JTextField textName = new JTextField();
    private JTextArea textAreaDescription = new JTextArea();
    // description asset type
    private JTextArea textareasAssetTypeDescription = new JTextArea();
    private MDecimalFormatedTextField textQuantity = new MDecimalFormatedTextField();

    private AddImageLabel addImageLabel;
    private AddImageLabel addLogoIconLabel;
    private AssetTypesComboBoxModel assetTypesComboBoxModel;

    public IssueAssetPanel() {
        initComponents();

        accountJLabel.setText(Lang.getInstance().translate("Account") + ":");

        nameJLabel.setText(Lang.getInstance().translate("Name") + ":");
        textName.setText("");
        titleJLabel.setFont(new Font("Tahoma", Font.PLAIN, 18)); 
        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setText(Lang.getInstance().translate("Issue Asset"));
        titleJLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        descriptionJLabel.setText(Lang.getInstance().translate("Description") + ":");
        typeJLabel.setText(Lang.getInstance().translate("Type") + ":");
        textAreaDescription.setLineWrap(true);
        textAreaDescription.setText("");
        quantityJLabel.setText(Lang.getInstance().translate("Quantity") + ":");

        textQuantity.setMaskType(textQuantity.maskLong);
        textQuantity.setText("1");
        scaleJLabel.setText(Lang.getInstance().translate("Scale") + ":");
        feeJLabel.setText(Lang.getInstance().translate("Fee Power") + ":");
        textFeePow.setSelectedItem("0");
        issueJButton.setText(Lang.getInstance().translate("Issue"));
        issueJButton.addActionListener(arg0 -> onIssueClick());
        // select combobox Asset type
        assetTypeJComboBox.addActionListener(e -> {
            JComboBox source = (JComboBox) e.getSource();
            AssetType assetType = (AssetType) source.getSelectedItem();
            textareasAssetTypeDescription.setText(assetType.getDescription());
        });

        // set start text area asset type
        textareasAssetTypeDescription.setText(((AssetType) assetTypesComboBoxModel.getSelectedItem()).getDescription());


    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;
        addImageLabel = new AddImageLabel(
                Lang.getInstance().translate("Add image"), WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG);
        addLogoIconLabel = new AddImageLabel(Lang.getInstance().translate("Add Logo"), WIDTH_LOGO, HEIGHT_LOGO, TypeOfImage.GIF);

        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 0, 6, 7);
        add(accountJLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 6, 8);
        add(fromJComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 0, 6, 7);
        add(nameJLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 6, 0);
        add(textName, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 12, 8, 8);

        addImageLabel.setPreferredSize(new Dimension(WIDTH_IMAGE, HEIGHT_IMAGE));
        add(addImageLabel, gridBagConstraints);


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 4;

        addLogoIconLabel.setPreferredSize(new Dimension(WIDTH_LOGO, HEIGHT_LOGO));
        add(addLogoIconLabel, gridBagConstraints);
        

        assetTypesComboBoxModel = new AssetTypesComboBoxModel();
        assetTypeJComboBox.setModel(assetTypesComboBoxModel);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.insets = new Insets(0, 0, 6, 8);
        add(assetTypeJComboBox, gridBagConstraints);

        textareasAssetTypeDescription.setLineWrap(true);
        textareasAssetTypeDescription.setWrapStyleWord(true);
        textareasAssetTypeDescription.setEditable(false);
        textareasAssetTypeDescription.setEnabled(false);
        textareasAssetTypeDescription.setColumns(20);
        textareasAssetTypeDescription.setRows(5);

        JScrollPane jScrollPane2 = new JScrollPane();
        jScrollPane2.setViewportView(textareasAssetTypeDescription);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weighty = 0.05;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.insets = new Insets(0, 0, 6, 8);
        add(jScrollPane2, gridBagConstraints);

        textAreaDescription.setColumns(20);
        textAreaDescription.setRows(5);
        jScrollPane1.setViewportView(textAreaDescription);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 6, 8);
        add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(8, 6, 6, 9);
        add(titleJLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 7, 7);
        add(descriptionJLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 7, 7);
        add(typeJLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 7, 7);
        add(quantityJLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 7, 10);
        add(textQuantity, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new Insets(0, 0, 7, 7);
        add(scaleJLabel, gridBagConstraints);

        textScale.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(24)));
        textScale.setSelectedIndex(8);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 7, 0);
        add(textScale, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new Insets(0, 0, 10, 0);
        add(feeJLabel, gridBagConstraints);


        textFeePow.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(9)));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 10, 10);
        add(textFeePow, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 10, 0);
        add(issueJButton, gridBagConstraints);
    }

    private String[] fillAndReceiveStringArray(int size) {
        String[] modelTextScale = new String[size];
        for (int i = 0; i < modelTextScale.length; i++) {
            modelTextScale[i] = i + "";
        }
        return modelTextScale;
    }

    public void onIssueClick() {
        // DISABLE
        issueJButton.setEnabled(false);
        if (checkWalletUnlock(issueJButton)){
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
            int asDeal = Transaction.FOR_NETWORK;

            // CREATE ASSET
            parsestep++;
            // SCALE, ASSET_TYPE, QUANTITY
            PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
            // if (currency_unmovabl_chk.isSelected()) asset_type = 1;
            // if (claim_right_obligation_chk.isSelected()) asset_type = 2;
            parsestep++;
            int assetType = ((AssetType) assetTypesComboBoxModel.getSelectedItem()).getId();

            IssueAssetTransaction issueAssetTransaction = (IssueAssetTransaction) Controller.getInstance().issueAsset(
                    creator, textName.getText(), textAreaDescription.getText(), addLogoIconLabel.getImgBytes(),
                    addImageLabel.getImgBytes(), false, scale, assetType, quantity, feePow);

            AssetCls asset = (AssetCls) issueAssetTransaction.getItem();

            String text = "<HTML><body>";
            text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                    + Lang.getInstance().translate("Issue Asset") + "<br><br><br>"
                    + Lang.getInstance().translate("Creator") + ":&nbsp;" + issueAssetTransaction.getCreator() + "<br>"
                    + "[" + asset.getKey() + "]" + Lang.getInstance().translate("Name") + ":&nbsp;" + asset.viewName() + "<br>"
                    + Lang.getInstance().translate("Quantity") + ":&nbsp;" + asset.getQuantity().toString() + "<br>"
                    + Lang.getInstance().translate("Asset Type") + ":&nbsp;"
                    + Lang.getInstance().translate(asset.viewAssetType() + "") + "<br>"
                    + Lang.getInstance().translate("Scale") + ":&nbsp;" + asset.getScale() + "<br>"
                    + Lang.getInstance().translate("Description") + ":<br>"
                    + library.to_HTML(Lang.getInstance().translate(asset.viewDescription())) + "<br>";
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
            int result = Controller.getInstance().getTransactionCreator().afterCreate(issueAssetTransaction, asDeal);

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
                case Transaction.INVALID_NAME_LENGTH:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Name must be between") + " "
                                    + issueAssetTransaction.getItem().getMinNameLen() + " "
                                    + Lang.getInstance().translate("and") + " "
                                    + ItemCls.MAX_NAME_LENGTH + " "
                                    + Lang.getInstance().translate("characters!"),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
                case Transaction.INVALID_DESCRIPTION_LENGTH:
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
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.getInstance().translate("Unknown error") + "[" + result + "]!",
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
