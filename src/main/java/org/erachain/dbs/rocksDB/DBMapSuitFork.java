package org.erachain.dbs.rocksDB;

import lombok.Getter;
import org.erachain.database.DBASet;
import org.erachain.dbs.DBTab;
import org.erachain.dbs.ForkedMap;
import org.erachain.dbs.rocksDB.integration.DBRocksDBTable;
import org.slf4j.Logger;

/**
 * Оболочка для Карты от конкретной СУБД чтобы эту оболочку вставлять в Таблицу, которая форкнута (см. fork()).
 * Тут всегда должен быть задан Родитель. Здесь другой порядок обработки данных в СУБД.
 * Так как тут есть слив в базу и WriteBatchIndexed - то не нужны всякие примочки с deleted
 * @param <T>
 * @param <U>
 */
public abstract class DBMapSuitFork<T, U> extends DBMapSuit<T, U> implements ForkedMap {

    @Getter
    protected DBTab<T, U> parent;

    public DBMapSuitFork(DBTab parent, DBRocksDBTable map, DBASet dcSet, Logger logger, U defaultValue) {
        assert (parent != null);

        this.databaseSet = dcSet;
        this.database = dcSet.database;
        this.logger = logger;
        this.defaultValue = defaultValue;

        this.parent = parent;

        // тут просто берем туже карту так как потом или сольем или убьем
        this.map = map;

    }

    @Override
    public void openMap() {
    }

    /**
     * просто стедаем коммит и все
     */
    @Override
    public void writeToParent() {
        commit();
    }

    @Override
    public String toString() {
        return getClass().getName() + ".FORK";
    }
}
