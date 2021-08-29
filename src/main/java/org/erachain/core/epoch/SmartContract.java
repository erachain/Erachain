package org.erachain.core.epoch;

import com.google.common.primitives.Ints;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;

public abstract class SmartContract {

    static final int DOGE_PLANET_1 = 1;


    protected final int id;
    protected final PublicKeyAccount maker;

    SmartContract(int id, PublicKeyAccount maker) {
        this.id = id;
        this.maker = maker;
    }

    public int getID() {
        return this.id;
    }

    public PublicKeyAccount getMaker() {
        return this.maker;
    }

    public Object[][] getItemsKeys() {
        return null;
    }

    /**
     * Эпохальный, запускается самим протоколом. Поэтому он не передается в сеть
     * Но для базы данных генерит данные, которые нужно читать и писать
     *
     * @return
     */
    public boolean isEpoch() {
        return false;
    }

    public int length(int forDeal) {
        return 4 + 32;
    }

    public byte[] toBytes(int forDeal) {
        byte[] data = new byte[4 + 32];
        System.arraycopy(Ints.toByteArray(id), 0, data, 0, 4);
        System.arraycopy(maker.getPublicKey(), 0, data, 4, 36);

        return new byte[8];
    }

    public static SmartContract Parses(byte[] data, int position, int forDeal) throws Exception {

        byte[] idBuffer = new byte[4];
        System.arraycopy(data, position, idBuffer, 0, 4);
        int id = Ints.fromByteArray(idBuffer);
        switch (id) {
            case DOGE_PLANET_1:
                return DogePlanet.Parse(data, position + 4, forDeal);
        }

        throw new Exception("wrong smart-contract id:" + id);
    }

    public String isValid(Transaction transaction) {
        return null;
    }

    abstract public boolean process(DCSet dcSet, Block block, Transaction transaction);

    abstract public boolean orphan(DCSet dcSet, Transaction transaction);

}
