package org.erachain.datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.database.SortableList;
import org.erachain.dbs.DBMapSuit;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDB;
import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDBFork;
import org.erachain.dbs.nativeMemMap.nativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.ItemAssetBalanceSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Hasher работает неверно! и вообще там 32 битное число 0 INTEGER - чего нифига не хватает!
 *
 * (пока не используется - по идее для бухгалтерских единиц отдельная таблица)
 * Балансы для заданного адреса на данный актив. balances for all account in blockchain<br>
 * <b>Список балансов:</b> имущество, займы, хранение, производство, резерв<br>
 * Каждый баланс: Всего Пришло и Остаток<br><br>
 *
 * <b>Ключ:</b> account.address + asset key<br>
 *
 * <b>Значение:</b> Балансы. in_OWN, in_RENT, on_HOLD = in_USE (TOTAL on HAND)
 *
 */
// TODO SOFT HARD TRUE

public class ItemAssetBalanceTabImpl extends DBTabImpl<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Tuple2<BigDecimal, BigDecimal>, // it DO
        Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >> implements ItemAssetBalanceTab {

    int ASSET_AMOUNT_INDEX = 1;
    int ADDRESS_ASSET_INDEX = 2;

    final static String dbSuitMain = "RocksDB";
    final static String dbSuitFork = "mem";

    public ItemAssetBalanceTabImpl(DCSet databaseSet, DB database) {
        super(databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_BALANCE_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_BALANCE_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_BALANCE_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_BALANCE_TYPE);
        }
    }

    public ItemAssetBalanceTabImpl(ItemAssetBalanceTab parent, DCSet databaseSet) {
        super(parent, databaseSet);
    }

    // TODO вставить настройки выбора СУБД
    @Override
    protected void getMap()
    {
        if (parent == null) {
            if (dbSuitMain.equals("MapDB"))
                map = new ItemAssetBalanceSuitMapDB(databaseSet, database);
            else if (dbSuitMain.equals("RocksDB"))
                map = new ItemAssetBalanceSuitRocksDB(databaseSet, database);
            else
                map = new ItemAssetBalanceSuitMapDB(databaseSet, database);
        } else {
            if (dbSuitFork.equals("MapDB"))
                map = new ItemAssetBalanceSuitMapDBFork((ItemAssetBalanceTab)parent, databaseSet);
            else if (dbSuitFork.equals("RocksDB"))
                map = new ItemAssetBalanceSuitRocksDB(databaseSet, database);
            else
                map = new nativeMapTreeMapFork(parent, databaseSet, ItemAssetBalanceSuit.DEFAULT_VALUE);

        }
    }

    public ItemAssetBalanceSuit getMapSuit() {
        return (ItemAssetBalanceSuit)map;
    }

    public void set(byte[] address, long key, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
        if (key < 0)
            key = -key;

        this.set(Bytes.concat(address, Longs.toByteArray(key)), value);
    }

    public boolean set(byte[] key, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

        boolean result = super.set(key, value);

        return result;

    }

    public Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> get(byte[] address, long key) {
        if (key < 0)
            key = -key;


        Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value = this.get(
                        Bytes.concat(address, Longs.toByteArray(key)));

        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(long assetKey) {

        if (Controller.getInstance().onlyProtocolIndexing)
            return null;

        if (assetKey < 0)
            assetKey = -assetKey;

        Collection<byte[]> keys;
        if (map instanceof ItemAssetBalanceSuitRocksDB) {
            //FILTER ALL KEYS
            keys = new ArrayList<>();
            Iterator<byte[]> iterator = ((ItemAssetBalanceSuit) map).assetIterator(assetKey);
            while (iterator.hasNext()) {
                keys.add(iterator.next());
            }
        } else {
            keys = ((ItemAssetBalanceSuit)map).assetKeys(assetKey);
        }

        //RETURN
        return new SortableList<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>(this, keys);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortableList<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(Account account) {

        if (Controller.getInstance().onlyProtocolIndexing)
            return null;

        Collection<byte[]> keys;
        if (map instanceof ItemAssetBalanceSuitRocksDB) {
            //FILTER ALL KEYS
            keys = new ArrayList<>();
            Iterator<byte[]> iterator = ((ItemAssetBalanceSuit) map).accountIterator(account);
            while (iterator.hasNext()) {
                keys.add(iterator.next());
            }
        } else {
            keys = ((ItemAssetBalanceSuit)map).accountKeys(account);
        }

        //RETURN
        return new SortableList<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>(this, keys);
    }

}
