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
                    //map = new BlocksSuitMapDBFork((TransactionTab) parent, databaseSet);
                    //break;
                case DBS_ROCK_DB:
                    //map = new BlocksSuitMapDBFotk((TransactionTab) parent, databaseSet);
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

    @Override
    public Block get(Integer height) {
        Block block = super.get(height);
        if (block == null)
            return null;

        // LOAD HEAD
        block.loadHeadMind((DCSet) databaseSet);

        // проверим занятую память и очистим если что
        if (parent == null && block.getTransactionCount() > 33) {
            // это не Форк базы и большой блок взяли - наверно надо чистить КЭШ
            if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
                if (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().totalMemory() >> 1)) {
                    ((DCSet) databaseSet).clearCache();
                }
            }

        }
        return block;

    }

    @Override
    public boolean add(Block block) {
        DCSet dcSet = (DCSet) databaseSet;
        byte[] signature = block.getSignature();
        if (dcSet.getBlockSignsMap().contains(signature)) {
            logger.error("already EXIST : " + this.size()
                    + " SIGN: " + Base58.encode(signature));
            return true;
        }
        int height = block.getHeight();

        if (block.getVersion() == 0) {
            // GENESIS block
        } else {

            // PROCESS FORGING DATA
        }

        PublicKeyAccount creator = block.getCreator();
        if (BlockChain.DEVELOP_USE && creator.getLastForgingData(dcSet) == null) {
            // так как унас новые счета сами стартуют без инициализации - надо тут учеть начало
            creator.setForgingData(dcSet, height - BlockChain.DEVELOP_FORGING_START, block.getForgingValue());
        }
        creator.setForgingData(dcSet, height, block.getForgingValue());

        // logger.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1200: " +
        // (System.currentTimeMillis() - start)*0.001);

        dcSet.getBlockSignsMap().set(signature, height);
        if (height < 1) {
            Long error = null;
            ++error;
        }

        dcSet.getBlocksHeadsMap().set(block.blockHead);
        this.setLastBlockSignature(signature);

        // logger.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1500: " +
        // (System.currentTimeMillis() - start)*0.001);

        // TODO feePool
        // this.setFeePool(_feePool);
        boolean sss = super.set(height, block);
        // logger.error("&&&&&&&&&&&&&&&&&&&&&&&&&&& 1600: " +
        // (System.currentTimeMillis() - start)*0.001);
        return sss;

    }

	/*
	public boolean set(int height, Block block) {
		return false;
	}
	 */

    // TODO make CHAIN deletes - only for LAST block!
    @Override
    public Block remove(byte[] signature, byte[] reference, PublicKeyAccount creator) {
        DCSet dcSet = (DCSet) databaseSet;

        int height = this.size();

        this.setLastBlockSignature(reference);
        dcSet.getBlockSignsMap().delete(signature);

        // ORPHAN FORGING DATA
        if (height > 1) {

            dcSet.getBlocksHeadsMap().remove();

            // удаляем данные форжинга - внутри уже идет проверка на повторное удаление
            creator.delForgingData(dcSet, height);

        }

        // use SUPER.class only!
        return super.remove(height);

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
