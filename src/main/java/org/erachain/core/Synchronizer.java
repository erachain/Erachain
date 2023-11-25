package org.erachain.core;

import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.BlockMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ReferenceMapImpl;
import org.erachain.dbs.DBTab;
import org.erachain.network.Peer;
import org.erachain.network.message.BlockMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.network.message.SignaturesMessage;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.Pair;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * функционал скачки цепочки с других узлов - догоняние сети
 */
public class Synchronizer extends Thread {

    public static final int GET_BLOCK_TIMEOUT = 20000 + (BlockChain.GENERATING_MIN_BLOCK_TIME_MS(BlockChain.VERS_30SEC + 1) >> (6 - (Controller.HARD_WORK >> 1)));
    public static final int GET_HEADERS_TIMEOUT = GET_BLOCK_TIMEOUT;
    private static final int BYTES_MAX_GET = BlockChain.MAX_BLOCK_SIZE_BYTES << 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(Synchronizer.class.getSimpleName());
    private static final byte[] PEER_TEST = new byte[]{(byte) 185, (byte) 195, (byte) 26, (byte) 245}; // 185.195.26.245
    public static int BAN_BLOCK_TIMES = 16;
    private static int MAX_ORPHAN_TRANSACTIONS_MY = (BlockChain.MAX_BLOCK_SIZE_GEN << 2);
    private Peer fromPeer;
    Controller ctrl;
    BlockChain bchain;

    public Synchronizer(Controller ctrl) {
        this.ctrl = ctrl;
        this.bchain = ctrl.getBlockChain();

        this.start();
    }

    // chack = true - check this signature in peer
    public static Block getBlock(byte[] signature, Peer peer, boolean check) throws Exception {

        // CREATE MESSAGE
        Message message = MessageFactory.getInstance().createGetBlockMessage(signature);

        // SEND MESSAGE TO PEER
        BlockMessage response = (BlockMessage) peer.getResponse(message, check ? GET_BLOCK_TIMEOUT << 1 : GET_BLOCK_TIMEOUT);

        // если ошибка то банить нужно в любом случае - чтобы не зацикливаться на этом пире

        // CHECK IF WE GOT RESPONSE
        if (response == null) {
            if (check) {
                return null;
            } else {
                // ERROR
                String mess = "*** getBlock: Peer timed out";
                peer.ban(mess);
                throw new Exception(mess);
            }
        }

        Block block = response.getBlock();
        if (block == null) {
            String mess = "*** getBlock: Block is NULL";
            peer.ban(mess);
            throw new Exception(mess);
        }

        // CHECK BLOCK SIGNATURE
        if (!block.isSignatureValid()) {
            int banTime = BAN_BLOCK_TIMES << 1;
            String mess = "*** getBlock: Dishonest peer - Invalid block --signature. Ban for " + banTime;
            peer.ban(banTime, mess);
            throw new Exception(mess);
        }

        // проверим - парсятся ли транзакции нормально
        try {
            block.getTransactions();
        } catch (Exception e) {
            int banTime = BAN_BLOCK_TIMES << 1;
            String mess = "*** getBlock: Dishonest peer - Invalid block on parse transactions. Ban for " + banTime;
            peer.ban(banTime, mess);
            throw new Exception(mess);
        }

        /////// block.makeTransactionsHash();
        // ADD TO LIST
        return block;
    }

    public Peer getPeer() {
        return fromPeer;
    }

