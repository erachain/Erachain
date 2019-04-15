package org.erachain.datachain;

import org.erachain.controller.Controller;
import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

import java.util.Map;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Выборы<br>
 */
public class ItemPollMap extends ItemMap {

    static final String NAME = "item_polls";
    private static final int TYPE = ItemCls.POLL_TYPE;

    public ItemPollMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                NAME,
                ObserverMessage.RESET_POLL_TYPE,
                ObserverMessage.ADD_POLL_TYPE,
                ObserverMessage.REMOVE_POLL_TYPE,
                ObserverMessage.LIST_POLL_TYPE
        );

    }

    public ItemPollMap(ItemPollMap parent) {
        super(parent);
    }

    // type+name not initialized yet! - it call as Super in New
    protected Map<Long, ItemCls> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                .makeOrGet();

    }

}
