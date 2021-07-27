package org.erachain.gui.items.statement;


import org.erachain.core.transaction.RSignNote;

public class FavoriteStatementsSplitPanel extends StatementsSplitPanel<RSignNote> {

    public static String NAME = "FavoriteStatementsSplitPanel";
    public static String TITLE = "Favorite Documents";

    private static final long serialVersionUID = 271757109356259483L;

    public FavoriteStatementsSplitPanel() {
        super(NAME, TITLE, new FavoriteStatementsTableModel(), FavoriteStatementsTableModel.COLUMN_SEQNO, true);
    }

    @Override
    RSignNote getTransaction(RSignNote rNote) {
        return rNote;
    }

}