    private ConcurrentHashMap<Long, Transaction> checkNewBlocks(Tuple2<Integer, Long> myHW, DCSet fork, Block lastCommonBlock, int checkPointHeight,
                                                                List<Block> newBlocks, Peer peer) throws Exception {

        LOGGER.debug("*** core.Synchronizer.checkNewBlocks - START");

        //Controller cnt = Controller.getInstance();
        BlockMap blockMap = fork.getBlockMap();

        // ORPHAN MY BLOCKS IN FORK TO VALIDATE THE NEW BLOCKS

        // GET LAST BLOCK
        Block lastBlock = blockMap.last();

        int lastHeight = lastBlock.getHeight();
        LOGGER.debug("*** core.Synchronizer.checkNewBlocks - lastBlock[" + lastHeight + "]\n" + " newBlocks.size = "
                + newBlocks.size() + "\n search common block in FORK" + " in mainDB: "
                + lastBlock.getHeight() + " in ForkDB: " + lastBlock.getHeight()
                + "\n for last CommonBlock = " + lastCommonBlock.getHeight());

        int countOrphanedTransactions = 0;
        /**
         * Ключ по Номер блока и порядка в нем
         */
        ConcurrentHashMap<Long, Transaction> orphanedTransactions = new ConcurrentHashMap<Long, Transaction>();

        // ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK - in FORK DB
        // ============ by EQUAL SIGNATURE !!!!!
        byte[] lastCommonBlockSignature = lastCommonBlock.getSignature();
        while (!Arrays.equals(lastBlock.getSignature(), lastCommonBlockSignature)) {

            LOGGER.debug("*** ORPHAN LAST BLOCK [" + lastBlock.getHeight() + "] in FORK_DB UNTIL WE HAVE REACHED COMMON BLOCK ["
                    + lastCommonBlock.getHeight() + "]");
            if (checkPointHeight > lastBlock.getHeight()) {
                String mess = "Dishonest peer by not valid lastCommonBlock[" + lastCommonBlock.getHeight() + "] < ["
                        + checkPointHeight + "] checkPointHeight";
                peer.ban(BAN_BLOCK_TIMES, mess);
                throw new Exception(mess);

            } else if (lastBlock.getVersion() == 0) {
                String mess = "Dishonest peer by not valid lastCommonBlock[" + lastCommonBlock.getHeight()
                        + "] Version == 0";
                peer.ban(BAN_BLOCK_TIMES, mess);
                throw new Exception(mess);

            }
            // logger.debug("*** core.Synchronizer.checkNewBlocks - try orphan:
            // " + lastBlock.getHeight(fork));
            if (ctrl.isOnStopping())
                throw new Exception("on stopping");

            int height = lastBlock.getHeight();

            if (++countOrphanedTransactions < MAX_ORPHAN_TRANSACTIONS_MY) {
                // сохраним откаченные транзакции - может их потом включим в очередь
                for (Transaction transaction : lastBlock.getTransactions()) {
                    orphanedTransactions.put(Longs.fromByteArray(transaction.getSignature()), transaction);
                }
                countOrphanedTransactions += lastBlock.getTransactionCount();
            }

            // Так как откаченные транзакций мы копим тут локально в orphanedTransactions
            // И с учетом что ниже сразу процессим
            try {
                lastBlock.orphan(fork, true);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                // выход - так как поймали внутреннюю ошибку базы
                ctrl.stopAndExit(311);
            }

            LOGGER.debug("*** checkNewBlocks - orphaned! chain size: " + fork.getBlockMap().size());
            lastBlock.close();
            ctrl.getDCSet().clearCache();

            lastBlock = blockMap.last();

        }

        LOGGER.debug("*** checkNewBlocks - lastBlock[" + lastBlock.getHeight() + "]");
        if (false) {
            // Тест откатов чтобы откатиться 1 раз и больше не синхриться - для проверки удаления
            test2 = true;
            return orphanedTransactions;

        }
        // VALIDATE THE NEW BLOCKS

        //////// здесь надо обновить для валидании ссылки счетов на поледние трнзакции за последние Х блоков\
        if (BlockChain.NOT_STORE_REFFS_HISTORY && BlockChain.CHECK_DOUBLE_SPEND_DEEP >= 0) {
            // TODO тут нужно обновить за последние 3-10 блоков значения  если проверка используется
            ReferenceMapImpl map = fork.getReferenceMap();

        }

        // Height & Weight
        int myHeight = myHW.a; // + 1; // высота на котрой тестировать СИЛУ цепочки
        long myWeight = myHW.b;
        int newHeight = lastBlock.getHeight() + newBlocks.size();
        // проверять СИЛУ цепочки только если лна не на много лучше моей высоты
        /// boolean checkFullWeight = !BlockChain.DEVELOP_USE;

        LOGGER.debug("*** checkNewBlocks - VALIDATE THE NEW BLOCKS in FORK");

        boolean isFromTrustedPeer = bchain.isPeerTrusted(peer);

        for (Block block : newBlocks) {

            int height = block.getHeight();
            int bbb = fork.getBlockMap().size() + 1;
            int hhh = fork.getBlocksHeadsMap().size() + 1;
            int sss = fork.getBlockSignsMap().size() + 1;
            assert (height == hhh);
            assert (bbb == hhh);
            assert (sss == hhh);

            if (height == fork.getBlockMap().size()) {
                if (Arrays.equals(block.getSignature(), fork.getBlockMap().getLastBlockSignature())) {
                    LOGGER.error("*** checkNewBlocks - already LAST! [" + height + "] "
                            + Base58.encode(block.getSignature())
                            + " from peer: " + peer);
                    continue;
                } else {
                    String mess = "*** checkNewBlocks - already LAST and not equal SIGN! [" + height + "] "
                            + Base58.encode(block.getSignature())
                            + " from peer: " + peer;
                    peer.ban(mess);
                    throw new Exception(mess);
                }
            } else {
                //Tuple2<Integer, Long> item = fork.getBlockSignsMap().get(block);
                if (fork.getBlockSignsMap().contains(block.getSignature())) {
                    LOGGER.error("*** checkNewBlocks - DUPLICATE SIGN! [" + height + "] "
                            + Base58.encode(block.getSignature())
                            + " from peer: " + peer);
                    continue;

                }
            }

            if (isFromTrustedPeer) {
                block.setFromTrustedPeer();
            }

            if (block.isFromTrustedPeer()) {
                // нужно все равно просчитать заголовок блока и решить блок
                int invalid = block.isValidHead(fork);
                if (invalid > 0) {
                    // все же может не просчитаться высота блока м цель его из-за ошибки валидации
                    // поэтому делаем проверку все равно
                    // INVALID BLOCK THROW EXCEPTION
                    String mess = "Dishonest peer by not is Valid block, height: " + height;
                    if (invalid > Block.INVALID_BRANCH) {
                        peer.ban(BAN_BLOCK_TIMES << 1, mess);
                    } else {
                        peer.setMute(1);
                    }
                    throw new Exception(mess);
                }
                LOGGER.debug("*** not VALIDATE  [" + height + "] from trusted PEER");

                block.process(fork, false);
            } else {
                LOGGER.debug("*** VALIDATE in FORK [" + height + "]");

                // CHECK IF VALID
                if (block.heightBlock > BlockChain.ALL_VALID_BEFORE && !block.isSignatureValid()) {
                    // INVALID BLOCK THROW EXCEPTION
                    String mess = "Dishonest peer by not is Valid block, height: " + height;
                    peer.ban(BAN_BLOCK_TIMES << 1, mess);
                    throw new Exception(mess);
                }

                try {
                    block.getTransactions();
                } catch (Exception e) {
                    LOGGER.debug(e.getMessage(), e);
                    String mess = "Dishonest peer error block.getTransactions PARSE: " + height;
                    peer.ban(BAN_BLOCK_TIMES << 1, mess);
                    throw new Exception(mess);
                }

                if (block.heightBlock > BlockChain.ALL_VALID_BEFORE) {
                    if (block.isValid(fork,
                            true /// это же проверка в ФОРКЕ - тут нужно! Тем более что там внутри процессинг уже идет
                    ) > 0) {
                        // INVALID BLOCK THROW EXCEPTION
                        String mess = "Dishonest peer by not is Valid block, height: " + height;
                        peer.ban(BAN_BLOCK_TIMES << 1, mess);
                        throw new Exception(mess);
                    }
                } else {
                    // тут не было проверки заголовка а надо бы - чтобы его создать
                    int invalid = block.isValidHead(fork);
                    if (invalid > 0) {
                        // чисто для лога - мол предупреждение что там Заголовок битый
                        LOGGER.info("BEFORE ALL_VALID - Block.Head ERROR: " + invalid);
                    }
                    // и полностью просчитать блок
                    block.process(fork, false);
                }
            }

            /// откатывать уже не нужно - он в isValid выше просчитался

            // проверка силы цепочки на уровне нашего блока и если высота новой цепочки меньше нашей
            if (height - 1 == myHeight && myWeight > block.blockHead.totalWinValue
            ) {
                String mess = "Weak FullWeight, height: " + height
                        + " myWeight > ext.Weight: " + myWeight + " > " + fork.getBlocksHeadsMap().getFullWeight();
                LOGGER.debug(peer + " " + mess);
                // суть в том что тут цепочка на этой высоте слабже моей,
                // поэтому мы ее пока забаним чтобы с ней постоянно не синхронизироваться
                // - может мы лучше цепочку собрем еще
                // тут нельзя НЕ банить - будет циклическая синхронизация с этим узлом
                int peersCount = ctrl.network.getActivePeersCounter(false, true);
                if (peersCount < Settings.getInstance().getMinConnections()) {
                    // ничего не делаем - пиров и так мало - синхримся с этого пира иначе будет нестабильная вся сеть
                    // догонять и откатываться все узлы начнут
                    ;
                } else if (peersCount > Settings.getInstance().getMaxConnections() - 3) {
                    // и так дофига пиров - можно и забанить
                    mess = "Dishonest peer by " + mess;
                    peer.ban(mess);
                    throw new Exception(mess);
                } else {
                    // пиров еще достаточно но заткнем ему рот - не будем по нему ориентироваться
                    peer.setMute(Controller.MUTE_PEER_COUNT);
                    return null;

                }
            }

            block.close();
            block = null;

            ///тут ключ по старому значению - просто так не получится найти orphanedTransactions.remove();
        }

        LOGGER.debug("*** END");
        return orphanedTransactions;

    }

