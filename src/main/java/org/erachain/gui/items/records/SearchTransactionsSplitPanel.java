package org.erachain.gui.items.records;

import org.erachain.gui.models.SearchTransactionsTableModel;

/**
 * search transactions
 */
public class SearchTransactionsSplitPanel extends SearchTransactionsSplitPanelClass {

    public static String NAME = "SearchTransactionsSplitPanel";
    public static String TITLE = "Search Transactions";

    public SearchTransactionsSplitPanel() {
        super(NAME, TITLE, new SearchTransactionsTableModel());
    }

}