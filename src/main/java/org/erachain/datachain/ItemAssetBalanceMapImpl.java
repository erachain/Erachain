package org.erachain.datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.PagedIndexMap;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDB;
import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDBFork;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.ItemAssetBalanceSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.erachain.database.IDB.DBS_MAP_DB;
import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Балансы для заданного адреса на данный актив. balances for all account in blockchain<br>
 * <b>Список балансов:</b> имущество, займы, хранение, производство, резерв<br>
 * Каждый баланс: Всего Пришло и Остаток<br><br>
 *
 * <b>Ключ:</b> account.address.short[20] + asset key[8]<br>
 *
 * <b>Значение:</b> Балансы. in_OWN, in_RENT, on_HOLD = in_USE (TOTAL on HAND)
 */
// TODO SOFT HARD TRUE
@Slf4j
public class ItemAssetBalanceMapImpl extends DBTabImpl<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Tuple2<BigDecimal, BigDecimal>, // it DO
        Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >> implements ItemAssetBalanceMap {

    static final boolean SIZE_ENABLE = false;

    public ItemAssetBalanceMapImpl(int dbsUsed, DCSet databaseSet, DB database) {
        super(dbsUsed, databaseSet, database);

        if (databaseSet.isWithObserver()) {
            this.observableData.put(DBTab.NOTIFY_RESET, ObserverMessage.RESET_BALANCE_TYPE);
            this.observableData.put(DBTab.NOTIFY_LIST, ObserverMessage.LIST_BALANCE_TYPE);
            this.observableData.put(DBTab.NOTIFY_ADD, ObserverMessage.ADD_BALANCE_TYPE);
            this.observableData.put(DBTab.NOTIFY_REMOVE, ObserverMessage.REMOVE_BALANCE_TYPE);
        }
    }

    public ItemAssetBalanceMapImpl(int dbsUsed, ItemAssetBalanceMap parent, DCSet databaseSet) {
        super(dbsUsed, parent, databaseSet);
    }

    // TODO вставить настройки выбора СУБД
    @Override
    public void openMap() {


        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new ItemAssetBalanceSuitRocksDB(databaseSet, database, this);
                    break;
                default:
                    map = new ItemAssetBalanceSuitMapDB(databaseSet, database, this);
            }
        } else {
            switch (dbsUsed) {
                case DBS_MAP_DB:
                case DBS_ROCK_DB:
                    map = new ItemAssetBalanceSuitMapDBFork((ItemAssetBalanceMap) parent, databaseSet, this);
                    break;
                default: {
                    if (BlockChain.TEST_DB == 0)
                        // тут нужна обработка по списку держателей Актива
                        // ДЛЯ обработки множественных выплат нужна эта таблица а не в МЕМОКН - там нет нужных индексов
                        map = new ItemAssetBalanceSuitMapDBFork((ItemAssetBalanceMap) parent, databaseSet, this);
                    else
                        map = new NativeMapTreeMapFork(parent, databaseSet, Fun.BYTE_ARRAY_COMPARATOR, this);
                }
            }
        }
    }


    @Override
    public Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> getDefaultValue(byte[] key) {

        BigDecimal initialAmount = BigDecimal.ZERO;
        if (BlockChain.ERA_COMPU_ALL_UP) {
            long assetKey = ItemAssetBalanceMap.getAssetKeyFromKey(key);
            if (assetKey == AssetCls.ERA_KEY)
                initialAmount = BigDecimal.valueOf(BlockChain.GENESIS_ERA_TOTAL / 1000 * (5000 + key[10]) / 5000);

            else if (assetKey == AssetCls.FEE_KEY)
                initialAmount = new BigDecimal("100.0");

            else if (BlockChain.isNovaAsset(assetKey)) {
                initialAmount = new BigDecimal("1000.0");
            }
        }

        return new Fun.Tuple5<
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
                Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
                (new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, initialAmount),
                        new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Fun.Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO));
    }

    public boolean contains(byte[] address, long key) {
        if (key < 0)
            key = -key;

        return this.contains(Bytes.concat(address, Longs.toByteArray(key)));
    }

    public void put(byte[] address, long key, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {
        if (key < 0)
            key = -key;

        this.put(Bytes.concat(address, Longs.toByteArray(key)), value);
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

    /***
     * @param assetKey KEY for balance found + found balance
     * @return
     */
    public List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> getBalancesList(long assetKey) {

        if (Controller.getInstance().onlyProtocolIndexing || parent != null)
            return null;

        if (assetKey < 0)
            assetKey = -assetKey;

        List<Tuple2<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
                list = new ArrayList<>();

        byte[] key;
        try (IteratorCloseable<byte[]> iterator = ((ItemAssetBalanceSuit) map).getIteratorByAsset(assetKey)) {
            while (iterator.hasNext()) {
                key = iterator.next();
                list.add(new Tuple2<>(key, map.get(key)));
            }
        } catch (IOException e) {
        }

        return list;
    }

    /**
     * @param account KEY for balance found + found balance
     * @return
     */
    public List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> getBalancesList(Account account) {

        List<Tuple2<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
                list = new ArrayList<>();

        byte[] key;
        try (IteratorCloseable<byte[]> iterator = ((ItemAssetBalanceSuit) map).accountIterator(account)) {
            if (iterator == null)
                return list;

            while (iterator.hasNext()) {
                key = iterator.next();
                list.add(new Tuple2<>(key, map.get(key)));
            }
        } catch (IOException e) {
        }

        return list;
    }

    public IteratorCloseable<byte[]> getIteratorByAccount(Account account) {

        if (Controller.getInstance().onlyProtocolIndexing || parent != null)
            return null;

        return ((ItemAssetBalanceSuit) map).accountIterator(account);

    }

    /**
     *
     * @param assetKey
     * @return
     */
    public IteratorCloseable<byte[]> getIteratorByAsset(long assetKey) {

        if (assetKey < 0)
            assetKey = -assetKey;

        return ((ItemAssetBalanceSuit) map).getIteratorByAsset(assetKey);

    }

    public class PagedOwners extends PagedIndexMap<byte[],
            Tuple3<Long, BigDecimal, byte[]>,
            Tuple2<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> {

        public PagedOwners(DBTabImpl mapImpl) {
            super(mapImpl);
        }

        @Override
        public Tuple2<byte[],
                Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
        get(byte[] key) {

            return new Tuple2(ItemAssetBalanceMap.getShortAccountFromKey(key), mapImpl.get(key));
        }

        @Override
        public boolean equalsRow(Tuple2<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> pageRow,
                                 Tuple2<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> row) {
            return Arrays.equals(pageRow.a, row.a)
                    && pageRow.b.equals(row.b);

        }

        @Override
        public boolean equalsKey(byte[] pageKey, byte[] key) {
            return Arrays.equals(pageKey, key);
        }

        @Override
        public Tuple3<Long, BigDecimal, byte[]> makeSecondaryKey(byte[] key,
                                                                 Tuple2<byte[], Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> value) {

            return new Tuple3<>(ItemAssetBalanceMap.getAssetKeyFromKey(key), value.b.a.b, ItemAssetBalanceMap.getShortAccountFromKey(key));
        }

        @Override
        public IteratorCloseable<byte[]> getIterator(Tuple3<Long, BigDecimal, byte[]> fromSecondaryKey, boolean descending) {
            return ((ItemAssetBalanceSuit) map).getIteratorByAsset(fromSecondaryKey.a, fromSecondaryKey.b, fromSecondaryKey.c, descending);
        }

    }

    /**
     * page of Short Address + Balances .. start & end ownAmount for keys
     */
    public List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
    getOwnersPage(Long assetKey, BigDecimal fromOwnAmount, byte[] fromAddres, int offset, int limit, boolean fillFullPage) {

        if (parent != null) {
            if (Controller.getInstance().onlyProtocolIndexing) {
                return null;
            }
            throw new RuntimeException("In FORK DCSet not implimented!");
        }

        PagedOwners pager = new PagedOwners(this);

        return pager.getPageList(new Tuple3<>(assetKey, fromOwnAmount, fromAddres), offset, limit, fillFullPage);

    }

}
