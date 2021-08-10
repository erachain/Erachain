package org.erachain.core.item.assets;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class AssetUniqueSeriesCopyTest {

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

        AssetVenture assetVenture = new AssetVenture(itemAppData, maker, "movable", icon, image, "...", 0, 8, 10L);

        boolean iconAsURL = true;
        int iconType = AssetCls.MEDIA_TYPE_VIDEO;
        boolean imageAsURL = true;
        int imageType = AssetCls.MEDIA_TYPE_AUDIO;
        Long startDate = System.currentTimeMillis();
        Long stopDate = null;
        itemAppData = AssetCls.makeAppData(iconAsURL, iconType, imageAsURL, imageType,
                startDate, stopDate, "tag", null, true);
        AssetUniqueSeriesCopy assetUniqueCopy = new AssetUniqueSeriesCopy(1L, assetVenture, 10, 1);
        //AssetUniqueSeriesCopy.setReference(Crypto.getInstance().digest(assetUniqueCopy.toBytes(forDeal, false, false)), dbRef);

    }
}