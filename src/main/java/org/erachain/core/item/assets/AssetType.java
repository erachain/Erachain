package org.erachain.core.item.assets;

import org.erachain.core.item.ItemCls;
import org.erachain.lang.Lang;
import org.mapdb.Fun;

import java.util.StringJoiner;

public class AssetType {
    Integer id;
    String name;
    String nameFull;
    String description;

    public AssetType(Integer assetType) {
        this.id = assetType;
        this.name = Lang.T(AssetCls.viewAssetTypeCls(assetType));
        this.nameFull = "<b>" + AssetCls.charAssetType(Long.MAX_VALUE, assetType)
                + AssetCls.viewAssetTypeAbbrev(assetType) + "</b>:" + Lang.T(AssetCls.viewAssetTypeFullCls(assetType));

        long startKey = ItemCls.getStartKey(
                AssetCls.ASSET_TYPE, AssetCls.START_KEY_OLD, AssetCls.MIN_START_KEY_OLD);
        StringJoiner joiner = new StringJoiner(", ");
        for (Fun.Tuple2<?, String> action : AssetCls.viewAssetTypeActionsList(startKey,
                assetType, null, true)) {
            joiner.add(Lang.T(action.b));
        }

        this.description = Lang.T(AssetCls.viewAssetTypeDescriptionCls(assetType)) + ".<br>";
        if (AssetCls.isReverseSend(assetType)) {
            description += Lang.T("Actions for OWN balance is reversed") + ".<br>";
        }
        description += "<b>" + Lang.T("Acceptable actions") + ":</b><br>" + joiner.toString();

        String dexDesc = AssetCls.viewAssetTypeDescriptionDEX(assetType, startKey);
        if (dexDesc != null) {
            description += "<br><b>" + Lang.T("DEX rules and taxes") + ":</b><br>" + Lang.T(dexDesc);
        }

    }

    @Override
    public String toString() {

        return "<HTML> {"
                // + NumberAsString.formatAsString(this.getBalanceUSE(FEE_KEY))
                + id
                + "}" + " " + nameFull;
    }

    public String getName(){
        return name;
    }
    public String getNameFull(){
        return nameFull;
    }
    public String getDescription(){
        return description;
    }
    public Integer getId(){
        return id;
    }
    
}
