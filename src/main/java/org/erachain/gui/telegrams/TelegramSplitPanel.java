package org.erachain.gui.telegrams;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.AEScrypto;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.Converter;
import org.erachain.utils.TableMenuPopupUtil;
import org.mapdb.Fun.Tuple2;

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

    /**
     * Creates new form TelegramSplitPanel
     */
    private static String iconFile = Settings.getInstance().getPatnIcons() + "TelegramSplitPanel.png";
    LeftTelegram leftTelegram;
    RightTelegramPanel rightTelegramPanel;
    private static final long serialVersionUID = 1L;
    public AccountsPanel accountPanel;
    //  public AssetCls assetSelect;
    private Account selecArg;
    //   private RightTelegramPanel rightPanel;
    private AccountsComboBoxModel accountsModel;
    static TelegramSplitPanel th;
    private FavoriteAccountsTableModel accountModel;
    private MTable tableFavoriteAccounts;
    protected int row;
    private Account recipient;
    private Account sender;


    @SuppressWarnings("rawtypes")
    public TelegramSplitPanel() {
        super("TelegramSplitPanel");
        //th = this;
        this.jScrollPanelLeftPanel.setVisible(false);
        this.searchToolBar_LeftPanel.setVisible(false);
        this.toolBarLeftPanel.setVisible(false);
        this.setName(Lang.getInstance().translate("My Accounts"));
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
            rightTelegramPanel.jLabelRaght.setText(Settings.getInstance().getTelegramDefaultReciever());
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
                    rightTelegramPanel.jLabelRaght.setText("");
                    return;
                }
                String account = (String) accountModel.getValueAt((tableFavoriteAccounts.convertRowIndexToModel(tableFavoriteAccounts.getSelectedRow())), accountModel.COLUMN_ADDRESS);
                rightTelegramPanel.jLabelRaght.setText(account);
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
            rightTelegramPanel.jLabelRaght.setText(Lang.getInstance().translate("All"));
            rightTelegramPanel.walletTelegramsFilterTableModel.setReceiver(null);
            rightTelegramPanel.jPanelBottom.setVisible(false);
            leftTelegram.jButtonAddAccount.setVisible(false);
        } else {
            this.leftTelegram.jCxbRecipientmessages.setSelected(true);
            tableFavoriteAccounts.setVisible(true);
            rightTelegramPanel.jPanelBottom.setVisible(true);
            leftTelegram.jButtonAddAccount.setVisible(true);
            if (tableFavoriteAccounts.getSelectedRow() >= 0) {
                String account = (String) accountModel.getValueAt((tableFavoriteAccounts.convertRowIndexToModel(tableFavoriteAccounts.getSelectedRow())), accountModel.COLUMN_ADDRESS);
                rightTelegramPanel.jLabelRaght.setText(account);
                rightTelegramPanel.walletTelegramsFilterTableModel.setReceiver(account);
            } else {
                rightTelegramPanel.jLabelRaght.setText("");
            }
        }

        this.leftTelegram.jCxbAllmessages.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                tableFavoriteAccounts.setVisible(false);
                Settings.getInstance().setTelegramRatioReciever("all");
                rightTelegramPanel.jLabelRaght.setText(Lang.getInstance().translate("All"));
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
                    rightTelegramPanel.jLabelRaght.setText("");
                    return;
                }
                String account = (String) accountModel.getValueAt((tableFavoriteAccounts.convertRowIndexToModel(tableFavoriteAccounts.getSelectedRow())), accountModel.COLUMN_ADDRESS);
                rightTelegramPanel.jLabelRaght.setText(account);
                rightTelegramPanel.walletTelegramsFilterTableModel.setReceiver(account);

            }

        });

        rightTelegramPanel.jLabelCenter.setText(" <->");
        rightTelegramPanel.jButtonSendTelegram.setText(Lang.getInstance().translate("Send"));
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

        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Copy Account"));
        copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Tuple2<String, Tuple2<String, String>> account = accountModel.getItem(row);
                StringSelection value = new StringSelection(account.a);
                clipboard.setContents(value, null);
            }
        });
        menu.add(copyAddress);

        JMenuItem menu_copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
        menu_copyPublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                Tuple2<String, Tuple2<String, String>> account = accountModel.getItem(row);
                byte[] publick_Key = Controller.getInstance().getPublicKeyByAddress(account.a);
                PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
                StringSelection value = new StringSelection(public_Account.getBase58());
                clipboard.setContents(value, null);
            }
        });
        menu.add(menu_copyPublicKey);

        JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send asset"));
        Send_Coins_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tuple2<String, Tuple2<String, String>> account1 = accountModel.getItem(row);
                Account account = new Account(account1.a);
                MainPanel.getInstance().insertNewTab(Lang.getInstance().translate("Send asset"),
                        new AccountAssetSendPanel(null,
                                null, account, null, null), AccountAssetSendPanel.getIcon());


            }
        });
        menu.add(Send_Coins_item_Menu);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send mail"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tuple2<String, Tuple2<String, String>> account1 = accountModel.getItem(row);
                Account account = new Account(account1.a);
                MainPanel.getInstance().insertNewTab(Lang.getInstance().translate("Send Mail"),
                        new MailSendPanel(null, account, null), MailSendPanel.getIcon());

            }
        });
        menu.add(Send_Mail_item_Menu);

        JMenuItem setName = new JMenuItem(Lang.getInstance().translate("Edit name"));
        setName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tuple2<String, Tuple2<String, String>> account1 = accountModel.getItem(row);

                new AccountSetNameDialog(account1.b.a);
                tableFavoriteAccounts.repaint();

            }
        });
        menu.add(setName);

        JMenuItem menuItemDelete = new JMenuItem(Lang.getInstance().translate("Remove Favorite"));
        menuItemDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!Controller.getInstance().isWalletUnlocked()) {
                    // ASK FOR PASSWORD
                    String password = PasswordPane.showUnlockWalletDialog(MainFrame.getInstance());
                    if (password.equals("")) {

                        return;
                    }
                    if (!Controller.getInstance().unlockWallet(password)) {
                        // WRONG PASSWORD
                        JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                                Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                        // ENABLE

                        return;
                    }
                }

                int row = tableFavoriteAccounts.getSelectedRow();
                try {
                    row = tableFavoriteAccounts.convertRowIndexToModel(row);
                    Tuple2<String, Tuple2<String, String>> ac = accountModel.getItem(row);
                    Controller.getInstance().wallet.database.getFavoriteAccountsMap().delete(ac.a);
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
        Controller.getInstance().deleteObserver(accountPanel.reload_Button);
        Controller.getInstance().deleteObserver(accountPanel.newAccount_Button);
    }

    class Account_Tab_Listener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent arg0) {

            AssetCls asset = (AssetCls) accountPanel.cbxFavorites.getSelectedItem();
            Account account = null;
            //      if (accountPanel.table.getSelectedRow() >= 0)
            //           account = accountPanel.tableModel.getAccount(accountPanel.table.convertRowIndexToModel(accountPanel.table.getSelectedRow()));
            if (account == null) return;
            if (asset == null) return;
            //       if (account.equals(selecArg) && asset.equals(assetSelect)) return;
            selecArg = account;
//        assetSelect = asset;
            //       rightPanel.tableModel.set_Account(account);
            //       rightPanel.tableModel.fireTableDataChanged();
            //       rightPanel.set_Asset(asset);
            //     jScrollPaneJPanelRightPanel.setViewportView(rightPanel);

        }

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
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
                return;
            }
        }

        //READ SENDER
        sender = (Account) this.leftTelegram.jComboAccount.getSelectedItem();

        //READ RECIPIENT
        String recipientAddress = rightTelegramPanel.jLabelRaght.getText();

        Tuple2<Account, String> result = Account.tryMakeAccount(recipientAddress);
        //ORDINARY RECIPIENT
        if (result.b == null) {
            this.recipient = result.a;
        } else {
            JOptionPane.showMessageDialog(null, Lang.getInstance().translate(result.b), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

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
                        JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message format is not base58 or hex!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

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


        boolean encryptMessage = rightTelegramPanel.jcheckIsEnscript.isSelected();

        byte[] encrypted = (encryptMessage) ? new byte[]{1} : new byte[]{0};
        byte[] isTextByte = new byte[]{1};

        Long key = 1L;


        if (messageBytes != null) {
            if (messageBytes.length > BlockChain.MAX_REC_DATA_BYTES) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Message size exceeded!") + " <= MAX", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

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
                    JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("The recipient has not yet performed any action in the blockchain.\nYou can't send an encrypted message to him."), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

                    //ENABLE
                    this.rightTelegramPanel.jButtonSendTelegram.setEnabled(true);

                    return;
                }

                messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
            }
        }
        String head = "Send Telegram";
        if (head == null) head = "";
        if (head.getBytes(StandardCharsets.UTF_8).length > 256) {

            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Title size exceeded!") + " <= 256", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;

        }

        // CREATE TX MESSAGE
        Transaction transaction = Controller.getInstance().r_Send(
                Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress()), feePow, recipient, key,
                amount, head, messageBytes, isTextByte, encrypted, 0);

        Controller.getInstance().broadcastTelegram(transaction, true);


        // ENABLE
        rightTelegramPanel.jButtonSendTelegram.setEnabled(true);
    }

    public boolean checkError() {

        return true;
    }

    public static Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }

}