    // process new BLOCKS to DB and orphan DB
    public void synchronizeNewBlocks(DCSet dcSet, Block lastCommonBlock, int checkPointHeight,
                                     List<Block> newBlocks, Peer peer) throws Exception {
        //Controller cnt = Controller.getInstance();

        Tuple2<Integer, Long> myHW = bchain.getHWeightFull(dcSet);

        /**
         * если в конце взведено - значит в момент слива какие-то таблицы не залились
         */
        boolean dbsBroken = false;

        // VERIFY ALL BLOCKS TO PREVENT ORPHANING INCORRECTLY
        DB database = DCSet.getHardBaseForFork();
        try (DCSet fork = dcSet.fork(database, "synchronizeNewBlocks")) {

            // освободим всю память
            dcSet.clearCache();
            if (ctrl.doesWalletKeysExists()) {
                try {
                    ctrl.getWallet().dwSet.clearCache();
                } catch (Exception ee) {
                }

            }

            ConcurrentHashMap<Long, Transaction> orphanedTransactions
                    = checkNewBlocks(myHW, fork, lastCommonBlock, checkPointHeight, newBlocks, peer);

            if (orphanedTransactions == null) {
                // это такая же как у нас цепочка - MUTE ее
                return;
            }

            // сюда может прити только если проверка прошла успешно

            // NEW BLOCKS ARE ALL VALID SO WE CAN ORPHAN THEM FOR REAL NOW

            // если проверка прошла успешно то значит Форк базы готов для слива в оснонвую
            // И без пересчета заново сделаем слив
            // Но сначала откаченные транзакции сохраним
            // соберем транзакции с блоков которые будут откачены в нашей цепочке

            if (ctrl.onlyProtocolIndexing) {
                // вторичные индексы НЕ нужны - можно быстрый слив из ФОРКА
                LOGGER.debug("*** TRY writeToParent");
                // теперь сливаем изменения
                dbsBroken = true; // если останется взведенным значит что-то не залилось правильно
                fork.writeToParent();
                dbsBroken = false;
            } else {

                // вторичные индексы нужны то нельзя быстрый просчет - иначе вторичные при сиве из форка не создадутся
                fork.close(); // закроем чтобы память освободить

                // NEW BLOCKS ARE ALL VALID SO WE CAN ORPHAN THEM FOR REAL NOW
                //// Map<String, byte[]> states = new TreeMap<String, byte[]>();

                // GET LAST BLOCK
                Block lastBlock = dcSet.getBlockMap().last();

                // ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK - in MAIN DB
                // ============ by EQUAL SIGNATURE !!!!!
                byte[] lastCommonBlockSignature = lastCommonBlock.getSignature();
                int countOrphanedTransactions = 0;
                while (!Arrays.equals(lastBlock.getSignature(), lastCommonBlockSignature)) {
                    if (ctrl.isOnStopping())
                        throw new Exception("on stopping");

                    // THROWN is new Better Peer
                    ctrl.checkNewBetterPeer(peer);

                    // ADD ORPHANED TRANSACTIONS
                    // orphanedTransactions.addAll(lastBlock.getTransactions());
                    for (Transaction transaction : lastBlock.getTransactions()) {
                        if (ctrl.isOnStopping())
                            throw new Exception("on stopping");
                        if (countOrphanedTransactions < MAX_ORPHAN_TRANSACTIONS_MY) {
                            countOrphanedTransactions++;
                            orphanedTransactions.put(Longs.fromByteArray(transaction.getSignature()), transaction);
                        }
                    }

                    LOGGER.debug("*** synchronize - orphanedTransactions.size:" + orphanedTransactions.size());
                    LOGGER.debug("*** synchronize - orphan block... " + dcSet.getBlockMap().size());

                    // так как выше мы запоминаем откаченные транзакции то тут их не будем сохранять в базу
                    this.pipeProcessOrOrphan(dcSet, lastBlock, true, false, true);

                    lastBlock.close();
                    lastBlock = dcSet.getBlockMap().last();

                }

                LOGGER.debug("*** chain size after orphan " + dcSet.getBlockMap().size());


                // PROCESS THE NEW BLOCKS
                LOGGER.debug("*** synchronize PROCESS NEW blocks.size:" + newBlocks.size());
                for (Block block : newBlocks) {

                    // THROWN is new Better Peer
                    ctrl.checkNewBetterPeer(peer);

                    if (ctrl.isOnStopping())
                        throw new Exception("on stopping");

                    if (dcSet.getBlockSignsMap().contains(block.getSignature())) {
                        LOGGER.error("*** add CHAIN - DUPLICATE SIGN! [" + block.getHeight() + "] "
                                + Base58.encode(block.getSignature())
                                + " from peer: " + peer);
                        continue;
                    }

                    // SYNCHRONIZED PROCESSING
                    LOGGER.debug("*** begin PIPE");

                    this.pipeProcessOrOrphan(dcSet, block, false, false, false);

                    LOGGER.debug("*** begin REMOVE orphanedTransactions");
                    for (Transaction transaction : block.getTransactions()) {
                        if (ctrl.isOnStopping())
                            throw new Exception("on stopping");

                        Long key = Longs.fromByteArray(transaction.getSignature());
                        if (orphanedTransactions.containsKey(key))
                            orphanedTransactions.remove(key);
                    }

                    block.close();
                }
            }

            // теперь все транзакции в пул опять закидываем
            for (Transaction transaction : orphanedTransactions.values()) {
                if (ctrl.isOnStopping())
                    throw new Exception("on stopping");

                ctrl.transactionsPool.offerMessage(transaction);
            }
        } finally {
            LOGGER.debug("*** END writeToParent");
        }

        if (dbsBroken) {
            // TODO: нужно откатить чтоли все или там и так атомарно - но у кадой таблицы своя атомарность ((
        }

    }

