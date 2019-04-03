package org.erachain.gui.records;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RVouch;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.gui.transaction.VouchingDetailsFrame;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

//import org.erachain.gui.*;

public class VouchRecordDialog extends JDialog {

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
    public VouchRecordDialog(Integer block_No, Integer rec_No, Account account) {
        super();
        vouch(block_No, rec_No, account);

    }
    //private javax.swing.JLabel jLabel_RecordInfo;

    public VouchRecordDialog(Integer block_No, Integer rec_No) {
        super();
        vouch(block_No, rec_No, null);


    }

    private void vouch(Integer block_No, Integer rec_No, Account account) {
        //ICON

        this.account = account;
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        initComponents();
        if (block_No != null && rec_No != null) {
            jTextField_recordID.setText(block_No.toString() + "-" + rec_No.toString());
            VouchRecordDialog.record = refreshRecordDetails(jTextField_recordID.getText());
            jTextField_recordID.enable(false);


        }
        //	setSize(400,300);
        this.setTitle(Lang.getInstance().translate("Vouch Record"));
        this.setResizable(true);
        this.setModal(true);

//	    setPreferredSize(new Dimension(MainFrame.getInstance().desktopPane.getWidth()-100,MainFrame.getInstance().desktopPane.getHeight()-100));
        //PACK
        this.setPreferredSize(MainFrame.getInstance().getPreferredSize());
        this.pack();
        //       this.setResizable(false);
      //  this.setSize(MainFrame.getInstance().getWidth() - 100, MainFrame.getInstance().getHeight() - 100);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        //MainFrame.this.add(comp, constraints).setFocusable(false);
    }

    //private Transaction refreshRecordDetails(JTextField recordTxt, JLabel recordDetails)
    private Transaction refreshRecordDetails(String text) {

		/*
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			infoPanel.show_mess(Lang.getInstance().translate("Status must be OK to show public key details."));
	        jLabel_RecordInfo.setViewportView(infoPanel);
			return null;
		}
		*/

        Transaction record = null;
        if (text.length() < 40) {
            //record = RVouch.getVouchingRecord(DLSet.getInstance(), jTextField_recordID.getText());
            record = DCSet.getInstance().getTransactionFinalMap().getRecord(text);
        } else {
            record = Transaction.findByDBRef(DCSet.getInstance(), Base58.decode(text));
        }

        if (record == null) {
            //	infoPanel.show_mess(Lang.getInstance().translate("Error - use signature of record or blockNo-recNo"));
            //    jLabel_RecordInfo.setViewportView(infoPanel);
            return record;
        }

        //ENABLE
        jButton_Confirm.setEnabled(true);

        JPanel infoPanel = TransactionDetailsFactory.getInstance().createTransactionDetail(record);
        jLabel_Name_Records.setText(Lang.getInstance().translate(record.viewTypeName()));
        //	infoPanel.show_001(record);
        //infoPanel.setFocusable(false);
        jLabel_RecordInfo.setViewportView(infoPanel);

        return record;
    }

    public void onGoClick()
    //JComboBox<Account> jComboBox_YourAddress, JTextField feePowTxt)
    {

        if (!OnDealClick.proccess1(jButton_Confirm)) return;

        Account creator = (Account) jComboBox_YourAddress.getSelectedItem();
        //String address = pubKey1Txt.getText();
        int feePow = 0;
        int parse = 0;
        try {

            //READ FEE POW
            feePow = Integer.parseInt(jFormattedTextField_Fee.getText());
        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
            }

            //ENABLE
            jButton_Confirm.setEnabled(true);

            return;
        }

        //Account authenticator =  new Account(address);
        PrivateKeyAccount authenticator = Controller.getInstance().getPrivateKeyAccountByAddress(creator.getAddress());

        int version = 0; // without user signs

        Transaction transaction = Controller.getInstance().r_Vouch(0, Transaction.FOR_NETWORK,
                authenticator, feePow,
                record.getBlockHeight(), record.getSeqNo());
        //Pair<Transaction, Integer> result = new Pair<Transaction, Integer>(null, 0);

        String Status_text = "";
        IssueConfirmDialog dd = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                Lang.getInstance().translate("Send Mail"), (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text, Lang.getInstance().translate("Confirmation Transaction"));
        //Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((RSend) transaction);
        VouchingDetailsFrame ww = new VouchingDetailsFrame((RVouch) transaction);

        dd.jScrollPane1.setViewportView(ww);
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        //	JOptionPane.OK_OPTION
        if (dd.isConfirm) {


            Integer result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, Transaction.FOR_NETWORK);


            //CHECK VALIDATE MESSAGE
            if (result == Transaction.VALIDATE_OK) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Record has been certified") + "!", Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {

                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }
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

        jLabel_recordID.setText(Lang.getInstance().translate("BlockNo-recNo or signature") + ":");
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
        jTextField_recordID.setToolTipText(Lang.getInstance().translate("BlockNo-recNo or signature"));
        //   jTextField_recordID.setMinimumSize(new java.awt.Dimension(300, 20));
        jTextField_recordID.setText(""); // NOI18N
        //    jTextField_recordID.setPreferredSize(new java.awt.Dimension(300, 20));
        jTextField_recordID.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                VouchRecordDialog.record = refreshRecordDetails(jTextField_recordID.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                VouchRecordDialog.record = refreshRecordDetails(jTextField_recordID.getText());
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

        jLabel_YourAddress.setText(Lang.getInstance().translate("Your account") + ":");
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


        jLabel_Fee.setText(Lang.getInstance().translate("Fee Power") + ":");
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
        //    jFormattedTextField_Fee.setPreferredSize(new java.awt.Dimension(100, 20));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = gridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        getContentPane().add(jFormattedTextField_Fee, gridBagConstraints);

        jButton_Cansel = new MButton(Lang.getInstance().translate("Cancel"), 2);
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

        jButton_Confirm = new MButton(Lang.getInstance().translate("Confirm"), 2);
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
        //      getContentPane().add(jLabel_Title, gridBagConstraints);
        jLabel_Title.setText(Lang.getInstance().translate("Information about the record"));
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
        //      getContentPane().add(jLabel_Title, gridBagConstraints);

        getContentPane().add(jLabel_Name_Records, gridBagConstraints);


        //    pack();
    }// <
    // End of variables declaration

}
