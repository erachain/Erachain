package org.erachain.utils;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.transaction.TransactionAmount;

import java.math.BigDecimal;

public class BigDecimalUtil {

    static public void toBytes8(byte[] data, int pos, BigDecimal amount) {

        // CALCULATE ACCURACY of AMMOUNT
        int different_scale = amount.scale() - BlockChain.AMOUNT_DEDAULT_SCALE;
        BigDecimal amountBase;
        if (different_scale != 0) {
            // RESCALE AMOUNT
            amountBase = amount.scaleByPowerOfTen(different_scale);
            if (different_scale < 0)
                different_scale += TransactionAmount.SCALE_MASK + 1;

            // WRITE ACCURACY of AMMOUNT
            data[pos] = (byte) (data[pos] | different_scale);
        } else {
            amountBase = amount;
        }

        // WRITE AMOUNT
        byte[] amountBytes = Longs.toByteArray(amountBase.unscaledValue().longValue());
        //amountBytes = Bytes.ensureCapacity(amountBytes, AMOUNT_LENGTH, 0);
        System.arraycopy(amountBytes, 0, data, 1, amountBytes.length);
    }

}
