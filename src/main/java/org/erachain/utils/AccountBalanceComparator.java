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
        return -o1.getBalanceInSettedPosition(AssetCls.FEE_KEY).b.compareTo(o2.getBalanceInSettedPosition(AssetCls.FEE_KEY).b);
    }

}
