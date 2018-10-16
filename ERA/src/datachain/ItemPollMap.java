package datachain;

import core.item.ItemCls;
import database.serializer.ItemSerializer;
import org.mapdb.DB;
import utils.ObserverMessage;

import java.util.Map;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Выборы<br>
 */
public class ItemPollMap extends Item_Map {

    static final String NAME = "item_polls";
    static final int TYPE = ItemCls.POLL_TYPE;

    public ItemPollMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                //TYPE,
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
                //.valueSerializer(new AssetSerializer())
                .makeOrGet();
    }

}
