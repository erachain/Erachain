package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.SerializerBase;

@Slf4j
public class TransactionSuitMapDBinMem extends TransactionSuitMapDB {

    public TransactionSuitMapDBinMem(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    public void getMap() {

        database = DBMaker
                .newMemoryDB()
                //.transactionDisable()
                //.cacheHardRefEnable()
                //
                //.newMemoryDirectDB()
                .make();

        // OPEN MAP
        map = database.createHashMap("transactions")
                .keySerializer(SerializerBase.LONG)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();


    }

}
