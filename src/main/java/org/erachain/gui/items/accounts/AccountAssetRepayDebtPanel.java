package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;

import java.math.BigDecimal;

@SuppressWarnings("serial")

public class AccountAssetRepayDebtPanel extends AccountAssetActionPanelCls {

    public AccountAssetRepayDebtPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person) {
        super(null, null, false, assetIn,
                TransactionAmount.ACTION_REPAY_DEBT, accountFrom, accountTo, null);

        iconName = "AccountAssetRepayDebtPanel";
    }

    protected BigDecimal getAmount() {
        return amount;
    }

    protected Long getAssetKey() {
        return -key;
    }

}