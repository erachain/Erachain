package org.erachain.datachain;


import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Назначает статус для персоны. Использует схему карты Ключ + Ключ - Значение: KK_Map,
 * в котрой по ключу ищем значение там карта по ключу еще и
 * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись.<br>
 *     <br>

    key: (Long)PERSON <br>
    Value:<br>
        TreeMap<(Long) STATUS
        Stack(Tuple5(
            (Long) beg_date,
            (Long)end_date,

            (byte[]) any additional data,

            Integer,
            Integer
        ))

     * @return dcMap
     */

public class KKPersonStatusMap extends KK_Map {

    public KKPersonStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "person_status",
                ObserverMessage.RESET_PERSON_STATUS_TYPE,
                ObserverMessage.ADD_PERSON_STATUS_TYPE,
                ObserverMessage.REMOVE_PERSON_STATUS_TYPE,
                ObserverMessage.LIST_PERSON_STATUS_TYPE
        );
    }

    public KKPersonStatusMap(KKPersonStatusMap parent) {
        super(parent);
    }

}
