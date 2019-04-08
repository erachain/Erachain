package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.transaction.IssueStatusRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.library;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.erachain.gui.GUIConstants.*;

public class IssueStatusPanel extends JPanel {
    private JComboBox<Account> cbxFrom;
    private JComboBox<String> txtFeePow = new JComboBox<String>();
    private JTextField txtName = new JTextField();
    private JTextArea txtareaDescription = new JTextArea();
    private JButton issueButton;
    private JCheckBox jcheckUnique;
    private AddImageLabel addImageLabel;
    private AddImageLabel addLogoIconPanel;


    public IssueStatusPanel() {
        String colorText = "FF0000";
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 83, 0, 0, 0, 0};
        gridBagLayout.columnWidths = new int[]{-16, 0, 0, 0, 216, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0};
        setLayout(gridBagLayout);

        JLabel labelCaption = new JLabel();
        labelCaption.setFont(new Font("Tahoma", Font.PLAIN, 18));
        labelCaption.setHorizontalAlignment(SwingConstants.CENTER);
        labelCaption.setText(Lang.getInstance().translate("Create Status"));
        labelCaption.setHorizontalTextPosition(SwingConstants.CENTER);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.3;
        gridBagConstraints.insets = new Insets(8, 15, 8, 15);
        add(labelCaption, gridBagConstraints);

        addImageLabel = new AddImageLabel(
                Lang.getInstance().translate("Add image"),
                WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG);
        addImageLabel.setPreferredSize(new Dimension(WIDTH_IMAGE, HEIGHT_IMAGE));

        GridBagConstraints gbcAddImagePanel = new GridBagConstraints();
        gbcAddImagePanel.anchor = GridBagConstraints.NORTH;
        gbcAddImagePanel.fill = GridBagConstraints.HORIZONTAL;
        gbcAddImagePanel.gridheight = 3;
        gbcAddImagePanel.gridwidth = 2;
        gbcAddImagePanel.insets = new Insets(0, 0, 5, 5);
        gbcAddImagePanel.gridx = 0;
        gbcAddImagePanel.gridy = 1;
        add(addImageLabel, gbcAddImagePanel);

        JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        GridBagConstraints gbcFromLabel = new GridBagConstraints();
        gbcFromLabel.anchor = GridBagConstraints.WEST;
        gbcFromLabel.insets = new Insets(0, 0, 5, 5);
        gbcFromLabel.gridx = 2;
        gbcFromLabel.gridy = 1;
        add(fromLabel, gbcFromLabel);

        cbxFrom = new JComboBox<>(new AccountsComboBoxModel());
        GridBagConstraints gbcCbxFrom = new GridBagConstraints();
        gbcCbxFrom.gridwidth = 3;
        gbcCbxFrom.fill = GridBagConstraints.HORIZONTAL;
        gbcCbxFrom.insets = new Insets(0, 0, 5, 5);
        gbcCbxFrom.gridx = 4;
        gbcCbxFrom.gridy = 1;
        add(this.cbxFrom, gbcCbxFrom);

        JLabel nameLabel = new JLabel(
                "<HTML><p style=':#" + colorText + "'>" + Lang.getInstance().translate("Name") + ": </p></html>");
        GridBagConstraints gbcNameLabel = new GridBagConstraints();
        gbcNameLabel.anchor = GridBagConstraints.WEST;
        gbcNameLabel.insets = new Insets(0, 0, 5, 5);
        gbcNameLabel.gridx = 2;
        gbcNameLabel.gridy = 2;
        add(nameLabel, gbcNameLabel);

        GridBagConstraints gbcTxtName = new GridBagConstraints();
        gbcTxtName.gridwidth = 2;
        gbcTxtName.fill = GridBagConstraints.BOTH;
        gbcTxtName.insets = new Insets(0, 0, 5, 5);
        gbcTxtName.gridx = 4;
        gbcTxtName.gridy = 2;
        add(txtName, gbcTxtName);

