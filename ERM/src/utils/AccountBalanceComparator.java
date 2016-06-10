package utils;

import java.util.Comparator;

import core.account.Account;
import core.item.assets.AssetCls;

/**
 * Sorts Accounts by balance.
 * @author Skerberus
 *
 */
public class AccountBalanceComparator implements Comparator<Account> {

	@Override
	public int compare(Account o1, Account o2) {
		return o1.getBalance(1, AssetCls.FEE_KEY).compareTo(o2.getBalance(1, AssetCls.FEE_KEY));
	}

}
