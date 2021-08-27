package org.erachain.core.epoch;

import org.erachain.core.block.Block;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;

public class EpochSmartContract {

    static public String isValid(Transaction transaction) {

        if (DogePlanet.isSelected(transaction)) {
            return DogePlanet.isValid((RSend) transaction);
        }

        return null;
    }

    /**
     * @param dcSet
     * @param block
     * @param transaction
     * @return
     */
    static public boolean process(DCSet dcSet, Block block, Transaction transaction) {

        if (DogePlanet.isSelected(transaction)) {
            return DogePlanet.process(dcSet, block, (RSend) transaction);
        }

        return false;
    }

    static public boolean orphan(DCSet dcSet, Block block, Transaction transaction) {

        if (DogePlanet.isSelected(transaction)) {
            return DogePlanet.orphan(dcSet, block, (RSend) transaction);
        }

        return false;
    }
}
