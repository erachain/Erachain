package org.erachain.dbs.mapDB;

import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.SerializerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionSuitMapDBinMem extends TransactionSuitMapDB {

    static Logger logger = LoggerFactory.getLogger(TransactionSuitMapDBinMem.class.getSimpleName());

    public TransactionSuitMapDBinMem(DBASet databaseSet, DB database) {
        super(databaseSet, database);
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
