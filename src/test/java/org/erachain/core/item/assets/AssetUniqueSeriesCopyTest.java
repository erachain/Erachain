package org.erachain.core.item.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.IssueAssetTransaction;
import org.erachain.core.transaction.Transaction;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AssetUniqueSeriesCopyTest {

    int forDeal = Transaction.FOR_NETWORK;
    long dbRef = 1111L;

    byte feePow = 0;

    byte[] itemAppData;

    private byte[] icon = "qwe qweiou sdjk fh".getBytes(StandardCharsets.UTF_8);
    private byte[] image = "qwe qweias d;alksd;als dajd lkasj dlou sdjk fh".getBytes(StandardCharsets.UTF_8);

    Controller cntrl;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

    @Test
    public void parse() {

        boolean iconAsURL = true;
        int iconType = AssetCls.MEDIA_TYPE_VIDEO;
        boolean imageAsURL = true;
        int imageType = AssetCls.MEDIA_TYPE_AUDIO;
        Long startDate = System.currentTimeMillis();
        Long stopDate = null;
        itemAppData = AssetCls.makeAppData(iconAsURL, iconType, imageAsURL, imageType,
                startDate, stopDate, "tag", null, true, true, true);
        AssetUnique origAsset = new AssetUnique(itemAppData, maker, "String name", null, null, "String description", 3);

        IssueAssetTransaction issueTX = new IssueAssetTransaction(maker, origAsset, feePow, System.currentTimeMillis(), 0L);
        issueTX.sign(maker, Transaction.FOR_NETWORK);

        AssetVenture assetVenture = new AssetVenture(itemAppData, maker, "copy", icon, image, "...", 0, 8, 10L);


        AssetUniqueSeriesCopy assetCopy = AssetUniqueSeriesCopy.makeCopy(issueTX, assetVenture, 1001, 10, 2);

        //CONVERT TO BYTES
        byte[] rawAsset = assetCopy.toBytes(Transaction.FOR_NETWORK, false, false);

        //CHECK DATA LENGTH - 157
        assertEquals(rawAsset.length, assetCopy.getDataLength(false));

        try {
            //PARSE FROM BYTES
            AssetCls parsedAsset = AssetFactory.getInstance().parse(Transaction.FOR_NETWORK, rawAsset, false);

            //CHECK INSTANCE
            assertEquals(true, parsedAsset instanceof AssetUniqueSeriesCopy);

            //CHECK ISSUER
            assertEquals(assetCopy.getMaker().getAddress(), parsedAsset.getMaker().getAddress());

            assertEquals(assetCopy.getName(), parsedAsset.getName());

            assertEquals(Arrays.equals(assetCopy.getAppData(), parsedAsset.getAppData()), true);

            assertEquals(assetCopy.getDescription(), parsedAsset.getDescription());

            assertEquals(assetCopy.getQuantity(), parsedAsset.getQuantity());

            assertEquals(assetCopy.isUseDApp(), parsedAsset.isUseDApp());


        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }

    }
}