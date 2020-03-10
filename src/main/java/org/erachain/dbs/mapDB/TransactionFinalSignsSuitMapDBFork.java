package org.erachain.dbs.mapDB;

// 30/03

import com.google.common.primitives.UnsignedBytes;
import lombok.extern.slf4j.Slf4j;
import org.erachain.database.DBASet;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.erachain.datachain.TransactionFinalMapSignsSuit;
import org.mapdb.DB;
import org.mapdb.Hasher;
import org.mapdb.SerializerBase;

import java.util.TreeMap;


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
public class TransactionFinalSignsSuitMapDBFork extends DBMapSuitFork<byte[], Long>
        implements TransactionFinalMapSignsSuit {


    public TransactionFinalSignsSuitMapDBFork(TransactionFinalMapSigns parent, DBASet databaseSet, boolean sizeEnable) {
        super(parent, databaseSet, logger, sizeEnable, null);
    }

    @Override
    public void openMap() {
        //OPEN MAP
        if (database == null) {
            map = new TreeMap<>(UnsignedBytes.lexicographicalComparator());
        } else {

            // HASH map is so QUICK
            DB.HTreeMapMaker mapConstruct = database.createHashMap("signature_final_tx")
                    .keySerializer(SerializerBase.BYTE_ARRAY)
                    .hasher(Hasher.BYTE_ARRAY)
                    .valueSerializer(SerializerBase.LONG);

            if (sizeEnable)
                mapConstruct = mapConstruct.counterEnable();

            map = mapConstruct.makeOrGet();

        }
    }

}
