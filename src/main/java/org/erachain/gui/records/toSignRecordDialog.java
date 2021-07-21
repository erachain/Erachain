package org.erachain.gui.records;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.RVouch;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.Gui;
import org.erachain.gui.MainFrame;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.gui.transaction.SigningDetailsFrame;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class toSignRecordDialog extends JDialog {

    private static final long serialVersionUID = 2717571093561259483L;

    private static Transaction record;
    Account account;

    // Variables declaration - do not modify
    private MButton jButton_Cansel;
    private MButton jButton_Confirm;
    private JComboBox<Account> jComboBox_YourAddress;
    private javax.swing.JLabel jLabel_Fee;
    private javax.swing.JTextField jFormattedTextField_Fee;
    private javax.swing.JLabel jLabel_Fee_Check;
    private javax.swing.JScrollPane jLabel_RecordInfo;
    private javax.swing.JLabel jLabel_recordID;
    private javax.swing.JTextField jTextField_recordID;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JLabel jLabel_Name_Records;
    private javax.swing.JLabel jLabel_YourAddress;

    public toSignRecordDialog(Integer block_No, Integer rec_No, Account account) {
        toSign(block_No, rec_No, account);
    }

    public toSignRecordDialog(Integer block_No, Integer rec_No) {
        toSign(block_No, rec_No, null);
    }

    private void toSign(Integer block_No, Integer rec_No, Account account) {
        //ICON

        this.account = account;
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        initComponents();
        setPreferredSize(new Dimension(1000, 600));
        //setMinimumSize(new Dimension(1000, 600));
        //setMaximumSize(new Dimension(1000, 600));


        if (block_No != null && rec_No != null) {
            jTextField_recordID.setText(block_No.toString() + "-" + rec_No.toString());
            toSignRecordDialog.record = refreshRecordDetails(jTextField_recordID.getText());
            jTextField_recordID.setEnabled(false);
        }

        this.setTitle(Lang.T("To sign Record"));
        this.setResizable(true);
        this.setModal(true);

        //PACK
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    //private Transaction refreshRecordDetails(JTextField recordTxt, JLabel recordDetails)
    private Transaction refreshRecordDetails(String text) {

        Transaction record;
        if (text.length() < 40) {
            //record = RVouch.getVouchingRecord(DLSet.getInstance(), jTextField_recordID.getText());
            record = DCSet.getInstance().getTransactionFinalMap().getRecord(text);
        } else {
            record = Transaction.findByDBRef(DCSet.getInstance(), Base58.decode(text, Crypto.SIGNATURE_LENGTH));
        }

        if (record == null) {
            jLabel_Title.setText(Lang.T("Error - use signature of record or blockNo-recNo"));
            return record;
        }

        //ENABLE
        jButton_Confirm.setEnabled(true);

        JPanel infoPanel = TransactionDetailsFactory.getInstance().createTransactionDetail(record);
        jLabel_Name_Records.setText(Lang.T(record.viewFullTypeName()));
        //	infoPanel.show_001(record);
        //infoPanel.setFocusable(false);
        jLabel_RecordInfo.setViewportView(infoPanel);

        return record;
    }

    public void onGoClick()
    //JComboBox<Account> jComboBox_YourAddress, JTextField feePowTxt)
    {

        if (!OnDealClick.proccess1(jButton_Confirm)) return;

        Account creatorAccount = (Account) jComboBox_YourAddress.getSelectedItem();
        //String address = pubKey1Txt.getText();
        int feePow = 0;
        int parse = 0;
        try {

            //READ FEE POW
            feePow = Integer.parseInt(jFormattedTextField_Fee.getText());
        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
            }

            //ENABLE
            jButton_Confirm.setEnabled(true);

            return;
        }

        //Account authenticator =  new Account(address);
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(creatorAccount.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        int version = 0; // without user signs

        Transaction transaction = Controller.getInstance().r_Vouch(0, Transaction.FOR_NETWORK,
                creator, feePow,
                record.getBlockHeight(), record.getSeqNo());
        //Pair<Transaction, Integer> result = new Pair<Transaction, Integer>(null, 0);

        String Status_text = "";
        IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                Lang.T("To sign"), (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text, Lang.T("Confirmation Transaction"));
        //Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((RSend) transaction);
        SigningDetailsFrame ww = new SigningDetailsFrame((RVouch) transaction);

        confirmDialog.jScrollPane1.setViewportView(ww);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);

        //	JOptionPane.OK_OPTION
        if (confirmDialog.isConfirm > 0) {
            ResultDialog.make(this, transaction, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE, null);
        }
        //ENABLE
        jButton_Confirm.setEnabled(true);

    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel_RecordInfo = new javax.swing.JScrollPane();
        //jLabel_RecordInfo = new javax.swing.JLabel();
        jLabel_YourAddress = new javax.swing.JLabel();
        //jComboBox_YourAddress = new javax.swing.JComboBox<>();

        jLabel_recordID = new javax.swing.JLabel();
        jTextField_recordID = new javax.swing.JTextField();

        jLabel_Fee = new javax.swing.JLabel();
        jFormattedTextField_Fee = new javax.swing.JTextField();
        //     jButton_Cansel = new javax.swing.JButton();
        //      jButton_Confirm = new javax.swing.JButton();
        jLabel_Fee_Check = new javax.swing.JLabel();
        jLabel_Title = new javax.swing.JLabel();
        jLabel_Name_Records = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        //    setMinimumSize(new java.awt.Dimension(650, 23));
        setModal(true);
        //    setPreferredSize(new java.awt.Dimension(700, 600));
        addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
                //formAncestorMoved(evt);
            }

            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
            }
        });
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{0, 9, 0, 9, 0, 9, 0};
        layout.rowHeights = new int[]{0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0, 9, 0};
        getContentPane().setLayout(layout);

        jLabel_recordID.setText(Lang.T("BlockNo-recNo or signature") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_recordID, gridBagConstraints);

	        /*
	        try {
	            jFormattedTextField_ToDo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.##.####")));
	        } catch (java.text.ParseException ex) {
	            ex.printStackTrace();
	        }
	        */
        //  jTextField_recordID.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextField_recordID.setToolTipText(Lang.T("BlockNo-recNo or signature"));
        //   jTextField_recordID.setMinimumSize(new java.awt.Dimension(300, 20));
        jTextField_recordID.setText(""); // NOI18N
        //    jTextField_recordID.setPreferredSize(new java.awt.Dimension(300, 20));
        jTextField_recordID.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                toSignRecordDialog.record = refreshRecordDetails(jTextField_recordID.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                toSignRecordDialog.record = refreshRecordDetails(jTextField_recordID.getText());
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        getContentPane().add(jTextField_recordID, gridBagConstraints);


        jLabel_RecordInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        //   infoPanel = new RecordInfo();

        //info.show_001(record);
        //infoPanel.setFocusable(false);
        //jLabel_RecordInfo.setViewportView(infoPanel);

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
        getContentPane().add(jLabel_RecordInfo, gridBagConstraints);

        jLabel_YourAddress.setText(Lang.T("Your account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        // gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        gridBagConstraints.insets = new java.awt.Insets(21, 27, 0, 0);
        getContentPane().add(jLabel_YourAddress, gridBagConstraints);

        //AccountsComboBoxModel
        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());
        //   jComboBox_YourAddress.setMinimumSize(new java.awt.Dimension(500, 22));
        //    jComboBox_YourAddress.setPreferredSize(new java.awt.Dimension(500, 22));
        if (account != null) jComboBox_YourAddress.setSelectedItem(account);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        // gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 13);
        gridBagConstraints.insets = new java.awt.Insets(21, 0, 0, 13);
        getContentPane().add(jComboBox_YourAddress, gridBagConstraints);


        jLabel_Fee.setText(Lang.T("Fee Power") + ":");
        jLabel_Fee.setVisible(Gui.SHOW_FEE_POWER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_Fee, gridBagConstraints);

        //  jFormattedTextField_Fee.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#"))));
        //   jFormattedTextField_Fee.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        //    jFormattedTextField_Fee.setMinimumSize(new java.awt.Dimension(100, 20));
        jFormattedTextField_Fee.setText("0");
        jFormattedTextField_Fee.setVisible(Gui.SHOW_FEE_POWER);
        //    jFormattedTextField_Fee.setPreferredSize(new java.awt.Dimension(100, 20));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = gridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        getContentPane().add(jFormattedTextField_Fee, gridBagConstraints);

        jButton_Cansel = new MButton(Lang.T("Cancel"), 2);
        jButton_Cansel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dispose();
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 29, 0);
        getContentPane().add(jButton_Cansel, gridBagConstraints);

        jButton_Confirm = new MButton(Lang.T("Confirm"), 2);
        jButton_Confirm.setToolTipText("");
        jButton_Confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGoClick();
            }
        });


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        getContentPane().add(jButton_Confirm, gridBagConstraints);

        jLabel_Fee_Check.setText("0..6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 17;
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
        //      getContentPane().add(jLabelTitle, gridBagConstraints);
        jLabel_Title.setText(Lang.T("Information about the record"));
        getContentPane().add(jLabel_Title, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        //    gridBagConstraints.insets = new java.awt.Insets(12, 9, 11, 9);
        gridBagConstraints.insets = new java.awt.Insets(12, 23, 0, 9);
        //      getContentPane().add(jLabelTitle, gridBagConstraints);

        getContentPane().add(jLabel_Name_Records, gridBagConstraints);

        pack();
    }

}
