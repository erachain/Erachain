package org.erachain.core.item.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AssetVentureTest {

    int forDeal = Transaction.FOR_NETWORK;
    long dbRef = 1111L;

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
                startDate, stopDate, "tag", null, true, true);

        AssetVenture assetVenture = new AssetVenture(itemAppData, maker, "movable", icon, image, "...", 0, 8, 10L);

        //CONVERT TO BYTES
        byte[] rawAsset = assetVenture.toBytes(Transaction.FOR_NETWORK, false, false);

        //CHECK DATA LENGTH - 157
        assertEquals(rawAsset.length, assetVenture.getDataLength(false));

        try {
            //PARSE FROM BYTES
            AssetCls parsedAsset = AssetFactory.getInstance().parse(Transaction.FOR_NETWORK, rawAsset, false);

            //CHECK INSTANCE
            assertEquals(true, parsedAsset instanceof AssetVenture);

            //CHECK ISSUER
            assertEquals(assetVenture.getMaker().getAddress(), parsedAsset.getMaker().getAddress());

            assertEquals(Arrays.equals(assetVenture.getAppData(), parsedAsset.getAppData()), true);

            assertEquals(assetVenture.getDescription(), parsedAsset.getDescription());

            assertEquals(assetVenture.getQuantity(), parsedAsset.getQuantity());

            assertEquals(assetVenture.isAnonimDenied(), parsedAsset.isAnonimDenied());

        } catch (Exception e) {
            fail("Exception while parsing transaction.");
        }


    }

    @Test
    public void toBytes() {
    }

    @Test
    public void getDataLength() {
    }
}