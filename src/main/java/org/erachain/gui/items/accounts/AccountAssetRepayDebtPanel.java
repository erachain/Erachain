package org.erachain.gui.items.accounts;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.lang.Lang;

import java.math.BigDecimal;

@SuppressWarnings("serial")

public class AccountAssetRepayDebtPanel extends AccountAssetActionPanelCls {

    public AccountAssetRepayDebtPanel(AssetCls assetIn, Account accountFrom, Account accountTo, PersonCls person) {
        super(null, null, false, assetIn,
                TransactionAmount.ACTION_REPAY_DEBT, accountFrom, accountTo, null);

        iconName = "AccountAssetRepayDebtPanel";
        jLabel_Title.setText(Lang.T("Repay Debt") + " (" + Lang.T("Transfer to debt") + ")" + " - " + asset.viewName());

    }

    protected BigDecimal getAmount() {
        return amount;
    }

    protected Long getAssetKey() {
        return -key;
    }

}