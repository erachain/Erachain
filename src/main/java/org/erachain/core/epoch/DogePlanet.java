package org.erachain.core.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;

public class DogePlanet extends SmartContract {

    static private final PublicKeyAccount MAKER = new PublicKeyAccount("1");
    private int count;
    private long keyEnd;

    DogePlanet(int count) {
        super(DOGE_PLANET_1, MAKER);
        this.count = count;
    }

    DogePlanet(int count, long keyEnd) {
        super(DOGE_PLANET_1, MAKER);
        this.count = count;
        this.keyEnd = keyEnd;
    }

    @Override
    public Object[][] getItemsKeys() {
        Object[][] itemKeys = new Object[count][];

        int i = 0;
        do {
            itemKeys[i] = new Object[]{ItemCls.ASSET_TYPE, keyEnd - i};
        } while (++i < count);

        return itemKeys;

    }

    @Override
    public int length(int forDeal) {
        if (forDeal == Transaction.FOR_DB_RECORD)
            return 16;

        return 4;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] data = Ints.toByteArray(id);

        if (forDeal == Transaction.FOR_DB_RECORD) {
            data = Bytes.concat(data, Ints.toByteArray(count));
            return Bytes.concat(data, Longs.toByteArray(keyEnd));
        }

        return data;

    }

    static DogePlanet Parse(byte[] data, int pos, int forDeal) {

        byte[] countBuffer = new byte[4];
        System.arraycopy(data, pos, countBuffer, 0, 4);

        if (forDeal == Transaction.FOR_DB_RECORD) {
            // возьмем в базе готовый ключ актива
            byte[] keyBuffer = new byte[8];
            System.arraycopy(data, pos, keyBuffer, 0, 8);
            return new DogePlanet(Ints.fromByteArray(countBuffer), Longs.fromByteArray(keyBuffer));
        }

        return new DogePlanet(Ints.fromByteArray(countBuffer));
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

        AssetUnique planet;
        int i = count;
        do {
            planet = new AssetUnique(null, maker, "new planet", null, null,
                    null, AssetCls.AS_NON_FUNGIBLE);
            planet.setReference(transaction.getSignature(), transaction.getDBRef());

            //INSERT INTO DATABASE
            keyEnd = dcSet.getItemAssetMap().incrementPut(planet);

        } while (--i > 0);


        return false;
    }


    @Override
    public boolean orphan(DCSet dcSet, Transaction transaction) {

        RSend txSend = (RSend) transaction;
        int i = 0;
        do {
            //DELETE FROM DATABASE
            dcSet.getItemAssetMap().decrementDelete(keyEnd - i);
        } while (++i < count);


        return false;
    }

}
