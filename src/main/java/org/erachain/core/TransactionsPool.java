package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMapSigns;
import org.erachain.datachain.TransactionMapImpl;
import org.erachain.network.message.TransactionMessage;
import org.erachain.ntp.NTP;
import org.erachain.utils.MonitoredThread;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TransactionsPool extends MonitoredThread {

    private final static boolean USE_MONITOR = false;
    private static final boolean LOG_UNCONFIRMED_PROCESS = BlockChain.TEST_MODE;
    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsPool.class.getSimpleName());

    public static final int QUEUE_LENGTH = BlockChain.TEST_DB > 0 ? BlockChain.TEST_DB << 1 :
            BlockChain.MAX_BLOCK_SIZE_GEN >> 1;
    BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<Object>(QUEUE_LENGTH);

    private Controller controller;
    private BlockChain blockChain;
    private DCSet dcSet;
    private TransactionMapImpl utxMap;
    private TransactionFinalMapSigns txSignsMap;
    private boolean needClearMap;

    public long missedTransactions = 0L;

    int height;
    int blockPeriod;
    long heightTimestamp;
    long ntpTimestamp;
    long ntpFutureTimestamp;

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

    public synchronized void needClear(boolean cutDeadTime) {
        needClearMap = true;
        blockingQueue.offer(EMPTY);
    }

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
            utx.resetNotOwnedDApp(); // иначе в ГУИ валится - флаг поднят а контракта нету

            // ADD TO UNCONFIRMED TRANSACTIONS
            // нужно проверять существующие для правильного отображения числа их в статусе ГУИ
            // TODO посмотреть почему сюда двойные записи часто прилетают из sender.network.checkHandledTransactionMessages(data, sender, false)
            if (controller.useGui) {
                // если GUI включено то только если нет в карте то событие пошлется тут
                // возможно пока стояла в осереди другая уже добавилась - но опять же из Пира все дубли должны были убираться
                utxMap.set(utx.getSignature(), utx);
            } else {
                utxMap.putDirect(utx);
            }
        } else if (item instanceof Long) {
            //LOGGER.debug("deleteDirect: {}", Transaction.viewDBRef((Long) item));
            utxMap.deleteDirect((Long) item);

        } else if (item instanceof byte[]) {
            //LOGGER.debug("deleteDirect: {}", Base58.encode((byte[]) item));
            utxMap.deleteDirect((byte[]) item);

        } else if (item instanceof TransactionMessage) {

            long onMessageProcessTiming = System.nanoTime();

            TransactionMessage transactionMessage = (TransactionMessage) item;

            // GET TRANSACTION
            Transaction transaction = transactionMessage.getTransaction();

            // DEADTIME
            if (transaction.getDeadline() < heightTimestamp) {
                LOGGER.warn("Transaction so old: sign {} - tx Deadline {} < {}", transaction.viewSignature(),
                        new Timestamp(transaction.getDeadline()),
                        new Timestamp(heightTimestamp));
                return;
            }

            // FUTURE
            if (transaction.getTimestamp() > ntpFutureTimestamp) {
                LOGGER.warn("Transaction from future: sign {} - tx Time {} > {}", transaction.viewSignature(),
                        new Timestamp(transaction.getTimestamp()),
                        new Timestamp(ntpFutureTimestamp));
                return;
            }

            // ALREADY EXIST in CHAIN
            if (txSignsMap.contains(transaction.getSignature())) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("txSignsMap exits: sign {}", transaction.viewSignature());

                return;
            }

            // ALREADY EXIST in UTX
            byte[] signature = transaction.getSignature();
            long latency = NTP.getTime();
            // проверка на двойной ключ в таблице ожидания транзакций
            if (utxMap.contains(signature)) {
                latency = NTP.getTime() - latency;
                if (LOG_UNCONFIRMED_PROCESS) {
                    if (latency > 20 && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("utxMap CONTAINS latency: {}", latency);
                    }
                }
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("utxMap exits: sign {}", transaction.viewSignature());

                return;
            }

            // CHECK IF SIGNATURE IS VALID ////// ------- OR GENESIS TRANSACTION
            // !! NEED SET UP curren HEIGHT for check
            transaction.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
            if (transaction.getCreator() == null
                    || !transaction.isSignatureValid(dcSet)) {
                // DISHONEST PEER
                transaction.errorValue = "INVALID SIGNATURE";
                if (transactionMessage.getSender() != null) {
                    // in TEST may be NULL
                    transactionMessage.getSender().ban("invalid transaction signature");
                }

                if (LOGGER.isInfoEnabled())
                    LOGGER.info("Signature not Valid: sign {}", transaction.viewSignature());

                return;
            }

            if (LOG_UNCONFIRMED_PROCESS) {
                latency = NTP.getTime() - latency;
                if (latency > 20 && LOGGER.isDebugEnabled()) {
                    LOGGER.debug("isSignatureValid latency: {}", latency);
                }
                latency = NTP.getTime();
            }

            // проверка на двойной ключ в основной таблице транзакций
            if (this.controller.isOnStopping()
                    || BlockChain.CHECK_DOUBLE_SPEND_DEEP == 0
                    && false // теперь не проверяем так как люч сделал длинный dbs.rocksDB.TransactionFinalSignsSuitRocksDB.KEY_LEN
                    && this.dcSet.getTransactionFinalMapSigns().contains(signature)) {
                return;
            }

            if (LOG_UNCONFIRMED_PROCESS) {
                latency = NTP.getTime() - latency;
                if (latency > 30 && LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getTransactionFinalMapSigns CONTAINS latency: {}", latency);
                }
                latency = NTP.getTime();
            }


            // BROADCAST
            controller.network.broadcast(transactionMessage, false);

            // ADD TO UNCONFIRMED TRANSACTIONS
            // нужно проверять существующие для правильного отображения числа их в статусе ГУИ
            // TODO посмотреть почему сюда двойные записи часто прилетают из sender.network.checkHandledTransactionMessages(data, sender, false)
            if (controller.useGui) {
                // если GUI включено, то только если нет в карте то событие пошлется тут
                // возможно пока стояла в осереди другая уже добавилась - но опять же из Пира все дубли должны были убираться
                utxMap.set(transaction.getSignature(), transaction);
            } else {
                utxMap.putDirect(transaction);
            }

            if (LOG_UNCONFIRMED_PROCESS) {
                latency = NTP.getTime() - latency;
                if (latency > 30 && LOGGER.isDebugEnabled()) {
                    LOGGER.debug("utxMap ADD latency: {}", latency);
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

            if (controller.doesWalletKeysExists()) {
                controller.getWallet().insertUnconfirmedTransaction(transaction);
            }

        }
    }

    public void run() {

        runned = true;
        //Message message;
        while (runned) {

            if (height != controller.getMyHeight()) {
                // высота сменилась - пересчитаем края времени
                height = controller.getMyHeight();
                blockPeriod = BlockChain.GENERATING_MIN_BLOCK_TIME_MS(height);
                heightTimestamp = blockChain.getTimestamp(height);
                utxMap.clearByDeadTime(heightTimestamp);
            }

            // Это не зависит от цепочки - всегда пересчитываем
            ntpTimestamp = blockChain.getTimestamp(blockChain.getHeightOnTimestampMS(NTP.getTime()));
            ntpFutureTimestamp = blockChain.getTimestamp(2 + blockChain.getHeightOnTimestampMS(NTP.getTime()));

            // PROCESS
            try {
                processMessage(blockingQueue.poll(blockPeriod >> 1, TimeUnit.MILLISECONDS));
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
