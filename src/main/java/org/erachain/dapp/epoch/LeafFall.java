package org.erachain.dapp.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
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
public class LeafFall extends EpochDAPP {

    public static final int ID = 1;
    static public final String NAME = "Magic Leaf";
    static public final boolean DISABLED = BlockChain.MAIN_MODE;
    static public final String SHORT = "Награды для тех кто торгует на бирже";
    static public final String DESC = "Награды для тех кто торгует на бирже";

    // global values - save in smart-contracts maps
    private int count;
    private long keyInit;

    // local values for each TX
    /**
     * = 0 - not calcutaed yet! 1...256
     */
    private int resultHash;

    /**
     * list of assets for this smart-contract
     */
    private static long[] leafs = new long[]{1048725L, 1048727L, 1048726L, 1048728L,
            1048729L, 1048730L, 1048731L, 1048732L};

    // use for check: need to initiate values?
    static final Fun.Tuple2 INIT_KEY = new Fun.Tuple2(ID, "i");
    static final Fun.Tuple2 COUNT_VAR = new Fun.Tuple2(ID, "c");

    public LeafFall() {
        super(ID);
    }

    public LeafFall(int resultHash) {
        super(ID);
        this.resultHash = resultHash;
    }

    public static LeafFall initInfo() {
        return new LeafFall();
    }

    public boolean isDisabled() {
        return DISABLED;
    }

    public String getName() {
        return NAME;
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
    private void makeResultHash(Block block, Transaction transaction) {
        if (block == null) {
            // not confirmed - not make any!
            return;
        }

        resultHash = 1 + Byte.toUnsignedInt((byte) (block.getSignature()[5] + transaction.getSignature()[5]));
    }

    /**
     * Make random leaf key by block and tx signatures
     *
     * @return
     */
    private int getLevelResult() {

        int reverseResultHash = 256 - this.resultHash;
        int level;
        if (reverseResultHash < 2)
            level = 7;
        else if (reverseResultHash < 4)
            level = 6;
        else if (reverseResultHash < 8)
            level = 5;
        else if (reverseResultHash < 16)
            level = 4;
        else if (reverseResultHash < 32)
            level = 3;
        else if (reverseResultHash < 64)
            level = 2;
        else if (reverseResultHash < 128)
            level = 1;
        else
            level = 0;

        return level;
    }

    private void action(DCSet dcSet, Block block, Transaction transaction, boolean asOrphan) {

        if (resultHash == 0) {
            makeResultHash(block, transaction);
        }

        int levelResult = getLevelResult();
        long leafKey = leafs[levelResult];

        PublicKeyAccount creator = transaction.getCreator();
        // TRANSFER LEAF from MAKER to RECIPIENT
        creator.changeBalance(dcSet, asOrphan, false, leafKey,
                BigDecimal.ONE, false, false, false);
        stock.changeBalance(dcSet, !asOrphan, false, leafKey,
                BigDecimal.ONE, false, false, false);

        //
        BigDecimal resultBG = new BigDecimal(resultHash);
        // ACCAUNTING RARITY RESULT from MAKER to RECIPIENT
        creator.changeBalance(dcSet, asOrphan, false, keyInit,
                resultBG, false, false, false);
        stock.changeBalance(dcSet, !asOrphan, false, keyInit,
                resultBG, false, false, false);

        if (block != null) {
            // add remark for action
            block.addCalculated(creator, keyInit, resultBG,
                    "Magic leaf fall win: %1!".replaceFirst("%1", "" + levelResult), transaction.getDBRef());
        }

    }

    @Override
    public Object[][] getItemsKeys() {

        Object[][] itemKeys = new Object[1][];
        itemKeys[0] = new Object[]{ItemCls.ASSET_TYPE, leafs[getLevelResult()]};

        return itemKeys;

    }

    @Override
    public int length(int forDeal) {
        int len = super.length(forDeal);
        if (forDeal == Transaction.FOR_DB_RECORD)
            return len + 4;

        return len;
    }

    @Override
    public byte[] toBytes(int forDeal) {

        byte[] data = super.toBytes(forDeal);

        if (forDeal == Transaction.FOR_DB_RECORD) {
            return Bytes.concat(data, Ints.toByteArray(resultHash));
        }

        return data;

    }

    public static LeafFall Parse(byte[] data, int pos, int forDeal) {

        // skip ID
        pos += 4;

        if (forDeal == Transaction.FOR_DB_RECORD) {
            // GET RESULT HASH
            byte[] resultHashBuffer = new byte[4];
            System.arraycopy(data, pos, resultHashBuffer, 0, 4);

            return new LeafFall(Ints.fromByteArray(resultHashBuffer));
        }

        return new LeafFall();
    }

    private void init(DCSet dcSet, Transaction transaction) {

        // обязательно так как может из Сборки Блока прити уже иниализированный
        count = 0;

        /**
         * for accounting total leaf for person
         */
        AssetVenture leafSum = new AssetVenture(null, stock, "LeafFall_sum", null, null,
                null, AssetCls.AS_SELF_MANAGED_ACCOUNTING, 0, 0);
        leafSum.setReference(transaction.getSignature(), transaction.getDBRef());

        //INSERT INTO DATABASE
        keyInit = dcSet.getItemAssetMap().incrementPut(leafSum);
        dcSet.getSmartContractValues().put(INIT_KEY, keyInit);

        ItemAssetMap map = dcSet.getItemAssetMap();
        for (long leafKey : leafs) {
            AssetCls leafAsset = map.get(leafKey);
            leafAsset.setMaker(stock);
            // update MAKER
            map.put(leafKey, leafAsset);
        }

    }

    private void loadValues(SmartContractValues valuesMap) {
        keyInit = (Long) valuesMap.get(INIT_KEY);
        count = (Integer) valuesMap.get(COUNT_VAR);
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
        if (valuesMap.contains(INIT_KEY)) {
            loadValues(valuesMap);
        } else {
            init(dcSet, transaction);
        }

        action(dcSet, block, transaction, false);

        valuesMap.put(COUNT_VAR, ++count);

        return false;
    }

    @Override
    public void orphan(DCSet dcSet, Transaction transaction) {

        SmartContractValues valuesMap = dcSet.getSmartContractValues();
        loadValues(valuesMap);

        // leafKey already calculated OR get from DB
        action(dcSet, null, transaction, true);

        if (count == 1) {
            /**
             * remove all data from db
             */
            wipe(dcSet, valuesMap);
        } else {
            valuesMap.put(COUNT_VAR, --count);
        }

    }

    private void wipe(DCSet dcSet, SmartContractValues valuesMap) {
        // remove ASSET
        dcSet.getItemAssetMap().decrementDelete(keyInit);

        // TODO - сделать удаление всех разом по Tuple2(id, null)
        valuesMap.delete(INIT_KEY);
        valuesMap.delete(COUNT_VAR);
    }

}
