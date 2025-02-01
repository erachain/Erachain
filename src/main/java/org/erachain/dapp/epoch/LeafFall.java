package org.erachain.dapp.epoch;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.DApp;
import org.erachain.dapp.DAppFactory;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.SmartContractValues;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static org.erachain.core.item.assets.AssetTypes.AS_INSIDE_ASSETS;
import static org.erachain.core.item.assets.AssetTypes.AS_SELF_MANAGED_ACCOUNTING;

/**
 * Ctrl+Shift-T (IntellijIDEA) - make test unit
 */
public class LeafFall extends EpochDApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeafFall.class.getSimpleName());

    public static final int ID = 1;
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));
    static public final String NAME = "Magic Leaf";
    static public final boolean DISABLED = BlockChain.MAIN_MODE;

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
    private static long[] LEAF_KEY = BlockChain.MAIN_MODE ?
            new long[]{1048725L, 1048727L, 1048726L, 1048728L, 1048729L, 1048730L, 1048731L, 1048732L}
            : new long[]{20201L, 20202L, 20203L, 20204L, 20205L, 20206L, 20207L, 20208L};

    // use for check: need to initiate values?
    static final Fun.Tuple2 INIT_KEY = new Fun.Tuple2(ID, "i");
    static final Fun.Tuple2 COUNT_VAR = new Fun.Tuple2(ID, "c");

    public LeafFall() {
        super(ID, MAKER);
    }

    public LeafFall(Transaction commandTx, Block block) {
        super(ID, MAKER, commandTx, block);
    }

    public LeafFall(int resultHash) {
        super(ID, MAKER);
        this.resultHash = resultHash;
    }

    @Override
    public DApp of(Transaction commandTx, Block block) {
        // Тут по другому он создается - на основе сложного порядка
        throw new RuntimeException("Wrong OF(...)");
    }

    public static void setDAppFactory() {
        DAppFactory.DAPP_BY_ID.put(ID, new LeafFall());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isDisabled(int height) {
        return DISABLED;
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

    private void action(boolean asOrphan) {

        if (resultHash == 0) {
            makeResultHash(block, commandTx);
        }

        int levelResult = getLevelResult();
        long leafKey = LEAF_KEY[levelResult];

        PublicKeyAccount creator = commandTx.getCreator();
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
                    "Magic leaf fall win: %1!".replaceFirst("%1", "" + levelResult), commandTx.getDBRef());
        }

    }

    @Override
    public Object[][] getItemsKeys() {

        Object[][] itemKeys = new Object[1][];
        itemKeys[0] = new Object[]{ItemCls.ASSET_TYPE, LEAF_KEY[getLevelResult()]};

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

    private void init() {

        // обязательно так как может из Сборки Блока прити уже иниализированный
        count = 0;

        /**
         * for accounting total leaf for person
         */
        AssetVenture leafSum = new AssetVenture(null, stock, "LeafFall_sum", null, null,
                null, AS_SELF_MANAGED_ACCOUNTING, 0, 0);
        leafSum.setReference(commandTx.getSignature(), commandTx.getDBRef());

        //INSERT INTO DATABASE
        keyInit = dcSet.getItemAssetMap().incrementPut(leafSum);
        dcSet.getSmartContractValues().put(INIT_KEY, keyInit);

        ItemAssetMap map = dcSet.getItemAssetMap();
        if (BlockChain.MAIN_MODE) {
            // Тут вручную созданы активы - надо их перевести на владение в Контракт
            for (long leafKey : LEAF_KEY) {
                AssetCls leafAsset = map.get(leafKey);
                // Переведем ранее созданные активы на этот Контракт
                leafAsset.setMaker(stock);
                // update MAKER
                map.put(leafKey, leafAsset);
            }
        } else {
            for (int i = 0; i < LEAF_KEY.length; i++) {
                AssetVenture leaf = new AssetVenture(null, stock, "LeafFall_" + (i + 1), null, null,
                        null, AS_INSIDE_ASSETS, 0, 0);
                leaf.setReference(commandTx.getSignature(), commandTx.getDBRef());
                //INSERT INTO BLOCKCHAIN DATABASE
                dcSet.getItemAssetMap().put(LEAF_KEY[i], leaf);
                //INSERT INTO CONTRACT DATABASE
                //dcSet.getSmartContractValues().put(keyID, assetKey);
            }
        }

    }

    private void loadValues(SmartContractValues valuesMap) {
        keyInit = (Long) valuesMap.get(INIT_KEY);
        count = (Integer) valuesMap.get(COUNT_VAR);
    }

    @Override
    public void process() {

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
            init();
        }

        action(false);

        valuesMap.put(COUNT_VAR, ++count);

    }

    @Override
    public void orphan() {

        SmartContractValues valuesMap = dcSet.getSmartContractValues();
        loadValues(valuesMap);

        // leafKey already calculated OR get from DB
        action(true);

        if (count == 1) {
            /**
             * remove all data from db
             */
            wipe(valuesMap);
        } else {
            valuesMap.put(COUNT_VAR, --count);
        }

    }

    private void wipe(SmartContractValues valuesMap) {
        // remove ASSET
        dcSet.getItemAssetMap().decrementDelete(keyInit);

        // TODO - сделать удаление всех разом по Tuple2(id, null)
        valuesMap.delete(INIT_KEY);
        valuesMap.delete(COUNT_VAR);
    }

}
