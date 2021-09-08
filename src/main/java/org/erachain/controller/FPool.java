package org.erachain.controller;

import org.erachain.core.BlockChain;
import org.erachain.database.DPSet;
import org.erachain.database.FPoolMap;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;
import org.erachain.utils.MonitoredThread;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;

public class FPool extends MonitoredThread {

    Controller controller;
    BlockChain blockChain;
    DCSet dcSet;
    DPSet dpSet;
    BigDecimal tax = new BigDecimal("5");

    private boolean runned;

    private static final Logger LOGGER = LoggerFactory.getLogger(FPool.class.getSimpleName());

    FPool(Controller controller, BlockChain blockChain, DCSet dcSet) {
        this.controller = controller;
        this.blockChain = blockChain;
        this.dcSet = dcSet;

        this.setName("Forging Pool[" + this.getId() + "]");

        try {
            this.dpSet = DPSet.reCreateDB();
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            try {
                this.dpSet.close();
            } catch (Exception e2) {
            }

            File dir = new File(Settings.getInstance().getFPoolDir());
            // delete dir
            if (dir.exists()) {
                try {
                    Files.walkFileTree(dir.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
                } catch (IOException e3) {
                }
            }

            this.dpSet = DPSet.reCreateDB();
        }

        this.start();

    }

    public void run() {

        runned = true;

        FPoolMap map = dpSet.getFPoolMap();

        while (runned) {

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                //ERROR SLEEPING
                return;
            }
        }

        LOGGER.info("Forging Pool halted");

    }

    public void halt() {
        this.runned = false;
        interrupt();
    }

}