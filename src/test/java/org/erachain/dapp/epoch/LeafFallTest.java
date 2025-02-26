package org.erachain.dapp.epoch;

import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.Transaction;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class LeafFallTest {

    byte[] seed = Crypto.getInstance().digest("test_A".getBytes());
    byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
    PrivateKeyAccount accountA = new PrivateKeyAccount(privateKey);
    long timestamp = System.currentTimeMillis();

    @Test
    public void toBytes() {
    }

    @Test
    public void parse() {
        LeafFall dApp = new LeafFall();
        byte[] data = dApp.toBytes(Transaction.FOR_NETWORK);
        assertEquals(data.length, dApp.length(Transaction.FOR_NETWORK));
        LeafFall dAppParse = LeafFall.Parse(data, 0, Transaction.FOR_NETWORK);
        assertEquals(dAppParse.getCount(), dApp.getCount());

        dApp = new LeafFall(123);
        data = dApp.toBytes(Transaction.FOR_DB_RECORD);
        assertEquals(data.length, dApp.length(Transaction.FOR_DB_RECORD));
        dAppParse = LeafFall.Parse(data, 0, Transaction.FOR_DB_RECORD);
        assertEquals(dAppParse.getCount(), dApp.getCount());
        assertEquals(dAppParse.getKeyInit(), dApp.getKeyInit());
        assertEquals(dAppParse.getResultHash(), dApp.getResultHash());
    }

    @Test
    public void parseOrder() throws Exception {
        LeafFall dApp = new LeafFall();
        byte[] data = dApp.toBytes(Transaction.FOR_NETWORK);
        assertEquals(data.length, dApp.length(Transaction.FOR_NETWORK));
        LeafFall dAppParse = LeafFall.Parse(data, 0, Transaction.FOR_NETWORK);
        assertEquals(dAppParse.getCount(), dApp.getCount());

        CreateOrderTransaction orderCreation = new CreateOrderTransaction(accountA, 1L, 2L, BigDecimal.ONE,
                BigDecimal.TEN, (byte) 0, timestamp++, 0L);
        orderCreation.sign(accountA, Transaction.FOR_NETWORK);
        orderCreation.setDApp(dApp);
        data = orderCreation.toBytes(Transaction.FOR_NETWORK, true);
        CreateOrderTransaction orderCreationParse = (CreateOrderTransaction) CreateOrderTransaction.Parse(data, Transaction.FOR_NETWORK);
        assertEquals(orderCreation.getAmountWant(), orderCreationParse.getAmountWant());
        assertEquals(orderCreation.getWantKey(), orderCreationParse.getWantKey());
        orderCreation.resetNotOwnedDApp();

        dApp = new LeafFall(123);
        data = dApp.toBytes(Transaction.FOR_DB_RECORD);
        assertEquals(data.length, dApp.length(Transaction.FOR_DB_RECORD));
        dAppParse = LeafFall.Parse(data, 0, Transaction.FOR_DB_RECORD);
        assertEquals(dAppParse.getCount(), dApp.getCount());
        assertEquals(dAppParse.getKeyInit(), dApp.getKeyInit());
        assertEquals(dAppParse.getResultHash(), dApp.getResultHash());

        orderCreation.setDApp(dApp);
        data = orderCreation.toBytes(Transaction.FOR_DB_RECORD, true);
        orderCreationParse = (CreateOrderTransaction) CreateOrderTransaction.Parse(data, Transaction.FOR_DB_RECORD);
        assertEquals(orderCreation.getWantKey(), orderCreationParse.getWantKey());
        assertEquals(orderCreation.getAmountWant(), orderCreationParse.getAmountWant());
        orderCreation.resetNotOwnedDApp();

    }

    @Test
    public void process() {
    }

    @Test
    public void orphan() {
    }
}