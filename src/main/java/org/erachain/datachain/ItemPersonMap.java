package org.erachain.datachain;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.database.serializer.ItemSerializer;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.ReverseComparator;

import java.util.Map;
import java.util.NavigableSet;

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

    public ItemPersonMap(ItemMap parent) {
        super(parent);
    }

    // type+name not initialized yet! - it call as Super in New
    @SuppressWarnings("unchecked")
    protected Map<Long, ItemCls> getMap(DB database) {
        //OPEN MAP
        map = database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                .makeOrGet();
        if (Controller.getInstance().onlyProtocolIndexing) {
            // NOT USE SECONDARY INDEXES
            return map;
        }

        makeOwnerKey(database);

        return map;
    }

}
