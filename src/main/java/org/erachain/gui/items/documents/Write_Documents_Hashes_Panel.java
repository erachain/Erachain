package org.erachain.gui.items.documents;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.R_Hashes;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.Split_Panel;
import org.erachain.gui.items.link_hashes.Issue_Hash_Imprint;
import org.erachain.gui.items.link_hashes.Table_Model_Issue_Hashes;
import org.erachain.gui.library.MTable;
import org.erachain.gui.library.My_JFileChooser;
import org.erachain.lang.Lang;
import org.erachain.utils.FileHash;
import org.erachain.utils.Pair;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Write_Documents_Hashes_Panel extends Split_Panel {
    public Table_Model_Issue_Hashes table_Model;
    public MTable Table_Hash;
    Issue_Hash_Imprint issue_Hash_Imprint;
    private JButton jButton3_jToolBar_RightPanel;

    public Write_Documents_Hashes_Panel() {
        super("Write_Documents_Hashes_Panel");
        // left panel

        this.toolBar_LeftPanel.setVisible(false);
        this.searchToolBar_LeftPanel.setVisible(false);
        this.jTable_jScrollPanel_LeftPanel.setVisible(false);
        issue_Hash_Imprint = new Issue_Hash_Imprint();
        this.jScrollPanel_LeftPanel.setViewportView(issue_Hash_Imprint);

        issue_Hash_Imprint.jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onIssueClick();
            }
        });

        // Right panel
        // this.jToolBar_RightPanel.setSize(WIDTH, 200);
        jButton2_jToolBar_RightPanel.setText(Lang.getInstance().translate("Import Hashs"));
        // jButton2_jToolBar_RightPanel.setSize(70, 30);
        // jButton2_jToolBar_RightPanel.setPreferredSize(new
        // Dimension(100,200));
        jButton2_jToolBar_RightPanel.addActionListener(new ActionListener() {
            // create Hashs
            @Override
            public void actionPerformed(ActionEvent e) {
                Hashs_from_Files(true);

            }
        });

        jButton1_jToolBar_RightPanel.setText(Lang.getInstance().translate("Delete Hash"));
        jButton1_jToolBar_RightPanel.addActionListener(new ActionListener() {
            // delete row
            @Override
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

        jButton3_jToolBar_RightPanel = new JButton();
        jButton3_jToolBar_RightPanel.setText(Lang.getInstance().translate("Create Hash"));
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

        jToolBar_RightPanel.add(jButton3_jToolBar_RightPanel);


        table_Model = new Table_Model_Issue_Hashes(0);
        Table_Hash = new MTable(table_Model);
        this.jScrollPane_jPanel_RightPanel.setViewportView(Table_Hash);

    }

    public void onIssueClick() {
        // DISABLE
        issue_Hash_Imprint.jButton.setEnabled(false);

        // CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            // NETWORK NOT OK
            JOptionPane.showMessageDialog(null,
                    Lang.getInstance().translate(
                            "You are unable to send a transaction while synchronizing or while having no connections!"),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

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
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                        Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

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
            feePow = Integer.parseInt((String)issue_Hash_Imprint.txtFeePow.getSelectedItem());
            // READ AMOUNT
            // float amount = Float.parseFloat(this.txtAmount.getText());

            // NAME TOTAL
            url = issue_Hash_Imprint.jTextField_URL.getText().trim();

            description = issue_Hash_Imprint.jTextArea_Description.getText();

        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid quantity!"),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }

        List<String> hashes = this.table_Model.getValues(0);

        List<String> twins = R_Hashes.findTwins(DCSet.getInstance(), hashes);
        if (!twins.isEmpty()) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate("Twin hashes") + ": " + twins.toString(),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            issue_Hash_Imprint.jButton.setEnabled(true);
            return;
        }

        // CREATE IMPRINT
        PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
        Pair<Transaction, Integer> result = Controller.getInstance().r_Hashes(creator, feePow, url, description,
                String.join(" ", hashes));

        // CHECK VALIDATE MESSAGE
        if (result.getB() == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Imprint issue has been sent!"),
                    Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            // this.dispose();
        } else {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate("Unknown error") + "[" + result.getB() + "]!",
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }

        // ENABLE
        issue_Hash_Imprint.jButton.setEnabled(true);
    }

    protected void Hashs_from_Files(boolean importing) {
        // TODO Auto-generated method stub
        // true - если импорт из файла
        // false - если создаем хэш для файлов

        // открыть диалог для файла
        //JFileChooser chooser = new JFileChooser();
        // руссификация диалога выбора файла
        //new All_Options().setUpdateUI(chooser);
        My_JFileChooser chooser = new My_JFileChooser();
        chooser.setDialogTitle(Lang.getInstance().translate("Select File"));


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
                    table_Model.addRow(new Object[]{"",
                            Lang.getInstance().translate("error reading") + " - " + file_name});
                }

                if (hashesStr.length() > 0) {
                    String[] hashes = hashesStr.split("\\s*(\\s|,|!|;|:|\n|\\.)\\s*");
                    for (String hashB58 : hashes) {
                        if (hashB58 != null && !hashB58.equals(new String("")))
                            table_Model.addRow(new Object[]{hashB58, Lang.getInstance().translate("imported from") + " " + file_name});
                    }

                }

            } else {

                // make HASHES from files
                File[] patchs = chooser.getSelectedFiles();

                for (File patch : patchs) {

                    /// HASHING
                    FileHash gf = new FileHash(patch);
                    String hashes = gf.getHash();
                    table_Model.addRow(new Object[]{hashes,
                            Lang.getInstance().translate("from file ") +  patch.getPath()});
                    gf = null;
                }


            }
            table_Model.addRow(new Object[]{"", ""});
            table_Model.fireTableDataChanged();
            Table_Hash.setRowSelectionInterval(table_Model.getRowCount() - 1, table_Model.getRowCount() - 1);


        }

    }

}
