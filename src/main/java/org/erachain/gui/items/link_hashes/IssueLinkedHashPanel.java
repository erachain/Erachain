package org.erachain.gui.items.link_hashes;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.blockexplorer.WebTransactionsHTML;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.ResultDialog;
import org.erachain.gui.SplitPanel;
import org.erachain.gui.library.FileChooser;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

//import java.awt.GridBagConstraints;

public class IssueLinkedHashPanel extends SplitPanel {

    public static String NAME = "IssueLinkedHashPanel";
    public static String TITLE = "Issue Linked Hash";

    TableModelIssueHashes table_Model;
    IssueHashImprint issue_Hash_Imprint;
    private JTable Table_Hash;
    private JButton jButton3_jToolBar_RightPanel;

    public IssueLinkedHashPanel() {
        super(NAME, TITLE);

        jButton1_jToolBar_RightPanel.setVisible(false);
        jButton2_jToolBar_RightPanel.setVisible(false);
        GridBagLayout gridBagLayout = (GridBagLayout) rightPanel1.getLayout();
        gridBagLayout.rowWeights = new double[]{0.0, 0.0};
        gridBagLayout.columnWeights = new double[]{0.0};
        // GridBagLayout gridBagLayout = (GridBagLayout) rightPanel1.getLayout();
        // gridBagLayout.rowWeights = new double[]{1.0, 0.0};
        // gridBagLayout.columnWeights = new double[]{1.0};
        // left panel

        this.toolBarLeftPanel.setVisible(false);
        this.searchToolBar_LeftPanel.setVisible(false);
        this.jTableJScrollPanelLeftPanel.setVisible(false);
        issue_Hash_Imprint = new IssueHashImprint();
        this.jScrollPanelLeftPanel.setViewportView(issue_Hash_Imprint);

        issue_Hash_Imprint.jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onIssueClick();
            }
        });

        JPanel panel = new JPanel();
        jToolBarRightPanel.add(panel);

        JButton btnNewButton = new JButton();
        btnNewButton.setText(Lang.T("Delete Hash"));
        panel.add(btnNewButton);
        btnNewButton.setHorizontalAlignment(SwingConstants.LEFT);
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                int a = table_Model.getRowCount();
                if (table_Model.getRowCount() > 1) {
                    int selRow = Table_Hash.getSelectedRow();
                    Object f = table_Model.getValueAt(selRow, 0);
                    if (selRow != -1 && table_Model.getRowCount() >= selRow) {
                        ((DefaultTableModel) table_Model).removeRow(selRow);
                        table_Model.fireTableDataChanged();
                    }
                }

            }
        });
        btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btnNewButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnNewButton.setMinimumSize(new Dimension(33, 9));
        btnNewButton.setMaximumSize(new Dimension(33, 9));

        jButton3_jToolBar_RightPanel = new JButton();
        panel.add(jButton3_jToolBar_RightPanel);
        jButton3_jToolBar_RightPanel.setText(Lang.T("Create Hash"));
        // jButton3_jToolBar_RightPanel.setFocusable(false);
        jButton3_jToolBar_RightPanel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3_jToolBar_RightPanel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3_jToolBar_RightPanel.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                Hashs_from_Files(false);

            }
        });
        jButton3_jToolBar_RightPanel.setFont(new Font("Tahoma", 0, 14));

        JButton btnNewButton_1 = new JButton();
        btnNewButton_1.setText(Lang.T("Import Hashs"));
        panel.add(btnNewButton_1);
        btnNewButton_1.setHorizontalAlignment(SwingConstants.LEFT);
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Hashs_from_Files(true);
            }
        });
        btnNewButton_1.setFont(new Font("Tahoma", Font.PLAIN, 14));

        JPanel panel_1 = new JPanel();
        panel_1.setMaximumSize(new Dimension(32767, 32));
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
        gbc_panel_1.anchor = GridBagConstraints.NORTH;
        gbc_panel_1.insets = new Insets(0, 0, 5, 0);
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 0;
        rightPanel1.add(panel_1, gbc_panel_1);

        table_Model = new TableModelIssueHashes(0);
        Table_Hash = new JTable(table_Model);
        this.jScrollPaneJPanelRightPanel.setViewportView(Table_Hash);

    }

    public void onIssueClick() {
        // DISABLE
        issue_Hash_Imprint.jButton.setEnabled(false);

        // CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            // NETWORK NOT OK
            JOptionPane.showMessageDialog(null,
                    Lang.T(
                            "You are unable to send a transaction while synchronizing or while having no connections!"),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);

            // ENABLE
            issue_Hash_Imprint.jButton.setEnabled(true);

            return;
        }

        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"),
                        Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                // ENABLE
                issue_Hash_Imprint.jButton.setEnabled(true);

                return;
            }
        }

        // READ CREATOR
        Account sender = (Account) issue_Hash_Imprint.jComboBox_Account.getSelectedItem();

        long parse = 0;
        int feePow = 0;
        String url = "";
        String description = "";
        try {

            // READ FEE POW
            feePow = Integer.parseInt((String) issue_Hash_Imprint.txtFeePow.getSelectedItem());
            // READ AMOUNT
            // float amount = Float.parseFloat(this.txtAmount.getText());

            // NAME TOTAL
            url = issue_Hash_Imprint.jTextField_URL.getText().trim();

            description = issue_Hash_Imprint.jTextArea_Description.getText();

        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid fee!"),
                        Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(new JFrame(), Lang.T("Invalid quantity!"),
                        Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }

        List<String> hashes = this.table_Model.getValues(0);

        // CREATE IMPRINT
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.T(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        Transaction transaction = Controller.getInstance().r_Hashes(creator, feePow, url, description,
                String.join(" ", hashes));

        WebTransactionsHTML webHTML = new WebTransactionsHTML(DCSet.getInstance(), Lang.getInstance().getLangForNode());
        JSONObject outJson = webHTML.get_HTML_Body(transaction, "");
        String htmlDescr = (String) outJson.get("head");
        htmlDescr += (String) outJson.get("body");
        htmlDescr = transaction.toJson().toJSONString();
        htmlDescr = htmlDescr.replace(",", ",<br>");

        IssueConfirmDialog confirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, transaction,
                htmlDescr, (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), "",
                Lang.T("Linked Imprint issue has been sent!"));
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);

        if (confirmDialog.isConfirm > 0) {
            ResultDialog.make(this, transaction, confirmDialog.isConfirm == IssueConfirmDialog.TRY_FREE);
        }

        // ENABLE
        issue_Hash_Imprint.jButton.setEnabled(true);
    }

    protected void Hashs_from_Files(boolean importing) {
        // TODO Auto-generated method stub
        // true - если импорт из файла
        // false - если создаем хэш для файлов

        // открыть диалог для файла
        // JFileChooser chooser = new JFileChooser();
        // руссификация диалога выбора файла
        // new All_Options().setUpdateUI(chooser);
        FileChooser chooser = new FileChooser();
        chooser.setDialogTitle(Lang.T("Select File"));

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        if (importing)
            chooser.setMultiSelectionEnabled(false);

        // FileNameExtensionFilter filter = new FileNameExtensionFilter(
        // "Image", "png", "jpg");
        // chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // если есть пустые строки удаляем их
            int i;
            for (i = 0; i <= table_Model.getRowCount() - 1; i++) {
                if (table_Model.getValueAt(i, 0).toString().equals("")) {

                    table_Model.removeRow(i);

                }
            }

            if (importing) {
                // IMPORT FROM FILE
                File patch = chooser.getSelectedFile();
                String file_name = patch.getPath();
                String hashesStr = "";
                try {
                    hashesStr = new String(Files.readAllBytes(Paths.get(file_name)));
                } catch (IOException e) {
                    e.printStackTrace();
                    table_Model.addRow(
                            new Object[]{"", Lang.T("error reading") + " - " + file_name});
                }

                if (hashesStr.length() > 0) {
                    String[] hashes = hashesStr.split("\\s*(\\s|,|!|;|:|\n|\\.)\\s*");
                    for (String hashB58 : hashes) {
                        if (hashB58 != null && !hashB58.equals(new String("")))
                            table_Model.addRow(new Object[]{hashB58,
                                    Lang.T("imported from") + " " + file_name});
                    }

                }

            } else {

                // make HASHES from files
                File[] patchs = chooser.getSelectedFiles();

                for (File patch : patchs) {

                    String file_name = patch.getPath();
                    File file = new File(patch.getPath());

                    // преобразуем в байты
                    long file_len = file.length();
                    if (file_len > Integer.MAX_VALUE) {
                        table_Model.addRow(new Object[]{"",
                                Lang.T("length very long") + " - " + file_name});
                        continue;
                    }
                    byte[] fileInArray = new byte[(int) file.length()];
                    FileInputStream f = null;
                    try {
                        f = new FileInputStream(patch.getPath());
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        table_Model.addRow(new Object[]{"",
                                Lang.T("error streaming") + " - " + file_name});
                        continue;
                    }
                    try {
                        f.read(fileInArray);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        table_Model.addRow(
                                new Object[]{"", Lang.T("error reading") + " - " + file_name});
                        continue;
                    }
                    try {
                        f.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        continue;
                    }

                    /// HASHING
                    String hashes = Base58.encode(Crypto.getInstance().digest(fileInArray));
                    table_Model.addRow(new Object[]{hashes, Lang.T("from file ") + file_name});

                }

            }
            table_Model.addRow(new Object[]{"", ""});
            table_Model.fireTableDataChanged();
            Table_Hash.setRowSelectionInterval(table_Model.getRowCount() - 1, table_Model.getRowCount() - 1);
        }
    }
}