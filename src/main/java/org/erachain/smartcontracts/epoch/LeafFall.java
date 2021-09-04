package org.erachain.smartcontracts.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.SmartContractValues;
import org.mapdb.Fun;

import java.math.BigDecimal;

/**
 * Ctrl+Shift-T (IntellijIDEA) - make test unit
 */
public class LeafFall extends EpochSmartContract {

    public static final int ID = 1;
    static public final PublicKeyAccount MAKER = new PublicKeyAccount(Base58.encode(Longs.toByteArray(ID)));

    private int count;
    private long keyInit;
    private int resultHash;

    /**
     * list of assets for this smart-contract
     */
    private static long[] leafs = new long[]{1048721L, 1048722L, 1048723L, 1048724L,
            1048724L, 1048724L, 1048724L, 1048724L};

    static final Fun.Tuple2 COUNT_KEY = new Fun.Tuple2(ID, "c");

    public LeafFall(int count) {
        super(ID, MAKER);
        this.count = count;
    }

    public LeafFall(int count, long keyInit, int resultHash) {
        super(ID, MAKER);
        this.count = count;
        this.keyInit = keyInit;
        this.resultHash = resultHash;
    }

    public int getCount() {
        return count;
    }

    public long getKeyInit() {
        return keyInit;
    }

    public int getResultHash() {
        return resultHash;
    }

    /**
     * Make random leaf key by block and tx signatures
     *
     * @param block
     * @param transaction
     * @return
     */
    private int makeResultHash(Block block, Transaction transaction) {
        if (block == null) {
            // not confirmed - not make any!
            return 250;
        }

        return Byte.toUnsignedInt((byte) (block.getSignature()[5] + transaction.getSignature()[5]));
    }

    /**
     * Make random leaf key by block and tx signatures
     *
     * @return
     */
    private long getLeafKey() {

        int level;
        if (resultHash < 2)
            level = 7;
        else if (resultHash < 4)
            level = 6;
        else if (resultHash < 8)
            level = 5;
        else if (resultHash < 16)
            level = 4;
        else if (resultHash < 32)
            level = 3;
        else if (resultHash < 64)
            level = 2;
        else if (resultHash < 128)
            level = 1;
        else
            level = 0;

        return leafs[level];
    }

    private void action(DCSet dcSet, Block block, Transaction transaction, boolean asOrphan) {

        if (resultHash == 0) {
            resultHash = makeResultHash(block, transaction);
        }

        long leafKey = getLeafKey();

        // TRANSFER LEAF from MAKER to RECIPIENT
        transaction.getCreator().changeBalance(dcSet, asOrphan, false, leafKey,
                BigDecimal.ONE, false, false, false);
        maker.changeBalance(dcSet, !asOrphan, false, leafKey,
                BigDecimal.ONE, false, false, false);

        BigDecimal resultBG = new BigDecimal(resultHash);
        // ACCAUNTING RARITY RESULT from MAKER to RECIPIENT
        transaction.getCreator().changeBalance(dcSet, asOrphan, false, keyInit,
                resultBG, false, false, false);
        maker.changeBalance(dcSet, !asOrphan, false, keyInit,
                resultBG, false, false, false);

    }

    @Override
    public Object[][] getItemsKeys() {
        if (keyInit == 0) {
            // not confirmed yet
            return null;
        }

        Object[][] itemKeys = new Object[1][];

        itemKeys[0] = new Object[]{ItemCls.ASSET_TYPE, getLeafKey()};

        return itemKeys;

    }

    @Override
    public int length(int forDeal) {
        if (forDeal == Transaction.FOR_DB_RECORD)
            return 20;

        return 8;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] data = Ints.toByteArray(id);
        data = Bytes.concat(data, Ints.toByteArray(count));

        if (forDeal == Transaction.FOR_DB_RECORD) {
            return Bytes.concat(Bytes.concat(data, Longs.toByteArray(keyInit)),
                    Ints.toByteArray(resultHash));
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
            // GET INITIAL KEY
            byte[] keyBuffer = new byte[8];
            System.arraycopy(data, pos, keyBuffer, 0, 8);
            pos += 8;

            // GET RESULT HASH
            byte[] resultHashBuffer = new byte[4];
            System.arraycopy(data, pos, resultHashBuffer, 0, 4);

            return new LeafFall(Ints.fromByteArray(countBuffer), Longs.fromByteArray(keyBuffer),
                    Ints.fromByteArray(resultHashBuffer));
        }

        return new LeafFall(Ints.fromByteArray(countBuffer));
    }

    private void init(DCSet dcSet, Transaction transaction) {

        // обязательно так как может из Сборки Блока прити уже иниализированный
        count = 0;

        /**
         * for accounting total leaf for person
         */
        AssetVenture leafSum = new AssetVenture(null, maker, "LeafFall_sum", null, null,
                null, AssetCls.AS_SELF_MANAGED_ACCOUNTING, 0, 0);
        leafSum.setReference(transaction.getSignature(), transaction.getDBRef());

        //INSERT INTO DATABASE
        keyInit = dcSet.getItemAssetMap().incrementPut(leafSum);

        ItemAssetMap map = dcSet.getItemAssetMap();
        for (long leafKey : leafs) {
            AssetCls leafAsset = map.get(leafKey);
            leafAsset.setMaker(MAKER);
            // update MAKER
            map.put(leafKey, leafAsset);
        }

    }


    @Override
    public boolean process(DCSet dcSet, Block block, Transaction transaction) {

        /**
         * use this state storage if many variables used in smart-contract
         */
        //SmartContractState stateMap = dcSet.getSmartContractState();

        /**
         * Use this values storage if several variables used in smart-contract
         *  and orphans values not linked to previous state
         */
        SmartContractValues valuesMap = dcSet.getSmartContractValues();

        // CHECK if INITIALIZED
        if (valuesMap.contains(COUNT_KEY)) {
            count = (Integer) valuesMap.get(COUNT_KEY);
        } else {
            init(dcSet, transaction);
        }

        action(dcSet, block, transaction, false);

        valuesMap.put(COUNT_KEY, ++count);

        return false;
    }

    private void wipe(DCSet dcSet) {
        dcSet.getItemAssetMap().decrementDelete(keyInit);
        SmartContractValues valuesMap = dcSet.getSmartContractValues();
        valuesMap.delete(COUNT_KEY);
    }

    @Override
    public boolean orphan(DCSet dcSet, Transaction transaction) {

        SmartContractValues valuesMap = dcSet.getSmartContractValues();
        count = (Integer) valuesMap.get(COUNT_KEY);

        // leafKey already calculated OR get from DB
        action(dcSet, null, transaction, true);

        if (count == 1) {
            /**
             * remove all data from db
             */
            wipe(dcSet);
        } else {
            valuesMap.put(COUNT_KEY, --count);
        }

        return false;
    }


}