    boolean test2;
    public void synchronize(DCSet dcSet, int checkPointHeight, Peer peer, int peerHeight, byte[] lastCommonBlockSignature_in) throws Exception {

        try {
            fromPeer = peer;

            boolean isFromTrustedPeer = bchain.isPeerTrusted(peer);

            if (ctrl.isOnStopping())
                throw new Exception("on stopping");

            /*
             * logger.error("Synchronizing from peer: " + peer.toString() + ":" +
             * peer);
             */

            // освободим HEAP и память - нам не нужна она все равно
            dcSet.clearCache();

            fromPeer = peer;

            byte[] lastBlockSignature = dcSet.getBlockMap().getLastBlockSignature();

            // FIND HEADERS for common CHAIN
            if (true || Arrays.equals(peer.getAddress().getAddress(), PEER_TEST)) {
                LOGGER.info("Synchronizing from peer: " + peer.toString() + ":" + peer
                        + " my HEIGHT: " + dcSet.getBlocksHeadsMap().size());
            }

            byte[] lastCommonBlockSignature;
            List<byte[]> signatures;
            if (lastCommonBlockSignature_in == null) {
                Tuple2<byte[], List<byte[]>> headers = this.findHeaders(peer, peerHeight, lastBlockSignature, checkPointHeight);
                lastCommonBlockSignature = headers.a;
                signatures = headers.b;
            } else {
                // уже задана точка отката - тест
                lastCommonBlockSignature = lastCommonBlockSignature_in;
                signatures = this.getBlockSignatures(lastCommonBlockSignature, peer);
                signatures.remove(0);
            }

            if (lastCommonBlockSignature == null) {

                if (test2) {
                    throw new Exception("STOP on DEBUG");
                }

                // simple ACCEPT tail CHAIN - MY LAST block founded in PEER
                if (signatures == null || signatures.isEmpty())
                    return;

                // CREATE BLOCK BUFFER
                LOGGER.debug(
                        "START BUFFER" + " peer: " + peer + " for blocks: " + signatures.size());

                BlockBuffer blockBuffer = new BlockBuffer(signatures, peer);

                Block blockFromPeer = null;
                String errorMess = null;
                int banTime = BAN_BLOCK_TIMES >> 2;

                try {

                    // GET AND PROCESS BLOCK BY BLOCK
                    for (byte[] signature : signatures) {
                        if (ctrl.isOnStopping()) {
                            // STOP BLOCKBUFFER
                            blockBuffer.stopThread();
                            throw new Exception("on stopping");
                        }

                        // THROWN is new Better Peer
                        ctrl.checkNewBetterPeer(peer);

                        // GET BLOCK
                        LOGGER.debug("try get BLOCK from BUFFER");

                        try {
                            blockFromPeer = blockBuffer.getBlock(signature);
                            if (isFromTrustedPeer) {
                                blockFromPeer.setFromTrustedPeer();
                            }
                        } catch (Exception e) {
                            if (ctrl.isOnStopping()) {
                                throw new Exception("on stopping");
                            }

                            blockBuffer.stopThread();
                            peer.ban("get block BUFFER - " + e.getMessage());
                            throw new Exception(e);
                        }

                        if (blockFromPeer == null) {

                            // INVALID BLOCK THROW EXCEPTION
                            errorMess = "Dishonest peer on block null";
                            banTime = BAN_BLOCK_TIMES >> 2;
                            break;
                        }

                        if (ctrl.isOnStopping()) {
                            // STOP BLOCKBUFFER
                            blockBuffer.stopThread();
                            throw new Exception("on stopping");
                        }

                        if (ctrl.isOnStopping()) {
                            // STOP BLOCKBUFFER
                            blockBuffer.stopThread();
                            throw new Exception("on stopping");
                        }

                        if (blockFromPeer.isFromTrustedPeer()) {
                            // нужно все равно просчитать заголовок блока
                            int invalid = blockFromPeer.isValidHead(dcSet);
                            if (invalid > 0) {
                                // все же может не просчитаться высота блока м цель его из-за ошибки валидации
                                // поэтому делаем проверку все равно
                                // INVALID BLOCK THROW EXCEPTION
                                errorMess = "Dishonest peer by not is Valid block";
                                if (invalid > Block.INVALID_BRANCH) {
                                    banTime = BAN_BLOCK_TIMES << 1;
                                } else {
                                    banTime = 0;
                                }
                                break;
                            }
                            LOGGER.debug("*** checkNewBlocks - not VALIDATE from trusted PEER");
                        } else {
                            // если это не довернный узел то полная проверка
                            if (!blockFromPeer.isSignatureValid()) {
                                errorMess = "invalid Sign!";
                                banTime = BAN_BLOCK_TIMES << 1;
                                break;
                            }
                            LOGGER.debug("BLOCK Signature is Valid");

                            errorMess = bchain.blockFromFuture(blockFromPeer.heightBlock);
                            if (errorMess != null) {
                                break;
                            }

                            try {
                                // тут может парсинг транзакций упасть
                                blockFromPeer.getTransactions();
                            } catch (Exception e) {
                                if (ctrl.isOnStopping()) {
                                    throw new Exception("on stopping");
                                }

                                errorMess = "invalid PARSE! " + e.getMessage();
                                LOGGER.debug(errorMess, e);
                                banTime = BAN_BLOCK_TIMES << 1;
                                if (BlockChain.CHECK_BUGS > 9) {
                                    ctrl.stopAndExit(339);
                                }
                                break;
                            } catch (Throwable e) {
                                errorMess = "invalid PARSE! " + e.getMessage();
                                LOGGER.debug(errorMess, e);
                                banTime = BAN_BLOCK_TIMES << 1;
                                ctrl.stopAndExit(339);
                                break;
                            }

                            try (DCSet fork = dcSet.fork(DCSet.makeDBinMemory(), "synchronize")) {
                                if (blockFromPeer.isValid(fork, false) > 0) {

                                    errorMess = "invalid BLOCK";
                                    banTime = BAN_BLOCK_TIMES;
                                    break;
                                }
                            } catch (java.lang.OutOfMemoryError e) {
                                errorMess = "error io isValid! [" + blockFromPeer.heightBlock + "] " + e.getMessage();
                                LOGGER.debug(errorMess, e);
                                banTime = BAN_BLOCK_TIMES;
                                ctrl.stopAndExit(340);
                                break;
                            } catch (Exception e) {

                                if (ctrl.isOnStopping()) {
                                    throw new Exception("on stopping");
                                }

                                errorMess = "error on isValid! [" + blockFromPeer.heightBlock + "] " + e.getMessage();
                                LOGGER.debug(errorMess, e);
                                banTime = BAN_BLOCK_TIMES;
                                if (BlockChain.CHECK_BUGS > 9) {
                                    ctrl.stopAndExit(340);
                                }
                                break;
                            } catch (Throwable e) {
                                errorMess = "error io isValid! [" + blockFromPeer.heightBlock + "] " + e.getMessage();
                                LOGGER.debug(errorMess, e);
                                banTime = BAN_BLOCK_TIMES;
                                ctrl.stopAndExit(341);
                                break;
                            }
                            LOGGER.debug("BLOCK is Valid");
                        }

                        if (ctrl.isOnStopping()) {
                            blockBuffer.stopThread();
                            throw new Exception("on stopping");
                        }

                        try {
                            // PROCESS BLOCK

                            LOGGER.debug("try PROCESS");
                            this.pipeProcessOrOrphan(dcSet, blockFromPeer, false, false, false);

                            LOGGER.debug("synchronize BLOCK END process");
                            blockBuffer.clearBlock(blockFromPeer.getSignature());
                            LOGGER.debug("synchronize clear from BLOCK BUFFER");
                            continue;

                        } catch (Exception e) {

                            // STOP BLOCKBUFFER
                            blockBuffer.stopThread();

                            if (ctrl.isOnStopping()) {
                                throw new Exception("on stopping");
                            } else {
                                throw new Exception(e);
                            }
                        } catch (Throwable e) {

                            // STOP BLOCKBUFFER
                            blockBuffer.stopThread();

                            LOGGER.error(e.getMessage(), e);
                            ctrl.stopAndExit(343);

                        } finally {
                            blockFromPeer.close();
                        }

                    }

                } finally {
                    // STOP BLOCKBUFFER
                    blockBuffer.stopThread();
                }

                if (errorMess != null) {
                    // INVALID BLOCK THROW EXCEPTION
                    String mess = "Dishonest peer on SYNCHRONIZE block " + errorMess;
                    peer.ban(banTime, mess);
                    throw new Exception(mess);
                }

                if (ctrl.isOnStopping()) {
                    throw new Exception("on stopping");
                }

                LOGGER.debug(
                        "STOP BUFFER" + " peer: " + peer + " for blocks: " + signatures.size());


            } else {

                // GET THE BLOCKS FROM SIGNATURES
                List<Block> blocks = this.getBlocks(dcSet, signatures, peer);

                if (ctrl.isOnStopping()) {
                    throw new Exception("on stopping");
                }

                Block lastCommonBlock = dcSet.getBlockSignsMap().getBlock(lastCommonBlockSignature);

                // SYNCHRONIZE BLOCKS
                LOGGER.info("synchronize with ORPHAN from common block [" + lastCommonBlock.getHeight()
                        + "] for blocks: " + blocks.size());
                synchronizeNewBlocks(dcSet, lastCommonBlock, checkPointHeight, blocks, peer);

                if (ctrl.isOnStopping()) {
                    throw new Exception("on stopping");
                }
            }
        } finally {
            this.fromPeer = null;
        }

    }

