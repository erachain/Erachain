package datachain;

import org.mapdb.DB;
import utils.ObserverMessage;

/**
 * Назначает голосования для объединения. Использует схему карты Ключ + Ключ - Значение: KK_Map,
 * в котрой по ключу ищем значение там карта по ключу еще и
 * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

 * @return dcMap
 */
public class KKPollUnionMap extends KK_Map {
    public KKPollUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "status_union",
                ObserverMessage.RESET_POLL_UNION_TYPE,
                ObserverMessage.ADD_POLL_UNION_TYPE,
                ObserverMessage.REMOVE_POLL_UNION_TYPE,
                ObserverMessage.LIST_POLL_UNION_TYPE
        );
    }

    public KKPollUnionMap(KKPollUnionMap parent) {
        super(parent);
    }

}
