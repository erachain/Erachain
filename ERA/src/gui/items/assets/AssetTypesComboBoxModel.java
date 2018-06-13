package gui.items.assets;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.item.assets.AssetCls;
import core.item.assets.AssetType;
import lang.Lang;
import utils.AccountBalanceComparator;
import utils.AssetTypeComparator;
import utils.ObserverMessage;

import javax.swing.*;
import java.util.*;

@SuppressWarnings("serial")
public class AssetTypesComboBoxModel extends DefaultComboBoxModel<AssetType> {

    public AssetTypesComboBoxModel() {
        // INSERT ALL ACCOUNTS
        
        ArrayList<AssetType> list = new ArrayList<AssetType>();

        if( BlockChain.DEVELOP_USE){
            list.add(new AssetType( AssetCls.AS_OUTSIDE_GOODS,Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_GOODS)), "Description AS_OUTSIDE_GOODS"));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_IMMOVABLE,Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_IMMOVABLE)),"Description AS_OUTSIDE_IMMOVABLE"));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_CURRENCY,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_CURRENCY)),
                                ""));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_SERVICE,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_SERVICE)),
                                ""));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_SHARE,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_SHARE)), ""));
            list.add(new AssetType(AssetCls.AS_OUTSIDE_OTHER_CLAIM,
                                Lang.getInstance()
                                        .translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_OTHER_CLAIM)),
                                ""));
            list.add(new AssetType(AssetCls.AS_INSIDE_ASSETS,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_ASSETS)), ""));
            list.add(new AssetType(AssetCls.AS_INSIDE_CURRENCY,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_CURRENCY)),
                                ""));
            list.add(new AssetType(AssetCls.AS_INSIDE_UTILITY,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_UTILITY)),
                                ""));
            list.add(new AssetType(AssetCls.AS_INSIDE_SHARE,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_SHARE)), ""));
            list.add(new AssetType(AssetCls.AS_INSIDE_BONUS,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_BONUS)), ""));
            list.add(new AssetType(AssetCls.AS_INSIDE_RIGHTS,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_RIGHTS)), ""));
            list.add(new AssetType(AssetCls.AS_INSIDE_OTHER_CLAIM,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_OTHER_CLAIM)),
                                ""));
            list.add(new AssetType(AssetCls.AS_ACCOUNTING,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_ACCOUNTING)), ""));
                        }
                else {
                    list.add(new AssetType(AssetCls.AS_OUTSIDE_GOODS,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_GOODS)), ""));
                    list.add( new AssetType(AssetCls.AS_OUTSIDE_IMMOVABLE,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_IMMOVABLE)), ""));
                    list.add(new AssetType(AssetCls.AS_OUTSIDE_CURRENCY,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_CURRENCY)), ""));
                    list.add(new AssetType(AssetCls.AS_OUTSIDE_SERVICE,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_SERVICE)), ""));
                    list.add(new AssetType(AssetCls.AS_OUTSIDE_SHARE,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_SHARE)), ""));
                    list.add(new AssetType(AssetCls.AS_OUTSIDE_OTHER_CLAIM,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_OUTSIDE_OTHER_CLAIM)), ""));
                    list.add(new AssetType(AssetCls.AS_INSIDE_ASSETS,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_ASSETS)), ""));
                    list.add( new AssetType(AssetCls.AS_INSIDE_CURRENCY,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_CURRENCY)), ""));
                    list.add(new AssetType(AssetCls.AS_INSIDE_UTILITY,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_UTILITY)), ""));
                    list.add(new AssetType(AssetCls.AS_INSIDE_SHARE,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_SHARE)), ""));
                    list.add(new AssetType(AssetCls.AS_INSIDE_BONUS,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_BONUS)), ""));
                    list.add(new AssetType(AssetCls.AS_INSIDE_RIGHTS,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_RIGHTS)), ""));
                    list.add(new AssetType(AssetCls.AS_INSIDE_OTHER_CLAIM,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_INSIDE_OTHER_CLAIM)), ""));
                    list.add(new AssetType(AssetCls.AS_ACCOUNTING,
                                Lang.getInstance().translate(AssetCls.viewAssetTypeCls(AssetCls.AS_ACCOUNTING)), ""));
                        };

       ;

       // ArrayList<AssetType> accoountsToSort = new ArrayList<Account>(Controller.getInstance().getAccounts());
        Collections.sort(list, new AssetTypeComparator());
       // Collections.reverse(list);
        for (AssetType type : list) {
            this.addElement(type);
        }

    }

}
