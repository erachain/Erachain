package org.erachain.dbs.rocksDB;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockGenerator;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.database.DBASet;
import org.erachain.datachain.ItemAssetBalanceSuit;
import org.erachain.dbs.rocksDB.common.RocksDB;
import org.erachain.dbs.rocksDB.indexes.SimpleIndexDB;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.erachain.dbs.rocksDB.transformation.ByteableBigDecimal;
import org.erachain.dbs.rocksDB.transformation.ByteableBigInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableTrivial;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.*;

import static org.erachain.dbs.rocksDB.utils.ConstantsRocksDB.ROCKS_DB_FOLDER;

public class ItemAssetBalanceSuitRocksDB extends DBMapSuit<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
        Tuple2<BigDecimal, BigDecimal>, // in DEBT
        Tuple2<BigDecimal, BigDecimal>, // in STOCK
        Tuple2<BigDecimal, BigDecimal>, // it DO
        Tuple2<BigDecimal, BigDecimal>  // on HOLD
        >>
            implements ItemAssetBalanceSuit {

    private final String NAME_TABLE = "ITEM_ASSET_BALANCE_TABLE";
    private final String balanceKeyAssetIndexName = "balances_by_asset";
    private final String balanceAddressIndexName = "balances_by_address";
    private SimpleIndexDB<
            byte[],
            Tuple5<
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>>,
            byte[]> balanceKeyAssetIndex;

    private SimpleIndexDB<
            byte[],
            Tuple5<
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>>,
            byte[]> balanceAddressIndex;

    public ItemAssetBalanceSuitRocksDB(DBASet databaseSet, DB database) {
        super(databaseSet, database);
    }

    //private final ByteableBigDecimal byteableBigDecimal = new ByteableBigDecimal();
    private final ByteableBigInteger byteableBigInteger = new ByteableBigInteger();

    @Override
    protected void getMap() {

        map = new DBRocksDBTable<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, // in OWN - total INCOMED + BALANCE
                Tuple2<BigDecimal, BigDecimal>, // in DEBT
                Tuple2<BigDecimal, BigDecimal>, // in STOCK
                Tuple2<BigDecimal, BigDecimal>, // it DO
                Tuple2<BigDecimal, BigDecimal>  // on HOLD
                >>(
                new ByteableTrivial(),
                new org.erachain.dbs.rocksDB.transformation.differentLength.ByteableTuple5Tuples2BigDecimal(), NAME_TABLE, indexes,
                org.erachain.dbs.rocksDB.common.RocksDbSettings.initCustomSettings(7, 64, 32,
                        256, 128,
                        1, 256, 32, false),
                ROCKS_DB_FOLDER);

        if (databaseSet != null)
            databaseSet.addExternalMaps(this);

    }

    @Override
    public void createIndexes() {

        balanceKeyAssetIndex = new SimpleIndexDB<>(balanceKeyAssetIndexName,
                (key, value) -> {
                    // Address
                    byte[] shortAddress = new byte[20];
                    System.arraycopy(key, 0, shortAddress, 0, 20);
                    // ASSET KEY
                    byte[] assetKeyBytes = new byte[8];
                    System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                    int sign = value.a.b.signum();
                    // берем абсолютное значение
                    BigDecimal shiftForSortBG = value.a.b;//.setScale(TransactionAmount.maxSCALE);
                    BigInteger shiftForSortBI = shiftForSortBG.unscaledValue();
                    //shiftForSortBI = new BigInteger("-1").subtract(shiftForSortBI);
                    byte[] shiftForSortOrig = shiftForSortBI.toByteArray();
                    byte[] shiftForSortBuff = new byte[10];
                    System.arraycopy(shiftForSortOrig, 0, shiftForSortBuff,
                            shiftForSortBuff.length - shiftForSortOrig.length, shiftForSortOrig.length);

                    if (sign >= 0) {
                        // учтем знак числа
                       // shiftForSortBuff[0] = (byte)(-1 + shiftForSortBuff[0]);
                        // сковертируем
                        for (int i = 3; i < shiftForSortBuff.length; i++) {
                            //shiftForSortBuff[i-1] = shiftForSortBuff[i];
                        }
                    } else {
                        // учтем знак числа
                        //shiftForSortBuff[0] = (byte)(0 - shiftForSortBuff[0]);
                        // сковертируем
                        //for (int i = 0; i < shiftForSortBuff.length; i++) {
                        //    //shiftForSortBuff[i] = (byte)(0 + shiftForSortBuff[i]);
                        //}
                    }

                    return org.bouncycastle.util.Arrays.concatenate(
                            assetKeyBytes,
                            shiftForSortBuff
                            //shortAddress - он уже есть в главном ключе
                    );
                },
                (result, key) -> result);

        balanceAddressIndex = new SimpleIndexDB<>(balanceAddressIndexName,
                (key, value) -> {
                    // Address
                    byte[] shortAddress = new byte[20];
                    System.arraycopy(key, 0, shortAddress, 0, 20);
                    // ASSET KEY
                    byte[] assetKeyBytes = new byte[8];
                    System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                    return org.bouncycastle.util.Arrays.concatenate(
                            shortAddress,
                            assetKeyBytes);
                },
                (result, key) -> result); // ByteableTrivial

        indexes = new ArrayList<>();
        indexes.add(balanceKeyAssetIndex);
        indexes.add(balanceAddressIndex);
    }

    @Override
    public Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public void reset() {
        databaseSet.close();
        File dbFile = new File(Paths.get(ROCKS_DB_FOLDER).toString(), NAME_TABLE);
        dbFile.delete();
    }


    // TODO - release it

    public List<byte[]> assetKeys_bad(long assetKey) {
        return ((DBRocksDBTable)map).filterAppropriateValuesAsKeys(
                Longs.toByteArray(assetKey),
                balanceKeyAssetIndex.getColumnFamilyHandle());
    }

    public List<byte[]> assetKeys(long assetKey) {
        return ((DBRocksDBTable)map).filterAppropriateValuesAsByteKeys(
                Longs.toByteArray(assetKey),
                balanceKeyAssetIndex.getColumnFamilyHandle());
    }


    @Override
    public Iterator<byte[]> assetIterator(long assetKey) {
        return assetKeys(assetKey).iterator();
    }

    public List<byte[]> accountKeys(Account account) {
        if (false) {
            return ((DBRocksDBTable) map).filterAppropriateValuesAsKeys(
                    account.getShortAddressBytes(),
                    balanceAddressIndex.getColumnFamilyHandle());
        } else {
            RocksIterator iter = ((DBRocksDBTable) map).db.db.database.newIterator(
                    balanceAddressIndex
                    //balanceKeyAssetIndex
                            .getColumnFamilyHandle());
            List<byte[]> result = new ArrayList<byte[]>();

            for (iter.seek(account.getShortAddressBytes()); iter.isValid() && new String(iter.key())
                    .startsWith(new String(account.getShortAddressBytes())); iter.next()) {
                byte[] key = iter.key();
                byte[] value = iter.value();
                result.add(iter.value());
            }
            return result;

        }
    }

    @Override
    public Iterator<byte[]> accountIterator(Account account) {
        return accountKeys(account).iterator();
    }

}
