package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;

import java.math.BigDecimal;


@SuppressWarnings("serial")

public class AccountAssetSendPanel extends AccountAssetActionPanelCls {

    public AccountAssetSendPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person, String message, boolean backward) {
        super(null, null, backward, assetIn, TransactionAmount.ACTION_SEND, accountFrom, accountTo, message);

        iconName = "AccountAssetSendPanel";
    }

    protected BigDecimal getAmount() {
        return amount;
    }

    protected Long getAssetKey() {
        return key;
    }

}