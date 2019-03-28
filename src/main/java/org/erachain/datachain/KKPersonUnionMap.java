package org.erachain.datachain;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Назначает персон для объединения. Использует схему карты Ключ + Ключ - Значение: KKMap,
 * в котрой по ключу ищем значение там карта по ключу еще и
 * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

 * @return dcMap
 */
public class KKPersonUnionMap extends KKMap {

    public KKPersonUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "person_union",
                ObserverMessage.RESET_PERSON_UNION_TYPE,
                ObserverMessage.ADD_PERSON_UNION_TYPE,
                ObserverMessage.REMOVE_PERSON_UNION_TYPE,
                ObserverMessage.LIST_PERSON_UNION_TYPE
        );

    }

    public KKPersonUnionMap(KKPersonUnionMap parent) {
        super(parent);
    }
}
