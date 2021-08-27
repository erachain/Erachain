package org.erachain.core;


import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ReferenceMapImpl;
import org.erachain.datachain.TransactionMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.network.message.MessageFactory;
import org.erachain.network.message.SignaturesMessage;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.MonitoredThread;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.*;

/**
 * основной верт, решающий последовательно три задачи - либо собираем блок, проверяем отставание от сети
 * и синхронизируемся с сетью если не догнали ее, либо ловим новый блок из сети и заносим его в цепочку блоков
 */
public class BlockGenerator extends MonitoredThread implements Observer {

    private static Logger LOGGER = LoggerFactory.getLogger(BlockGenerator.class.getSimpleName());

    private static int WAIT_STEP_MS = 100;

    private static Controller ctrl = Controller.getInstance();
    private static int local_status = 0;
    private PrivateKeyAccount acc_winner;
    //private List<Block> lastBlocksForTarget;
    private byte[] solvingReference;
    private List<PrivateKeyAccount> cachedAccounts;
    private ForgingStatus forgingStatus = ForgingStatus.FORGING_DISABLED;
    private boolean walletOnceUnlocked = false;
    private int orphanto = 0;
    private static List<byte[]> needRemoveInvalids;

    private int syncTo = 0;
    private Peer syncFromPeer;


    private final DCSet dcSet;
    private final BlockChain bchain;

    public BlockGenerator(DCSet dcSet, BlockChain bchain, boolean withObserve) {

        this.dcSet = dcSet;
        this.bchain = bchain;

        if (Settings.getInstance().isGeneratorKeyCachingEnabled()) {
            this.cachedAccounts = new ArrayList<PrivateKeyAccount>();
        }

        if (withObserve) addObserver();
        this.setName("Thread BlockGenerator - " + this.getId());
    }

    public static int getStatus() {
        return local_status;
    }

    public static String viewStatus() {

        switch (local_status) {
            case -1:
                return "-1 STOPed";
            case 1:
                return "1 FLUSH, WAIT";
            case 2:
                return "2 FLUSH, TRY";
            case 3:
                return "3 UPDATE";
            case 31:
                return "31 UPDATE SAME";
            case 41:
                return "41 WAIT MAKING";
            case 4:
                return "4 PREPARE MAKING";
            case 5:
                return "5 GET WIN ACCOUNT";
            case 6:
                return "6 WAIT BEFORE MAKING";
            case 7:
                return "7 MAKING NEW BLOCK";
            case 8:
                return "8 BROADCASTING";
            default:
                return "0 WAIT";
        }
    }

    public Peer betterPeer;
    /**
     * если цепочка встала из-за патовой ситуации то попробовать ее решить
     ^ путем выбора люолее сильной а не длинной
     * так же кажые 10 блоков проверяеем самую толстую цепочку
     */
    public boolean checkWeightPeers() {
        // MAY BE PAT SITUATION

        //logger.debug("try check better WEIGHT peers");

        betterPeer = null;

        Peer peer;
        this.setMonitorStatus("checkWeightPeers");

        int counter = 0;
        // на всякий случай поставим ораничение
        while (counter++ < 30) {

            Tuple2<Integer, Long> myHW = ctrl.getBlockChain().getHWeightFull(dcSet);
            if (myHW.a < 5)
                break;
            byte[] lastSignature = dcSet.getBlocksHeadsMap().get(myHW.a - 2).signature;
            //byte[] lastSignature = bchain.getLastBlockSignature(dcSet);

            // не тестируем те узлы которые мы заткунули по Силе - они выдаются Силу выше хотя цепочка та же
            Tuple3<Integer, Long, Peer> maxPeer = ctrl.getMaxPeerHWeight(0, true, true);

            peer = maxPeer.c;

            if (peer == null) {
                return false;
            }

            ///LOGGER.debug("better WEIGHT peers found: " + maxPeer);

            SignaturesMessage response = null;
            try {

                response = (SignaturesMessage) peer.getResponse(
                        MessageFactory.getInstance().createGetHeadersMessage(lastSignature),
                        Synchronizer.GET_BLOCK_TIMEOUT >> 2);
            } catch (Exception e) {
                LOGGER.debug("RESPONSE error " + peer + " " + e.getMessage());
                // remove HW from peers
                peer.setCorrectionWeight(myHW);
                continue;
            }

            if (response == null) {
                LOGGER.debug("peer RESPONSE is null " + peer);
                // remove HW from peers
                peer.setCorrectionWeight(myHW);
                continue;
            }

            List<byte[]> headers = response.getSignatures();
            if (headers.isEmpty()) {
                // общего блока не найдено - значит цепочка уже разошлась - делаем откат своего блока???
                // Или ждем когда сами встанем
                LOGGER.debug("I to orphan - Peer has DEEP different CHAIN " + maxPeer);
                betterPeer = peer;
                return true;
            }

            // Удалим то что унас тоже есть
            do {
                headers.remove(0);
            } while (!headers.isEmpty() && dcSet.getBlockSignsMap().contains(headers.get(0)));

            if (headers.isEmpty()) {
                // если прилетели данные с этого ПИРА - сброим их в то что мы сами вычислили
                LOGGER.debug("peer has same Weight " + maxPeer);
                peer.setCorrectionWeight(myHW);
                // продолжим поиск дальше
                continue;
            } else {
                LOGGER.debug("I to orphan - Peer has different CHAIN " + maxPeer);
                betterPeer = peer;
                return true;
            }

        }

        return false;

    }

    public Block generateNextBlock(PrivateKeyAccount account,
                                   Block parentBlock, Tuple2<List<Transaction>, Integer> transactionsItem, int forgingValue, long winValue, long previousTarget) {

        if (transactionsItem == null) {
            return null;
        }

        int version = parentBlock.getNextBlockVersion(dcSet);
        byte[] atBytes;
        atBytes = new byte[0];

        //CREATE NEW BLOCK
        Block newBlock = new Block(version, parentBlock, account,
                transactionsItem, atBytes,
                forgingValue, winValue, previousTarget);
        newBlock.sign(account);
        return newBlock;

    }

