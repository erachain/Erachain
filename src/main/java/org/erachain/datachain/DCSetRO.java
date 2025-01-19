package org.erachain.datachain;
// upd 09/03

import lombok.extern.slf4j.Slf4j;
import org.erachain.dbs.DBTab;

import java.io.File;
import java.io.IOError;

/**
 * набор таблиц для пересборки цепочки
 */
@Slf4j
public class DCSetRO extends DCSet {

    private BlocksMapImpl blockMap;
    // Нужно для размера - height
    private BlockSignsMap blockSignsMap;
    private BlocksHeadsMap blocksHeadsMap;

    public DCSetRO(File dbFile, int defaultDBS) {
        super(dbFile, DCSet.makeReadOnlyFileDB(dbFile), false, false);

        try {
            this.blockMap = new BlocksMapImpl(defaultDBS != DBS_FAST ? defaultDBS :
                    BLOCKS_MAP
                    , this, database);

            this.blockSignsMap = new BlockSignsMap(this, database);
            //this.blocksHeadsMap = new BlocksHeadsMap(this, database);

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

    /**
     * ключ: подпись блока
     * занчение: номер блока (высота, height)<br>
     * <p>
     * TODO - убрать длинный индекс
     *
     * @return
     */
    @Override
    public BlockSignsMap getBlockSignsMap() {
        return this.blockSignsMap;
        //throw new RuntimeException("not implemented");
    }

    /**
     * Block Height -> Block.BlockHead - заголовок блока влючая все что вычислено <br>
     * <p>
     * + FACE - version, creator, signature, transactionsCount, transactionsHash<br>
     * + parentSignature<br>
     * + Forging Data - Forging Value, Win Value, Target Value<br>
     */
    @Override
    public BlocksHeadsMap getBlocksHeadsMap() {
        //return this.blocksHeadsMap;
        throw new RuntimeException("not implemented");
    }

    @Override
    public synchronized void close() {

        if (this.database != null) {
            // THIS IS not FORK
            if (!this.database.isClosed()) {
                this.addUses();

                for (DBTab tab : tables) {
                    try {
                        tab.close();
                    } catch (IOError e) {
                        logger.error(e.getMessage(), e);
                    }
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
            }

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
