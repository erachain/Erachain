package org.erachain.core.wallet;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.block.Block;
import org.erachain.datachain.DCSet;
import org.erachain.utils.MonitoredThread;
import org.erachain.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class WalletUpdater extends MonitoredThread {

    private final static boolean USE_MONITOR = true;
    private final static boolean logPings = true;
    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletUpdater.class.getSimpleName());

    private static final int QUEUE_LENGTH = 1024 + (256 >> (Controller.HARD_WORK >> 1));
    BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(QUEUE_LENGTH);

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

    public void offerMessage(Object object) {
        blockingQueue.offer(object);
    }

    public void processMessage(Object object) {

        if (object == null)
            return;

        if (object instanceof Pair) {
            Pair<Boolean, Block> pair = (Pair<Boolean, Block>) object;

            if (pair.getA()) {
                if (wallet.checkNeedSyncWallet(pair.getB().getSignature())) {
                    wallet.orphanBlock(dcSet, pair.getB());
                } else {
                    wallet.synchronizeBody(false);
                }
            } else {
                if (wallet.checkNeedSyncWallet(pair.getB().getReference())) {
                    wallet.processBlock(dcSet, pair.getB());
                } else {
                    wallet.synchronizeBody(false);
                }
            }
        } else if (object instanceof Boolean) {
            // synchronize
            boolean reset = (Boolean) object;

            if (!reset && Controller.getInstance().isProcessingWalletSynchronize()
                    || Controller.getInstance().isOnStopping()
                    || Controller.getInstance().noDataWallet || Controller.getInstance().noUseWallet) {
                return;
            }

            // here ICREATOR
            Controller.getInstance().setProcessingWalletSynchronize(true);
            Controller.getInstance().setNeedSyncWallet(false);

            Controller.getInstance().walletSyncStatusUpdate(-1);

            LOGGER.info(" >>>>>>>>>>>>>>> *** Synchronizing wallet..." + (reset ? " RESET" : ""));

            DCSet dcSet = DCSet.getInstance();

            ///////////////////////////////////// IS CHAIN VALID
            if (Wallet.CHECK_CHAIN_BROKENS_ON_SYNC_WALLET) {
                LOGGER.info("TEST CHAIN .... ");
                for (int i = 1; i <= dcSet.getBlockMap().size(); i++) {
                    Block block = dcSet.getBlockMap().getAndProcess(i);
                    if (block.getHeight() != i) {
                        Long error = null;
                        ++error;
                    }
                    if (block.blockHead.heightBlock != i) {
                        Long error = null;
                        ++error;
                    }
                    Block.BlockHead head = dcSet.getBlocksHeadsMap().get(i);
                    if (head.heightBlock != i) {
                        Long error = null;
                        ++error;
                    }
                    if (i > 1) {
                        byte[] reference = block.getReference();
                        Block parent = dcSet.getBlockSignsMap().getBlock(reference);
                        if (parent == null) {
                            Long error = null;
                            ++error;
                        }
                        if (parent.getHeight() != i - 1) {
                            Long error = null;
                            ++error;
                        }
                        parent = dcSet.getBlockMap().getAndProcess(i - 1);
                        if (!Arrays.equals(parent.getSignature(), reference)) {
                            Long error = null;
                            ++error;
                        }
                    }
                    byte[] signature = block.getSignature();
                    int signHeight = dcSet.getBlockSignsMap().get(signature);
                    if (signHeight != i) {
                        Long error = null;
                        ++error;
                    }
                }
            }

            Block block;

            if (reset) {

                // полная пересборка кошелька

                // break current synchronization if exists
                wallet.synchronizeBodyUsed.set(false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                wallet.synchronizeBody(reset);
                return;

            } else {

                byte[] lastSignature = wallet.database.getLastBlockSignature();
                if (lastSignature == null) {
                    offerMessage(true);
                    return;
                }

                block = dcSet.getBlockSignsMap().getBlock(lastSignature);
                if (block == null) {
                    offerMessage(true);
                    return;
                }
            }

            // break current synchronization if exists
            wallet.synchronizeBodyUsed.set(false);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            // запустим догоняние
            wallet.synchronizeBody(false);
        }
    }

    public void run() {

        runned = true;
        //Message message;
        while (runned) {
            try {
                processMessage(blockingQueue.poll(1L, TimeUnit.SECONDS));
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
