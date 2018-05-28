package datachain;

import org.mapdb.DB;
import utils.ObserverMessage;

//import database.serializer.AssetSerializer;

// income, balance, 
public class UnionAssetsBalance extends _BalanceMap {
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

    public UnionAssetsBalance(UnionAssetsBalance parent) {
        super(parent);
    }

}
