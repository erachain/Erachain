package org.erachain.datachain;

import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Балансы для объединений - пока не используется - на будущее
 *
 * Ключ: Номер Объединания + Номер Актива
 * Значение: балансы
 */
public class UnionAssetsBalance extends BalanceMap {
    static final String NAME = "union";

    public UnionAssetsBalance(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                NAME,
                ObserverMessage.RESET_BALANCE_TYPE,
                ObserverMessage.ADD_BALANCE_TYPE,
                ObserverMessage.REMOVE_BALANCE_TYPE,
                ObserverMessage.LIST_BALANCE_TYPE
        );
    }

    public UnionAssetsBalance(UnionAssetsBalance parent, DCSet dcSet) {
        super(parent, dcSet);
    }

}
