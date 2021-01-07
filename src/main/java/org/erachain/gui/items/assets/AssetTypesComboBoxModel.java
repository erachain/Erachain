package org.erachain.gui.items.assets;

import org.erachain.core.BlockChain;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetType;
import org.erachain.utils.AssetTypeComparator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("serial")
public class AssetTypesComboBoxModel extends DefaultComboBoxModel<AssetType> {
    
    public AssetTypesComboBoxModel() {
        // INSERT ALL ACCOUNTS

        ArrayList<AssetType> list = new ArrayList<AssetType>();

        list.add(new AssetType(AssetCls.AS_OUTSIDE_GOODS));
        list.add(new AssetType(AssetCls.AS_OUTSIDE_IMMOVABLE));
        list.add(new AssetType(AssetCls.AS_OUTSIDE_CURRENCY));
        list.add(new AssetType(AssetCls.AS_OUTSIDE_SERVICE));
        list.add(new AssetType(AssetCls.AS_OUTSIDE_BILL));
        list.add(new AssetType(AssetCls.AS_OUTSIDE_WORK_TIME_HOURS));
        list.add(new AssetType(AssetCls.AS_OUTSIDE_WORK_TIME_MINUTES));
        list.add(new AssetType(AssetCls.AS_OUTSIDE_SHARE));

        list.add(new AssetType(AssetCls.AS_MY_DEBT));

        list.add(new AssetType(AssetCls.AS_OUTSIDE_OTHER_CLAIM));

        list.add(new AssetType(AssetCls.AS_INSIDE_ASSETS));
        list.add(new AssetType(AssetCls.AS_INSIDE_CURRENCY));
        list.add(new AssetType(AssetCls.AS_INSIDE_UTILITY));
        list.add(new AssetType(AssetCls.AS_INSIDE_SHARE));
        list.add(new AssetType(AssetCls.AS_INSIDE_BONUS));
        list.add(new AssetType(AssetCls.AS_INSIDE_ACCESS));
        list.add(new AssetType(AssetCls.AS_INSIDE_VOTE));
        list.add(new AssetType(AssetCls.AS_BANK_GUARANTEE));
        list.add(new AssetType(AssetCls.AS_BANK_GUARANTEE_TOTAL));
        list.add(new AssetType(AssetCls.AS_INDEX));
        list.add(new AssetType(AssetCls.AS_INSIDE_OTHER_CLAIM));

        list.add(new AssetType(AssetCls.AS_ACCOUNTING));
        list.add(new AssetType(AssetCls.AS_SELF_MANAGED_ACCOUNTING));
        list.add(new AssetType(AssetCls.AS_SELF_ACCOUNTING_LOAN));
        list.add(new AssetType(AssetCls.AS_SELF_ACCOUNTING_MUTUAL_AID_FUND));
        list.add(new AssetType(AssetCls.AS_SELF_ACCOUNTING_CASH_FUND));

        if (BlockChain.TEST_MODE) {
            // list.add(new AssetType(AssetCls.AS_SELF_ACCOUNTING_CASH_FUND));
        }

        Collections.sort(list, new AssetTypeComparator());
        // Collections.reverse(list);
        for (AssetType type : list) {
            this.addElement(type);
        }

    }
    
}
