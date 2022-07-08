package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.erachain.datachain.TransactionMapImpl;
import org.erachain.network.message.TransactionMessage;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TransactionsPool extends MonitoredThread {

    private final static boolean USE_MONITOR = false;
    private static final boolean LOG_UNCONFIRMED_PROCESS = BlockChain.TEST_MODE;
    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsPool.class.getSimpleName());

    private static final int QUEUE_LENGTH = BlockChain.TEST_DB > 0 ? BlockChain.TEST_DB << 1 :
            BlockChain.MAX_BLOCK_SIZE_GEN >> 1;
    BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<Object>(QUEUE_LENGTH);

    private Controller controller;
    private BlockChain blockChain;
    private DCSet dcSet;
    private TransactionMapImpl utxMap;
    private TransactionFinalMapSigns txSignsMap;
    private boolean needClearMap;

    public long missedTransactions = 0L;

    public TransactionsPool(Controller controller, BlockChain blockChain, DCSet dcSet) {
        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;
        this.utxMap = dcSet.getTransactionTab();
        this.txSignsMap = dcSet.getTransactionFinalMapSigns();

        this.setName("Transactions Pool[" + this.getId() + "]");

        this.start();

        dcSet.getTransactionTab().setPool(this);

    }

    /**
     * check utxMap.isClosed
     *
     * @param sign
     * @return
     */
    public boolean contains(byte[] sign) {
        if (utxMap.isClosed())
            return false;
        return utxMap.contains(sign);
    }

    /**
     * check utxMap.isClosed
     *
     * @param sign
     * @return
     */
    public Transaction get(byte[] sign) {
        if (utxMap.isClosed())
            return null;
        return utxMap.get(sign);
    }

    /**
     * @param item
     */
    public boolean offerMessage(Object item) {
        boolean result = blockingQueue.offer(item);
        if (!result) {
            ++missedTransactions;
        }
        return result;
    }

    protected Boolean EMPTY = Boolean.TRUE;
    protected boolean cutDeadTime;

    public synchronized void needClear(boolean cutDeadTime) {
        needClearMap = true;
        this.cutDeadTime = cutDeadTime;
        blockingQueue.offer(EMPTY);
    }

    private int clearCount;
    private long pointClear;

    /**
     * If value as TransactionMessage - test sign and add. If value as Transaction - add without test sign, value as Long - delete
     */
    public void processMessage(Object item) {

        if (item == null)
            return;

        if (item instanceof Transaction) {

            if (txSignsMap.contains(((Transaction) item).getSignature()))
                return;

            Transaction utx = (Transaction) item;
            utx.resetEpochDAPP(); // иначе в ГУИ валится - флаг полднят а контракта нету

            // ADD TO UNCONFIRMED TRANSACTIONS
            // нужно проверять существующие для правильного отображения числа их в статусе ГУИ
            // TODO посмотреть почему сюда двойные записи часто прилетают из sender.network.checkHandledTransactionMessages(data, sender, false)
            if (controller.useGui) {
                // если GUI включено то только если нет в карте то событие пошлется тут
                // возможно пока стояла в осереди другая уже добавилась - но опять же из Пира все дубли должны были убираться
                if (!utxMap.set(utx.getSignature(), utx)) {
                    clearCount++;
                }
            } else {
                utxMap.putDirect(utx);
                clearCount++;
            }
        } else if (item instanceof Long) {
            utxMap.deleteDirect((Long) item);

        } else if (item instanceof byte[]) {
            utxMap.deleteDirect((byte[]) item);

        } else if (item instanceof TransactionMessage) {


            long timeCheck = System.currentTimeMillis();
            long onMessageProcessTiming = System.nanoTime();

            TransactionMessage transactionMessage = (TransactionMessage) item;

            // GET TRANSACTION
            Transaction transaction = transactionMessage.getTransaction();
            if (txSignsMap.contains(transaction.getSignature()))
                return;

            long currentTimestamp = this.blockChain.getTimestamp(this.dcSet);
            // DEADTIME
            if (transaction.getDeadline() < this.blockChain.getTimestamp(this.dcSet)
                    || transaction.getTimestamp() > currentTimestamp + 3600000) {
                // so OLD transaction or from future
                return;
            }

            // ALREADY EXIST
            byte[] signature = transaction.getSignature();

            // проверка на двойной ключ в таблице ожидания транзакций
            if (utxMap.contains(signature)) {
                if (LOG_UNCONFIRMED_PROCESS) {
                    timeCheck = System.currentTimeMillis() - timeCheck;
                    if (timeCheck > 20) {
                        LOGGER.debug("TRANSACTION_TYPE process CONTAINS in UNC period: " + timeCheck);
                    }
                }
                return;
            }

            // CHECK IF SIGNATURE IS VALID ////// ------- OR GENESIS TRANSACTION
            // !! NEED SET UP curren HEIGHT for check
            transaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
            if (transaction.getCreator() == null
                    || !transaction.isSignatureValid(DCSet.getInstance())) {
                // DISHONEST PEER
                transaction.errorValue = "INVALID SIGNATURE";
                if (transactionMessage.getSender() != null) {
                    // in TEST may be NULL
                    transactionMessage.getSender().ban("invalid transaction signature");
                }

                return;
            }

            if (LOG_UNCONFIRMED_PROCESS) {
                timeCheck = System.currentTimeMillis() - timeCheck;
                if (timeCheck > 20) {
                    LOGGER.debug("TRANSACTION_TYPE proccess CONTAINS in UNC period: " + timeCheck);
                }
                timeCheck = System.currentTimeMillis();
            }

            // проверка на двойной ключ в основной таблице транзакций
            if (this.controller.isOnStopping()
                    || BlockChain.CHECK_DOUBLE_SPEND_DEEP == 0
                    && false // теперь не проверяем так как люч сделал длинный dbs.rocksDB.TransactionFinalSignsSuitRocksDB.KEY_LEN
                    && this.dcSet.getTransactionFinalMapSigns().contains(signature)) {
                return;
            }

            if (LOG_UNCONFIRMED_PROCESS) {
                timeCheck = System.currentTimeMillis() - timeCheck;
                if (timeCheck > 30) {
                    LOGGER.debug("TRANSACTION_TYPE process CONTAINS in FINAL period: " + timeCheck);
                }
                timeCheck = System.currentTimeMillis();
            }


            // ADD TO UNCONFIRMED TRANSACTIONS
            // нужно проверять существующие для правильного отображения числа их в статусе ГУИ
            // TODO посмотреть почему сюда двойные записи часто прилетают из sender.network.checkHandledTransactionMessages(data, sender, false)
            if (controller.useGui) {
                // если GUI включено то только если нет в карте то событие пошлется тут
                // возможно пока стояла в осереди другая уже добавилась - но опять же из Пира все дубли должны были убираться
                if (!utxMap.set(transaction.getSignature(), transaction)) {
                    clearCount++;
                }
            } else {
                utxMap.putDirect(transaction);
                clearCount++;
            }

            if (LOG_UNCONFIRMED_PROCESS) {
                timeCheck = System.currentTimeMillis() - timeCheck;
                if (timeCheck > 30) {
                    LOGGER.debug("TRANSACTION_TYPE process ADD period: " + timeCheck);
                }
            }

            // время обработки считаем тут
            onMessageProcessTiming = System.nanoTime() - onMessageProcessTiming;
            if (onMessageProcessTiming < 999999999999L) {
                // при переполнении может быть минус
                // в миеросекундах подсчет делаем
                onMessageProcessTiming /= 1000;
                this.controller.unconfigmedMessageTimingAverage = ((this.controller.unconfigmedMessageTimingAverage << 8)
                        + onMessageProcessTiming - this.controller.unconfigmedMessageTimingAverage) >> 8;
            }

            // BROADCAST
            controller.network.broadcast(transactionMessage, false);

            if (controller.doesWalletKeysExists()) {
                controller.getWallet().insertUnconfirmedTransaction(transaction);
            }

        }
    }

    public void run() {

        long poinClear = 0;
        long poinCleared = 0;
        int clearedUTXs = 0;

        long minorClear = 0;

        runned = true;
        //Message message;
        while (runned) {

            if (System.currentTimeMillis() - poinClear > 60000 || clearCount - poinCleared > 10000) {

                /////// CLEAR
                try {

                    poinClear = System.currentTimeMillis();

                    // проверяем на переполнение пула чтобы лишние очистить
                    boolean isStatusOK = controller.isStatusOK();
                    needClearMap = !DCSet.needResetUTXPoolMap
                            && clearCount > (BlockChain.TEST_DB > 0 ? BlockChain.TEST_DB << 2 : ((isStatusOK ? QUEUE_LENGTH << 2 : 1000) << (Controller.HARD_WORK >> 2)))
                            || DCSet.needResetUTXPoolMap && clearCount > QUEUE_LENGTH << (5 + (Controller.HARD_WORK >> 2));

                    int sizeUTX = utxMap.size();
                    if (needClearMap) {
                        // очистим транзакции из очереди
                        needClearMap = false;

                        /// те транзакции что съела сборка блока тоже учтем
                        clearedUTXs += clearCount - sizeUTX;

                        poinCleared = clearCount = 0;

                        if (isStatusOK) {
                            if (sizeUTX > BlockChain.MAX_UNCONFIGMED_MAP_SIZE) {
                                int height = dcSet.getBlocksHeadsMap().size();
                                long timestamp = Controller.getInstance().getBlockChain().getTimestamp(height);
                                long keepTime = BlockChain.GENERATING_MIN_BLOCK_TIME_MS(timestamp) << 3;
                                keepTime = timestamp - (keepTime >> 1) + (keepTime << (5 - Controller.HARD_WORK >> 1));
                                clearedUTXs += utxMap.clearByDeadTimeAndLimit(keepTime, false);
                            }
                        } else {
                            // если идет синхронизация, то удаляем все что есть не на текущее время
                            // и так как даже если мы вот-вот засинхримся мы все равно блок не сможем сразу собрать
                            // из-за мягкой синхронизации с сетью - а значит и нам не нужно заботиться об удаленных трнзакциях
                            // у нас - они будут включены другими нодами которые полностью в синхре
                            // мы выступаем лишь как ретранслятор - при этом у нас запас по времени хранения все равно должен быть
                            // чтобы помнить какие транзакции мы уже словили и ретранслировали
                            if (sizeUTX > BlockChain.MAX_UNCONFIGMED_MAP_SIZE >> 3) {
                                int height = dcSet.getBlocksHeadsMap().size();
                                long timestamp = Controller.getInstance().getBlockChain().getTimestamp(height);
                                long keepTime = BlockChain.GENERATING_MIN_BLOCK_TIME_MS(timestamp) << 3;
                                keepTime = timestamp - (keepTime >> 1) + (keepTime << (5 - Controller.HARD_WORK >> 1));
                                clearedUTXs += utxMap.clearByDeadTimeAndLimit(keepTime, true);
                            }
                        }
                    } else if (isStatusOK &&
                            System.currentTimeMillis() - minorClear > BlockChain.GENERATING_MIN_BLOCK_TIME_MS(0)) {
                        // each block
                        minorClear = System.currentTimeMillis();
                        int height = dcSet.getBlocksHeadsMap().size();
                        long keepTime = Controller.getInstance().getBlockChain().getTimestamp(height)
                                - BlockChain.GENERATING_MIN_BLOCK_TIME_MS(0);
                        clearedUTXs += utxMap.clearByDeadTimeAndLimit(keepTime, true);
                    }

                    boolean needReset = clearedUTXs > DCSet.DELETIONS_BEFORE_COMPACT >> (isStatusOK ? 0 : 3)
                            //|| System.currentTimeMillis() - poinClear - 1000 > BlockChain.GENERATING_MIN_BLOCK_TIME_MS(height) << 3
                            ;
                    // reset Map & repopulate UTX table
                    if (needReset) {

                        int height = dcSet.getBlocksHeadsMap().size();

                        clearedUTXs = 0;
                        LOGGER.debug("try RESET POOL UTXs");
                        poinClear = System.currentTimeMillis();
                        LOGGER.debug("try RESET POOL UTXs, size: " + sizeUTX);
                        // нужно скопировать из таблици иначе после закрытия ее ошибка обращения
                        // так .values() выдает не отдельный массив а объект базы данных!
                        Transaction[] items = utxMap.values().toArray(new Transaction[]{});
                        utxMap.clear();
                        clearCount = 0;
                        long timestamp = Controller.getInstance().getBlockChain().getTimestamp(height);
                        int countDeleted = 0;
                        if (sizeUTX < BlockChain.MAX_UNCONFIGMED_MAP_SIZE) {
                            for (Transaction item : items) {
                                if (timestamp > item.getDeadline()) {
                                    countDeleted++;
                                    continue;
                                }
                                utxMap.put(item);
                            }
                            clearCount += countDeleted;
                            LOGGER.debug("ADDED UTXs: " + utxMap.size() + " for " + (System.currentTimeMillis() - poinClear)
                                    + " ms, DELETED by Deadlime:  " + countDeleted);
                        } else {
                            // переполненение - удалим все старые
                            int i = sizeUTX;
                            Transaction item;
                            do {
                                item = items[--i];
                                if (timestamp > item.getDeadline())
                                    continue;
                                utxMap.put(item);
                            } while (sizeUTX - i < BlockChain.MAX_UNCONFIGMED_MAP_SIZE);
                            countDeleted = sizeUTX - utxMap.size();
                            clearCount += countDeleted;
                            LOGGER.debug("ADDED UTXs: " + utxMap.size() + " for " + (System.currentTimeMillis() - poinClear)
                                    + " ms, DELETED by oversize:  " + countDeleted);
                        }
                    }
                } catch (OutOfMemoryError e) {
                    LOGGER.error(e.getMessage(), e);
                    Controller.getInstance().stopAndExit(456);
                    return;
                } catch (IllegalMonitorStateException e) {
                    break;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }


            // PROCESS
            try {
                processMessage(blockingQueue.take());
            } catch (OutOfMemoryError e) {
                blockingQueue = null;
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(457);
                return;
            } catch (IllegalMonitorStateException e) {
                blockingQueue = null;
                Controller.getInstance().stopAndExit(458);
                break;
            } catch (InterruptedException e) {
                blockingQueue = null;
                break;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }

        }

        LOGGER.info("Transactions Pool halted");
    }

    public void halt() {
        this.runned = false;
        interrupt();
    }

}
