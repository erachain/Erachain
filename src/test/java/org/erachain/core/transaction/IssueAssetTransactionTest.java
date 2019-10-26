package org.erachain.core.transaction;

import lombok.extern.slf4j.Slf4j;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.settings.Settings;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.Fun;

import java.io.File;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;


@Slf4j
public class IssueAssetTransactionTest {

    long FEE_KEY = AssetCls.FEE_KEY;

    int[] TESTED_DBS = new int[]{IDB.DBS_MAP_DB, IDB.DBS_ROCK_DB, IDB.DBS_MAP_DB_IN_MEM};
    DCSet dcSet;

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

    private GenesisBlock gb;
    private BlockChain bchain;
    ItemAssetMap assetMap;

    // INIT ASSETS
    private void init(int dbs) {

        File dbFile = new File(Settings.getInstance().getDataDir(), "chain.dat");
        dbFile.getParentFile().mkdirs();

        DB database = DCSet.makeFileDB(dbFile);

        dcSet = DCSet.createEmptyHardDatabaseSet(database, dbs);
        cntrl = Controller.getInstance();
        cntrl.initBlockChain(dcSet);
        bchain = cntrl.getBlockChain();
        gb = bchain.getGenesisBlock();

        assetMap = dcSet.getItemAssetMap();

    }

    @Test
    public void test1() {

        int START_KEY = 1000;
        boolean twice = false;
        int size;

        for (int dbs: TESTED_DBS) {

            do {

                init(dbs);

                int k = 0;
                int step = 3;

                // создадим в базе несколько записей
                do {
                    assetMovable = new AssetVenture(maker, "movable-" + key, icon, image, "...", 0, 8, 500l);
                    assetMovable.setReference(Crypto.getInstance().digest(assetMovable.toBytes(false, false)));
                    key = assetMovable.insertToMap(dcSet, START_KEY);
                    size = assetMap.size();
                    assertEquals(key, size);
                } while (k++ < step);

                //dcSet.flush(k, true, false);
                logger.info("SIZE = " + assetMap.size());

                k = 0;
                do {
                    key = assetMap.size();
                    AssetCls item = (AssetCls) assetMap.remove(key);
                } while (k++ < step);

                dcSet.flush(k, true, false);

                dcSet.close();
                logger.info("End test " + (twice ? "create DB" : "open DB"));
                twice = !twice;

            } while (twice);
        }
    }

    @Test
    public void process() {
    }

    @Test
    public void orphan() {
    }
}