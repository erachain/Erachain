package org.erachain.gui.bank;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.IconPanel;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.items.mails.MailInfo;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.lang.Lang;
import org.erachain.utils.StrJSonFine;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;

public class IssueSendPaymentOrder extends IconPanel {

    public static String NAME = "IssueSendPaymentOrder";
    public static String TITLE = "Send payment order";
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private IssueSendPaymentOrder1 issue_Panel;
    private IssueSendPaymentOrder th;

    /*
     * To change this license header, choose License Headers in Project
     * Properties. To change this template file, choose Tools | Templates and
     * open the template in the editor.
     */
    public IssueSendPaymentOrder() {
        super(NAME, TITLE);
        th = this;
        setLayout(new java.awt.BorderLayout());
        JScrollPane scroll = new JScrollPane();
        issue_Panel = new IssueSendPaymentOrder1();
        scroll.setViewportView(issue_Panel);
        add(scroll);

        issue_Panel.jButton_OK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                onSendClick();
            }
        });
        issue_Panel.jButton_Cancel.addActionListener(e -> onClearAllClick());


    }

    public void onSendClick() {
        // DISABLE
        issue_Panel.jButton_OK.setEnabled(false);
        String error_mes = "";
        // TODO TEST
        // CHECK IF NETWORK OK
        /*
         * if(Controller.getInstance().getStatus() != Controller.STATUS_OKE) {
         * //NETWORK NOT OK JOptionPane.showMessageDialog(null,
         * "You are unable to send a transaction while synchronizing or while having no connections!"
         * , "Error", JOptionPane.ERROR_MESSAGE);
         *
         * //ENABLE this.sendButton.setEnabled(true);
         *
         * return; }
         */

        if (issue_Panel.jTextField_Account_in_Bank.getForeground() == Color.RED)
            error_mes += "Error ACCOUNT_ERA_Of_Bank";
        if (issue_Panel.jTextField_BIK.getForeground() == Color.RED) error_mes += "\n Error BIK";
        if (issue_Panel.jTextField_INN.getForeground() == Color.RED) error_mes += "\n Error inn";
        if (issue_Panel.jTextField_Reciever_BIK.getForeground() == Color.RED) error_mes += "\n Error Reciever BIK";
        if (issue_Panel.jTextField_Reciever_INN.getForeground() == Color.RED) error_mes += "\n Error Reciever INN";
        if (issue_Panel.jTextField_Recivier_Account_in_Bank1.getForeground() == Color.RED)
            error_mes += "\n Error Reciever Account in Bank";
        if (issue_Panel.jTextField_Doc_Num.getForeground() == Color.RED) error_mes += "\n Error Document Number";
        if (issue_Panel.jTextField_Doc_Date.getForeground() == Color.RED) error_mes += "\n Error Document Date";
        // READ RECIPIENT
        String recipientAddress = issue_Panel.jComboBox_ACCOUNT_ERA_Of_Bank.getText();

        // ORDINARY RECIPIENT
        Tuple2<Account, String> accountRes = Account.tryMakeAccount(recipientAddress);
        Account recipient = accountRes.a;
        if (recipient == null) {
            //	JOptionPane.showMessageDialog(null, accountRes.b, Lang.T("Error"),
            //			JOptionPane.ERROR_MESSAGE);
            error_mes += "\n" + accountRes.b;
            // ENABLE
            //issue_Panel.jButton_OK.setEnabled(true);
            //return;
        }
        if (issue_Panel.jTextField_Ammount.getForeground() == Color.RED) error_mes += " \n Error Amount";


        if (error_mes.length() > 0) {
            JOptionPane.showMessageDialog(new JFrame(),
                    error_mes,
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            issue_Panel.jButton_OK.setEnabled(true);
            return;
        }


        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (password.equals("")) {
                issue_Panel.jButton_OK.setEnabled(true);
                return;
            }
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"),
                        Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                issue_Panel.jButton_OK.setEnabled(true);
                return;
            }
        }

        // READ SENDER
        Account sender = (Account) issue_Panel.jComboBox_Account.getSelectedItem();


        String message = packMessage();

        boolean isTextB = true;

        byte[] messageBytes = null;

        if (message != null && message.length() > 0) {
            if (isTextB) {
                messageBytes = message.getBytes(StandardCharsets.UTF_8);
            } else {
                try {
                    messageBytes = Base58.decode(message); //Converter.parseHexString(message);
                } catch (Exception g) {
                    try {
                        messageBytes = Base58.decode(message);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(new JFrame(),
                                Lang.T("Message format is not base58 or hex!"),
                                Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                        // ENABLE
                        issue_Panel.jButton_OK.setEnabled(true);
                        return;
                    }
                }
            }
        }

        // if no TEXT - set null
        if (messageBytes != null && messageBytes.length == 0)
            messageBytes = null;
        // if amount = 0 - set null


        boolean encryptMessage = issue_Panel.jCheckBox_Encrypted.isSelected();

        byte[] encrypted = (encryptMessage) ? new byte[]{1} : new byte[]{0};
        byte[] isTextByte = (isTextB) ? new byte[]{1} : new byte[]{0};

        AssetCls asset;
        long key = 0l;


        Integer result;

        if (messageBytes != null) {
            if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.T("Message size exceeded!") + " <= MAX",
                        Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                issue_Panel.jButton_OK.setEnabled(true);
                return;
            }

            if (encryptMessage) {
                // sender
                PrivateKeyAccount account = Controller.getInstance()
                        .getWalletPrivateKeyAccountByAddress(sender.getAddress().toString());
                byte[] privateKey = account.getPrivateKey();

                // recipient
                byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
                if (publicKey == null) {
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.T(ApiErrorFactory.getInstance().messageError(ApiErrorFactory.ERROR_NO_PUBLIC_KEY)),
                            Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                    // ENABLE
                    issue_Panel.jButton_OK.setEnabled(true);

                    return;
                }

                messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
            }
        }


        String head = "SPO"; // send payment order
        if (head == null)
            head = "";
        if (head.getBytes(StandardCharsets.UTF_8).length > 256) {

            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Title size exceeded!") + " <= 256",
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;

        }

        ExLink exLink = null;
        Long linkRef = Transaction.parseDBRef("");
        if (linkRef != null) {
            exLink = new ExLinkAppendix(linkRef);
        }

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send(
                Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress()), exLink, 0, recipient, key,
                null, head, messageBytes, isTextByte, encrypted, 0);

        String Status_text = "";
        IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                Lang.T("Send Payment Order"), (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2),
                Status_text, Lang.T("Confirmation transaction send payment order"));

        MailInfo ww = new MailInfo((RSend) transaction);
        ww.jTabbedPane1.setVisible(false);
        confirmDialog.jScrollPane1.setViewportView(ww);
        confirmDialog.setLocationRelativeTo(th);
        confirmDialog.setVisible(true);

        // JOptionPane.OK_OPTION
        if (confirmDialog.isConfirm > 0) {
            ResultDialog.make(this, transaction, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE, null, null);
        }
        // ENABLE
        issue_Panel.jButton_OK.setEnabled(true);
    }

    /**
     * Clear all text field in form "issue send payment"
     */
    public void onClearAllClick() {
        issue_Panel.jComboBox_ACCOUNT_ERA_Of_Bank.setText("");
        issue_Panel.jTextArea_Description.setText("");
        issue_Panel.jTextField_Doc_Num.setText("");
        issue_Panel.jTextField_Ammount.setText("");
        issue_Panel.jTextField_Doc_Date.setText("");
        issue_Panel.jTextField_Account_in_Bank.setText("");
        issue_Panel.jTextField_Ammount.setText("");
        issue_Panel.jTextField_BIK.setText("");
        issue_Panel.jTextField_INN.setText("");
        issue_Panel.jTextField_Reciever_BIK.setText("");
        issue_Panel.jTextField_Reciever_INN.setText("");
        issue_Panel.jTextField_Recivier_Account_in_Bank1.setText("");
    }

    private String packMessage() {
        // TODO Auto-generated method stub
        JSONObject jSON_Message = new JSONObject();
        jSON_Message.put("OKYD", "0401060"); // set payment order
        jSON_Message.put("24", issue_Panel.jTextArea_Description.getText()); // description
        jSON_Message.put("9", issue_Panel.jTextField_Account_in_Bank.getText());
        jSON_Message.put("7", issue_Panel.jTextField_Ammount.getText()); // set amount
        jSON_Message.put("11", issue_Panel.jTextField_BIK.getText()); // sender bik
        jSON_Message.put("60", issue_Panel.jTextField_INN.getText()); // sender inn
        jSON_Message.put("14", issue_Panel.jTextField_Reciever_BIK.getText()); // reciever bik
        jSON_Message.put("61", issue_Panel.jTextField_Reciever_INN.getText()); // reciever inn
        jSON_Message.put("15", issue_Panel.jTextField_Recivier_Account_in_Bank1.getText()); // reciever chet
        jSON_Message.put("3", issue_Panel.jTextField_Doc_Num.getText());
        jSON_Message.put("4", issue_Panel.jTextField_Doc_Date.getText()); // date

        byte[] messageBytes = StrJSonFine.convert(jSON_Message).getBytes(StandardCharsets.UTF_8);


        String a = new String(messageBytes, StandardCharsets.UTF_8);
        return a;
    }

}

