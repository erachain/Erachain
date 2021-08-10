package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.datachain.DCSet;
import org.erachain.network.message.GetBlockMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlocksRequest extends MonitoredThread {

    private final static boolean USE_MONITOR = false;
    //private final static boolean logPings = true;
    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(BlocksRequest.class.getSimpleName());

    /**
     * запросов может быть много - они очередь не сильно нагружают - так чтобы разрывов часто не было
     * иначе buffer-3 = null ошибка на той стороне вылетает
     */
    private static final int QUEUE_LENGTH = 300 + (256 >> (Controller.HARD_WORK >> 1));
    /**
     * число выданных транзакций
     */
    private static final int TX_COUNTER_WAIT = 1000 << Controller.HARD_WORK;

    BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(QUEUE_LENGTH);

    private Controller controller;
    private BlockChain blockChain;
    private DCSet dcSet;

    public BlocksRequest(Controller controller, BlockChain blockChain, DCSet dcSet) {
        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;

        this.setName("BlockRequest[" + this.getId() + "]");

        this.start();
    }

    /**
     * @param message
     */
    public void offerMessage(Message message) {
        if (!blockingQueue.offer(message)) {
            LOGGER.debug("skip ---- " + message);
        }
    }

    public int processMessage(Message message) {

        if (message == null)
            return 0;

        GetBlockMessage getBlockMessage = (GetBlockMessage) message;


        if (USE_MONITOR) {
            LOGGER.debug("controller.Controller.onMessage(Message).GET_BLOCK_TYPE ->.getSignature()"
                    + " form PEER: " + getBlockMessage.getSender()
                    + " sign: " + Base58.encode(getBlockMessage.getSignature()));
            this.setMonitorStatus("try GET_BLOCK " + Base58.encode(getBlockMessage.getSignature()));
        }

        // ASK BLOCK FROM BLOCKCHAIN
        Block newBlock = this.blockChain.getBlock(dcSet, getBlockMessage.getSignature());

        if (USE_MONITOR) {
            LOGGER.debug(newBlock == null? "NOT found" : "found at " + newBlock.getHeight());
            this.setMonitorStatusAfter();
            if (newBlock == null) {
                String mess = "Block NOT FOUND for sign:" + getBlockMessage.getSignature();
                this.setMonitorStatus(mess);
            }
        }

        if (newBlock == null) {
            String mess = "Block NOT FOUND for sign:" + getBlockMessage.getSignature();
            //Controller.getInstance().banPeerOnError(message.getSender(), mess);
            return 0;
        }

        // CREATE RESPONSE WITH SAME ID
        Message response = MessageFactory.getInstance().createBlockMessage(newBlock);
        response.setId(message.getId());

        // SEND RESPONSE BACK WITH SAME ID
        if (USE_MONITOR) {
            this.setMonitorStatus("try GET_BLOCK " + Base58.encode(getBlockMessage.getSignature()));
        }

        boolean result = message.getSender().offerMessage(response);

        if (USE_MONITOR) {
            LOGGER.debug("block [" + newBlock.getHeight() + "] "
                    + (result? "sended" : "not sended") + " -> " + getBlockMessage.getSender());
            this.setMonitorStatus("offerMessage " + (result?" OK" : " bad"));
        }

        return 3 + newBlock.getTransactionCount();
    }

    public void run() {

        runned = true;
        //Message message;
        int counter = 0;

        this.initMonitor();

        while (runned) {

            this.setMonitorPoint();

            try {
                counter += processMessage(blockingQueue.take());
            } catch (OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAndExit(276);
                return;
            } catch (IllegalMonitorStateException e) {
                break;
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            // FREEZE sometimes
            if (counter > TX_COUNTER_WAIT) {
                counter = 0;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    break;
                }
            }

        }

        LOGGER.info("Block Request halted");
    }

    public void halt() {
        this.runned = false;
    }

}
