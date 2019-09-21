package org.erachain.dbs.rocksDB.indexes.indexByteables;

import org.erachain.core.transaction.TransactionAmount;
import org.erachain.dbs.rocksDB.indexes.IndexByteable;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.erachain.dbs.rocksDB.RockSets.ROCK_BIG_DECIMAL_LEN;

/**
 * Это преобразователь для вторичного индекса включает сортировочное преобразование,
 * так чтобы в сортировке учавствовал байтовый код правильно
 */

public class IndexByteableBigDecimal implements IndexByteable<BigDecimal, Long> {

    /**
     * result - то что прилетело из уровня создания вторичного ключа - в simpleIndexDB.getBiFunction().apply(key, value);
     * смотри org.erachain.dbs.rocksDB.integration.DBRocksDBTable#put(java.lang.Object, java.lang.Object)
     * key - первичный ключ
     */
    @Override
    public byte[] toBytes(BigDecimal value, Long key) {

        int sign = value.signum();
        // берем абсолютное значение
        BigDecimal shiftForSortBG = value.abs().setScale(TransactionAmount.maxSCALE);
        BigInteger shiftForSortBI = shiftForSortBG.unscaledValue();
        byte[] shiftForSortOrig = shiftForSortBI.toByteArray();

        assert (ROCK_BIG_DECIMAL_LEN > shiftForSortOrig.length);

        byte[] shiftForSortBuff = new byte[ROCK_BIG_DECIMAL_LEN];
        System.arraycopy(shiftForSortOrig, 0, shiftForSortBuff,
                ROCK_BIG_DECIMAL_LEN - shiftForSortOrig.length, shiftForSortOrig.length);

        if (sign >= 0) {
            // учтем знак числа
            // сковертируем
            int shiftSign = 0;
            for (int i = ROCK_BIG_DECIMAL_LEN - 1; i >= 0; i--) {
                int temp = 128 + Byte.toUnsignedInt(shiftForSortBuff[i]) + shiftSign;
                shiftForSortBuff[i] = (byte)(temp);

                // учтем перенос на следующий байт
                if (temp > 255) {
                    shiftSign = 1;
                } else {
                    shiftSign = 0;
                }
            }
        } else {
            // учтем знак числа
            // сковертируем
            int shiftSign = 0;
            for (int i = ROCK_BIG_DECIMAL_LEN - 1; i >= 0; i--) {
                int temp = 128 - Byte.toUnsignedInt(shiftForSortBuff[i]) - shiftSign;
                shiftForSortBuff[i] = (byte)(temp);

                // учтем перенос на следующий байт
                if (temp < 0 ) {
                    shiftSign = 1;
                } else {
                    shiftSign = 0;
                }
            }
        }

        return shiftForSortBuff;
    }
}
