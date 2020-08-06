package org.erachain.gui.items.polls;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.IssuePollRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.IconPanel;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.models.CreateOptionsTableModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.erachain.gui.items.utils.GUIConstants.*;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

public class IssuePollPanel extends IconPanel {

    private JComboBox<Account> cbxFrom;
    private JComboBox<String> txtFee = new JComboBox<>();
    private JTextField txtName = new JTextField();
    private JTextArea txtareaDescription = new JTextArea();
    private JButton createButton;
    private CreateOptionsTableModel optionsTableModel;
    private AddImageLabel addImageLabel;
    private AddImageLabel addLogoIconLabel;
    private final MTable table;
    private JLabel titleJLabel = new JLabel();

    public IssuePollPanel() {
        super("IssuePollPanel");
        setLayout(new GridBagLayout());
        optionsTableModel = new CreateOptionsTableModel(new Object[]{Lang.getInstance().translate("Name")}, 0);
        addImageLabel = new AddImageLabel(
                Lang.getInstance().translate("Add image"), WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG,
                0, ItemCls.MAX_IMAGE_LENGTH, WIDTH_IMAGE_INITIAL, HEIGHT_IMAGE_INITIAL);

        addLogoIconLabel = new AddImageLabel(Lang.getInstance().translate("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO, TypeOfImage.GIF,
                0, ItemCls.MAX_ICON_LENGTH, WIDTH_LOGO_INITIAL, HEIGHT_LOGO_INITIAL);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();


        titleJLabel.setFont(FONT_TITLE);
        titleJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleJLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleJLabel.setText(Lang.getInstance().translate("Issue Poll"));

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        add(titleJLabel, gridBagConstraints);

        GridBagConstraints gbcAddImagePanel = new GridBagConstraints();
        gbcAddImagePanel.gridx = 0;
        gbcAddImagePanel.gridy = 2;
        gbcAddImagePanel.gridheight = 2;
        gbcAddImagePanel.anchor = GridBagConstraints.NORTH;
        gbcAddImagePanel.insets = new Insets(0, 12, 8, 8);
        add(addImageLabel, gbcAddImagePanel);

        GridBagConstraints gbcAddLogoIconPanel = new GridBagConstraints();
        gbcAddLogoIconPanel.gridx = 0;
        gbcAddLogoIconPanel.gridy = 4;
        gbcAddImagePanel.gridheight = 1;
        add(addLogoIconLabel, gbcAddLogoIconPanel);

        // BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.gridx = 0;
        buttonGBC.gridy = 7;
        buttonGBC.gridwidth = 3;
        buttonGBC.anchor = GridBagConstraints.CENTER;
        // BUTTON Register
        createButton = new JButton(Lang.getInstance().translate("Create"));
        createButton.addActionListener(e -> onRegisterClick());
        add(createButton, buttonGBC);

        JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        GridBagConstraints gbcFromLabel = new GridBagConstraints();
        gbcFromLabel.gridx = 1;
        gbcFromLabel.gridy = 1;
        gbcFromLabel.anchor = GridBagConstraints.NORTHEAST;
        add(fromLabel, gbcFromLabel);

        // LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.gridx = 1;
        labelGBC.gridy = 2;
        labelGBC.anchor = GridBagConstraints.NORTHEAST;
        JLabel nameLabel = new JLabel(Lang.getInstance().translate("Name") + ":");
        add(nameLabel, labelGBC);

        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        GridBagConstraints gbcDescriptionLabel = new GridBagConstraints();
        gbcDescriptionLabel.gridx = 1;
        gbcDescriptionLabel.gridy = 3;
        gbcDescriptionLabel.anchor = GridBagConstraints.NORTHEAST;
        add(descriptionLabel, gbcDescriptionLabel);

        JLabel optionsLabel = new JLabel(Lang.getInstance().translate("Options") + ":");
        GridBagConstraints gbcOptionsLabel = new GridBagConstraints();
        gbcOptionsLabel.gridx = 1;
        gbcOptionsLabel.gridy = 4;
        gbcOptionsLabel.anchor = GridBagConstraints.NORTHEAST;
        add(optionsLabel, gbcOptionsLabel);

        JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
        feeLabel.setVisible(Gui.SHOW_FEE_POWER);
        GridBagConstraints gbcFeeLabel = new GridBagConstraints();
        gbcFeeLabel.gridx = 1;
        gbcFeeLabel.gridy = 6;
        gbcFeeLabel.anchor = GridBagConstraints.NORTHEAST;
        add(feeLabel, gbcFeeLabel);

        // TEXTFIELD GBC
        GridBagConstraints txtGBC = new GridBagConstraints();
        txtGBC.gridx = 2;
        txtGBC.gridy = 1;
        txtGBC.gridwidth = 2;
        txtGBC.fill = GridBagConstraints.HORIZONTAL;
        txtGBC.anchor = GridBagConstraints.NORTHWEST;
        cbxFrom = new JComboBox<>(new AccountsComboBoxModel());
        add(cbxFrom, txtGBC);

        GridBagConstraints gbcTxtName = new GridBagConstraints();
        gbcTxtName.gridx = 2;
        gbcTxtName.gridy = 2;
        gbcTxtName.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtName.weightx = 0.1;
        add(txtName, gbcTxtName);


        // TXTAREA NAME
        GridBagConstraints gbcDescription = new GridBagConstraints();
        gbcDescription.gridx = 2;
        gbcDescription.gridy = 3;
        gbcDescription.weighty = 0.1;
        gbcDescription.weightx = 0.1;
        gbcDescription.gridwidth = 2;
        gbcDescription.fill = GridBagConstraints.BOTH;
        gbcDescription.anchor = GridBagConstraints.NORTHWEST;
        JScrollPane scrollPaneDescription = new JScrollPane();
        scrollPaneDescription.setViewportView(txtareaDescription);
        add(scrollPaneDescription, gbcDescription);

        // TABLE OPTIONS
        GridBagConstraints gbcOptionalTable = new GridBagConstraints();
        gbcOptionalTable.gridx = 2;
        gbcOptionalTable.gridy = 4;
        gbcOptionalTable.weighty = 0.1;
        gbcOptionalTable.gridwidth = 2;
        gbcOptionalTable.fill = GridBagConstraints.BOTH;
        gbcOptionalTable.anchor = GridBagConstraints.CENTER;
        table = new MTable(optionsTableModel);
        JScrollPane scrollPaneOptionalTable = new JScrollPane();
        scrollPaneOptionalTable.setViewportView(table);
        add(scrollPaneOptionalTable, gbcOptionalTable);

        JButton deleteButton = new JButton(Lang.getInstance().translate("Delete"));
        deleteButton.addActionListener(e -> deleteRow());
        GridBagConstraints gbcDeleteButton = new GridBagConstraints();
        gbcDeleteButton.gridx = 2;
        gbcDeleteButton.gridy = 5;
        gbcDeleteButton.fill = GridBagConstraints.HORIZONTAL;
        gbcDeleteButton.gridwidth = 2;
        add(deleteButton, gbcDeleteButton);


        txtFee.setModel(new DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFee.setSelectedIndex(0);
        txtFee.setVisible(Gui.SHOW_FEE_POWER);

        GridBagConstraints gbcTxtFee = new GridBagConstraints();
        gbcTxtFee.gridx = 2;
        gbcTxtFee.gridy = 6;
        gbcTxtFee.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtFee.gridwidth = 2;
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
        if (checkWalletUnlock(createButton)) {
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
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        IssuePollRecord issuePoll = (IssuePollRecord) Controller.getInstance().issuePoll(creator,
                txtName.getText(), txtareaDescription.getText(),
                optionsTableModel.getOptions(), icon, image, feePow);

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Issue Voting") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issuePoll.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + this.txtName.getText() + "<br>";
        text += "<br>" + Lang.getInstance().translate("Description") + ":<br>"
                + Library.to_HTML(this.txtareaDescription.getText()) + "<br>";
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


    private void deleteRow() {
        if (optionsTableModel.getRowCount() > 1) {
            int selRow = table.getSelectedRow();
            if (selRow != -1) {
                optionsTableModel.removeRow(selRow);
            }
        }
    }

}
