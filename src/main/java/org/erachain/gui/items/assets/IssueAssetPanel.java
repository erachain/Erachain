package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.IconPanel;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;

import static org.erachain.gui.items.utils.GUIConstants.*;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

/**
 * @author Саша
 */
public class IssueAssetPanel extends IconPanel {

    public static String NAME = "IssueAssetPanel";
    public static String TITLE = "Issue Asset";

    private JLabel titleJLabel = new JLabel();
    private JLabel accountJLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
    private JLabel descriptionJLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
    private JLabel feeJLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
    private JLabel scaleJLabel = new JLabel(Lang.getInstance().translate("Scale") + ":");
    private JLabel nameJLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
    private JLabel quantityJLabel = new JLabel(Lang.getInstance().translate("Quantity") + ":");
    private JLabel typeJLabel = new JLabel(Lang.getInstance().translate("Type") + ":");

    private JComboBox<String> textFeePow = new JComboBox<>();
    private JComboBox<Account> fromJComboBox = new JComboBox<>(new AccountsComboBoxModel());
    private JComboBox<AssetType> assetTypeJComboBox = new JComboBox();
    private JComboBox<String> textScale = new JComboBox<>();
    private JButton issueJButton = new JButton(Lang.getInstance().translate("Issue"));
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JTextField textName = new JTextField("");
    private JTextArea textAreaDescription = new JTextArea("");
    // description asset type
    private JTextArea textareasAssetTypeDescription = new JTextArea();
    private MDecimalFormatedTextField textQuantity = new MDecimalFormatedTextField();

    private AddImageLabel addImageLabel;
    private AddImageLabel addLogoIconLabel;
    private AssetTypesComboBoxModel assetTypesComboBoxModel;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3 = new JScrollPane() ;
    private javax.swing.JTextField jTextFieldReference = new JTextField();
    private javax.swing.JLabel jLabelReference = new JLabel(Lang.getInstance().translate("Reference") + ":");
    private javax.swing.JTextField jTextFieldItem1 = new JTextField();
    private javax.swing.JLabel jLabelItem1 = new JLabel(Lang.getInstance().translate("item1") + ":");

    public IssueAssetPanel() {
        super(NAME, TITLE);
// init
        jScrollPane2 = new JScrollPane();
        addImageLabel = new AddImageLabel(
                Lang.getInstance().translate("Add image"), WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG,
                0, ItemCls.MAX_IMAGE_LENGTH, WIDTH_IMAGE_INITIAL, HEIGHT_IMAGE_INITIAL);
        addImageLabel.setBorder(null);
        addLogoIconLabel = new AddImageLabel(Lang.getInstance().translate("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO, TypeOfImage.GIF,
                0, ItemCls.MAX_ICON_LENGTH, WIDTH_LOGO_INITIAL, HEIGHT_LOGO_INITIAL);
        addLogoIconLabel.setBorder(null);
        assetTypesComboBoxModel = new AssetTypesComboBoxModel();
        assetTypeJComboBox.setModel(assetTypesComboBoxModel);
        textScale.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(24)));
        textScale.setSelectedIndex(8);
//
        initComponents();
        titleJLabel.setFont(FONT_TITLE);
        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleJLabel.setText(Lang.getInstance().translate(TITLE));
        textAreaDescription.setLineWrap(true);
        textQuantity.setMaskType(textQuantity.maskLong);
        textQuantity.setText("0");
        textFeePow.setModel(new DefaultComboBoxModel<>(fillAndReceiveStringArray(9)));
        textFeePow.setSelectedItem("0");

        feeJLabel.setVisible(Gui.SHOW_FEE_POWER);
        textFeePow.setVisible(Gui.SHOW_FEE_POWER);

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


    private void initComponents(){
        GridBagConstraints gridBagConstraints;
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(addLogoIconLabel, gridBagConstraints);



        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(accountJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        add(fromJComboBox, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        add(textName, gridBagConstraints);


        nameJLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(nameJLabel, gridBagConstraints);


        typeJLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(typeJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        add(assetTypeJComboBox, gridBagConstraints);


        jScrollPane3.setBorder(null);
        jScrollPane3.setViewportView(addImageLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.gridheight = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.1;
        add(jScrollPane3, gridBagConstraints);


        textareasAssetTypeDescription.setEditable(false);
        textareasAssetTypeDescription.setColumns(20);
        textareasAssetTypeDescription.setRows(5);
        textareasAssetTypeDescription.setBorder(null);
        textareasAssetTypeDescription.setDoubleBuffered(true);
        textareasAssetTypeDescription.setDragEnabled(true);
        jScrollPane1.setViewportView(textareasAssetTypeDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        add(jScrollPane1, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(quantityJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(scaleJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        add(textQuantity, gridBagConstraints);

        textAreaDescription.setColumns(20);
        textAreaDescription.setRows(5);
        jScrollPane2.setViewportView(textAreaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 8);
        add(jScrollPane2, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(descriptionJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 26;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        add(issueJButton, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 27;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 0, 7);
        add(titleJLabel, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 24;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 7);
        add(textScale, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(jLabelItem1, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        add(jTextFieldItem1, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(jLabelReference, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        add(jTextFieldReference, gridBagConstraints);
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
                    creator, textName.getText(), textAreaDescription.getText(), addLogoIconLabel.getImgBytes(),
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