        addLogoIconPanel = new AddImageLabel(Lang.getInstance().translate("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO, TypeOfImage.GIF);
        addLogoIconPanel.setPreferredSize(new Dimension(WIDTH_LOGO, HEIGHT_LOGO));

        GridBagConstraints gbcAddLogoIconPanel = new GridBagConstraints();
        gbcAddLogoIconPanel.anchor = GridBagConstraints.EAST;
        gbcAddLogoIconPanel.insets = new Insets(0, 0, 5, 5);
        gbcAddLogoIconPanel.gridx = 0;
        gbcAddLogoIconPanel.gridy = 4;
        add(addLogoIconPanel, gbcAddLogoIconPanel);

        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        GridBagConstraints gbcDescriptionLabel = new GridBagConstraints();
        gbcDescriptionLabel.anchor = GridBagConstraints.NORTHWEST;
        gbcDescriptionLabel.insets = new Insets(0, 0, 5, 5);
        gbcDescriptionLabel.gridx = 2;
        gbcDescriptionLabel.gridy = 3;
        add(descriptionLabel, gbcDescriptionLabel);

        JScrollPane scrollDescription = new JScrollPane();
        scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbcScrollDescription = new GridBagConstraints();
        gbcScrollDescription.gridwidth = 3;
        gbcScrollDescription.fill = GridBagConstraints.BOTH;
        gbcScrollDescription.insets = new Insets(0, 0, 5, 5);
        gbcScrollDescription.gridx = 4;
        gbcScrollDescription.gridy = 3;
        scrollDescription.setViewportView(txtareaDescription);
        add(scrollDescription, gbcScrollDescription);

        JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + " (0..6)" + ":");
        GridBagConstraints gbcFeeLabel = new GridBagConstraints();
        gbcFeeLabel.insets = new Insets(0, 0, 5, 5);
        gbcFeeLabel.gridx = 2;
        gbcFeeLabel.gridy = 4;
        add(feeLabel, gbcFeeLabel);

        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFeePow.setSelectedIndex(0);

        GridBagConstraints gbcTxtFeePow = new GridBagConstraints();
        gbcTxtFeePow.gridwidth = 3;
        gbcTxtFeePow.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtFeePow.insets = new Insets(0, 0, 5, 5);
        gbcTxtFeePow.gridx = 4;
        gbcTxtFeePow.gridy = 4;
        add(txtFeePow, gbcTxtFeePow);

        JLabel singleLabel = new JLabel(Lang.getInstance().translate("Single") + ":");
        GridBagConstraints gbcSingleLabel = new GridBagConstraints();
        gbcSingleLabel.insets = new Insets(0, 0, 5, 5);
        gbcSingleLabel.gridx = 2;
        gbcSingleLabel.gridy = 5;
        add(singleLabel, gbcSingleLabel);

        jcheckUnique = new JCheckBox();
        GridBagConstraints gbcJCheckUnique = new GridBagConstraints();
        gbcJCheckUnique.anchor = GridBagConstraints.WEST;
        gbcJCheckUnique.insets = new Insets(0, 0, 5, 5);
        gbcJCheckUnique.gridx = 4;
        gbcJCheckUnique.gridy = 5;
        add(jcheckUnique, gbcJCheckUnique);

        issueButton = new JButton(Lang.getInstance().translate("Issue"));
        issueButton.addActionListener(e -> onIssueClick());
        GridBagConstraints gbcIssueButton = new GridBagConstraints();
        gbcIssueButton.insets = new Insets(0, 0, 5, 5);
        gbcIssueButton.gridx = 6;
        gbcIssueButton.gridy = 6;
        add(issueButton, gbcIssueButton);

        setVisible(true);
    }

    public void onIssueClick() {
        // DISABLE
        issueButton.setEnabled(false);
        if (checkWalletUnlock()) {
            return;
        }
        // READ CREATOR
        Account sender = (Account) cbxFrom.getSelectedItem();

        int feePow;
        try {
            feePow = Integer.parseInt((String) this.txtFeePow.getSelectedItem());
        } catch (Exception e) {
            String mess = "Invalid fee power 0..6";
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            issueButton.setEnabled(true);
            return;
        }
        byte[] icon = addLogoIconPanel.getImgBytes();
        byte[] image = addImageLabel.getImgBytes();
        boolean unique = jcheckUnique.isSelected();
        PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
        IssueStatusRecord issueStatus = (IssueStatusRecord) Controller.getInstance().issueStatus(creator,
                txtName.getText(), txtareaDescription.getText(), unique, icon, image, feePow);
        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Create Status") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issueStatus.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + issueStatus.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>"
                + library.to_HTML(issueStatus.getItem().getDescription()) + "<br>";
        text += Lang.getInstance().translate("Unique") + ": " + ((StatusCls) issueStatus.getItem()).isUnique()
                + "<br>";
        String Status_text = "";
        IssueConfirmDialog issueConfirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, issueStatus,
                text,
                (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"));
        issueConfirmDialog.setLocationRelativeTo(this);
        issueConfirmDialog.setVisible(true);
        if (!issueConfirmDialog.isConfirm) {
            issueButton.setEnabled(true);
            return;
        }
        // VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issueStatus, Transaction.FOR_NETWORK);
        // CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Status issue has been sent") + "!",
                    Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            clearPanel();
        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }
        // ENABLE
        issueButton.setEnabled(true);
    }

    private boolean checkWalletUnlock() {
        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                        Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                issueButton.setEnabled(true);

                return true;
            }
        }
        return false;
    }

    private void clearPanel() {
        txtName.setText("");
        txtareaDescription.setText("");
        txtFeePow.setSelectedItem("0");
    }
}
