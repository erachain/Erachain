package org.erachain.core.transaction;

import com.google.common.primitives.Bytes;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAppendix;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.dapp.DAPP;
import org.erachain.database.IDB;
import org.erachain.datachain.DCSet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class RSendPacketTest {

    static Logger LOGGER = LoggerFactory.getLogger(RSendPacketTest.class.getName());

    byte[] typeBytes = new byte[]{RSend.TYPE_ID, 0, 0, 0};

    Controller cntrl;
    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("tes213sdffsdft".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    byte[] seed_1 = Crypto.getInstance().digest("tes213sdffsdft_1".getBytes());
    byte[] privateKey_1 = Crypto.getInstance().createKeyPair(seed_1).getA();
    PrivateKeyAccount maker_1 = new PrivateKeyAccount(privateKey_1);

    Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");

    AssetCls asset;
    AssetCls assetMovable;
    long key = AssetCls.FEE_KEY;
    RSend rSend;
    //CREATE EMPTY MEMORY DATABASE
    private DCSet dcSet;
    private GenesisBlock gb;
    Block block;
    private BlockChain bchain;

    ExLink exLink = new ExLinkAppendix(123123L);
    DAPP DAPP = null;
    Object[][] packet = null;
    byte feePow = 0;
    String title = "test";
    byte[] messageDate = "message MESS".getBytes();
    byte[] isTextByte = new byte[]{1};
    byte[] encryptedByte = new byte[]{0};
    long timestamp = 123L;
    long flagsTX = 0L;
    byte[] signatureBytes = Bytes.concat(Crypto.getInstance().digest("456123".getBytes()), Crypto.getInstance().digest("q234234".getBytes()));
    long seqNo = 98123234;
    long feeLong = 123456L;

    // INIT ASSETS
    private void init() {

        dcSet = DCSet.createEmptyDatabaseSet(IDB.DBS_MAP_DB);
        Controller.getInstance().setDCSet(dcSet);
        gb = new GenesisBlock();
        block = gb;

        try {
            gb.process(dcSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, dcSet);
        maker.changeBalance(dcSet, false, false, AssetCls.ERA_KEY, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        maker.changeBalance(dcSet, false, false, AssetCls.FEE_KEY, BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

    }

    @Test
    public void parse() {
    }

    @Test
    public void toBytes() {

        int balancePos = TransactionAmount.ACTION_SEND;

        packet = new Object[2][];
        // 0: (long) AssetKey, 1: Amount, 2: Price, 3: Discounted Price, 4: Tax as percent, 5: Fee as absolute value, 6: memo, 7: Asset (after setDC())
        packet[0] = new Object[]{3L, new BigDecimal("123.023"), new BigDecimal("120.0"),
                new BigDecimal("0.015"), new BigDecimal("1.5"), null, "memo memo", null};
        packet[1] = new Object[]{4L, new BigDecimal("500.0"), new BigDecimal("500.0"),
                null, null, new BigDecimal("5.0"), "memo 3 memo", null};

        rSend = new RSend(typeBytes, maker, exLink, DAPP, feePow, recipient, balancePos, key, packet, title, messageDate, isTextByte,
                encryptedByte, timestamp, flagsTX, signatureBytes, seqNo, feeLong);

        byte[] raw = rSend.toBytes(Transaction.FOR_NETWORK, true);

        RSend parsedTX;
        try {
            parsedTX = (RSend) RSend.Parse(raw, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            assertEquals(e.getMessage(), "");
            parsedTX = null;
        }

        assertEquals(parsedTX.getPacket()[0][6], rSend.getPacket()[0][6]);
        assertEquals(parsedTX.getPacket()[1][6], rSend.getPacket()[1][6]);

    }

    @Test
    public void processPacketSend() {

        init();

        BigDecimal tax1 = new BigDecimal("0.01");
        BigDecimal tax1min = new BigDecimal("1");
        BigDecimal tax2 = new BigDecimal("0.01");
        BigDecimal tax2min = new BigDecimal("0.01");
        BlockChain.ASSET_TRANSFER_PERCENTAGE_TAB.put(1L, tax1);
        BlockChain.ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(1L, tax1min);
        BlockChain.ASSET_TRANSFER_PERCENTAGE_TAB.put(2L, tax2);
        BlockChain.ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(2L, tax2min);
        BlockChain.ASSET_BURN_PERCENTAGE_TAB.put(1L, new BigDecimal("0.25"));
        BlockChain.ASSET_BURN_PERCENTAGE_TAB.put(2L, new BigDecimal("0.25"));


        int balancePos = TransactionAmount.ACTION_SEND;

        packet = new Object[2][];
        // 0: (long) AssetKey, 1: Amount, 2: Price, 3: Discounted Price, 4: Tax as percent, 5: Fee as absolute value, 6: memo, 7: Asset (after setDC())
        packet[0] = new Object[]{AssetCls.ERA_KEY, new BigDecimal("123.023"), new BigDecimal("120.0"),
                new BigDecimal("0.015"), new BigDecimal("1.5"), null, "memo memo", null};
        packet[1] = new Object[]{AssetCls.FEE_KEY, new BigDecimal("0.5"), new BigDecimal("0.045"),
                null, null, new BigDecimal("0.0"), "memo 3 memo", null};

        rSend = new RSend(typeBytes, maker, exLink, DAPP, feePow, recipient, balancePos, key, packet, title, messageDate, isTextByte,
                encryptedByte, timestamp, flagsTX, signatureBytes, seqNo, feeLong);

        rSend.setHeightSeq(BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        rSend.sign(maker, Transaction.FOR_NETWORK);
        rSend.setDC(dcSet, Transaction.FOR_NETWORK, BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);

        BigDecimal balMaker1 = maker.getBalance(dcSet, AssetCls.ERA_KEY, balancePos).b;
        BigDecimal balMaker2 = maker.getBalance(dcSet, AssetCls.FEE_KEY, balancePos).b;
        BigDecimal balRecipient1 = recipient.getBalance(dcSet, AssetCls.ERA_KEY, balancePos).b;
        BigDecimal balRecipient2 = recipient.getBalance(dcSet, AssetCls.FEE_KEY, balancePos).b;

        ///////////////// PROCESS
        rSend.processBody(block, Transaction.FOR_NETWORK);

        BigDecimal tax1result = Objects.requireNonNull(TransactionAmount.calcSendTAX(999, (Long) packet[0][0], (AssetCls) packet[0][7], (BigDecimal) packet[0][1])).a;
        BigDecimal tax2result = Objects.requireNonNull(TransactionAmount.calcSendTAX(999, (Long) packet[1][0], (AssetCls) packet[1][7], (BigDecimal) packet[1][1])).a;

        BigDecimal tax1TXresult = rSend.assetsPacketFEE.get((AssetCls) packet[0][7]).a;
        BigDecimal tax2TXresult = rSend.assetsPacketFEE.get((AssetCls) packet[1][7]).a;

        assertEquals(tax1result, tax1TXresult);
        assertEquals(tax2result, tax2TXresult);

        assertEquals(balRecipient1.add((BigDecimal) packet[0][1]), recipient.getBalance(dcSet, AssetCls.ERA_KEY, balancePos).b);
        assertEquals(balRecipient2.add((BigDecimal) packet[1][1]), recipient.getBalance(dcSet, AssetCls.FEE_KEY, balancePos).b);

        BigDecimal balMaker1new = maker.getBalance(dcSet, AssetCls.ERA_KEY, balancePos).b;
        assertEquals(balMaker1.subtract((BigDecimal) packet[0][1]).subtract(tax1result), balMaker1new);

        assertEquals(balMaker2.subtract((BigDecimal) packet[1][1]).subtract(rSend.getFee()).subtract(tax2result),
                maker.getBalance(dcSet, AssetCls.FEE_KEY, balancePos).b);


        ///////////////// ORPHAN
        /// TAKE FORN DB
        rSend = (RSend) rSend.copy();
        rSend.setDC(dcSet, Transaction.FOR_NETWORK, BlockChain.SKIP_INVALID_SIGN_BEFORE, 1);
        rSend.orphan(block, Transaction.FOR_NETWORK);

        assertEquals(balMaker1.stripTrailingZeros(),
                maker.getBalance(dcSet, AssetCls.ERA_KEY, balancePos).b.stripTrailingZeros());
        assertEquals(balRecipient1.stripTrailingZeros(), recipient.getBalance(dcSet, AssetCls.ERA_KEY, balancePos).b.stripTrailingZeros());

        assertEquals(balMaker2.stripTrailingZeros(),
                maker.getBalance(dcSet, AssetCls.FEE_KEY, balancePos).b.stripTrailingZeros());
        assertEquals(balRecipient2.stripTrailingZeros(), recipient.getBalance(dcSet, AssetCls.FEE_KEY, balancePos).b.stripTrailingZeros());


        /// TAXES
        tax1TXresult = rSend.assetsPacketFEE.get((AssetCls) packet[0][7]).a;
        tax2TXresult = rSend.assetsPacketFEE.get((AssetCls) packet[1][7]).a;

        assertEquals(tax1result, tax1TXresult);
        assertEquals(tax2result, tax2TXresult);

    }

}