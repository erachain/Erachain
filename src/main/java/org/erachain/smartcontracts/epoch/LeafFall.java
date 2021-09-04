package org.erachain.smartcontracts.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.SmartContractValues;
import org.mapdb.Fun;

import java.math.BigDecimal;

public class LeafFall extends EpochSmartContract {

    public static final int ID = 1;
    static public final PublicKeyAccount MAKER = new PublicKeyAccount("" + ID);

    private int count;
    private long keyEnd;

    static final Fun.Tuple2 COUNT_KEY = new Fun.Tuple2(ID, "c");

    public LeafFall(int count) {
        super(ID, MAKER);
        this.count = count;
    }

    public LeafFall(int count, long keyEnd) {
        super(ID, MAKER);
        this.count = count;
        this.keyEnd = keyEnd;
    }

    public int getCount() {
        return count;
    }

    public long getKeyEnd() {
        return keyEnd;
    }

    @Override
    public Object[][] getItemsKeys() {
        if (keyEnd == 0) {
            // not confirmed yet
            return null;
        }

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

        return 8;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] data = Ints.toByteArray(id);
        data = Bytes.concat(data, Ints.toByteArray(count));

        if (forDeal == Transaction.FOR_DB_RECORD) {
            return Bytes.concat(data, Longs.toByteArray(keyEnd));
        }

        return data;

    }

    public static LeafFall Parse(byte[] data, int pos, int forDeal) {

        // skip ID
        pos += 4;

        byte[] countBuffer = new byte[4];
        System.arraycopy(data, pos, countBuffer, 0, 4);
        pos += 4;

        if (forDeal == Transaction.FOR_DB_RECORD) {
            // возьмем в базе готовый ключ актива
            byte[] keyBuffer = new byte[8];
            System.arraycopy(data, pos, keyBuffer, 0, 8);
            return new LeafFall(Ints.fromByteArray(countBuffer), Longs.fromByteArray(keyBuffer));
        }

        return new LeafFall(Ints.fromByteArray(countBuffer));
    }

    @Override
    public boolean process(DCSet dcSet, Block block, Transaction transaction) {

        AssetUnique planet;
        int i = count;

        /**
         * use this state storage if many variables used in smart-contract
         */
        //SmartContractState stateMap = dcSet.getSmartContractState();

        /**
         * Use this values storage if several variables used in smart-contract
         *  and orphans values not linked to previous state
         */
        SmartContractValues valuesMap = dcSet.getSmartContractValues();
        Integer totalIssuedObj = (Integer) valuesMap.get(COUNT_KEY);
        int totalIssued;
        if (totalIssuedObj == null)
            totalIssued = 0;
        else
            totalIssued = totalIssuedObj;

        do {

            totalIssued++;

            planet = new AssetUnique(null, maker, "Doge Planet #" + totalIssued, null, null,
                    null, AssetCls.AS_NON_FUNGIBLE);
            planet.setReference(transaction.getSignature(), transaction.getDBRef());

            //INSERT INTO DATABASE
            keyEnd = dcSet.getItemAssetMap().incrementPut(planet);
            transaction.getCreator().changeBalance(dcSet, false, false, keyEnd,
                    BigDecimal.ONE, false, false, false);

        } while (--i > 0);

        valuesMap.put(COUNT_KEY, totalIssued);


        return false;
    }


    @Override
    public boolean orphan(DCSet dcSet, Transaction transaction) {

        SmartContractValues valuesMap = dcSet.getSmartContractValues();
        Integer totalIssued = (Integer) valuesMap.get(COUNT_KEY);

        int i = 0;
        do {

            transaction.getCreator().changeBalance(dcSet, true, false, keyEnd,
                    BigDecimal.ONE, false, false, false);

            //DELETE FROM DATABASE
            dcSet.getItemAssetMap().decrementDelete(keyEnd - i);
        } while (++i < count);

        valuesMap.put(COUNT_KEY, totalIssued - count);

        return false;
    }

}
