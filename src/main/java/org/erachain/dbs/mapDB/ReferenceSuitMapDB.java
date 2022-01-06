package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.datachain.ReferenceSuit;
import org.mapdb.DB;
import org.mapdb.Hasher;
import org.mapdb.SerializerBase;


/**
 * seek time reference to tx_Parent by address+timestamp
 * account.address -> LAST[TX.timestamp + TX.dbRef]
 * account.address + TX.timestamp -> PARENT[TX.timestamp + TX.dbRef]
 */
@Slf4j
public class ReferenceSuitMapDB extends DBMapSuit<byte[], long[]>
        implements ReferenceSuit {

    public ReferenceSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    public void openMap() {

        //OPEN MAP
        map = database.createHashMap("references")
                .keySerializer(SerializerBase.BYTE_ARRAY) // ОЧЕНЬ ВАЖНО! иначе работать не будет поиск с байтами
                // проверка в org.erachain.core.account.AccountTest.setLastTimestamp
                .hasher(Hasher.BYTE_ARRAY) // ОЧЕНЬ ВАЖНО! иначе работать не будет поиск с байтами
                // проверка в org.erachain.core.account.AccountTest.setLastTimestamp
                .valueSerializer(SerializerBase.LONG_ARRAY)
                .makeOrGet();
    }

}