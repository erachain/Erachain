package org.erachain.gui.exdata;

import org.erachain.core.account.Account;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

public class PayoutsModel extends DefaultTableModel {

    public PayoutsModel(List<Fun.Tuple3<Account, BigDecimal, BigDecimal>> payouts) {
        super(new String[]{Lang.T("No."),
                        Lang.T("Balance"),
                        Lang.T("Account"),
                        Lang.T("Payout")
                },
                payouts.size());
        setRows(payouts);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    public void setRows(List<Fun.Tuple3<Account, BigDecimal, BigDecimal>> payouts) {
        int count = 0;
        Vector vector = getDataVector();
        for (Fun.Tuple3<Account, BigDecimal, BigDecimal> item : payouts) {

            Vector<Object> rowVector = new Vector<Object>(4);
            rowVector.addElement(count + 1);
            rowVector.addElement(item.b.toPlainString());
            rowVector.addElement(item.a.getPersonAsString());
            rowVector.addElement(item.c.toPlainString());

            vector.set(count++, rowVector);
        }
        fireTableDataChanged();
    }
}

