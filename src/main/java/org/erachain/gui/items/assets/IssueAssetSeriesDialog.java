package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RVouch;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.MainFrame;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MButton;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.gui.transaction.SigningDetailsFrame;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class IssueAssetSeriesDialog extends JDialog {

    private static final long serialVersionUID = 2717571093561259483L;

    private static Transaction record;
    Account account;

    // Variables declaration - do not modify
    private MButton jButton_Cansel;
    private MButton jButton_Confirm;
    private JComboBox<Account> jComboBox_YourAddress;
    private JLabel jLabel_Fee;
    private JTextField jFormattedTextField_Fee;
    private JLabel jLabel_Fee_Check;
    private JScrollPane jLabel_RecordInfo;
    private JLabel jLabel_Title;
    private JLabel jLabel_Name_Records;
    private JLabel jLabel_YourAddress;

    public IssueAssetSeriesDialog(AssetCls origAsset, Account account) {
        toSign(origAsset, account);
    }

    public IssueAssetSeriesDialog(AssetCls origAsset) {
        toSign(origAsset, null);
    }

    private void toSign(AssetCls origAsset, Account account) {
        //ICON

        this.account = account;
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        initComponents();
        setPreferredSize(new Dimension(0, 600));
        //setMinimumSize(new Dimension(1000, 600));
        //setMaximumSize(new Dimension(1000, 600));

        IssueAssetSeriesDialog.record = refreshRecordDetails(origAsset);

        this.setTitle(Lang.T("Issue Series of the Aseet"));
        this.setResizable(true);
        this.setModal(true);

        //PACK
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private Transaction refreshRecordDetails(AssetCls origAsset) {

        //ENABLE
        jButton_Confirm.setEnabled(true);

        jLabel_Name_Records.setText(Lang.T(origAsset.toString()));
        jLabel_RecordInfo.setViewportView(new AssetInfo(origAsset, false));

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
        GridBagConstraints gridBagConstraints;

        jLabel_RecordInfo = new JScrollPane();
        jLabel_YourAddress = new JLabel();

        jLabel_Fee = new JLabel();
        jFormattedTextField_Fee = new JTextField();
        jLabel_Fee_Check = new JLabel();
        jLabel_Title = new JLabel();
        jLabel_Name_Records = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        GridBagLayout layout = new GridBagLayout();
        getContentPane().setLayout(layout);

        jLabel_RecordInfo.setBorder(BorderFactory.createEtchedBorder());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        //    gridBagConstraints.insets = new java.awt.Insets(12, 9, 0, 9);
        gridBagConstraints.insets = new Insets(0, 9, 0, 9);
        getContentPane().add(jLabel_RecordInfo, gridBagConstraints);

        jLabel_YourAddress.setText(Lang.T("Account") + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(21, 27, 0, 0);
        getContentPane().add(jLabel_YourAddress, gridBagConstraints);

        jComboBox_YourAddress = new JComboBox<Account>(new AccountsComboBoxModel());
        if (account != null) jComboBox_YourAddress.setSelectedItem(account);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(21, 0, 0, 13);
        getContentPane().add(jComboBox_YourAddress, gridBagConstraints);


        jLabel_Fee.setText(Lang.T("Fee Power") + ":");
        jLabel_Fee.setVisible(Gui.SHOW_FEE_POWER);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new Insets(0, 27, 0, 0);
        getContentPane().add(jLabel_Fee, gridBagConstraints);
        jFormattedTextField_Fee.setText("0");
        jFormattedTextField_Fee.setVisible(Gui.SHOW_FEE_POWER);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = gridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 20, 0);
        getContentPane().add(jFormattedTextField_Fee, gridBagConstraints);

        jButton_Cansel = new MButton(Lang.T("Cancel"), 2);
        jButton_Cansel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new Insets(1, 0, 29, 0);
        getContentPane().add(jButton_Cansel, gridBagConstraints);

        jButton_Confirm = new MButton(Lang.T("Confirm"), 2);
        jButton_Confirm.setToolTipText("");
        jButton_Confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onGoClick();
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
        getContentPane().add(jButton_Confirm, gridBagConstraints);

        pack();
    }

}
