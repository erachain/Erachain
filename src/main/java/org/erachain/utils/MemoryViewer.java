package org.erachain.utils;

import org.erachain.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryViewer extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    Controller controller;

    public MemoryViewer(Controller controller) {

        this.controller = controller;
        setName("Memory manager");

    }

    public void run() {

        int i = 0;
        while (i++ < 100) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                // need for EXIT
                return;
            }
            if (Controller.getInstance().isOnStopping())
                return;
        }
        if (Controller.getInstance().isOnStopping())
            return;

        // if memory !Ok
        while (true) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
            if (Controller.getInstance().isOnStopping())
                return;

            //threads
            if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
                // System.out.println("########################### Free Memory:"
                // + Runtime.getRuntime().freeMemory());
                if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL) {
                    System.gc();

                    if (controller.isAllThreadsGood()) {
                        continue;
                    }

                    if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL >> 1)
                        Controller.getInstance().stopAll(1000);
                }
            }

        }
    }

}
