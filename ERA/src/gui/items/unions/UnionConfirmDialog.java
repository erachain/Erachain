package gui.items.unions;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.item.unions.UnionCls;
import core.transaction.Transaction;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import utils.Pair;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class UnionConfirmDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JComboBox<Account> accountLBox;
    // Variables declaration - do not modify
    private javax.swing.JButton jButton_Cansel;
    private javax.swing.JButton jButton_Confirm;
    private JComboBox<Account> jComboBox_YourAddress;
    private javax.swing.JFormattedTextField jFormattedTextField_Fee;
    private javax.swing.JFormattedTextField jFormattedTextField_ToDo;
    private javax.swing.JLabel jLabel_Address1;
    private javax.swing.JLabel jLabel_Address2;
    private javax.swing.JLabel jLabel_Address2_Check;
    private javax.swing.JLabel jLabel_Address3;
    private javax.swing.JLabel jLabel_Address3_Check;
    private javax.swing.JLabel jLabel_Adress1_Check;
    private javax.swing.JLabel jLabel_Fee;
    private javax.swing.JLabel jLabel_Fee_Check;
    // private javax.swing.JLabel jLabel_UnionInfo;
    private javax.swing.JScrollPane jLabel_UnionInfo;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JLabel jLabel_ToDo;
    private javax.swing.JLabel jLabel_ToDo_Check;
    private javax.swing.JLabel jLabel_YourAddress;
    private javax.swing.JTextField jTextField_Address1;
    private javax.swing.JTextField jTextField_Address2;
    private javax.swing.JTextField jTextField_Address3;
    public UnionConfirmDialog(JComponent apers, UnionCls union) {
        super();

        initComponents(union);

        this.setModal(true);
        setSize(400, 300);
        this.setTitle(Lang.getInstance().translate("Union confirm"));
        this.setResizable(true);
        setPreferredSize(new Dimension(500, 600));
//PACK
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    private void refreshReceiverDetails(JTextField pubKeyTxt, JLabel pubKeyDetails) {
        String toValue = pubKeyTxt.getText();

        if (toValue.isEmpty()) {
            pubKeyDetails.setText("");
            return;
        }

        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            pubKeyDetails.setText(Lang.getInstance().translate("Status must be OK to show public key details."));
            return;
        }

        //CHECK IF RECIPIENT IS VALID ADDRESS
        boolean isValid = false;
        try {
            isValid = PublicKeyAccount.isValidPublicKey(toValue);
        } catch (Exception e) {

        }

    }

    @SuppressWarnings("null")
    public void onGoClick(
            UnionCls union, JButton Button_Confirm,
            JTextField pubKey1Txt, JTextField pubKey2Txt, JTextField pubKey3Txt, JTextField toDateTxt, JTextField feePowTxt) {

        if (!OnDealClick.proccess1(Button_Confirm)) return;

        Account creator = (Account) this.accountLBox.getSelectedItem();
        int parse = 0;
        String toDateStr = toDateTxt.getText();
        try {

            //READ to DAY
            parse++;
            if (toDateStr.length() > 0) {
            }
        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid to Date"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }

        List<PublicKeyAccount> sertifiedPublicKeys = new ArrayList<PublicKeyAccount>();
        if (pubKey1Txt.getText().length() == 30) {
            PublicKeyAccount userAccount1 = new PublicKeyAccount(Base58.decode(pubKey1Txt.getText()));
            if (userAccount1.isValid()) sertifiedPublicKeys.add(userAccount1);
        }
        if (pubKey2Txt.getText().length() > 30) {
            PublicKeyAccount userAccount2 = new PublicKeyAccount(Base58.decode(pubKey2Txt.getText()));
            if (userAccount2.isValid()) sertifiedPublicKeys.add(userAccount2);
        }
        if (pubKey3Txt.getText().length() > 30) {
            PublicKeyAccount userAccount3 = new PublicKeyAccount(Base58.decode(pubKey3Txt.getText()));
            if (userAccount3.isValid()) sertifiedPublicKeys.add(userAccount3);
        }

        //Account authenticator =  new Account(address);
        PrivateKeyAccount authenticator = Controller.getInstance().getPrivateKeyAccountByAddress(creator.getAddress());

        Pair<Transaction, Integer> result = null;
        //Controller.getInstance().r_SertifyUnion(version, false, authenticator,
        //		feePow, union.getKey(),
        //		sertifiedPublicKeys, toDate);

        //CHECK VALIDATE MESSAGE
        if (result.getB() == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Union has been authenticated!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } else {

            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result.getB())), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        //ENABLE
        Button_Confirm.setEnabled(true);

    }

    private void initComponents(UnionCls union) {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel_UnionInfo = new javax.swing.JScrollPane();
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
        jLabel_ToDo = new javax.swing.JLabel();
        jFormattedTextField_ToDo = new javax.swing.JFormattedTextField();
        jLabel_Fee = new javax.swing.JLabel();
        jFormattedTextField_Fee = new javax.swing.JFormattedTextField();
        jButton_Cansel = new javax.swing.JButton();
        jButton_Confirm = new javax.swing.JButton();
        jLabel_ToDo_Check = new javax.swing.JLabel();
        jLabel_Fee_Check = new javax.swing.JLabel();
        jLabel_Title = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(650, 23));
        setModal(true);
        setPreferredSize(new java.awt.Dimension(700, 600));
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

        jLabel_UnionInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        Union_Info info = new Union_Info();
        info.show_Union_001(union);
        info.setFocusable(false);
        jLabel_UnionInfo.setViewportView(info);
        // jLabel_UnionInfo.set
        //   jLabel_UnionInfo.setText(Lang.getInstance().translate("Public Keys of") + " " + union.viewName() +":");
        //     jLabel_UnionInfo.setText(new Union_Info().Get_HTML_Union_Info_001(union) );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        //    gridBagConstraints.insets = new java.awt.Insets(12, 9, 0, 9);
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 9);
        getContentPane().add(jLabel_UnionInfo, gridBagConstraints);

        jLabel_YourAddress.setText(Lang.getInstance().translate("Your account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        // gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        gridBagConstraints.insets = new java.awt.Insets(21, 27, 0, 0);
        getContentPane().add(jLabel_YourAddress, gridBagConstraints);

        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());
        jComboBox_YourAddress.setMinimumSize(new java.awt.Dimension(500, 22));
        jComboBox_YourAddress.setPreferredSize(new java.awt.Dimension(500, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        // gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 13);
        gridBagConstraints.insets = new java.awt.Insets(21, 0, 0, 13);
        getContentPane().add(jComboBox_YourAddress, gridBagConstraints);

        jLabel_Address1.setText(Lang.getInstance().translate("Account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_Address1, gridBagConstraints);

        jTextField_Address1.setMinimumSize(new java.awt.Dimension(300, 20));
        jTextField_Address1.setName(""); // NOI18N
        jTextField_Address1.setPreferredSize(new java.awt.Dimension(300, 20));
        //  jTextField_Address1.setRequestFocusEnabled(false);
        jTextField_Address1.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
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

        jLabel_Adress1_Check.setText(Lang.getInstance().translate("Insert firsr Account"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jLabel_Adress1_Check, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jTextField_Address1, gridBagConstraints);

        jLabel_Address2.setText(Lang.getInstance().translate("Account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_Address2, gridBagConstraints);

        jTextField_Address2.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
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

        jLabel_Address2_Check.setText(Lang.getInstance().translate("insert second Addres"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jLabel_Address2_Check, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jTextField_Address2, gridBagConstraints);


        jLabel_Address3.setText(Lang.getInstance().translate("Account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_Address3, gridBagConstraints);

        jTextField_Address3.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
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

        jLabel_Address3_Check.setText(Lang.getInstance().translate("insert next Account"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jLabel_Address3_Check, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jTextField_Address3, gridBagConstraints);


        jLabel_ToDo.setText(Lang.getInstance().translate("To date") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_ToDo, gridBagConstraints);

        try {
            jFormattedTextField_ToDo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.##.####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        jFormattedTextField_ToDo.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jFormattedTextField_ToDo.setToolTipText("");
        jFormattedTextField_ToDo.setMinimumSize(new java.awt.Dimension(100, 20));
        jFormattedTextField_ToDo.setName(""); // NOI18N
        jFormattedTextField_ToDo.setPreferredSize(new java.awt.Dimension(100, 20));
        jFormattedTextField_ToDo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_ToDoActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jFormattedTextField_ToDo, gridBagConstraints);

        jLabel_Fee.setText(Lang.getInstance().translate("Fee Power") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_Fee, gridBagConstraints);

        jFormattedTextField_Fee.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("####,###.00"))));
        jFormattedTextField_Fee.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jFormattedTextField_Fee.setMinimumSize(new java.awt.Dimension(100, 20));
        jFormattedTextField_Fee.setPreferredSize(new java.awt.Dimension(100, 20));
        jFormattedTextField_Fee.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextField_FeeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 48, 0);
        getContentPane().add(jFormattedTextField_Fee, gridBagConstraints);

        jButton_Cansel.setText(Lang.getInstance().translate("Cancel"));
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

        jButton_Confirm.setText(Lang.getInstance().translate("Confirm"));
        jButton_Confirm.setToolTipText("");
        jButton_Confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGoClick(union, jButton_Confirm, jTextField_Address1, jTextField_Address2, jTextField_Address3, jFormattedTextField_ToDo, jFormattedTextField_Fee);
            }
        });


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        getContentPane().add(jButton_Confirm, gridBagConstraints);

        jLabel_ToDo_Check.setText("insert date");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jLabel_ToDo_Check, gridBagConstraints);

        jLabel_Fee_Check.setText("insert fee");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        getContentPane().add(jLabel_Fee_Check, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        //    gridBagConstraints.insets = new java.awt.Insets(12, 9, 11, 9);
        gridBagConstraints.insets = new java.awt.Insets(12, 23, 0, 9);
        getContentPane().add(jLabel_Title, gridBagConstraints);
        jLabel_Title.setText(Lang.getInstance().translate("Information about the union"));
        getContentPane().add(jLabel_Title, gridBagConstraints);

        pack();
    }// <

    private void jFormattedTextField_ToDoActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }
    //    private javax.swing.JEditorPane jLabel_UnionInfo;

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
