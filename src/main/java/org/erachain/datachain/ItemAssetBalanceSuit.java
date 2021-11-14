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

    /**
     * for list holders. Amount is negate already
     *
     * @param assetKey
     * @param fromOwnAmount
     * @param descending
     * @return
     */
    IteratorCloseable<byte[]> getIteratorByAsset(long assetKey, BigDecimal fromOwnAmount, boolean descending);

    IteratorCloseable<byte[]> getIteratorByAsset(long assetKey, BigDecimal fromOwnAmount, byte[] addressShort, boolean descending);

    //IteratorCloseable<byte[]> getIteratorByAsset(byte[] secondaryKey, boolean descending);

    IteratorCloseable<byte[]> accountIterator(Account account);

    /**
     * нужно для поиска кобча всего для первой страницы если пользователь задал только Актив и величину Имею
     *
     * @param assetKey
     * @param fromAmount
     * @return
     */
    byte[] getAssetFloorKey(long assetKey, BigDecimal fromAmount);

}
