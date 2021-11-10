package org.erachain.gui.items.accounts;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.database.wallet.FavoriteAccountsMap;
import org.erachain.gui.PasswordPane;
import org.erachain.lang.Lang;
import org.erachain.utils.MenuPopupUtil;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Саша
 */
public class AccountNameAdd extends javax.swing.JDialog {

    FavoriteAccountsMap favoriteAccountsMap = Controller.getInstance().getWallet().dwSet.getFavoriteAccountsMap();
    private AccountNameAdd th;
    // Variables declaration - do not modify
    private javax.swing.ButtonGroup buttonGroupSelectType;
    private javax.swing.JButton jButton_Cancel;
    private javax.swing.JButton jButton_OK;
    private javax.swing.JLabel jLabel_Account;
    private javax.swing.JLabel jLabel_Description;
    private javax.swing.JLabel jLabel_Name;
    private javax.swing.JLabel jLabel_Title;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextAreaDescription;
    private javax.swing.JTextField jTextFieldAccount;
    private javax.swing.JTextField jTextFieldName;

    /**
     * Creates new form Account_Show
     */
    public AccountNameAdd() {

        super();
        th = this;
        if (false && !Controller.getInstance().isWalletUnlocked()) {
            //ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (password.equals("")) {
                this.jButton_OK.setEnabled(true);
                return;
            }
            if (!Controller.getInstance().unlockWallet(password)) {
                //WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.jButton_OK.setEnabled(true);
                return;
            }
        }

        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        initComponents();
        jButton_Cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                setVisible(false);
            }
        });

        jButton_OK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String accountName = th.jTextFieldName.getText();
                String description = th.jTextAreaDescription.getText();
                if (accountName.trim().length() == 0) {
                    JOptionPane.showMessageDialog(null, Lang.T("Empty Name"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    String address = th.jTextFieldAccount.getText();
                    Tuple2<Account, String> result = Account.tryMakeAccount(address);
                    if (result.a == null) {
                        JOptionPane.showMessageDialog(null, Lang.T(result.b),
                                Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        Account newAccount = result.a;
                        if (newAccount instanceof PublicKeyAccount) {
                            favoriteAccountsMap.put(newAccount.getAddress(), new Tuple3(((PublicKeyAccount) newAccount).getBase58(),
                                    accountName, description));
                        } else {
                            favoriteAccountsMap.put(newAccount.getAddress(), new Tuple3(null, accountName, description));
                        }
                        setVisible(false);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, Lang.T("Invalid Account!"), Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
                }
            }

        });

        // popup menu
        MenuPopupUtil.installContextMenu(jTextFieldAccount);
        MenuPopupUtil.installContextMenu(jTextFieldName);
        MenuPopupUtil.installContextMenu(jTextAreaDescription);

        this.setLocationRelativeTo(null);
        setModal(true);
        //   setAlwaysOnTop(true);
        this.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupSelectType = new javax.swing.ButtonGroup();
        jLabel_Title = new javax.swing.JLabel();
        jLabel_Name = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaDescription = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jButton_OK = new javax.swing.JButton();
        jButton_Cancel = new javax.swing.JButton();
        jLabel_Description = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel_Account = new javax.swing.JLabel();
        jTextFieldAccount = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[]{0, 5, 0, 5, 0, 5, 0, 5, 0};
        layout.rowHeights = new int[]{0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0, 8, 0};
        getContentPane().setLayout(layout);

        jLabel_Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Title.setText(Lang.T("Add new account"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        getContentPane().add(jLabel_Title, gridBagConstraints);

        jLabel_Name.setText(Lang.T("Name") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        getContentPane().add(jLabel_Name, gridBagConstraints);

        jTextFieldName.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        getContentPane().add(jTextFieldName, gridBagConstraints);

        jTextAreaDescription.setColumns(20);
        jTextAreaDescription.setRows(5);
        jScrollPane2.setViewportView(jTextAreaDescription);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.weighty = 0.4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[]{0, 5, 0};
        jPanel1Layout.rowHeights = new int[]{0};
        jPanel1.setLayout(jPanel1Layout);

        jButton_OK.setText(Lang.T("Ok"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanel1.add(jButton_OK, gridBagConstraints);

        jButton_Cancel.setText(Lang.T("Cancel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel1.add(jButton_Cancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        getContentPane().add(jPanel1, gridBagConstraints);

        jLabel_Description.setText(Lang.T("Description") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        getContentPane().add(jLabel_Description, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 10);
        getContentPane().add(jPanel2, gridBagConstraints);

        jLabel_Account.setText(Lang.T("Account") + ":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        getContentPane().add(jLabel_Account, gridBagConstraints);

        jTextFieldAccount.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        getContentPane().add(jTextFieldAccount, gridBagConstraints);

        pack();

    }// </editor-fold>
    // End of variables declaration                   
}
