package org.erachain.datachain;


import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Person has Status of Union - person Ermolaev get Director status in Polza union
 смотри KK_K_Map

 Здесь Для Персоны Объединения задается Статус
 * Назначает статус для объединения. Использует схему карты Ключ + Ключ - Значение: KK_К_Map,
 * в котрой по ключу ищем значение там еще карта по ключу.
 * Результат это Стэк из значений Конец, Номер Блока, подпись транзакции
 */
public class KK_KPersonStatusUnionMap extends KK_K_Map {

    public KK_KPersonStatusUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "person_status_union",
                ObserverMessage.RESET_PERSON_STATUS_UNION_TYPE,
                ObserverMessage.ADD_PERSON_STATUS_UNION_TYPE,
                ObserverMessage.REMOVE_PERSON_STATUS_UNION_TYPE,
                ObserverMessage.LIST_PERSON_STATUS_UNION_TYPE
        );
    }

    public KK_KPersonStatusUnionMap(KK_KPersonStatusUnionMap parent) {
        super(parent);
    }

}
