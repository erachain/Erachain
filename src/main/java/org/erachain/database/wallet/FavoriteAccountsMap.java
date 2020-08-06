package org.erachain.database.wallet;

import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * Мои избранные счета - для телеграмм например
 * <p>
 * key: address -> value: name + description
 */
public class FavoriteAccountsMap extends DCUMapImpl<String, Tuple2<String, String>> {
    static Logger LOGGER = LoggerFactory.getLogger(FavoriteAccountsMap.class.getName());

    public FavoriteAccountsMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {

            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.WALLET_ACCOUNT_PROPERTIES_LIST);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.WALLET_ACCOUNT_PROPERTIES_ADD);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.WALLET_ACCOUNT_PROPERTIES_DELETE);
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.WALLET_ACCOUNT_PROPERTIES_RESET);
        }
    }

    @Override
    public void openMap() {
        // OPEN MAP
        map = database.createTreeMap("favorite_accounts_map")
                .counterEnable()
                .makeOrGet();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void getMemoryMap() {
        map = new TreeMap<String, Tuple2<String, String>>(Fun.COMPARATOR);
    }

    @Override
    public void put(String key, Tuple2<String, String> value) {
        super.put(key, value);
        ((DWSet) databaseSet).hardFlush();
    }

    @Override
    public void delete(String key) {
        super.delete(key);
        ((DWSet) databaseSet).hardFlush();
    }

}
