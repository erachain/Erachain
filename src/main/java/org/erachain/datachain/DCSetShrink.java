package org.erachain.datachain;
// upd 09/03

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOError;

/**
 * набор таблиц для пересборки цепочки
 */
@Slf4j
public class DCSetShrink extends DCSet {

    private BlocksMapImpl blockMap;

    public DCSetShrink(File dbFile) {
        super(dbFile, DCSet.makeShrinkFileDB(dbFile), false, false);

        try {
            this.blockMap = new BlocksMapImpl(DBS_MAP_DB, this, database);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            this.close();
            throw e;
        }

    }

    @Override
    public BlocksMapImpl getBlockMap() {
        return this.blockMap;
    }

    @Override
    public synchronized void close() {

        if (this.database == null)
            return;

        // THIS IS not FORK
        if (!this.database.isClosed()) {
            this.addUses();

            try {
                blockMap.close();
            } catch (IOError e) {
                logger.error(e.getMessage(), e);
            }

            // улучшает работу финализера
            tables = null;
            try {
                this.database.close();
            } catch (IOError e) {
                logger.error(e.getMessage(), e);
            }
            // улучшает работу финализера
            this.database = null;

            this.uses = 0;

            logger.info("closed " + toString());
        }

    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public String toString() {
        return "ReadOnly backup " + getFile().getName();
    }

}
