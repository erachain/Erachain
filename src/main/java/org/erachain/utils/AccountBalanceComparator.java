package org.erachain.utils;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;

import java.util.Comparator;

/**
 * Sorts Accounts by balance.
 *
 * @author Skerberus
 */
public class AccountBalanceComparator implements Comparator<Account> {

    @Override
    public int compare(Account o1, Account o2) {
        return o1.getConfBalance3(1, AssetCls.FEE_KEY).a.compareTo(o2.getConfBalance3(1, AssetCls.FEE_KEY).a);
    }

}
