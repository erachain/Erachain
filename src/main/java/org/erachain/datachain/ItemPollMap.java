package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

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

    public ItemPollMap(ItemPollMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    // type+name not initialized yet! - it call as Super in New
    public void openMap() {
        //OPEN MAP
        map = database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                .makeOrGet();

    }

}
