package org.erachain.datachain;

import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.database.SortableList;
import org.erachain.dbs.DBMapSuit;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.mapDB.ItemAssetBalanceSuitMapDB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Observer;
import java.util.Set;

public interface ItemAssetBalanceTab extends DBTab<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Tuple2<BigDecimal, BigDecimal>, // it DO
        Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >> {

    static long getAssetKeyFromKey(byte[] key) {
        // ASSET KEY
        byte[] assetKeyBytes = new byte[8];
        System.arraycopy(key, 20, assetKeyBytes, 0, 8);
        return Longs.fromByteArray(assetKeyBytes);
    }

    static byte[] getShortAccountFromKey(byte[] key) {
        // ASSET KEY
        byte[] shortAddressBytes = new byte[20];
        System.arraycopy(key, 0, shortAddressBytes, 0, 20);
        return shortAddressBytes;

    }

    void set(byte[] address, long key, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value);

    Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> get(byte[] key);

    Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> get(byte[] address, long key);

    SortableList<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(long key);

    SortableList<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> getBalancesSortableList(Account account);

    Set<byte[]> keySet();

    Iterator<byte[]> getIterator(int index, boolean descending);

    void reset();

    void addObserver(Observer o);

    ItemAssetBalanceSuit getMapSuit();

    }
