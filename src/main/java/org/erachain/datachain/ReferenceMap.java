package org.erachain.datachain;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;
import org.erachain.core.crypto.Base58;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * TODO: Надо подумать может она лишняя??
 * seek reference to tx_Parent by address+timestamp
 * account.address -> <tx2.parentTimestamp>
 *
 */
public class ReferenceMap extends DCMap<byte[], Long> {

    public ReferenceMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public ReferenceMap(ReferenceMap parent) {
        super(parent, null);
    }

    @Override
    protected Map<byte[], Long> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("references")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .valuesOutsideNodesEnable()
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<byte[], Long> getMemoryMap() {
        return new TreeMap<>(UnsignedBytes.lexicographicalComparator());
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Long getDefaultValue() {
        // NEED for toByte for not referenced accounts
        return 0L;
    }

    public Long get(String address, Long timestamp) {
        return get(Bytes.concat(Base58.decode(address), Longs.toByteArray(timestamp)));
    }

    public void set(String address, Long timestamp, Long reference) {
        set(Bytes.concat(Base58.decode(address), Longs.toByteArray(timestamp)), reference);
    }

    public void delete(String address, Long timestamp) {
        delete(Bytes.concat(Base58.decode(address), Longs.toByteArray(timestamp)));
    }

    public Long getLast(String address) {
        return get(Base58.decode(address));
    }

}