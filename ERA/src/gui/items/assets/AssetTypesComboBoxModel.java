package gui.items.assets;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;

import core.BlockChain;
import core.item.assets.AssetCls;
import core.item.assets.AssetType;
import utils.AssetTypeComparator;

@SuppressWarnings("serial")
public class AssetTypesComboBoxModel extends DefaultComboBoxModel<AssetType> {
    
    public AssetTypesComboBoxModel() {
        // INSERT ALL ACCOUNTS
        
        ArrayList<AssetType> list = new ArrayList<AssetType>();
        
        if (BlockChain.DEVELOP_USE) {
            list.add(new AssetType(AssetCls.AS_OUTSIDE_GOODS));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_IMMOVABLE));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_CURRENCY));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_SERVICE));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_SHARE));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_BILL));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_BILL_EX));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_OTHER_CLAIM));
            
            list.add(new AssetType(AssetCls.AS_INSIDE_ASSETS));
            list.add(new AssetType(AssetCls.AS_INSIDE_CURRENCY));
            list.add(new AssetType(AssetCls.AS_INSIDE_UTILITY));
            list.add(new AssetType(AssetCls.AS_INSIDE_SHARE));
            list.add(new AssetType(AssetCls.AS_INSIDE_BONUS));
            list.add(new AssetType(AssetCls.AS_INSIDE_ACCESS));
            list.add(new AssetType(AssetCls.AS_INSIDE_VOTE));
            list.add(new AssetType(AssetCls.AS_INDEX));
            list.add(new AssetType(AssetCls.AS_INSIDE_OTHER_CLAIM));
            
            list.add(new AssetType(AssetCls.AS_ACCOUNTING));
        } else {
            list.add(new AssetType(AssetCls.AS_OUTSIDE_GOODS));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_IMMOVABLE));
            //list.add(new AssetType(AssetCls.AS_OUTSIDE_CURRENCY));
            //list.add(new AssetType(AssetCls.AS_OUTSIDE_SERVICE));
            //list.add(new AssetType(AssetCls.AS_OUTSIDE_SHARE));
            //list.add(new AssetType(AssetCls.AS_OUTSIDE_OTHER_CLAIM));
            
            list.add(new AssetType(AssetCls.AS_INSIDE_ASSETS));
            //list.add(new AssetType(AssetCls.AS_INSIDE_CURRENCY));
            //list.add(new AssetType(AssetCls.AS_INSIDE_UTILITY));
            //list.add(new AssetType(AssetCls.AS_INSIDE_SHARE));
            //list.add(new AssetType(AssetCls.AS_INSIDE_BONUS));
            //list.add(new AssetType(AssetCls.AS_INSIDE_RIGHTS));
            //list.add(new AssetType(AssetCls.AS_INSIDE_VOTE));
            list.add(new AssetType(AssetCls.AS_INDEX));
            //list.add(new AssetType(AssetCls.AS_INSIDE_OTHER_CLAIM));
            
            list.add(new AssetType(AssetCls.AS_ACCOUNTING));
        }
        
        // ArrayList<AssetType> accoountsToSort = new
        // ArrayList<Account>(Controller.getInstance().getAccounts());
        Collections.sort(list, new AssetTypeComparator());
        // Collections.reverse(list);
        for (AssetType type : list) {
            this.addElement(type);
        }
        
    }
    
}
