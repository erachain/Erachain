package org.erachain.gui.items.statement;

import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.records.SearchTransactionsSplitPanelClass;
import org.erachain.lang.Lang;

import javax.swing.*;
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
                    RNoteInfo.retrievePassword(rNote);
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
