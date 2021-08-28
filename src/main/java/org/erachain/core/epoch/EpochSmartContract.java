package org.erachain.core.epoch;

import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;

public class EpochSmartContract {


    /**
     * Делает смотр-контракт протокольный (на эпоху).
     *
     * @param transaction
     * @return
     */
    static public SmartContract make(Transaction transaction) {

        if (transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend txSend = (RSend) transaction;
            if (txSend.getAbsKey() == 10234L
                    && txSend.balancePosition() == TransactionAmount.ACTION_SPEND
            ) {
                return new DogePlanet();
            }
        }

        return null;

    }

}
