package org.erachain.datachain;

import org.erachain.core.account.Account;
import org.erachain.dbs.IteratorCloseable;

import java.math.BigDecimal;

public interface ItemAssetBalanceSuit {

    IteratorCloseable<byte[]> getIteratorByAsset(long assetKey);

    IteratorCloseable<byte[]> getIteratorByAsset(long assetKey, BigDecimal fromOwnAmount);

    IteratorCloseable<byte[]> accountIterator(Account account);

}
