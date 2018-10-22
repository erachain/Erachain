package org.erachain.datachain;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Назначает статус для актива. Использует схему карты Ключ + Ключ - Значение: KK_Map,
 * в котрой по ключу ищем значение там карта по ключу еще и
 * результат это Стэк из значений Начало, Конец, Данные, Ссылка на запись

 * @return dcMap
 */
public class KKAssetStatusMap extends KK_Map {
    public KKAssetStatusMap(DCSet databaseSet, DB database) {
        super(databaseSet, database, "asset_status",
                ObserverMessage.RESET_ASSET_STATUS_TYPE,
                ObserverMessage.ADD_ASSET_STATUS_TYPE,
                ObserverMessage.REMOVE_ASSET_STATUS_TYPE,
                ObserverMessage.LIST_ASSET_STATUS_TYPE
        );
    }

    public KKAssetStatusMap(KKAssetStatusMap parent) {
        super(parent);
    }


}
