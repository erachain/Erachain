package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Представление<br>
 */
public class ItemStatementMap extends ItemMap {

    static final String NAME = "item_statements";
    private static final int TYPE = ItemCls.STATEMENT_TYPE;

    public ItemStatementMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                NAME,
                ObserverMessage.RESET_STATEMENT_TYPE,
                ObserverMessage.ADD_STATEMENT_TYPE,
                ObserverMessage.REMOVE_STATEMENT_TYPE,
                ObserverMessage.LIST_STATEMENT_TYPE
        );
    }

    public ItemStatementMap(ItemStatementMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    // type+name not initialized yet! - it call as Super in New
    protected void openMap() {
        //OPEN MAP
        map = database.createTreeMap(NAME)
                .valueSerializer(new ItemSerializer(TYPE))
                .makeOrGet();
    }

}