    public List<byte[]> getBlockSignatures(byte[] header, Peer peer) throws Exception {

        /// CREATE MESSAGE
        Message message = MessageFactory.getInstance().createGetHeadersMessage(header);

        // SEND MESSAGE TO PEER
        // see response callback in controller.Controller.onMessage(Message)
        // type = GET_SIGNATURES_TYPE
        SignaturesMessage response;

        int timeSOT;
        if (peer.network.getActivePeers(false).size() < 3) {
            // тут может очень большой файл в блоке - и будет разрывать связь со всеми - дадим ему пройти
            timeSOT = 600000;
        } else {
            timeSOT = GET_HEADERS_TIMEOUT;
        }
        try {
            response = (SignaturesMessage) peer.getResponse(message, timeSOT);
        } catch (Exception e) {
            peer.ban("Cannot retrieve headers, error SOT: " + timeSOT + " " + e.getMessage());
            throw new Exception("Failed to communicate with peer (retrieve headers) - response = null");
        }

        if (response == null) {
            peer.ban("Cannot retrieve headers =null, SOT: " + timeSOT);
            throw new Exception("Failed to communicate with peer (retrieve headers) - response = null");
        }

        return response.getSignatures();
    }

    static byte[] badCheck = null; // Base58.decode("5SxUG...");
    public void checkBadBlock(Peer peer) throws Exception {

        if (false && BlockChain.DEMO_MODE) {
            // TODO тут только в Девелопе такой блок - если убьем то удалить эту проверку
            List<byte[]> headersCheck = this.getBlockSignatures(badCheck, peer);
            if (!headersCheck.isEmpty()) {
                String mess = "Dishonest peer: my CHECKPOINT SIGNATURE -> BAD FORK";
                peer.ban(BAN_BLOCK_TIMES, mess);
                throw new Exception(mess);
            }
        }
    }

