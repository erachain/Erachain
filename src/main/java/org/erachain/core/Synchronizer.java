package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.DBMap;
import org.erachain.datachain.BlockMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionMap;
import org.erachain.network.Peer;
import org.erachain.network.message.BlockMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.network.message.SignaturesMessage;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * функционал скачки цепочки с других узлов - догоняние сети
 */
public class Synchronizer {

    public static final int GET_BLOCK_TIMEOUT = BlockChain.GENERATING_MIN_BLOCK_TIME_MS >> 3;
    public static final int GET_HEADERS_TIMEOUT = GET_BLOCK_TIMEOUT;
    private static final int BYTES_MAX_GET = 1024 << 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(Synchronizer.class);
    private static final byte[] PEER_TEST = new byte[]{(byte) 185, (byte) 195, (byte) 26, (byte) 245}; // 185.195.26.245
    public static int BAN_BLOCK_TIMES = 8 * BlockChain.GENERATING_MIN_BLOCK_TIME / 60;
    private static int MAX_ORPHAN_TRANSACTIONS = BlockChain.DEVELOP_USE? 200000: 50000;
    // private boolean run = true;
    // private Block runedBlock;
    private Peer fromPeer;

    public Synchronizer() {
        // this.run = true;
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
            int banTime = BAN_BLOCK_TIMES;
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

    private void checkNewBlocks(Tuple2<Integer, Long> myHW, DCSet fork, Block lastCommonBlock, int checkPointHeight,
                                List<Block> newBlocks, Peer peer) throws Exception {

        LOGGER.debug("*** core.Synchronizer.checkNewBlocks - START");

        Controller cnt = Controller.getInstance();
        BlockMap blockMap = fork.getBlockMap();

        // ORPHAN BLOCK IN FORK TO VALIDATE THE NEW BLOCKS

        // GET LAST BLOCK
        Block lastBlock = blockMap.last();

        int lastHeight = lastBlock.getHeight();
        LOGGER.debug("*** core.Synchronizer.checkNewBlocks - lastBlock[" + lastHeight + "]\n" + " newBlocks.size = "
                + newBlocks.size() + "\n search common block in FORK" + " in mainDB: "
                + lastBlock.getHeight() + " in ForkDB: " + lastBlock.getHeight()
                + "\n for last CommonBlock = " + lastCommonBlock.getHeight());

        int countTransactionToOrphan = 0;
        // ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK - in FORK DB
        // ============ by EQUAL SIGNATURE !!!!!
        byte[] lastCommonBlockSignature = lastCommonBlock.getSignature();
        while (!Arrays.equals(lastBlock.getSignature(), lastCommonBlockSignature)) {
            LOGGER.debug("*** ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK ["
                    + lastBlock.getHeight() + "]");
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
            // LOGGER.debug("*** core.Synchronizer.checkNewBlocks - try orphan:
            // " + lastBlock.getHeight(fork));
            if (cnt.isOnStopping())
                throw new Exception("on stopping");

            int height = lastBlock.getHeight();

            fork.getTransactionMap().clearByDeadTimeAndLimit(
                    cnt.getBlockChain().getTimestamp(height), false);

            // проверим на переполнение откаченных трнзакций
            if (countTransactionToOrphan > MAX_ORPHAN_TRANSACTIONS) {
                String mess = "Dishonest peer by on lastCommonBlock[" + lastCommonBlock.getHeight()
                        + "] - reached MAX_ORPHAN_TRANSACTIONS: " + MAX_ORPHAN_TRANSACTIONS;
                peer.ban(BAN_BLOCK_TIMES >> 2, mess);
                throw new Exception(mess);
            }

            int bbb = fork.getBlockMap().size();
            int hhh = fork.getBlocksHeadsMap().size();
            int sss = fork.getBlockSignsMap().size();
            assert (height == hhh);
            assert (bbb == hhh);
            assert (sss == hhh);

            // runedBlock = lastBlock; // FOR quick STOPPING
            countTransactionToOrphan += lastBlock.getTransactionCount();
            lastBlock.orphan(fork);

            int height2 = lastBlock.getHeight();
            int bbb2 = fork.getBlockMap().size();
            int hhh2 = fork.getBlocksHeadsMap().size();
            int sss2 = fork.getBlockSignsMap().size();
            assert (height2 == hhh2);
            assert (bbb2 == hhh2);
            assert (sss2 == hhh2);

            LOGGER.debug("*** core.Synchronizer.checkNewBlocks - orphaned! chain size: " + fork.getBlockMap().size());
            lastBlock = blockMap.last();
        }

        LOGGER.debug("*** core.Synchronizer.checkNewBlocks - lastBlock[" + lastBlock.getHeight() + "]");

        // VALIDATE THE NEW BLOCKS

        // Height & Weight
        int testHeight = myHW.a + 1; // высота на котрой тестировать СИЛУ цепочки
        long myWeight = myHW.b;
        int newHeight = lastBlock.getHeight() + newBlocks.size();
        // проверять СИЛУ цепочки только если лна не на много лучше моей высоты
        boolean checkFullWeight = testHeight + 2 > newHeight;

        LOGGER.debug("*** checkNewBlocks - VALIDATE THE NEW BLOCKS in FORK");

        for (Block block : newBlocks) {
            int height = block.getHeight();
            int bbb = fork.getBlockMap().size();
            int hhh = fork.getBlocksHeadsMap().size();
            int sss = fork.getBlockSignsMap().size();
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
                    peer.ban(BAN_BLOCK_TIMES >> 3, mess);
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

            LOGGER.debug("*** checkNewBlocks - VALIDATE [" + height + "]");

            // CHECK IF VALID
            if (!block.isSignatureValid()) {
                // INVALID BLOCK THROW EXCEPTION
                String mess = "Dishonest peer by not is Valid block, heigh: " + height;
                peer.ban(BAN_BLOCK_TIMES, mess);
                throw new Exception(mess);
            }

            try {
                block.getTransactions();
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
                String mess = "Dishonest peer error block.getTransactions PARSE: " + height;
                peer.ban(BAN_BLOCK_TIMES << 2, mess);
                throw new Exception(mess);
            }

            if (!block.isValid(fork, true)) {
                // INVALID BLOCK THROW EXCEPTION
                String mess = "Dishonest peer by not is Valid block, heigh: " + height;
                peer.ban(BAN_BLOCK_TIMES, mess);
                throw new Exception(mess);
            }

            // PROCESS TO VALIDATE NEXT BLOCKS
            // runedBlock = block;
            /// already in Validate block.process(fork);
            if (checkFullWeight && testHeight == height) {
                if (myWeight >= fork.getBlocksHeadsMap().getFullWeight()) {
                    // суть в том что тут цепоска на этой высоте слабже моей,
                    // поэтому мы ее пока забаним чтобы с ней постоянно не синхронизироваться
                    // - может мы лучше цепочку собрем еще

                    // INVALID BLOCK THROW EXCEPTION
                    String mess = "Dishonest peer by weak FullWeight, heigh: " + height;
                    peer.ban(BAN_BLOCK_TIMES >> 3, mess);
                    throw new Exception(mess);

                }

            }

        }

        LOGGER.debug("*** core.Synchronizer.checkNewBlocks - END");

    }

    // process new BLOCKS to DB and orphan DB
    public List<Transaction> synchronize_blocks(DCSet dcSet, Block lastCommonBlock, int checkPointHeight,
                                                List<Block> newBlocks, Peer peer) throws Exception {
        TreeMap<String, Transaction> orphanedTransactions = new TreeMap<String, Transaction>();
        Controller cnt = Controller.getInstance();

        Tuple2<Integer, Long> myHW = cnt.getBlockChain().getHWeightFull(dcSet);

        DCSet fork;
        // VERIFY ALL BLOCKS TO PREVENT ORPHANING INCORRECTLY
        if (BlockGenerator.TEST_001) {
            /// checkNewBlocks(dcSet.forkinFile(), lastCommonBlock, newBlocks,
            /// peer);
            fork = dcSet.fork();
            checkNewBlocks(myHW, fork, lastCommonBlock, checkPointHeight, newBlocks, peer);
            fork.close();
        } else {
            fork = dcSet.fork();
            checkNewBlocks(myHW, fork, lastCommonBlock, checkPointHeight, newBlocks, peer);
            fork.close();
        }

        // NEW BLOCKS ARE ALL VALID SO WE CAN ORPHAN THEM FOR REAL NOW
        //// Map<String, byte[]> states = new TreeMap<String, byte[]>();

        // GET LAST BLOCK
        Block lastBlock = dcSet.getBlockMap().last();

        // ORPHAN LAST BLOCK UNTIL WE HAVE REACHED COMMON BLOCK - in MAIN DB
        // ============ by EQUAL SIGNATURE !!!!!
        byte[] lastCommonBlockSignature = lastCommonBlock.getSignature();
        while (!Arrays.equals(lastBlock.getSignature(), lastCommonBlockSignature)) {
            if (cnt.isOnStopping())
                throw new Exception("on stopping");

            // ADD ORPHANED TRANSACTIONS
            // orphanedTransactions.addAll(lastBlock.getTransactions());
            for (Transaction transaction : lastBlock.getTransactions()) {
                if (cnt.isOnStopping())
                    throw new Exception("on stopping");
                orphanedTransactions.put(new BigInteger(1, transaction.getSignature()).toString(16), transaction);
            }
            LOGGER.debug("*** synchronize - orphanedTransactions.size:" + orphanedTransactions.size());
            LOGGER.debug("*** synchronize - orphan block... " + dcSet.getBlockMap().size());
            this.pipeProcessOrOrphan(dcSet, lastBlock, true, false);

            lastBlock = dcSet.getBlockMap().last();
        }

        LOGGER.debug("*** chain size after orphan " + dcSet.getBlockMap().size());


        // PROCESS THE NEW BLOCKS
        LOGGER.debug("*** synchronize PROCESS NEW blocks.size:" + newBlocks.size());
        for (Block block : newBlocks) {

            if (cnt.isOnStopping())
                throw new Exception("on stopping");

            if (dcSet.getBlockSignsMap().contains(block.getSignature())) {
                LOGGER.error("*** add CHAIN - DUPLICATE SIGN! [" + block.getHeight() + "] "
                        + Base58.encode(block.getSignature())
                        + " from peer: " + peer);
                continue;
            }

            // SYNCHRONIZED PROCESSING
            LOGGER.debug("*** begin PIPE");
            this.pipeProcessOrOrphan(dcSet, block, false, false);

            LOGGER.debug("*** begin REMOVE orphanedTransactions");
            for (Transaction transaction : block.getTransactions()) {
                if (cnt.isOnStopping())
                    throw new Exception("on stopping");

                String key = new BigInteger(1, transaction.getSignature()).toString(16);
                if (orphanedTransactions.containsKey(key))
                    orphanedTransactions.remove(key);
            }

        }

        // CLEAR for DEADs
        TransactionMap map = dcSet.getTransactionMap();
        List<Transaction> orphanedTransactionsList = new ArrayList<Transaction>();
        for (Transaction transaction : orphanedTransactions.values()) {
            if (cnt.isOnStopping())
                throw new Exception("on stopping");

            // CHECK IF DEADLINE PASSED
            if (!map.contains(transaction.getSignature())) {
                orphanedTransactionsList.add(transaction);
            }
        }

        return orphanedTransactionsList;
    }

    /*
     * private List<byte[]> getBlockSignatures(Block start, int amount, Peer
     * peer) throws Exception { //ASK NEXT 500 HEADERS SINCE START byte[]
     * startSignature = start.getSignature(); List<byte[]> headers =
     * this.getBlockSignatures(startSignature, peer); List<byte[]> nextHeaders;
     * if(!headers.isEmpty() && headers.size() < amount) { do { nextHeaders =
     * this.getBlockSignatures(headers.get(headers.size()-1), peer);
     * headers.addAll(nextHeaders); } while(headers.size() < amount &&
     * !nextHeaders.isEmpty()); }
     *
     * return headers; }
     */

    public void synchronize(DCSet dcSet, int checkPointHeight, Peer peer, int peerHeight) throws Exception {

        Controller cnt = Controller.getInstance();

        if (cnt.isOnStopping())
            throw new Exception("on stopping");

        /*
         * LOGGER.error("Synchronizing from peer: " + peer.toString() + ":" +
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

        Tuple2<byte[], List<byte[]>> headers = this.findHeaders(peer, peerHeight, lastBlockSignature, checkPointHeight);
        byte[] lastCommonBlockSignature = headers.a;
        List<byte[]> signatures = headers.b;

        if (lastCommonBlockSignature == null) {
            // simple ACCEPT tail CHAIN - MY LAST block founded in PEER
            if (signatures == null || signatures.isEmpty())
                return;

            // CREATE BLOCK BUFFER
            LOGGER.debug(
                    "START BUFFER" + " peer: " + peer + " for blocks: " + signatures.size());
            BlockBuffer blockBuffer = new BlockBuffer(signatures, peer);
            Block blockFromPeer;

            String errorMess = null;
            int banTime = BAN_BLOCK_TIMES >> 2;

            // GET AND PROCESS BLOCK BY BLOCK
            for (byte[] signature : signatures) {
                if (cnt.isOnStopping()) {
                    // STOP BLOCKBUFFER
                    blockBuffer.stopThread();
                    throw new Exception("on stopping");
                }

                // GET BLOCK
                LOGGER.debug("try get BLOCK from BUFFER");

                long time1 = System.currentTimeMillis();
                try {
                    blockFromPeer = blockBuffer.getBlock(signature);
                } catch (Exception e) {
                    blockBuffer.stopThread();
                    peer.ban(0, "get block BUFFER - " + e.getMessage());
                    throw new Exception(e);
                }

                if (blockFromPeer == null) {

                    // INVALID BLOCK THROW EXCEPTION
                    errorMess = "Dishonest peer on block null";
                    banTime = BAN_BLOCK_TIMES >> 4;
                    break;
                }

                if (cnt.isOnStopping()) {
                    // STOP BLOCKBUFFER
                    blockBuffer.stopThread();
                    throw new Exception("on stopping");
                }

                ///blockFromPeer.setCalcGeneratingBalance(dcSet); // NEED SET it
                ///LOGGER.debug("BLOCK Calc Generating Balance");

                if (cnt.isOnStopping()) {
                    // STOP BLOCKBUFFER
                    blockBuffer.stopThread();
                    throw new Exception("on stopping");
                }

                if (!blockFromPeer.isSignatureValid()) {
                    errorMess = "invalid Sign!";
                    banTime = BAN_BLOCK_TIMES << 1;
                    break;
                }
                LOGGER.debug("BLOCK Signature is Valid");

                if (blockFromPeer.getTimestamp() + (BlockChain.WIN_BLOCK_BROADCAST_WAIT_MS >> 2) > NTP.getTime()) {
                    errorMess = "invalid Timestamp from FUTURE";
                    break;
                }

                try {
                    // тут может парсинг транзакций упасть
                    blockFromPeer.getTransactions();
                } catch (Exception e) {
                    LOGGER.debug(e.getMessage(), e);
                    errorMess = "invalid PARSE! " + e.getMessage();
                    banTime = BAN_BLOCK_TIMES << 1;
                    break;
                }

                if (!blockFromPeer.isValid(dcSet, false)) {

                    errorMess = "invalid BLOCK";
                    banTime = BAN_BLOCK_TIMES;
                    break;
                }
                LOGGER.debug("BLOCK is Valid");

                if (cnt.isOnStopping()) {
                    blockBuffer.stopThread();
                    throw new Exception("on stopping");
                }

                try {
                    // PROCESS BLOCK

                    LOGGER.debug("try pipeProcessOrOrphan");
                    this.pipeProcessOrOrphan(dcSet, blockFromPeer, false, false);

                    LOGGER.debug("synchronize BLOCK END process");
                    blockBuffer.clearBlock(blockFromPeer.getSignature());
                    LOGGER.debug("synchronize clear from BLOCK BUFFER");
                    continue;

                } catch (Exception e) {

                    // STOP BLOCKBUFFER
                    blockBuffer.stopThread();

                    if (cnt.isOnStopping()) {
                        throw new Exception("on stopping");
                    } else {
                        throw new Exception(e);
                    }
                }
            }

            // STOP BLOCKBUFFER
            blockBuffer.stopThread();

            if (errorMess != null) {
                // INVALID BLOCK THROW EXCEPTION
                String mess = "Dishonest peer on block " + errorMess;
                peer.ban(banTime, mess);
                throw new Exception(mess);
            }

            if (cnt.isOnStopping()) {
                throw new Exception("on stopping");
            }

            // RECURSIVE CALL if new block is GENERATED
            /////synchronize(dcSet, checkPointHeight, peer, peerHeight);

        } else {

            // GET THE BLOCKS FROM SIGNATURES
            List<Block> blocks = this.getBlocks(dcSet, signatures, peer);

            if (cnt.isOnStopping()) {
                throw new Exception("on stopping");
            }

            Block lastCommonBlock = dcSet.getBlockSignsMap().getBlock(lastCommonBlockSignature);

            // SYNCHRONIZE BLOCKS
            LOGGER.error("synchronize with OPRHAN from common block [" + lastCommonBlock.getHeight()
                    + "] for blocks: " + blocks.size());
            List<Transaction> orphanedTransactions = this.synchronize_blocks(dcSet, lastCommonBlock, checkPointHeight,
                    blocks, peer);

            blocks = null;

            if (cnt.isOnStopping()) {
                throw new Exception("on stopping");
            }

            // SEND ORPHANED TRANSACTIONS TO PEER
            TransactionMap map = dcSet.getTransactionMap();
            for (Transaction transaction : orphanedTransactions) {
                if (cnt.isOnStopping()) {
                    throw new Exception("on stopping");
                }

                byte[] sign = transaction.getSignature();
                if (!map.contains(sign))
                    map.set(sign, transaction);
            }
        }

    }

    private List<byte[]> getBlockSignatures(byte[] header, Peer peer) throws Exception {

        /*
         * LOGGER.
         * error("core.Synchronizer.getBlockSignatures(byte[], Peer) for: " +
         * Base58.encode(header));
         */

        /// CREATE MESSAGE
        Message message = MessageFactory.getInstance().createGetHeadersMessage(header);

        // SEND MESSAGE TO PEER
        // see response callback in controller.Controller.onMessage(Message)
        // type = GET_SIGNATURES_TYPE
        SignaturesMessage response;
        try {
            response = (SignaturesMessage) peer.getResponse(message, GET_HEADERS_TIMEOUT);
        } catch (Exception e) {
            peer.ban("Cannot retrieve headers");
            throw new Exception("Failed to communicate with peer (retrieve headers) - response = null");
        }

        if (response == null) {
            // cannot retrieve headers
            peer.ban("Cannot retrieve headers");
            throw new Exception("Failed to communicate with peer (retrieve headers) - response = null");
        }

        return response.getSignatures();
    }

    private Tuple2<byte[], List<byte[]>> findHeaders(Peer peer, int peerHeight, byte[] lastBlockSignature,
                                                     int checkPointHeight) throws Exception {

        DCSet dcSet = DCSet.getInstance();
        Controller cnt = Controller.getInstance();

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

            if (headers.size() == 0) {
                cnt.resetWeightOfPeer(peer);
                String mess = "Peer is SAME as me";
                //peer.ban(0, mess);
                throw new Exception(mess);
            }

            // null - not ned orphan my CHAIN
            return new Tuple2<byte[], List<byte[]>>(null, headers);

        }

        byte[] signCheck = dcSet.getBlocksHeadsMap().get(checkPointHeight).signature;

        List<byte[]> headersCheck = this.getBlockSignatures(signCheck, peer);
        if (headersCheck.isEmpty()) {
            String mess = "Dishonest peer: my CHECKPOINT SIGNATURE -> not found";
            peer.ban(BAN_BLOCK_TIMES, mess);
            throw new Exception(mess);
        }

        // int myChainHeight =
        // Controller.getInstance().getBlockChain().getHeight();
        //int maxChainHeight = dcSet.getBlockSignsMap().getHeight(lastBlockSignature);
        int maxChainHeight = dcSet.getBlockMap().size();
        if (maxChainHeight < checkPointHeight) {
            String mess = "Dishonest peer: my checkPointHeight[" + checkPointHeight + "\n -> not found";
            peer.ban(BAN_BLOCK_TIMES, mess);
            throw new Exception(mess);
        }

        LOGGER.info("findHeaders " + " maxChainHeight: " + maxChainHeight + " to minHeight: " + checkPointHeight);

        // try get check point block from peer
        // GENESIS block nake ERROR in org.erachain.network.Peer.sendMessage(Message) ->
        // this.out.write(message.toBytes());
        // TODO fix it error
        byte[] checkPointHeightSignature;
        Block checkPointHeightCommonBlock = null;
        checkPointHeightSignature = dcSet.getBlocksHeadsMap().get(checkPointHeight).signature;

        try {
            // try get common block from PEER
            // not need CHECK peer on ping = false
            checkPointHeightCommonBlock = getBlock(checkPointHeightSignature, peer, false);
        } catch (Exception e) {
            String mess = "in getBlock:\n" + e.getMessage() + "\n *** in Peer: " + peer;
            //// banned in getBlock -- peer.ban(BAN_BLOCK_TIMES>>3, mess);
            throw new Exception(mess);
        }

        if (checkPointHeightCommonBlock == null) {
            String mess = "Dishonest peer: my block[" + checkPointHeight + "\n -> common BLOCK not found";
            peer.ban(BAN_BLOCK_TIMES, mess);
            throw new Exception(mess);
        }

        // GET HEADERS UNTIL COMMON BLOCK IS FOUND OR ALL BLOCKS HAVE BEEN
        // CHECKED
        // int step = BlockChain.SYNCHRONIZE_PACKET>>2;
        byte[] lastCommonBlockSignature;
        int step = 2;
        do {
            if (cnt.isOnStopping()) {
                throw new Exception("on stopping");
            }

            maxChainHeight -= step;

            if (maxChainHeight < checkPointHeight) {
                maxChainHeight = checkPointHeight;
                lastCommonBlockSignature = checkPointHeightCommonBlock.getSignature();
            } else {
                lastCommonBlockSignature = dcSet.getBlocksHeadsMap().get(maxChainHeight).signature;
            }

            LOGGER.debug(
                    "findHeaders try found COMMON header" + " step: " + step + " maxChainHeight: " + maxChainHeight);

            headers = this.getBlockSignatures(lastCommonBlockSignature, peer);

            LOGGER.debug("findHeaders try found COMMON header" + " founded headers: " + headers.size());

            if (headers.size() > 1) {
                if (maxChainHeight < checkPointHeight) {
                    String mess = "Dishonest peer by maxChainHeight < checkPointHeight " + peer;
                    peer.ban(BAN_BLOCK_TIMES, mess);
                    throw new Exception(mess);
                }
                break;
            }

            if (step < 10000)
                step <<= 1;

        } while (maxChainHeight > checkPointHeight && headers.isEmpty());

        LOGGER.info("findHeaders AFTER try found COMMON header" + " founded headers: " + headers.size());

        // CLEAR head of common headers exclude LAST!
        while (headers.size() > 1 && dcSet.getBlockSignsMap().contains(headers.get(0))) {
            lastCommonBlockSignature = headers.remove(0);
        }

        LOGGER.info("findHeaders headers CLEAR" + "now headers: " + headers.size());

        return new Tuple2<byte[], List<byte[]>>(lastCommonBlockSignature, headers);

    }

