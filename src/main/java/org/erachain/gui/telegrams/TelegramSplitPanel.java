package org.erachain.gui.telegrams;

import org.erachain.api.ApiErrorFactory;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.wallet.FavoriteAccountsMap;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.Converter;
import org.erachain.utils.TableMenuPopupUtil;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * @author Саша
 */
public class TelegramSplitPanel extends SplitPanel {

    public static String NAME = "TelegramSplitPanel";
    public static String TITLE = "Telegrams";

    /**
     * Creates new form TelegramSplitPanel
     */
    LeftTelegram leftTelegram;
    RightTelegramPanel rightTelegramPanel;
    private static final long serialVersionUID = 1L;
    public AccountsPanel accountPanel;
    private FavoriteAccountsTableModel accountModel;
    private MTable tableFavoriteAccounts;
    protected int row;
    private Account recipient;
    private Account sender;


    @SuppressWarnings("rawtypes")
    public TelegramSplitPanel() {
        super(NAME, TITLE);

        this.jScrollPanelLeftPanel.setVisible(false);
        this.searchToolBar_LeftPanel.setVisible(false);
        this.toolBarLeftPanel.setVisible(false);
        this.setName(Lang.T("My Accounts"));
        this.jToolBarRightPanel.setVisible(false);

        GridBagConstraints PanelGBC = new GridBagConstraints();
        PanelGBC.fill = GridBagConstraints.BOTH;
        PanelGBC.anchor = GridBagConstraints.NORTHWEST;
        PanelGBC.weightx = 1;
        PanelGBC.weighty = 1;
        PanelGBC.gridx = 0;
        PanelGBC.gridy = 0;
        accountPanel = new AccountsPanel();
        leftTelegram = new LeftTelegram();
        rightTelegramPanel = new RightTelegramPanel();
        this.leftPanel.add(leftTelegram, PanelGBC);
        this.jSplitPanel.setLeftComponent(leftTelegram);
        this.jSplitPanel.setRightComponent(rightTelegramPanel);
        // EVENTS on CURSOR
        // accountPanel.table.getSelectionModel().addListSelectionListener(new Account_Tab_Listener());

        accountModel = new FavoriteAccountsTableModel();
        tableFavoriteAccounts = new MTable(this.accountModel);


        //get telegram
        leftTelegram.jButtonGetTelegrams.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                Controller.getInstance().telegramStore.broadcastGetTelegram(sender.getAddress());
            }

        });

        leftTelegram.jScrollPaneCenter.setViewportView(tableFavoriteAccounts);

        //   leftTelegram.jTableFavoriteAccounts.setModel(accountModel);
        leftTelegram.jButtonAddAccount.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                // TODO Auto-generated method stub
                new AccountNameAdd();

            }

        });


