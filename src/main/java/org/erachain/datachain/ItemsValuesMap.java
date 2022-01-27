package org.erachain.datachain;

import com.google.common.primitives.Longs;
import org.erachain.core.item.ItemCls;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.IteratorCloseable;
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

    /**
     * search
     *
     * @param personKey
     * @param descending
     * @return
     */
    public IteratorCloseable<Tuple3<Long, Byte, byte[]>> getIssuedPersonsIter(Long personKey, int itemType, boolean descending) {

        byte[] itemIssuedBytesMAX = new byte[]{(byte) itemType, 127, 127, 127, 127, 127, 127, 127};
        byte[] itemIssuedBytesMIN = new byte[]{(byte) itemType};

        Tuple3<Long, Byte, byte[]> fromKey = new Tuple3<>(personKey, (byte) ItemCls.PERSON_TYPE, descending ? itemIssuedBytesMAX : itemIssuedBytesMIN);
        Tuple3<Long, Byte, byte[]> toKey = new Tuple3<>(personKey, (byte) ItemCls.PERSON_TYPE, descending ? itemIssuedBytesMIN : itemIssuedBytesMAX);

        return getIterator(fromKey, toKey, descending);

    }

    public byte[] makeIssuedItemKey(ItemCls item) {
        byte[] itemIssuedBytes = Longs.toByteArray(item.getKey());
        // first byte as type
        itemIssuedBytes[0] = (byte) item.getItemType();
        return itemIssuedBytes;

    }

    public void putIssuedItem(ItemCls issuer, ItemCls item, Long dbRef) {
        put(new Fun.Tuple3<Long, Byte, byte[]>(issuer.getKey(), (byte) issuer.getItemType(), makeIssuedItemKey(item)), Longs.toByteArray(dbRef));

    }

    public void deleteIssuedItem(ItemCls issuer, ItemCls item) {
        delete(new Fun.Tuple3<Long, Byte, byte[]>(issuer.getKey(), (byte) issuer.getItemType(), makeIssuedItemKey(item)));

    }
}
