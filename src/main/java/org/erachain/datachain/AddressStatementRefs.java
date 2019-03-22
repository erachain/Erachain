package org.erachain.datachain;


import org.mapdb.DB;
import org.erachain.utils.ObserverMessage;

/**
 * Хранит для этого адреса и времени создания ссылки на транзакции типа Statement, см. супер класс
 * @return
 */

public class AddressStatementRefs extends AddressItemRefs {
    static final String NAME = "statement";

    public AddressStatementRefs(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                NAME,
                ObserverMessage.RESET_STATEMENT_TYPE,
                ObserverMessage.ADD_STATEMENT_TYPE,
                ObserverMessage.REMOVE_STATEMENT_TYPE,
                ObserverMessage.LIST_STATEMENT_TYPE
        );
    }

    public AddressStatementRefs(AddressStatementRefs parent) {
        super(parent);
    }

}
