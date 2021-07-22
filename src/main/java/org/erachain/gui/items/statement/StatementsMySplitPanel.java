package org.erachain.gui.items.statement;


public class StatementsMySplitPanel extends StatementsSplitPanel {

    public static String NAME = "StatementsMySplitPanel";
    public static String TITLE = "My Documents";

    private static final long serialVersionUID = 2717571093561259483L;

    public StatementsMySplitPanel() {
        super(NAME, TITLE, new MyStatementsTableModel(), MyStatementsTableModel.COLUMN_SEQNO, false);
    }
}