    private void testTransactions(int blockHeight, long timePointForValidTX) {

        SecureRandom randomSecure = new SecureRandom();
        // сдвиг назад организуем

        long blockTimestampBeg = bchain.getTimestamp(blockHeight - 1) + 10;
        long blockTimestampEnd = timePointForValidTX;


        LOGGER.info("generate TEST txs: " + BlockChain.TEST_DB
                + " period: " + new Timestamp(blockTimestampBeg) + " >>> " + new Timestamp(blockTimestampEnd));

        boolean generateNewAccount = false;

        long assetKey = 2L;
        BigDecimal amount = new BigDecimal("0.00000001");
        RSend messageTx;
        byte[] isText = new byte[]{1};
        byte[] encryptMessage = new byte[]{0};

        Random random = new Random();

        long timestamp;
        PublicKeyAccount recipient;
        HashMap<PrivateKeyAccount, Long> creatorsReference = new HashMap<>();
        for (int index = 0; index < BlockChain.TEST_DB; index++) {

            if (generateNewAccount) {
                byte[] seedRecipient = new byte[32];
                randomSecure.nextBytes(seedRecipient);
                recipient = new PublicKeyAccount(seedRecipient);
            } else {
                recipient = BlockChain.TEST_DB_ACCOUNTS[random.nextInt(BlockChain.TEST_DB_ACCOUNTS.length)];
            }

            PrivateKeyAccount creator;
            // ограничим выборку счетов как сверху так и снизу
            int countSeek = 3 + (BlockChain.TEST_DB_ACCOUNTS.length >> 5);
            do {
                timestamp = 0;
                creator = BlockChain.TEST_DB_ACCOUNTS[random.nextInt(BlockChain.TEST_DB_ACCOUNTS.length)];
                if (creatorsReference.containsKey(creator)) {
                    timestamp = creatorsReference.get(creator);
                    timestamp++;
                } else {
                    // определим время создания для каждого счета
                    timestamp = blockTimestampBeg;
                }
            } while (timestamp > blockTimestampEnd && countSeek-- > 0);

            creatorsReference.put(creator, timestamp);

            if (BlockChain.NOT_CHECK_SIGNS) {
                byte[] sign = new byte[64];
                // первые 8 байт нужно уникальные
                System.arraycopy(Longs.toByteArray(random.nextLong()), 0, sign, 0, 8);
                System.arraycopy(creator.getPublicKey(), 0, sign, 8, 32);
                System.arraycopy(recipient.getPublicKey(), 0, sign, 32, 32);

                messageTx = new RSend(creator, (byte) 0, recipient, assetKey,
                        amount, "TEST" + blockHeight + "-" + index, null, isText, encryptMessage, timestamp, 0L, sign);
            } else {
                messageTx = new RSend(creator, null, null, (byte) 0, recipient, assetKey,
                        amount, "TEST" + blockHeight + "-" + index, null, isText, encryptMessage, timestamp, 0L);
                messageTx.sign(creator, Transaction.FOR_NETWORK);
            }

            // может переполниться очередь на добавление транзакций - подождем
            while (!ctrl.transactionsPool.offerMessage(messageTx)) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
            }

        }

