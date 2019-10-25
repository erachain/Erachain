package org.erachain.datachain;

// 30/03

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.dbs.DBTabImpl;
import org.erachain.dbs.mapDB.BlocksSuitMapDB;
import org.erachain.dbs.nativeMemMap.NativeMapHashMapFork;
import org.erachain.dbs.rocksDB.BlocksSuitRocksDB;
import org.erachain.utils.ObserverMessage;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.Fun;

import static org.erachain.database.IDB.DBS_MAP_DB;
import static org.erachain.database.IDB.DBS_ROCK_DB;

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
public class BlocksMapImpl extends DBTabImpl<Integer, Block> implements BlockMap {

    //@Setter
    private byte[] lastBlockSignature;
    private Atomic.Boolean processingVar;
    private Boolean processing;

    public BlocksMapImpl(int dbs, DCSet databaseSet, DB database) {
        super(dbs, databaseSet, database);

        // PROCESSING
        this.processingVar = database.getAtomicBoolean("processingBlock");
        this.processing = this.processingVar.get();
    }

    public BlocksMapImpl(int dbs, BlockMap parent, DCSet dcSet) {
        super(dbs, parent, dcSet);

        this.lastBlockSignature = parent.getLastBlockSignature();
        this.processing = false; /// parent.isProcessing();

    }

    @Override
    protected void openMap() {
        // OPEN MAP
        if (parent == null) {
            switch (dbsUsed) {
                case DBS_ROCK_DB:
                    map = new BlocksSuitRocksDB(databaseSet, database);
                    break;
                default:
                    map = new BlocksSuitMapDB(databaseSet, database);
            }
        } else {
            switch (dbsUsed) {
                case DBS_MAP_DB:
                    //map = new BlocksSuitMapDBFork((TransactionMap) parent, databaseSet);
                    //break;
                case DBS_ROCK_DB:
                    //map = new BlocksSuitMapDBFotk((TransactionMap) parent, databaseSet);
                    //break;
                default:
                    map = new NativeMapHashMapFork(parent, databaseSet, null);
            }
        }
    }


    @Override
    public Block last() {
        return get(size());
    }

    @Override
    public byte[] getLastBlockSignature() {
        if (lastBlockSignature == null) {
            lastBlockSignature = ((DCSet) databaseSet).getBlocksHeadsMap().get(this.size()).signature;
        }
        return lastBlockSignature;
    }

    @Override
    public void resetLastBlockSignature() {

        // TODO: еще вопрос про org.erachain.datachain.BlocksHeadsMap.getFullWeight

        lastBlockSignature = ((DCSet) databaseSet).getBlocksHeadsMap().get(this.size()).signature;
    }

    public void setLastBlockSignature(byte[] signature) {
        lastBlockSignature = signature;
    }

    @Override
    public boolean isProcessing() {
        if (processing != null) {
            return processing;
        }

        return false;
    }

    @Override
    public void setProcessing(boolean processing) {
        if (processingVar != null) {
            if (DCSet.isStoped()) {
                return;
            }
            processingVar.set(processing);
        }
        this.processing = processing;
    }

    @Override
    public Block getWithMind(int height) {
        return get(height);

    }

    protected long cacheClearedTime;
    @Override
    public Block get(Integer height) {
        Block block = super.get(height);
        if (block == null)
            return null;

        // LOAD HEAD
        block.loadHeadMind((DCSet) databaseSet);

        // проверим занятую память и очистим если что
        // это не Форк базы и большой блок взяли - наверно надо чистить КЭШ
        if (parent == null && System.currentTimeMillis() - cacheClearedTime > 10000
                && block.getTransactionCount() > 10
                && Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()
                && Runtime.getRuntime().freeMemory() <
                        Controller.MIN_MEMORY_TAIL << 3
                        //(Runtime.getRuntime().totalMemory() >> 1)
                ) {
            cacheClearedTime = System.currentTimeMillis();
            databaseSet.clearCache();
            System.gc();
        }
        return block;

    }

    @Override
    public void put(Block block) {
        DCSet dcSet = (DCSet) databaseSet;
        byte[] signature = block.getSignature();
        if (dcSet.getBlockSignsMap().contains(signature)) {
            logger.error("already EXIST : " + this.size()
                    + " SIGN: " + Base58.encode(signature));
            return;
        }
        int height = block.getHeight();

        PublicKeyAccount creator = block.getCreator();
        if (BlockChain.ERA_COMPU_ALL_UP && creator.getLastForgingData(dcSet) == null) {
            // так как у нас новые счета сами стартуют без инициализации - надо тут учеть начало
            creator.setForgingData(dcSet, height - BlockChain.DEVELOP_FORGING_START, block.getForgingValue());
        }
        creator.setForgingData(dcSet, height, block.getForgingValue());

        dcSet.getBlockSignsMap().set(signature, height);
        if (height < 1) {
            Long error = null;
            ++error;
        }

        dcSet.getBlocksHeadsMap().set(block.blockHead);
        this.setLastBlockSignature(signature);

        if (BlockChain.CHECK_BUGS > 5) {
            Block.BlockHead head = block.blockHead;
            Fun.Tuple2<Integer, Integer> lastPoint = dcSet.getAddressForging().getLast(block.getCreator().getAddress());
            if (lastPoint.a > head.heightBlock) {
                LOGGER.error("NOT VALID forging POINTS:" + lastPoint + " > " + head.heightBlock);
                Long i = null;
                i++;
            }
        }

        super.put(height, block);

    }

    // TODO make CHAIN deletes - only for LAST block!
    @Override
    public void delete(byte[] signature, byte[] reference, PublicKeyAccount creator) {
        DCSet dcSet = (DCSet) databaseSet;

        int height = this.size();

        this.setLastBlockSignature(reference);
        dcSet.getBlockSignsMap().delete(signature);

        // ORPHAN FORGING DATA
        if (height > 1) {

            dcSet.getBlocksHeadsMap().delete();

            // удаляем данные форжинга - внутри уже идет проверка на повторное удаление
            creator.delForgingData(dcSet, height);

        }

        if (BlockChain.CHECK_BUGS > 5) {
            Fun.Tuple2<Integer, Integer> lastPoint = dcSet.getAddressForging().getLast(creator.getAddress());
            if (lastPoint.b > height) {
                LOGGER.error("NOT VALID forging POINTS:" + lastPoint + " > " + height);
                Long i = null;
                i++;
            }
        }

        super.delete(height);

    }

    @Override
    public void notifyResetChain() {
        this.setChanged();
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_RESET_BLOCK_TYPE, null));
    }

    @Override
    public void notifyProcessChain(Block block) {

        if (Controller.getInstance().isOnStopping()) {
            return;
        }

        logger.debug("++++++ NOTIFY CHAIN_ADD_BLOCK_TYPE");
        this.setChanged();
        // NEED in BLOCK!
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_ADD_BLOCK_TYPE, block));

        logger.debug("++++++ NOTIFY CHAIN_ADD_BLOCK_TYPE END");
    }

    @Override
    public void notifyOrphanChain(Block block) {

        if (Controller.getInstance().isOnStopping()) {
            return;
        }

        logger.debug("===== NOTIFY CHAIN_REMOVE_BLOCK_TYPE");
        this.setChanged();
        // NEED in BLOCK!
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE, block));

        logger.debug("===== NOTIFY CHAIN_REMOVE_BLOCK_TYPE END");
    }

}