    private Tuple2<byte[], List<byte[]>> findHeaders(Peer peer, int peerHeight, byte[] lastBlockSignature,
                                                     int checkPointHeight) throws Exception {

        DCSet dcSet = DCSet.getInstance();

        LOGGER.info("findHeaders(Peer: " + peer + ", peerHeight: " + peerHeight
                + ", checkPointHeight: " + checkPointHeight);

        List<byte[]> headers = this.getBlockSignatures(lastBlockSignature, peer);

        LOGGER.info("findHeaders(Peer) headers.size: " + headers.size());

        int headersSize = headers.size();

        if (headersSize > 0) {
            // MY LAST BLOCK IS FOUNDED in PEER
            do {
                headers.remove(0);
            } while (headers.size() > 0 && dcSet.getBlockSignsMap().contains(headers.get(0)));

            if (headers.isEmpty()) {
                peer.setMute(Controller.MUTE_PEER_COUNT);

                String mess = "Peer is SAME as me";
                LOGGER.debug(peer + " " + mess);
                return new Tuple2<byte[], List<byte[]>>(null, null);
            }

            // null - not need orphan my CHAIN
            return new Tuple2<byte[], List<byte[]>>(null, headers);

        }

        byte[] checkPointSign = dcSet.getBlocksHeadsMap().get(checkPointHeight).signature;

        List<byte[]> headersCheck = this.getBlockSignatures(checkPointSign, peer);
        if (headersCheck.isEmpty()) {
            String mess = "Dishonest peer: my CHECKPOINT SIGNATURE -> not found";
            peer.ban(BAN_BLOCK_TIMES, mess);
            throw new Exception(mess);
        }

        final int myChainHeight = dcSet.getBlockMap().size();
        if (myChainHeight < checkPointHeight) {
            String mess = "Dishonest peer: my checkPointHeight[" + checkPointHeight + "\n -> not found";
            peer.ban(BAN_BLOCK_TIMES, mess);
            throw new Exception(mess);
        }

        LOGGER.info("findHeaders maxChainHeight: " + myChainHeight + " to minHeight: " + checkPointHeight);

        // GET HEADERS UNTIL COMMON BLOCK IS FOUND OR ALL BLOCKS HAVE BEEN
        // CHECKED
        byte[] lastCommonBlockSignature;
        int step = 2;
        int currentCheckChainHeight = myChainHeight;
        do {
            if (ctrl.isOnStopping()) {
                throw new Exception("on stopping");
            }

            currentCheckChainHeight -= step;

            if (currentCheckChainHeight < checkPointHeight) {
                currentCheckChainHeight = checkPointHeight;
                lastCommonBlockSignature = checkPointSign;
            } else {
                lastCommonBlockSignature = dcSet.getBlocksHeadsMap().get(currentCheckChainHeight).signature;
            }

            LOGGER.debug(
                    "findHeaders try found COMMON header" + " step: " + step + " currentMaxChainHeight: " + currentCheckChainHeight);

            headers = this.getBlockSignatures(lastCommonBlockSignature, peer);

            LOGGER.debug("findHeaders try found COMMON header" + " founded headers: " + headers.size());

            if (headers.size() > 1) {
                if (currentCheckChainHeight < checkPointHeight) {
                    String mess = "Dishonest peer by currentMaxChainHeight < checkPointHeight " + peer;
                    peer.ban(BAN_BLOCK_TIMES, mess);
                    throw new Exception(mess);
                }
                break;
            }

            if (step < 10000)
                step <<= 1;

        } while (currentCheckChainHeight > checkPointHeight && headers.isEmpty());

        LOGGER.info("findHeaders AFTER try found COMMON header" + " founded headers: " + headers.size());

        // CLEAR head of common headers exclude LAST!
        while (headers.size() > 1 && dcSet.getBlockSignsMap().contains(headers.get(0))) {
            lastCommonBlockSignature = headers.remove(0);
        }

        /**
         * Нам не нужно тут большую цепочку брать так как с откатом будет проверка блоков сначала и это может занять
         * слишком много времени - так что сначала синхронизируемся до ближайшего верхнего + 2
         * Чтобы проверить правильность и силу цепочки/
         */
        int commonBockHeight = dcSet.getBlockSignsMap().get(lastCommonBlockSignature);
        // Так же дальше будет проверка на силу цепочки - поэтому надо 3 блока добавить
        int needChainLenght = 3 + myChainHeight - commonBockHeight;
        if (headers.size() > needChainLenght) {
            headers = headers.subList(0, needChainLenght);
        }

        LOGGER.info("findHeaders headers CLEAR" + "now headers: " + headers.size());

        return new Tuple2<byte[], List<byte[]>>(lastCommonBlockSignature, headers);

    }

