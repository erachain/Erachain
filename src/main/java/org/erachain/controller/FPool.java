package org.erachain.controller;

import org.erachain.core.BlockChain;
import org.erachain.datachain.DCSet;
import org.erachain.utils.MonitoredThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class FPool extends MonitoredThread {

    Controller controller;
    BlockChain blockChain;
    DCSet dcSet;
    BigDecimal tax = new BigDecimal("5");

    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(FPool.class.getSimpleName());

    FPool(Controller controller, BlockChain blockChain, DCSet dcSet) {
        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;

        this.setName("Forging Pool[" + this.getId() + "]");

        this.start();

    }

    public void run() {

        long poinClear = 0;
        long poinCleared = 0;
        int clearedUTXs = 0;

        long minorClear = 0;

        runned = true;
        //Message message;
        while (runned) {

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                //ERROR SLEEPING
                return;
            }
        }

    }
}