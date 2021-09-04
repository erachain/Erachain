package org.erachain.smartcontracts.epoch;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.smartcontracts.SmartContract;

public abstract class EpochSmartContract extends SmartContract {


    EpochSmartContract(int id, PublicKeyAccount maker) {
        super(id, maker);
    }

    /**
     * Эпохальный смарт-контракт
     *
     * @return
     */
    @Override
    public boolean isEpoch() {
        return true;
    }

    /**
     * Делает смотр-контракт протокольный (на эпоху).
     *
     * @param transaction
     * @return
     */
    static public SmartContract make(Transaction transaction) {

        if (BlockChain.TEST_MODE && transaction.getType() == Transaction.SEND_ASSET_TRANSACTION) {
            RSend txSend = (RSend) transaction;
            if (txSend.balancePosition() == TransactionAmount.ACTION_SPEND
                    && txSend.hasAmount() && txSend.getAmount().signum() < 0
                // && txSend.getAbsKey() == 10234L
            ) {
                return new DogePlanet(Math.abs(transaction.getAmount().intValue()));
            }
        }

        return null;

    }

    /**
     * Делает смотр-контракт протокольный (на эпоху).
     *
     * @param transaction
     * @return
     */
    static public SmartContract make(Order order, Trade trade, Transaction transaction) {

        if (BlockChain.TEST_MODE  //&& transaction.getType() == Transaction.SEND_ASSET_TRANSACTION
        ) {
            return new DogePlanet(Math.abs(transaction.getAmount().intValue()));
        }

        return null;

    }

}
