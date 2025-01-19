package org.erachain.dapp.epoch;

import junit.framework.TestCase;
import org.erachain.core.transaction.Transaction;
import org.junit.Test;

public class OddEvenDAppTest extends TestCase {

    @Test
    public void testGetLastDigit() {
        Integer res = OddEvenDApp.sumLastDigits("CxMfAmPEL8xycmfGLy9WTCB7THvBHDqwm7wNGiGmzqLZjsruQYqBXtDbDe3RHWBS4AVJE5CjJJqVJziSo52NTLv", 3);
        assertEquals(res.intValue(), 2);

        assertEquals(OddEvenDApp.sumLastDigits("3RHWBS4AVJE5CjJJq0VJ0zoN0TLv", 3), null);
        assertEquals(OddEvenDApp.sumLastDigits("3RHWBS4AVJE5Cj5JJq9VJ7zoNTLv", 3), 1);

    }

    @Test
    public void testIsDisabled() {
        OddEvenDApp contract = new OddEvenDApp("1", "wait");
        assertEquals(contract.isDisabled(OddEvenDApp.DISABLED_BEFORE - 1), true);
        assertEquals(contract.isDisabled(OddEvenDApp.DISABLED_BEFORE + 1), false);
    }

    @Test
    public void testParse() {

        OddEvenDApp contract = new OddEvenDApp("1", "wait");
        byte[] data = contract.toBytes(Transaction.FOR_NETWORK);
        assertEquals(data.length, contract.length(Transaction.FOR_NETWORK));
        OddEvenDApp contractParse = OddEvenDApp.Parse(data, 0, Transaction.FOR_NETWORK);
        assertEquals(contractParse.getName(), contract.getName());

        data = contract.toBytes(Transaction.FOR_DB_RECORD);
        assertEquals(data.length, contract.length(Transaction.FOR_DB_RECORD));
        contractParse = OddEvenDApp.Parse(data, 0, Transaction.FOR_DB_RECORD);
        assertEquals(contractParse.getName(), contract.getName());
    }

}