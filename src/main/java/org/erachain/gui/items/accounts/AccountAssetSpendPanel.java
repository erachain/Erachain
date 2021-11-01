package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;

import java.math.BigDecimal;


@SuppressWarnings("serial")

public class AccountAssetSpendPanel extends AccountAssetActionPanelCls {

    public boolean noRecive;

    public AccountAssetSpendPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person, String message, boolean backward) {
        super(null, null, backward, assetIn, TransactionAmount.ACTION_SPEND, accountFrom, accountTo, message);

        iconName = "AccountAssetSpendPanel";
    }

    protected BigDecimal getAmount() {
        return amount == null ? null : amount.negate();
    }

    protected Long getAssetKey() {
        return -key;
    }

}