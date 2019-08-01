package org.erachain.core;

import org.erachain.controller.Controller;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.network.Peer;
import org.erachain.network.message.BlockMessage;
import org.erachain.network.message.Message;
import org.erachain.network.message.MessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * класс для сохранения блоков при асинхронной скачки цепочки с другого пира
 */
public class BlockBuffer extends Thread {
    private static final int BUFFER_SIZE = 1 + 64 >> BlockChain.HARD_WORK;
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockBuffer.class);
    private List<byte[]> signatures;
    private Peer peer;
    private int counter;
    private boolean error;
    private Map<byte[], BlockingQueue<Block>> blocks;
    private boolean run = true;

    public BlockBuffer(List<byte[]> signatures, Peer peer) {
        this.signatures = signatures;
        this.peer = peer;
        this.counter = 0;
        this.error = false;
        this.setName("Thread BlockBuffer - " + this.getId() + " for " + peer);
        this.blocks = new HashMap<byte[], BlockingQueue<Block>>(BUFFER_SIZE << 1, 1);
        this.start();
    }

    public void run() {
        while (this.run) {
            for (int i = 0; i < this.signatures.size() && i < this.counter + BUFFER_SIZE; i++) {

                if (Controller.getInstance().isOnStopping()) {
                    stopThread();
                    break;
                }

                byte[] signature = this.signatures.get(i);

                //CHECK IF WE HAVE ALREADY LOADED THIS BLOCK
                if (!this.blocks.containsKey(signature)) {
                    //LOAD BLOCK
                    // время ожидания увеличиваем по мере номера блока - он ведь на той тсроне синхронно нам будет посылаться
                    this.loadBlock(signature, Synchronizer.GET_BLOCK_TIMEOUT + i * (Synchronizer.GET_BLOCK_TIMEOUT >> 1));

                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ERROR SLEEPING
                break;
            }
        }
    }

    private void loadBlock(final byte[] signature, long timeSOT) {
        //CREATE QUEUE
        final BlockingQueue<Block> blockingQueue = new ArrayBlockingQueue<Block>(1);
        this.blocks.put(signature, blockingQueue);

        //LOAD BLOCK IN THREAD
        new Thread("loadBlock " + Base58.encode(signature)) {
            public void run() {
                //CREATE MESSAGE
                Message message = MessageFactory.getInstance().createGetBlockMessage(signature);

                long timePoint = System.currentTimeMillis();

                //SEND MESSAGE TO PEER
                BlockMessage response = (BlockMessage) peer.getResponse(message, timeSOT);

                //CHECK IF WE GOT RESPONSE
                if (response == null) {
                    //ERROR
                    LOGGER.debug("ERROR block BUFFER response == null");
                    error = true;
                    return;
                }

                Block block = response.getBlock();
                //CHECK BLOCK SIGNATURE
                if (block == null) {
                    LOGGER.debug("ERROR block BUFFER block");
                    error = true;
                    return;
                }

                //ADD TO LIST
                blockingQueue.add(block);

                LOGGER.debug("block BUFFER added: "
                        + block.getTransactionCount() + "tx, "
                        + response.getLength() / 1000 + "kB, "
                        + (System.currentTimeMillis() - timePoint) + "ms"
                );

            }
        }.start();
    }

    public Block getBlock(byte[] signature) throws Exception {

        Block block;
        if (this.blocks.containsKey(signature)) {
            if (this.error) {
                throw new Exception("Block buffer error 0");
            }

        } else {

            //CHECK ERROR
            if (this.error) {
                throw new Exception("Block buffer error 1");
            }

            //CHECK IF ALREADY LOADED BLOCK
            //LOAD BLOCK
            this.loadBlock(signature, Synchronizer.GET_BLOCK_TIMEOUT >> 1);

            //GET BLOCK
            if (this.error) {
                throw new Exception("Block buffer error 2");
            }

        }

        //UPDATE COUNTER
        this.counter = this.signatures.indexOf(signature);

        //
        block = this.blocks.get(signature).poll(Synchronizer.GET_BLOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        if (block == null) {
            throw new Exception("Block buffer error 3 =null");
        }

        return block;
    }

    public void clearBlock(byte[] signature) {

        if (this.blocks.containsKey(signature)) {
            this.blocks.remove(signature);
        }

    }

    public void stopThread() {
        try {
            this.run = false;

            this.join();

        } catch (InterruptedException e) {
            //INTERRUPTED
        }
    }

}
