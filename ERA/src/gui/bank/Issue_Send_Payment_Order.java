package gui.bank;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.crypto.AEScrypto;
import core.crypto.Base58;
import core.item.assets.AssetCls;
import core.transaction.R_Send;
import core.transaction.Transaction;
import gui.MainFrame;
import gui.PasswordPane;
import gui.items.mails.Mail_Info;
import gui.library.*;
import gui.models.AccountsComboBoxModel;
import gui.transaction.OnDealClick;
import lang.Lang;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import utils.Converter;
import utils.MenuPopupUtil;
import utils.StrJSonFine;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class Issue_Send_Payment_Order extends javax.swing.JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Issue_Send_Payment_Order1 issue_Panel;
    private Issue_Send_Payment_Order th;

    /*
     * To change this license header, choose License Headers in Project
     * Properties. To change this template file, choose Tools | Templates and
     * open the template in the editor.
     */
    public Issue_Send_Payment_Order() {
        th = this;
        setLayout(new java.awt.BorderLayout());
        JScrollPane scroll = new JScrollPane();
        issue_Panel = new Issue_Send_Payment_Order1();
        scroll.setViewportView(issue_Panel);
        add(scroll);

        issue_Panel.jButton_OK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                onSendClick();

            }

        });


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
            //	JOptionPane.showMessageDialog(null, accountRes.b, Lang.getInstance().translate("Error"),
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
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                        Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

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
                messageBytes = message.getBytes(Charset.forName("UTF-8"));
            } else {
                try {
                    messageBytes = Converter.parseHexString(message);
                } catch (Exception g) {
                    try {
                        messageBytes = Base58.decode(message);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(new JFrame(),
                                Lang.getInstance().translate("Message format is not base58 or hex!"),
                                Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

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
                        Lang.getInstance().translate("Message size exceeded!") + " <= MAX",
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                issue_Panel.jButton_OK.setEnabled(true);
                return;
            }

            if (encryptMessage) {
                // sender
                PrivateKeyAccount account = Controller.getInstance()
                        .getPrivateKeyAccountByAddress(sender.getAddress().toString());
                byte[] privateKey = account.getPrivateKey();

                // recipient
                byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
                if (publicKey == null) {
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.getInstance().translate(
                                    "The recipient has not yet performed any action in the blockchain.\nYou can't send an encrypted message to him."),
                            Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

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
                    Lang.getInstance().translate("Title size exceeded!") + " <= 256",
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;

        }

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send(
                Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), 0, recipient, key,
                null, head, messageBytes, isTextByte, encrypted);
        // test result = new Pair<Transaction, Integer>(null,
        // Transaction.VALIDATE_OK);

        String Status_text = "<HTML>" + Lang.getInstance().translate("Size") + ":&nbsp;" + transaction.viewSize(false)
                + " Bytes, ";
        Status_text += "<b>" + Lang.getInstance().translate("Fee") + ":&nbsp;" + transaction.getFee().toString()
                + " COMPU</b><br></body></HTML>";

        Issue_Confirm_Dialog dd = new Issue_Confirm_Dialog(MainFrame.getInstance(), true,
                Lang.getInstance().translate("Send Payment Order"), (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2),
                Status_text, Lang.getInstance().translate("Confirmation Transaction") + " "
                + Lang.getInstance().translate("Send Payment Order"));

        Mail_Info ww = new Mail_Info((R_Send) transaction);
        ww.jTabbedPane1.setVisible(false);
        dd.jScrollPane1.setViewportView(ww);
        dd.setLocationRelativeTo(th);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            result = Controller.getInstance().getTransactionCreator().afterCreate(transaction, false);

            // CHECK VALIDATE MESSAGE
            if (result == transaction.VALIDATE_OK) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate("Payment Order has been sent!"),
                        Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate(OnDealClick.resultMess(result)),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
        // ENABLE
        issue_Panel.jButton_OK.setEnabled(true);
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

        byte[] messageBytes = StrJSonFine.convert(jSON_Message).getBytes(Charset.forName("UTF-8"));


        String a = new String(messageBytes, Charset.forName("UTF-8"));
        return a;
    }
}

class Issue_Send_Payment_Order1 extends JPanel {
    // Variables declaration - do not modify
    javax.swing.JButton jButton_Cancel;
    javax.swing.JButton jButton_OK;
    javax.swing.JCheckBox jCheckBox_Encrypted;
    JTextField jComboBox_ACCOUNT_ERA_Of_Bank;
    JComboBox<Account> jComboBox_Account;
    javax.swing.JLabel jLabel_4_Data;
    javax.swing.JLabel jLabel_ACCOUNT_ERA_Of_Bank;
    javax.swing.JLabel jLabel_Account;
    javax.swing.JLabel jLabel_Ammount;
    javax.swing.JLabel jLabel_BIK;
    javax.swing.JLabel jLabel_Date;
    javax.swing.JLabel jLabel_Description;
    javax.swing.JLabel jLabel_Number;
    javax.swing.JLabel jLabel_Payment_info;
    javax.swing.JLabel jLabel_Reciever;
    javax.swing.JLabel jLabel_Reciever_BIK;
    javax.swing.JLabel jLabel_Reciever_INN;
    javax.swing.JLabel jLabel_Recivier_in_Bank1;
    javax.swing.JLabel jLabel_Sender;
    javax.swing.JLabel jLabel_Title;
    javax.swing.JLabel jLabel_in_Bank;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JSeparator jSeparator1;
    javax.swing.JSeparator jSeparator2;
    javax.swing.JSeparator jSeparator3;
    javax.swing.JTextArea jTextArea_Description;
    My_Int_Long_JTextField jTextField_Doc_Num;
    My_Date_JFormatedTextField jTextField_Doc_Date;
    My_Bank_Account_JTextField jTextField_Account_in_Bank;
    My_Ammount_JTextField jTextField_Ammount;
    My_BIK_JTextField jTextField_BIK;
    My_INN_JTextField jTextField_INN;
    My_BIK_JTextField jTextField_Reciever_BIK;
    My_INN_JTextField jTextField_Reciever_INN;
    My_Bank_Account_JTextField jTextField_Recivier_Account_in_Bank1;
    private AccountsComboBoxModel accountsModel;
    public Issue_Send_Payment_Order1() {
        this.accountsModel = new AccountsComboBoxModel();
        this.jComboBox_Account = new JComboBox<Account>(accountsModel);
        initComponents();
        // menu
        MenuPopupUtil.installContextMenu(this.jComboBox_ACCOUNT_ERA_Of_Bank);
        MenuPopupUtil.installContextMenu(this.jTextArea_Description);
        // labels
        this.jButton_Cancel.setText(Lang.getInstance().translate("Cancel"));
        this.jButton_OK.setText(Lang.getInstance().translate("Send"));
        this.jCheckBox_Encrypted.setText(Lang.getInstance().translate("Encrypt"));
        this.jLabel_4_Data.setText(Lang.getInstance().translate("INN"));
        this.jLabel_ACCOUNT_ERA_Of_Bank.setText(Lang.getInstance().translate("To: (Account ERA)"));
        this.jLabel_Account.setText(Lang.getInstance().translate("Select Account"));
        this.jLabel_Ammount.setText(Lang.getInstance().translate("Amount"));
        this.jLabel_BIK.setText(Lang.getInstance().translate("BIK"));
        this.jLabel_Date.setText(Lang.getInstance().translate("Date"));
        this.jLabel_Description.setText(Lang.getInstance().translate("Purpose payment"));
        this.jLabel_Number.setText(Lang.getInstance().translate("Order Number"));
        this.jLabel_Payment_info.setText(Lang.getInstance().translate("Payment details"));
        this.jLabel_Reciever.setText(Lang.getInstance().translate("Recipient"));
        this.jLabel_Reciever_BIK.setText(Lang.getInstance().translate("BIK"));
        ;
        this.jLabel_Reciever_INN.setText(Lang.getInstance().translate("INN"));
        this.jLabel_Recivier_in_Bank1.setText(Lang.getInstance().translate("Account"));
        this.jLabel_Sender.setText(Lang.getInstance().translate("Sender"));
        this.jLabel_Title.setText(Lang.getInstance().translate("Send Payment Order"));
        this.jLabel_in_Bank.setText(Lang.getInstance().translate("Account"));


    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel_4_Data = new javax.swing.JLabel();
        jTextField_INN = new My_INN_JTextField();
        jLabel_in_Bank = new javax.swing.JLabel();
        jTextField_Account_in_Bank = new My_Bank_Account_JTextField();
        jLabel_Account = new javax.swing.JLabel();
        // jComboBox_Account = new javax.swing.JComboBox<>();
        jLabel_BIK = new javax.swing.JLabel();
        jTextField_BIK = new My_BIK_JTextField();
        jLabel_ACCOUNT_ERA_Of_Bank = new javax.swing.JLabel();
        jComboBox_ACCOUNT_ERA_Of_Bank = new JTextField();
        jLabel_Sender = new javax.swing.JLabel();
        jLabel_Reciever = new javax.swing.JLabel();
        jLabel_Reciever_INN = new javax.swing.JLabel();
        jTextField_Reciever_INN = new My_INN_JTextField();
        jLabel_Reciever_BIK = new javax.swing.JLabel();
        jTextField_Reciever_BIK = new My_BIK_JTextField();
        jLabel_Recivier_in_Bank1 = new javax.swing.JLabel();
        jTextField_Recivier_Account_in_Bank1 = new My_Bank_Account_JTextField();
        jLabel_Ammount = new javax.swing.JLabel();
        jTextField_Ammount = new My_Ammount_JTextField();
        jLabel_Description = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea_Description = new javax.swing.JTextArea();
        jCheckBox_Encrypted = new javax.swing.JCheckBox();
        jLabel_Title = new javax.swing.JLabel();
        jLabel_Payment_info = new javax.swing.JLabel();
        jLabel_Number = new javax.swing.JLabel();
        jTextField_Doc_Num = new My_Int_Long_JTextField();
        jLabel_Date = new javax.swing.JLabel();

        MaskFormatter mf = null;
        try {
            mf = new MaskFormatter("##-##-####");
            mf.setPlaceholderCharacter('_');
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        jTextField_Doc_Date = new My_Date_JFormatedTextField(mf);
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jButton_Cancel = new javax.swing.JButton();
        jButton_OK = new javax.swing.JButton();

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0};
        layout.rowHeights = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0,
                5, 0, 5, 0, 5, 0, 5, 0};
        setLayout(layout);

        jLabel_4_Data.setText("inn");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_4_Data, gridBagConstraints);

        jTextField_INN.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_INN, gridBagConstraints);

        jLabel_in_Bank.setText("Account in Bank");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_in_Bank, gridBagConstraints);

        jTextField_Account_in_Bank.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_Account_in_Bank, gridBagConstraints);

        jLabel_Account.setText("Account");
        jLabel_Account.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Account, gridBagConstraints);

        // jComboBox_Account.setModel(new javax.swing.DefaultComboBoxModel<>(new
        // String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jComboBox_Account, gridBagConstraints);

        jLabel_BIK.setText("BIK");
        jLabel_BIK.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_BIK, gridBagConstraints);

        jTextField_BIK.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_BIK, gridBagConstraints);

        jLabel_ACCOUNT_ERA_Of_Bank.setText("ACCOUNT_ERA_Of_Bank");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_ACCOUNT_ERA_Of_Bank, gridBagConstraints);

        // jComboBox_ACCOUNT_ERA_Of_Bank.setModel(new
        // javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2",
        // "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jComboBox_ACCOUNT_ERA_Of_Bank, gridBagConstraints);

        jLabel_Sender.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Sender.setText("Sender");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(jLabel_Sender, gridBagConstraints);

        jLabel_Reciever.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Reciever.setText("Reciever");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(jLabel_Reciever, gridBagConstraints);

        jLabel_Reciever_INN.setText("Reciever INN");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Reciever_INN, gridBagConstraints);

        jTextField_Reciever_INN.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_Reciever_INN, gridBagConstraints);

        jLabel_Reciever_BIK.setText("Reciever BIK");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Reciever_BIK, gridBagConstraints);

        jTextField_Reciever_BIK.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_Reciever_BIK, gridBagConstraints);

        jLabel_Recivier_in_Bank1.setText("Reciever Account in Bank");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Recivier_in_Bank1, gridBagConstraints);

        jTextField_Recivier_Account_in_Bank1.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_Recivier_Account_in_Bank1, gridBagConstraints);

        jLabel_Ammount.setText("Ammount");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Ammount, gridBagConstraints);

        jTextField_Ammount.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_Ammount, gridBagConstraints);

        jLabel_Description.setText("Description");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Description, gridBagConstraints);

        jTextArea_Description.setColumns(20);
        jTextArea_Description.setLineWrap(true);
        jTextArea_Description.setRows(5);
        jTextArea_Description.setText("");
        jScrollPane1.setViewportView(jTextArea_Description);
        jScrollPane1.setMinimumSize(new Dimension(100, 25));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.weighty = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jScrollPane1, gridBagConstraints);

        jCheckBox_Encrypted.setText("Encrypted");
        jCheckBox_Encrypted.setSelected(true);
        jCheckBox_Encrypted.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 32;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        add(jCheckBox_Encrypted, gridBagConstraints);

        jLabel_Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Title.setText("Title");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(jLabel_Title, gridBagConstraints);

        jLabel_Payment_info.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Payment_info.setText("Payment Info");
        jLabel_Payment_info.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(jLabel_Payment_info, gridBagConstraints);

        jLabel_Number.setText("Number");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Number, gridBagConstraints);

        jTextField_Doc_Num.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_Doc_Num, gridBagConstraints);

        jLabel_Date.setText("Date");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(jLabel_Date, gridBagConstraints);

        //jTextField_Doc_Date.setText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(jTextField_Doc_Date, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(jSeparator3, gridBagConstraints);

        jButton_Cancel.setText("Cancel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 34;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(jButton_Cancel, gridBagConstraints);

        jButton_OK.setText("OK");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 34;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(jButton_OK, gridBagConstraints);
    }// </editor-fold>
    // End of variables declaration
}
