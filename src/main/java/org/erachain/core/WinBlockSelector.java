package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.datachain.DCSet;
import org.erachain.network.MessagesProcessor;
import org.erachain.network.Peer;
import org.erachain.network.message.*;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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

    private void sendWinOrLastBlockToPeer(Peer peer) {
        Block myWinBlock = blockChain.getWaitWinBuffer();
        myWinBlock = myWinBlock == null ? blockChain.getLastBlock(dcSet) : myWinBlock;
        if (myWinBlock != null) {
            LOGGER.debug("send my last or Win " + myWinBlock + " to " + peer);
            peer.sendWinBlock((BlockWinMessage) MessageFactory.getInstance().createWinBlockMessage(myWinBlock));
        }
    }

    public void processMessage(Message message) throws Exception {

        if (message == null)
            return;

        long onMessageProcessTiming = System.nanoTime();

        BlockWinMessage blockWinMessage = (BlockWinMessage) message;

        // ASK BLOCK FROM BLOCKCHAIN
        Block newBlock = blockWinMessage.getBlock();
        LOGGER.info("received new WIN Block from " + blockWinMessage.getSender().getAddress() + " "
                + newBlock);

        if (newBlock.heightBlock == controller.getMyHeight() + 2) {
            LOGGER.debug("newBlock.height == height + 2");
            // это может быть новый блок уже после того как мы засинхрились и словили ПогбедныйБлок предпоследний
            Block previousWaitWinBlock = blockChain.getWaitWinBuffer();
            if (previousWaitWinBlock == null) {
                LOGGER.debug("previous WaitWinBlock == null - try get it form peer");
                previousWaitWinBlock = ((BlockMessage) message.getSender().getResponse(new GetBlockMessage(newBlock.getReference()))).getBlock();
                if (previousWaitWinBlock == null) {
                    LOGGER.debug("previous WaitWinBlock from peer = null");
                    return;
                }

                if (!blockChain.setWaitWinBuffer(dcSet, previousWaitWinBlock, message.getSender())) {
                    LOGGER.debug("previous WaitWinBlock from peer is Invalid");
                    return;
                }
            }

            if (Arrays.equals(previousWaitWinBlock.getSignature(), newBlock.getReference())) {
                // Если действительно жто родитель нового Победного то его нужно залить в нашу цепочку сперва
                LOGGER.debug("previous WaitWinBlock sign = ref new Block. Try flush previous");
                if (!controller.flushNewBlockGenerated()) {
                    return;
                }
            }
        } else if (newBlock.heightBlock != controller.getMyHeight() + 1) {
            // иначе пропускаем
            LOGGER.debug("newBlock.height != height + 1 - ignore {}", newBlock);
            return;
        }

        // если мы синхронизируемся - то берем победный блок а потои
        // его перепроверим при выходе из синхронизации
        if (this.controller.isStatusSynchronizing()) {
            LOGGER.info("ADD unchecked on Synchronizing - " + newBlock + " from " + blockWinMessage.getSender().getAddress());
            blockChain.setWaitWinBufferUnchecked(newBlock);
            // и разошлем его дальше тоже, так как если мы выпали в оставание то всем свои перешлем все равно
            controller.network.broadcastWinBlock(blockWinMessage, false);
            return;
        }

        int invalid = newBlock.isValidHead(dcSet);

        if (invalid > 0) {
            // то проверим заголовок
            LOGGER.info("Block[{}] HEAD is Invalid={} - ignore", newBlock.toString(), invalid);

            if (invalid <= Block.INVALID_REFERENCE) {
                // на всякий случай вышлем свой блок - возможно это как раз запрос на посылку нашего победного блока
                // а если у нас уже в буфере нет, то пошлем наш последний блок
                sendWinOrLastBlockToPeer(message.getSender());
            }

            return;
        }

        // тут внутри проверка полной валидности
        if (blockChain.setWaitWinBuffer(dcSet, newBlock,
                message.getSender() // тут забаним пир если не сошелся так ка заголовок то верный был
        )) {
            // IF IT WIN
            // BROADCAST
            //List<Peer> excludes = new ArrayList<Peer>();
            //excludes.add(message.getSender());
            controller.network.broadcastWinBlock(blockWinMessage, false);

            onMessageProcessTiming = System.nanoTime() - onMessageProcessTiming;
            if (onMessageProcessTiming < 999999999999l) {
                // при переполнении может быть минус
                // в миеросекундах подсчет делаем
                Controller.getInstance().getBlockChain().updateTXWinnedTimingAverage(onMessageProcessTiming, newBlock.getTransactionCount());
            }

        } else {
            // на всякий случай вышлем свой блок - возможно это как раз запрос на посылку нашего победного блока
            // а если у нас уже в буфере нет, то пошлем наш последний блок
            sendWinOrLastBlockToPeer(message.getSender());
        }
    }

    public void run() {

        runned = true;

        while (runned) {
            try {
                Message message = blockingQueue.take();
                processMessage(message);
            } catch (java.lang.OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(566);
                break;
            } catch (java.lang.IllegalMonitorStateException e) {
                blockingQueue = null;
                Controller.getInstance().stopAndExit(567);
                break;
            } catch (java.lang.InterruptedException e) {
                break;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                if (BlockChain.CHECK_BUGS > 9) {
                    // тут нельзя выходить так как просто битым блоком смогут все ноды убить при атаке
                    Controller.getInstance().stopAndExit(569);
                    break;
                }
            }

        }

        blockingQueue = null;

        LOGGER.info("WinBlock Selector halted");
    }

    public void halt() {
        this.runned = false;
    }

}
