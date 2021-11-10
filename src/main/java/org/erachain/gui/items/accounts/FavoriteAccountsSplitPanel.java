package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.database.wallet.FavoriteAccountsMap;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.WalletItemImprintsTableModel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.utils.TableMenuPopupUtil;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FavoriteAccountsSplitPanel extends SplitPanel {

    public static String NAME = "FavoriteAccountsSplitPanel";
    public static String TITLE = "Favorite Accounts";

    private static final long serialVersionUID = 1L;
    static Logger LOGGER = LoggerFactory.getLogger(FavoriteAccountsSplitPanel.class);
    protected int row;
    private FavoriteAccountsTableModel accountsTableModel;
    private FavoriteAccountsMap accountsMap;

    public FavoriteAccountsSplitPanel() {
        super(NAME, TITLE);

        GridBagLayout gridBagLayout = (GridBagLayout) leftPanel.getLayout();
        gridBagLayout.rowWeights = new double[]{0.0, 0.0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0};

        if (Controller.getInstance().doesWalletDatabaseExists())
            accountsMap = Controller.getInstance().getWallet().dwSet.getFavoriteAccountsMap();

        // CREATE TABLE
        this.accountsTableModel = new FavoriteAccountsTableModel();
        final MTable accountsTable = new MTable(this.accountsTableModel);

        // column #1
        TableColumnModel columnModel = accountsTable.getColumnModel();
        TableColumn column1 = columnModel.getColumn(WalletItemImprintsTableModel.COLUMN_KEY);
        column1.setMaxWidth(100);
        column1.setPreferredWidth(50);

        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.NORTHWEST;
        gbc_panel.insets = new Insets(0, 0, 5, 5);
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 0;
        leftPanel.add(panel, gbc_panel);

        GridBagConstraints GBC = new GridBagConstraints();
        GBC.insets = new Insets(5, 20, 10, 15);
        GBC.anchor = GridBagConstraints.NORTHWEST;
        JButton btnSaveButton = new JButton(Lang.T("Save"));
        panel.add(btnSaveButton, GBC);

        JButton btnLoadButton = new JButton(Lang.T("Load"));
        panel.add(btnLoadButton, GBC);

        JButton btnAddButton = new JButton(Lang.T("Add"));
        panel.add(btnAddButton, GBC);

        button1ToolBarLeftPanel.setVisible(false);
        button2ToolBarLeftPanel.setVisible(false);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);
        Label_search_Info_Panel.setVisible(false);
        search_Info_Panel.setVisible(false);

        // Event LISTENER

        btnAddButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                new AccountNameAdd();
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
                int row1 = accountsTable.getSelectedRow();
                if (row1 < 0)
                    return;

                row = accountsTable.convertRowIndexToModel(row1);

            }
        });

        JMenuItem copyAddress = new JMenuItem(Lang.T("Copy Account"));
        copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Tuple2<String, Tuple3<String, String, String>> account = accountsTableModel.getItem(row);
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
                Tuple2<String, Tuple3<String, String, String>> item = accountsTableModel.getItem(row);
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

        menu.addSeparator();

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.T("Send mail"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tuple2<String, Tuple3<String, String, String>> item = accountsTableModel.getItem(row);
                Account accountTo = FavoriteAccountsMap.detPublicKeyOrAccount(item.a, item.b);
                MainPanel.getInstance().insertNewTab(Lang.T("Send Mail"),
                        new MailSendPanel(null, accountTo, null));
            }
        });
        menu.add(Send_Mail_item_Menu);

        menu.addSeparator();

        JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.T("Send asset"));
        Send_Coins_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tuple2<String, Tuple3<String, String, String>> item = accountsTableModel.getItem(row);
                Account accountTo = FavoriteAccountsMap.detPublicKeyOrAccount(item.a, item.b);
                MainPanel.getInstance().insertNewTab(Lang.T("Send"), new AccountAssetSendPanel(null, null,
                        null, accountTo, null, null, false));

            }
        });
        menu.add(Send_Coins_item_Menu);

        menu.addSeparator();

        JMenuItem setName = new JMenuItem(Lang.T("Edit name"));
        setName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Tuple2<String, Tuple3<String, String, String>> item = accountsTableModel.getItem(row);
                new AccountSetNameDialog(item.a);
                accountsTable.repaint();

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

                int row = accountsTable.getSelectedRow();
                try {
                    row = accountsTable.convertRowIndexToModel(row);
                    Tuple2<String, Tuple3<String, String, String>> item = accountsTableModel.getItem(row);
                    accountsMap.delete(item.a);
                } catch (Exception e1) {
                    logger.error(e1.getMessage(), e1);
                }

            }
        });
        menu.add(menuItemDelete);

        TableMenuPopupUtil.installContextMenu(accountsTable, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        btnSaveButton.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                String result = Controller.getInstance().saveFavorites();
                if (result == null) {
                    JOptionPane.showMessageDialog(
                            null, Lang.T("Favorite recordings are saved"), Lang.T("INFO"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(
                        null, Lang.T(result), Lang.T("ERROR"), JOptionPane.ERROR_MESSAGE);

            }
        });

        btnLoadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                String result = Controller.getInstance().loadFavorites();
                if (result == null) {
                    JOptionPane.showMessageDialog(
                            null, Lang.T("Favorite recordings uploaded"), Lang.T("INFO"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(
                        null, Lang.T(result), Lang.T("ERROR"), JOptionPane.ERROR_MESSAGE);

            }

        });

    }

    @Override
    public void onClose() {
        // delete observer left panel
        accountsTableModel.deleteObservers();

    }

}
