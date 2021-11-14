package org.erachain.datachain;

import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.IteratorCloseable;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;

import java.math.BigDecimal;
import java.util.List;
import java.util.Observer;

/**
 * Interface for MAP + static methods
 * addressShort[20] + assetKey[8] - > balance
 */
public interface ItemAssetBalanceMap extends DBTab<byte[], Tuple5<
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

    boolean contains(byte[] address, long key);

    void put(byte[] address, long key, Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value);

    Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> get(byte[] address, long key);

    List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> getBalancesList(long key);

    List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>> getBalancesList(Account account);

    IteratorCloseable<byte[]> getIteratorByAsset(long assetKey);

    IteratorCloseable<byte[]> getIteratorByAccount(Account account);

    List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
    getOwnersPage(long assetKey, BigDecimal fromOwnAmount, int offset, int limit, boolean fillFullPage);

    List<Tuple2<byte[], Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>>
    getOwnersPage(byte[] fromKey, int offset, int limit, boolean fillFullPage);

    void addObserver(Observer o);

}
