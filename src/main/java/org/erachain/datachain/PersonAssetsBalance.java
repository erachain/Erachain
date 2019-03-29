package org.erachain.datachain;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

//import database.serializer.AssetSerializer;

/**
 * для хранения балансов по Персоне - на будущее
 * Ключ: Номер Перосны + Номер Актива
 * Значение: Балансы
 */
public class PersonAssetsBalance extends BalanceMap {
    static final String NAME = "person";

    public PersonAssetsBalance(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                NAME,
                ObserverMessage.RESET_BALANCE_TYPE,
                ObserverMessage.ADD_BALANCE_TYPE,
                ObserverMessage.REMOVE_BALANCE_TYPE,
                ObserverMessage.LIST_BALANCE_TYPE
        );
    }

    public PersonAssetsBalance(PersonAssetsBalance parent) {
        super(parent);
    }

}
