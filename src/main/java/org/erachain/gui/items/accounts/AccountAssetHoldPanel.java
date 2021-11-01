package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;

import java.math.BigDecimal;

public class AccountAssetHoldPanel extends AccountAssetActionPanelCls {

    public AccountAssetHoldPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person, boolean backward) {
        super(null, null, backward, assetIn, TransactionAmount.ACTION_HOLD, accountFrom, accountTo, null);

        iconName = "AccountAssetHoldPanel";

    }

    protected BigDecimal getAmount() {
        return amount == null ? null : amount.negate();
    }

    protected Long getAssetKey() {
        return key;
    }

}