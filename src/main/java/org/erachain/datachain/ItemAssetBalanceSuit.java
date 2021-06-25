package org.erachain.datachain;

import org.erachain.core.account.Account;
import org.erachain.dbs.IteratorCloseable;

public interface ItemAssetBalanceSuit {

    IteratorCloseable<byte[]> getIteratorByAsset(long assetKey);

    IteratorCloseable<byte[]> accountIterator(Account account);

}
