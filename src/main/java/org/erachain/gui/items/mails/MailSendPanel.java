package org.erachain.gui.items.mails;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.*;
import org.erachain.gui.items.accounts.AccountRenderer;
import org.erachain.gui.items.accounts.AccountsComboBoxModel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.MButton;
import org.erachain.gui.library.RecipientAddress;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.utils.Converter;
import org.erachain.utils.MenuPopupUtil;
import org.mapdb.Fun.Tuple2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//import org.erachain.settings.Settings;

@SuppressWarnings("serial")

public class MailSendPanel extends IconPanel implements RecipientAddress.RecipientAddressInterface {

    public static String NAME = "MailSendPanel";
    public static String TITLE = "Send Mail";

    // TODO - "A" - &
    final static String wrongFirstCharOfAddress = "A";
    // private final MessagesTableModel messagesTableModel;
    private final MailsHTMLTableModel messagesHistoryTable;
    public JTextArea txtMessage;
    public JTextField txt_Title;
    int y;
    PersonCls person;
    private JComboBox<Account> cbxFrom;
    private JComboBox cbx_To;
    private RecipientAddress recipientBox;
    private JTextField txtAmount;
    private JComboBox<String> txtFeePow;
    private JCheckBox encrypted;
    private JCheckBox isText;
    private MButton sendButton;
    private org.erachain.gui.models.AccountsComboBoxModel accountsModel;
    private JTextField txtRecDetails;
    private JLabel messageLabel;
    private MailSendPanel th;
    public JTextField exLinkText;
    public JTextField exLinkDescription;
    public JLabel exLinkTextLabel;
    public JLabel exLinkDescriptionLabel;

