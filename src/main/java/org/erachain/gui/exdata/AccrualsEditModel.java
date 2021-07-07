package org.erachain.gui.exdata;

import org.erachain.core.account.Account;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccrualsEditModel extends AccrualsModel {

    public AccrualsEditModel() {
        super(new ArrayList<>(), false);
        addRow(new Object[]{1, "", "", "", ""});
    }

    public AccrualsEditModel(List<Fun.Tuple4<Account, BigDecimal, BigDecimal, Fun.Tuple2<Integer, String>>> accruals, boolean onlyErrors) {
        super(accruals, onlyErrors);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 2;
    }

}

