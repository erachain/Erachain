package org.erachain.core.epoch;

import org.erachain.core.transaction.Transaction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DogePlanetTest {

    @Test
    public void length() {
    }

    @Test
    public void toBytes() {
        DogePlanet contract = new DogePlanet(3);
        byte[] data = contract.toBytes(Transaction.FOR_NETWORK);
        assertEquals(data.length, contract.length(Transaction.FOR_NETWORK));

        data = contract.toBytes(Transaction.FOR_DB_RECORD);
        assertEquals(data.length, contract.length(Transaction.FOR_DB_RECORD));

    }

    @Test
    public void parse() {
    }

    @Test
    public void process() {
    }

    @Test
    public void orphan() {
    }
}