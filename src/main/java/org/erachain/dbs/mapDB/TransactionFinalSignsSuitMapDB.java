package org.erachain.dbs.mapDB;

// 30/03

import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionFinalMapSignsSuit;
import org.mapdb.DB;
import org.mapdb.Hasher;
import org.mapdb.SerializerBase;


/**
 * Хранит блоки полностью - с транзакциями
 * <p>
 * ключ: номер блока (высота, height)<br>
 * занчение: Блок<br>
 * <p>
 * Есть вторичный индекс, для отчетов (blockexplorer) - generatorMap
 * TODO - убрать длинный индек и вставить INT
 *
 * @return
 */

@Slf4j
public class TransactionFinalSignsSuitMapDB extends DBMapSuit<byte[], Long> implements TransactionFinalMapSignsSuit {


    public TransactionFinalSignsSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger);
    }

    @Override
    protected void getMap() {
        //OPEN MAP
        // HASH map is so QUICK
        map = database.createHashMap("signature_final_tx")
                .keySerializer(SerializerBase.BYTE_ARRAY)
                .hasher(Hasher.BYTE_ARRAY)
                .valueSerializer(SerializerBase.LONG)
                .makeOrGet();
    }

}
