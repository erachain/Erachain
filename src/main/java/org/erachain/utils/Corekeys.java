package org.erachain.utils;

import com.google.common.primitives.Bytes;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.crypto.RIPEMD160;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public enum Corekeys {

    DEFAULT("defaultkey", KeyVariation.DEFAULTKEY), WEBSITE("website", KeyVariation.DEFAULTKEY), BLOGWHITELIST("blogwhitelist", KeyVariation.LISTKEY), BLOGBLACKLIST(
            "blogblacklist", KeyVariation.LISTKEY), BLOGDESCRIPTION("blogdescription", KeyVariation.DEFAULTKEY), BLOGTITLE("blogtitle", KeyVariation.DEFAULTKEY), BLOGENABLE(
            "blogenable", KeyVariation.EXISTSKEY), PROFILEENABLE("profileenable", KeyVariation.EXISTSKEY), PROFILEAVATAR("profileavatar", KeyVariation.DEFAULTKEY), PROFILEFOLLOW(
            "profilefollow", KeyVariation.LISTKEY), PROFILEMAINGRAPHIC("profilemaingraphic", KeyVariation.DEFAULTKEY), PROFILELIKEPOSTS("profilelikeposts", KeyVariation.LISTKEY), BLOGBLOCKCOMMENTS("blogblockcomments", KeyVariation.EXISTSKEY);
    private final String keyname;
    private KeyVariation variation;

    private Corekeys(String keyname, KeyVariation variation) {
        this.keyname = keyname;
        this.variation = variation;
    }

    public static boolean isPartOf(String enumString) {
        Corekeys[] values = Corekeys.values();
        for (Corekeys corekey : values) {
            if (enumString.equals(corekey.toString())) {
                return true;
            }
        }

        return false;
    }

    // seek first char in ADDR
    public static int findAddressVersion(String first_char) {
        byte[] seed = Crypto.getInstance().digest("7qqBNd4qPWPAJBw9iPmEnNRiRmzvrF6hWUd4ZrjL3TQu".getBytes());
        byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
        PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);

        //SHA256 PUBLICKEY FOR PROTECTION
        MessageDigest sha256;
        byte[] publicKeyHash;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
            publicKeyHash = sha256.digest(maker.getPublicKey());
        } catch (NoSuchAlgorithmException e) {
            return -1111;
        }

        //RIPEMD160 TO CREATE A SHORTER ADDRESS
        RIPEMD160 ripEmd160 = new RIPEMD160();
        publicKeyHash = ripEmd160.digest(publicKeyHash);

        //CONVERT TO LIST
        List<Byte> addressList = new ArrayList<Byte>();

        for (byte i = 0; i <= 60; i++) {
            //ADD VERSION BYTE
            Byte versionByte = Byte.valueOf(i); //ADDRESS_VERSION);
            addressList.add(versionByte);

            addressList.addAll(Bytes.asList(publicKeyHash));

            //GENERATE CHECKSUM
            byte[] checkSum = sha256.digest(sha256.digest(Bytes.toArray(addressList)));

            //ADD FIRST 4 BYTES OF CHECKSUM TO ADDRESS
            addressList.add(checkSum[0]);
            addressList.add(checkSum[1]);
            addressList.add(checkSum[2]);
            addressList.add(checkSum[3]);

            //BASE58 ENCODE ADDRESS
            String address = Base58.encode(Bytes.toArray(addressList));
            if (address.startsWith(first_char)) return i;
        }

        return -1111;
    }

    public String getKeyname() {
        return keyname;
    }

    public String toString() {
        return keyname;
    }

    public KeyVariation getVariation() {
        return variation;
    }

}
