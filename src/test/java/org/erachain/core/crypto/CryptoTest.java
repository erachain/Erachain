package org.erachain.core.crypto;

import org.junit.Test;

import java.security.Signature;

public class CryptoTest {

    @Test
    public void digest() {
    }

    @Test
    public void doubleDigest() {
    }

    @Test
    public void createKeyPair() {
    }

    @Test
    public void sign() {
        //for (CryptoPrimitive c : CryptoPrimitive.values())
        //    System.out.println(c);

        /// java.security.Security.addProvider(new net.i2p.crypto.eddsa.EdDSASecurityProvider());

        try {
            Signature provider = Signature.getInstance("Ed25519");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void verify() {
    }
}