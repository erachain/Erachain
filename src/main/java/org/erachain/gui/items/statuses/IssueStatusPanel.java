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

@SuppressWarnings("serial")
public class IssueStatusPanel extends JPanel {
    private JComboBox<Account> cbxFrom;
    private JComboBox<String> txtFeePow;
    private JTextField txtName;
    private JTextArea txtareaDescription;
    private JButton issueButton;
    private JCheckBox jCheck_Unique;
    private IssueStatusPanel th;
    private AddImageLabel add_Image_Panel;
    private AddImageLabel add_Logo_Icon_Panel;

    // @SuppressWarnings({ "unchecked", "rawtypes" })
    public IssueStatusPanel() {

        String colorText = "ff0000";
        th = this;

        // LAYOUT
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowHeights = new int[] { 0, 0, 0, 83, 0, 0, 0, 0 };
        gridBagLayout.columnWidths = new int[] { -16, 0, 0, 0, 216, 0, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
        gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0 };
        this.setLayout(gridBagLayout);

        // COMBOBOX GBC
        GridBagConstraints cbxGBC = new GridBagConstraints();
        // cbxGBC.insets = new Insets(5,5,5,5);
        cbxGBC.fill = GridBagConstraints.NONE;
        cbxGBC.anchor = GridBagConstraints.NORTHWEST;
        cbxGBC.weightx = 0;
        cbxGBC.gridx = 1;
        cbxGBC.insets = new java.awt.Insets(5, 3, 5, 15);

        int gridy = 0;

        // JLabel info_Label = new JLabel();
        // info_Label.setText(Lang.getInstance().translate("Issue Status"));
        // labelGBC.gridy = gridy;

        JLabel jLabel1 = new JLabel();
        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(Lang.getInstance().translate("Create Status"));
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.3;
        gridBagConstraints.insets = new java.awt.Insets(8, 15, 8, 15);
        add(jLabel1, gridBagConstraints);

        // this.add(info_Label, labelGBC);
        gridy++;

        add_Image_Panel = new AddImageLabel(
                Lang.getInstance().translate("Add image") + (" (max %1%kB)").replace("%1%", "1024"), 250, 250,TypeOfImage.JPEG);
        GridBagConstraints gbc_add_Image_Panel = new GridBagConstraints();
        gbc_add_Image_Panel.anchor = GridBagConstraints.NORTH;
        gbc_add_Image_Panel.fill = GridBagConstraints.HORIZONTAL;
        gbc_add_Image_Panel.gridheight = 3;
        gbc_add_Image_Panel.gridwidth = 2;
        gbc_add_Image_Panel.insets = new Insets(0, 0, 5, 5);
        gbc_add_Image_Panel.gridx = 0;
        gbc_add_Image_Panel.gridy = 1;
        add(add_Image_Panel, gbc_add_Image_Panel);
        JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        GridBagConstraints gbc_fromLabel = new GridBagConstraints();
        gbc_fromLabel.anchor = GridBagConstraints.WEST;
        gbc_fromLabel.insets = new Insets(0, 0, 5, 5);
        gbc_fromLabel.gridx = 2;
        gbc_fromLabel.gridy = 1;

        this.add(fromLabel, gbc_fromLabel);
        this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        GridBagConstraints gbc_cbxFrom = new GridBagConstraints();
        gbc_cbxFrom.gridwidth = 3;
        gbc_cbxFrom.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbxFrom.insets = new Insets(0, 0, 5, 5);
        gbc_cbxFrom.gridx = 4;
        gbc_cbxFrom.gridy = 1;
        this.add(this.cbxFrom, gbc_cbxFrom);
        JLabel nameLabel = new JLabel(
                "<HTML><p style=':#" + colorText + "'>" + Lang.getInstance().translate("Name") + ": </p></html>");
        GridBagConstraints gbc_nameLabel = new GridBagConstraints();
        gbc_nameLabel.anchor = GridBagConstraints.WEST;
        gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
        gbc_nameLabel.gridx = 2;
        gbc_nameLabel.gridy = 2;
        this.add(nameLabel, gbc_nameLabel);
        this.txtName = new JTextField();
        GridBagConstraints gbc_txtName = new GridBagConstraints();
        gbc_txtName.gridwidth = 2;
        gbc_txtName.fill = GridBagConstraints.BOTH;
        gbc_txtName.insets = new Insets(0, 0, 5, 5);
        gbc_txtName.gridx = 4;
        gbc_txtName.gridy = 2;
        this.add(txtName, gbc_txtName);

        add_Logo_Icon_Panel = new AddImageLabel(Lang.getInstance().translate("Add Logo"), 50, 50,TypeOfImage.GIF);

        GridBagConstraints gbc_add_Logo_Icon_Panel = new GridBagConstraints();
        gbc_add_Logo_Icon_Panel.anchor = GridBagConstraints.EAST;
        gbc_add_Logo_Icon_Panel.insets = new Insets(0, 0, 5, 5);
        gbc_add_Logo_Icon_Panel.gridx = 6;
        gbc_add_Logo_Icon_Panel.gridy = 2;
        add(add_Logo_Icon_Panel, gbc_add_Logo_Icon_Panel);
        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
        gbc_descriptionLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_descriptionLabel.insets = new Insets(0, 0, 5, 5);
        gbc_descriptionLabel.gridx = 2;
        gbc_descriptionLabel.gridy = 3;
        this.add(descriptionLabel, gbc_descriptionLabel);

        // TEXTFIELD GBC

        JScrollPane scrollDescription = new JScrollPane();
        scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbc_scrollDescription = new GridBagConstraints();
        gbc_scrollDescription.gridwidth = 3;
        gbc_scrollDescription.fill = GridBagConstraints.BOTH;
        gbc_scrollDescription.insets = new Insets(0, 0, 5, 5);
        gbc_scrollDescription.gridx = 4;
        gbc_scrollDescription.gridy = 3;

        this.add(scrollDescription, gbc_scrollDescription);
        this.txtareaDescription = new JTextArea();
        scrollDescription.setViewportView(txtareaDescription);

        JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + " (0..6)" + ":");
        GridBagConstraints gbc_feeLabel = new GridBagConstraints();
        gbc_feeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_feeLabel.gridx = 2;
        gbc_feeLabel.gridy = 4;
        this.add(feeLabel, gbc_feeLabel);
        txtFeePow = new JComboBox<String>();
        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }));
        txtFeePow.setSelectedIndex(0);
        GridBagConstraints gbc_txtFeePow = new GridBagConstraints();
        gbc_txtFeePow.gridwidth = 3;
        gbc_txtFeePow.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtFeePow.insets = new Insets(0, 0, 5, 5);
        gbc_txtFeePow.gridx = 4;
        gbc_txtFeePow.gridy = 4;
        this.add(this.txtFeePow, gbc_txtFeePow);

        // PACK
        // this.pack();
        // this.setResizable(false);
        // this.setLocationRelativeTo(null);
        this.setMinimumSize(new Dimension(0, 0));
        JLabel unoqueLabel = new JLabel(Lang.getInstance().translate("Single") + ":");
        GridBagConstraints gbc_unoqueLabel = new GridBagConstraints();
        gbc_unoqueLabel.insets = new Insets(0, 0, 5, 5);
        gbc_unoqueLabel.gridx = 2;
        gbc_unoqueLabel.gridy = 5;
        this.add(unoqueLabel, gbc_unoqueLabel);
        jCheck_Unique = new JCheckBox();
        GridBagConstraints gbc_jCheck_Unique = new GridBagConstraints();
        gbc_jCheck_Unique.anchor = GridBagConstraints.WEST;
        gbc_jCheck_Unique.insets = new Insets(0, 0, 5, 5);
        gbc_jCheck_Unique.gridx = 4;
        gbc_jCheck_Unique.gridy = 5;
        this.add(this.jCheck_Unique, gbc_jCheck_Unique);

        this.issueButton = new JButton(Lang.getInstance().translate("Issue"));
        this.issueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onIssueClick();
            }
        });
        GridBagConstraints gbc_issueButton = new GridBagConstraints();
        gbc_issueButton.insets = new Insets(0, 0, 5, 5);
        gbc_issueButton.gridx = 6;
        gbc_issueButton.gridy = 6;
        this.add(this.issueButton, gbc_issueButton);
        this.setVisible(true);
    }

    public void onIssueClick() {
        // DISABLE
        this.issueButton.setEnabled(false);

        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                        Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                this.issueButton.setEnabled(true);

                return;
            }
        }

        // READ CREATOR
        Account sender = (Account) this.cbxFrom.getSelectedItem();

        int parse = 0;
        int feePow = 0;
        try {

            // READ FEE POW
            feePow = Integer.parseInt((String)this.txtFeePow.getSelectedItem());

        } catch (Exception e) {
            String mess = "Invalid pars... " + parse;
            switch (parse) {
            case 0:
                mess = "Invalid fee power 0..6";
                break;
            }
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            this.issueButton.setEnabled(true);
            return;
        }

        byte[] icon = add_Logo_Icon_Panel.getImgBytes();
        byte[] image = add_Image_Panel.getImgBytes();

        boolean unique = jCheck_Unique.isSelected();
        PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
        IssueStatusRecord issue_Status = (IssueStatusRecord) Controller.getInstance().issueStatus(creator,
                this.txtName.getText(), this.txtareaDescription.getText(), unique, icon, image, feePow);

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Create Status") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issue_Status.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + issue_Status.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>"
                + library.to_HTML(issue_Status.getItem().getDescription()) + "<br>";

        text += Lang.getInstance().translate("Unique") + ": " + ((StatusCls) issue_Status.getItem()).isUnique()
                + "<br>";
        String Status_text = "";

        //System.out.print("\n" + text + "\n");

        IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, issue_Status,
                text,
                (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"));
        dd.setLocationRelativeTo(th);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (!dd.isConfirm) { // s!= JOptionPane.OK_OPTION) {

            this.issueButton.setEnabled(true);

            return;
        }

        // VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issue_Status, Transaction.FOR_NETWORK);

        // CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Status issue has been sent") + "!",
                    Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            // this.dispose();
            clearPanel();

        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        // ENABLE
        this.issueButton.setEnabled(true);
    }

    void clearPanel() {

        this.txtName.setText("");
        this.txtareaDescription.setText("");
        this.txtFeePow.setSelectedItem("0");

    }

}
