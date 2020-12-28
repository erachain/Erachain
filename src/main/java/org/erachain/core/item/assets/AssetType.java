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
        this.name = Lang.getInstance().translate(AssetCls.viewAssetTypeCls(assetType));
        this.nameFull = Lang.getInstance().translate(AssetCls.viewAssetTypeFullCls(assetType));

        StringJoiner joiner = new StringJoiner(", ");
        for (Fun.Tuple2<?, String> action : AssetCls.viewAssetTypeActionsList(ItemCls.getStartKey(
                AssetCls.ASSET_TYPE, AssetCls.START_KEY_OLD, AssetCls.MIN_START_KEY_OLD),
                assetType, null, true)) {
            joiner.add(Lang.getInstance().translate(action.b));
        }

        this.description = Lang.getInstance().translate(AssetCls.viewAssetTypeDescriptionCls(assetType)) + ".<br>";
        if (AssetCls.isReverseSend(assetType)) {
            description += Lang.getInstance().translate("Actions for OWN balance is reversed") + ".<br>";
        }
        description += "<b>" + Lang.getInstance().translate("Acceptable actions") + ":</b><br>" + joiner.toString();

    }

    @Override
    public String toString() {


        return " {"
                // + NumberAsString.formatAsString(this.getBalanceUSE(FEE_KEY))
                + id
                + "}" + " " + nameFull ;
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
