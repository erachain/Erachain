package org.erachain;

import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.core.transaction.*;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.ntp.NTP;
import org.junit.Ignore;
import org.junit.Test;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@Ignore
public class DatabaseTests {

    static Logger LOGGER = LoggerFactory.getLogger(DatabaseTests.class.getName());
    byte[] assetReference = new byte[Crypto.SIGNATURE_LENGTH];
    Long releaserReference = null;

    long[] itemFlags = null;
    long txFlags = 0L;

    BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
    long ERM_KEY = Transaction.RIGHTS_KEY;
    long FEE_KEY = Transaction.FEE_KEY;
    //long ALIVE_KEY = StatusCls.ALIVE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] personReference = new byte[Crypto.SIGNATURE_LENGTH];
    long timestamp = NTP.getTime();
    Long last_ref;
    boolean asPack = false;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    PersonCls personGeneral;
    PersonCls person;
    long personKey = -1;
    IssuePersonRecord issuePersonTransaction;
    //int version = 0; // without signs of person
    int version = 1; // with signs of person
    private byte[] icon = new byte[0]; // default value
    private byte[] image = new byte[0]; // default value
    private byte[] ownerSignature = new byte[Crypto.SIGNATURE_LENGTH];
    //CREATE EMPTY MEMORY DATABASE
    private DCSet dcSet;
    private GenesisBlock gb;

    // INIT PERSONS
    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet(0);

