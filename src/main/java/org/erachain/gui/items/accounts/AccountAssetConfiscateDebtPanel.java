package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;

import java.math.BigDecimal;

//import org.erachain.settings.Settings;

@SuppressWarnings("serial")

public class AccountAssetConfiscateDebtPanel extends AccountAssetActionPanelCls {

    public AccountAssetConfiscateDebtPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person) {
        super(null, null, true, assetIn,
                TransactionAmount.ACTION_DEBT, accountFrom, accountTo, null);

        iconName = "AccountAssetConfiscateDebtPanel";

    }

    protected BigDecimal getAmount() {
        return amount;
    }

    protected Long getAssetKey() {
        return -key;
    }

}