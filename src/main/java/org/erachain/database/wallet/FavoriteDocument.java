package org.erachain.database.wallet;

import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;


public class FavoriteDocument extends FavoriteItemMap {

    public FavoriteDocument(DWSet dWSet, DB database) {
        super(dWSet, database, ObserverMessage.ADD_STATEMENT_FAVORITES_TYPE, "statement", 0);

    }

}

