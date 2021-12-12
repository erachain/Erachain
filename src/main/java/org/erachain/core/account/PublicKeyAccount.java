package org.erachain.core.account;

import org.erachain.core.crypto.Base32;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.json.simple.JSONObject;

public class PublicKeyAccount extends Account {

    public static final int PUBLIC_KEY_LENGTH = Crypto.HASH_LENGTH;

    protected byte[] publicKey;

    public PublicKeyAccount(byte[] publicKey) {
        super(Crypto.getInstance().getAddressBytes(publicKey));
        this.publicKey = publicKey;
    }

    /**
     * for make DAPP publick key
     *
     * @param type
     * @param publicKey
     */
    public PublicKeyAccount(byte type, byte[] publicKey) {
        super(Crypto.getInstance().getAddressBytes(type, publicKey));
        this.publicKey = publicKey;
    }

    public PublicKeyAccount(String publicKey) {
        this(Base58.decode(publicKey, PUBLIC_KEY_LENGTH));
    }

    public static PublicKeyAccount makeForDApp(byte[] publicKey) {
        return new PublicKeyAccount(Crypto.AT_ADDRESS_VERSION, publicKey);
    }

    //CHECK IF IS VALID PUBLIC KEY and MAKE NEW
    public static boolean isValidPublicKey(byte[] publicKey) {
        if (publicKey == null
                || publicKey.length != PUBLIC_KEY_LENGTH)
            return false;
        return true;
    }

    public static boolean validLen(String publicKey) {
        return publicKey.length() > PublicKeyAccount.PUBLIC_KEY_LENGTH + (PublicKeyAccount.PUBLIC_KEY_LENGTH >> 3);
    }

    public static boolean isValidPublicKey(String publicKey) {

        byte[] pk = null;
        if (publicKey.startsWith("+")) {
            // BASE.32 from  BANK
            publicKey = publicKey.substring(1);
            if (validLen(publicKey)) {
                try {
                    pk = Base32.decode(publicKey, PUBLIC_KEY_LENGTH);
                } catch (Exception e) {
                    return false;
                }
            } else
                return false;
        } else {
            try {
                // Base58 string len = 33-34 for ADDRESS and 40-44 for PubKey
                if (validLen(publicKey)) {
                    pk = Base58.decode(publicKey, PUBLIC_KEY_LENGTH);
                } else
                    return false;
            } catch (Exception e) {
                return false;
            }
        }
        return isValidPublicKey(pk);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public JSONObject toJson() {
        JSONObject json = super.toJson();
        json.put("pubkey", getBase58());
        return json;
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