        gb = new GenesisBlock();
        try {
            gb.process(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        last_ref = gb.getTimestamp();

        // GET RIGHTS TO CERTIFIER
        byte gender = 1;
        long birthDay = timestamp - 12345678;
        personGeneral = new PersonHuman(itemFlags, maker, "Ermolaev Dmitrii Sergeevich as certifier", birthDay, birthDay - 1,
                gender, "Slav", (float) 28.12345, (float) 133.7777,
                "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);

        GenesisIssuePersonRecord genesis_issue_person = new GenesisIssuePersonRecord(personGeneral);
        genesis_issue_person.process(gb, Transaction.FOR_NETWORK);
        GenesisCertifyPersonRecord genesis_certify = new GenesisCertifyPersonRecord(maker, 0L);
        genesis_certify.process(gb, Transaction.FOR_NETWORK);

        maker.setLastTimestamp(new long[]{last_ref, 0}, dcSet);
        maker.changeBalance(dcSet, true, false, ERM_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        maker.changeBalance(dcSet, true, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        person = new PersonHuman(itemFlags, maker, "Ermolaev Dmitrii Sergeevich", birthDay, birthDay - 2,
                gender, "Slav", (float) 28.12345, (float) 133.7777,
                "white", "green", "шанет", 188, icon, image, "изобретатель, мыслитель, создатель идей", ownerSignature);

        //CREATE ISSUE PERSON TRANSACTION
        issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp, maker.getLastTimestamp(dcSet)[0], null);

    }

    @Test
    public void databaseFork() {

        init();

        issuePersonTransaction.sign(maker, Transaction.FOR_NETWORK);
        issuePersonTransaction.process(gb, Transaction.FOR_NETWORK);

        issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp++, maker.getLastTimestamp(dcSet)[0], null);
        issuePersonTransaction.sign(maker, Transaction.FOR_NETWORK);
        issuePersonTransaction.process(gb, Transaction.FOR_NETWORK);

        issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp++, maker.getLastTimestamp(dcSet)[0], null);
        issuePersonTransaction.sign(maker, Transaction.FOR_NETWORK);
        issuePersonTransaction.process(gb, Transaction.FOR_NETWORK);


        //assertEquals(dcSet.getItemPersonMap().keySet().toString(), "");
        //assertEquals(dcSet.getItemPersonMap().getValuesAll().toString(), "");
        //CREATE FORK
        DCSet fork = dcSet.fork(this.toString());

        issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp++, maker.getLastTimestamp(fork)[0], null);
        issuePersonTransaction.sign(maker, Transaction.FOR_NETWORK);
        issuePersonTransaction.process(gb, Transaction.FOR_NETWORK);

        issuePersonTransaction = new IssuePersonRecord(maker, person, FEE_POWER, timestamp++, maker.getLastTimestamp(fork)[0], null);
        issuePersonTransaction.sign(maker, Transaction.FOR_NETWORK);
        issuePersonTransaction.process(gb, Transaction.FOR_NETWORK);

        //assertEquals(PersonCls.getItem(fork, ItemCls.PERSON_TYPE, 1).getDBMap(fork).keySet().toString(), "");

        //SET BALANCE
        dcSet.getAssetBalanceMap().put(seed, 1L, new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                (new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ONE, BigDecimal.ONE),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ONE, BigDecimal.ONE),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ONE, BigDecimal.ONE),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ONE, BigDecimal.ONE),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ONE, BigDecimal.ONE)
                ));

        //CHECK VALUE IN DB
        assertEquals(BigDecimal.ONE, dcSet.getAssetBalanceMap().get(seed, 1L));

        //CHECK VALUE IN FORK
        assertEquals(BigDecimal.ONE, fork.getAssetBalanceMap().get(seed, 1L));

        //SET BALANCE IN FORK
        fork.getAssetBalanceMap().put(seed, 1L, new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                (
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.TEN, BigDecimal.TEN),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.TEN, BigDecimal.TEN),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.TEN, BigDecimal.TEN),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.TEN, BigDecimal.TEN),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.TEN, BigDecimal.TEN)
                ));

        //CHECK VALUE IN DB
        assertEquals(BigDecimal.ONE, dcSet.getAssetBalanceMap().get(seed, 1L));

        //CHECK VALUE IN FORK
        assertEquals(BigDecimal.TEN, fork.getAssetBalanceMap().get(seed, 1L));

        //CREATE SECOND FORK
        DCSet fork2 = fork.fork(this.toString());

        //SET BALANCE IN FORK2
        fork2.getAssetBalanceMap().put(seed, 1L, new Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                (
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO)
                ));

        //CHECK VALUE IN DB
        assertEquals(BigDecimal.ONE, dcSet.getAssetBalanceMap().get(seed, 1L));

        //CHECK VALUE IN FORK
        assertEquals(BigDecimal.TEN, fork.getAssetBalanceMap().get(seed, 1L));

        //CHECK VALUE IN FORK
        assertEquals(BigDecimal.ZERO, fork2.getAssetBalanceMap().get(seed, 1L));
    }

    @Test
    public void databaseAssets() {

        try {
            DCSet.reCreateDB(false, false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        GenesisBlock gb = new GenesisBlock();
        try {
            gb.process(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ItemAssetMap dbMap = dcSet.getItemAssetMap();
        Collection<ItemCls> assets = dbMap.values();
        for (ItemCls asset : assets) {
            //Asset asset = DLSet.getInstance().getAssetMap().get(key);
            AssetCls aa = (AssetCls) asset;
            LOGGER.info("ASSET - " + asset.getKey(dcSet) + " : " + asset.getName()
                    + " : " + aa.getQuantity()
                    + " - " + aa.getReference().length
                    + ": " + aa.getReference());
            //db.add(asset);
        }

        dbMap.incrementPut(dbMap.get(1l));
        LOGGER.info("keys " + dbMap.keySet());

        //Collection<Asset> issues = DLSet.getInstance().getIssueAssetMap.getValuesAll();

        //long key = db.);

    }

    @Test
    public void databaseAssetsAddGet() {

        init();

        AssetCls asset = new AssetVenture(itemFlags, maker, "test", icon, image, "strontje", 0, 8, 50000l);
        Transaction issueAssetTransaction = new IssueAssetTransaction(maker, null, asset, FEE_POWER, timestamp, maker.getLastTimestamp(dcSet)[0]);
        issueAssetTransaction.sign(maker, Transaction.FOR_NETWORK);
        issueAssetTransaction.process(gb, Transaction.FOR_NETWORK);
        //logger.info(asset.toString() + " getQuantity " + asset.getQuantity());

        long key = asset.getKey(dcSet);

        ItemAssetMap assetDB = dcSet.getItemAssetMap();
        Collection<ItemCls> assets = assetDB.values();
        for (ItemCls asset_2 : assets) {
            AssetCls aa = (AssetCls) asset_2;
            LOGGER.info(aa.toString() + " getQuantity " + aa.getQuantity());
        }

    }
}
