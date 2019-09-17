package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
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
        long time = System.nanoTime();
        //Random rand = new Random();
        ByteableBigDecimal serializer = new ByteableBigDecimal();
        for (int i=0; i < 1000000L; i++) {
            byte[] bytes = serializer.toBytesObject(new BigDecimal("" + i + "." + (i >> 3)));
        }
        long timeEnd = System.nanoTime() - time;
        logger.info("microSec Time: " + timeEnd /1000);
    }
}