package org.erachain.core.epoch;

import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;

public class DogePlanet extends SmartContract {

    private long key;

    DogePlanet() {
        super(new PublicKeyAccount("1"));
    }

    DogePlanet(byte[] data, int pos) {

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
