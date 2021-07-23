package org.erachain.utils;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.*;
import org.erachain.gui.items.accounts.AccountAssetSendPanel;
import org.erachain.gui.items.accounts.MyAccountsSplitPanel;
import org.erachain.gui.items.assets.SearchAssetsSplitPanel;
import org.erachain.gui.models.WalletTransactionsTableModel;
import org.erachain.gui.settings.SettingsFrame;
import org.erachain.gui.transaction.TransactionDetailsFactory;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class SysTray implements Observer {


    private static final Logger LOGGER = LoggerFactory.getLogger(SysTray.class);
    private static SysTray systray = null;
    private TrayIcon icon = null;
    private PopupMenu createPopupMenu;

    public boolean stopMessages = BlockChain.TEST_DB > 0;

    private long timePoint;

    public SysTray() {
        Controller.getInstance().addObserver(this);
    }

    public synchronized static SysTray getInstance() {
        if (systray == null) {
            systray = new SysTray();
        }

        return systray;
    }

    public void createTrayIcon() throws HeadlessException,
            MalformedURLException, AWTException, FileNotFoundException {
        if (icon == null) {
            if (!SystemTray.isSupported()) {
                LOGGER.info("SystemTray is not supported");
            } else {

                //String toolTipText = "Erachain.org "	+ Controller.getInstance().getVersion();
                createPopupMenu = createPopupMenu();
                TrayIcon icon = new TrayIcon(createImage(
                        "images/icons/icon32.png", "tray icon"), Controller.getInstance().getApplicationName(false),
                        createPopupMenu);

                icon.setImageAutoSize(true);

                SystemTray.getSystemTray().add(icon);
                this.icon = icon;

                icon.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Gui.getInstance().bringtoFront();
                        } catch (Exception e1) {
                            LOGGER.error(e1.getMessage(), e1);
                        }
                    }
                });

                this.update(new Observable(), new ObserverMessage(ObserverMessage.NETWORK_STATUS, Controller.getInstance().getStatus()));
            }
        }
    }

    public void sendMessage(String caption, String text, TrayIcon.MessageType messagetype) {
        if (icon != null && ! stopMessages) {
            icon.displayMessage(caption, text,
                    messagetype);
        }
    }

    // Obtain the image URL
    private Image createImage(String path, String description)
            throws MalformedURLException, FileNotFoundException {

        File file = new File(path);

        if (!file.exists()) {
            throw new FileNotFoundException("Iconfile not found: " + path);
        }

        URL imageURL = file.toURI().toURL();
        return (new ImageIcon(imageURL, description)).getImage();
    }

    private PopupMenu createPopupMenu() throws HeadlessException {
        PopupMenu menu = new PopupMenu();


        MenuItem decentralizedWeb = new MenuItem("erachain.org Web/Social Network");
        decentralizedWeb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    URLViewer.openWebpage(new URL("http://127.0.0.1:" + Settings.getInstance().getWebPort()));
                } catch (MalformedURLException e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }
            }
        });
        menu.add(decentralizedWeb);
        MenuItem walletStatus = new MenuItem("Wallet Status (web)");
        walletStatus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    URLViewer.openWebpage(new URL("http://127.0.0.1:" + Settings.getInstance().getWebPort() + "/index/status.html"));
                } catch (MalformedURLException e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }
            }
        });
        menu.add(walletStatus);


        MenuItem lock = new MenuItem(Lang.T("Lock/Unlock Wallet"));
        lock.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Controller.getInstance().isWalletUnlocked()) {
                    Controller.getInstance().lockWallet();
                } else {
                    String password = PasswordPane.showUnlockWalletDialog(MainFrame.getInstance());
                    if (!password.equals("") && !Controller.getInstance().unlockWallet(password)) {
                        JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        menu.add(lock);


        MenuItem settings = new MenuItem(Lang.T("Settings"));
        settings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SettingsFrame();
            }
        });
        menu.add(settings);


        MenuItem console = new MenuItem(Lang.T("Console"));
        console.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame(Lang.T("Console"));

                frame.setSize(800, 600);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setVisible(true);
                ConsolePanel ap = new ConsolePanel();
                frame.getContentPane().add(ap);

            }
        });
        menu.add(console);

        MenuItem transactions = new MenuItem(Lang.T("Transactions"));
        transactions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame(Lang.T("Transactions"));

                frame.setSize(800, 600);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setVisible(true);
                final WalletTransactionsTableModel transactionsModel = new WalletTransactionsTableModel();
                final JTable transactionsTable = new JTable(transactionsModel);

                //TRANSACTIONS SORTER
                //Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
                //indexes.put(WalletTransactionsTableModel.COLUMN_CONFIRMATIONS, WTransactionMap.TIMESTAMP_INDEX);
                //indexes.put(WalletTransactionsTableModel.COLUMN_TIMESTAMP, WTransactionMap.TIMESTAMP_INDEX);
                //indexes.put(WalletTransactionsTableModel.COLUMN_CREATOR, TransactionMap.ADDRESS_INDEX);
                //indexes.put(WalletTransactionsTableModel.COLUMN_AMOUNT, TransactionMap.AMOUNT_INDEX);
                //  CoreRowSorter sorter = new CoreRowSorter(transactionsModel, indexes);
                //  transactionsTable.setRowSorter(sorter);

                //TRANSACTION DETAILS
                transactionsTable.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            //GET ROW
                            int row = transactionsTable.getSelectedRow();
                            row = transactionsTable.convertRowIndexToModel(row);

                            //GET TRANSACTION
                            Transaction transaction = transactionsModel.getItem(row).b;

                            //SHOW DETAIL SCREEN OF TRANSACTION
                            TransactionDetailsFactory.createTransactionDetail(transaction);
                        }
                    }
                });

                frame.getContentPane().add(new JScrollPane(transactionsTable));
            }
        });
        menu.add(transactions);

        MenuItem messages = new MenuItem(Lang.T("Send"));
        messages.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JFrame frame = new JFrame(Lang.T("Send"));

                //    frame.setSize(800, 600);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                AccountAssetSendPanel ap = new AccountAssetSendPanel(null, null, null, null, null, false);
                frame.getContentPane().add(ap);
                frame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
        menu.add(messages);

        MenuItem assets = new MenuItem(Lang.T("Assets"));
        assets.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame(Lang.T("Assets"));
                frame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
                frame.setSize(800, 600);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                SearchAssetsSplitPanel ap = new SearchAssetsSplitPanel(true);
                frame.getContentPane().add(ap);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            }
        });
        menu.add(assets);

        MenuItem my_Accounts = new MenuItem(Lang.T("My Accounts"));
        my_Accounts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame(Lang.T("My Accounts"));

                frame.setSize(800, 600);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                frame.getContentPane().add(
                        new MyAccountsSplitPanel());
                frame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            }
        });
        menu.add(my_Accounts);


        MenuItem exit = new MenuItem(Lang.T("Quit"));
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ClosingDialog();
            }
        });
        menu.add(exit);

        return menu;
    }

    public void setToolTipText(String text) {
        this.icon.setToolTip(text);
    }

    @Override
    public void update(Observable arg0, Object arg1) {

        if (Controller.getInstance().isOnStopping() || this.icon == null) {
            return;
        }

        ObserverMessage message = (ObserverMessage) arg1;

        if (message.getType() == ObserverMessage.WALLET_SYNC_STATUS) {

            if (System.currentTimeMillis() - timePoint < 2000)
                return;

            timePoint = System.currentTimeMillis();

            int currentHeight = (int) message.getValue();
            int height = DCSet.getInstance().getBlockMap().size();
            if (currentHeight == 0 || currentHeight == height) {
                this.update(null, new ObserverMessage(
                        ObserverMessage.NETWORK_STATUS, Controller.getInstance().getStatus()));
                return;
            }
            String networkStatus = Lang.T("Wallet Synchronizing");
            String syncProcent = 100 * currentHeight / Controller.getInstance().getBlockChain().getHWeightFull(DCSet.getInstance()).a + "%";

            String toolTipText = networkStatus + " " + syncProcent;
            toolTipText += "\n" + Lang.T("Height") + ": " + currentHeight + "/" + Controller.getInstance().getBlockChain().getHWeightFull(DCSet.getInstance()).a + "/" + Controller.getInstance().getMaxPeerHWeight(0, false, false).a;
            setToolTipText(toolTipText);

        } else if (message.getType() == ObserverMessage.BLOCKCHAIN_SYNC_STATUS) {

            if (System.currentTimeMillis() - timePoint < 2000)
                return;

            timePoint = System.currentTimeMillis();

            int currentHeight = (int) message.getValue();

            String syncProcent = "";
            if (Controller.getInstance().getStatus() == Controller.STATUS_SYNCHRONIZING) {
                syncProcent = 100 * currentHeight / Controller.getInstance().getMaxPeerHWeight(0, false, false).a + "%";
            }

            String toolTipText = syncProcent;
            toolTipText += "\n" + Lang.T("Height") + ": " + currentHeight + "/" + Controller.getInstance().getMaxPeerHWeight(0, false, false);
            setToolTipText(toolTipText);

        } else if (message.getType() == ObserverMessage.CHAIN_ADD_BLOCK_TYPE || message.getType() == ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE
                || message.getType() == ObserverMessage.NETWORK_STATUS) {

            // непонятно в каких случаях когда это прилетает делать
            if (true || Controller.getInstance().getStatus() == Controller.STATUS_OK || Controller.getInstance().getStatus() == Controller.STATUS_NO_CONNECTIONS
            ) {

                if (System.currentTimeMillis() - timePoint < 2000)
                    return;

                timePoint = System.currentTimeMillis();

                String networkStatus = "";
                String syncProcent = "";
                String toolTipText = Controller.getInstance().getApplicationName(false) + "\n";

                if (Controller.getInstance().getStatus() == Controller.STATUS_NO_CONNECTIONS) {
                    networkStatus = Lang.T("No connections");
                    syncProcent = "";
                } else if (Controller.getInstance().getStatus() == Controller.STATUS_SYNCHRONIZING) {
                    networkStatus = Lang.T("Synchronizing");
                } else if (Controller.getInstance().getStatus() == Controller.STATUS_OK) {
                    networkStatus = Lang.T("OK");
                    syncProcent = "";
                }
                toolTipText = networkStatus + " " + syncProcent;
                toolTipText += "\n" + Lang.T("Height") + ": " + Controller.getInstance().getBlockChain().getHWeightFull(DCSet.getInstance()).a;
                setToolTipText(toolTipText);
            }
        }
    }

}