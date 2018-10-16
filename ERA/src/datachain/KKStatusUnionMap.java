package datachain;

import org.mapdb.DB;
import utils.ObserverMessage;

/**
 * Назначает статус для объединения. Использует схему карты Ключ + Ключ - Значение: KK_Map,
 * в котрой по ключу ищем значение там карта по ключу еще и
 * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

 * @return dcMap
 */
public class KKStatusUnionMap extends KK_Map {
    public KKStatusUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "status_union",
                ObserverMessage.RESET_STATUS_UNION_TYPE,
                ObserverMessage.ADD_STATUS_UNION_TYPE,
                ObserverMessage.REMOVE_STATUS_UNION_TYPE,
                ObserverMessage.LIST_STATUS_UNION_TYPE
        );
    }

    public KKStatusUnionMap(KKStatusUnionMap parent) {
        super(parent);
    }

}
