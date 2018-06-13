package core.item.assets;

import lang.Lang;

public class AssetType {
    Integer id;
    String name;
    String description;

    public AssetType(Integer id){
        this.id = id;
        this.name = Lang.getInstance().translate(AssetCls.viewAssetTypeCls(id));
        this.description = Lang.getInstance().translate(AssetCls.viewAssetTypeDescriptionCls(id));
        
    }

    public AssetType(Integer id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
        
    }
    @Override
    public String toString() {
        

        return " {"
                // + NumberAsString.formatAsString(this.getBalanceUSE(FEE_KEY))
                + id
                + "}" + " " + name ;
    }
    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    public Integer getId(){
        return id;
    }
    
}
