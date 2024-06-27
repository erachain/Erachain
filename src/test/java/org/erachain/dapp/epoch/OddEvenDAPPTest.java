package org.erachain.dapp.epoch;

import junit.framework.TestCase;
import org.erachain.core.transaction.Transaction;
import org.junit.Test;

public class OddEvenDAPPTest extends TestCase {

    @Test
    public void testGetLastDigit() {
        Integer res = OddEvenDAPP.sumLastDigits("CxMfAmPEL8xycmfGLy9WTCB7THvBHDqwm7wNGiGmzqLZjsruQYqBXtDbDe3RHWBS4AVJE5CjJJqVJziSo52NTLv", 3);
        assertEquals(res.intValue(), 2);

        assertEquals(OddEvenDAPP.sumLastDigits("3RHWBS4AVJE5CjJJq0VJ0zoN0TLv", 3), null);
        assertEquals(OddEvenDAPP.sumLastDigits("3RHWBS4AVJE5Cj5JJq9VJ7zoNTLv", 3), 1);

    }

    @Test
    public void testParse() {

        OddEvenDAPP contract = new OddEvenDAPP("1", "wait");
        byte[] data = contract.toBytes(Transaction.FOR_NETWORK);
        assertEquals(data.length, contract.length(Transaction.FOR_NETWORK));
        OddEvenDAPP contractParse = OddEvenDAPP.Parse(data, 0, Transaction.FOR_NETWORK);
        assertEquals(contractParse.getName(), contract.getName());

        data = contract.toBytes(Transaction.FOR_DB_RECORD);
        assertEquals(data.length, contract.length(Transaction.FOR_DB_RECORD));
        contractParse = OddEvenDAPP.Parse(data, 0, Transaction.FOR_DB_RECORD);
        assertEquals(contractParse.getName(), contract.getName());
    }

}