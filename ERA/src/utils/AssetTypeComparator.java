package utils;

import core.account.Account;
import core.item.assets.AssetCls;
import core.item.assets.AssetType;

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
