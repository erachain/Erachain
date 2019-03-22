package org.erachain.datachain;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Назначает актив для объединения. Использует схему карты Ключ + Ключ - Значение: KKMap,
 * в котрой по ключу ищем значение там карта по ключу еще и
 * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

 * @return dcMap
 */
public class KKAssetUnionMap extends KKMap {
    public KKAssetUnionMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "asset_union",
                ObserverMessage.RESET_ASSET_UNION_TYPE,
                ObserverMessage.ADD_ASSET_UNION_TYPE,
                ObserverMessage.REMOVE_ASSET_UNION_TYPE,
                ObserverMessage.LIST_ASSET_UNION_TYPE
        );
    }

    public KKAssetUnionMap(KKAssetUnionMap parent) {
        super(parent);
    }

}
