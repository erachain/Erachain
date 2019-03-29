package org.erachain.datachain;


import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Person has Status of Union - person Ermolaev get Director status in Polza union
 смотри KKKMap

 Здесь Для Персоны Объединения задается Статус
 * Назначает статус для объединения. Использует схему карты Ключ + Ключ - Значение: KKKMap,
 * в котрой по ключу ищем значение там еще карта по ключу.
 * Результат это Стэк из значений Конец, Номер Блока, подпись транзакции
 */
public class KKKMapPersonStatusUnion extends KKKMap {

    public KKKMapPersonStatusUnion(DCSet databaseSet, DB database) {
        super(databaseSet, database, "person_status_union",
                ObserverMessage.RESET_PERSON_STATUS_UNION_TYPE,
                ObserverMessage.ADD_PERSON_STATUS_UNION_TYPE,
                ObserverMessage.REMOVE_PERSON_STATUS_UNION_TYPE,
                ObserverMessage.LIST_PERSON_STATUS_UNION_TYPE
        );
    }

    public KKKMapPersonStatusUnion(KKKMapPersonStatusUnion parent) {
        super(parent);
    }

}
