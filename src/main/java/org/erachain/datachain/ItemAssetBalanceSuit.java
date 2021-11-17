package org.erachain.datachain;

import org.erachain.core.account.Account;
import org.erachain.dbs.IteratorCloseable;

import java.math.BigDecimal;

public interface ItemAssetBalanceSuit {

    /**
     * Amount is negate already
     *
     * @param assetKey
     * @return
     */
    IteratorCloseable<byte[]> getIteratorByAsset(long assetKey);

    IteratorCloseable<byte[]> getIteratorByAsset(long assetKey, BigDecimal fromOwnAmount, byte[] addressShort, boolean descending);

    IteratorCloseable<byte[]> accountIterator(Account account);

}
