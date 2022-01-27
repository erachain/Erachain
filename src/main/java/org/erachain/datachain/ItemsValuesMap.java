package org.erachain.datachain;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;
import org.erachain.core.item.ItemCls;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.dbs.MergedOR_IteratorsNoDuplicates;
import org.erachain.dbs.mapDB.ItemsValuesMapDB;
import org.erachain.dbs.nativeMemMap.NativeMapTreeMapFork;
import org.erachain.dbs.rocksDB.ItemsValuesRocksDB;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple3;

import static org.erachain.database.IDB.DBS_ROCK_DB;

/**
 * Хранит по вещи что угодно<br>
 * <br>
 * Ключ: номер актив + байт тип + байт тип связи<br>
 * Значение: что угодно - byte[]<br>
 */
public class ItemsValuesMap extends DBTabImpl<Tuple3<Long, Byte, byte[]>, byte[]> {

    public ItemsValuesMap(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);
    }

    public ItemsValuesMap(int dbs, ItemsValuesMap parent, DCSet dcSet) {
        super(dbs, parent, dcSet);
    }

    @Override
    public void openMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new ItemsValuesRocksDB(databaseSet, database);
                    break;
                default:
                    map = new ItemsValuesMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                default:
                    map = new NativeMapTreeMapFork(parent, databaseSet, Fun.TUPLE3_COMPARATOR, this);
            }
        }
    }

    public IteratorCloseable<Tuple3<Long, Byte, byte[]>> getIssuedPersons(Long personKey, boolean descending) {

        Tuple3<Long, Byte, byte[]> fromKey = new Tuple3<>(personKey, (byte) ItemCls.PERSON_TYPE, descending ? Longs.toByteArray(Long.MAX_VALUE) : new byte[0]);
        Tuple3<Long, Byte, byte[]> toKey = new Tuple3<>(personKey, (byte) ItemCls.PERSON_TYPE, descending ? new byte[0] : Longs.toByteArray(Long.MAX_VALUE));

        if (parent == null) {
            return getIterator(fromKey, toKey, descending);
        } else {
            return new MergedOR_IteratorsNoDuplicates((Iterable) ImmutableList.of(
                    parent.getIterator(fromKey, toKey, descending),
                    getIterator(fromKey, toKey, descending)),
                    new Fun.Tuple3Comparator<>(Fun.COMPARATOR, Fun.COMPARATOR, Fun.BYTE_ARRAY_COMPARATOR), descending);
        }
    }
}
