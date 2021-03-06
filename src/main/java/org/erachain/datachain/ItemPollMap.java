package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Выборы<br>
 */
public class ItemPollMap extends ItemMap {

    public ItemPollMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                ItemCls.POLL_TYPE, ObserverMessage.RESET_POLL_TYPE,
                ObserverMessage.ADD_POLL_TYPE,
                ObserverMessage.REMOVE_POLL_TYPE,
                ObserverMessage.LIST_POLL_TYPE
        );

    }

    public ItemPollMap(ItemPollMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
