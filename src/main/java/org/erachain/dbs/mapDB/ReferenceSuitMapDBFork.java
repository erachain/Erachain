package org.erachain.dbs.mapDB;

import com.google.common.primitives.UnsignedBytes;
import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.datachain.ReferenceMap;
import org.erachain.datachain.ReferenceSuit;
import org.mapdb.Hasher;
import org.mapdb.SerializerBase;

import java.util.TreeMap;


/**
 * seek reference to tx_Parent by address+timestamp
 * account.address -> LAST[TX.timestamp + TX.dbRef]
 * account.address + TX.timestamp -> PARENT[TX.timestamp + TX.dbRef]
 */
@Slf4j
public class ReferenceSuitMapDBFork extends DBMapSuitFork<byte[], long[]>
        implements ReferenceSuit {

    public ReferenceSuitMapDBFork(ReferenceMap parent, DBASet databaseSet) {
        super(parent, databaseSet, logger, false, null);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        if (database == null) {
            map = new TreeMap<>(UnsignedBytes.lexicographicalComparator());
        } else {
            map = database.createHashMap("references")
                    .keySerializer(SerializerBase.BYTE_ARRAY) // ОЧЕНЬ ВАЖНО! иначе работать не будет поиск с байтами
                    // проверка в org.erachain.core.account.AccountTest.setLastTimestamp
                    .hasher(Hasher.BYTE_ARRAY) // ОЧЕНЬ ВАЖНО! иначе работать не будет поиск с байтами
                    // проверка в org.erachain.core.account.AccountTest.setLastTimestamp
                    .valueSerializer(SerializerBase.LONG_ARRAY)
                    .makeOrGet();
        }
    }

}