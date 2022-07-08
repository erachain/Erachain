package org.erachain.gui.items.persons;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RCertifyPubKeys;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.Gui;
import org.erachain.gui.MainFrame;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.CertifyPubKeysDetailsFrame;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
import org.erachain.utils.Pair;
import org.mapdb.Fun.Tuple4;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

//public class PersonConfirm extends JDialog { // InternalFrame  {
public class PersonCertifyPubKeysDialog extends JDialog {

    // private JComboBox<Account> accountLBox;

    // private static final long serialVersionUID = 1L;
    private static final long serialVersionUID = 2717571093561259483L;
    // Variables declaration - do not modify
    private MButton jButton_Cansel;
    private MButton jButton_Confirm;
    private JComboBox<Account> jComboBox_YourAddress;
    private javax.swing.JTextField jFormattedTextField_Fee;
    private javax.swing.JTextField jTextField_addDays;
    private javax.swing.JLabel jLabel_Address1;
    private javax.swing.JLabel jLabel_Address2;
    private javax.swing.JLabel jLabel_Address2_Check;
    private javax.swing.JLabel jLabel_Address3;
    private javax.swing.JLabel jLabel_Address3_Check;
    private javax.swing.JLabel jLabel_Adress1_Check;
    private javax.swing.JLabel jLabel_Fee;
    private javax.swing.JLabel jLabel_Fee_Check;
    // private javax.swing.JLabel jLabel_PersonInfo;
    private javax.swing.JScrollPane jLabel_PersonInfo;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JLabel jLabel_addDays;
    private javax.swing.JLabel jLabel_addDays_Check;
    private javax.swing.JLabel jLabel_YourAddress;
    private javax.swing.JTextField jTextField_Address1;
    private javax.swing.JTextField jTextField_Address2;
    private javax.swing.JTextField jTextField_Address3;

    public PersonCertifyPubKeysDialog(PersonCls person, PublicKeyAccount publicKey) {
        super();

        // ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        initComponents(person, publicKey);

        this.setTitle(Lang.T("Certification of Account"));
        this.setModal(true);

        setPreferredSize(new Dimension(1200, 600));
        this.setResizable(true);
        // PACK
        this.pack();
        this.setLocationRelativeTo(null); // set after PACK!!
        this.setVisible(true);
    }

    private void refreshReceiverDetails(JTextField pubKeyTxt, JLabel pubKeyDetails) {
        String pubKeyStr = pubKeyTxt.getText();
        if (pubKeyStr == null || pubKeyStr.trim().isEmpty()) {
            pubKeyDetails.setText("");
            return;
        }

        pubKeyStr = pubKeyStr.trim();

        if (Base58.isExtraSymbols(pubKeyStr)) {
            pubKeyDetails.setText(Lang.T(OnDealClick.resultMess(Transaction.INVALID_PUBLIC_KEY)));
            return;
        }

        PublicKeyAccount pubKey = new PublicKeyAccount(pubKeyStr);

        if (!pubKey.isValid()) {
            pubKeyDetails.setText(Lang.T(OnDealClick.resultMess(Transaction.INVALID_PUBLIC_KEY)));
            return;
        }

        // SHOW PubKey for BANK
        String personDetails = "+" + pubKey.getBase32() + "<br>";

        // SHOW account for FEE asset
        Tuple4<Long, Integer, Integer, Integer> addressDuration = pubKey.getPersonDuration(DCSet.getInstance());

        if (addressDuration == null) {
            personDetails += "<b>" + Lang.T("Account is valid for certification") + "</b>";
        } else {
            // TEST TIME and EXPIRE TIME
            long current_time = NTP.getTime();

            // TEST TIME and EXPIRE TIME
            int daysLeft = addressDuration.b - (int) (current_time / (long) 86400000);
            if (daysLeft < 0)
                personDetails += Lang.T("Personalize ended %days% ago").replace("%days%",
                        "" + daysLeft);
            else
                personDetails += Lang.T("Personalize is valid for %days% days").replace("%days%",
                        "" + daysLeft);

            personDetails += "<br>" + Lang.T("Person is still alive");

        }
        pubKeyDetails.setText("<html>" + personDetails + "<br>" + pubKey.toString(Transaction.FEE_KEY) + "</html>");

    }

