package org.erachain.core.account;

import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrivateKeyAccountTest {

    @Test
    public void getSeed() {
        String privKyeMobi = "5BzAsmeoXwNzzWdeR253vxLXv7qqutHg952CbFsj1iqaBf3AeswgE9JjzEu78ddi516imb1FaR78gbf54812i7Fe";
        byte[] privKyeMobiByte = Base58.decode(privKyeMobi);
        PrivateKeyAccount privAccount = new PrivateKeyAccount(privKyeMobiByte);
        byte[] signature = Crypto.getInstance().sign(privAccount, privKyeMobiByte);
        assertEquals(true, Crypto.getInstance().verify(privAccount.getPublicKey(), signature, privKyeMobiByte));


    }

    @Test
    public void getPrivateKey() {
    }

    @Test
    public void getKeyPair() {
    }
}