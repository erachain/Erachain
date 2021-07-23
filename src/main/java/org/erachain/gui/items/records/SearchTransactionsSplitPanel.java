package org.erachain.gui.items.records;

import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.models.SearchTransactionsTableModel;
import org.erachain.lang.Lang;

import javax.swing.*;

/**
 * search transactions
 */
public class SearchTransactionsSplitPanel extends SearchTransactionsSplitPanelClass {

    public static String NAME = "SearchTransactionsSplitPanel";
    public static String TITLE = "Search Transactions";

    public SearchTransactionsSplitPanel() {
        super(NAME, TITLE, new SearchTransactionsTableModel());
    }

    @Override
    protected void favorite_set(Transaction transaction) {

        // CHECK IF FAVORITES
        if (cnt.isTransactionFavorite(transaction)) {
            int dd = JOptionPane.showConfirmDialog(MainFrame.getInstance(), Lang.T("Delete from favorite") + "?", Lang.T("Delete from favorite"), JOptionPane.OK_CANCEL_OPTION);

            if (dd == 0) cnt.removeTransactionFavorite(transaction);
        } else {
            cnt.addTransactionFavorite(transaction);
        }

        jTableJScrollPanelLeftPanel.repaint();

    }

}