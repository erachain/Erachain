package org.erachain.gui.items.statement;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.items.records.SearchTransactionsSplitPanelClass;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchStatementsSplitPanel extends SearchTransactionsSplitPanelClass {

    public static String NAME = "SearchStatementsSplitPanel";
    public static String TITLE = "Search Documents";

    JMenuItem getPasswordMenuItems = new JMenuItem(Lang.T("Retrieve Password"));

    public SearchStatementsSplitPanel() {
        super(NAME, TITLE, new SearchStatementsTableModel());

        // favorite menu
        getPasswordMenuItems.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedTransaction == null)
                    return;

                RSignNote rNote = (RSignNote) selectedTransaction;
                if (rNote.isEncrypted()) {

                    Controller cntr = Controller.getInstance();
                    if (!cntr.isWalletUnlocked()) {
                        //ASK FOR PASSWORD
                        String password = PasswordPane.showUnlockWalletDialog(null);
                        if (!cntr.unlockWallet(password)) {
                            //WRONG PASSWORD
                            JOptionPane.showMessageDialog(null, Lang.T("Invalid password"), Lang.T("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    Account account = cntr.getInvolvedAccount(rNote);
                    Fun.Tuple3<Integer, String, byte[]> result = rNote.getPassword(account);
                    if (result.a < 0) {
                        JOptionPane.showMessageDialog(null,
                                Lang.T(result.b == null ? "Not exists Account access" : result.b),
                                Lang.T("Not decrypted"), JOptionPane.ERROR_MESSAGE);
                        return;

                    } else if (result.b != null) {
                        JOptionPane.showMessageDialog(null,
                                Lang.T(" In pos: " + result.a + " - " + result.b),
                                Lang.T("Not decrypted"), JOptionPane.ERROR_MESSAGE);
                        return;

                    }

                    StringSelection stringSelection = new StringSelection(Base58.encode(result.c));
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                    JOptionPane.showMessageDialog(new JFrame(),
                            Lang.T("Password of the '%1' has been copy to buffer")
                                    .replace("%1", rNote.viewHeightSeq())
                                    + ".",
                            Lang.T("Success"), JOptionPane.INFORMATION_MESSAGE);

                }
            }
        });
        mainMenu.add(getPasswordMenuItems, mainMenu.getComponentCount() - 1);
        mainMenu.add(new JPopupMenu.Separator(), mainMenu.getComponentCount() - 1);

    }

    @Override
    protected void favorite_set(Transaction transaction) {

        // CHECK IF FAVORITES
        if (cnt.isDocumentFavorite(transaction)) {
            int dd = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.T("Delete from favorite") + "?", Lang.T("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);

            if (dd == 0) cnt.removeDocumentFavorite(transaction);
        } else {
            cnt.addDocumentFavorite(transaction);
        }

        jTableJScrollPanelLeftPanel.repaint();

    }

    @Override
    protected void updateMenu() {
        getPasswordMenuItems.setEnabled(((RSignNote) selectedTransaction).isEncrypted());
    }

}
