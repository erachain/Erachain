package org.erachain.utils;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.transaction.TransactionAmount;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class BigDecimalUtil {

    static public void toBytes9(byte[] data, int pos, BigDecimal amount) {

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

    static public BigDecimal fromBytes9(byte[] data, int position) {

        //READ SCALE
        byte scale = data[position++];

        // READ AMOUNT
        byte[] amountBytes = Arrays.copyOfRange(data, position, position + 8);
        BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), BlockChain.AMOUNT_DEDAULT_SCALE);

        // CHECK ACCURACY of AMOUNT
        if (scale != -1) {
            // not use old FLAG from vers 2
            int accuracy = scale & TransactionAmount.SCALE_MASK;
            if (accuracy > 0) {
                if (accuracy >= TransactionAmount.SCALE_MASK_HALF) {
                    accuracy -= TransactionAmount.SCALE_MASK + 1;
                }

                // RESCALE AMOUNT
                amount = amount.scaleByPowerOfTen(-accuracy);
            }
        }

        return amount;
    }

}
