package org.erachain.gui.items.statement;

import org.erachain.gui.items.records.SearchTransactionsSplitPanelClass;

public class SearchStatementsSplitPanel extends SearchTransactionsSplitPanelClass {

    public static String NAME = "SearchStatementsSplitPanel";
    public static String TITLE = "Search Documents";

    public SearchStatementsSplitPanel() {
        super(NAME, TITLE, new SearchStatementsTableModel());
    }

}
