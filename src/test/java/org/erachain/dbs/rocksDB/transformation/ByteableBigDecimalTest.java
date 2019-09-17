package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@Slf4j
public class ByteableBigDecimalTest {


    @Test
    public void receiveObjectFromBytes() {
    }

    @Test
    public void toBytesObject() {

        long totalMemory = Runtime.getRuntime().totalMemory();
        logger.info(" used MEMORY[kB]: " + totalMemory / 1000);
        ByteableBigDecimal serializer = new ByteableBigDecimal();

        long time = System.nanoTime();
        for (int i=0; i < 1000000L; i++) {
            byte[] bytes = serializer.toBytesObject(new BigDecimal("" + i + "." + (i >> 3)));
        }
        long timeDiff = System.nanoTime() - time;

        long totalMemoryDiff = Runtime.getRuntime().totalMemory() - totalMemory;
        logger.info(" used MEMORY[kB]: " + Runtime.getRuntime().totalMemory() / 1000);
        logger.info("Time[mcs]: " + timeDiff /1000 + " used MEMORY[kB]: " + totalMemoryDiff / 1000);
    }
}