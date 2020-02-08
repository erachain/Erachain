package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.persons.PersonHuman;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.ntp.NTP;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RSertifyPubKeysTest {

    int[] TESTED_DBS = new int[]{IDB.DBS_MAP_DB, IDB.DBS_ROCK_DB};
    DCSet dcSet;

    Controller cntrl;

    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
    byte[] seed_1 = Crypto.getInstance().digest("tes213sdffsdft_1".getBytes());
    byte[] privateKey_1 = Crypto.getInstance().createKeyPair(seed_1).getA();
    PrivateKeyAccount creator_2 = new PrivateKeyAccount(privateKey_1);
    long key = 0;
    byte[] ownerSignature = new byte[64];
    List<PublicKeyAccount> sertifiedPublicKeys = new ArrayList<>();
    byte feePow = (byte) 0;
    byte gender = (byte) 0;
    long timestamp;
    PersonCls person;
    ItemAssetMap assetMap;
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    private GenesisBlock gb;
    private BlockChain bchain;

    // INIT ASSETS
    private void init(int dbs) {

        dcSet = DCSet.createEmptyHardDatabaseSet(dbs);
        cntrl = Controller.getInstance();
        cntrl.initBlockChain(dcSet);
        bchain = cntrl.getBlockChain();
        gb = bchain.getGenesisBlock();

        assetMap = dcSet.getItemAssetMap();

        sertifiedPublicKeys.add(creator_2);

        timestamp = NTP.getTime();

        person = new PersonHuman(creator, "START PERSON",
                timestamp, timestamp, gender, "String race", 0f, 0f,
                "String skinColor", "String eyeColor", "String hairСolor", 170,
                icon, image, ".", ownerSignature);
        person.setReference(new byte[64]);

        person.insertToMap(dcSet, BlockChain.AMOUNT_SCALE_FROM + 1);


    }

    @Test
    public void test1() {

        for (int dbs : TESTED_DBS) {

            init(dbs);

            assertEquals(creator.getLastTimestamp(dcSet), null);

            RSertifyPubKeys certPubKey = new RSertifyPubKeys(0, creator, feePow,
                    person.getKey(dcSet), sertifiedPublicKeys, timestamp, 0L);
            certPubKey.setDC(dcSet, Transaction.FOR_NETWORK, 3, 5);
            certPubKey.process(null, Transaction.FOR_NETWORK);

            creator.getLastTimestamp(dcSet);

            assertEquals(new long[]{123L, 345L}, creator.getLastTimestamp(dcSet));

        }

    }


    @Test
    public void parse() {
    }

    @Test
    public void toBytes() {
    }

    @Test
    public void isSignatureValid() {
    }

    @Test
    public void isValid() {
    }

    @Test
    public void process() {
    }

    @Test
    public void orphan() {
    }
}