package org.erachain.core.item.assets;

import org.erachain.lang.Lang;

public class AssetType {
    Integer id;
    String name;
    String nameFull;
    String description;

    public AssetType(Integer id){
        this.id = id;
        this.name = Lang.getInstance().translate(AssetCls.viewAssetTypeCls(id));
        this.nameFull = Lang.getInstance().translate(AssetCls.viewAssetTypeFullCls(id));
        this.description = Lang.getInstance().translate(AssetCls.viewAssetTypeDescriptionCls(id));
        
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
