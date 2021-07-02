package org.erachain.gui.exdata;

import org.erachain.core.account.Account;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

public class AccrualsModel extends DefaultTableModel {

    static Vector<Object> headVector = new Vector<Object>(8) {{
        add(Lang.T("No."));
        add(Lang.T("Balance"));
        add(Lang.T("Account"));
        add(Lang.T("Accrual"));
        add(Lang.T("Error"));
    }};

    public AccrualsModel(List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>> accruals, boolean onlyErrors) {
        super(setRows(accruals, onlyErrors), headVector);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    static Vector<Object> setRows(List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>> accruals, boolean onlyErrors) {
        int count = 0;
        Vector vector = new Vector();

        Vector<Object> rowVector;

        for (Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>> item : accruals) {

            if (onlyErrors && item.d == null)
                continue;

            rowVector = new Vector<Object>(8);
            rowVector.addElement(++count);
            rowVector.addElement(item.b.toPlainString());
            rowVector.addElement(item.a.getPersonAsString());
            rowVector.addElement(item.c.toPlainString());
            if (item.d == null) {
                rowVector.addElement("");
            } else {
                rowVector.addElement(Lang.T(OnDealClick.resultMess(item.d.a)) + (item.d.b == null ? "" : " - " + item.d.b));
            }

            vector.add(rowVector);
        }
        return vector;
    }
}

