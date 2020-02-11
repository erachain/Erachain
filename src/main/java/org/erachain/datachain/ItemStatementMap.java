package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Представление<br>
 */
public class ItemStatementMap extends ItemMap {

    public ItemStatementMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                ItemCls.STATEMENT_TYPE, ObserverMessage.RESET_STATEMENT_TYPE,
                ObserverMessage.ADD_STATEMENT_TYPE,
                ObserverMessage.REMOVE_STATEMENT_TYPE,
                ObserverMessage.LIST_STATEMENT_TYPE
        );
    }

    public ItemStatementMap(ItemStatementMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
