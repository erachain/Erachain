package org.erachain.gui.items.unions;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.IssueUnionRecord;
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
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.ParseException;

import static org.erachain.gui.GUIConstants.*;

//import java.math.BigDecimal;
//import org.erachain.settings.Settings;

@SuppressWarnings("serial")
public class IssueUnionPanel extends JPanel {
    private JComboBox<Account> cbxFrom;
    private JComboBox<String> txtFeePow;
    private JTextField txtName;
    private JTextArea txtareaDescription;
    private JTextField txtBirthday;
    private JTextField txtParent;
    private JButton issueButton;
    private IssueUnionPanel th;
    // Variables declaration - do not modify
    private JLabel titleJLabel;
    private JLabel accountJLabel;
    private JLabel descriptionJLabel;
    private JLabel feeJLabel;
    private AddImageLabel addLogoPanel;
    private AddImageLabel addImagePanel;
    private javax.swing.JScrollPane jScrollPane1;
    private JLabel nameJLabel;
    private JLabel birthdayJLabel;
    private JLabel parentJLabel;

    public IssueUnionPanel() {
//		super(Lang.getInstance().translate("Erachain.org") + " - " + Lang.getInstance().translate("Issue Union"));

        String colorText = "ff0000"; // цвет текста в форме
        th = this;
        this.issueButton = new JButton();
        txtFeePow = new JComboBox<String>();
        txtName = new JTextField();
        txtareaDescription = new JTextArea();
        txtBirthday = new JTextField();
        txtParent = new JTextField();


        initComponents();

        addLogoPanel.setPreferredSize(new Dimension(250, 50));


        //BUTTON Register


        this.issueButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onIssueClick();
            }
        });


        this.setMinimumSize(new Dimension(0, 0));
        this.setVisible(true);
    }

    public void onIssueClick() {
        //DISABLE
        this.issueButton.setEnabled(false);

        //CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            //NETWORK NOT OK
            JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.issueButton.setEnabled(true);

            return;
        }

        //CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            //ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (!Controller.getInstance().unlockWallet(password)) {
                //WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.issueButton.setEnabled(true);

                return;
            }
        }

        //READ CREATOR
        Account sender = (Account) this.cbxFrom.getSelectedItem();

        int parse = 0;
        int feePow = 0;
        long birthday = 0;
        long parent = -1;
        try {

            //READ FEE POW
            feePow = Integer.parseInt((String)this.txtFeePow.getSelectedItem());

            // READ BIRTHDAY
            parse++;
            //birthday = Long.parseLong(this.txtBirthday.getText());
            // 1970-08-12 03:05:07
            String bd = this.txtBirthday.getText();
            if (bd.length() < 11) bd = bd + " 12:12:12";// UTC";
            Timestamp ts = Timestamp.valueOf(bd);
            birthday = ts.getTime();

            //READ PARENT
            parse++;
            parent = Integer.parseInt(this.txtParent.getText());

        } catch (Exception e) {
            String mess = "Invalid pars... " + parse;
            switch (parse) {
                case 0:
                    mess = "Invalid fee power 0..6";
                    break;
                case 1:
                    mess = "Invalid birthday [YYYY-MM-DD]";
                    break;
                case 2:
                    mess = "Invalid parent";
                    break;
            }
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            this.issueButton.setEnabled(true);
            return;
        }

        byte[] icon = null;
        byte[] image = null;
        //CREATE ASSET
        //PrivateKeyAccount creator, String fullName, int feePow, long birthday,
        //byte gender, String race, float birthLatitude, float birthLongitude,
        //String skinColor, String eyeColor, String hairСolor, int height, String description
        PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
        IssueUnionRecord issue_Union = (IssueUnionRecord) Controller.getInstance().issueUnion(
                creator, this.txtName.getText(), birthday, parent, this.txtareaDescription.getText(),
                addLogoPanel.getImgBytes(), image,
                feePow);
        //Issue_Asset_Confirm_Dialog cont = new Issue_Asset_Confirm_Dialog(issueAssetTransaction);
        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;" + Lang.getInstance().translate("Issue Union") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issue_Union.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + issue_Union.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>" + library.to_HTML(issue_Union.getItem().getDescription()) + "<br>";
        text += Lang.getInstance().translate("Date") + ":&nbsp;" + ((UnionCls) issue_Union.getItem()).getBirthday() + "<br>";
        text += Lang.getInstance().translate("Parent") + ":&nbsp;" + ((UnionCls) issue_Union.getItem()).getParent() + "<br>";

        String Status_text = "";

        //	    UIManager.put("OptionPane.cancelButtonText", "Отмена");
        //	    UIManager.put("OptionPane.okButtonText", "Готово");

        //	int s = JOptionPane.showConfirmDialog(MainFrame.getInstance(), text, Lang.getInstance().translate("Issue Asset"),  JOptionPane.YES_NO_OPTION);

        IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, issue_Union,
                text, (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
        dd.setLocationRelativeTo(th);
        dd.setVisible(true);

        //	JOptionPane.OK_OPTION
        if (!dd.isConfirm) { //s!= JOptionPane.OK_OPTION)	{

            this.issueButton.setEnabled(true);

            return;
        }
        //VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issue_Union, Transaction.FOR_NETWORK);
        //CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Union issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            //		this.dispose();
            clearPanel();
        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        //ENABLE
        this.issueButton.setEnabled(true);
    }

    void clearPanel() {

        this.txtName.setText("");
        this.txtareaDescription.setText("");
        this.txtBirthday.setText("1970-12-08");
        this.txtParent.setText("-1");
        this.txtFeePow.setSelectedItem("0");


    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        accountJLabel = new JLabel();
        nameJLabel = new JLabel();
        addLogoPanel = new AddImageLabel(Lang.getInstance().translate("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO,TypeOfImage.GIF);
        addImagePanel = new AddImageLabel((Lang.getInstance().translate("Add image") +
                (" (max %1%kB)").replace("%1%", "1024")),
                WIDTH_IMAGE, HEIGHT_IMAGE,TypeOfImage.JPEG);
        jScrollPane1 = new javax.swing.JScrollPane();
        titleJLabel = new JLabel();
        descriptionJLabel = new JLabel();
        birthdayJLabel = new JLabel();
        parentJLabel = new JLabel();
        feeJLabel = new JLabel();

        setLayout(new java.awt.GridBagLayout());

        accountJLabel.setText(Lang.getInstance().translate("Account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 7);
        add(accountJLabel, gridBagConstraints);

        this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 8);
        add(cbxFrom, gridBagConstraints);

        nameJLabel.setText(Lang.getInstance().translate("Name") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 7);
        add(nameJLabel, gridBagConstraints);

        txtName.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(txtName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 8);
        add(addLogoPanel, gridBagConstraints);

        addImagePanel.setPreferredSize(new java.awt.Dimension(250, 350));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
        add(addImagePanel, gridBagConstraints);

        txtareaDescription.setColumns(20);
        txtareaDescription.setRows(5);
        jScrollPane1.setViewportView(txtareaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 8);
        add(jScrollPane1, gridBagConstraints);

        titleJLabel.setText(Lang.getInstance().translate("Issue Union"));
        titleJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleJLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 6, 6, 9);
        add(titleJLabel, gridBagConstraints);

        descriptionJLabel.setText(Lang.getInstance().translate("Description") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 7);
        add(descriptionJLabel, gridBagConstraints);

        birthdayJLabel.setText(Lang.getInstance().translate("Birthday") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 7);
        add(birthdayJLabel, gridBagConstraints);

//TXT Birthday


        // Маска ввода
        MaskFormatter mf1 = null;
        try {
            mf1 = new MaskFormatter("####-##-##");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        this.txtBirthday = new JFormattedTextField(mf1);
        this.txtBirthday.setText("1970-12-08");


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 10);
        add(txtBirthday, gridBagConstraints);

        parentJLabel.setText(Lang.getInstance().translate("Parent") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 7);
        add(parentJLabel, gridBagConstraints);

        txtParent.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        add(txtParent, gridBagConstraints);

        feeJLabel.setText(Lang.getInstance().translate("Fee Power") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(feeJLabel, gridBagConstraints);

        txtFeePow = new JComboBox<String>();
        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" }));
        txtFeePow.setSelectedIndex(0);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        add(txtFeePow, gridBagConstraints);

        issueButton.setText(Lang.getInstance().translate("Issue"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(issueButton, gridBagConstraints);
    }// </editor-fold>

    // End of variables declaration

}