    public MailSendPanel(Account accountFrom, Account accountTo, PersonCls person) {

        super(NAME, TITLE);
        th = this;
        this.person = person;
        sendButton = new MButton(Lang.T("Send"), 2);
        y = 0;

        GridBagConstraints fieldGBC = new GridBagConstraints();
        fieldGBC.insets = new Insets(5, 5, 5, 5);
        fieldGBC.fill = GridBagConstraints.HORIZONTAL;
        fieldGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        fieldGBC.gridx = 1;

        GridBagLayout gridBagLayout = new GridBagLayout();
        // gridBagLayout.columnWidths = new int[]{0, 112, 140, 0, 0};
        // gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
        this.setLayout(gridBagLayout);

        // PADDING
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        // ASSET FAVORITES
        GridBagConstraints favoritesGBC = new GridBagConstraints();
        favoritesGBC.insets = new Insets(5, 5, 5, 0);
        favoritesGBC.fill = GridBagConstraints.BOTH;
        favoritesGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        favoritesGBC.weightx = 1.0;
        favoritesGBC.gridwidth = 5;
        favoritesGBC.gridx = 0;
        favoritesGBC.gridy = y;

        txtRecDetails = new JTextField();

        this.accountsModel = new org.erachain.gui.models.AccountsComboBoxModel();

        // LABEL FROM
        GridBagConstraints labelFromGBC = new GridBagConstraints();
        labelFromGBC.insets = new Insets(5, 10, 5, 5);
        labelFromGBC.fill = GridBagConstraints.HORIZONTAL;
        labelFromGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        labelFromGBC.weightx = 0;
        labelFromGBC.gridx = 0;
        labelFromGBC.gridy = ++y;
        JLabel fromLabel = new JLabel(Lang.T("Select account") + ":");
        this.add(fromLabel, labelFromGBC);
        // fontHeight =
        // fromLabel.getFontMetrics(fromLabel.getFont()).getHeight();

        // COMBOBOX FROM
        GridBagConstraints cbxFromGBC = new GridBagConstraints();
        cbxFromGBC.gridwidth = 6;
        cbxFromGBC.insets = new Insets(5, 5, 5, 10);
        cbxFromGBC.fill = GridBagConstraints.HORIZONTAL;
        cbxFromGBC.anchor = GridBagConstraints.NORTHWEST;
        cbxFromGBC.weightx = 0.1;
        cbxFromGBC.gridx = 1;
        cbxFromGBC.gridy = y;

        this.cbxFrom = new JComboBox<Account>(accountsModel);
        this.cbxFrom.setRenderer(new AccountRenderer(Transaction.FEE_KEY));
        this.add(this.cbxFrom, cbxFromGBC);
        if (accountFrom != null)
            cbxFrom.setSelectedItem(accountFrom);

        // LABEL TO
        GridBagConstraints labelToGBC = new GridBagConstraints();
        labelToGBC.gridy = ++y;
        labelToGBC.insets = new Insets(5, 10, 5, 10);
        labelToGBC.fill = GridBagConstraints.HORIZONTAL;
        labelToGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        labelToGBC.weightx = 0;
        labelToGBC.gridx = 0;
        JLabel toLabel = new JLabel(Lang.T("To: (address or name)"));
        this.add(toLabel, labelToGBC);

        // TXT TO
        GridBagConstraints txtToGBC = new GridBagConstraints();
        txtToGBC.gridwidth = 6;
        txtToGBC.insets = new Insets(5, 5, 5, 10);
        txtToGBC.fill = GridBagConstraints.HORIZONTAL;
        txtToGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        txtToGBC.weightx = 0.1;
        txtToGBC.gridx = 1;
        txtToGBC.gridy = y;

        recipientBox = new RecipientAddress(this);

        // LABEL RECEIVER
        GridBagConstraints labelDetailsGBC = new GridBagConstraints();
        labelDetailsGBC.gridy = ++y;
        labelDetailsGBC.insets = new Insets(5, 10, 5, 5);
        labelDetailsGBC.fill = GridBagConstraints.HORIZONTAL;
        labelDetailsGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        labelDetailsGBC.gridx = 0;
        JLabel recDetailsLabel = new JLabel(Lang.T("Receiver details") + ":");
        this.add(recDetailsLabel, labelDetailsGBC);

        // RECEIVER DETAILS
        GridBagConstraints txtReceiverGBC = new GridBagConstraints();
        txtReceiverGBC.gridwidth = 6;
        txtReceiverGBC.insets = new Insets(5, 5, 5, 10);
        txtReceiverGBC.fill = GridBagConstraints.HORIZONTAL;
        txtReceiverGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        txtReceiverGBC.gridx = 1;
        txtReceiverGBC.gridy = y;

        txtRecDetails.setEditable(false);
        this.add(txtRecDetails, txtReceiverGBC);

        // LABEL TITLE
        GridBagConstraints labelMessageGBC = new GridBagConstraints();
        labelMessageGBC.insets = new Insets(5, 10, 5, 5);
        labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;
        labelMessageGBC.anchor = GridBagConstraints.FIRST_LINE_START;
        labelMessageGBC.gridx = 0;
        labelMessageGBC.gridy = ++y;

        JLabel title_Label = new JLabel(Lang.T("Title") + ":");
        this.add(title_Label, labelMessageGBC);

        // TXT TITLE
        GridBagConstraints txtMessageGBC = new GridBagConstraints();
        txtMessageGBC.gridwidth = 6;
        txtMessageGBC.insets = new Insets(5, 5, 5, 10);
        txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;
        txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
        txtMessageGBC.gridx = 1;
        txtMessageGBC.gridy = y;

        txt_Title = new JTextField();

        this.add(txt_Title, txtMessageGBC);

        // LABEL MESSAGE
        // GridBagConstraints labelMessageGBC = new GridBagConstraints();
        labelMessageGBC.insets = new Insets(5, 10, 5, 5);
        labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;
        labelMessageGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        labelMessageGBC.gridx = 0;
        labelMessageGBC.gridy = ++y;

        messageLabel = new JLabel(Lang.T("Message") + ":");

        // TXT MESSAGE
        // GridBagConstraints txtMessageGBC = new GridBagConstraints();
        txtMessageGBC.gridwidth = 6;
        txtMessageGBC.insets = new Insets(5, 5, 5, 10);
        // txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;
        txtMessageGBC.fill = java.awt.GridBagConstraints.BOTH;
        txtMessageGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        txtMessageGBC.gridx = 1;
        txtMessageGBC.gridy = y;
        txtMessageGBC.weighty = 0.2;

        this.txtMessage = new JTextArea();
        this.txtMessage.setRows(4);
        this.txtMessage.setColumns(25);
        // this.txtMessage.setMinimumSize(new Dimension(200,150));

        this.txtMessage.setBorder(this.recipientBox.getBorder());

        JScrollPane messageScroll = new JScrollPane(this.txtMessage);
        // messageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        // messageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(messageScroll, txtMessageGBC);

        this.add(messageLabel, labelMessageGBC);

        fieldGBC.gridy = ++y;
        encrypted = new JCheckBox(Lang.T("Encrypt message"));
        encrypted.setSelected(true);
        this.add(encrypted, fieldGBC);

        // TEXT ISTEXT
        fieldGBC.gridy = ++y;
        isText = new JCheckBox(Lang.T("As Text"));
        isText.setSelected(true);
        this.add(isText, fieldGBC);

        //exLink
        exLinkDescriptionLabel = new JLabel();
        exLinkTextLabel = new JLabel();
        exLinkText = new JTextField();
        exLinkDescription = new JTextField();

        exLinkTextLabel.setText(Lang.T("Append to") + ":");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = ++y;
        gridBagConstraints.anchor= GridBagConstraints.LINE_START;
        gridBagConstraints.insets =  new Insets(5, 10, 5, 10);
        add(exLinkTextLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 15);
        add(exLinkText, gridBagConstraints);

        exLinkDescriptionLabel.setText(Lang.T("Parent") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = y;
        gridBagConstraints.anchor= GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        add(exLinkDescriptionLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new Insets(5, 5, 5, 10);
        add(exLinkDescription, gridBagConstraints);

        // exlink


        // LABEL AMOUNT
        GridBagConstraints amountlabelGBC = new GridBagConstraints();
        amountlabelGBC.insets = new Insets(5, 5, 5, 5);
        amountlabelGBC.fill = GridBagConstraints.HORIZONTAL;
        amountlabelGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        amountlabelGBC.weightx = 0;
        amountlabelGBC.gridx = 0;
        amountlabelGBC.gridy = ++y;

        final JLabel amountLabel = new JLabel(Lang.T("Amount") + ":");
        amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        // this.add(amountLabel, amountlabelGBC);

        // TXT AMOUNT
        GridBagConstraints txtAmountGBC = new GridBagConstraints();
        txtAmountGBC.insets = new Insets(5, 5, 5, 5);
        txtAmountGBC.fill = GridBagConstraints.HORIZONTAL;
        txtAmountGBC.anchor = GridBagConstraints.FIRST_LINE_START;// .NORTHWEST;
        txtAmountGBC.weightx = 0;
        txtAmountGBC.gridx = 1;
        txtAmountGBC.gridy = y;

        txtAmount = new JTextField("0.00000000");
        txtAmount.setPreferredSize(new Dimension(130, 22));
        // this.add(txtAmount, txtAmountGBC);

        // LABEL GBC
        GridBagConstraints feelabelGBC = new GridBagConstraints();
        feelabelGBC.anchor = GridBagConstraints.EAST;
        feelabelGBC.gridy = y;
        feelabelGBC.insets = new Insets(5, 5, 5, 5);
        feelabelGBC.fill = GridBagConstraints.BOTH;
        feelabelGBC.weightx = 0;
        feelabelGBC.gridx = 2;
        final JLabel feeLabel = new JLabel(Lang.T("Fee Power") + ":");
        feeLabel.setVisible(Gui.SHOW_FEE_POWER);
        feeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        feeLabel.setVerticalAlignment(SwingConstants.TOP);
        this.add(feeLabel, feelabelGBC);

        // FEE TXT
        GridBagConstraints feetxtGBC = new GridBagConstraints();
        feetxtGBC.fill = GridBagConstraints.BOTH;
        feetxtGBC.insets = new Insets(5, 5, 5, 5);
        feetxtGBC.anchor = GridBagConstraints.NORTH;
        feetxtGBC.gridx = 3;
        feetxtGBC.gridy = y;

        txtFeePow = new JComboBox<String>();
        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFeePow.setSelectedIndex(0);
        txtFeePow.setPreferredSize(new Dimension(130, 22));
        txtFeePow.setVisible(Gui.SHOW_FEE_POWER);
        this.add(txtFeePow, feetxtGBC);

        // BUTTON DECRYPTALL
        GridBagConstraints decryptAllGBC = new GridBagConstraints();
        decryptAllGBC.insets = new Insets(5, 5, 5, 5);
        decryptAllGBC.fill = GridBagConstraints.HORIZONTAL;
        decryptAllGBC.anchor = GridBagConstraints.NORTHWEST;
        decryptAllGBC.gridwidth = 1;
        decryptAllGBC.gridx = 3;
        decryptAllGBC.gridy = ++y;
        JButton decryptButton = new JButton(Lang.T("Decrypt All"));
        // this.add(decryptButton, decryptAllGBC);

        // BUTTON SEND
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 25, 5, 5);
        buttonGBC.fill = GridBagConstraints.BOTH;
        buttonGBC.anchor = GridBagConstraints.PAGE_START;
        buttonGBC.gridx = 2;
        buttonGBC.gridy = y;

        // sendButton.setPreferredSize(new Dimension(80, 25));
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSendClick();
            }
        });
        this.add(sendButton, buttonGBC);

        // MESSAGES HISTORY TABLE

        messagesHistoryTable = new MailsHTMLTableModel(this, (Account) cbxFrom.getSelectedItem());

        cbxFrom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messagesHistoryTable.setMyAccount((Account) cbxFrom.getSelectedItem());
            }
        });

        messagesHistoryTable.setTableHeader(null);
        messagesHistoryTable.setEditingColumn(0);
        messagesHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane historyScrollPane = new JScrollPane(messagesHistoryTable);
        historyScrollPane.setPreferredSize(new Dimension(100, 100));
        historyScrollPane.setWheelScrollingEnabled(true);

        // BOTTOM GBC
        GridBagConstraints messagesGBC = new GridBagConstraints();
        messagesGBC.insets = new Insets(5, 5, 5, 5);
        messagesGBC.fill = GridBagConstraints.BOTH;
        messagesGBC.anchor = GridBagConstraints.NORTHWEST;
        messagesGBC.weightx = 0;
        messagesGBC.gridx = 0;

        // ADD BOTTOM SO IT PUSHES TO TOP
        messagesGBC.gridy = ++y;
        messagesGBC.weighty = 4;
        messagesGBC.gridwidth = 7;

        add(historyScrollPane, messagesGBC);

        // BUTTON DECRYPTALL
        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((MailsHTMLTableModel) messagesHistoryTable).CryptoOpenBoxAll();
            }
        });

        // CONTEXT MENU
        MenuPopupUtil.installContextMenu(txtAmount);
        MenuPopupUtil.installContextMenu(txtMessage);
        MenuPopupUtil.installContextMenu(txtRecDetails);

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {

                messageLabel.setText("<html>" + Lang.T("Message") + ":<br>("
                        + txtMessage.getText().length() + ")</html>");

            }
        }, 0, 500, TimeUnit.MILLISECONDS);

        // if person show selectbox with all adresses for person
        if (person != null) {

            AccountsComboBoxModel accounts_To_Model = new AccountsComboBoxModel(person.getKey());
            this.cbx_To = new JComboBox(accounts_To_Model);
            if (accounts_To_Model.getSize() != 0) {
                this.add(this.cbx_To, txtToGBC);
                recipientBox.setSelectedAccount((Account) cbx_To.getSelectedItem());
                Account account1 = new Account(recipientBox.getSelectedAddress());
                txtRecDetails.setText(account1.toString());
                toLabel.setText(Lang.T("Select Account To") + ": ");
                cbx_To.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String str = (String) cbx_To.getSelectedItem();
                        if (str != null) {
                            recipientBox.setSelectedAccount((Account) cbx_To.getSelectedItem());
                            refreshReceiverDetails();
                        }

                    }
                });
            } else {

                this.recipientBox.setMessage("has no Accounts");
                sendButton.setEnabled(false);

            }
        } else {

            if (accountTo != null) {
                recipientBox.setSelectedAccount(accountTo);
            }
            this.add(recipientBox, txtToGBC);
        }

        /*
         * this.pack(); this.setLocationRelativeTo(null);
         * this.setMaximizable(true);
         * this.setTitle(Lang.T("Persons"));
         * this.setClosable(true); this.setResizable(true);
         */

        // Container parent = this.getParent();
        // this.setSize(new Dimension(
        // (int)parent.getSize().getWidth()-80,(int)parent.getSize().getHeight()-150));
        // this.setLocation(20, 20);
        // this.setIconImages(icons);

        // CLOSE
        // setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        // this.setResizable(true);
        // splitPane_1.setDividerLocation((int)((double)(this.getHeight())*0.7));//.setDividerLocation(.8);
        // this.setVisible(true);
        refreshReceiverDetails();
        this.setMinimumSize(new Dimension(0, 0));

    }

    private void refreshReceiverDetails() {
        String recipient = recipientBox.getSelectedAddress();

        AssetCls asset = Controller.getInstance().getAsset(Transaction.FEE_KEY);
        sendButton.setEnabled(false);

        if (recipient.isEmpty()) {
            txtRecDetails.setText("");
            messagesHistoryTable.setSideAccount(null);
            return;
        }

        this.txtRecDetails.setText(Lang.T(
                Account.getDetailsForEncrypt(recipient, asset.getKey(),
                        encrypted.isSelected(), true)));

        Tuple2<Account, String> accountRes = Account.tryMakeAccount(recipient);
        if (accountRes.b == null) {
            messagesHistoryTable.setSideAccount(accountRes.a);
        }

        sendButton.setEnabled(true);

    }

    public void onSendClick() {
        // DISABLE
        this.sendButton.setEnabled(false);

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

        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (password.equals("")) {
                this.sendButton.setEnabled(true);
                return;
            }
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"),
                        Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                this.sendButton.setEnabled(true);
                return;
            }
        }

        // READ SENDER
        Account sender = (Account) cbxFrom.getSelectedItem();

        // READ RECIPIENT
        String recipientAddress = recipientBox.getSelectedAddress();

        // ORDINARY RECIPIENT
        Tuple2<Account, String> accountRes = Account.tryMakeAccount(recipientAddress);
        Account recipient = accountRes.a;
        if (recipient == null) {
            JOptionPane.showMessageDialog(null, accountRes.b, Lang.T("Error"),
                    JOptionPane.ERROR_MESSAGE);

            // ENABLE
            this.sendButton.setEnabled(true);
            return;
        }

        int parsing = 0;
        int feePow = 0;
        BigDecimal amount = null;
        try {
            // READ AMOUNT
            parsing = 1;
            amount = new BigDecimal(txtAmount.getText());

            // READ FEE
            parsing = 2;
            feePow = Integer.parseInt((String) this.txtFeePow.getSelectedItem());
        } catch (Exception e) {
            // CHECK WHERE PARSING ERROR HAPPENED
            switch (parsing) {
                case 1:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid amount!"),
                            Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    break;

                case 2:

                    JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee!"),
                            Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    break;
            }
            // ENABLE
            this.sendButton.setEnabled(true);
            return;
        }

        String message = txtMessage.getText();

        boolean isTextB = isText.isSelected();

        byte[] messageBytes = null;

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
                        JOptionPane.showMessageDialog(new JFrame(),
                                Lang.T("Message format is not base58 or hex!"),
                                Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                        // ENABLE
                        this.sendButton.setEnabled(true);
                        return;
                    }
                }
            }
        }

        // if no TEXT - set null
        if (messageBytes != null && messageBytes.length == 0)
            messageBytes = null;
        // if amount = 0 - set null
        if (amount.compareTo(BigDecimal.ZERO) == 0)
            amount = null;

        boolean encryptMessage = encrypted.isSelected();

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
                this.sendButton.setEnabled(true);
                return;
            }

            if (encryptMessage) {
                // sender
                PrivateKeyAccount creator = Controller.getInstance()
                        .getWalletPrivateKeyAccountByAddress(sender.getAddress());
                if (creator == null) {
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                            Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                byte[] privateKey = creator.getPrivateKey();

                // recipient
                byte[] publicKey;
                if (recipient instanceof PublicKeyAccount) {
                    publicKey = ((PublicKeyAccount) recipient).getPublicKey();
                } else {
                    publicKey = Controller.getInstance().getPublicKey(recipient);
                }
                if (publicKey == null) {
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.T(
                                    ApiErrorFactory.getInstance().messageError(ApiErrorFactory.ERROR_NO_PUBLIC_KEY)),
                            Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                    // ENABLE
                    this.sendButton.setEnabled(true);

                    return;
                }

                messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
            }
        }

        String head = this.txt_Title.getText();
        if (head == null)
            head = "";
        if (head.getBytes(StandardCharsets.UTF_8).length > 256) {

            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T("Title size exceeded!") + " <= 256",
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;

        }

        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        ExLink exLink = null;
        Long linkRef = Transaction.parseDBRef("-");
        if (linkRef != null) {
            exLink = new ExLinkAppendix(linkRef);
        }

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send(creator, exLink, feePow, recipient, key,
                amount, head, messageBytes, isTextByte, encrypted, 0);
        // test result = new Pair<Transaction, Integer>(null,
        // Transaction.VALIDATE_OK);

        String Status_text = "";
        IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                Lang.T("Send Mail"), (int) (th.getWidth() / 1.2), (int) (th.getHeight() / 1.2),
                Status_text, Lang.T("Confirmation transaction send mail"));

        MailInfo ww = new MailInfo((RSend) transaction);
        ww.jTabbedPane1.setVisible(false);
        confirmDialog.jScrollPane1.setViewportView(ww);
        confirmDialog.setLocationRelativeTo(th);
        confirmDialog.setVisible(true);

        // JOptionPane.OK_OPTION
        if (confirmDialog.isConfirm > 0) {
            ResultDialog.make(this, transaction, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE, null);
        }
        // ENABLE
        this.sendButton.setEnabled(true);
    }

    // выполняемая процедура при изменении адреса получателя
    @Override
    public void recipientAddressWorker(String e) {
        refreshReceiverDetails();
    }

}
