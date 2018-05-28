package datachain;

import org.mapdb.DB;
import utils.ObserverMessage;

//import database.serializer.AssetSerializer;

// income, balance, 
public class PersonAssetsBalance extends _BalanceMap {
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
