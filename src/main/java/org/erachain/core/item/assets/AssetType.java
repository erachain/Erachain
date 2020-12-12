package org.erachain.core.item.assets;

import org.erachain.core.item.ItemCls;
import org.erachain.lang.Lang;

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
        for (String action : AssetCls.viewAssetTypeActionsList(ItemCls.getStartKey(
                AssetCls.ASSET_TYPE, AssetCls.START_KEY, AssetCls.MIN_START_KEY),
                assetType)) {
            joiner.add(Lang.getInstance().translate(action));
        }

        this.description = Lang.getInstance().translate(AssetCls.viewAssetTypeDescriptionCls(assetType)) + ".\n";
        if (AssetCls.isReverseSend(assetType)) {
            description += Lang.getInstance().translate("Actions for OWN balance is reversed") + ".\n";
        }
        description += Lang.getInstance().translate("Acceptable actions") + ":\n" + joiner.toString();

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
