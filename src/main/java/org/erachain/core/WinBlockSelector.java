package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.datachain.DCSet;
import org.erachain.network.Peer;
import org.erachain.network.message.BlockWinMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class WinBlockSelector extends MonitoredThread {

    private final static boolean USE_MONITOR = true;
    private final static boolean logPings = true;
    private static final int OFFER_WAIT = 20000;

    private static final Logger LOGGER = LoggerFactory.getLogger(WinBlockSelector.class);

    private static final int QUEUE_LENGTH = 20;
    BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(QUEUE_LENGTH);

    private Controller controller;
    private BlockChain blockChain;
    private DCSet dcSet;

    private HashSet<String> waitWinBufferProcessed = new HashSet<String>();

    public WinBlockSelector(Controller controller, BlockChain blockChain, DCSet dcSet) {
        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;

        this.setName("WinBlockSelector[" + this.getId() + "]");

        this.start();
    }

    public void clearWaitWinBufferProcessed() {
        waitWinBufferProcessed = new HashSet<String>();
    }


    /**
     * @param message
     */
    public synchronized boolean putMessage(Message message) {
        try {
            return blockingQueue.offer(message, OFFER_WAIT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void processMessage(Message message) {

        long onMessageProcessTiming = System.nanoTime();

        BlockWinMessage blockWinMessage = (BlockWinMessage) message;

        // ASK BLOCK FROM BLOCKCHAIN
        Block newBlock = blockWinMessage.getBlock();

        // if already it block in process
        String key = newBlock.getCreator().getBase58();

        synchronized (this.waitWinBufferProcessed) {
            if (!waitWinBufferProcessed.add(key)
                    || Arrays.equals(dcSet.getBlockMap().getLastBlockSignature(), newBlock.getSignature()))
                return;
        }

        String info = " received new WIN Block from " + blockWinMessage.getSender().getAddress() + " "
                + newBlock.toString();
        LOGGER.debug(info);

        if (controller.getStatus() == controller.STATUS_SYNCHRONIZING) {
            if (false) {
                // SET for FUTURE without CHECK
                blockChain.clearWaitWinBuffer();
                // тут он невалидный точно
                blockChain.setWaitWinBuffer(dcSet, newBlock, blockWinMessage.getSender());
                return;
            } else
                // нет нельзя его в буфер класть так как там нет проверки потом на валидность
                return;
        }

        if (!newBlock.isValidHead(dcSet)) {
            info = "Block (" + newBlock.toString() + ") is Invalid";
            message.getSender().ban(30, info);
            return;
        }

        // тут внутри проверка полной валидности
        if (blockChain.setWaitWinBuffer(dcSet, newBlock, message.getSender())) {
            // IF IT WIN
            // BROADCAST
            List<Peer> excludes = new ArrayList<Peer>();
            excludes.add(message.getSender());
            message.getSender().network.asyncBroadcastWinBlock(blockWinMessage, excludes, false);

            onMessageProcessTiming = System.nanoTime() - onMessageProcessTiming;

            if (onMessageProcessTiming < 999999999999l) {
                // при переполнении может быть минус
                // в миеросекундах подсчет делаем
                // ++ 10 потому что там ФОРК базы делаем - он очень медленный
                onMessageProcessTiming = onMessageProcessTiming / 1000 / (10 + newBlock.getTransactionCount());
                if (controller.transactionMessageTimingCounter < 1 << 3) {
                    controller.transactionMessageTimingCounter++;
                    controller.transactionMessageTimingAverage = ((controller.transactionMessageTimingAverage
                            * controller.transactionMessageTimingCounter)
                            + onMessageProcessTiming - controller.transactionMessageTimingAverage)
                            / controller.transactionMessageTimingCounter;
                } else
                    controller.transactionMessageTimingAverage = ((controller.transactionMessageTimingAverage << 3)
                            + onMessageProcessTiming - controller.transactionMessageTimingAverage) >> 3;
            }

        } else {
            // SEND my BLOCK

            // GET till not NULL
            Block myWinBlock = blockChain.getWaitWinBuffer();
            if (myWinBlock != null) {
                // оказывается иногда там НУЛ случается
                // надо сначала взять, проскрить и потом слать
                Message messageBestWin = MessageFactory.getInstance()
                        .createWinBlockMessage(myWinBlock);
                message.getSender().putMessage(messageBestWin);
            }
        }
    }

    public void run() {

        while (true) {

            Message message = null;
            message = blockingQueue.poll();

            if (message == null)
                continue;

            processMessage(message);
        }
    }

}
