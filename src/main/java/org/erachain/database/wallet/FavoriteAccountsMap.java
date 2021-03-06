package org.erachain.database.wallet;

import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DCUMapImpl;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

/**
 * Мои избранные счета - для телеграмм например
 * <p>
 * key: address -> value: pubKey, name + JSON("description": "...")
 */
public class FavoriteAccountsMap extends DCUMapImpl<String, Tuple3<String, String, String>> {
    static Logger LOGGER = LoggerFactory.getLogger(FavoriteAccountsMap.class.getName());

    public FavoriteAccountsMap(DWSet dWSet, DB database) {
        super(dWSet, database);

        if (databaseSet.isWithObserver()) {

            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.WALLET_ACCOUNT_FAVORITE_LIST);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.WALLET_ACCOUNT_FAVORITE_ADD);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.WALLET_ACCOUNT_FAVORITE_DELETE);
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.WALLET_ACCOUNT_FAVORITE_RESET);
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
        map = new TreeMap<String, Tuple3<String, String, String>>(Fun.COMPARATOR);
    }

    public static Account detPublicKeyOrAccount(String key, Tuple3<String, String, String> item) {
        if (item.a == null) {
            return new Account(key);
        } else {
            return new PublicKeyAccount(item.a);
        }
    }

    @Override
    public void put(String key, Tuple3<String, String, String> value) {
        super.put(key, value);
        ((DWSet) databaseSet).hardFlush();
    }

    @Override
    public void delete(String key) {
        super.delete(key);
        ((DWSet) databaseSet).hardFlush();
    }

}
