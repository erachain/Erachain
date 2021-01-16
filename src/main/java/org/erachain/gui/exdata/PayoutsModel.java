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
        super(new String[]{Lang.T("No"),
                        Lang.T("Balance"),
                        Lang.T("Account"),
                        Lang.T("Payout")
                },
                payouts.size() + 1);
        setRows(payouts);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    public void setRows(List<Fun.Tuple3<Account, BigDecimal, BigDecimal>> payouts) {
        int count = 1;
        Vector vector = getDataVector();
        for (Fun.Tuple3<Account, BigDecimal, BigDecimal> item : payouts) {
            vector.set(count, new Object[]{++count, item.b.toPlainString(), item.a.getPersonAsString(), item.c.toPlainString()});
        }
        fireTableDataChanged();
    }
}

