package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.dapp.DAPP;

public abstract class EpochDAPP extends DAPP {

    public EpochDAPP(int id) {
        super(id);
    }

    public EpochDAPP(int id, PublicKeyAccount maker) {
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
