package org.erachain.gui.items.persons;

import org.erachain.api.ApiErrorFactory;
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

        this.setTitle(Lang.getInstance().translate("Certification of Account"));
        this.setResizable(true);
        this.setModal(true);

         setPreferredSize(MainFrame.getInstance().getPreferredSize());
        // PACK
        this.pack();
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        // MainFrame.this.add(comp, constraints).setFocusable(false);
    }

    private void refreshReceiverDetails(JTextField pubKeyTxt, JLabel pubKeyDetails) {
        String toValue = pubKeyTxt.getText();

        // CHECK IF RECIPIENT IS VALID ADDRESS
        boolean isValid = false;
        try {
            isValid = !toValue.isEmpty() && PublicKeyAccount.isValidPublicKey(toValue);
        } catch (Exception e) {
        }

        if (!isValid) {
            pubKeyDetails.setText(ApiErrorFactory.getInstance().messageError(Transaction.INVALID_ADDRESS));
            return;
        }

        PublicKeyAccount account = new PublicKeyAccount(toValue);
        // SHOW PubKey for BANK
        String personDetails = "+" + account.getBase32() + "<br>";

        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            pubKeyDetails.setText("<html>" + personDetails
                    + Lang.getInstance().translate("Status must be OK to show public key details.") + "</html>");
            return;
        }

        // SHOW account for FEE asset
        Tuple4<Long, Integer, Integer, Integer> addressDuration = account.getPersonDuration(DCSet.getInstance());

        if (addressDuration == null) {
            personDetails += "<b>" + Lang.getInstance().translate("Account is valid for certification") + "</b>";
        } else {
            // TEST TIME and EXPIRE TIME
            long current_time = NTP.getTime();

            // TEST TIME and EXPIRE TIME
            int daysLeft = addressDuration.b - (int) (current_time / (long) 86400000);
            if (daysLeft < 0)
                personDetails += Lang.getInstance().translate("Personalize ended %days% ago").replace("%days%",
                        "" + daysLeft);
            else
                personDetails += Lang.getInstance().translate("Personalize is valid for %days% days").replace("%days%",
                        "" + daysLeft);

            personDetails += "<br>" + Lang.getInstance().translate("Person is still alive");

        }
        pubKeyDetails.setText("<html>" + personDetails + "<br>" + account.toString(Transaction.FEE_KEY) + "</html>");

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
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
            }

            // ENABLE
            Button_Confirm.setEnabled(true);

            return;
        }

        Pair<Integer, Integer> toDateResult = ItemCls.resolveEndDayFromStr(toDateStr, BlockChain.DEFAULT_DURATION);
        if (toDateResult.getA() < 0) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid to Date"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            Button_Confirm.setEnabled(true);
            return;

        } else {
            toDate = toDateResult.getB();
        }

        List<PublicKeyAccount> certifiedPublicKeys = new ArrayList<PublicKeyAccount>();
        if (pubKey1Txt.getText().length() > 30) {
            PublicKeyAccount userAccount1 = new PublicKeyAccount(Base58.decode(pubKey1Txt.getText()));
            if (userAccount1.isValid())
                certifiedPublicKeys.add(userAccount1);
        }
        if (pubKey2Txt.getText().length() > 30) {
            PublicKeyAccount userAccount2 = new PublicKeyAccount(Base58.decode(pubKey2Txt.getText()));
            if (userAccount2.isValid())
                certifiedPublicKeys.add(userAccount2);
        }
        if (pubKey3Txt.getText().length() > 30) {
            PublicKeyAccount userAccount3 = new PublicKeyAccount(Base58.decode(pubKey3Txt.getText()));
            if (userAccount3.isValid())
                certifiedPublicKeys.add(userAccount3);
        }

        if (certifiedPublicKeys.isEmpty()) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Nothing to personalize"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            Button_Confirm.setEnabled(true);
            return;

        }

        // Account authenticator = new Account(address);
        PrivateKeyAccount authenticator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creator.getAddress());
        if (authenticator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        int version = 0; // without user signs

        Transaction transaction = Controller.getInstance().r_CertifyPubKeysPerson(version, Transaction.FOR_NETWORK, authenticator, feePow,
                person.getKey(), certifiedPublicKeys, toDate);

        String Status_text = "";
        IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                Lang.getInstance().translate("Certification of Account"), (int) (this.getWidth() / 1.2),
                (int) (this.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
        CertifyPubKeysDetailsFrame ww = new CertifyPubKeysDetailsFrame((RCertifyPubKeys) transaction);
        dd.jScrollPane1.setViewportView(ww);
        dd.setLocationRelativeTo(this);
        dd.pack();
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            Integer result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);

            // CHECK VALIDATE MESSAGE
            if (result == Transaction.VALIDATE_OK) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Public Key was Certified") + "!",
                        Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
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
        // setMinimumSize(new java.awt.Dimension(800, 600));
        setModal(true);
        // setPreferredSize(new java.awt.Dimension(800, 600));
        addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
                formAncestorMoved(evt);
            }

            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
            }
        });
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{0, 9, 0, 9, 0, 9, 0};
        layout.rowHeights = new int[]{0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
        getContentPane().setLayout(layout);

        jLabel_PersonInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        PersonInfo info = new PersonInfo();
        info.show_001(person);
        info.setFocusable(false);
        jLabel_PersonInfo.setViewportView(info);
        pack();

        // jLabel_PersonInfo.set
        // jLabel_PersonInfo.setText(Lang.getInstance().translate("Public Keys
        // of") + " " + person.viewName() +":");
        // jLabel_PersonInfo.setText(new
        // PersonInfo().Get_HTML_Person_Info_001(person) );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        // gridBagConstraints.insets = new java.awt.Insets(12, 9, 0, 9);
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 9);
        getContentPane().add(jLabel_PersonInfo, gridBagConstraints);

        jLabel_YourAddress.setText(Lang.getInstance().translate("Your account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        // gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        gridBagConstraints.insets = new java.awt.Insets(21, 27, 0, 0);
        getContentPane().add(jLabel_YourAddress, gridBagConstraints);

        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());
        // jComboBox_YourAddress.setMinimumSize(new java.awt.Dimension(500,
        // 22));
        // jComboBox_YourAddress.setPreferredSize(new java.awt.Dimension(500,
        // 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        // gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 13);
        gridBagConstraints.insets = new java.awt.Insets(21, 0, 0, 13);
        getContentPane().add(jComboBox_YourAddress, gridBagConstraints);

        jLabel_Address1.setText(Lang.getInstance().translate("Public key") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
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
            jLabel_Adress1_Check.setText(Lang.getInstance().translate("Insert Public Key"));
        } else {
            jTextField_Address1.setText(publicKey.getBase58());
            // refreshReceiverDetails(jTextField_Address1,
            // jLabel_Adress1_Check);
        }
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.weightx = 0.1;
        // gridBagConstraints.gridheight =7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jLabel_Adress1_Check, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jTextField_Address1, gridBagConstraints);

        jLabel_Address2.setText(Lang.getInstance().translate("Public Key") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        // getContentPane().add(jLabel_Address2, gridBagConstraints);

        jTextField_Address2.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address2, jLabel_Address2_Check);
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address2, jLabel_Address2_Check);
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address2, jLabel_Address2_Check);
            }
        });

        jLabel_Address2_Check.setText(Lang.getInstance().translate("insert second Public Key"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        // getContentPane().add(jLabel_Address2_Check, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        // getContentPane().add(jTextField_Address2, gridBagConstraints);

        jLabel_Address3.setText(Lang.getInstance().translate("Public Key") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        // getContentPane().add(jLabel_Address3, gridBagConstraints);

        jTextField_Address3.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address2, jLabel_Address2_Check);
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address3, jLabel_Address3_Check);
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                refreshReceiverDetails(jTextField_Address3, jLabel_Address3_Check);
            }
        });

        jLabel_Address3_Check.setText(Lang.getInstance().translate("insert next Public Key"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        // getContentPane().add(jLabel_Address3_Check, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        // getContentPane().add(jTextField_Address3, gridBagConstraints);

        jLabel_addDays.setText(Lang.getInstance().translate("Add Days") + ":");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;

        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        //getContentPane().add(jLabel_addDays, gridBagConstraints);

        /*
         * try { jFormattedTextField_ToDo.setFormatterFactory(new
         * javax.swing.text.DefaultFormatterFactory(new
         * javax.swing.text.MaskFormatter("##.##.####"))); } catch
         * (java.text.ParseException ex) { ex.printStackTrace(); }
         */
        // jTextField_addDays.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextField_addDays.setToolTipText("");
        // jTextField_addDays.setMinimumSize(new java.awt.Dimension(100, 20));
        jTextField_addDays.setText("0"); // NOI18N
        // jTextField_addDays.setPreferredSize(new java.awt.Dimension(100, 20));
        jTextField_addDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_ToDoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        //getContentPane().add(jTextField_addDays, gridBagConstraints);

        jLabel_Fee.setText(Lang.getInstance().translate("Fee Power") + ":");
        jLabel_Fee.setVisible(Gui.SHOW_FEE_POWER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_Fee, gridBagConstraints);

        // jFormattedTextField_Fee.setFormatterFactory(new
        // javax.swing.text.DefaultFormatterFactory(new
        // javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#"))));
        jFormattedTextField_Fee.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        // jFormattedTextField_Fee.setMinimumSize(new java.awt.Dimension(100,
        // 20));
        jFormattedTextField_Fee.setText("0");
        jFormattedTextField_Fee.setVisible(Gui.SHOW_FEE_POWER);
        // jFormattedTextField_Fee.setPreferredSize(new java.awt.Dimension(100,
        // 20));
        jFormattedTextField_Fee.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_FeeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;

        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        getContentPane().add(jFormattedTextField_Fee, gridBagConstraints);

        jButton_Cansel = new MButton(Lang.getInstance().translate("Cancel"), 2);
        jButton_Cansel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 29, 0);
        getContentPane().add(jButton_Cansel, gridBagConstraints);

        jButton_Confirm = new MButton(Lang.getInstance().translate("Confirm"), 2);
        jButton_Confirm.setToolTipText("");
        jButton_Confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGoClick(person, jButton_Confirm, jComboBox_YourAddress, jTextField_Address1, jTextField_Address2,
                        jTextField_Address3, jTextField_addDays, jFormattedTextField_Fee);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        getContentPane().add(jButton_Confirm, gridBagConstraints);

        jLabel_addDays_Check.setText("<html>'.' =2 " + Lang.getInstance().translate("year") + ",<br> '+' ="
                + Lang.getInstance().translate("MAX days") + ",<br> '-' =" + Lang.getInstance().translate("Unconfirmed")
                + "</HTML>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        getContentPane().add(jLabel_addDays_Check, gridBagConstraints);

        jLabel_Fee_Check.setText("0..6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        // getContentPane().add(jLabel_Fee_Check, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        // gridBagConstraints.insets = new java.awt.Insets(12, 9, 11, 9);
        gridBagConstraints.insets = new java.awt.Insets(12, 23, 0, 9);
        getContentPane().add(jLabel_Title, gridBagConstraints);
        jLabel_Title.setText(Lang.getInstance().translate("Information about the person"));
        getContentPane().add(jLabel_Title, gridBagConstraints);

        pack();
    }// <

    private void jFormattedTextField_ToDoActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }
    // private javax.swing.JEditorPane jLabel_PersonInfo;

    private void jTextField_Address2ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void jButton_CanselActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:

    }

    private void jButton_ConfirmActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void jTextField_Address1ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void jFormattedTextField_FeeActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void formAncestorMoved(java.awt.event.HierarchyEvent evt) {
        // TODO add your handling code here:
    }

    private void jTextField_Address3ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }
    // End of variables declaration

}
