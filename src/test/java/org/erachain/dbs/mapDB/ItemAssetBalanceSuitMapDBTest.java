package org.erachain.dbs.mapDB;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.erachain.core.account.Account;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetBalanceMap;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.dbs.IteratorCloseable;
import org.junit.Test;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ItemAssetBalanceSuitMapDBTest {

    DCSet dcSet;
    ItemAssetBalanceMap balsMap;
    long assetKey = 10;

    byte[] accountShort1 = new byte[Account.ADDRESS_SHORT_LENGTH];
    byte[] accountShort2 = new byte[Account.ADDRESS_SHORT_LENGTH];
    byte[] accountShort3 = new byte[Account.ADDRESS_SHORT_LENGTH];

    byte[] key;
    Fun.Tuple5<
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>,
            Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>> balance;

    Fun.Tuple2<Fun.Tuple2<Long, BigDecimal>, byte[]> fromSecondaryKey;

    IteratorCloseable<byte[]> iterator;

    private void init() {

        Random rand = new Random();
        rand.nextBytes(accountShort1);
        rand.nextBytes(accountShort2);
        rand.nextBytes(accountShort3);

        dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);

        balsMap = dcSet.getAssetBalanceMap();


    }

    @Test
    public void getIteratorByAsset() {

        init();

        BigDecimal amo1 = new BigDecimal("12.23");
        BigDecimal amo2 = new BigDecimal("32.3");
        BigDecimal amo3 = new BigDecimal("0.31");

        balance = new Fun.Tuple5<>(new Fun.Tuple2<>(BigDecimal.ZERO, amo1),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO));
        balsMap.put(Bytes.concat(accountShort1, Longs.toByteArray(assetKey)), balance);

        balance = new Fun.Tuple5<>(new Fun.Tuple2<>(BigDecimal.ZERO, amo2),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO));
        balsMap.put(Bytes.concat(accountShort2, Longs.toByteArray(assetKey)), balance);

        balance = new Fun.Tuple5<>(new Fun.Tuple2<>(BigDecimal.ZERO, amo3),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO));
        balsMap.put(Bytes.concat(accountShort3, Longs.toByteArray(assetKey)), balance);

        //////////////////////
        balance = new Fun.Tuple5<>(new Fun.Tuple2<>(BigDecimal.ZERO, amo3),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO));
        balsMap.put(Bytes.concat(accountShort2, Longs.toByteArray(assetKey + 1)), balance);

        balance = new Fun.Tuple5<>(new Fun.Tuple2<>(BigDecimal.ZERO, amo2),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO));
        balsMap.put(Bytes.concat(accountShort3, Longs.toByteArray(assetKey + 1)), balance);

        balance = new Fun.Tuple5<>(new Fun.Tuple2<>(BigDecimal.ZERO, amo1),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO));
        balsMap.put(Bytes.concat(accountShort2, Longs.toByteArray(assetKey - 1)), balance);

        balance = new Fun.Tuple5<>(new Fun.Tuple2<>(BigDecimal.ZERO, amo3),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO),
                new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO), new Fun.Tuple2<>(BigDecimal.ZERO, BigDecimal.ZERO));
        balsMap.put(Bytes.concat(accountShort3, Longs.toByteArray(assetKey - 1)), balance);

        boolean desc = false;
        iterator = ((ItemAssetBalanceSuit) balsMap.getSuit()).getIteratorByAsset(assetKey, amo2, null, desc);
        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo2);

        assertEquals(iterator.hasNext(), false);

        iterator = ((ItemAssetBalanceSuit) balsMap.getSuit()).getIteratorByAsset(assetKey, amo2, accountShort2, desc);
        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo2);

        assertEquals(iterator.hasNext(), false);

        iterator = ((ItemAssetBalanceSuit) balsMap.getSuit()).getIteratorByAsset(assetKey, amo1, null, desc);
        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo1);

        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo2);

        assertEquals(iterator.hasNext(), false);

        iterator = ((ItemAssetBalanceSuit) balsMap.getSuit()).getIteratorByAsset(assetKey, amo1, accountShort1, desc);
        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo1);

        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo2);

        assertEquals(iterator.hasNext(), false);

        /////////////////////
        desc = !desc;
        iterator = ((ItemAssetBalanceSuit) balsMap.getSuit()).getIteratorByAsset(assetKey, amo3, null, desc);
        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo3);

        assertEquals(iterator.hasNext(), false);

        iterator = ((ItemAssetBalanceSuit) balsMap.getSuit()).getIteratorByAsset(assetKey, amo3, accountShort3, desc);
        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo3);

        assertEquals(iterator.hasNext(), false);

        iterator = ((ItemAssetBalanceSuit) balsMap.getSuit()).getIteratorByAsset(assetKey, amo1, null, desc);
        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo1);

        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo3);

        assertEquals(iterator.hasNext(), false);

        iterator = ((ItemAssetBalanceSuit) balsMap.getSuit()).getIteratorByAsset(assetKey, amo1, accountShort1, desc);
        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo1);

        key = iterator.next();
        balance = balsMap.get(key);
        assertEquals(balance.a.b, amo3);

        assertEquals(iterator.hasNext(), false);

    }

}