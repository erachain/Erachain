package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.DB;
import org.mapdb.Hasher;
import org.mapdb.SerializerBase;

import java.util.TreeMap;


/**
 * seek reference to tx_Parent by address+timestamp
 * account.address -> LAST[TX.timestamp + TX.dbRef]
 * account.address + TX.timestamp -> PARENT[TX.timestamp + TX.dbRef]
 *
 */
public class ReferenceMap extends DCUMap<byte[], long[]> {

    public ReferenceMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public ReferenceMap(ReferenceMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        map = database.createHashMap("references")
                .keySerializer(SerializerBase.BYTE_ARRAY) // ОЧЕНЬ ВАЖНО! иначе работатьт не будет поиск с байтами
                // проверка в org.erachain.core.account.AccountTest.setLastTimestamp
                .hasher(Hasher.BYTE_ARRAY) // ОЧЕНЬ ВАЖНО! иначе работатьт не будет поиск с байтами
                // проверка в org.erachain.core.account.AccountTest.setLastTimestamp
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected void getMemoryMap() {
        map = new TreeMap<>(UnsignedBytes.lexicographicalComparator());
    }

    protected void createIndexes() {
    }

    @Override
    protected long[] getDefaultValue() {
        // NEED for toByte for not referenced accounts
        return null;
    }

}