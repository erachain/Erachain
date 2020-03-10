package org.erachain.core.wallet;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.datachain.DCSet;
import org.erachain.utils.MonitoredThread;
import org.erachain.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WalletUpdater extends MonitoredThread {

    private final static boolean USE_MONITOR = true;
    private final static boolean logPings = true;
    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletUpdater.class.getSimpleName());

    private static final int QUEUE_LENGTH = 1024 + (256 >> (Controller.HARD_WORK >> 1));
    BlockingQueue<Pair<Boolean, Block>> blockingQueue = new ArrayBlockingQueue<>(QUEUE_LENGTH);

    private Controller controller;
    private BlockChain blockChain;
    private DCSet dcSet;
    private Wallet wallet;

    public WalletUpdater(Controller controller, BlockChain blockChain, DCSet dcSet, Wallet wallet) {
        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;
        this.wallet = wallet;

        this.setName("WalletUpdater[" + this.getId() + "]");

        this.start();
    }

    /**
     * TODO сделать чтобы сюда приходило последовательное добавление блоков в кошелек или их синхронизация
     * @param pair
     */
    public boolean offerMessage(Pair<Boolean, Block> pair) {

        boolean result = blockingQueue.offer(pair);
        return result;
    }

    public void processMessage(Pair<Boolean, Block> pair) {

        if (pair == null)
            return;

        if (pair.getA()) {
            if (wallet.checkNeedSyncWallet(pair.getB().getSignature())) {
                wallet.orphanBlock(dcSet, pair.getB());
            }
        } else {
            if (wallet.checkNeedSyncWallet(pair.getB().getReference())) {
                wallet.processBlock(dcSet, pair.getB());
            }
        }
    }

    public void run() {

        runned = true;
        //Message message;
        while (runned) {
            try {
                processMessage(blockingQueue.take());
            } catch (OutOfMemoryError e) {
                LOGGER.error(e.getMessage(), e);
                Controller.getInstance().stopAll(686);
                return;
            } catch (IllegalMonitorStateException e) {
                break;
            } catch (InterruptedException e) {
                break;
            }

        }

        LOGGER.info("Wallet Updater halted");
    }

    public void halt() {
        this.runned = false;
    }

}
