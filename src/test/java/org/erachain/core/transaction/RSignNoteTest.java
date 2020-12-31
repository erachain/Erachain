package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.ExPays;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.ntp.NTP;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class RSignNoteTest {

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

    byte version = (byte) 3;
    byte[] flagsExData = new byte[4];

    byte[] exDataBytes;

    @Test
    public void toBytes() {

        ExLink exLink = null;
        int feePow = 0;
        long templateKey = 0;

        int flags = 0;
        Long assetKey = 1L;
        int balancePos = 1;
        boolean backward = false;
        int payMethod = ExPays.PAYMENT_METHOD_ABSOLUTE;
        BigDecimal payMethodValue = BigDecimal.ONE;
        BigDecimal amountMin = null;
        BigDecimal amountMax = null;
        Long filterAssetKey = 2L;
        int filterBalancePos = TransactionAmount.ACTION_SEND;
        int filterBalanceSide = TransactionAmount.BALANCE_SIDE_LEFT;
        BigDecimal filterBalanceMIN = null;
        BigDecimal filterBalanceMAX = BigDecimal.TEN;
        int filterTXType = 0;
        Long filterTXStartSeqNo = null;
        Long filterTXEndSeqNo = null;
        int filterByGender = ExPays.FILTER_PERSON_ONLY_MAN;
        boolean selfPay = true;

        ExPays exPays = new ExPays(flags, assetKey, balancePos, backward, payMethod, payMethodValue, amountMin, amountMax,
                filterAssetKey, filterBalancePos, filterBalanceSide, filterBalanceMIN, filterBalanceMAX,
                filterTXType, filterTXStartSeqNo, filterTXEndSeqNo, filterByGender, selfPay);

        ExData exData = new ExData(flagsExData, exLink, exPays, "title", (byte) 0, null,
                (byte) 0, null,
                (byte) 0, null, null, (byte) 0, null, null);

        try {
            exDataBytes = exData.toByte();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            exDataBytes = null;
        }

        RSignNote rNote = new RSignNote(version, (byte) 0, (byte) 0, maker_1, (byte) feePow,
                templateKey, exDataBytes, NTP.getTime(), 0L);


        byte[] noteBytes = rNote.toBytes(Transaction.FOR_DB_RECORD, true);
        RSignNote parsedNote = null;
        byte[] parsedBytes;
        try {
            parsedNote = (RSignNote) RSignNote.Parse(noteBytes, Transaction.FOR_DB_RECORD);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        byte[] bytes = parsedNote.toBytes(Transaction.FOR_DB_RECORD, true);

        assertEquals(bytes.length, noteBytes.length);


    }

    @Test
    public void parse() {
    }

    @Test
    public void getDataLength() {
    }

    @Test
    public void process() {
    }

    @Test
    public void orphan() {
    }
}