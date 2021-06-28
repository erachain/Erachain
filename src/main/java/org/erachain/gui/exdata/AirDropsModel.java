package org.erachain.gui.exdata;

import org.erachain.core.account.Account;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

public class AirDropsModel extends DefaultTableModel {

    public AirDropsModel(List<Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>> accruals) {
        super(new String[]{Lang.T("No."),
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

    public void setRows(List<Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>>> accruals) {
        int count = 0;
        Vector vector = getDataVector();
        for (Fun.Tuple3<Account, BigDecimal, Fun.Tuple2<Integer, String>> item : accruals) {

            Vector<Object> rowVector = new Vector<Object>(4);
            rowVector.addElement(count + 1);
            rowVector.addElement(item.a.getPersonAsString());
            rowVector.addElement(item.b.toPlainString());
            rowVector.addElement(item.c == null ? "" : Lang.T(OnDealClick.resultMess(item.c.a)));

            vector.set(count++, rowVector);
        }
        fireTableDataChanged();
    }
}

