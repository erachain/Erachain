package org.erachain.dbs.mapDB;

// 30/03

import lombok.extern.slf4j.Slf4j;
import org.erachain.core.block.Block;
import org.erachain.database.DBASet;
import org.erachain.database.serializer.BlockSerializer;
import org.erachain.datachain.BlocksSuit;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

;

//import com.sun.media.jfxmedia.logging.Logger;

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
public class BlocksSuitMapDB extends DBMapSuit<Integer, Block> implements BlocksSuit {


    public BlocksSuitMapDB(DBASet databaseSet, DB database) {
        super(databaseSet, database, logger, false);
    }

    @Override
    public void openMap() {
        HI = Integer.MAX_VALUE;
        LO = 0;

        // OPEN MAP
        map = database.createTreeMap("blocks")
                .keySerializer(BTreeKeySerializer.BASIC)
                .valueSerializer(new BlockSerializer())
                .valuesOutsideNodesEnable()
                .makeOrGet();

    }

}
