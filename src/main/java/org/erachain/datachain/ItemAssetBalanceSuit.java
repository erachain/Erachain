package org.erachain.datachain;

import org.erachain.core.account.Account;

import java.util.Collection;
import java.util.Iterator;

public interface ItemAssetBalanceSuit {

    Iterator<byte[]> assetIterator(long assetKey);
    Collection<byte[]> assetKeys(long assetKey);

    Iterator<byte[]> accountIterator(Account account);
    Collection<byte[]> accountKeys(Account account);

}
