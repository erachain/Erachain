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
import org.jetbrains.annotations.NotNull;
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
    public void openMap() {
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
                    map = new NativeMapHashMapFork(parent, databaseSet, this);
            }
        }
    }

    @Override
    public int size() {
        return ((DCSet) databaseSet).getBlockSignsMap().size();
    }

    @Override
    public Block last() {
        return getAndProcess(size());
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

    protected long cacheClearedTime;
    public Block getAndProcess(Integer height) {
        Block block = get(height);
        if (block == null)
            return null;

        if (false) {
            // проверим занятую память и очистим если что
            // это не Форк базы и большой блок взяли - наверно надо чистить КЭШ
            if (parent == null && System.currentTimeMillis() - cacheClearedTime > 10000
                    && block.getTransactionCount() > 10
                    && Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()
                    && Runtime.getRuntime().freeMemory() <
                    (Runtime.getRuntime().totalMemory() >> 10)
                            + Controller.MIN_MEMORY_TAIL
            ) {
                cacheClearedTime = System.currentTimeMillis();
                databaseSet.clearCache();
                System.gc();
            }
        }

        // LOAD HEAD
        block.loadHeadMind((DCSet) databaseSet);

        return block;

    }

    @Override
    public void putAndProcess(Block block) {
        DCSet dcSet = (DCSet) databaseSet;
        byte[] signature = block.getSignature();
        int height = block.getHeight();
        if (height < 1) {
            Long error = null;
            ++error;
        }

        if (dcSet.getBlockSignsMap().contains(signature)) {
            logger.error("already EXIST : " + height
                    + " SIGN: " + Base58.encode(signature));
            return;
        }

        dcSet.getBlockSignsMap().put(signature, height);
        if (dcSet.getBlockSignsMap().size() != height) {
            // так как это вызывается асинхронно при проверке прилетающих победных блоков
            // то тут иногда вылетает ошибка - но в общем должно быть норм все
            logger.error("CHECK TABS: \n getBlockSignsMap().size() != height : "
                    + dcSet.getBlockSignsMap().size() + " != " + height
                    + " : " + block);
            if (BlockChain.CHECK_BUGS > 9) {
                Long error = null;
                ++error;
            }
        }

        PublicKeyAccount creator = block.getCreator();
        if (BlockChain.ERA_COMPU_ALL_UP && creator.getLastForgingData(dcSet) == null) {
            // так как у нас новые счета сами стартуют без инициализации - надо тут учесть начало
            int diff = height - BlockChain.DEVELOP_FORGING_START;
            if (diff <= 0) {
                diff = height;
            }
            int forgingValue = block.getForgingValue();
            // учитываем начало - там внутри оно установится само
            creator.setForgingData(dcSet, diff, forgingValue);
            // учитываем тут сам факт сборки блока
            // тогда будет правильно все - иначе была ошибка - повторный сбор через 1 блок с этого счет после первого сбора
            creator.setForgingData(dcSet, height, forgingValue);
        } else {
            creator.setForgingData(dcSet, height, block.getForgingValue());
        }

        dcSet.getBlocksHeadsMap().putAndProcess(height, block.blockHead);
        this.setLastBlockSignature(signature);

        if (BlockChain.CHECK_BUGS > 5) {
            Block.BlockHead head = block.blockHead;
            Fun.Tuple3<Integer, Integer, Integer> lastPoint = dcSet.getAddressForging().getLast(block.getCreator().getAddress());
            if (lastPoint.a > head.heightBlock) {
                LOGGER.error("NOT VALID forging POINTS:" + lastPoint + " > " + head.heightBlock);
                Long error = null;
                error++;
            }
        }

        put(height, block);

    }

    // TODO make CHAIN deletes - only for LAST block!
    @Override
    public void deleteAndProcess(byte[] signature, byte[] reference, PublicKeyAccount creator, int height) {

        if (height < 2)
            return;

        DCSet dcSet = (DCSet) databaseSet;

        dcSet.getBlockSignsMap().delete(signature);

        this.setLastBlockSignature(reference);

        // ORPHAN FORGING DATA
        dcSet.getBlocksHeadsMap().deleteAndProcess(height);

        // удаляем данные форжинга - внутри уже идет проверка на повторное удаление
        creator.delForgingData(dcSet, height);

        delete(height);

        if (BlockChain.CHECK_BUGS > 5 && !BlockChain.ERA_COMPU_ALL_UP && BlockChain.ALL_VALID_BEFORE < height) {
            Fun.Tuple3<Integer, Integer, Integer> lastPoint = dcSet.getAddressForging().getLast(creator.getAddress());
            if (lastPoint == null) {
                LOGGER.error("NOT VALID forging POINTS: lastPoint == null");
                Long error = null;
                error++;
            } else if (lastPoint.a > height) {
                LOGGER.error("NOT VALID forging POINTS:" + lastPoint + " > " + height);
                Long error = null;
                error++;
            }
        }

    }

    public void deleteAndProcess(@NotNull Block block) {
        deleteAndProcess(block.getSignature(), block.getReference(), block.getCreator(), block.heightBlock);
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

        long time = System.currentTimeMillis();
        this.setChanged();
        // NEED in BLOCK!
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_ADD_BLOCK_TYPE, block));
        time -= System.currentTimeMillis();
        if (time < -1) {
            logger.debug("++++++ NOTIFY CHAIN_ADD_BLOCK_TYPE period: " + -time);
        }

    }

    @Override
    public void notifyOrphanChain(Block block) {

        if (Controller.getInstance().isOnStopping()) {
            return;
        }

        long time = System.currentTimeMillis();
        this.setChanged();
        // NEED in BLOCK!
        this.notifyObservers(new ObserverMessage(ObserverMessage.CHAIN_REMOVE_BLOCK_TYPE, block));
        time -= System.currentTimeMillis();
        if (time < -1) {
            logger.debug("===== NOTIFY CHAIN_REMOVE_BLOCK_TYPE period: " + -time);
        }

    }

}
