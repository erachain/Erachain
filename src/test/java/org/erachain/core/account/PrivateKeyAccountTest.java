package org.erachain.core.account;

import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.junit.Test;

import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;

public class PrivateKeyAccountTest {

    @Test
    public void mobilePrivateKey() {
        String privKyeMobi = "5BzAsmeoXwNzzWdeR253vxLXv7qqutHg952CbFsj1iqaBf3AeswgE9JjzEu78ddi516imb1FaR78gbf54812i7Fe";
        byte[] privKyeMobiByte = Base58.decode(privKyeMobi, Crypto.SIGNATURE_LENGTH);
        PrivateKeyAccount privAccount = new PrivateKeyAccount(privKyeMobiByte);
        byte[] signature = Crypto.getInstance().sign(privAccount, privKyeMobiByte);
        assertEquals(true, Crypto.getInstance().verify(privAccount.getPublicKey(), signature, privKyeMobiByte));

    }

    @Test
    public void nodePrivateKey() {
        SecureRandom random = new SecureRandom();
        byte[] seed = new byte[32];
        random.nextBytes(seed);

        PrivateKeyAccount privAccount = new PrivateKeyAccount(seed);
        byte[] signature = Crypto.getInstance().sign(privAccount, seed);
        assertEquals(true, Crypto.getInstance().verify(privAccount.getPublicKey(), signature, seed));

    }

    @Test
    public void getPrivateKey() {
    }

    @Test
    public void getKeyPair() {
    }
}