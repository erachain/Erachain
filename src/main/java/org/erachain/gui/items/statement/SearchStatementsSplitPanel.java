package org.erachain.gui.items.statement;

import org.erachain.core.transaction.Transaction;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.records.SearchTransactionsSplitPanelClass;
import org.erachain.lang.Lang;

import javax.swing.*;

public class SearchStatementsSplitPanel extends SearchTransactionsSplitPanelClass {

    public static String NAME = "SearchStatementsSplitPanel";
    public static String TITLE = "Search Documents";

    public SearchStatementsSplitPanel() {
        super(NAME, TITLE, new SearchStatementsTableModel());
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

}
