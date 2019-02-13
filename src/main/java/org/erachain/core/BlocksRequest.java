package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.datachain.DCSet;
import org.erachain.network.Peer;
import org.erachain.network.message.BlockWinMessage;
import org.erachain.network.message.GetBlockMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlocksRequest extends MonitoredThread {

    private final static boolean USE_MONITOR = true;
    private final static boolean logPings = true;
    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(BlocksRequest.class);

    private static final int QUEUE_LENGTH = BlockChain.DEVELOP_USE? 50 : 50;
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
        blockingQueue.offer(message);
    }

    public int processMessage(Message message) {

        if (message == null)
            return 0;

        GetBlockMessage getBlockMessage = (GetBlockMessage) message;

        /*
         * LOGGER.
         * error("controller.Controller.onMessage(Message).GET_BLOCK_TYPE ->.getSignature()"
         * + " form PEER: " + getBlockMessage.getSender().toString()
         * + " sign: " +
         * Base58.encode(getBlockMessage.getSignature()));
         */

        // ASK BLOCK FROM BLOCKCHAIN
        Block newBlock = this.blockChain.getBlock(dcSet, getBlockMessage.getSignature());

        // CREATE RESPONSE WITH SAME ID
        Message response = MessageFactory.getInstance().createBlockMessage(newBlock);
        response.setId(message.getId());

        // SEND RESPONSE BACK WITH SAME ID
        message.getSender().offerMessage(response);

        if (false && newBlock == null) {
            String mess = "Block NOT FOUND for sign:" + getBlockMessage.getSignature();
            //banPeerOnError(message.getSender(), mess);
        }

        return 3 + newBlock.getTransactionCount();
    }

    public void run() {

        try {
            runned = true;
            Message message;
            int counter = 0;
            while (runned) {
                try {
                    counter += processMessage(blockingQueue.take());
                } catch (OutOfMemoryError e) {
                    Controller.getInstance().stopAll(86);
                    break;
                } catch (IllegalMonitorStateException e) {
                    break;
                } catch (InterruptedException e) {
                    break;
                }

                // FREEZE sometimes
                if (counter > 1000) {
                    counter = 0;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            controller.stopAll(44);
            return;
        }

        LOGGER.info("Block Request halted");
    }

    public void halt() {
        this.runned = false;
    }

}