    public void onGoClick(PersonCls person, JButton Button_Confirm, JComboBox<Account> jComboBox_YourAddress,
                          JTextField pubKey1Txt, JTextField pubKey2Txt, JTextField pubKey3Txt, JTextField toDateTxt,
                          JTextField feePowTxt) {

        if (!OnDealClick.proccess1(Button_Confirm))
            return;

        Account creator = (Account) jComboBox_YourAddress.getSelectedItem();
        // String address = pubKey1Txt.getText();
        int toDate = 0;
        int feePow = 0;
        int parse = 0;
        String toDateStr = toDateTxt.getText();
        try {

            // READ FEE POW
            feePow = Integer.parseInt(feePowTxt.getText());
        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee"),
                        Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
            }

            // ENABLE
            Button_Confirm.setEnabled(true);

            return;
        }

        Pair<Integer, Integer> toDateResult = ItemCls.resolveEndDayFromStr(toDateStr, BlockChain.DEFAULT_DURATION);
        if (toDateResult.getA() < 0) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid to Date"),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

            Button_Confirm.setEnabled(true);
            return;

        } else {
            toDate = toDateResult.getB();
        }

        List<PublicKeyAccount> certifiedPublicKeys = new ArrayList<PublicKeyAccount>();
        if (!pubKey1Txt.getText().isEmpty() && pubKey1Txt.getText().length() < 55) {
            PublicKeyAccount userAccount1 = new PublicKeyAccount(Base58.decode(pubKey1Txt.getText(), PublicKeyAccount.PUBLIC_KEY_LENGTH));
            if (userAccount1.isValid())
                certifiedPublicKeys.add(userAccount1);
        }
        if (!pubKey2Txt.getText().isEmpty() && pubKey2Txt.getText().length() < 55) {
            PublicKeyAccount userAccount2 = new PublicKeyAccount(Base58.decode(pubKey2Txt.getText(), PublicKeyAccount.PUBLIC_KEY_LENGTH));
            if (userAccount2.isValid())
                certifiedPublicKeys.add(userAccount2);
        }
        if (!pubKey3Txt.getText().isEmpty() && pubKey3Txt.getText().length() < 55) {
            PublicKeyAccount userAccount3 = new PublicKeyAccount(Base58.decode(pubKey3Txt.getText(), PublicKeyAccount.PUBLIC_KEY_LENGTH));
            if (userAccount3.isValid())
                certifiedPublicKeys.add(userAccount3);
        }

        if (certifiedPublicKeys.isEmpty()) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.T(OnDealClick.resultMess(Transaction.INVALID_PUBLIC_KEY)),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

            Button_Confirm.setEnabled(true);
            return;

        }

        // Account authenticator = new Account(address);
        PrivateKeyAccount authenticator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creator.getAddress());
        if (authenticator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        int version = 0; // without user signs

        Transaction transaction = Controller.getInstance().r_CertifyPubKeysPerson(version, Transaction.FOR_NETWORK, authenticator, feePow,
                person.getKey(), certifiedPublicKeys, toDate);

        String Status_text = "";
        IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                Lang.T("Certification of Account"), 0,
                0, Status_text, Lang.T("Confirmation Transaction"));
        CertifyPubKeysDetailsFrame ww = new CertifyPubKeysDetailsFrame((RCertifyPubKeys) transaction);
        confirmDialog.jScrollPane1.setViewportView(ww);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);

        // JOptionPane.OK_OPTION
        if (confirmDialog.isConfirm > 0) {
            if (ResultDialog.make(this, transaction, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE, null, null)) {
                dispose();
            }
        }
        // ENABLE
        Button_Confirm.setEnabled(true);

    }

    private void initComponents(PersonCls person, PublicKeyAccount publicKey) {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel_PersonInfo = new javax.swing.JScrollPane();
        jLabel_YourAddress = new javax.swing.JLabel();
        jComboBox_YourAddress = new javax.swing.JComboBox<>();
        jLabel_Address1 = new javax.swing.JLabel();
        jTextField_Address1 = new javax.swing.JTextField();
        jLabel_Address2 = new javax.swing.JLabel();
        jTextField_Address2 = new javax.swing.JTextField();
        jLabel_Address3 = new javax.swing.JLabel();
        jTextField_Address3 = new javax.swing.JTextField();
        jLabel_Adress1_Check = new javax.swing.JLabel();
        jLabel_Address2_Check = new javax.swing.JLabel();
        jLabel_Address3_Check = new javax.swing.JLabel();
        jLabel_addDays = new javax.swing.JLabel();
        jTextField_addDays = new javax.swing.JTextField();
        jLabel_Fee = new javax.swing.JLabel();
        jFormattedTextField_Fee = new javax.swing.JTextField();
        // jButton_Cansel = new javax.swing.JButton();
        // jButton_Confirm = new javax.swing.JButton();
        jLabel_addDays_Check = new javax.swing.JLabel();
        jLabel_Fee_Check = new javax.swing.JLabel();
        jLabel_Title = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        //layout.columnWidths = new int[]{0, 9, 0, 9, 0, 9, 0};
        //layout.rowHeights = new int[]{0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
        getContentPane().setLayout(layout);

        jLabel_PersonInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        JPanel info = new PersonInfo002(person, false);
        info.setFocusable(false);
        jLabel_PersonInfo.setViewportView(info);

        Insets insets = new Insets(3, 27, 3, 0);
        Insets insetsField = new Insets(3, 0, 3, 27);

        int gridy = 0;

        jLabel_YourAddress.setText(Lang.T("Your account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = insets;
        getContentPane().add(jLabel_YourAddress, gridBagConstraints);

        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());
        // jComboBox_YourAddress.setMinimumSize(new java.awt.Dimension(500,
        // 22));
        // jComboBox_YourAddress.setPreferredSize(new java.awt.Dimension(500,
        // 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = insetsField;
        getContentPane().add(jComboBox_YourAddress, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = insets;
        jLabel_Title.setText(Lang.T("Information about the person"));
        getContentPane().add(jLabel_Title, gridBagConstraints);

        // jLabel_PersonInfo.set
        // jLabel_PersonInfo.setText(Lang.T("Public Keys
        // of") + " " + person.viewName() +":");
        // jLabel_PersonInfo.setText(new
        // PersonInfo().Get_HTML_Person_Info_001(person) );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = insets;
        gridBagConstraints.insets = insetsField;
        getContentPane().add(jLabel_PersonInfo, gridBagConstraints);

        jLabel_Address1.setText(Lang.T("Public key") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;
        getContentPane().add(jLabel_Address1, gridBagConstraints);

        // jTextField_Address1.setMinimumSize(new java.awt.Dimension(300, 20));
        jTextField_Address1.setName(""); // NOI18N
        // jTextField_Address1.setPreferredSize(new java.awt.Dimension(300,
        // 20));
        // jTextField_Address1.setRequestFocusEnabled(false);
        jTextField_Address1.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address1, jLabel_Adress1_Check);
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address1, jLabel_Adress1_Check);
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address1, jLabel_Adress1_Check);
            }
        });

        if (publicKey == null || publicKey.isPerson()) {
            jLabel_Adress1_Check.setText(Lang.T("Insert Public Key"));
        } else {
            jTextField_Address1.setText(publicKey.getBase58());
            // refreshReceiverDetails(jTextField_Address1,
            // jLabel_Adress1_Check);
        }
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = insetsField;
        getContentPane().add(jTextField_Address1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.insets = insetsField;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jLabel_Adress1_Check, gridBagConstraints);

        jLabel_addDays.setText(Lang.T("Add active days") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = insets;

        jTextField_addDays.setToolTipText("");
        jTextField_addDays.setText("0"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = insets;

        jLabel_addDays_Check.setText("<html>'.' =2 " + Lang.T("year") + ",<br> '+' ="
                + Lang.T("MAX days") + ",<br> '-' =" + Lang.T("Unconfirmed")
                + "</HTML>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = insets;
        //getContentPane().add(jLabel_addDays_Check, gridBagConstraints);

        jLabel_Fee.setText(Lang.T("Fee Power") + ":");
        jLabel_Fee.setVisible(Gui.SHOW_FEE_POWER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = insets;
        getContentPane().add(jLabel_Fee, gridBagConstraints);

        jFormattedTextField_Fee.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jFormattedTextField_Fee.setText("0");
        jFormattedTextField_Fee.setVisible(Gui.SHOW_FEE_POWER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = insetsField;
        getContentPane().add(jFormattedTextField_Fee, gridBagConstraints);

        jButton_Cansel = new MButton(Lang.T("Cancel"), 2);
        jButton_Cansel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = ++gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = insetsField;
        getContentPane().add(jButton_Cansel, gridBagConstraints);

        jButton_Confirm = new MButton(Lang.T("Confirm"), 2);
        jButton_Confirm.setToolTipText("");
        jButton_Confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGoClick(person, jButton_Confirm, jComboBox_YourAddress, jTextField_Address1, jTextField_Address2,
                        jTextField_Address3, jTextField_addDays, jFormattedTextField_Fee);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = insetsField;
        getContentPane().add(jButton_Confirm, gridBagConstraints);

    }// <

}
