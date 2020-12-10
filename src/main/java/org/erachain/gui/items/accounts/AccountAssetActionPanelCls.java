package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;
import org.erachain.gui.Gui;
import org.erachain.gui.IconPanel;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.items.assets.AssetInfo;
import org.erachain.gui.items.assets.ComboBoxAssetsModel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MDecimalFormatedTextField;
import org.erachain.gui.library.RecipientAddress;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.Send_RecordDetailsFrame;
import org.erachain.lang.Lang;
import org.erachain.utils.Converter;
import org.erachain.utils.MenuPopupUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

//import org.erachain.gui.AccountRenderer;

public abstract class AccountAssetActionPanelCls extends IconPanel implements RecipientAddress.RecipientAddressInterface {


    public Account sender;

    public Account recipient;

    public String message;

    public byte[] messageBytes;

    public BigDecimal amount;

    public boolean backward;

    public ExLink exLink;

    public int feePow;

    public boolean isTextB;

    public AssetCls asset;

    //public String title;

    public long key;

    public String head;

    public byte[] isTextByte;

    public byte[] encrypted;
    public Integer result;

    public int balancePosition;

    public boolean noReceive;

    /**
     * Creates new form AccountAssetActionPanelCls
     */

    private AccountsComboBoxModel accountsModel;

