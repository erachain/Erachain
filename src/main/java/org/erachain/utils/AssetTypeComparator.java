package org.erachain.utils;

import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;

import java.util.Comparator;

/**
 * Sorts Accounts by balance.
 *
 * @author Skerberus
 */
public class AssetTypeComparator implements Comparator<AssetType> {

    @Override
    public int compare(AssetType o1, AssetType o2) {
        return o1.getId().compareTo(o2.getId());
    }

}
