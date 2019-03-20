package org.erachain.datachain;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Назначает статус для актива. Использует схему карты Ключ + Ключ - Значение: KKMap,
 * в котрой по ключу ищем значение там карта по ключу еще и
 * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

 * @return dcMap
 */
public class KKUnionStatusMap extends KKMap {

    public KKUnionStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "union_status",
                ObserverMessage.RESET_UNION_STATUS_TYPE,
                ObserverMessage.ADD_UNION_STATUS_TYPE,
                ObserverMessage.REMOVE_UNION_STATUS_TYPE,
                ObserverMessage.LIST_UNION_STATUS_TYPE
        );
    }

    public KKUnionStatusMap(KKUnionStatusMap parent) {
        super(parent);
    }

}
