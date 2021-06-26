package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static org.erachain.gui.PasswordPane.getButton;

public class WalletImportButton extends WalletButton {

    public WalletImportButton() {
        super("Import", "Import private key");
    }

    void action() {

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new GridLayout(2, 2));

        //Labels for the textfield components
        JLabel label = new JLabel(Lang.T("Enter secret key") + ":");
        JTextField field = new JTextField();

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                Container parent = field.getTopLevelAncestor();

                if (keyCode == KeyEvent.VK_ENTER) {
                } else if (keyCode == KeyEvent.VK_ESCAPE) {
                    final JButton btn = getButton(parent, Lang.T("Cancel"));
                    btn.doClick();
                }
            }
        });

        // Add the components to the JPanel
        userPanel.add(label);
        userPanel.add(field);

        int n = JOptionPane.showOptionDialog(
                null,
                userPanel,
                Lang.T("Import private key"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null
        );

        if (n == JOptionPane.NO_OPTION) {
            return;
        }

        // DECODE SEED
        byte[] privateKeyBytes64;
        try {
            privateKeyBytes64 = Base58.decode(field.getText().trim(), Crypto.SIGNATURE_LENGTH);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    Lang.T("Wrong Base58 format") + ":" + e.getMessage(), Lang.T("ERROR"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // CHECK SEED LENGTH
        if (privateKeyBytes64 == null) {
            JOptionPane.showMessageDialog(null, Lang.T("Wrong Base58 format"), Lang.T("ERROR"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // CONVERT TO BYTE
        String mess = Controller.getInstance().importPrivateKey(privateKeyBytes64);
        if (mess.isEmpty())
            return;

        JOptionPane.showMessageDialog(null, Lang.T(mess), Lang.T("ERROR"), JOptionPane.ERROR_MESSAGE);


    }
}
