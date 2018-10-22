package org.erachain.core.account;

import org.erachain.core.crypto.Base32;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.utils.Pair;

import java.util.Arrays;

//import java.math.BigDecimal;
//import org.mapdb.Fun.Tuple2;
//import org.mapdb.Fun.Tuple3;

public class PublicKeyAccount extends Account {

    public static final int PUBLIC_KEY_LENGTH = Crypto.HASH_LENGTH;
    //public static final int STIRNG_PUBLIC_KEY_LENGTH = Base58.encode(new byte[PUBLIC_KEY_LENGTH]).length();

    protected byte[] publicKey;

    public PublicKeyAccount(byte[] publicKey) {
        super(Crypto.getInstance().getAddress(publicKey));
        this.publicKey = publicKey;
    }

    public PublicKeyAccount(String publicKey) {
        this(Base58.decode(publicKey));
    }

    protected PublicKeyAccount() {

    }

    //CHECK IF IS VALID PUBLIC KEY and MAKE NEW
    public static boolean isValidPublicKey(byte[] publicKey) {
        if (publicKey == null
                || publicKey.length != PUBLIC_KEY_LENGTH)
            return false;
        return true;
    }

    public static boolean isValidPublicKey(String publicKey) {

        byte[] pk = null;
        if (publicKey.startsWith("+")) {
            // BASE.32 from  BANK
            publicKey = publicKey.substring(1);
            try {
                pk = Base32.decode(publicKey);
            } catch (Exception e) {
                return false;
            }
            return isValidPublicKey(pk);
        } else {
            try {
                pk = Base58.decode(publicKey);
            } catch (Exception e) {
                return false;
            }
            return isValidPublicKey(pk);
        }
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    //EQUALS
    @Override
    public boolean equals(Object b) {
        if (b instanceof PublicKeyAccount) {
            return Arrays.equals(publicKey, ((PublicKeyAccount) b).getPublicKey());
        } else if (b instanceof byte[]) {
            byte[] bs = (byte[]) b;
            if (bs.length == 20) {
                return Arrays.equals(this.shortBytes, bs);
            } else if (bs.length == 25) {
                return Arrays.equals(this.bytes, bs);
            } else {
                return Arrays.equals(this.publicKey, bs);
            }
        } else if (b instanceof Account) {
            return this.address.equals(((Account) b).getAddress());
        } else if (b instanceof String) {
            return this.address.equals((String) b);
        }

        return false;
    }

    public String getBase58() {
        return Base58.encode(publicKey);
    }

    public String getBase32() {
        return Base32.encode(publicKey);
    }

    public boolean isValid() {
        return isValidPublicKey(this.publicKey);
    }


}
