package org.erachain.gui.exdata;

import org.erachain.core.account.Account;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

public class PayListModel extends DefaultTableModel {

    public static String lastError;
    static Vector<Object> headVector = new Vector<Object>(8) {{
        add(Lang.T("No."));
        add(Lang.T("Account"));
        add(Lang.T("Amount"));
        add(Lang.T("Memo"));
        add(Lang.T("Error"));
    }};

    public PayListModel() {
        super(new Vector(), headVector);
        lastError = null;
    }

    public PayListModel(List<String> lines) {
        super(setRows(lines), headVector);
    }

    public PayListModel(List<Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>> accruals, boolean onlyErrors) {
        super(setRows(accruals, onlyErrors), headVector);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false && column == 1;
    }

    /**
     * separate lines by SPACE
     *
     * @param lines
     * @return
     */
    static Vector setRows(List<String> lines) {
        lastError = null;

        int count = 0;
        Vector<Vector> data = new Vector();

        Vector<Object> rowVector;
        Fun.Tuple2<Account, String> result;
        String error;
        for (String row : lines) {
            rowVector = new Vector<Object>(8);
            error = "";
            String[] items = row.split(" ");
            result = Account.tryMakeAccount(items[0]);
            rowVector.addElement(++count);
            if (result.a == null) {
                rowVector.add(items[0]);
                error = lastError = Lang.T(result.b);
            } else {
                rowVector.add(result.a);
            }

            if (items.length < 2) {
                rowVector.add(BigDecimal.ZERO);
                rowVector.add("");
                error = lastError = Lang.T("Amount is missing");
                rowVector.add(error);
                data.add(rowVector);
                continue;
            }

            try {
                rowVector.add(new BigDecimal(items[1]));
            } catch (Exception e) {
                rowVector.add(items[1]);
                error = lastError = Lang.T("Wrong amount # Ошибка в кол-ве");
            }

            if (items.length > 2) {
                int inx = row.indexOf(" ", items[0].length() + 1) + 1;
                rowVector.add(row.substring(inx));
            } else {
                rowVector.add("");
            }

            rowVector.add(error);

            data.add(rowVector);
        }

        return data;
    }

    static Vector setRows(List<Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>> accruals, boolean onlyErrors) {
        lastError = null;

        int count = 0;
        Vector data = new Vector();

        Vector<Object> rowVector;

        for (Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>> item : accruals) {

            if (onlyErrors && item.b == null)
                continue;

            rowVector = new Vector<Object>(8);
            rowVector.addElement(++count);
            rowVector.addElement(item.a);
            if (item.b == null) {
                rowVector.addElement("");
            } else {
                rowVector.addElement(Lang.T(OnDealClick.resultMess(item.c.a)) + (item.c.b == null ? "" : " - " + item.c.b));
            }

            data.add(rowVector);
        }

        return data;
    }

}

