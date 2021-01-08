package org.erachain.gui.items.unions;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.items.statuses.ComboBoxStatusesModel;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class UnionSetStatusDialog extends JDialog {

    private JComboBox<Account> accountLBox;
    private JComboBox<StatusCls> statusLBox;
    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify
    private javax.swing.JButton jButton_Cansel;
    private javax.swing.JButton jButton_SetStatus;
    private JComboBox<ItemCls> jComboBox_Status;
    private JComboBox<Account> jComboBox_YourAddress;
    private javax.swing.JFormattedTextField jFormattedTextField_Fee;
    private javax.swing.JFormattedTextField jFormattedTextField_ToDo;
    private javax.swing.JLabel jLabel_Fee;
    private javax.swing.JLabel jLabel_Fee_Check;
    private javax.swing.JScrollPane jLabel_UnionInfo;
    private javax.swing.JLabel jLabel_Status;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JLabel jLabel_ToDo;
    private javax.swing.JLabel jLabel_ToDo_Check;
    private javax.swing.JLabel jLabel_YourAddress;
    public UnionSetStatusDialog(JComponent apers, UnionCls union) {
        super();


        initComponents(union);

        this.setModal(true);
        this.setTitle(Lang.T("Union set status"));

        setPreferredSize(new Dimension(400, 600));
        //PACK
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    public void onGoClick(
            UnionCls union, JButton Button_Confirm,
            JTextField pubKey1Txt, JTextField pubKey2Txt, JTextField pubKey3Txt, JTextField toDateTxt, JTextField feePowTxt) {

        if (!OnDealClick.proccess1(Button_Confirm)) return;

        Account creatorAccount = (Account) this.accountLBox.getSelectedItem();
        int parse = 0;
        String toDateStr = toDateTxt.getText();
        try {

            //READ to DAY
            parse++;
            if (toDateStr.length() > 0) {
            }
        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid to Date"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }

        List<PublicKeyAccount> certifiedPublicKeys = new ArrayList<PublicKeyAccount>();
        if (pubKey1Txt.getText().length() == 30) {
            PublicKeyAccount userAccount1 = new PublicKeyAccount(Base58.decode(pubKey1Txt.getText()));
            if (userAccount1.isValid()) certifiedPublicKeys.add(userAccount1);
        }
        if (pubKey2Txt.getText().length() > 30) {
            PublicKeyAccount userAccount2 = new PublicKeyAccount(Base58.decode(pubKey2Txt.getText()));
            if (userAccount2.isValid()) certifiedPublicKeys.add(userAccount2);
        }
        if (pubKey3Txt.getText().length() > 30) {
            PublicKeyAccount userAccount3 = new PublicKeyAccount(Base58.decode(pubKey3Txt.getText()));
            if (userAccount3.isValid()) certifiedPublicKeys.add(userAccount3);
        }

        //Account authenticator =  new Account(address);
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creatorAccount.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        Pair<Transaction, Integer> result = null;

        //CHECK VALIDATE MESSAGE
        if (result.getB() == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.T("Union has been authenticated!"), Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } else {

            JOptionPane.showMessageDialog(new JFrame(), Lang.T(OnDealClick.resultMess(result.getB())), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
        }

        //ENABLE
        Button_Confirm.setEnabled(true);

    }

    private void initComponents(UnionCls union) {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel_UnionInfo = new javax.swing.JScrollPane();
        jLabel_YourAddress = new javax.swing.JLabel();
        jComboBox_YourAddress = new javax.swing.JComboBox<>();
        jLabel_Status = new javax.swing.JLabel();
        jLabel_ToDo = new javax.swing.JLabel();
        jFormattedTextField_ToDo = new javax.swing.JFormattedTextField();
        jLabel_Fee = new javax.swing.JLabel();
        jFormattedTextField_Fee = new javax.swing.JFormattedTextField();
        jButton_Cansel = new javax.swing.JButton();
        jButton_SetStatus = new javax.swing.JButton();
        jLabel_ToDo_Check = new javax.swing.JLabel();
        jLabel_Fee_Check = new javax.swing.JLabel();
        jLabel_Title = new javax.swing.JLabel();
        jComboBox_Status = new javax.swing.JComboBox<>();

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
        layout.columnWidths = new int[]{0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
        layout.rowHeights = new int[]{0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
        getContentPane().setLayout(layout);

        jLabel_UnionInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        UnionInfo info = new UnionInfo();
        info.show_Union_001(union);
        info.setFocusable(false);
        jLabel_UnionInfo.setViewportView(info);
        //     jLabel_UnionInfo.setText(new UnionInfo().Get_HTML_Union_Info_001(union) );
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 9);
        getContentPane().add(jLabel_UnionInfo, gridBagConstraints);

        jLabel_YourAddress.setText(Lang.T("Your account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(21, 27, 0, 0);
        getContentPane().add(jLabel_YourAddress, gridBagConstraints);

        // jComboBox_YourAddress.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());
        jComboBox_YourAddress.setMinimumSize(new java.awt.Dimension(500, 22));
        jComboBox_YourAddress.setPreferredSize(new java.awt.Dimension(500, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(21, 0, 0, 13);
        getContentPane().add(jComboBox_YourAddress, gridBagConstraints);

        jLabel_Status.setText(Lang.T("Status") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_Status, gridBagConstraints);

        jLabel_ToDo.setText(Lang.T("To Do:"));
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

        jLabel_Fee.setText(Lang.T("Fee Power") + ":");
        jLabel_Fee.setVisible(Gui.SHOW_FEE_POWER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_Fee, gridBagConstraints);

        jFormattedTextField_Fee.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("####,###.00"))));
        jFormattedTextField_Fee.setVisible(Gui.SHOW_FEE_POWER);
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

        jButton_Cansel.setText(Lang.T("Cancel"));
        jButton_Cansel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 29, 0);
        getContentPane().add(jButton_Cansel, gridBagConstraints);

        jButton_SetStatus.setText(Lang.T("Set status"));
        jButton_SetStatus.setToolTipText("");
        jButton_SetStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //   	onGoClick(union,  jButton_SetStatus, pubKey1Txt, pubKey1Txt, pubKey1Txt, toDate, feePow);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        getContentPane().add(jButton_SetStatus, gridBagConstraints);

        jLabel_ToDo_Check.setText(Lang.T("insert date"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jLabel_ToDo_Check, gridBagConstraints);

        jLabel_Fee_Check.setText(Lang.T("insert fee"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        getContentPane().add(jLabel_Fee_Check, gridBagConstraints);

        jLabel_Title.setText(Lang.T("Information about the union"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 23, 0, 9);
        getContentPane().add(jLabel_Title, gridBagConstraints);

        //jComboBox_Status.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_Status = new JComboBox(new ComboBoxStatusesModel());
        jComboBox_Status.setMinimumSize(new java.awt.Dimension(400, 22));
        jComboBox_Status.setPreferredSize(new java.awt.Dimension(400, 22));
        jComboBox_Status.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_StatusActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        getContentPane().add(jComboBox_Status, gridBagConstraints);

        pack();
    }// </editor-fold>

    private void jFormattedTextField_ToDoActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void jButton_CanselActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void jButton_ConfirmActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void jFormattedTextField_FeeActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void formAncestorMoved(java.awt.event.HierarchyEvent evt) {
        // TODO add your handling code here:
    }

    private void jComboBox_StatusActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }
    // End of variables declaration


}