// set position sender
        if (Settings.getInstance().getTelegramDefaultSender() != null) {

            try {
                leftTelegram.jComboAccount
                        .setSelectedItem(new Account(Settings.getInstance().getTelegramDefaultSender()));
                leftTelegram.jComboAccount.repaint();
                rightTelegramPanel.jLabelLeft.setText(Settings.getInstance().getTelegramDefaultSender());
                rightTelegramPanel.walletTelegramsFilterTableModel.setSender(Settings.getInstance().getTelegramDefaultSender());
                sender = (Account) this.leftTelegram.jComboAccount.getSelectedItem();
            } catch (Exception e) {
                // TODO: handle exception
            }
        } else {
            leftTelegram.jComboAccount
                    .setSelectedIndex(0);
            rightTelegramPanel.walletTelegramsFilterTableModel.setSender(leftTelegram.jComboAccount.getItemAt(0).getAddress());
            rightTelegramPanel.jLabelLeft.setText(leftTelegram.jComboAccount.getItemAt(0).getAddress());
            leftTelegram.jComboAccount.repaint();
            sender = (Account) this.leftTelegram.jComboAccount.getSelectedItem();
        }

        leftTelegram.jComboAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Account asset = ((Account) leftTelegram.jComboAccount.getSelectedItem());

                if (asset != null) {
                    rightTelegramPanel.jLabelLeft.setText(asset.getAddress());
                    rightTelegramPanel.walletTelegramsFilterTableModel.setSender(asset.getAddress());
                    Settings.getInstance().setTelegramDefaultSender(asset.getAddress());
                    sender = (Account) leftTelegram.jComboAccount.getSelectedItem();

                }

            }
        });


        // set position from table recievers
        if (Settings.getInstance().getTelegramDefaultReciever() != null) {
            rightTelegramPanel.jLabelRecipient.setText(Settings.getInstance().getTelegramDefaultReciever());
            rightTelegramPanel.walletTelegramsFilterTableModel.setReceiver(Settings.getInstance().getTelegramDefaultReciever());
            int k = accountModel.getRowCount();
            for (int i = 0; i < k; i++) {
                if (accountModel.getItem(i).a.equals(Settings.getInstance().getTelegramDefaultReciever())) {
                    tableFavoriteAccounts.setRowSelectionInterval(tableFavoriteAccounts.convertRowIndexToModel(i), tableFavoriteAccounts.convertRowIndexToModel(i));
                }
            }

        } else {

        }

        tableFavoriteAccounts.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                // TODO Auto-generated method stub
                if (tableFavoriteAccounts.getSelectedRow() < 0
                        || tableFavoriteAccounts.getSelectedRow() >= accountModel.getRowCount()) {
                    rightTelegramPanel.jLabelRecipient.setText("");
                    return;
                }
                String account = (String) accountModel.getValueAt((tableFavoriteAccounts.convertRowIndexToModel(
                        tableFavoriteAccounts.getSelectedRow())), accountModel.COLUMN_ADDRESS);
                rightTelegramPanel.jLabelRecipient.setText(account);
                rightTelegramPanel.walletTelegramsFilterTableModel.setReceiver(account);
                // set settings
                Settings.getInstance().setTelegramDefaultReciever(account);
            }


        });
        // view ratiobutton
        // set position from table recievers
        String a = Settings.getInstance().getTelegramRatioReciever();
        if (Settings.getInstance().getTelegramRatioReciever() == null
                || Settings.getInstance().getTelegramRatioReciever().equals("all")
        ) {
            this.leftTelegram.jCxbAllmessages.setSelected(true);
            tableFavoriteAccounts.setVisible(false);
            rightTelegramPanel.jLabelRecipient.setText(Lang.T("All"));
            rightTelegramPanel.walletTelegramsFilterTableModel.setReceiver(null);
            rightTelegramPanel.jPanelBottom.setVisible(false);
            leftTelegram.jButtonAddAccount.setVisible(false);
        } else {
            this.leftTelegram.jCxbRecipientmessages.setSelected(true);
            tableFavoriteAccounts.setVisible(true);
            rightTelegramPanel.jPanelBottom.setVisible(true);
            leftTelegram.jButtonAddAccount.setVisible(true);
            if (tableFavoriteAccounts.getSelectedRow() >= 0) {
                String account = (String) accountModel.getValueAt((tableFavoriteAccounts.convertRowIndexToModel(
                        tableFavoriteAccounts.getSelectedRow())), accountModel.COLUMN_ADDRESS);
                rightTelegramPanel.jLabelRecipient.setText(account);
                rightTelegramPanel.walletTelegramsFilterTableModel.setReceiver(account);
            } else {
                rightTelegramPanel.jLabelRecipient.setText("");
            }
        }

        this.leftTelegram.jCxbAllmessages.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                tableFavoriteAccounts.setVisible(false);
                Settings.getInstance().setTelegramRatioReciever("all");
                rightTelegramPanel.jLabelRecipient.setText(Lang.T("All"));
                rightTelegramPanel.walletTelegramsFilterTableModel.setReceiver(null);
                rightTelegramPanel.jPanelBottom.setVisible(false);
                leftTelegram.jButtonAddAccount.setVisible(false);
            }

        });

        this.leftTelegram.jCxbRecipientmessages.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                tableFavoriteAccounts.setVisible(true);
                Settings.getInstance().setTelegramRatioReciever("recipient");
                rightTelegramPanel.jPanelBottom.setVisible(true);
                leftTelegram.jButtonAddAccount.setVisible(true);
                if (tableFavoriteAccounts.getSelectedRow() < 0
                        || tableFavoriteAccounts.getSelectedRow() >= accountModel.getRowCount()) {
                    rightTelegramPanel.jLabelRecipient.setText("");
                    return;
                }
                String account = (String) accountModel.getValueAt((tableFavoriteAccounts.convertRowIndexToModel(tableFavoriteAccounts.getSelectedRow())), accountModel.COLUMN_ADDRESS);
                rightTelegramPanel.jLabelRecipient.setText(account);
                rightTelegramPanel.walletTelegramsFilterTableModel.setReceiver(account);

            }

        });

        rightTelegramPanel.jLabelCenter.setText(" <->");
        rightTelegramPanel.jButtonSendTelegram.setText(Lang.T("Send"));
        rightTelegramPanel.jButtonSendTelegram.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                onSendClick();
            }


        });

        // menu

        JPopupMenu menu = new JPopupMenu();

        menu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                // TODO Auto-generated method stub
                int row1 = tableFavoriteAccounts.getSelectedRow();
                if (row1 < 0)
                    return;

                row = tableFavoriteAccounts.convertRowIndexToModel(row1);

            }
        });

        JMenuItem copyAddress = new JMenuItem(Lang.T("Copy Account"));
        copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Tuple2<String, Tuple3<String, String, String>> account = accountModel.getItem(row);
                StringSelection value = new StringSelection(account.a);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(value, null);
            }
        });
        menu.add(copyAddress);

        JMenuItem menu_copyPublicKey = new JMenuItem(Lang.T("Copy Public Key"));
        menu_copyPublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String publicKey58;
                Tuple2<String, Tuple3<String, String, String>> item = accountModel.getItem(row);
                if (item.b.a == null) {
                    byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(item.a);
                    if (publicKey == null) {
                        publicKey58 = "not found";
                    } else {
                        publicKey58 = new PublicKeyAccount(publicKey).getBase58();
                    }
                } else {
                    publicKey58 = item.b.a;
                }
                StringSelection value = new StringSelection(publicKey58);

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(value, null);
            }
        });
        menu.add(menu_copyPublicKey);

        JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.T("Send asset"));
        Send_Coins_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tuple2<String, Tuple3<String, String, String>> item = accountModel.getItem(row);
                Account accountTo = FavoriteAccountsMap.detPublicKeyOrAccount(item.a, item.b);
                MainPanel.getInstance().insertNewTab(Lang.T("Send asset"),
                        new AccountAssetSendPanel(null, null,
                                null, accountTo, null, null, false));


            }
        });
        menu.add(Send_Coins_item_Menu);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.T("Send mail"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tuple2<String, Tuple3<String, String, String>> item = accountModel.getItem(row);
                Account accountTo = FavoriteAccountsMap.detPublicKeyOrAccount(item.a, item.b);
                MainPanel.getInstance().insertNewTab(Lang.T("Send Mail"),
                        new MailSendPanel(null, accountTo, null));

            }
        });
        menu.add(Send_Mail_item_Menu);

        JMenuItem setName = new JMenuItem(Lang.T("Edit name"));
        setName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tuple2<String, Tuple3<String, String, String>> item = accountModel.getItem(row);
                new AccountSetNameDialog(item.a);
                tableFavoriteAccounts.repaint();

            }
        });
        menu.add(setName);

        JMenuItem menuItemDelete = new JMenuItem(Lang.T("Remove Favorite"));
        menuItemDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (false && !Controller.getInstance().isWalletUnlocked()) {
                    // ASK FOR PASSWORD
                    String password = PasswordPane.showUnlockWalletDialog(MainFrame.getInstance());
                    if (password.equals("")) {

                        return;
                    }
                    if (!Controller.getInstance().unlockWallet(password)) {
                        // WRONG PASSWORD
                        JOptionPane.showMessageDialog(null, Lang.T("Invalid password"),
                                Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                        // ENABLE

                        return;
                    }
                }

                int row = tableFavoriteAccounts.getSelectedRow();
                try {
                    row = tableFavoriteAccounts.convertRowIndexToModel(row);
                    Tuple2<String, Tuple3<String, String, String>> item = accountModel.getItem(row);
                    Controller.getInstance().getWallet().dwSet.getFavoriteAccountsMap().delete(item.a);
                } catch (Exception e1) {
                    logger.error(e1.getMessage(), e1);
                }

            }
        });
        menu.add(menuItemDelete);

        TableMenuPopupUtil.installContextMenu(tableFavoriteAccounts, menu);


    }

    public void onClose() {
        //  rightPanel.tableModel.deleteObserver();
        accountPanel.tableModel.deleteObservers();
        Controller.getInstance().deleteObserver(accountPanel.updateButton);
        Controller.getInstance().deleteObserver(accountPanel.newAccountButton);
    }

    public void onSendClick() {

        this.rightTelegramPanel.jButtonSendTelegram.setEnabled(false);
        //CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            //ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (password.equals("")) {
                this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
                return;
            }
            if (!Controller.getInstance().unlockWallet(password)) {
                //WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
                return;
            }
        }

        //READ SENDER
        sender = (Account) this.leftTelegram.jComboAccount.getSelectedItem();

        //READ RECIPIENT
        String recipientAddress = rightTelegramPanel.jLabelRecipient.getText();

        Tuple2<Account, String> result = Account.tryMakeAccount(recipientAddress);
        //ORDINARY RECIPIENT
        if (result.b == null) {
            this.recipient = result.a;
        } else {
            JOptionPane.showMessageDialog(null, Lang.T(result.b), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
            return;
        }

        BigDecimal amount = null;
        int feePow = 0;
        boolean isTextB = true;

        byte[] messageBytes = null;
        String message = rightTelegramPanel.jTextPaneText.getText();

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
                        JOptionPane.showMessageDialog(new JFrame(), Lang.T("Message format is not base58 or hex") + "!",
                                Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                        //ENABLE
                        this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
                        return;
                    }
                }
            }
        }
        // if no TEXT - set null
        if (messageBytes != null && messageBytes.length == 0) messageBytes = null;
        // if amount = 0 - set null


        boolean encryptMessage = rightTelegramPanel.checkIsEncrypt.isSelected();

        byte[] encrypted = (encryptMessage) ? new byte[]{1} : new byte[]{0};
        byte[] isTextByte = new byte[]{1};

        Long key = 1L;


        if (messageBytes != null) {
            if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.T("Message size exceeded") + " <= MAX", Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
                return;
            }

            if (encryptMessage) {
                //sender
                PrivateKeyAccount account = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress().toString());
                byte[] privateKey = account.getPrivateKey();

                //recipient
                byte[] publicKey = Controller.getInstance().getPublicKeyByAddress(recipient.getAddress());
                if (publicKey == null) {
                    JOptionPane.showMessageDialog(new JFrame(), Lang.T(
                            ApiErrorFactory.getInstance().messageError(ApiErrorFactory.ERROR_NO_PUBLIC_KEY)
                    ), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

                    //ENABLE
                    this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);

                    return;
                }

                messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
            }
        }
        String head = rightTelegramPanel.jTxtTitle.getText();

        if (head == null) head = "";
        if (head.getBytes(StandardCharsets.UTF_8).length > 256) {

            JOptionPane.showMessageDialog(new JFrame(), Lang.T("Title size exceeded") + " <= 256",
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;

        }

        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.T("Wallet is busy, try later") + "!",
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
        } else {
            // CREATE TX MESSAGE
            Transaction transaction = Controller.getInstance().r_Send(
                    creator, null, smartContract, feePow, recipient, key,
                    amount, head, messageBytes, isTextByte, encrypted, 0);

            Controller.getInstance().broadcastTelegram(transaction, true);
        }


        // ENABLE
        rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
    }

    public boolean checkError() {

        return true;
    }

}