package org.erachain.core.epoch;

import com.google.common.primitives.Ints;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;

public abstract class SmartContract {

    static final int DOGE_PLANET_SC = 1;
    protected PublicKeyAccount maker;

    SmartContract() {
    }

    SmartContract(PublicKeyAccount maker) {
        this.maker = maker;
    }

    public PublicKeyAccount getMaker() {
        return this.maker;
    }

    public int length(int forDeal) {
        return 0;
    }

    public byte[] toBytes(int forDeal) {
        return new byte[8];
    }

    //abstract SmartContract Parse(byte[] data, int forDeal) throws Exception;

    public static SmartContract parse(byte[] data, int position) throws Exception {

        byte[] idBuffer = new byte[4];
        System.arraycopy(data, position, idBuffer, 0, 4);
        int id = Ints.fromByteArray(idBuffer);
        switch (id) {
            case DOGE_PLANET_SC:
                return new DogePlanet(data, position);
        }

        throw new Exception("wrong smart-contract id:" + id);
    }

    public String isValid(Transaction transaction) {
        return null;
    }

    abstract public boolean process(DCSet dcSet, Block block, Transaction transaction);

    abstract public boolean orphan(DCSet dcSet, Transaction transaction);

}
