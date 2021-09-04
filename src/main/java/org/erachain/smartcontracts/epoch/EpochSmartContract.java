package org.erachain.smartcontracts.epoch;

import org.erachain.core.account.PublicKeyAccount;
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


}
