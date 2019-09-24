package org.erachain.dbs.mapDB;

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.TransactionSerializer;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.SerializerBase;

import java.io.File;

@Slf4j
public class TransactionSuitMapDBinMem extends TransactionSuitMapDB {

    public TransactionSuitMapDBinMem(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    public void getMap() {

        if (false) {
            database = DBMaker
                    .newMemoryDB()
                    //.transactionDisable()
                    //.cacheHardRefEnable()
                    //
                    //.newMemoryDirectDB()
                    .make();
        } else {
            File dbFile = new File(Settings.getInstance().getDataDir(), "txPool.dat");
            database = DCSet.getHardBase(dbFile);
        }

        // OPEN MAP
        map = database.createHashMap("transactions")
                .keySerializer(SerializerBase.LONG)
                .valueSerializer(new TransactionSerializer())
                .counterEnable()
                .makeOrGet();


    }

    @Override
    public void close() {
        database.close();
    }

    @Override
    public void clear() {
        database.close();
        getMap();
        createIndexes();
    }
}
