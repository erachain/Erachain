package org.erachain.gui.items.accounts;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.database.wallet.AccountsPropertisMap;
import org.erachain.datachain.SortableList;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.Split_Panel;
import org.erachain.gui.items.mails.Mail_Send_Dialog;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.My_JFileChooser;
import org.erachain.gui.models.WalletItemImprintsTableModel;
import org.erachain.lang.Lang;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.erachain.settings.Settings;
import org.erachain.utils.Pair;
import org.erachain.utils.SaveStrToFile;
import org.erachain.utils.TableMenuPopupUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
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

public class Accounts_Name_Search_SplitPanel extends Split_Panel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static Logger LOGGER = Logger.getLogger(Accounts_Name_Search_SplitPanel.class.getName());
    protected My_JFileChooser chooser;
    protected int row;
    private Accounts_Name_TableModel tableModelImprints;
    private JButton button3_ToolBar_LeftPanel;
    private AccountsPropertisMap db;

    public Accounts_Name_Search_SplitPanel() {
        super("Accounts_Name_Search_SplitPanel");
        GridBagLayout gridBagLayout = (GridBagLayout) leftPanel.getLayout();
        gridBagLayout.rowWeights = new double[]{0.0, 0.0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0};
        db = Controller.getInstance().wallet.database.getAccountsPropertisMap();
        setName(Lang.getInstance().translate("Favorite Accounts"));
        searthLabel_SearchToolBar_LeftPanel.setText(Lang.getInstance().translate("Search") + ":  ");
        searthLabel_SearchToolBar_LeftPanel.setVisible(true);
        // this.searchTextField_SearchToolBar_LeftPanel.setVisible(true);
        // this.searchToolBar_LeftPanel.setVisible(true);
        // not show buttons
        // button1_ToolBar_LeftPanel.setVisible(false);
        // button2_ToolBar_LeftPanel.setVisible(false);
        button1_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Load"));
        button2_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Save"));
        button3_ToolBar_LeftPanel = new JButton();
        button3_ToolBar_LeftPanel.setText(Lang.getInstance().translate("Add"));
        this.toolBar_LeftPanel.add(button3_ToolBar_LeftPanel);
        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);

        // CREATE TABLE
        this.tableModelImprints = new Accounts_Name_TableModel();
        final MTable imprintsTable = new MTable(this.tableModelImprints);

        // CHECKBOX FOR FAVORITE
        // TableColumn favoriteColumn =
        // imprintsTable.getColumnModel().getColumn(TableModelUnions.COLUMN_FAVORITE);
        // favoriteColumn.setCellRenderer(new Renderer_Boolean());
        // //unionsTable.getDefaultRenderer(Boolean.class));
        // favoriteColumn.setMinWidth(50);
        // favoriteColumn.setMaxWidth(50);
        // favoriteColumn.setPreferredWidth(50);//.setWidth(30);
        // column #1
        TableColumn column1 = imprintsTable.getColumnModel().getColumn(WalletItemImprintsTableModel.COLUMN_KEY);// .COLUMN_CONFIRMED);
        column1.setMinWidth(1);
        column1.setMaxWidth(1000);
        column1.setPreferredWidth(50);

        // set showvideo
        jTable_jScrollPanel_LeftPanel.setModel(this.tableModelImprints);
        jTable_jScrollPanel_LeftPanel = imprintsTable;
        jScrollPanel_LeftPanel.setViewportView(jTable_jScrollPanel_LeftPanel);

        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.NORTHWEST;
        gbc_panel.insets = new Insets(0, 0, 5, 5);
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 0;
        leftPanel.add(panel, gbc_panel);

        JButton btnLoadButton = new JButton(Lang.getInstance().translate("Load"));
        panel.add(btnLoadButton);

        JButton btnSaveButton = new JButton(Lang.getInstance().translate("Save"));
        panel.add(btnSaveButton);

        JButton btnAddButton = new JButton(Lang.getInstance().translate("Add"));
        panel.add(btnAddButton);
        button1_ToolBar_LeftPanel.setVisible(false);
        button2_ToolBar_LeftPanel.setVisible(false);
        button3_ToolBar_LeftPanel.setVisible(false);

        // Event LISTENER
        jTable_jScrollPanel_LeftPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                /*
                 * ImprintCls imprint = null; if (jTable_jScrollPanel_LeftPanel.getSelectedRow()
                 * >= 0 ) imprint = tableModelImprints.getImprint(jTable_jScrollPanel_LeftPanel.
                 * convertRowIndexToModel(jTable_jScrollPanel_LeftPanel.getSelectedRow()));
                 *
                 *
                 *
                 * // info.show_001(person);
                 *
                 * // search_Person_SplitPanel.jSplitPanel.setDividerLocation(
                 * search_Person_SplitPanel.jSplitPanel.getDividerLocation()); //
                 * search_Person_SplitPanel.searchTextField_SearchToolBar_LeftPanel.setEnabled(
                 * true); Imprints_Info_Panel info_panel = new Imprints_Info_Panel(imprint);
                 * info_panel.setPreferredSize(new
                 * Dimension(jScrollPane_jPanel_RightPanel.getSize().width-50,
                 * jScrollPane_jPanel_RightPanel.getSize().height-50));
                 * jScrollPane_jPanel_RightPanel.setViewportView(info_panel);
                 */
            }

        });

        btnAddButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                new Account_Name_Add();
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

        JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Copy Account"));
        copyAddress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Pair<String, Tuple2<String, String>> account = tableModelImprints.getAccount(row);
                StringSelection value = new StringSelection(account.getA());
                clipboard.setContents(value, null);
            }
        });
        menu.add(copyAddress);

        JMenuItem menu_copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
        menu_copyPublicKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

                Pair<String, Tuple2<String, String>> account = tableModelImprints.getAccount(row);
                byte[] publick_Key = Controller.getInstance().getPublicKeyByAddress(account.getA());
                PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
                StringSelection value = new StringSelection(public_Account.getBase58());
                clipboard.setContents(value, null);
            }
        });
        menu.add(menu_copyPublicKey);

        JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send asset"));
        Send_Coins_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Pair<String, Tuple2<String, String>> account1 = tableModelImprints.getAccount(row);
                Account account = new Account(account1.getA());
                new Account_Send_Dialog(null, null, account, null);

            }
        });
        menu.add(Send_Coins_item_Menu);

        JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send mail"));
        Send_Mail_item_Menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Pair<String, Tuple2<String, String>> account1 = tableModelImprints.getAccount(row);
                Account account = new Account(account1.getA());
                new Mail_Send_Dialog(null, null, account, null);

            }
        });
        menu.add(Send_Mail_item_Menu);

        JMenuItem setName = new JMenuItem(Lang.getInstance().translate("Edit name"));
        setName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Pair<String, Tuple2<String, String>> account1 = tableModelImprints.getAccount(row);

                new Account_Set_Name_Dialog(account1.getA());
                imprintsTable.repaint();

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

                int row = imprintsTable.getSelectedRow();
                try {
                    row = imprintsTable.convertRowIndexToModel(row);
                    Pair<String, Tuple2<String, String>> ac = tableModelImprints.getAccount(row);
                    db.delete(ac.getA());
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
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
                chooser = new My_JFileChooser();

                chooser.setDialogTitle(Lang.getInstance().translate("Save File"));
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
                                Lang.getInstance().translate("File") + Lang.getInstance().translate("Exists") + "! "
                                        + Lang.getInstance().translate("Overwrite") + "?",
                                Lang.getInstance().translate("Message"), JOptionPane.OK_CANCEL_OPTION,
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

                        SortableList<String, Tuple2<String, String>> lists = db.getList();

                        for (Pair<String, Tuple2<String, String>> list : lists) {
                            JSONObject account = new JSONObject();
                            account.put("name", list.getB().a);
                            account.put("json", list.getB().b);
                            output.put(list.getA(), account);

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

                chooser = new My_JFileChooser();

                chooser.setDialogTitle(Lang.getInstance().translate("Open File"));
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
                            JSONObject ss = (JSONObject) inJSON.get(a);
                            Object a1 = ss.get("name");
                            Object a2 = ss.get("json");
                            db.set(a, new Tuple2(ss.get("name"), ss.get("json")));
                        }

                        // while (it.hasNext()){
                        // Object ss = it..next();
                        // ss=ss;

                        // }

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
    public void delay_on_close() {
        // delete observer left panel
        tableModelImprints.deleteObserver();
        // get component from right panel
        Component c1 = jScrollPane_jPanel_RightPanel.getViewport().getView();
        // if Person_Info 002 delay on close
        // if (c1 instanceof Imprints_Info_Panel) (
        // (Imprints_Info_Panel)c1).delay_on_Close();

    }

}