    private List<Block> getBlocks(DCSet dcSet, List<byte[]> signatures, Peer peer) throws Exception {

        LOGGER.debug("try get BLOCKS from common block SIZE:" + signatures.size() + " - " + peer);

        List<Block> blocks = new ArrayList<Block>();

        int bytesGet = 0;
        for (byte[] signature : signatures) {
            if (ctrl.isOnStopping()) {
                throw new Exception("on stopping");
            }

            // ADD TO LIST
            Block block = getBlock(signature, peer, true);
            if (block == null)
                break;

            blocks.add(block);
            bytesGet += 1500 + block.getDataLength(false);
            if (bytesGet > BYTES_MAX_GET) {
                break;
            }
        }

        return blocks;
    }

    /**
     * @param dcSet
     * @param block
     * @param doOrphan
     * @param hardFlush
     * @param notStoreTXs
     * @throws Exception
     */
    // SYNCHRONIZED DO NOT PROCCESS A BLOCK AT THE SAME TIME
    // SYNCHRONIZED MIGHT HAVE BEEN PROCESSING PREVIOUS BLOCK
    public synchronized void pipeProcessOrOrphan(DCSet dcSet, Block block, boolean doOrphan, boolean hardFlush,
                                                 boolean notStoreTXs)
            throws Exception {

        // CHECK IF WE ARE STILL PROCESSING BLOCKS
        if (ctrl.isOnStopping()) {
            throw new Exception("on stopping");
        }

        long processTiming = System.nanoTime();
        int txCount = block.getTransactionCount();

        dcSet.getBlockMap().setProcessing(true);
        boolean observOn = ctrl.doesWalletKeysExists() && ctrl.useGui;
        Integer countObserv_ADD = null;
        Integer countObserv_REMOVE = null;

        Exception error = null;
        Throwable thrown = null;
        boolean nedToCloseBlock = true;

        // нужно закрыть в любом случае блок по выходу так как он уже отыграл
        // и тем более если там был внутри ФоркБазы - ее надо закрыть
        // иначе при догонянии цепочки несколькими нодами - частые откаты и т.д. и Форки не закрываются иногда
        try {
            if (doOrphan) {

                try {
                    block.orphan(dcSet, notStoreTXs);
                    dcSet.getBlockMap().setProcessing(false);
                    // FARDFLUSH not use in each case - only after accumulate size
                    dcSet.flush(512 + block.blockHead.transactionsCount * 512 + block.blockHead.size << 1, false, doOrphan);

                    if (ctrl.isOnStopping())
                        return;

                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    error = new Exception(e);

                } catch (Exception e) {

                    if (ctrl.isOnStopping()) {
                        return;
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        error = new Exception(e);
                    }
                } catch (Throwable e) {
                    if (ctrl.isOnStopping()) {
                        return;
                    } else {
                        thrown = new Throwable(e);
                    }
                } finally {

                    if (ctrl.isOnStopping()) {
                        // was BREAK - try ROLLBACK
                        dcSet.rollback();
                        throw new Exception("on stopping");
                    }

                    if (error != null) {
                        // was BREAK - try ROLLBACK
                        try {
                            // was BREAK - try ROLLBACK
                            dcSet.rollback();
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                            ctrl.stopAndExit(322);
                            return;
                        } catch (Throwable e) {
                            LOGGER.error(e.getMessage(), e);
                            ctrl.stopAndExit(327);
                            return;
                        }

                        throw error;

                    } else if (thrown != null) {

                        LOGGER.error(thrown.getMessage(), thrown);

                        try {
                            // was BREAK - try ROLLBACK
                            dcSet.rollback();
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                            ctrl.stopAndExit(324);
                            return;
                        } catch (Throwable e) {
                            LOGGER.error(e.getMessage(), e);
                            ctrl.stopAndExit(337);
                            return;
                        }

                        ctrl.stopAndExit(335);

                    }

                    if (observOn) {

                        if (countObserv_ADD != null) {
                            try {
                                dcSet.getTransactionTab().setObservableData(DBTab.NOTIFY_ADD, countObserv_ADD);
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }

                        try {
                            dcSet.getBlockMap().notifyOrphanChain(block);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }

                }

            } else {

                // PROCESS
                try {
                    if (block.hasValidatedForkDB()) {
                        block.saveToChainFromvalidatedForkDB();
                    } else {
                        block.process(dcSet, false);
                    }

                    dcSet.getBlockMap().setProcessing(false);

                    // FLUSH not use in each case - only after accumulate size
                    dcSet.flush(512 + block.blockHead.transactionsCount * 512 + block.blockHead.size << 1, false, doOrphan);

                    if (ctrl.isOnStopping())
                        return;

                    if (!ctrl.isStatusSynchronizing() && Settings.getInstance().getNotifyIncomingConfirmations() > 0) {
                        ctrl.NotifyWalletIncoming(block.getTransactions());
                    }

                    if (ctrl.isOnStopping())
                        return;

                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    error = new Exception(e);

                } catch (Exception e) {

                    if (ctrl.isOnStopping()) {
                        return;
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        error = new Exception(e);
                    }
                } catch (Throwable e) {
                    if (ctrl.isOnStopping()) {
                        return;
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        thrown = new Throwable(e);
                    }
                } finally {

                    if (ctrl.isOnStopping()) {
                        // was BREAK - try ROLLBACK
                        dcSet.rollback();
                        throw new Exception("on stopping");
                    }

                    if (error != null) {

                        try {
                            // was BREAK - try ROLLBACK
                            dcSet.rollback();
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                            ctrl.stopAndExit(346);
                            return;
                        } catch (Throwable e) {
                            LOGGER.error(e.getMessage(), e);
                            ctrl.stopAndExit(347);
                            return;
                        }

                        throw error;

                    } else if (thrown != null) {

                        try {
                            // was BREAK - try ROLLBACK
                            dcSet.rollback();
                            ctrl.stopAndExit(351);
                            return;
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                            ctrl.stopAndExit(352);
                            return;
                        } catch (Throwable e) {
                            LOGGER.error(e.getMessage(), e);
                            ctrl.stopAndExit(353);
                            return;
                        }

                    }

                    // NOTIFY to WALLET

                    if (observOn) {

                        if (countObserv_REMOVE != null) {
                            try {
                                dcSet.getTransactionTab().setObservableData(DBTab.NOTIFY_REMOVE, countObserv_REMOVE);
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }

                        try {
                            dcSet.getBlockMap().notifyProcessChain(block);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }

                    }
                }
            }

            if (!dcSet.isFork()) {
                // только запись в нашу цепочку

                if (ctrl.doesWalletKeysExists() && !ctrl.noDataWallet && ctrl.getWallet().walletUpdater != null) {
                    nedToCloseBlock = false; // не надо закрывать - он еще в очереди на обработку в кошельке томится - там закроют
                    ctrl.getWallet().walletUpdater.offerMessage(new Pair(doOrphan, block));
                }

                processTiming = System.nanoTime() - processTiming;
                if (processTiming < 999999999999L) {
                    // при переполнении может быть минус
                    // в миеросекундах подсчет делаем
                    bchain.updateTXProcessTimingAverage(processTiming, txCount);

                    LOGGER.debug("PROCESS SPEED for: " + txCount + "tx is :" + (txCount + Controller.BLOCK_AS_TX_COUNT) * 1000000000L
                            / processTiming
                            + " tx/s");
                }
            }

        } finally {
            /// иногда нельзя блок закрывать - вдруг он в кошелек пошел на обработку и если в этот момент закроется? там то по очереди
            if (nedToCloseBlock)
                block.close();
        }

    }

    /**
     * проверка отставания от сети по силе узлов рядом
     */
    public void run() {

        if (ctrl.network == null)
            return;

        long timeTmp;
        long timePoint = 0;
        BlockGenerator blockGenerator;

        int blockTime = BlockChain.GENERATING_MIN_BLOCK_TIME_MS(BlockChain.VERS_30SEC + 1);
        long shiftPoint = blockTime + (blockTime >> 1) - (blockTime >> 2);
        // INIT wait START
        do {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                return;
            }

            blockGenerator = ctrl.getBlockGenerator();
        } while (blockGenerator == null);

        boolean needCheck = false;

        DCSet dcSet = DCSet.getInstance();
        while (!ctrl.isOnStopping()) {

            try {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }

                timeTmp = bchain.getTimestamp(dcSet) + shiftPoint;
                if (timeTmp > NTP.getTime())
                    continue;

                if (timeTmp + (blockTime << 1) < NTP.getTime()) {
                    // мы встали - проверяем в любом случае
                    needCheck = true;
                    try {
                        // подождем чуток еще иначе очень часто будет опрос
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        break;
                    }
                } else {

                    if (timePoint == timeTmp)
                        // новый блок не прилетал - ждем
                        continue;

                    // иначе просиходит сброс и синхронизация новая
                    if (blockGenerator.getForgingStatus() == BlockGenerator.ForgingStatus.FORGING_WAIT)
                        continue;
                }
                timePoint = timeTmp;


                // снизим ожижание блокировки с "сильных но таких же как мы" узлов
                ctrl.network.decrementWeightOfPeerMutes();

                if (!ctrl.isStatusOK())
                    continue;


                if (needCheck || BlockChain.CHECK_PEERS_WEIGHT_AFTER_BLOCKS < 2) {
                    // проверим силу других цепочек - и если есть сильнее то сделаем откат у себя так чтобы к ней примкнуть
                    needCheck = true;
                } else {
                    Tuple2<Integer, Long> myHW = bchain.getHWeightFull(dcSet);
                    if (myHW.a % BlockChain.CHECK_PEERS_WEIGHT_AFTER_BLOCKS == 0) {
                        // проверим силу других цепочек - и если есть сильнее то сделаем откат у себя так чтобы к ней примкнуть
                        needCheck = true;
                    }
                }

                if (needCheck) {
                    LOGGER.debug("try CHECK BETTER CHAIN PEER");
                    needCheck = false;
                    if (blockGenerator.checkWeightPeers()) {
                        LOGGER.debug("FOUND BETTER CHAIN PEER " + blockGenerator.betterPeer);
                    }
                }


            } catch (OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                ctrl.stopAndExit(396);
                return;
            } catch (IllegalMonitorStateException e) {
                break;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

        LOGGER.info("halted");
    }

}
