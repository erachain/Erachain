package org.erachain.utils;

import org.erachain.controller.Controller;
import org.apache.log4j.Logger;

public class MemoryViewer extends Thread {
    private static final Logger LOGGER = Logger.getLogger(Controller.class);

    Controller controller;

    public MemoryViewer(Controller controller) {

        this.controller = controller;
        setName("Memory manager");

    }

    public void run() {

        try {
            sleep(100000);
        } catch (Exception e) {
        }

        // if memory !Ok
        while (true) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
            }

            //threads
            if (Runtime.getRuntime().maxMemory() == Runtime.getRuntime().totalMemory()) {
                // System.out.println("########################### Free Memory:"
                // + Runtime.getRuntime().freeMemory());
                if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL) {
                    System.gc();

                    if (controller.isAllThreadsGood()) {
                        continue;
                    }

                    if (Runtime.getRuntime().freeMemory() < Controller.MIN_MEMORY_TAIL >> 2)
                        Controller.getInstance().stopAll(99);
                }
            }

        }
    }

}
