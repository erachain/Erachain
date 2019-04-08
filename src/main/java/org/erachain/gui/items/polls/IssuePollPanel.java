package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.library;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.models.CreateOptionsTableModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.erachain.gui.GUIConstants.*;

public class IssuePollPanel extends JPanel {
    private JComboBox<Account> cbxFrom;
    private JComboBox<String> txtFee = new JComboBox<>();
    private JTextField txtName = new JTextField();
    private JTextArea txtareaDescription = new JTextArea();
    private JButton createButton;
    private CreateOptionsTableModel optionsTableModel;
    private AddImageLabel addImageLabel;
    private AddImageLabel addLogoIconLabel;
    private final MTable table;

    public IssuePollPanel() {
        // LAYOUT
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowHeights = new int[]{0, 0, 88, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
        setLayout(gridBagLayout);

        // TEXTFIELD GBC
        GridBagConstraints txtGBC = new GridBagConstraints();
        txtGBC.insets = new Insets(5, 5, 5, 0);
        txtGBC.fill = GridBagConstraints.HORIZONTAL;
        txtGBC.anchor = GridBagConstraints.NORTHWEST;
        txtGBC.weightx = 1;
        txtGBC.gridwidth = 2;
        txtGBC.gridx = 2;
        txtGBC.gridy = 0;
        cbxFrom = new JComboBox<>(new AccountsComboBoxModel());
        add(cbxFrom, txtGBC);

        // BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 5, 0, 5);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridwidth = 2;
        buttonGBC.gridx = 0;
        buttonGBC.gridy = 6;
        // BUTTON Register
        createButton = new JButton(Lang.getInstance().translate("Create"));
        createButton.addActionListener(e -> onRegisterClick());
        add(createButton, buttonGBC);

        JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        GridBagConstraints gbcFromLabel = new GridBagConstraints();
        gbcFromLabel.insets = new Insets(0, 0, 5, 5);
        gbcFromLabel.gridx = 1;
        gbcFromLabel.gridy = 0;
        add(fromLabel, gbcFromLabel);


        optionsTableModel = new CreateOptionsTableModel(new Object[]{Lang.getInstance().translate("Name")}, 0);

        // LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(5, 5, 5, 5);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 0;
        labelGBC.weighty = 0;
        labelGBC.gridx = 1;
        labelGBC.gridy = 1;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        add(nameLabel, labelGBC);

        // ICON
        addImageLabel = new AddImageLabel(
                Lang.getInstance().translate("Add image"), WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG);

        GridBagConstraints gbcAddImagePanel = new GridBagConstraints();
        gbcAddImagePanel.anchor = GridBagConstraints.NORTH;
        gbcAddImagePanel.gridheight = 3;
        gbcAddImagePanel.insets = new Insets(0, 0, 5, 5);
        gbcAddImagePanel.gridx = 0;
        gbcAddImagePanel.gridy = 1;
        addImageLabel.setPreferredSize(new Dimension(WIDTH_IMAGE, HEIGHT_IMAGE));

        add(addImageLabel, gbcAddImagePanel);

        GridBagConstraints gbcTxtName = new GridBagConstraints();
        gbcTxtName.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtName.insets = new Insets(0, 0, 5, 5);
        gbcTxtName.gridx = 2;
        gbcTxtName.gridy = 1;
        add(txtName, gbcTxtName);

        addLogoIconLabel = new AddImageLabel(Lang.getInstance().translate("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO, TypeOfImage.GIF);
        addLogoIconLabel.setPreferredSize(new Dimension(WIDTH_LOGO, HEIGHT_LOGO));

        GridBagConstraints gbcAddLogoIconPanel = new GridBagConstraints();
        gbcAddLogoIconPanel.insets = new Insets(0, 0, 5, 0);
        gbcAddImagePanel.gridheight = 1;
        gbcAddLogoIconPanel.gridx = 0;
        gbcAddLogoIconPanel.gridy = 5;
        add(addLogoIconLabel, gbcAddLogoIconPanel);

        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        GridBagConstraints gbcDescriptionLabel = new GridBagConstraints();
        gbcDescriptionLabel.anchor = GridBagConstraints.NORTH;
        gbcDescriptionLabel.insets = new Insets(0, 0, 5, 5);
        gbcDescriptionLabel.gridx = 1;
        gbcDescriptionLabel.gridy = 2;
        this.add(descriptionLabel, gbcDescriptionLabel);

        // TXTAREA NAME
        GridBagConstraints gbcDescription = new GridBagConstraints();
        gbcDescription.gridwidth = 2;
        gbcDescription.gridx = 2;
        gbcDescription.gridy = 2;
        gbcDescription.weighty = 0.3;
        gbcDescription.insets = new Insets(0, 5, 5, 0);
        gbcDescription.fill = GridBagConstraints.BOTH;
        gbcDescription.anchor = GridBagConstraints.NORTHWEST;
        JScrollPane scrollPaneDescription = new JScrollPane();
        scrollPaneDescription.setViewportView(txtareaDescription);
        add(scrollPaneDescription, gbcDescription);



        JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
        GridBagConstraints gbcOptionsLabel = new GridBagConstraints();
        gbcOptionsLabel.anchor = GridBagConstraints.NORTH;
        gbcOptionsLabel.insets = new Insets(0, 0, 5, 5);
        gbcOptionsLabel.gridx = 1;
        gbcOptionsLabel.gridy = 3;
        add(optionsLabel, gbcOptionsLabel);

        // TABLE OPTIONS
        GridBagConstraints gbcOptionalTable = new GridBagConstraints();
        gbcOptionalTable.gridwidth = 2;
        gbcOptionalTable.insets = new Insets(0, 0, 5, 0);
        gbcOptionalTable.gridx = 2;
        gbcOptionalTable.gridy = 3;
        gbcOptionalTable.weighty = 0.1;
        gbcOptionalTable.fill = GridBagConstraints.BOTH;
        gbcOptionalTable.anchor = GridBagConstraints.NORTHWEST;
        table = new MTable(optionsTableModel);
        JScrollPane scrollPaneOptionalTable = new JScrollPane();
        scrollPaneOptionalTable.setViewportView(table);
        add(scrollPaneOptionalTable, gbcOptionalTable);

        JButton deleteButton = new JButton(Lang.getInstance().translate("Delete"));
        deleteButton.addActionListener(e -> deleteRow());
        GridBagConstraints gbcDeleteButton = new GridBagConstraints();
        gbcDeleteButton.gridwidth = 2;
        gbcDeleteButton.fill = GridBagConstraints.HORIZONTAL;
        gbcDeleteButton.insets = new Insets(0, 0, 5, 5);
        gbcDeleteButton.gridx = 2;
        gbcDeleteButton.gridy = 4;
        add(deleteButton, gbcDeleteButton);

        JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee") + ":");
        GridBagConstraints gbcFeeLabel = new GridBagConstraints();
        gbcFeeLabel.insets = new Insets(0, 0, 5, 5);
        gbcFeeLabel.gridx = 1;
        gbcFeeLabel.gridy = 5;
        add(feeLabel, gbcFeeLabel);

        txtFee.setModel(new DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFee.setSelectedIndex(0);

        GridBagConstraints gbcTxtFee = new GridBagConstraints();
        gbcTxtFee.gridwidth = 2;
        gbcTxtFee.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtFee.insets = new Insets(0, 0, 5, 5);
        gbcTxtFee.gridx = 2;
        gbcTxtFee.gridy = 5;
        add(txtFee, gbcTxtFee);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Delete"));
        copyAddress.addActionListener(e -> deleteRow());
        menu.add(copyAddress);
        TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
        setVisible(true);
    }

    public void onRegisterClick() {
        // DISABLE
        createButton.setEnabled(false);
        if (checkWalletUnlock()){
            return;
        }

        // READ CREATOR
        Account sender = (Account) cbxFrom.getSelectedItem();
        int feePow;
        try {
            // READ FEE POWER
            feePow = Integer.parseInt((String) txtFee.getSelectedItem());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee Power!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            createButton.setEnabled(true);
            return;
        }

        if (optionsTableModel.getRowCount() < 1) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            createButton.setEnabled(true);
            return;
        }
        if (optionsTableModel.getRowCount() == 1 && optionsTableModel.getValueAt(0, 0).equals("")) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Null Options!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            // ENABLE
            createButton.setEnabled(true);
            return;
        }
        byte[] icon = addLogoIconLabel.getImgBytes();
        byte[] image = addImageLabel.getImgBytes();

        // CREATE POLL
        PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
        IssuePollRecord issuePoll = (IssuePollRecord) Controller.getInstance().issuePoll(creator,
                txtName.getText(), txtareaDescription.getText(),
                optionsTableModel.getOptions(), icon, image, feePow);

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Issue Voting") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issuePoll.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + this.txtName.getText() + "<br>";
        text += "<br>" + Lang.getInstance().translate("Description") + ":<br>"
                + library.to_HTML(this.txtareaDescription.getText()) + "<br>";
        text += "<br>" + Lang.getInstance().translate("Options") + ":<br>";

        List<String> options = optionsTableModel.getOptions();

        for (int i = 0; i < options.size(); i++) {
            text += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + options.get(i);

        }
        text += "<br>    ";
        String statusText = "";
        IssueConfirmDialog issueConfirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, issuePoll,
                text, (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), statusText,
                Lang.getInstance().translate("Confirmation Transaction"));
        issueConfirmDialog.setLocationRelativeTo(this);
        issueConfirmDialog.setVisible(true);

        // JOptionPane.OK_OPTION
        if (!issueConfirmDialog.isConfirm) {
            createButton.setEnabled(true);
            return;
        }

        // VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issuePoll, Transaction.FOR_NETWORK);
        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Poll issue has been sent") + "!",
                    Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        // ENABLE
        createButton.setEnabled(true);
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
                createButton.setEnabled(true);
                return true;
            }
        }
        return false;
    }

    private void deleteRow() {
        if (optionsTableModel.getRowCount() > 1) {
            int selRow = table.getSelectedRow();
            if (selRow != -1) {
                optionsTableModel.removeRow(selRow);
            }
        }
    }
}
