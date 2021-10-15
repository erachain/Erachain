package org.erachain.dbs.rocksDB.transformation;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;

@Slf4j
public class ByteableBigDecimalTest {


    @Test
    public void receiveObjectFromBytes() {
    }

    /**
     * это неэффективный пример создания ключей
     */
    @Test
    public void toBytesObject() {

        /**
         * А вот пример как у Глеба сериалзитор был сделан - не эффективно:
         *     public BigDecimal receiveObjectFromBytes(byte[] bytes) {
         *         byte[] sizeArray = Arrays.copyOfRange(bytes, 0, Integer.BYTES);
         *         Integer size = byteableInteger.receiveObjectFromBytes(sizeArray);
         *         byte[] scaleArray = Arrays.copyOfRange(bytes, Integer.BYTES, 2 * Integer.BYTES);
         *         Integer scale = byteableInteger.receiveObjectFromBytes(scaleArray);
         *         byte[] number = Arrays.copyOfRange(bytes, 2 * Integer.BYTES, 2 * Integer.BYTES + size);
         *         BigDecimal bigDecimal = new BigDecimal(new BigInteger(number));
         *         sizeReaded = 2 * Integer.BYTES + size;
         *         return bigDecimal.setScale(scale);
         *     }
         *
         *     @Override
         *     public byte[] toBytesObject(BigDecimal value) {
         *         byte[] bytes = value.toBigInteger().toByteArray();
         *         return org.bouncycastle.util.Arrays.concatenate(
         *                 byteableInteger.toBytesObject(bytes.length),
         *                 byteableInteger.toBytesObject(value.scale()),
         *                 bytes);
         *     }
         *
         *  - лишние объекты, пустые байты...
         *
         *  и вот результат п тестам в 3-4 раза скорость ипамять хуже:
         *
         */
        long totalMemory = Runtime.getRuntime().totalMemory();
        logger.info(" used MEMORY[kB]: " + totalMemory / 1000);
        ByteableBigDecimal serializer = new ByteableBigDecimal();

        long time = System.nanoTime();
        for (int i = 0; i < 1000000L; i++) {
            BigDecimal value = new BigDecimal("" + i + "." + (i >> 3));
            byte[] bytes = serializer.toBytesObject(value);
        }
        long timeDiff = System.nanoTime() - time;

        long totalMemoryDiff = Runtime.getRuntime().totalMemory() - totalMemory;
        logger.info(" used MEMORY[kB]: " + Runtime.getRuntime().totalMemory() / 1000);
        logger.info("Time[mcs]: " + timeDiff / 1000 + " used MEMORY[kB]: " + totalMemoryDiff / 1000);

    }

    /**
     * Это правильный пример создания ключей
     */
    @Test
    public void toBytesObjectMapDB() {
        // То же самое но как работает Мар DB

        long totalMemory = Runtime.getRuntime().totalMemory();
        logger.info(" used MEMORY[kB]: " + totalMemory / 1000);

        long time = System.nanoTime();
        for (int i = 0; i < 1000000L; i++) {
            ///byte[] bytes = serializer.toBytesObject(new BigDecimal("" + i + "." + (i >> 3)));
            BigDecimal value = new BigDecimal("" + i + "." + (i >> 3));
            byte[] bytesA = value.unscaledValue().toByteArray();
            byte[] result = new byte[bytesA.length + 2];
            System.arraycopy(bytesA, 0, result, 2, bytesA.length);
            result[0] = (byte)bytesA.length;
            result[1] = (byte)value.scale();

        }
        long timeDiff = System.nanoTime() - time;

        long totalMemoryDiff = Runtime.getRuntime().totalMemory() - totalMemory;
        logger.info(" used MEMORY[kB]: " + Runtime.getRuntime().totalMemory() / 1000);
        logger.info("Time[mcs]: " + timeDiff /1000 + " used MEMORY[kB]: " + totalMemoryDiff / 1000);


    }
}