package org.erachain.gui.items.accounts;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.database.wallet.FavoriteAccountsMap;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.items.mails.MailSendPanel;
import org.erachain.gui.library.FileChooser;
import org.erachain.gui.library.MTable;
import org.erachain.gui.models.WalletItemImprintsTableModel;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.erachain.utils.SaveStrToFile;
import org.erachain.utils.TableMenuPopupUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FavoriteAccountsSplitPanel extends SplitPanel {

    public static String NAME = "FavoriteAccountsSplitPanel";
    public static String TITLE = "Favorite Accounts";

    private static final long serialVersionUID = 1L;
    static Logger LOGGER = LoggerFactory.getLogger(FavoriteAccountsSplitPanel.class);
    protected FileChooser chooser;
    protected int row;
    private FavoriteAccountsTableModel accountsTableModel;
    private JButton button3_ToolBar_LeftPanel;
    private FavoriteAccountsMap accountsMap;

    public FavoriteAccountsSplitPanel() {
        super(NAME, TITLE);

        GridBagLayout gridBagLayout = (GridBagLayout) leftPanel.getLayout();
        gridBagLayout.rowWeights = new double[]{0.0, 0.0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0};

        if (Controller.getInstance().doesWalletDatabaseExists())
            accountsMap = Controller.getInstance().wallet.database.getFavoriteAccountsMap();

        searthLabelSearchToolBarLeftPanel.setVisible(true);
        // this.searchTextFieldSearchToolBarLeftPanelDocument.setVisible(true);
        // this.searchToolBar_LeftPanel.setVisible(true);
        // not show buttons
        // button1ToolBarLeftPanel.setVisible(false);
        // button2ToolBarLeftPanel.setVisible(false);
        button1ToolBarLeftPanel.setText(Lang.T("Load"));
        button2ToolBarLeftPanel.setText(Lang.T("Save"));
        button3_ToolBar_LeftPanel = new JButton();
        button3_ToolBar_LeftPanel.setText(Lang.T("Add"));
        this.toolBarLeftPanel.add(button3_ToolBar_LeftPanel);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);

        // CREATE TABLE
        this.accountsTableModel = new FavoriteAccountsTableModel();
        final MTable imprintsTable = new MTable(this.accountsTableModel);

        // CHECKBOX FOR FAVORITE
        // TableColumn favoriteColumn =
        // imprintsTable.getColumnModel().getColumn(TableModelUnionsItemsTableModel.COLUMN_FAVORITE);
        // favoriteColumn.setCellRenderer(new RendererBoolean());
        // //unionsTable.getDefaultRenderer(Boolean.class));
        // favoriteColumn.setMinWidth(50);
        // favoriteColumn.setMaxWidth(50);
        // favoriteColumn.setPreferredWidth(50);//.setWidth(30);
        // column #1
        TableColumnModel columnModel = imprintsTable.getColumnModel();
        TableColumn column1 = columnModel.getColumn(WalletItemImprintsTableModel.COLUMN_KEY);// .COLUMN_CONFIRMED);
        column1.setMaxWidth(100);
        column1.setPreferredWidth(50);

        // set showvideo
        jTableJScrollPanelLeftPanel.setModel(this.accountsTableModel);
        jTableJScrollPanelLeftPanel = imprintsTable;
        jScrollPanelLeftPanel.setViewportView(jTableJScrollPanelLeftPanel);

        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.NORTHWEST;
        gbc_panel.insets = new Insets(0, 0, 5, 5);
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 0;
        leftPanel.add(panel, gbc_panel);

        JButton btnLoadButton = new JButton(Lang.T("Load"));
        panel.add(btnLoadButton);

        JButton btnSaveButton = new JButton(Lang.T("Save"));
        panel.add(btnSaveButton);

        JButton btnAddButton = new JButton(Lang.T("Add"));
        panel.add(btnAddButton);
        button1ToolBarLeftPanel.setVisible(false);
        button2ToolBarLeftPanel.setVisible(false);
        button3_ToolBar_LeftPanel.setVisible(false);

        // Event LISTENER
        jTableJScrollPanelLeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                /*
                 * ImprintCls imprint = null; if (jTableJScrollPanelLeftPanel.getSelectedRow()
                 * >= 0 ) imprint = tableModelImprints.getImprint(jTableJScrollPanelLeftPanel.
                 * convertRowIndexToModel(jTableJScrollPanelLeftPanel.getSelectedRow()));
                 *
                 *
                 *
                 * // info.show_001(person);
                 *
                 * // search_Person_SplitPanel.jSplitPanel.setDividerLocation(
                 * search_Person_SplitPanel.jSplitPanel.getDividerLocation()); //
                 * search_Person_SplitPanel.searchTextFieldSearchToolBarLeftPanelDocument.setEnabled(
                 * true); ImprintsInfoPanel info_panel = new ImprintsInfoPanel(imprint);
                 * info_panel.setPreferredSize(new
                 * Dimension(jScrollPaneJPanelRightPanel.getSize().width-50,
                 * jScrollPaneJPanelRightPanel.getSize().height-50));
                 * jScrollPaneJPanelRightPanel.setViewportView(info_panel);
                 */
            }

        });

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
                int row1 = imprintsTable.getSelectedRow();
                if (row1 < 0)
                    return;

                row = imprintsTable.convertRowIndexToModel(row1);

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
                MainPanel.getInstance().insertNewTab(Lang.T("Send"), new AccountAssetSendPanel(null,
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
                imprintsTable.repaint();

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

                int row = imprintsTable.getSelectedRow();
                try {
                    row = imprintsTable.convertRowIndexToModel(row);
                    Tuple2<String, Tuple3<String, String, String>> item = accountsTableModel.getItem(row);
                    accountsMap.delete(item.a);
                } catch (Exception e1) {
                    logger.error(e1.getMessage(), e1);
                }

            }
        });
        menu.add(menuItemDelete);

      //  imprintsTable.setComponentPopupMenu(menu);
        TableMenuPopupUtil.installContextMenu(imprintsTable, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

        btnSaveButton.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                chooser = new FileChooser();

                chooser.setDialogTitle(Lang.T("Save File"));
                // chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
                chooser.setMultiSelectionEnabled(false);
                // chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                // chooser.setAcceptAllFileFilterUsed(false);
                // add filters
                FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("Era Name Accounts files (*.enaf)",
                        "enaf");
                chooser.setAcceptAllFileFilterUsed(false);// only filter
                chooser.addChoosableFileFilter(xmlFilter);
                chooser.setFileFilter(xmlFilter);

                if (chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

                    String pp = chooser.getSelectedFile().getPath();
                    if (!pp.contains(".enaf"))
                        pp += ".enaf";
                    File ff = new File(pp);
                    // if file
                    if (ff.exists() && ff.isFile()) {
                        int aaa = JOptionPane.showConfirmDialog(chooser,
                                Lang.T("File") + Lang.T("Exists") + "! "
                                        + Lang.T("Overwrite") + "?",
                                Lang.T("Message"), JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
                        System.out.print("\n gggg " + aaa);
                        if (aaa != 0) {
                            return;
                        }
                        ff.delete();

                    }

                    try (FileOutputStream fos = new FileOutputStream(pp)) {

                        // buffer
                        JSONObject output = new JSONObject();

                        // TODO переделать с  db.getList() на перебор по ключу
                        for (String key : accountsMap.keySet()) {
                            JSONObject account = new JSONObject();
                            Tuple3<String, String, String> item = accountsMap.get(key);
                            if (item.a != null) account.put("punKey", item.a);
                            account.put("name", item.b);
                            account.put("json", item.c);
                            output.put(key, account);

                        }
                        // byte[] buffer =(byte[]) ;
                        // copy buffer in file
                        /// fos.write(buffer, 0, buffer.length);
                        try {
                            SaveStrToFile.saveJsonFine(pp, output);
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                            JOptionPane
                                    .showMessageDialog(new JFrame(),
                                            "Error writing to the file: " + Settings.getInstance().getSettingsPath()
                                                    + "\nProbably there is no access.",
                                            "Error!", JOptionPane.ERROR_MESSAGE);
                        }

                    } catch (IOException ex) {

                        System.out.println(ex.getMessage());
                    }
                }
            }
        });

        btnLoadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                chooser = new FileChooser();

                chooser.setDialogTitle(Lang.T("Open File"));
                // chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
                chooser.setMultiSelectionEnabled(false);
                // chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                // chooser.setAcceptAllFileFilterUsed(false);
                // add filters
                FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("Era Name Accounts files (*.enaf)",
                        "enaf");
                chooser.setAcceptAllFileFilterUsed(false);// only filter
                chooser.addChoosableFileFilter(xmlFilter);
                chooser.setFileFilter(xmlFilter);

                if (chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {

                    File file = new File(chooser.getSelectedFile().getPath());

                    JSONObject inJSON;
                    try {
                        // OPEN FILE
                        // READ SETTINS JSON FILE
                        List<String> lines = Files.readLines(file, Charsets.UTF_8);

                        String jsonString = "";
                        for (String line : lines) {

                            // correcting single backslash bug
                            if (line.contains("userpath")) {
                                line = line.replace("\\", File.separator);
                            }

                            jsonString += line;
                        }

                        // CREATE JSON OBJECT
                        inJSON = (JSONObject) JSONValue.parse(jsonString);
                        inJSON = inJSON == null ? new JSONObject() : inJSON;

                        Set<String> keys = inJSON.keySet();
                        Iterator<String> itKeys = keys.iterator();
                        while (itKeys.hasNext()) {
                            String a = itKeys.next();
                            JSONObject item = (JSONObject) inJSON.get(a);
                            accountsMap.put(a, new Tuple3(item.get("pubKey"), item.get("name"), item.get("json")));
                        }

                    } catch (Exception e) {
                        LOGGER.info("Error while reading/creating settings.json " + file.getAbsolutePath()
                                + " using default!");
                        LOGGER.error(e.getMessage(), e);
                        inJSON = new JSONObject();
                    }
                }
            }

        });

    }

    @Override
    public void onClose() {
        // delete observer left panel
        accountsTableModel.deleteObservers();
        // get component from right panel
        Component c1 = jScrollPaneJPanelRightPanel.getViewport().getView();
        // if PersonInfo 002 delay on close
        // if (c1 instanceof ImprintsInfoPanel) (
        // (ImprintsInfoPanel)c1).delay_on_Close();

    }

}
