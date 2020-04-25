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

    static boolean isPalindrome(String word) {
        int len = word.length();

        String wordLower = word.toLowerCase();
        for (int i = 0; i < len >> 1; i++) {
            if (wordLower.charAt(i) != wordLower.charAt(len - i - 1)) {
                return false;
            }
        }

        return true;
    }

    @Test
    public void palinDrome() {
        String[] ps = new String[5];
        ps[0] = "Develeved";
        ps[1] = "size";
        ps[2] = "put";
        ps[3] = "tt";
        ps[4] = "Deveeved";

        for (String word : ps) {
            System.out.println(word + ": " + isPalindrome(word));
        }
    }


}