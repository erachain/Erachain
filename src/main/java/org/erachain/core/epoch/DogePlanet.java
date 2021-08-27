package org.erachain.core.epoch;

import org.erachain.core.block.Block;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.datachain.DCSet;

import java.math.BigDecimal;

public class DogePlanet {


    static public boolean isSelected(Transaction transaction) {

        if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend txSend = (RSend) transaction;
            if (txSend.getAbsKey() == 10234L
                    && txSend.balancePosition() == TransactionAmount.ACTION_SPEND
            ) {
                return true;
            }
        }

        return false;

    }


    static public String isValid(RSend transaction) {

        if (!transaction.getAmount().equals(BigDecimal.ONE))
            return "amount not 1";

        return null;

    }


    static public boolean process(DCSet dcSet, Block block, RSend transaction) {


        return false;
    }


    static public boolean orphan(DCSet dcSet, Block block, RSend transaction) {

        return false;
    }

}
