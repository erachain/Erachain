package org.erachain.smartcontracts.epoch;

import org.erachain.core.transaction.Transaction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LeafFallTest {

    @Test
    public void toBytes() {
    }

    @Test
    public void parse() {
        LeafFall contract = new LeafFall();
        byte[] data = contract.toBytes(Transaction.FOR_NETWORK);
        assertEquals(data.length, contract.length(Transaction.FOR_NETWORK));
        LeafFall contractParse = LeafFall.Parse(data, 0, Transaction.FOR_NETWORK);
        assertEquals(contractParse.getCount(), contract.getCount());

        contract = new LeafFall(123);
        data = contract.toBytes(Transaction.FOR_DB_RECORD);
        assertEquals(data.length, contract.length(Transaction.FOR_DB_RECORD));
        contractParse = LeafFall.Parse(data, 0, Transaction.FOR_DB_RECORD);
        assertEquals(contractParse.getCount(), contract.getCount());
        assertEquals(contractParse.getKeyInit(), contract.getKeyInit());
        assertEquals(contractParse.getResultHash(), contract.getResultHash());
    }

    @Test
    public void process() {
    }

    @Test
    public void orphan() {
    }
}