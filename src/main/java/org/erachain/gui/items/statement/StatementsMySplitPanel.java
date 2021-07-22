package org.erachain.gui.items.statement;


import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.mapdb.Fun;

public class StatementsMySplitPanel extends StatementsSplitPanel<Fun.Tuple2<Fun.Tuple2<Long, Integer>, Transaction>> {

    public static String NAME = "StatementsMySplitPanel";
    public static String TITLE = "My Documents";

    private static final long serialVersionUID = 2717571093561259483L;

    public StatementsMySplitPanel() {
        super(NAME, TITLE, new MyStatementsTableModel(), MyStatementsTableModel.COLUMN_SEQNO, false);
    }

    @Override
    RSignNote getTransaction(Fun.Tuple2<Fun.Tuple2<Long, Integer>, Transaction> item) {
        return (RSignNote) item.b;
    }

}