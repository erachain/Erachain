package org.erachain.core.account;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PublicKeyAccountTest {

    @Test
    public void equals() {

        PublicKeyAccount pub1 = new PublicKeyAccount("2LZUcwRA3N3j3SZ5paDXrfWZoSG6wWEdo3aGmjfmAhSz");
        PublicKeyAccount pub2 = new PublicKeyAccount("7Hhe41mfCwRA9oiVzqYLWim6br7qvzZY5CSwRi8jjiWF");

        PublicKeyAccount pub1a = new PublicKeyAccount("2LZUcwRA3N3j3SZ5paDXrfWZoSG6wWEdo3aGmjfmAhSz");

        assertEquals(pub1.equals(pub1a), true);
        assertEquals(pub1.equals(pub2), false);

        boolean equals = pub1.equals(pub1a);
        int hash1 = pub1.hashCode();
        int hash1a = pub1a.hashCode();
        assertEquals(hash1, hash1a);

        int hash2 = pub2.hashCode();
        assertNotEquals(hash1, hash2);

        Account acc1 = new Account(pub1.getAddress());
        Account acc2 = new Account(pub2.getAddress());

        Account acc1a = new Account(pub1.getAddress());


        assertEquals(acc1.equals(acc1a), true);
        assertEquals(acc1.equals(acc2), false);

        assertEquals(acc1, acc1a);
        assertNotEquals(acc1, acc2);

        assertEquals(acc1.hashCode(), acc1a.hashCode());

        assertNotEquals(acc1.hashCode(), acc2.hashCode());


    }

    @Test
    public void isValid() {
    }
}