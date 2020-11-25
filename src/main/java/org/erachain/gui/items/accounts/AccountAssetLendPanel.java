package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;

import java.math.BigDecimal;

//import org.erachain.settings.Settings;

@SuppressWarnings("serial")

public class AccountAssetLendPanel extends AccountAssetActionPanelCls {

    public AccountAssetLendPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person) {
        // "If You want to give a loan asset %asset%, fill in this form"
        super(null, null, false, assetIn,
                TransactionAmount.ACTION_DEBT, accountFrom, accountTo, null);

        iconName = "AccountAssetLendPanel";

    }

    protected BigDecimal getAmount() {
        return amount;
    }

    protected Long getAssetKey() {
        return -key;
    }

}