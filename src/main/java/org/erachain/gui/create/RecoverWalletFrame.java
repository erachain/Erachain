package org.erachain.gui.create;
// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class RecoverWalletFrame extends JFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecoverWalletFrame.class);
    private NoWalletFrame parent;
    private JTextField seedTxt;
    private JTextField passwordTxt;
    private JTextField amountTxt;
    private JTextField confirmPasswordTxt;
    private JTextField jTextFieldDataDir;

    public RecoverWalletFrame(NoWalletFrame parent) {
        super(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("Recover Wallet"));

        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //PARENT
        this.parent = parent;

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        //LABEL GBC
        GridBagConstraints labelGBC = new GridBagConstraints();
        labelGBC.insets = new Insets(5, 5, 5, 5);
        labelGBC.fill = GridBagConstraints.HORIZONTAL;
        labelGBC.anchor = GridBagConstraints.NORTHWEST;
        labelGBC.weightx = 1;
        labelGBC.gridwidth = 2;
        labelGBC.gridx = 0;

        //BUTTON GBC
        GridBagConstraints buttonGBC = new GridBagConstraints();
        buttonGBC.insets = new Insets(5, 5, 5, 5);
        buttonGBC.fill = GridBagConstraints.NONE;
        buttonGBC.anchor = GridBagConstraints.NORTHWEST;
        buttonGBC.gridwidth = 1;
        buttonGBC.gridx = 0;

        //LABEL
        labelGBC.gridy = 0;
        JLabel label1 = new JLabel(Lang.T("Please enter your wallet seed") + ":");
        this.add(label1, labelGBC);

        //ADD TEXTBOX
        labelGBC.gridy = 1;
        this.seedTxt = new JTextField();
        this.add(this.seedTxt, labelGBC);

        // MENU
        JPopupMenu menu = new JPopupMenu();
        JMenuItem pasteSeed = new JMenuItem(Lang.T("Paste"));
        pasteSeed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                try {
                    String clipboardContent = (String) clipboard.getData(DataFlavor.stringFlavor);
                    seedTxt.setText(clipboardContent);
                } catch (UnsupportedFlavorException | IOException e1) {
                    LOGGER.error(e1.getMessage(), e1);
                }
            }
        });
        menu.add(pasteSeed);
        seedTxt.setComponentPopupMenu(menu);

        //LABEL
        labelGBC.gridy = 2;
        labelGBC.insets.top = 00;
        JLabel label2 = new JLabel(Lang.T("Make sure your seed is in base58 format."));
        this.add(label2, labelGBC);

        //LABEL
        labelGBC.gridy = 3;
        labelGBC.insets.top = 10;
        JLabel label3 = new JLabel(Lang.T("Please create a new wallet password") + ":");
        this.add(label3, labelGBC);

        //ADD TEXTBOX
        labelGBC.gridy = 4;
        labelGBC.insets.top = 5;
        this.passwordTxt = new JPasswordField();
        this.add(this.passwordTxt, labelGBC);

        //LABEL
        labelGBC.gridy = 5;
        labelGBC.insets.top = 10;
        JLabel label4 = new JLabel(Lang.T("Please confirm your password") + ":");
        this.add(label4, labelGBC);

        //ADD TEXTBOX
        labelGBC.gridy = 6;
        labelGBC.insets.top = 5;
        this.confirmPasswordTxt = new JPasswordField();
        this.add(this.confirmPasswordTxt, labelGBC);

        //LABEL
        labelGBC.gridy = 7;
        labelGBC.insets.top = 10;
        JLabel label5 = new JLabel(Lang.T("Amount of accounts to recover") + ":");
        this.add(label5, labelGBC);

        //ADD TEXTBOX
        labelGBC.gridy = 8;
        labelGBC.insets.top = 5;
        this.amountTxt = new JTextField();
        this.amountTxt.setText("" + Settings.DEFAULT_ACCOUNTS);
        this.add(this.amountTxt, labelGBC);

        // path label
        labelGBC.gridy = labelGBC.gridy + 1;
        JLabel labelPath = new JLabel(Lang.T("Set the Wallet directory or leave it as default") + ":");
        this.add(labelPath, labelGBC);
        JPanel pan = new JPanel();
        pan.setLayout(new java.awt.GridBagLayout());
        GridBagConstraints panGBC = new GridBagConstraints();
        panGBC.insets = new Insets(5, 5, 5, 5);
        panGBC.fill = GridBagConstraints.HORIZONTAL;
        panGBC.anchor = GridBagConstraints.NORTHWEST;
        panGBC.weightx = 0.2;
        panGBC.gridx = 0;
        panGBC.gridy = 0;

        //path text
        jTextFieldDataDir = new JTextField(Settings.getInstance().getWalletKeysPath());
        jTextFieldDataDir.setEditable(false);
        pan.add(jTextFieldDataDir, panGBC);

        // button path  
        JButton btnBrowseWallet = new JButton(Lang.T("Browse..."));
        labelGBC.gridy = labelGBC.gridy + 1;
        btnBrowseWallet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                String path = jTextFieldDataDir.getText();
                File ff = new File(path);
                if (!ff.exists()) path = "." + File.separator;
                fileopen.setCurrentDirectory(new File(path));
                int ret = fileopen.showDialog(null, Lang.T("Set wallet dir"));
                if (ret == JFileChooser.APPROVE_OPTION) {
                    jTextFieldDataDir.setText(fileopen.getSelectedFile().toString());
                }
            }
        });

        panGBC = new java.awt.GridBagConstraints();
        panGBC.anchor = java.awt.GridBagConstraints.NORTHEAST;
        pan.add(btnBrowseWallet, panGBC);

        this.add(pan, labelGBC);

        //BUTTON confirm
        buttonGBC.gridy = labelGBC.gridy + 1;
        ;
        JButton confirmButton = new JButton(Lang.T("Confirm"));
        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onConfirmClick();
            }
        });
        this.add(confirmButton, buttonGBC);

        //BUTTON BACK
        buttonGBC.gridx = 1;
        JButton backButton = new JButton(Lang.T("Back"));
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBackClick();
            }
        });
        this.add(backButton, buttonGBC);

        //CLOSE NICELY
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Controller.getInstance().stopAndExit(0);
            }
        });

        //CALCULATE HEIGHT WIDTH
        this.pack();

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void onConfirmClick() {

        //CHECK IF SEEDS MATCH
        byte[] seed = null;
        try {
            seed = Base58.decode(this.seedTxt.getText().trim().replaceAll("\n", ""));
        } catch (Exception e) {
            seed = null;
        }

        if (seed == null || seed.length < Crypto.HASH_LENGTH - 3) {
            //INVALID SEED
            String message = Lang.T("Invalid or incorrect seed!") + " - " + (seed == null ? "NULL" : "byte[" + seed.length + "]");
            JOptionPane.showMessageDialog(new JFrame(), message, Lang.T("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        } else if (seed.length > 34) {
            // 64 bytes - from mobile
            return this.wallet.importPrivateKey(seed);
        }

        if (seed.length > Crypto.HASH_LENGTH
        String password = this.passwordTxt.getText();
        if (password.length() == 0) {
            //PASSWORD CANNOT BE EMPTY
            String message = Lang.T("Password cannot be empty!");
            JOptionPane.showMessageDialog(new JFrame(), message, Lang.T("Invalid password"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(this.confirmPasswordTxt.getText())) {
            //PASSWORDS DO NOT MATCH
            String message = Lang.T("Password do not match!");
            JOptionPane.showMessageDialog(new JFrame(), message, Lang.T("Invalid password"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amountString = this.amountTxt.getText();
        int amount = 0;

        try {
            amount = Integer.parseInt(amountString);
        } catch (Exception e) {
            //INVALID AMOUNT
            String message = Lang.T("Invalid amount!");
            JOptionPane.showMessageDialog(new JFrame(), message, Lang.T("Invalid amount"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (amount < 1 /*|| amount > 100*/) {
            //INVALID AMOUNT
            String message = Lang.T("Amount must be > 0!");
            JOptionPane.showMessageDialog(new JFrame(), message, Lang.T("Invalid amount"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        //RECOVER WALLET
        boolean res = Controller.getInstance().recoverWallet(seed, password, amount, jTextFieldDataDir.getText());
        if (res) {
            //CALLBACK
            this.parent.onWalletCreated();

            //CLOSE THIS WINDOW
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(
                    new JFrame(), Lang.T("Wallet already exists") + "!",
                    "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    private void onBackClick() {
        this.parent.setVisible(true);

        this.dispose();
    }
}
