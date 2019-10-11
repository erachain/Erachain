package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Персона<br>
 */

public class ItemPersonMap extends ItemMap {

    static final String NAME = "item_persons";
    static final int TYPE = ItemCls.PERSON_TYPE;

    public ItemPersonMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                //TYPE,
                NAME,
                ObserverMessage.RESET_PERSON_TYPE,
                ObserverMessage.ADD_PERSON_TYPE,
                ObserverMessage.REMOVE_PERSON_TYPE,
                ObserverMessage.LIST_PERSON_TYPE
        );
    }

    public ItemPersonMap(ItemMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    // type+name not initialized yet! - it call as Super in New
    @SuppressWarnings("unchecked")
    protected void openMap() {
        //OPEN MAP
        map = database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                .makeOrGet();

    }

}