        if (BlockChain.CHECK_DOUBLE_SPEND_DEEP >= 0) {
            //// только если включена проверка повторов - запускаем эту проверку на невалидные транзакции

            // добавить невалидных транзакций немного - по времени создания
            timestamp = blockTimestampBeg - 10000000L * 1L;
            PrivateKeyAccount[] creators = creatorsReference.keySet().toArray(new PrivateKeyAccount[0]);
            for (int index = 0; index < (BlockChain.TEST_DB >> 3); index++) {

                recipient = BlockChain.TEST_DB_ACCOUNTS[random.nextInt(BlockChain.TEST_DB_ACCOUNTS.length)];

                PrivateKeyAccount creator = creators[random.nextInt(creators.length)];

                messageTx = new RSend(creator, null, null, (byte) 0, recipient, assetKey,
                        amount, "TEST" + blockHeight + "-" + index, null, isText, encryptMessage,
                        timestamp, 0L);
                messageTx.sign(creator, Transaction.FOR_NETWORK);

                ctrl.transactionsPool.offerMessage(messageTx);

            }
        }

    }

    public Tuple2<List<Transaction>, Integer> getUnconfirmedTransactions(int blockHeight, long timestamp, BlockChain bchain,
                                                                         long max_winned_value) {

        LOGGER.debug("* * * * * COLLECT TRANSACTIONS to time: " + new Timestamp(timestamp));

        long start = System.currentTimeMillis();

        //CREATE FORK OF GIVEN DATABASE
        DCSet newBlockDC = null;

        Block waitWin;

        List<Transaction> transactionsList = new ArrayList<Transaction>();

        //	boolean transactionProcessed;
        long totalBytes = 0;
        int counter = 0;
        int check_time = 0;
        int max_time_gen = BlockChain.GENERATING_MIN_BLOCK_TIME_MS(blockHeight) >> 2;

        try {
            TransactionMap map = dcSet.getTransactionTab();
            needRemoveInvalids = new ArrayList<byte[]>();

            this.setMonitorStatusBefore("getUnconfirmedTransactions");

            try (IteratorCloseable<Long> iterator = map.getTimestampIterator(false)) {

                long testTime = 0;
                while (iterator.hasNext()) {

                    // проверим иногда - вдруг уже слишком долго собираем - останов сборки транзакций
                    // так как иначе такой блок и сеткой остальной не успеет обработаться
                    if (BlockChain.TEST_DB == 0 && check_time++ > 300) {
                        if (System.currentTimeMillis() - start > max_time_gen) {
                            LOGGER.debug("* * * * * COLLECT TRANSACTIONS BREAK by SPEND TIME[ms]: " + (System.currentTimeMillis() - start));
                            break;
                        }
                        check_time = 0;
                    }
                    if (ctrl.isOnStopping()) {
                        break;
                    }

                    if (bchain != null) {
                        waitWin = bchain.getWaitWinBuffer();
                        if (betterPeer != null || waitWin != null && waitWin.getWinValue() > max_winned_value) {
                            break;
                        }
                    }

                    Transaction transaction = map.get(iterator.next());

                    if (transaction == null) {
                        LOGGER.debug("* * * * * COLLECT TRANSACTIONS BREAK by NULL NEXT");
                        break;
                    }

                    // сбросим всё мясо которе в КЭШе может быть
                    // иначе может расчет не пойти так как какие-то данные уже заданы были из кошелька
                    transaction = transaction.copy();

                    if (BlockChain.CHECK_BUGS > 7) {
                        LOGGER.debug(" found TRANSACTION on " + new Timestamp(transaction.getTimestamp()) + " " + transaction.getCreator().getAddress());
                        if (testTime > transaction.getTimestamp()) {
                            LOGGER.error(" ERROR testTIME " + new Timestamp(testTime));
                        }
                        testTime = transaction.getTimestamp();
                    }

                    if (transaction.getTimestamp() > timestamp) {
                        LOGGER.debug("* * * * * COLLECT TRANSACTIONS BREAK by UTX TIMESTAMP: " + new Timestamp(transaction.getTimestamp()));
                        break;
                    }

                    // делать форк только если есть транзакции - так как это сильно кушает память
                    if (newBlockDC == null) {
                        //CREATE FORK OF GIVEN DATABASE
                        // создаем в памяти базу - так как она на 1 блок только нужна - а значит много памяти не возьмет
                        DB database = DCSet.makeDBinMemory();
                        newBlockDC = dcSet.fork(database, "getUnconfirmedTransactions");
                    }

                    try {

                        // здесь уже может быть ошибка если Ордер не найден например для Отмена ордера
                        transaction.setDC(newBlockDC, Transaction.FOR_NETWORK, blockHeight, counter + 1);

                        if (false // вообще-то все внутренние транзакции уже провверены на подпись!
                                && !transaction.isSignatureValid(newBlockDC)) {
                            needRemoveInvalids.add(transaction.getSignature());
                            continue;
                        }

                        if (false && // тут нельзя пока удалять - может она будет включена
                                // и пусть удаляется только если невалидная будет
                                timestamp > transaction.getDeadline()) {
                            needRemoveInvalids.add(transaction.getSignature());
                            continue;
                        }

                        if (transaction.isValid(Transaction.FOR_NETWORK, 0L) != Transaction.VALIDATE_OK) {
                            needRemoveInvalids.add(transaction.getSignature());
                            if (BlockChain.CHECK_BUGS > 1) {
                                LOGGER.error(" Transaction invalid: " + transaction.isValid(Transaction.FOR_NETWORK, 0L));
                            }
                            continue;
                        }

                        //CHECK IF ENOUGH ROOM
                        if (++counter > BlockChain.MAX_BLOCK_SIZE_GEN) {
                            counter--;
                            LOGGER.debug("* * * * * COLLECT TRANSACTIONS BREAK by MAX COUNT: " + counter);
                            break;
                        }

                        totalBytes += transaction.getDataLength(Transaction.FOR_NETWORK, true);
                        if (totalBytes > BlockChain.MAX_BLOCK_SIZE_BYTES_GEN) {
                            counter--;
                            LOGGER.debug("* * * * * COLLECT TRANSACTIONS BREAK by MAX BYTES: " + totalBytes);
                            break;
                        }

                        ////ADD INTO LIST
                        transactionsList.add(transaction);

                        //PROCESS IN NEWBLOCKDB
                        transaction.process(null, Transaction.FOR_NETWORK);

                    } catch (Exception e) {

                        if (ctrl.isOnStopping()) {
                            break;
                        }

                        //     transactionProcessed = true;

                        LOGGER.error(e.getMessage(), e);
                        //REMOVE FROM LIST
                        needRemoveInvalids.add(transaction.getSignature());

                    }

                }

            } finally {
                if (newBlockDC != null)
                    newBlockDC.close();
            }

            LOGGER.debug("get Unconfirmed Transactions = " + (System.currentTimeMillis() - start)
                    + "ms for trans: " + counter + " and DELETE: " + needRemoveInvalids.size()
                    + " from Poll: " + map.size());

            this.setMonitorStatusAfter();

        } catch (java.lang.Throwable e) {
            if (e instanceof java.lang.IllegalAccessError) {
                // налетели на закрытую таблицу
            } else {
                LOGGER.error(e.getMessage(), e);
            }
            LOGGER.debug("get Unconfirmed Transactions = " + (System.currentTimeMillis() - start)
                    + "ms for trans: " + counter + " and DELETE: " + needRemoveInvalids.size()
                    + " before CLEARED event!");

        }

        return new Tuple2<List<Transaction>, Integer>(transactionsList, counter);
    }

    private void clearInvalids() {
        if (needRemoveInvalids != null && !needRemoveInvalids.isEmpty()) {
            long start = System.currentTimeMillis();
            TransactionMap transactionsMap = dcSet.getTransactionTab();

            for (byte[] signature : needRemoveInvalids) {
                if (ctrl.isOnStopping()) {
                    return;
                }
                transactionsMap.delete(signature);
            }
            LOGGER.debug("clear INVALID Transactions = " + (System.currentTimeMillis() - start) + "ms for removed: " + needRemoveInvalids.size()
                    + " LEFT: " + transactionsMap.size());

            needRemoveInvalids = null;
        }

    }

    public void checkForRemove(long timestamp) {

        //CREATE FORK OF GIVEN DATABASE
        try (DCSet newBlockDC = dcSet.fork(DCSet.makeDBinMemory(), "checkForRemove")) {

            int blockHeight = newBlockDC.getBlockSignsMap().size() + 1;

            //Block waitWin;
            int counter = 0;
            int totalBytes = 0;

            long start = System.currentTimeMillis();

            TransactionMap map = dcSet.getTransactionTab();
            LOGGER.debug("get ITERATOR for Remove = " + (System.currentTimeMillis() - start) + " ms");

            needRemoveInvalids = new ArrayList<byte[]>();

            this.setMonitorStatusBefore("checkForRemove");

            try (IteratorCloseable<Long> iterator = map.getTimestampIterator(false)) {

                while (iterator.hasNext()) {

                    if (ctrl.isOnStopping()) {
                        return;
                    }

                    Transaction transaction = map.get(iterator.next());

                    if (transaction.getTimestamp() > timestamp)
                        break;

                    transaction.setDC(newBlockDC, Transaction.FOR_NETWORK, blockHeight, counter + 1);

                    if (false // тут уже все проверено внутри нашей базы
                            && !transaction.isSignatureValid(newBlockDC)) {
                        needRemoveInvalids.add(transaction.getSignature());
                        continue;
                    }

                    try {

                        if (transaction.isValid(Transaction.FOR_NETWORK, 0l) != Transaction.VALIDATE_OK) {
                            needRemoveInvalids.add(transaction.getSignature());
                            continue;
                        }

                        //CHECK IF ENOUGH ROOM
                        if (++counter > (BlockChain.MAX_BLOCK_SIZE << 2)) {
                            break;
                        }

                        totalBytes += transaction.getDataLength(Transaction.FOR_NETWORK, true);
                        if (totalBytes > (BlockChain.MAX_BLOCK_SIZE_BYTES_GEN << 2)) {
                            break;
                        }

                        //PROCESS IN NEWBLOCKDB
                        transaction.process(null, Transaction.FOR_NETWORK);

                        // GO TO NEXT TRANSACTION
                        continue;

                    } catch (Exception e) {

                        if (ctrl.isOnStopping()) {
                            return;
                        }

                        //     transactionProcessed = true;

                        LOGGER.error(e.getMessage(), e);
                        //REMOVE FROM LIST
                        needRemoveInvalids.add(transaction.getSignature());

                        continue;

                    }

                }

            } catch (java.lang.Throwable e) {
                if (e instanceof java.lang.IllegalAccessError) {
                    // налетели на закрытую таблицу
                } else {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            this.setMonitorStatusAfter();

            LOGGER.debug("get check for Remove = " + (System.currentTimeMillis() - start) + "ms for trans: " + map.size()
                    + " needRemoveInvalids:" + needRemoveInvalids.size());

        }

    }

    public ForgingStatus getForgingStatus() {
        return forgingStatus;
    }

    public void setForgingStatus(ForgingStatus status) {
        if (forgingStatus != status) {
            forgingStatus = status;
            ctrl.forgingStatusChanged(forgingStatus);
        }
    }

    public void setOrphanTo(int height) {
        this.orphanto = height;
    }

    public void setSyncTo(int height, Peer peer) {
        this.syncTo = height;
        this.syncFromPeer = peer;
    }

    public void addObserver() {
        new Thread("try syncForgingStatus") {
            @Override
            public void run() {

                //WE HAVE TO WAIT FOR THE WALLET TO ADD THAT LISTENER.
                while (!ctrl.doesWalletExists() || !ctrl.doesWalletDatabaseExists()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                ctrl.addWalletObserver(BlockGenerator.this);
                syncForgingStatus();
            }
        }.start();
        ctrl.addObserver(this);
    }

    private List<PrivateKeyAccount> getKnownAccounts() {
        //CHECK IF CACHING ENABLED
        if (Settings.getInstance().isGeneratorKeyCachingEnabled()) {
            List<PrivateKeyAccount> privateKeyAccounts = ctrl.getWalletPrivateKeyAccounts();

            //IF ACCOUNTS EXISTS
            if (!privateKeyAccounts.isEmpty()) {
                //CACHE ACCOUNTS
                this.cachedAccounts = privateKeyAccounts;
            }

            //RETURN CACHED ACCOUNTS
            return this.cachedAccounts;
        } else {
            //RETURN ACCOUNTS
            return ctrl.getWalletPrivateKeyAccounts();
        }
    }

    public void cacheKnownAccounts() {
        if (Settings.getInstance().isGeneratorKeyCachingEnabled()) {
            List<PrivateKeyAccount> privateKeyAccounts = ctrl.getWalletPrivateKeyAccounts();

            //IF ACCOUNTS EXISTS
            if (!privateKeyAccounts.isEmpty()) {
                //CACHE ACCOUNTS
                this.cachedAccounts = privateKeyAccounts;
            }
        }
    }

    @Override
    public void run() {

        //TransactionMap transactionsMap = dcSet.getTransactionMap();

        int heapOverflowCount = 0;

        long processTiming;
        long transactionMakeTimingCounter = 0;
        long transactionMakeTimingAverage = 0;

        /**
         * время пинга если идет синхронизация например
         */
        long pointPing = 0;
        long timeTmp;
        long timePoint = 0;
        long timePointForValidTX = 0;
        long flushPoint = 0;
        long timeUpdate = 0;
        int shift_height = 0;
        //byte[] unconfirmedTransactionsHash;
        //long winned_value_account;
        //long max_winned_value_account;
        int height = BlockChain.getHeight(dcSet) + 1;
        int forgingValue;
        int winned_forgingValue;
        long winValue;
        int targetedWinValue;
        long winned_winValue;
        long previousTarget = bchain.getTarget(dcSet);

        int wait_new_block_broadcast;
        long wait_step;
        boolean newWinner;
        long pointLogGoUpdate = 0;
        long pointLogWaitFlush = 0;

        boolean needRequestWinBlock = false;
        long blockTimeMS;
        this.initMonitor();

        Random random = new Random();
        if (BlockChain.TEST_DB > 0) {

            // REST balances! иначе там копится размер таблицы
            //dcSet.getAssetBalanceMap().clear();

            byte[] seed = Crypto.getInstance().digest("test24243k2l3j42kl43j".getBytes());
            byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
            BigDecimal balance = new BigDecimal("10000");

            for (int nonce = 0; nonce < BlockChain.TEST_DB_ACCOUNTS.length; nonce++) {
                BlockChain.TEST_DB_ACCOUNTS[nonce] = new PrivateKeyAccount(Wallet.generateAccountSeed(seed, nonce));
                // SET BALANCES
                BlockChain.TEST_DB_ACCOUNTS[nonce].changeBalance(dcSet, false, false, 2, balance, false, false, true);
            }
        }

        try {
            // если только что была синхронизация - возьмем новый блок
            Peer afterUpdatePeer = null;
            Block waitWin = null;
            while (!ctrl.isOnStopping()) {

                int timeStartBroadcast = BlockChain.WIN_TIMEPOINT(height);

                if (waitWin != null) {
                    // освободим память
                    waitWin.close();
                }

                Block solvingBlock;
                Peer peer = null;
                Tuple3<Integer, Long, Peer> maxPeer;
                SignaturesMessage response;
                blockTimeMS = BlockChain.GENERATING_MIN_BLOCK_TIME_MS(height);

                try {
                    Thread.sleep(WAIT_STEP_MS);
                } catch (InterruptedException e) {
                    break;
                }

                this.setMonitorPoint();

                if (ctrl.isOnStopping()) {
                    return;
                }

                // GET real HWeight
                // пингуем всех тут чтобы знать кому слать свои транакции
                // на самом деле они сами присылают свое состояние после апдейта
                if (BlockChain.TEST_DB > 0) {
                    pointPing = 0;
                } else if (NTP.getTime() - pointPing > blockTimeMS >> 1) {
                    // нужно просмотривать пиги для синхронизации так же - если там -ХХ то не будет синхронизации
                    pointPing = NTP.getTime();
                    ctrl.pingAllPeers(false);
                }

                if (this.syncTo > 0) {
                    try {
                        this.setMonitorStatusBefore("Synchronize to " + syncTo + (syncFromPeer == null ? "" : " from: " + peer));
                        if (syncFromPeer == null) {
                            Tuple3<Integer, Long, Peer> maxPeerHW = ctrl.getMaxPeerHWeight(-5, false, false);
                            syncFromPeer = maxPeerHW.c;
                        }
                        if (syncFromPeer != null) {

                            try {
                                // SYNCHRONIZE FROM PEER
                                ctrl.synchronizer.synchronize(dcSet, syncTo, syncFromPeer, syncFromPeer.getHWeight(true).a,
                                        dcSet.getBlocksHeadsMap().get(syncTo).signature);
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }

                        } else {
                            LOGGER.info("SyncTo: peer not found");
                        }

                    } finally {
                        syncTo = 0;
                        syncFromPeer = null;
                        this.setMonitorStatusAfter();
                    }

                } else if (this.orphanto > 0) {
                    try {
                        this.setMonitorStatusBefore("orphan to " + orphanto);
                        local_status = 9;
                        ctrl.setForgingStatus(ForgingStatus.FORGING_ENABLED);

                        // обязательно нужно чтобы память освобождать
                        // и если объект был изменен (с тем же ключем у него удалили поле внутри - чтобы это не выдавлось
                        // при новом запросе - иначе изменения прилетают в другие потоки и ошибку вызываю
                        dcSet.clearCache();

                        // и перед всем этим необходимо слить все изменения на диск чтобы потом когда откат закончился
                        // не было остаков в пакетах RocksDB и трынзакциях MapDB
                        dcSet.flush(0, true, true);

                        while (bchain.getHeight(dcSet) >= this.orphanto) {
                            try (Block block = bchain.getLastBlock(dcSet)) {
                                ctrl.orphanInPipe(block);
                            } catch (Exception e) {
                                // если ошибка то выход делаем чтобы зарегистрировать ошибку
                                // так как это наша личная ошибка внутри
                                LOGGER.error(e.getMessage(), e);
                                ctrl.stopAndExit(104);
                                return;
                            }
                        }

                        if (BlockChain.NOT_STORE_REFFS_HISTORY && BlockChain.CHECK_DOUBLE_SPEND_DEEP >= 0) {
                            // TODO тут нужно обновить за последние 3-10 блоков значения  если проверка используется
                            ReferenceMapImpl map = dcSet.getReferenceMap();

                            return;
                        }


                    } finally {
                        this.orphanto = 0;
                        ctrl.checkStatusAndObserve(0);

                        this.setMonitorStatusAfter();

                    }
                }

                timeTmp = bchain.getTimestamp(dcSet) + blockTimeMS;
                if (timeTmp > NTP.getTime())
                    continue;

                if (timePoint != timeTmp) {
                    timePoint = timeTmp;
                    timePointForValidTX = timePoint - BlockChain.UNCONFIRMED_SORT_WAIT_MS(height);
                    betterPeer = null;
                    needRequestWinBlock = false;

                    Timestamp timestampPoit = new Timestamp(timePoint);
                    LOGGER.info("+ + + + + START GENERATE POINT on " + timestampPoit + " for UTX time: " + new Timestamp(timePointForValidTX));
                    this.setMonitorStatus("+ + + + + START GENERATE POINT on " + timestampPoit);

                    flushPoint = timePoint + BlockChain.FLUSH_TIMEPOINT(height);
                    this.solvingReference = null;
                    local_status = 0;

                    // пинганем тут все чтобы знать кому слать вобедный блок
                    // а так же чтобы знать с кем мы в синхре или кто лучше нас в checkWeightPeers
                    pointPing = NTP.getTime();
                    ctrl.pingAllPeers(true);

                }

                // is WALLET
                if (BlockChain.TEST_DB > 0 || ctrl.doesWalletExists()
                        || height < BlockChain.ALL_VALID_BEFORE) {

                    if (timePoint > NTP.getTime()) {
                        continue;
                    }

                    local_status = 41;
                    this.setMonitorStatus("local_status " + viewStatus());

                    //CHECK IF WE HAVE CONNECTIONS and READY to GENERATE
                    // если на 1 высота выше хотябы то переходим на синхронизацию
                    // поэтому сдвиг = 0
                    ctrl.checkStatusAndObserve(0);

                    if (BlockChain.TEST_DB == 0 && forgingStatus == ForgingStatus.FORGING_WAIT
                            && (timePoint + (blockTimeMS << 1) < NTP.getTime()
                            || BlockChain.ERA_COMPU_ALL_UP && height < 100
                            || height < 10)) {

                        setForgingStatus(ForgingStatus.FORGING);
                    }

                    if (BlockChain.TEST_DB > 0 ||
                            (forgingStatus == ForgingStatus.FORGING // FORGING enabled
                                    && betterPeer == null && !ctrl.needUpToDate()
                                    && (this.solvingReference == null // AND GENERATING NOT MAKED
                                    || !Arrays.equals(this.solvingReference, dcSet.getBlockMap().getLastBlockSignature())
                            ))
                    ) {

                        /////////////////////////////// TRY FORGING ////////////////////////

                        if (ctrl.isOnStopping()) {
                            return;
                        }

                        //SET NEW BLOCK TO SOLVE
                        this.solvingReference = dcSet.getBlockMap().getLastBlockSignature();
                        solvingBlock = dcSet.getBlockMap().last();

                        if (ctrl.isOnStopping()) {
                            return;
                        }

                        /*
                         * нужно сразу взять транзакции которые бедум в блок класть - чтобы
                         * значть их ХЭШ -
                         * тоже самое и AT записями поидее
                         * и эти хэши закатываем уже в заголвок блока и подписываем
                         * после чего делать вычисление значения ПОБЕДЫ - она от подписи зависит
                         * если победа случиласть то
                         * далее сами трнзакции кладем в тело блока и закрываем его
                         */
                        /*
                         * нет не  так - вычисляеи победное значение и если оно выиграло то
                         * к нему транзакции собираем
                         * и время всегда одинаковое
                         *
                         */

                        local_status = 4;
                        this.setMonitorStatus("local_status " + viewStatus());

                        //GENERATE NEW BLOCKS
                        //this.lastBlocksForTarget = bchain.getLastBlocksForTarget(dcSet);
                        this.acc_winner = null;

                        //unconfirmedTransactionsHash = null;
                        winned_winValue = 0;
                        winned_forgingValue = 0;
                        //max_winned_value_account = 0;
                        height = bchain.getHeight(dcSet) + 1;
                        previousTarget = bchain.getTarget(dcSet);

                        if (BlockChain.TEST_DB == 0) {

                            ///if (height > BlockChain.BLOCK_COUNT) return;

                            //PREVENT CONCURRENT MODIFY EXCEPTION
                            List<PrivateKeyAccount> knownAccounts = this.getKnownAccounts();
                            if (knownAccounts == null) {
                                LOGGER.debug("knownAccounts is NULL");
                                continue;
                            }

                            local_status = 5;
                            this.setMonitorStatus("local_status " + viewStatus());

                            for (PrivateKeyAccount account : knownAccounts) {

                                forgingValue = account.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).intValue();
                                winValue = BlockChain.calcWinValue(dcSet, account, height, forgingValue, null);
                                if (winValue < 1)
                                    continue;

                                targetedWinValue = BlockChain.calcWinValueTargetedBase(dcSet, height, winValue, previousTarget);
                                if (targetedWinValue < 1)
                                    continue;

                                if (winValue > winned_winValue) {
                                    //this.winners.put(account, winned_value);
                                    acc_winner = account;
                                    winned_winValue = winValue;
                                    winned_forgingValue = forgingValue;
                                    //max_winned_value_account = winned_value_account;

                                }
                            }

                            if (BlockChain.CHECK_BUGS > 7) {
                                Tuple2<List<Transaction>, Integer> unconfirmedTransactions
                                        = getUnconfirmedTransactions(height, timePointForValidTX,
                                        bchain, winned_winValue);
                            }
                        } else if (!BlockChain.STOP_GENERATE_BLOCKS) {
                            /// тестовый аккаунт
                            acc_winner = BlockChain.TEST_DB_ACCOUNTS[random.nextInt(BlockChain.TEST_DB_ACCOUNTS.length)];
                            /// закатем в очередь транзакции
                            testTransactions(height, timePointForValidTX);
                        }

                        if (!BlockChain.STOP_GENERATE_BLOCKS && acc_winner != null) {

                            if (ctrl.isOnStopping()) {
                                return;
                            }

                            newWinner = false;
                            // Соберем тут транзакции сразу же чтобы потом не тратить время
                            Tuple2<List<Transaction>, Integer> unconfirmedTransactions
                                    = getUnconfirmedTransactions(height, timePointForValidTX,
                                    bchain, winned_winValue);

                            if (BlockChain.TEST_DB == 0) {

                                wait_new_block_broadcast = (timeStartBroadcast + BlockChain.FLUSH_TIMEPOINT(height)) >> 1;
                                int shiftTime = (int) (((wait_new_block_broadcast * (previousTarget - winned_winValue) * 10) / previousTarget));
                                wait_new_block_broadcast = wait_new_block_broadcast + shiftTime;

                                // сдвиг на заранее - только на 1/4 максимум
                                if (wait_new_block_broadcast < timeStartBroadcast) {
                                    wait_new_block_broadcast = timeStartBroadcast;
                                } else if (wait_new_block_broadcast > BlockChain.FLUSH_TIMEPOINT(height)) {
                                    wait_new_block_broadcast = BlockChain.FLUSH_TIMEPOINT(height);
                                }

                                if (wait_new_block_broadcast > 0
                                        // и мы не отстаем
                                        && NTP.getTime() < timePoint + wait_new_block_broadcast) {

                                    local_status = 6;
                                    this.setMonitorStatus("local_status " + viewStatus());

                                    LOGGER.info("@@@@@@@@ wait for new winner and BROADCAST: " + wait_new_block_broadcast / 1000);
                                    // SLEEP and WATCH break
                                    wait_step = wait_new_block_broadcast / WAIT_STEP_MS;

                                    this.setMonitorStatus("wait for new winner and BROADCAST: " + wait_new_block_broadcast / 1000);

                                    do {
                                        try {
                                            Thread.sleep(WAIT_STEP_MS);
                                        } catch (InterruptedException e) {
                                            return;
                                        }

                                        if (ctrl.isOnStopping()) {
                                            return;
                                        }

                                        waitWin = bchain.getWaitWinBuffer();
                                        if (waitWin != null && waitWin.calcWinValue(dcSet) > winned_winValue) {
                                            // NEW WINNER received
                                            newWinner = true;
                                            break;
                                        }

                                    }
                                    while (this.orphanto <= 0 && this.syncTo <= 0 && wait_step-- > 0
                                            && NTP.getTime() < timePoint + wait_new_block_broadcast
                                            && betterPeer == null && !ctrl.needUpToDate());
                                }

                            }

                            if (this.orphanto > 0 || this.syncTo > 0) {
                                continue;
                            } else if (ctrl.needUpToDate()) {
                                LOGGER.info("skip GENERATING block - need UPDATE");
                            } else if (betterPeer != null) {
                                LOGGER.info("skip GENERATING block - better PERR founf: " + betterPeer);
                            } else {

                                if (newWinner) {
                                    LOGGER.info("NEW WINER RECEIVED - drop my block");
                                } else {
                                    /////////////////////    MAKING NEW BLOCK  //////////////////////
                                    local_status = 7;
                                    this.setMonitorStatus("local_status " + viewStatus());

                                    // GET VALID UNCONFIRMED RECORDS for current TIMESTAMP
                                    LOGGER.info("GENERATE my BLOCK for TXs: " + unconfirmedTransactions.b);

                                    processTiming = System.nanoTime();

                                    try {

                                        Block generatedBlock = generateNextBlock(acc_winner, solvingBlock,
                                                unconfirmedTransactions, winned_forgingValue, winned_winValue, previousTarget);

                                        processTiming = System.nanoTime() - processTiming;

                                        // только если вблоке есть стрнзакции то вычислим
                                        if (generatedBlock.getTransactionCount() > 0
                                                && processTiming < 999999999999l) {
                                            // при переполнении может быть минус
                                            // в миеросекундах подсчет делаем
                                            // ++ 10 потому что там ФОРК базы делаем - он очень медленный
                                            processTiming = processTiming / 1000 /
                                                    (Controller.BLOCK_AS_TX_COUNT + generatedBlock.getTransactionCount());
                                            if (transactionMakeTimingCounter < 1 << 3) {
                                                transactionMakeTimingCounter++;
                                                transactionMakeTimingAverage = ((transactionMakeTimingAverage * transactionMakeTimingCounter)
                                                        + processTiming - transactionMakeTimingAverage) / transactionMakeTimingCounter;
                                            } else
                                                transactionMakeTimingAverage = ((transactionMakeTimingAverage << 3)
                                                        + processTiming - transactionMakeTimingAverage) >> 3;

                                            ctrl.setTransactionMakeTimingAverage(transactionMakeTimingAverage);
                                        }

                                        LOGGER.info("GENERATE done");

                                        if (generatedBlock == null) {
                                            if (ctrl.isOnStopping()) {
                                                return;
                                            }

                                            LOGGER.info("generateNextBlock is NULL... try wait");
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                            }

                                            continue;
                                        } else {
                                            //PASS BLOCK TO CONTROLLER
                                            LOGGER.info("bchain.setWaitWinBuffer, size: " + generatedBlock.getTransactionCount());
                                            if (bchain.setWaitWinBuffer(dcSet, generatedBlock,
                                                    null // не надо банить тут - может цепочка ушла ужеи это мой блок же
                                            )) {

                                                // need to BROADCAST
                                                local_status = 8;
                                                this.setMonitorStatus("local_status " + viewStatus());

                                                ctrl.broadcastWinBlock(generatedBlock);
                                                local_status = 0;
                                                this.setMonitorStatus("local_status " + viewStatus());

                                            } else {
                                                // already close in setWaitWinBuffer - generatedBlock.close();
                                                LOGGER.info("my BLOCK is weak ((...");
                                            }
                                        }
                                    } catch (Exception e) {
                                        LOGGER.error(e.getMessage(), e);
                                        if (BlockChain.CHECK_BUGS > 7) {
                                            ctrl.stopAndExit(106);
                                            return;
                                        }
                                    } catch (java.lang.OutOfMemoryError e) {
                                        LOGGER.error(e.getMessage(), e);
                                        ctrl.stopAndExit(105);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

                height = bchain.getHeight(dcSet);

                ////////////////////////////  FLUSH NEW BLOCK /////////////////////////
                // сдвиг 0 делаем
                ctrl.checkStatusAndObserve(bchain.isEmptyWaitWinBuffer() ? 0 : 1);
                if (betterPeer != null || orphanto > 0 || this.syncTo > 0
                        || timePoint + blockTimeMS < NTP.getTime()
                        && ctrl.needUpToDate()) {

                    if (NTP.getTime() - pointLogWaitFlush > blockTimeMS >> 2) {
                        pointLogWaitFlush = NTP.getTime();

                        bchain.clearWaitWinBuffer(); // close winBlock

                        LOGGER.info("To late for FLUSH - need UPDATE !");
                    }
                } else {

                    // try solve and flush new block from Win Buffer

                    // FLUSH WINER to DB MAP
                    if (this.solvingReference != null)
                        if (NTP.getTime() - pointLogWaitFlush > blockTimeMS >> 2) {
                            pointLogWaitFlush = NTP.getTime();
                            LOGGER.info("wait to FLUSH WINER to DB MAP " + (flushPoint - NTP.getTime()) / 1000);
                        }

                    // ждем основное время просто
                    while (BlockChain.TEST_DB == 0 && this.orphanto <= 0 && this.syncTo <= 0 && flushPoint > NTP.getTime() && betterPeer == null && !ctrl.needUpToDate()) {
                        try {
                            Thread.sleep(WAIT_STEP_MS);
                        } catch (InterruptedException e) {
                            return;
                        }

                        if (ctrl.isOnStopping()) {
                            return;
                        }
                    }

                    if (this.orphanto > 0 || this.syncTo > 0) {
                        bchain.clearWaitWinBuffer(); // close winBlock
                        continue;
                    }

                    if (needRequestWinBlock && bchain.isEmptyWaitWinBuffer() && ctrl.isStatusOK()) {
                        needRequestWinBlock = false;
                        // запросим блок у всех - у нас чето пусто
                        LOGGER.info("requestLastBlock");
                        ctrl.requestLastBlock();
                    }

                    // если мы догоняем время и быстро генерирум цепочку
                    if (!bchain.isEmptyWaitWinBuffer()
                            && NTP.getTime() - timePoint < blockTimeMS << 4) {
                        // то если уже почти догнали время, то начинаем большие задержки ожидания делать
                        try {
                            Thread.sleep(blockTimeMS >> 2);
                        } catch (InterruptedException e) {
                            return;
                        }

                        if (ctrl.isOnStopping()) {
                            return;
                        }

                    }

                    // если нет ничего в буфере то еще немного подождем
                    do {

                        waitWin = bchain.getWaitWinBuffer();
                        if (bchain.getWaitWinBuffer() != null) {
                            LOGGER.debug("wait WniBlock get: " + waitWin);
                            break;
                        }

                        try {
                            Thread.sleep(WAIT_STEP_MS);
                        } catch (InterruptedException e) {
                            return;
                        }

                        if (ctrl.isOnStopping()) {
                            return;
                        }
                    } while (this.orphanto <= 0 && this.syncTo <= 0
                            && timePoint + blockTimeMS > NTP.getTime()
                            // возможно уже надо обновиться - мы отстали
                            && betterPeer == null
                            && !ctrl.needUpToDate());

                    if (this.orphanto > 0 || this.syncTo > 0) {
                        waitWin = null;
                        bchain.clearWaitWinBuffer(); // close winBlock
                        continue;
                    }

                    if (waitWin == null && afterUpdatePeer != null) {
                        waitWin = null;
                        bchain.clearWaitWinBuffer(); // close winBlock
                        waitWin = ctrl.checkNewPeerUpdates(afterUpdatePeer);
                        afterUpdatePeer = null;
                    }

                    // если есть уже победный блок то посчитаем что у нас цепочка на 1 выше
                    ctrl.checkStatusAndObserve(waitWin == null ? 0 : 1);

                    if (waitWin == null) {
                        if (this.solvingReference != null) {
                            if (System.currentTimeMillis() - pointLogGoUpdate > blockTimeMS >> 2) {
                                pointLogGoUpdate = System.currentTimeMillis();
                                // сбросим и ссылку для генератора
                                this.solvingReference = null;
                                LOGGER.debug("WIN BUFFER is EMPTY - go to UPDATE");
                                // обнулим - чтобы потом сработало новое создание
                                this.solvingReference = null;
                            }
                        }

                    } else if (ctrl.needUpToDate()) {
                        // выбрасываем победителя
                        waitWin = null;
                        // и закроем его в буфере
                        bchain.clearWaitWinBuffer(); // close winBlock

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            return;
                        }
                        LOGGER.debug("need UPDATE! skip FLUSH BLOCK");
                    } else if (betterPeer != null) {
                        // выбрасываем победителя - закроем его
                        waitWin = null;
                        bchain.clearWaitWinBuffer(); // close winBlock

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            return;
                        }
                        LOGGER.debug("found better PEER! skip FLUSH BLOCK " + betterPeer);

                    } else {
                        // только если мы не отстали

                        this.solvingReference = null;

                        local_status = 1;
                        this.setMonitorStatus("local_status " + viewStatus());

                        // FLUSH WINER to DB MAP
                        LOGGER.info("TRY to FLUSH WINER to DB MAP");

                        try {
                            if (BlockChain.TEST_DB == 0 && flushPoint + blockTimeMS < NTP.getTime()) {
                                try {
                                    // если вдруг цепочка встала,, то догоняем не очень быстро чтобы принимать все
                                    // победные блоки не спеша
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    return;
                                }
                            }

                            local_status = 2;
                            this.setMonitorStatus("local_status " + viewStatus());

                            try {
                                if (!ctrl.flushNewBlockGenerated()) {
                                    this.setMonitorStatusAfter();
                                    // NEW BLOCK not FLUSHED
                                    LOGGER.info("NEW BLOCK not FLUSHED");
                                } else {
                                    this.setMonitorStatusAfter();
                                    if (forgingStatus == ForgingStatus.FORGING_WAIT)
                                        setForgingStatus(ForgingStatus.FORGING);
                                }
                            } catch (java.lang.OutOfMemoryError e) {
                                LOGGER.error(e.getMessage(), e);
                                ctrl.stopAndExit(135);
                                return;
                            }

                            if (ctrl.isOnStopping()) {
                                return;
                            }

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            if (ctrl.isOnStopping()) {
                                return;
                            }
                            LOGGER.error(e.getMessage(), e);
                        }

                        if (needRemoveInvalids != null) {
                            clearInvalids();
                        } else {
                            checkForRemove(timePointForValidTX);
                            clearInvalids();
                        }

                        // была обработка буфера, тогда на точку начала вернемся
                        continue;
                    }

                }

                ////////////////////////// UPDATE ////////////////////

                if (orphanto > 0 || syncTo > 0 || betterPeer == null &&
                        timePoint + blockTimeMS > NTP.getTime()) {
                    bchain.clearWaitWinBuffer(); // close winBlock
                    continue;
                }

                /// CHECK PEERS HIGHER
                // так как в девелопе все гоняют свои цепочки то посмотреть самыю жирную а не длинную
                ctrl.checkStatusAndObserve(shift_height);
                //CHECK IF WE ARE NOT UP TO DATE
                if (betterPeer != null || ctrl.needUpToDate()) {

                    LOGGER.info("update by " + (betterPeer != null ? "better PEER: " + betterPeer : "controller status"));

                    if (ctrl.isOnStopping()) {
                        return;
                    }

                    local_status = 3;
                    this.setMonitorStatus("local_status " + viewStatus());

                    this.solvingReference = null;

                    afterUpdatePeer = ctrl.update(shift_height);

                    local_status = 0;
                    this.setMonitorStatus("local_status " + viewStatus());

                    if (ctrl.isOnStopping()) {
                        return;
                    }

                    setForgingStatus(ForgingStatus.FORGING_WAIT);
                    needRequestWinBlock = true;

                }
            }

        } catch (java.lang.OutOfMemoryError e) {
            LOGGER.error(e.getMessage(), e);
            ctrl.stopAndExit(196);
            return;
        } catch (Exception e) {
            if (ctrl.isOnStopping()) {
                return;
            }

            LOGGER.error(e.getMessage(), e);

        } catch (Throwable e) {
            if (ctrl.isOnStopping()) {
                return;
            }

            LOGGER.error(e.getMessage(), e);

        } finally {
            // EXITED
            this.local_status = -1;
            this.setMonitorStatus("local_status " + viewStatus());
        }

    }

    @Override
    public void update(Observable arg0, Object arg1) {
        ObserverMessage message = (ObserverMessage) arg1;

        if (message.getType() == ObserverMessage.WALLET_STATUS || message.getType() == ObserverMessage.NETWORK_STATUS) {
            //WALLET ONCE UNLOCKED? WITHOUT UNLOCKING FORGING DISABLED
            if (!walletOnceUnlocked && message.getType() == ObserverMessage.WALLET_STATUS) {
                walletOnceUnlocked = true;
            }

            if (walletOnceUnlocked) {
                // WALLET UNLOCKED OR GENERATOR CACHING TRUE
                syncForgingStatus();
            }
        }

    }

    public void syncForgingStatus() {

        if (!Settings.getInstance().isForgingEnabled() || getKnownAccounts().isEmpty()) {
            setForgingStatus(ForgingStatus.FORGING_DISABLED);
            return;
        }

        int status = ctrl.getStatus();
        //CONNECTIONS OKE? -> FORGING
        // CONNECTION not NEED now !!
        // TARGET_WIN will be small
        if (status != Controller.STATUS_OK
            ///|| ctrl.isProcessingWalletSynchronize()
        ) {
            setForgingStatus(ForgingStatus.FORGING_ENABLED);
            return;
        }

        if (forgingStatus != ForgingStatus.FORGING) {
            setForgingStatus(ForgingStatus.FORGING_WAIT);
        }

		/*
		// NOT NEED to wait - TARGET_WIN will be small
		if (ctrl.isReadyForging())
			setForgingStatus(ForgingStatus.FORGING);
		else
			setForgingStatus(ForgingStatus.FORGING_WAIT);
			*/
    }

    public enum ForgingStatus {

        FORGING_DISABLED(0, Lang.T("Forging disabled")),
        FORGING_ENABLED(1, Lang.T("Forging enabled")),
        FORGING(2, Lang.T("Forging")),
        FORGING_WAIT(3, Lang.T("Forging awaiting another peer sync"));

        private final int statusCode;
        private String name;

        ForgingStatus(int status, String name) {
            statusCode = status;
            this.name = name;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getName() {
            return name;
        }

    }

}
