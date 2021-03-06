package org.erachain.datachain;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Назначает статус для объединения. Использует схему карты Ключ + Ключ - Значение: KKMap,
 * в котрой по ключу ищем значение там карта по ключу еще и
 * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

 * @return dcMap
 */
public class KKStatusUnionMap extends KKMap {
    public KKStatusUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "status_union",
                ObserverMessage.RESET_STATUS_UNION_TYPE,
                ObserverMessage.ADD_STATUS_UNION_TYPE,
                ObserverMessage.REMOVE_STATUS_UNION_TYPE,
                ObserverMessage.LIST_STATUS_UNION_TYPE
        );
    }

    public KKStatusUnionMap(KKStatusUnionMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
