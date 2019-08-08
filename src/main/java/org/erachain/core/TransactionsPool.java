package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionMap;
import org.erachain.network.message.Message;
import org.erachain.network.message.TransactionMessage;
import org.erachain.ntp.NTP;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TransactionsPool extends MonitoredThread {

    private final static boolean USE_MONITOR = true;
    private static final boolean LOG_UNCONFIRMED_PROCESS = BlockChain.DEVELOP_USE? false : false;
    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsPool.class);

    private static final int QUEUE_LENGTH = BlockChain.MAX_BLOCK_SIZE_GEN;
    BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(QUEUE_LENGTH);

    private Controller controller;
    private BlockChain blockChain;
    private DCSet dcSet;
    private TransactionMap txMap;

    public TransactionsPool(Controller controller, BlockChain blockChain, DCSet dcSet) {
        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;
        this.txMap = dcSet.getTransactionMap();

        this.setName("Transactions Pool[" + this.getId() + "]");

        this.start();
    }

    /**
     * @param message
     */
    public boolean offerMessage(Message message) {
        boolean result = blockingQueue.offer(message);
        if (!result) {
            this.controller.network.missedTransactions.incrementAndGet();
        }
        return result;
    }

    private int clearCount;
    public void processMessage(Message message) {

        if (message == null)
            return;

        long timeCheck = System.nanoTime();
        long onMessageProcessTiming = timeCheck;

        TransactionMessage transactionMessage = (TransactionMessage) message;

        // GET TRANSACTION
        Transaction transaction = transactionMessage.getTransaction();

        // CHECK IF SIGNATURE IS VALID ////// ------- OR GENESIS TRANSACTION
        if (transaction.getCreator() == null
                || !transaction.isSignatureValid(DCSet.getInstance())) {
            // DISHONEST PEER
            this.controller.banPeerOnError(message.getSender(), "invalid transaction signature");

            return;
        }

        // DEADTIME
        if (transaction.getDeadline() < this.blockChain.getTimestamp(this.dcSet)) {
            // so OLD transaction
            return;
        }

        if (LOG_UNCONFIRMED_PROCESS) {
            timeCheck = System.currentTimeMillis() - timeCheck;
            if (timeCheck > 10) {
                LOGGER.debug("TRANSACTION_TYPE proccess 1 period: " + timeCheck);
            }
        }

        // ALREADY EXIST
        byte[] signature = transaction.getSignature();

        if (LOG_UNCONFIRMED_PROCESS)
            timeCheck = System.currentTimeMillis();

        if (txMap.contains(signature)) {
            if (LOG_UNCONFIRMED_PROCESS) {
                timeCheck = System.currentTimeMillis() - timeCheck;
                if (timeCheck > 20) {
                    LOGGER.debug("TRANSACTION_TYPE process CONTAINS in UNC period: " + timeCheck);
                }
            }
            return;
        }
        if (LOG_UNCONFIRMED_PROCESS) {
            timeCheck = System.currentTimeMillis() - timeCheck;
            if (timeCheck > 20) {
                LOGGER.debug("TRANSACTION_TYPE proccess CONTAINS in UNC period: " + timeCheck);
            }
        }

        if (LOG_UNCONFIRMED_PROCESS)
            timeCheck = System.currentTimeMillis();

        if (this.dcSet.getTransactionFinalMapSigns().contains(signature) || this.controller.isOnStopping()) {
            return;
        }

        if (LOG_UNCONFIRMED_PROCESS) {
            timeCheck = System.currentTimeMillis() - timeCheck;
            if (timeCheck > 30) {
                LOGGER.debug("TRANSACTION_TYPE proccess CONTAINS in FINAL period: " + timeCheck);
            }
        }

        // ADD TO UNCONFIRMED TRANSACTIONS
        txMap.add(transaction);

        if (LOG_UNCONFIRMED_PROCESS) {
            timeCheck = System.currentTimeMillis() - timeCheck;
            if (timeCheck > 30) {
                LOGGER.debug("TRANSACTION_TYPE proccess ADD period: " + timeCheck);
            }
        }

        // время обработки считаем тут
        onMessageProcessTiming = System.nanoTime() - onMessageProcessTiming;
        if (onMessageProcessTiming < 999999999999l) {
            // при переполнении может быть минус
            // в миеросекундах подсчет делаем
            onMessageProcessTiming /= 1000;
            this.controller.unconfigmedMessageTimingAverage = ((this.controller.unconfigmedMessageTimingAverage << 8)
                    + onMessageProcessTiming - this.controller.unconfigmedMessageTimingAverage) >> 8;
        }

        // проверяем на переборт трнзакций в пуле чтобы лишние очистить
        if (++clearCount > 1000) {

            clearCount = 0;

            if (controller.isStatusOK()) {
                if (txMap.size() > BlockChain.MAX_UNCONFIGMED_MAP_SIZE) {
                    controller.clearUnconfirmedRecords(true);
                }
            } else {
                // если идет синхронизация, то удаляем все что есть не на текущее время
                // и так как даже если мы вот-вот засинхримся мы все равно блок не сможем сразу собрать
                // из-за мягкой синхронизации с сетью - а значит и нам не нужно заботиться об удаленных трнзакциях
                // у нас - они будут включены другими нодами которые полностью в синхре
                // мы выстыпаем лишь как ретрнслятор - при этом у нас запас по времени хранения все равно должен быть
                // чтобы помнить какие транзакции мы уже словили и ретранслировали
                if (txMap.size() > BlockChain.MAX_BLOCK_SIZE_GEN >> 1) {
                    txMap.clearByDeadTimeAndLimit(NTP.getTime(), true);
                }
            }
        }

        // если мы не в синхронизации - так как мы тогда
        // не знаем время текущее цепочки и не понимаем можно ли борадкастить дальше трнзакцию
        // так как непонятно - протухла она или нет

        // BROADCAST
        controller.network.broadcast(message, false);

        return;
    }

    public void run() {

        runned = true;
        //Message message;
        while (runned) {
            try {
                processMessage(blockingQueue.take());
            } catch (OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAll(56);
                return;
            } catch (IllegalMonitorStateException e) {
                break;
            } catch (InterruptedException e) {
                break;
            }

        }

        LOGGER.info("Transactions Pool halted");
    }

    public void halt() {
        this.runned = false;
    }

}
