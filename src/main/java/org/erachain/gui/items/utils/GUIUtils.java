package org.erachain.gui.items.utils;

import org.erachain.controller.Controller;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.lang.Lang;

import javax.swing.*;

public class GUIUtils {
    public static boolean checkWalletUnlock(JButton button) {
        // CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            // ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(MainFrame.getInstance());
            if (!Controller.getInstance().unlockWallet(password)) {
                // WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"),
                        Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
                // ENABLE
                if (button != null) {
                    button.setEnabled(true);
                }
                return true;
            }
        }
        return false;
    }
    public static boolean checkWalletUnlock() {
        return checkWalletUnlock(null);
    }
}
