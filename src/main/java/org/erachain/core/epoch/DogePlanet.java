package org.erachain.core.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;

public class DogePlanet extends SmartContract {

    static private final PublicKeyAccount MAKER = new PublicKeyAccount("1");
    private long key;

    DogePlanet() {
        super(DOGE_PLANET_1, MAKER);
    }

    DogePlanet(long key) {
        super(DOGE_PLANET_1, MAKER);
        this.key = key;
    }

    @Override
    public int length(int forDeal) {
        if (forDeal == Transaction.FOR_DB_RECORD)
            return 12;

        return 4;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        if (forDeal == Transaction.FOR_DB_RECORD) {
            return Bytes.concat(Ints.toByteArray(id), Longs.toByteArray(key));

        } else {
            return Ints.toByteArray(id);
        }

    }

    static DogePlanet Parse(byte[] data, int pos, int forDeal) {

        if (forDeal == Transaction.FOR_DB_RECORD) {
            // возьмем в базе готовый ключ актива
            byte[] keyBuffer = new byte[8];
            System.arraycopy(data, pos, keyBuffer, 0, 8);
            return new DogePlanet(Longs.fromByteArray(keyBuffer));
        }

        return new DogePlanet();
    }

    /**
     * Эпохальный смарт-контракт
     *
     * @return
     */
    public boolean isEpoch() {
        return true;
    }


    @Override
    public boolean process(DCSet dcSet, Block block, Transaction transaction) {

        AssetUnique planet = new AssetUnique(null, maker, "new planet", null, null,
                null, AssetCls.AS_NON_FUNGIBLE);
        planet.setReference(transaction.getSignature(), transaction.getDBRef());

        //INSERT INTO DATABASE
        key = dcSet.getItemAssetMap().incrementPut(planet);

        return false;
    }


    @Override
    public boolean orphan(DCSet dcSet, Transaction transaction) {

        //DELETE FROM DATABASE
        dcSet.getItemAssetMap().decrementDelete(key);

        return false;
    }

}
