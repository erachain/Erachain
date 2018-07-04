package gui.items.templates;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.item.assets.AssetCls;
import core.transaction.IssueTemplateRecord;
import core.transaction.Transaction;
import gui.MainFrame;
import gui.PasswordPane;
import gui.library.Issue_Confirm_Dialog;
import gui.library.My_Add_Image_Panel;
import gui.library.library;
import gui.models.AccountsComboBoxModel;
import gui.transaction.IssueTemplateDetailsFrame;
import lang.Lang;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class IssueTemplatePanel extends JPanel {
    private JComboBox<Account> jComboBox_Account_Creator;
    private IssueTemplatePanel th;
    // Variables declaration - do not modify
    private javax.swing.JButton jButton_Create;
    private javax.swing.JCheckBox jCheckBox_Encrypted;
    // private javax.swing.JComboBox<String> jComboBox_Account_Creator;
    private javax.swing.JComboBox<String> jComboBox_Template;
    private javax.swing.JLabel jLabel_Account_Creator;
    private javax.swing.JLabel jLabel_Content;
    private javax.swing.JLabel jLabel_Fee;
    private javax.swing.JLabel jLabel_Issue_Template;
    private javax.swing.JLabel jLabel_Template;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JLabel jLabel_auto_saze;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea_Content;
    private javax.swing.JComboBox<String> txtFeePow;
    private javax.swing.JTextField jTextField_Title;
    private GridBagConstraints gridBagConstraints_1;
    private GridBagConstraints gridBagConstraints_2;
    private My_Add_Image_Panel add_Image_Panel;
    private My_Add_Image_Panel add_Logo_Icon_Panel;
    private JLabel lblNewLabel;
    private GridBagConstraints gridBagConstraints_3;
    private GridBagConstraints gridBagConstraints_4;
    private GridBagConstraints gridBagConstraints_5;
    public IssueTemplatePanel() {

        // CLOSE
        // setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ICON
        th = this;
        this.jComboBox_Account_Creator = new JComboBox<Account>(new AccountsComboBoxModel());
        initComponents();
        this.setVisible(true);
    }

    public void onIssueClick() {
        // DISABLE
        this.jButton_Create.setEnabled(false);

        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                        Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                this.jButton_Create.setEnabled(true);

                return;
            }
        }

        // READ CREATOR
        Account sender = (Account) this.jComboBox_Account_Creator.getSelectedItem();

        long parse = 0;
        int feePow = 0;
        try {

            // READ FEE POW
            feePow = Integer.parseInt((String)this.txtFeePow.getSelectedItem());
        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid quantity!"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }

            // ENABLE
            this.jButton_Create.setEnabled(true);
            return;

        }

        byte[] icon = add_Logo_Icon_Panel.imgButes;
        byte[] image = add_Image_Panel.imgButes;

        // CREATE PLATE
        PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
        IssueTemplateRecord issueTemplate = (IssueTemplateRecord) Controller.getInstance().issueTemplate(creator,
                this.jTextField_Title.getText(), this.jTextArea_Content.getText(), icon, image, feePow);

        // Issue_Asset_Confirm_Dialog cont = new
        // Issue_Asset_Confirm_Dialog(issueAssetTransaction);
        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Issue Template") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issueTemplate.getCreator() + "<br>";
        text += Lang.getInstance().translate("Title") + ":&nbsp;" + issueTemplate.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>"
                + library.to_HTML(issueTemplate.getItem().getDescription()) + "<br>";
        String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;" + issueTemplate.viewSize(false)
                + " Bytes, ";
        Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + issueTemplate.getFee().toString()
                + " COMPU</b><br></body></HTML>";

        // System.out.print("\n"+ text +"\n");
        // UIManager.put("OptionPane.cancelButtonText", "Отмена");
        // UIManager.put("OptionPane.okButtonText", "Готово");

        // int s = JOptionPane.showConfirmDialog(MainFrame.getInstance(), text,
        // Lang.getInstance().translate("Issue Asset"), JOptionPane.YES_NO_OPTION);

        Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true, text,
                (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction") + " "
                        + Lang.getInstance().translate("Issue Template"));
        IssueTemplateDetailsFrame ww = new IssueTemplateDetailsFrame(issueTemplate);
        dd.jScrollPane1.setViewportView(ww);
        dd.setLocationRelativeTo(th);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (!dd.isConfirm) { // s!= JOptionPane.OK_OPTION) {

            this.jButton_Create.setEnabled(true);

            return;
        }

        // VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issueTemplate, false);

        // CHECK VALIDATE MESSAGE
        switch (result) {
            case Transaction.VALIDATE_OK:

                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Template issue has been sent!"),
                        Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                // this.dispose();

                break;

            case Transaction.NOT_ENOUGH_FEE:

                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Not enough %fee% balance!").replace("%fee%", AssetCls.FEE_NAME),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;

            case Transaction.INVALID_NAME_LENGTH:

                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Name must be between 1 and 100 characters!"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;

            case Transaction.INVALID_DESCRIPTION_LENGTH:

                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Description must be between 1 and 1000 characters!"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;

            case Transaction.CREATOR_NOT_PERSONALIZED:

                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Issuer account not personalized!"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;

            default:

                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Unknown error") + "[" + result + "]!",
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                break;

        }

        // ENABLE
        this.jButton_Create.setEnabled(true);
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jTextField_Title = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea_Content = new javax.swing.JTextArea();
        jLabel_Fee = new javax.swing.JLabel();
        // jComboBox_Account_Creator = new javax.swing.JComboBox<>();
        txtFeePow = new javax.swing.JComboBox<String>();
        jLabel_Template = new javax.swing.JLabel();
        jLabel_Issue_Template = new javax.swing.JLabel();
        jComboBox_Template = new javax.swing.JComboBox<>();
        jCheckBox_Encrypted = new javax.swing.JCheckBox();
        jButton_Create = new javax.swing.JButton();
        jLabel_auto_saze = new javax.swing.JLabel();
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{146, 32, 0, 0, 8, 0, 8, 0};
        layout.rowHeights = new int[]{0, 4, 51, 32, 0, 4, 0, 4, -12, 4, 0, 4, 0, 4, 0, 4, 0};
        setLayout(layout);

        lblNewLabel = new JLabel("New label");
        jLabel_Account_Creator = new javax.swing.JLabel();

        jLabel_Account_Creator.setText(Lang.getInstance().translate("Account Creator") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new Insets(0, 15, 5, 5);
        add(jLabel_Account_Creator, gridBagConstraints);
        add_Image_Panel = new My_Add_Image_Panel(
                Lang.getInstance().translate("Add Image") + (" (max %1%kB)").replace("%1%", "1024"), 250, 250);

        gridBagConstraints_3 = new java.awt.GridBagConstraints();
        gridBagConstraints_3.gridheight = 4;
        gridBagConstraints_3.gridx = 0;
        gridBagConstraints_3.gridy = 4;
        gridBagConstraints_3.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints_3.insets = new java.awt.Insets(0, 0, 6, 8);
        add(add_Image_Panel, gridBagConstraints_3);

        GridBagConstraints gbc_add_Logo_Icon_Panel = new GridBagConstraints();
        gbc_add_Logo_Icon_Panel.insets = new Insets(0, 0, 5, 5);
        gbc_add_Logo_Icon_Panel.gridx = 1;
        gbc_add_Logo_Icon_Panel.gridy = 0;

        jLabel_Title = new javax.swing.JLabel();

        jLabel_Title.setText(Lang.getInstance().translate("Title") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 15, 5, 5);
        add(jLabel_Title, gridBagConstraints);

        jTextField_Title.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_TitleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 15);
        add(jTextField_Title, gridBagConstraints);
        add_Logo_Icon_Panel = new My_Add_Image_Panel(Lang.getInstance().translate("Add Logo"), 50, 50);

        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
        gbc_lblNewLabel.gridx = 7;
        gbc_lblNewLabel.gridy = 4;
        add(add_Logo_Icon_Panel, gbc_lblNewLabel);
        jLabel_Content = new javax.swing.JLabel();

        jLabel_Content.setText(Lang.getInstance().translate("Content") + ":");
        gridBagConstraints_4 = new java.awt.GridBagConstraints();
        gridBagConstraints_4.gridheight = 2;
        gridBagConstraints_4.gridx = 1;
        gridBagConstraints_4.gridy = 6;
        gridBagConstraints_4.anchor = GridBagConstraints.NORTH;
        gridBagConstraints_4.insets = new Insets(0, 15, 5, 5);
        add(jLabel_Content, gridBagConstraints_4);

        jTextArea_Content.setColumns(20);
        // jTextArea_Content.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jTextArea_Content.setLineWrap(true);
        jTextArea_Content.setRows(18);
        jTextArea_Content.setAlignmentY(1.0F);
        jScrollPane1.setViewportView(jTextArea_Content);

        gridBagConstraints_5 = new java.awt.GridBagConstraints();
        gridBagConstraints_5.gridheight = 3;
        gridBagConstraints_5.gridx = 2;
        gridBagConstraints_5.gridy = 6;
        gridBagConstraints_5.gridwidth = 5;
        gridBagConstraints_5.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints_5.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints_5.weightx = 0.1;
        gridBagConstraints_5.weighty = 0.7;
        gridBagConstraints_5.insets = new Insets(0, 0, 5, 15);
        add(jScrollPane1, gridBagConstraints_5);

        jLabel_Fee.setText(Lang.getInstance().translate("Fee") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 15, 5, 5);
        add(jLabel_Fee, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 15);
        add(jComboBox_Account_Creator, gridBagConstraints);

       
        txtFeePow.setToolTipText("Level of FEE Power");
        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }));
        txtFeePow.setSelectedIndex(0);
        txtFeePow.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints_1 = new java.awt.GridBagConstraints();
        gridBagConstraints_1.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints_1.gridx = 3;
        gridBagConstraints_1.gridy = 12;
        gridBagConstraints_1.gridwidth = 3;
        gridBagConstraints_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints_1.weightx = 0.1;
        gridBagConstraints_1.anchor = java.awt.GridBagConstraints.LINE_START;
        add(txtFeePow, gridBagConstraints_1);

        jLabel_Template.setText(Lang.getInstance().translate("Template") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        // add(jLabel_Template, gridBagConstraints);

        // jLabel_Issue_Template.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel_Issue_Template.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Issue_Template.setText(Lang.getInstance().translate("Issue Template"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new Insets(12, 15, 5, 15);
        add(jLabel_Issue_Template, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
        // add(jComboBox_Template, gridBagConstraints);

        jCheckBox_Encrypted.setText(Lang.getInstance().translate("Encrypted"));
        jCheckBox_Encrypted.setToolTipText("");
        jCheckBox_Encrypted.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_EncryptedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        // add(jCheckBox_Encrypted, gridBagConstraints);

        jButton_Create.setText(Lang.getInstance().translate("Create"));
        jButton_Create.setRequestFocusEnabled(false);
        jButton_Create.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onIssueClick();
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 5, 15);
        add(jButton_Create, gridBagConstraints);
        gridBagConstraints_2 = new java.awt.GridBagConstraints();
        gridBagConstraints_2.gridheight = 3;
        gridBagConstraints_2.gridx = 3;
        gridBagConstraints_2.gridy = 14;
        gridBagConstraints_2.gridwidth = 5;
        gridBagConstraints_2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints_2.weightx = 0.1;
        gridBagConstraints_2.weighty = 0.3;
        add(jLabel_auto_saze, gridBagConstraints_2);
        this.setMinimumSize(new Dimension(0, 0));
    }// </editor-fold>

    private void jTextField_TitleActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void jCheckBox_EncryptedActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }
}
