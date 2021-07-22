package org.erachain.gui.items.statement;


public class FavoriteStatementsSplitPanel extends StatementsSplitPanel {

    public static String NAME = "FavoriteStatementsSplitPanel";
    public static String TITLE = "Favorite Documents";

    private static final long serialVersionUID = 271757109356259483L;

    public FavoriteStatementsSplitPanel() {
        super(NAME, TITLE, new FavoriteStatementsTableModel(), FavoriteStatementsTableModel.COLUMN_SEQNO, true);
    }

}
