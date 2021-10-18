package org.erachain.smartcontracts.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.smartcontracts.SmartContract;

public abstract class EpochSmartContract extends SmartContract {


    public EpochSmartContract(int id) {
        super(id, new PublicKeyAccount(Base58.encode(Longs.toByteArray(id))));
    }

    public EpochSmartContract(int id, PublicKeyAccount maker) {
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

    @Override
    public int length(int forDeal) {
        return 4;
    }

    @Override
    public byte[] toBytes(int forDeal) {
        return Ints.toByteArray(id);
    }


}