    private List<Block> getBlocks(DCSet dcSet, List<byte[]> signatures, Peer peer) throws Exception {

        LOGGER.debug("try get BLOCKS from common block SIZE:" + signatures.size() + " - " + peer);

        List<Block> blocks = new ArrayList<Block>();
        Controller cnt = Controller.getInstance();

        int bytesGet = 0;
        for (byte[] signature : signatures) {
            if (cnt.isOnStopping()) {
                throw new Exception("on stopping");
            }

            // ADD TO LIST
            Block block = getBlock(signature, peer, true);
            if (block == null)
                break;

            // NOW generating balance not was send by NET
            // need to SET it!
            ////block.setCalcGeneratingBalance(dcSet);

            blocks.add(block);
            bytesGet += 1500 + block.getDataLength(false);
            ///LOGGER.debug("block added with RECS:" + block.getTransactionCount() + " bytesGet kb: " + bytesGet / 1000);
            if (bytesGet > BYTES_MAX_GET) {
                break;
            }
        }

        return blocks;
    }

    // SYNCHRONIZED DO NOT PROCCESS A BLOCK AT THE SAME TIME
    // SYNCHRONIZED MIGHT HAVE BEEN PROCESSING PREVIOUS BLOCK
    public synchronized void pipeProcessOrOrphan(DCSet dcSet, Block block, boolean doOrphan, boolean hardFlush)
            throws Exception {
        Controller cnt = Controller.getInstance();

        // CHECK IF WE ARE STILL PROCESSING BLOCKS
        if (cnt.isOnStopping()) {
            throw new Exception("on stopping");
        }

        long processTiming = System.nanoTime();

        dcSet.getBlockMap().setProcessing(true);
        boolean observOn = cnt.doesWalletExists() && cnt.useGui;
        Integer countObserv_ADD = null;
        Integer countObserv_REMOVE = null;
        Integer countObserv_COUNT = null;
        if (observOn) {
      //      countObserv_ADD = dcSet.getTransactionMap().deleteObservableData(DBMap.NOTIFY_ADD);
      //      countObserv_REMOVE = dcSet.getTransactionMap().deleteObservableData(DBMap.NOTIFY_REMOVE);
      //      countObserv_COUNT = dcSet.getTransactionMap().deleteObservableData(DBMap.NOTIFY_COUNT);
        }

        Exception error = null;

        if (doOrphan) {

            try {
                block.orphan(dcSet);
                dcSet.getBlockMap().setProcessing(false);
                //dcSet.updateTxCounter(-block.getTransactionCount());
                // FARDFLUSH not use in each case - only after accumulate size
                int blockSize = (1 + block.getTransactionCount()) * 1000 + block.getDataLength(false);
                dcSet.flush(blockSize, false);

                if (cnt.isOnStopping())
                    return;

                // образать список только по максимальному размеру
                dcSet.getTransactionMap().clearByDeadTimeAndLimit(block.getTimestamp(), false);

                if (cnt.isOnStopping())
                    return;

            } catch (IOException e) {
                error = new Exception(e);

            } catch (Exception e) {

                if (cnt.isOnStopping()) {
                    return;
                } else {
                    error = new Exception(e);
                }
            } finally {
                if (cnt.isOnStopping()) {
                    throw new Exception("on stopping");
                }

                if (error != null) {
                    dcSet.rollback();
                    LOGGER.error(error.getMessage(), error);

                    if (error instanceof IOException) {
                        cnt.stopAll(22);
                        return;
                    }

                    throw new Exception(error);

                }

                if (observOn) {

                        if (countObserv_ADD != null) {
                            try {
                                dcSet.getTransactionMap().setObservableData(DBMap.NOTIFY_ADD, countObserv_ADD);
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
                block.process(dcSet);
                dcSet.getBlockMap().setProcessing(false);
                //dcSet.updateTxCounter(block.getTransactionCount());

                // FLUSH not use in each case - only after accumulate size
                int blockSize = (1 + block.getTransactionCount()) * 1000 + block.getDataLength(false);
                dcSet.flush(blockSize, false);

                // образать список и по времени протухания
                dcSet.getTransactionMap().clearByDeadTimeAndLimit(block.getTimestamp(), true);

                if (cnt.isOnStopping())
                    return;

                if (Settings.getInstance().getNotifyIncomingConfirmations() > 0) {
                    cnt.NotifyIncoming(block.getTransactions());
                }

                if (cnt.isOnStopping())
                    return;

                // NOTIFY to WALLET

            } catch (IOException e) {
                error = new Exception(e);

            } catch (Exception e) {

                if (cnt.isOnStopping()) {
                    return;
                } else {
                    error = new Exception(e);
                }
            } finally {
                if (cnt.isOnStopping()) {
                    throw new Exception("on stopping");
                }

                if (error != null) {
                    dcSet.rollback();
                    LOGGER.error(error.getMessage(), error);

                    if (error instanceof IOException) {
                        cnt.stopAll(22);
                        return;
                    }

                    throw new Exception(error);

                }

                if (observOn) {

                    if (countObserv_REMOVE != null) {
                        try {
                            dcSet.getTransactionMap().setObservableData(DBMap.NOTIFY_REMOVE, countObserv_REMOVE);
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
            processTiming = System.nanoTime() - processTiming;
            if (processTiming < 999999999999l) {
                // при переполнении может быть минус
                // в миеросекундах подсчет делаем
                cnt.getBlockChain().updateTXProcessTimingAverage(processTiming, block.getTransactionCount());
            }
        }


    }

    public void stop() {

        // this.run = false;
        // if (runedBlock != null)
        // runedBlock.stop();

        // this.pipeProcessOrOrphan(DLSet.getInstance(), null, false);
    }
}