    public AccountAssetActionPanelCls(String panelName, String title, boolean backward, AssetCls assetIn,
                                      int balancePosition,
                                      Account accountFrom, Account accountTo, String message) {

        super(panelName, title);
        if (assetIn == null)
            this.asset = Controller.getInstance().getAsset(2L);
        else
            this.asset = assetIn;

        this.backward = backward;

        // необходимо входящий параметр отделить так как ниже он по событию изменения актива будет как НУЛь вызваться
        // поэтому тут только приватную переменную юзаем дальше
        if (title == null) {
            this.title = asset.viewAssetTypeActionTitle(backward, balancePosition,
                    accountFrom != null && accountFrom.equals(asset.getOwner()));
        }

        if (panelName == null) {
            this.panelName = title + " [" + asset.getKey() + " ]";
            setName(this.panelName);
        }

        this.sender = accountFrom;

        recipient = accountTo;
        // возможно есть счет по умолчанию
        if (recipient == null && asset != null) {
            recipient = asset.defaultRecipient(balancePosition, backward);
        }

        this.balancePosition = balancePosition;

        initComponents(message);

        this.jTextField_Recive_Detail.setText("");
        this.jTextField_Mess_Title.setText("");

        if (this.asset.defaultAmountAssetType() == null)
            this.jTextField_Amount.setText("0");
        else
            this.jTextField_Amount.setText(this.asset.defaultAmountAssetType().toPlainString());

        // account ComboBox
        this.accountsModel = new AccountsComboBoxModel(balancePosition);
        jComboBox_Account.setModel(accountsModel);

        if (sender != null) {
            jComboBox_Account.setSelectedItem(sender);
        }

        jComboBox_Account.setRenderer(new AccountRenderer(asset.getKey()));

        // favorite combo box
        jComboBox_Asset.setModel(new ComboBoxAssetsModel());
        jComboBox_Asset.setEditable(false);
        //this.jComboBox_Asset.setEnabled(assetIn != null);

        exLinkDescription.setEditable(false);
        exLinkText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                viewLinkParent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                viewLinkParent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                viewLinkParent();
            }
        });


        if (asset.getKey() > 0 && asset.getKey() < 1000) {
            this.jTextArea_Account_Description.setText(Lang.getInstance().translate(asset.viewDescription()));
        } else {
            this.jTextArea_Account_Description.setText(asset.viewDescription());
        }

        boolean selected = false;
        for (int i = 0; i < jComboBox_Asset.getItemCount(); i++) {
            ItemCls item = jComboBox_Asset.getItemAt(i);
            if (item.getKey() == asset.getKey()) {
                // not worked jComboBox_Asset.setSelectedItem(asset);
                jComboBox_Asset.setSelectedIndex(i);
                selected = true;
                //    jComboBox_Asset.setEnabled(false);// .setEditable(false);
                break;
            } else {
                //    jComboBox_Asset.setEnabled(true);
            }
        }
        if (!selected) {
            jComboBox_Asset.addItem(asset);
            jComboBox_Asset.setSelectedItem(asset);
        }

        this.jComboBox_Fee.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        jComboBox_Fee.setVisible(Gui.SHOW_FEE_POWER);

        //ON FAVORITES CHANGE

        jComboBox_Account.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                sender = ((Account) jComboBox_Account.getSelectedItem());
                refreshLabels();

            }
        });


        // default set asset
        ////if (asset == null) asset = ((AssetCls) jComboBox_Asset.getSelectedItem());

        this.jComboBox_Asset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                asset = ((AssetCls) jComboBox_Asset.getSelectedItem());
                refreshLabels();

            }
        });

        jButton_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSendClick();
            }
        });

        this.jLabel_Mess_Title.setText(Lang.getInstance().translate("Title") + ":");
        this.jLabel_Mess.setText(Lang.getInstance().translate("Message") + ":");
        this.jCheckBox_Enscript.setText(Lang.getInstance().translate("Encrypt message") + ":");
        this.jCheckBox_Enscript.setSelected(true);
        this.jLabel_Asset.setText(Lang.getInstance().translate(asset.viewAssetType()) + ":");
        this.jLabel_Amount.setText(Lang.getInstance().translate("Amount") + ":");

        if (sender != null && asset != null) {
            jLabel_AmountHave.setText(Lang.getInstance().translate("Balance") + ": "
                    + sender.getBalanceInPosition(asset.getKey(), balancePosition).b.toPlainString());
        }

        this.jLabel_Fee.setText(Lang.getInstance().translate("Fee Power") + ":");
        jLabel_Fee.setVisible(Gui.SHOW_FEE_POWER);

        // CONTEXT MENU
        MenuPopupUtil.installContextMenu(this.jTextField_Amount);
        MenuPopupUtil.installContextMenu(this.jTextArea_Description);
        MenuPopupUtil.installContextMenu(this.jTextField_Recive_Detail);
        jTextArea_Account_Description.setWrapStyleWord(true);
        jTextArea_Account_Description.setLineWrap(true);
        jScrollPane2.setViewportView(new AssetInfo(asset, false));

        if (recipient == null) {
            jButton_ok.setEnabled(false);
        } else {
            if (recipient instanceof PublicKeyAccount) {
                recipientAddress.setSelectedAddress(((PublicKeyAccount) recipient).getBase58());
            } else {
                recipientAddress.setSelectedAddress(recipient.getAddress());
            }
            jButton_ok.setEnabled(true);
        }

        refreshLabels();
    }

    private void refreshLabels() {

        if (asset == null) {
            return;
        }

        boolean senderIsOwner = false;
        //sender = ((Account) jComboBox_Account.getSelectedItem());
        if (sender != null) {
            ((AccountRenderer) jComboBox_Account.getRenderer()).setAsset(asset.getKey());
            jComboBox_Account.repaint();
            senderIsOwner = sender.equals(asset.getOwner());
        }

        boolean recipientIsOwner = false;
        if (recipient != null) {
            recipientIsOwner = recipient.equals(asset.getOwner());
        }

        String title = Lang.getInstance().translate(asset.viewAssetTypeActionTitle(backward, balancePosition, senderIsOwner));
        jLabel_Title.setText(title + " - " + asset.viewName());

        setName(title + " ]" + asset.getKey() + " ]");
        String addAssetType = asset.viewAssetTypeAdditionAction(backward, balancePosition, senderIsOwner);
        if (addAssetType == null) {
            jButton_ok.setText(Lang.getInstance().translate(asset.viewAssetTypeActionOK(backward, balancePosition, senderIsOwner)));
        } else {
            jButton_ok.setText(Lang.getInstance().translate(asset.viewAssetTypeActionOK(backward, balancePosition, senderIsOwner))
                    + " (" + Lang.getInstance().translate(addAssetType) + ")");
        }

        this.jLabel_Account.setText(Lang.getInstance().translate(asset.viewAssetTypeCreator(backward, balancePosition, senderIsOwner)) + ":");

        this.jLabel_To.setText(Lang.getInstance().translate(
                asset.viewAssetTypeTarget(backward, balancePosition, recipientIsOwner) + " " + "Account") + ":");
        this.jLabel_Recive_Detail.setText(Lang.getInstance().translate("Account Details") + ":");

        // set scale
        int scale = asset.getScale();
        jTextField_Amount.setScale(scale);
        jScrollPane2.setViewportView(new AssetInfo(asset, false));

        if (sender != null) {
            if (balancePosition == TransactionAmount.ACTION_DEBT || balancePosition == TransactionAmount.ACTION_REPAY_DEBT) {
                // берем совместно с выданным кредитом
                BigDecimal forSale = sender.getForSale(DCSet.getInstance(), asset.getKey(), Controller.getInstance().getMyHeight(),
                        true);
                jLabel_AmountHave.setText(Lang.getInstance().translate("Balance") + ": "
                        + forSale.toPlainString());
            } else {
                jLabel_AmountHave.setText(Lang.getInstance().translate("Balance") + ": "
                        + sender.getBalanceInPosition(asset.getKey(), balancePosition).b.toPlainString());
            }
        }

    }

    protected void checkReadyToOK() {

        try {
            String recipientAddressStr = recipientAddress.getSelectedAddress().trim();
            if (recipientAddressStr.isEmpty() ||
                    !Crypto.getInstance().isValidAddress(recipientAddressStr) && !PublicKeyAccount.isValidPublicKey(recipientAddressStr)) {
                jButton_ok.setEnabled(false);
                return;
            }
        } catch (Exception e) {
            jButton_ok.setEnabled(false);
            return;
        }

        jButton_ok.setEnabled(true);

    }

    private void refreshReceiverDetails() {

        checkReadyToOK();

        String recipient = recipientAddress.getSelectedAddress();
        AssetCls asset = ((AssetCls) jComboBox_Asset.getSelectedItem());

        this.jTextField_Recive_Detail.setText(Lang.getInstance().translate(
                Account.getDetailsForEncrypt(recipient, asset.getKey(),
                        this.jCheckBox_Enscript.isSelected())));

        refreshLabels();

    }

    public boolean cheskError() {
        this.jButton_ok.setEnabled(false);

        //READ SENDER
        sender = (Account) jComboBox_Account.getSelectedItem();
        //CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            //ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (password.equals("")) {
                this.jButton_ok.setEnabled(true);
                return false;
            }
            if (!Controller.getInstance().unlockWallet(password)) {
                //WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.jButton_ok.setEnabled(true);
                return false;
            }
        }

        //READ RECIPIENT
        String recipientAddressStr = recipientAddress.getSelectedAddress().trim();

        //ORDINARY RECIPIENT
        if (Crypto.getInstance().isValidAddress(recipientAddressStr)) {
            this.recipient = new Account(recipientAddressStr);
        } else {
            if (PublicKeyAccount.isValidPublicKey(recipientAddressStr)) {
                recipient = new PublicKeyAccount(recipientAddressStr);
            } else {
                JOptionPane.showMessageDialog(null, "INVALID", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.jButton_ok.setEnabled(true);
                return false;
            }
        }

        exLink = null;
        Long linkRef = Transaction.parseDBRef(exLinkText.getText());
        if (linkRef != null) {
            exLink = new ExLinkAppendix(linkRef);
        }

        int parsing = 0;

        try {
            //READ AMOUNT
            parsing = 1;

            //READ FEE
            parsing = 2;
            feePow = Integer.parseInt((String) this.jComboBox_Fee.getSelectedItem());
        } catch (Exception e) {
        }

        try {
            //READ AMOUNT
            parsing = 1;
            amount = new BigDecimal(jTextField_Amount.getText());

            //READ FEE
            parsing = 2;
            feePow = Integer.parseInt((String) this.jComboBox_Fee.getSelectedItem());
        } catch (Exception e) {
            //CHECK WHERE PARSING ERROR HAPPENED
            switch (parsing) {
                case 1:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid amount!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case 2:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
            }
            //ENABLE
            this.jButton_ok.setEnabled(true);
            return false;
        }

        if (amount.equals(new BigDecimal("0.0"))) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Amount must be greater 0.0"), Lang.getInstance().translate("Error") + ":  " + Lang.getInstance().translate("Invalid amount!"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.jButton_ok.setEnabled(true);
            return false;
        }

        this.message = jTextArea_Description.getText();

        isTextB = true;
        messageBytes = null;
        if (message != null && message.length() > 0) {
            if (isTextB) {
                messageBytes = message.getBytes(StandardCharsets.UTF_8);
            } else {
                try {
                    messageBytes = Converter.parseHexString(message);
                } catch (Exception g) {
                    try {
                        messageBytes = Base58.decode(message);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message format is not base58 or hex!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                        //ENABLE
                        this.jButton_ok.setEnabled(true);
                        return false;
                    }
                }
            }
        }
        // if no TEXT - set null
        if (messageBytes != null && messageBytes.length == 0) messageBytes = null;
        // if amount = 0 - set null
        if (amount.compareTo(BigDecimal.ZERO) == 0) amount = null;

        boolean encryptMessage = this.jCheckBox_Enscript.isSelected();

        encrypted = (encryptMessage) ? new byte[]{1} : new byte[]{0};
        isTextByte = (isTextB) ? new byte[]{1} : new byte[]{0};


        if (amount != null) {
            //CHECK IF PAYMENT OR ASSET TRANSFER
            asset = (AssetCls) this.jComboBox_Asset.getSelectedItem();
            key = asset.getKey();
        }

        if (messageBytes != null) {
            if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message size exceeded!") + " <= MAX", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.jButton_ok.setEnabled(true);
                return false;
            }

            if (encryptMessage) {
                //sender
                PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
                byte[] privateKey = account.getPrivateKey();

                //recipient
                byte[] publicKey;
                if (recipient instanceof PublicKeyAccount) {
                    publicKey = ((PublicKeyAccount) recipient).getPublicKey();
                } else {
                    publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
                }

                if (publicKey == null) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("The recipient has not yet performed any action in the blockchain.\nYou can't send an encrypted message to him."), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                    //ENABLE
                    this.jButton_ok.setEnabled(true);

                    return false;
                }

                messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
            }
        }
        head = this.jTextField_Mess_Title.getText();
        if (head == null)
            head = "";
        if (head.getBytes(StandardCharsets.UTF_8).length > 256) {

            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Title size exceeded!") + " <= 256", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return false;

        }


        return true;
    }

    private void viewLinkParent() {
        String refStr = exLinkText.getText();
        Transaction parentTx = Controller.getInstance().getTransaction(refStr);
        if (parentTx == null) {
            exLinkDescription.setText(Lang.getInstance().translate("Not Found") + "!");
        } else {
            exLinkDescription.setText(parentTx.toStringShortAsCreator());
        }
    }

    // выполняемая процедура при изменении адреса получателя
    @Override
    public void recipientAddressWorker(String e) {
        refreshReceiverDetails();
    }

    protected abstract BigDecimal getAmount();

    protected abstract Long getAssetKey();

    public void onSendClick() {

        // confirm params
        if (!cheskError()) return;

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send((byte) 2, backward ? TransactionAmount.BACKWARD_MASK : 0,
                (byte) 0, Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress()), exLink, feePow,
                recipient, getAssetKey(), getAmount(), head, messageBytes, isTextByte, encrypted);

        String Status_text = "";
        IssueConfirmDialog dd = new IssueConfirmDialog(null, true, transaction,
                Lang.getInstance().translate(asset.viewAssetTypeActionOK(backward, balancePosition,
                        sender != null && sender.equals(asset.getOwner()))),
                (int) (this.getWidth() / 1.2), (int) (this.getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"), !noReceive);
        Send_RecordDetailsFrame ww = new Send_RecordDetailsFrame((RSend) transaction);

        dd.jScrollPane1.setViewportView(ww);
        dd.pack();
        dd.setLocationRelativeTo(this);
        dd.setVisible(true);

        // JOptionPane.OK_OPTION
        if (dd.isConfirm) {

            ResultDialog.make(this, transaction, jButton_ok.getText()
                    + " " + Lang.getInstance().translate("- was made") + "!");

        }

        // ENABLE
        this.jButton_ok.setEnabled(true);
    }


    private void initComponents(String message) {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel_Recive_Detail = new javax.swing.JLabel(Lang.getInstance().translate("Recipient Details") + ":");
        jLabel_Account = new javax.swing.JLabel(Lang.getInstance().translate("Sender") + ":");
        jLabel_To = new javax.swing.JLabel(Lang.getInstance().translate("Recipient") + ":");
        jComboBox_Account = new javax.swing.JComboBox<>();
        jLabel_Mess_Title = new javax.swing.JLabel();
        jTextField_Recive_Detail = new javax.swing.JTextField();
        jLabel_Title = new javax.swing.JLabel();
        jTextField_Mess_Title = new javax.swing.JTextField();
        jLabel_Mess = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea_Description = new javax.swing.JTextArea();
        jCheckBox_Enscript = new javax.swing.JCheckBox();
        jLabel_Asset = new javax.swing.JLabel();
        jComboBox_Asset = new javax.swing.JComboBox<>();
        jLabel_Amount = new javax.swing.JLabel();
        jTextField_Amount = new MDecimalFormatedTextField();
        jLabel_AmountHave = new javax.swing.JLabel();
        jLabel_Fee = new javax.swing.JLabel();
        jComboBox_Fee = new javax.swing.JComboBox<>();
        jButton_ok = new javax.swing.JButton();

        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea_Account_Description = new javax.swing.JTextArea();

        exLinkDescriptionLabel = new JLabel();
        exLinkTextLabel = new JLabel();
        exLinkText = new JTextField();
        exLinkDescription = new JTextField();

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        layout.rowHeights = new int[]{0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0};
        setLayout(layout);

        jLabel_Recive_Detail.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(jLabel_Recive_Detail, gridBagConstraints);

        recipientAddress = new RecipientAddress(this, recipient);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
        add(recipientAddress, gridBagConstraints);

        jLabel_Account.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(jLabel_Account, gridBagConstraints);

        jLabel_To.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(jLabel_To, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
        add(jComboBox_Account, gridBagConstraints);

        jLabel_Mess_Title.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(jLabel_Mess_Title, gridBagConstraints);

        jTextField_Recive_Detail.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
        add(jTextField_Recive_Detail, gridBagConstraints);

        jLabel_Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 15, 6, 15);
        add(jLabel_Title, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
        add(jTextField_Mess_Title, gridBagConstraints);

        jLabel_Mess.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(jLabel_Mess, gridBagConstraints);

        jTextArea_Description.setColumns(20);
        jTextArea_Description.setRows(5);
        jTextArea_Description.setText(message == null? "" : message);
        jScrollPane1.setViewportView(jTextArea_Description);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
        add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(jCheckBox_Enscript, gridBagConstraints);

        jLabel_Asset.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        add(jLabel_Asset, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
        add(jComboBox_Asset, gridBagConstraints);

        jLabel_AmountHave.setHorizontalAlignment(SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        add(jLabel_AmountHave, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        add(jTextField_Amount, gridBagConstraints);

        jLabel_Fee.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        add(jLabel_Fee, gridBagConstraints);

        jComboBox_Fee.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
        add(jComboBox_Fee, gridBagConstraints);
//exLink
        exLinkTextLabel.setText(Lang.getInstance().translate("Append to") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor= GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(exLinkTextLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
         add(exLinkText, gridBagConstraints);

        exLinkDescriptionLabel.setText(Lang.getInstance().translate("Parent") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor= GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(exLinkDescriptionLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
        add(exLinkDescription, gridBagConstraints);

        // exlink

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 15, 15);
        add(jButton_ok, gridBagConstraints);

        jTextArea_Account_Description.setColumns(20);
        jTextArea_Account_Description.setRows(5);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 15);
        add(jScrollPane2, gridBagConstraints);
    }



    public javax.swing.JButton jButton_ok;
    private javax.swing.JCheckBox jCheckBox_Enscript;
    private javax.swing.JComboBox<Account> jComboBox_Account;
    public javax.swing.JComboBox<ItemCls> jComboBox_Asset;
    private javax.swing.JComboBox<String> jComboBox_Fee;
    private javax.swing.JLabel jLabel_Asset;
    private javax.swing.JLabel jLabel_Account;
    private javax.swing.JLabel jLabel_Amount;
    private javax.swing.JLabel jLabel_AmountHave;
    private javax.swing.JLabel jLabel_Fee;
    private javax.swing.JLabel jLabel_Mess;
    private javax.swing.JLabel jLabel_Mess_Title;
    public javax.swing.JLabel jLabel_Recive_Detail;
    public javax.swing.JLabel jLabel_Title;
    public javax.swing.JLabel jLabel_To;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea_Account_Description;
    public javax.swing.JTextArea jTextArea_Description;
    public MDecimalFormatedTextField jTextField_Amount;
    public javax.swing.JTextField jTextField_Mess_Title;
    private javax.swing.JTextField jTextField_Recive_Detail;
    public RecipientAddress recipientAddress;
    public JTextField exLinkText;
    public JTextField exLinkDescription;
    public JLabel exLinkTextLabel;
    public JLabel exLinkDescriptionLabel;


}
