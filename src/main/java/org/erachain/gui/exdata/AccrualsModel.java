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

    public AccrualsModel(List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>> accruals) {
        super(new String[]{Lang.T("No."),
                        Lang.T("Balance"),
                        Lang.T("Account"),
                        Lang.T("Accrual"),
                        Lang.T("Error")
                },
                accruals.size());
        setRows(accruals);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    public void setRows(List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>> accruals) {
        int count = 0;
        Vector vector = getDataVector();
        for (Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>> item : accruals) {

            Vector<Object> rowVector = new Vector<Object>(4);
            rowVector.addElement(count + 1);
            rowVector.addElement(item.b.toPlainString());
            rowVector.addElement(item.a.getPersonAsString());
            rowVector.addElement(item.c.toPlainString());
            rowVector.addElement(item.d == null ? "" : Lang.T(OnDealClick.resultMess(item.d.a)));

            vector.set(count++, rowVector);
        }
        fireTableDataChanged();
    }
}

