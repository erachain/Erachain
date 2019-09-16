package org.erachain.datachain;

import java.util.Iterator;

public interface ItemAssetBalanceSuit {

    Iterator<byte[]> assetIterator(Long asset);

    Iterator<byte[]> addressIterator(String address);

}
