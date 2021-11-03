package org.erachain.core.transaction;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.exActions.ExFilteredPays;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class RSignNoteTest {

    Controller cntrl;
    BlockChain bchain;
    DCSet dcSet;
    private GenesisBlock gb;
    int asPack = Transaction.FOR_NETWORK;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 1;
    long timestamp = NTP.getTime();

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
    JSONObject json = new JSONObject();

    byte version = (byte) 3;
    byte[] flagsExData = new byte[]{version, 0, 0, 0};

    byte[] exDataBytes;

    private void init() {

        dcSet = DCSet.createEmptyHardDatabaseSet(IDB.DBS_MAP_DB);
        cntrl = Controller.getInstance();
        cntrl.initBlockChain(dcSet);
        bchain = cntrl.getBlockChain();
        gb = bchain.getGenesisBlock();

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        maker.changeBalance(dcSet, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        json.put("MS", "Message");
    }

    @Test
    public void toBytes() {

        init();

        ExLink exLink = null;
        int feePow = 0;
        long templateKey = 0;

        int flags = 0;
        Long assetKey = 1L;
        int balancePos = 1;
        boolean backward = false;
        int payMethod = ExFilteredPays.PAYMENT_METHOD_ABSOLUTE;
        BigDecimal payMethodValue = BigDecimal.ONE;
        BigDecimal amountMin = null;
        BigDecimal amountMax = null;
        Long filterAssetKey = 2L;
        int filterBalancePos = TransactionAmount.ACTION_SEND;
        int filterBalanceSide = Account.BALANCE_SIDE_LEFT;
        BigDecimal filterBalanceMIN = null;
        BigDecimal filterBalanceMAX = BigDecimal.TEN;
        int filterTXType = 0;
        Long filterTXStartSeqNo = null;
        Long filterTXEndSeqNo = null;
        int filterByGender = ExFilteredPays.FILTER_PERSON_ONLY_MAN;
        boolean selfPay = true;

        ExFilteredPays exFilteredPays = new ExFilteredPays(flags, assetKey, balancePos, backward, payMethod, payMethodValue, amountMin, amountMax,
                filterAssetKey, filterBalancePos, filterBalanceSide, filterBalanceMIN, filterBalanceMAX,
                filterTXType, filterTXStartSeqNo, filterTXEndSeqNo, filterByGender, selfPay);

        ExData exData = new ExData(flagsExData, exLink, exFilteredPays, "title", (byte) 0, null,
                (byte) 0, null,
                (byte) 0, null, null, json, null);

        try {
            exDataBytes = exData.toByte();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            exDataBytes = null;
        }

        RSignNote rNote = new RSignNote(version, (byte) 0, (byte) 0, maker, (byte) feePow,
                templateKey, exDataBytes, NTP.getTime(), 0L);
        rNote.sign(maker, Transaction.FOR_NETWORK);
        rNote.setDC(dcSet, Transaction.FOR_NETWORK, 1, 1, true);

        assertEquals(Transaction.VALIDATE_OK, rNote.isValid(Transaction.FOR_NETWORK, flags));

        int lenBeforeToBytes = rNote.getDataLength(Transaction.FOR_DB_RECORD, true);
        byte[] noteBytes = rNote.toBytes(Transaction.FOR_DB_RECORD, true);
        assertEquals(lenBeforeToBytes, noteBytes.length);

        rNote.process(gb, Transaction.FOR_NETWORK);

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