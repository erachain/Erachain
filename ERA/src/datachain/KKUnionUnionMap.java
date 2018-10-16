package datachain;

import org.mapdb.DB;
import utils.ObserverMessage;

/**
 * Назначает объединение для объединения. Использует схему карты Ключ + Ключ - Значение: KK_Map,
 * в котрой по ключу ищем значение там карта по ключу еще и
 * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись
 *
 // Union parent has unions
 // TODO - insert in DBSet
 * @return dcMap
 */
public class KKUnionUnionMap extends KK_Map {

    public KKUnionUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "union_union",
                ObserverMessage.RESET_UNION_UNION_TYPE,
                ObserverMessage.ADD_UNION_UNION_TYPE,
                ObserverMessage.REMOVE_UNION_UNION_TYPE,
                ObserverMessage.LIST_UNION_UNION_TYPE
        );
    }

    public KKUnionUnionMap(KKUnionUnionMap parent) {
        super(parent);
    }

}
