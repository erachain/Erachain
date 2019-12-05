package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.BlockWinMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WinBlockSelector extends MonitoredThread {

    private final static boolean USE_MONITOR = true;
    private final static boolean logPings = true;
    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(WinBlockSelector.class.getSimpleName());

    private static final int QUEUE_LENGTH = 8 + (64 >> (Controller.HARD_WORK>>1));
    BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(QUEUE_LENGTH);

    private Controller controller;
    private BlockChain blockChain;
    private DCSet dcSet;

    public WinBlockSelector(Controller controller, BlockChain blockChain, DCSet dcSet) {
        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;

        this.setName("WinBlockSelector[" + this.getId() + "]");

        this.start();
    }

    /**
     * @param message
     */
    public boolean offerMessage(Message message) {

        boolean result = blockingQueue.offer(message);
        if (!result) {
            this.controller.network.missedWinBlocks.incrementAndGet();
        }
        return result;
    }

    public void processMessage(Message message) {

        if (message == null)
            return;

        long onMessageProcessTiming = System.nanoTime();

        BlockWinMessage blockWinMessage = (BlockWinMessage) message;

        // ASK BLOCK FROM BLOCKCHAIN
        Block newBlock = blockWinMessage.getBlock();

        // если мы синхронизируемся - то берем победный блок а потои
        // его перепроверим при выходе из синхронизации
        if (this.controller.isStatusSynchronizing()) {
            blockChain.setWaitWinBufferUnchecked(newBlock);
            return;
        }
        String info = " received new WIN Block from " + blockWinMessage.getSender().getAddress() + " "
                + newBlock.toString();
        LOGGER.info(info);

        if (!newBlock.isValidHead(dcSet)) {
            // то проверим заголовок
            info = "Block HEAD is Invalid - ignore " + newBlock.toString();
            LOGGER.info(info);
            return;
        }

        // тут внутри проверка полной валидности
        if (blockChain.setWaitWinBuffer(dcSet, newBlock, message.getSender())) {
            // IF IT WIN
            // BROADCAST
            //List<Peer> excludes = new ArrayList<Peer>();
            //excludes.add(message.getSender());
            message.getSender().network.broadcastWinBlock(blockWinMessage, false);

            onMessageProcessTiming = System.nanoTime() - onMessageProcessTiming;
            if (onMessageProcessTiming < 999999999999l) {
                // при переполнении может быть минус
                // в миеросекундах подсчет делаем
                Controller.getInstance().getBlockChain().updateTXWinnedTimingAverage(onMessageProcessTiming, newBlock.getTransactionCount());
            }

        } else {
            // SEND my BLOCK
            Block myWinBlock = blockChain.getWaitWinBuffer();
            if (myWinBlock != null) {
                message.getSender().sendWinBlock((BlockWinMessage)MessageFactory.getInstance().createWinBlockMessage(myWinBlock));
            }
        }
    }

    public void run() {

        runned = true;

        while (runned) {
            try {
                processMessage(blockingQueue.take());
            } catch (java.lang.OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAll(66);
                break;
            } catch (java.lang.IllegalMonitorStateException e) {
                Controller.getInstance().stopAll(67);
                break;
            } catch (java.lang.InterruptedException e) {
                break;
            }

        }

        LOGGER.info("WinBlock Selector halted");
    }

    public void halt() {
        this.runned = false;
    }

}
