package org.erachain.core.transaction;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.junit.Test;
import org.mapdb.Fun;

import java.math.BigDecimal;


@Slf4j
public class IssueAssetTransactionTest {

    long FEE_KEY = AssetCls.FEE_KEY;

    Controller cntrl;

    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    byte[] seed_1 = Crypto.getInstance().digest("tes213sdffsdft_1".getBytes());
    byte[] privateKey_1 = Crypto.getInstance().createKeyPair(seed_1).getA();
    PrivateKeyAccount maker_1 = new PrivateKeyAccount(privateKey_1);
    AssetCls asset;
    AssetCls assetMovable;
    long key = 0;
    Fun.Tuple5<Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>, Fun.Tuple2<BigDecimal, BigDecimal>>
            balance5;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;
    private BlockChain bchain;
    ItemAssetMap assetMap;

    // INIT ASSETS
    private void init() {

        db = DCSet.createEmptyDatabaseSet(0);
        cntrl = Controller.getInstance();
        cntrl.initBlockChain(db);
        bchain = cntrl.getBlockChain();
        gb = bchain.getGenesisBlock();

        assetMap = db.getItemAssetMap();

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        maker.changeBalance(db, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false);

        maker_1.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);

    }

    @Test
    public void test1() {

        init();

        asset = new AssetVenture(maker, "aasdasd", icon, image, "asdasda", 1, 8, 50000l);
        // set SCALABLE assets ++
        asset.setReference(Crypto.getInstance().digest(asset.toBytes(false, false)));
        asset.insertToMap(db, BlockChain.AMOUNT_SCALE_FROM);
        asset.insertToMap(db, 0l);
        key = asset.getKey(db);


        boolean twice = false;


        do {
            long timeMillisBefore = System.currentTimeMillis();


            int k = 0;
            int step = 10;

            // создадим в базе несколько записей
            do {
                assetMovable = new AssetVenture(maker, "movable-" + key, icon, image, "...", 0, 8, 500l);
                assetMovable.setReference(Crypto.getInstance().digest(assetMovable.toBytes(false, false)));
                key = assetMovable.insertToMap(db, 1000);
            } while (k++ < 5);

            db.flush(k, true, false);
            logger.info("SIZE = " + assetMap.size());

            k = 0;
            do {
                int key = assetMap.size();
                AssetCls item = (AssetCls)assetMap.remove(key);
            } while(k++ < 1);

            db.flush(k, true, false);

            db.close();
            logger.info("End test " + (twice? "create DB" : "open DB"));
            twice = !twice;

        } while (twice);
    }

    @Test
    public void process() {
    }

    @Test
    public void orphan() {
    }
}