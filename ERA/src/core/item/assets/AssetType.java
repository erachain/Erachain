package core.item.assets;

import org.mapdb.Fun.Tuple2;

import core.item.persons.PersonCls;

public class AssetType {
    Integer id;
    String name;
    String description;
    
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
