package org.erachain.records;

import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.payment.Payment;
import org.erachain.core.transaction.ArbitraryTransactionV3;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.ntp.NTP;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore
public class TransactionV3Tests {

    static Logger LOGGER = LoggerFactory.getLogger(TransactionV3Tests.class.getName());

    Long releaserReference = null;

    long dbRef = 0L;
    long ERM_KEY = AssetCls.ERA_KEY;
    long FEE_KEY = AssetCls.FEE_KEY;
    byte FEE_POWER = (byte) 1;
    byte[] assetReference = new byte[64];
    long timestamp = NTP.getTime();

    ExLink exLink = null;

    byte[] itemAppData = null;
    long txFlags = 0L;

    //CREATE KNOWN ACCOUNT
    byte[] seed = Crypto.getInstance().digest("test".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    Account recipient = new Account("7MFPdpbaxKtLMWq7qvXU6vqTWbjJYmxsLW");
    BigDecimal amount = BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
    byte[] data = "test123!".getBytes();
    byte[] isText = new byte[]{1};
    byte[] encrypted = new byte[]{0};
    private byte[] icon = new byte[]{1, 3, 4, 5, 6, 9}; // default value
    private byte[] image = new byte[]{4, 11, 32, 23, 45, 122, 11, -45}; // default value
    //CREATE EMPTY MEMORY DATABASE
    private DCSet db;
    private GenesisBlock gb;

    // INIT ASSETS
    private void init() {

        db = DCSet.createEmptyDatabaseSet(0);
        gb = new GenesisBlock();
        try {
            gb.process(db);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        // FEE FUND
        maker.setLastTimestamp(new long[]{gb.getTimestamp(), 0}, db);
        maker.changeBalance(db, false, false, ERM_KEY, BigDecimal.valueOf(100).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);
        maker.changeBalance(db, false, false, FEE_KEY, BigDecimal.valueOf(1).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

    }

    @Test
    public void validateMessageTransactionV3() {

        init();


        RSend messageTransactionV3 = new RSend(
                maker, exLink, smartContract, FEE_POWER, //	ATFunding
                recipient,
                ERM_KEY,
                amount,
                "headdd", data,
                isText,
                encrypted,
                timestamp, maker.getLastTimestamp(db)[0]
        );
        messageTransactionV3.sign(maker, Transaction.FOR_NETWORK);

        assertEquals(messageTransactionV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);

        messageTransactionV3.process(gb,Transaction.FOR_NETWORK);

        assertEquals(BigDecimal.valueOf(1).subtract(messageTransactionV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(90).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(ERM_KEY, db));
        assertEquals(BigDecimal.valueOf(10).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient.getBalanceUSE(ERM_KEY, db));

        byte[] rawMessageTransactionV3 = messageTransactionV3.toBytes(Transaction.FOR_NETWORK, true);
        int dd = messageTransactionV3.getDataLength(Transaction.FOR_NETWORK, true);
        assertEquals(rawMessageTransactionV3.length, messageTransactionV3.getDataLength(Transaction.FOR_NETWORK, true));


        RSend messageTransactionV3_2 = null;
        try {
            messageTransactionV3_2 = (RSend) RSend.Parse(rawMessageTransactionV3, Transaction.FOR_NETWORK);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(new String(messageTransactionV3.getData()), new String(messageTransactionV3_2.getData()));
        assertEquals(messageTransactionV3.getCreator(), messageTransactionV3_2.getCreator());
        assertEquals(messageTransactionV3.getRecipient(), messageTransactionV3_2.getRecipient());
        assertEquals(messageTransactionV3.getKey(), messageTransactionV3_2.getKey());
        assertEquals(messageTransactionV3.getAmount(), messageTransactionV3_2.getAmount());
        assertEquals(messageTransactionV3.isEncrypted(), messageTransactionV3_2.isEncrypted());
        assertEquals(messageTransactionV3.isText(), messageTransactionV3_2.isText());

        assertEquals(messageTransactionV3.isSignatureValid(db), true);
        assertEquals(messageTransactionV3_2.isSignatureValid(db), true);
    }


    @Test
    public void validateArbitraryTransactionV3() {

        init();

        //ADD ERM ASSET
        AssetCls aTFundingAsset = new AssetVenture(itemAppData, new GenesisBlock().getCreator(), "ATFunding", icon, image, "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into ERM.", 0, 8, 250000000l);
        aTFundingAsset.setReference(assetReference, dbRef);
        db.getItemAssetMap().set(61l, aTFundingAsset);

        GenesisBlock genesisBlock = new GenesisBlock();
        try {
            genesisBlock.process(db);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();


        Account recipient1 = new Account("79MXwfzHPDGWoQUgyPXRf2fxKuzY1osNsg");
        Account recipient2 = new Account("76abzpJK61F4TAZFkqev2EY5duHVUvycZX");
        Account recipient3 = new Account("7JU8UTuREAJG2yht5ASn7o1Ur34P1nvTk5");

        long timestamp = NTP.getTime();

        //PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS

        maker.changeBalance(db, false, false, 61l, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        List<Payment> payments = new ArrayList<Payment>();
        payments.add(new Payment(recipient1, 61l, BigDecimal.valueOf(110).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
        payments.add(new Payment(recipient2, 61l, BigDecimal.valueOf(120).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
        payments.add(new Payment(recipient3, 61l, BigDecimal.valueOf(201).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));

        ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
                maker, payments, 111,
                data,
                FEE_POWER,
                timestamp + 100, maker.getLastTimestamp(db)[0]
        );
        arbitraryTransactionV3.sign(maker, Transaction.FOR_NETWORK);

        //if (NTP.getTime() < Transaction.getARBITRARY_TRANSACTIONS_RELEASE() || arbitraryTransactionV3.getTimestamp() < Transaction.getPOWFIX_RELEASE())
        if (false) {
            assertEquals(arbitraryTransactionV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.NOT_YET_RELEASED);
        } else {
            assertEquals(arbitraryTransactionV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);
        }

        arbitraryTransactionV3.process(gb,Transaction.FOR_NETWORK);

        assertEquals(BigDecimal.valueOf(1).subtract(arbitraryTransactionV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(1000 - 110 - 120 - 201).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(61l, db));
        assertEquals(BigDecimal.valueOf(110).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient1.getBalanceUSE(61l, db));
        assertEquals(BigDecimal.valueOf(120).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient2.getBalanceUSE(61l, db));
        assertEquals(BigDecimal.valueOf(201).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), recipient3.getBalanceUSE(61l, db));

        byte[] rawArbitraryTransactionV3 = arbitraryTransactionV3.toBytes(Transaction.FOR_NETWORK, true);

        ArbitraryTransactionV3 arbitraryTransactionV3_2 = null;
        try {
            arbitraryTransactionV3_2 = (ArbitraryTransactionV3) ArbitraryTransactionV3.Parse(Arrays.copyOfRange(rawArbitraryTransactionV3, 0, rawArbitraryTransactionV3.length));
            // already SIGNED - arbitraryTransactionV3_2.sign(creator);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(new String(arbitraryTransactionV3.getData()), new String(arbitraryTransactionV3_2.getData()));
        assertEquals(arbitraryTransactionV3.getPayments().get(0).toJson().toJSONString(),
                arbitraryTransactionV3_2.getPayments().get(0).toJson().toJSONString());
        assertEquals(arbitraryTransactionV3.getPayments().get(1).toJson().toJSONString(),
                arbitraryTransactionV3_2.getPayments().get(1).toJson().toJSONString());
        assertEquals(arbitraryTransactionV3.getPayments().get(2).toJson().toJSONString(),
                arbitraryTransactionV3_2.getPayments().get(2).toJson().toJSONString());
        assertEquals(arbitraryTransactionV3.getPayments().size(), arbitraryTransactionV3.getPayments().size());

        assertEquals(arbitraryTransactionV3.getService(), arbitraryTransactionV3_2.getService());
        assertEquals(arbitraryTransactionV3.getCreator(), arbitraryTransactionV3_2.getCreator());

        assertEquals(arbitraryTransactionV3.isSignatureValid(db), true);
        assertEquals(arbitraryTransactionV3_2.isSignatureValid(db), true);
    }

    @Test
    public void validateArbitraryTransactionV3withoutPayments() {

        init();

        AssetCls aTFundingAsset = new AssetVenture(itemAppData, gb.getCreator(), "ATFunding", icon, image, "This asset represents the funding of AT team for the integration of a Turing complete virtual machine into ERM.", 0, 8, 250000000l);
        aTFundingAsset.setReference(gb.getSignature(), dbRef);
        db.getItemAssetMap().set(61l, aTFundingAsset);

        //CREATE KNOWN ACCOUNT
        byte[] seed = Crypto.getInstance().digest("test".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();

        byte[] data = "test123!".getBytes();

        long timestamp = NTP.getTime();

        //PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS

        maker.changeBalance(db, false, false, 61L, BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), false, false, false);

        List<Payment> payments = new ArrayList<Payment>();

        ArbitraryTransactionV3 arbitraryTransactionV3 = new ArbitraryTransactionV3(
                maker, payments, 111,
                data,
                FEE_POWER,
                timestamp, maker.getLastTimestamp(db)[0]
        );
        arbitraryTransactionV3.sign(maker, Transaction.FOR_NETWORK);

        //if (NTP.getTime() < Transaction.getARBITRARY_TRANSACTIONS_RELEASE() || arbitraryTransactionV3.getTimestamp() < Transaction.getPOWFIX_RELEASE())
        if (false) {
            assertEquals(arbitraryTransactionV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.NOT_YET_RELEASED);
        } else {
            assertEquals(arbitraryTransactionV3.isValid(Transaction.FOR_NETWORK, txFlags), Transaction.VALIDATE_OK);
        }

        arbitraryTransactionV3.process(gb,Transaction.FOR_NETWORK);

        assertEquals(BigDecimal.valueOf(1).subtract(arbitraryTransactionV3.getFee()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(FEE_KEY, db));
        assertEquals(BigDecimal.valueOf(1000).setScale(BlockChain.AMOUNT_DEDAULT_SCALE), maker.getBalanceUSE(61l, db));


        byte[] rawArbitraryTransactionV3 = arbitraryTransactionV3.toBytes(Transaction.FOR_NETWORK, true);

        ArbitraryTransactionV3 arbitraryTransactionV3_2 = null;
        try {
            arbitraryTransactionV3_2 = (ArbitraryTransactionV3) ArbitraryTransactionV3.Parse(Arrays.copyOfRange(rawArbitraryTransactionV3, 4, rawArbitraryTransactionV3.length));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(new String(arbitraryTransactionV3.getData()), new String(arbitraryTransactionV3_2.getData()));

        assertEquals(arbitraryTransactionV3.getPayments().size(), arbitraryTransactionV3.getPayments().size());

        assertEquals(arbitraryTransactionV3.getService(), arbitraryTransactionV3_2.getService());
        assertEquals(arbitraryTransactionV3.getCreator(), arbitraryTransactionV3_2.getCreator());

        assertEquals(arbitraryTransactionV3.isSignatureValid(db), true);
        assertEquals(arbitraryTransactionV3_2.isSignatureValid(db), true);
    }

}
