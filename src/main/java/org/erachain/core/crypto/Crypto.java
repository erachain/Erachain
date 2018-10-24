package org.erachain.core.crypto;

import com.google.common.primitives.Bytes;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.erachain.utils.Pair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Crypto {

    public static final int HASH_LENGTH = 32;
    public static final int SIGNATURE_LENGTH = 2 * HASH_LENGTH;

    public static final byte ADDRESS_VERSION = 15;
    public static final byte AT_ADDRESS_VERSION = 23;
    static Logger LOGGER = LoggerFactory.getLogger(Crypto.class.getName());
    private static Crypto instance;

    private Crypto() {

    }

    public static Crypto getInstance() {
        if (instance == null) {
            instance = new Crypto();
        }

        return instance;
    }

    public byte[] digest(byte[] input) {
        try {
            //SHA256
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(input);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public byte[] doubleDigest(byte[] input) {
        //DOUBLE SHA256
        return this.digest(this.digest(input));
    }

    public Pair<byte[], byte[]> createKeyPair(byte[] seed) {
        try {
            //GENERATE PUBLIC KEY
            return Ed25519.createKeyPair(seed);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public String getAddressFromShort(byte[] addressShort) {

        //CONVERT TO LIST
        List<Byte> addressList = new ArrayList<Byte>();

        //ADD VERSION BYTE
        Byte versionByte = Byte.valueOf(ADDRESS_VERSION);
        addressList.add(versionByte);

        addressList.addAll(Bytes.asList(addressShort));

        //GENERATE CHECKSUM
        byte[] checkSum = this.doubleDigest(Bytes.toArray(addressList));

        //ADD FIRST 4 BYTES OF CHECKSUM TO ADDRESS
        addressList.add(checkSum[0]);
        addressList.add(checkSum[1]);
        addressList.add(checkSum[2]);
        addressList.add(checkSum[3]);

        //BASE58 ENCODE ADDRESS
        String address = Base58.encode(Bytes.toArray(addressList));

        return address;
    }

    public String getAddress(byte[] publicKey) {
        //SHA256 PUBLICKEY FOR PROTECTION
        byte[] publicKeyHash = this.digest(publicKey);

        //RIPEMD160 TO CREATE A SHORTER ADDRESS
        RIPEMD160 ripEmd160 = new RIPEMD160();
        publicKeyHash = ripEmd160.digest(publicKeyHash);

        return this.getAddressFromShort(publicKeyHash);

    }

    public String getATAddress(byte[] signature) {
        //SHA256 SIGNATURE TO CONVERT IT TO 32BYTE
        byte[] publicKeyHash = this.digest(signature);

        //RIPEMD160 TO CREATE A SHORTER ADDRESS
        RIPEMD160 ripEmd160 = new RIPEMD160();
        publicKeyHash = ripEmd160.digest(publicKeyHash);

        //CONVERT TO LIST
        List<Byte> addressList = new ArrayList<Byte>();

        //ADD VERSION BYTE
        Byte versionByte = Byte.valueOf(AT_ADDRESS_VERSION);
        addressList.add(versionByte);

        addressList.addAll(Bytes.asList(publicKeyHash));

        //GENERATE CHECKSUM
        byte[] checkSum = this.doubleDigest(Bytes.toArray(addressList));

        //ADD FIRST 4 BYTES OF CHECKSUM TO ADDRESS
        addressList.add(checkSum[0]);
        addressList.add(checkSum[1]);
        addressList.add(checkSum[2]);
        addressList.add(checkSum[3]);

        //BASE58 ENCODE ADDRESS
        String address = Base58.encode(Bytes.toArray(addressList));

        return address;
    }

    public boolean isValidAddress(String address) {
        try {
            //BASE 58 DECODE
            byte[] addressBytes = Base58.decode(address);

            //CHECK BYTES
            if (addressBytes.length != Account.ADDRESS_LENGTH) {
                return false;
            }

            //CHECK VERSION
            if (addressBytes[0] == ADDRESS_VERSION) {
                //CONVERT TO LIST
                List<Byte> addressList = new ArrayList<Byte>();
                addressList.addAll(Bytes.asList(addressBytes));

                //REMOVE CHECKSUM
                byte[] checkSum = new byte[4];
                checkSum[3] = addressList.remove(addressList.size() - 1);
                checkSum[2] = addressList.remove(addressList.size() - 1);
                checkSum[1] = addressList.remove(addressList.size() - 1);
                checkSum[0] = addressList.remove(addressList.size() - 1);

                //GENERATE ADDRESS CHECKSUM
                byte[] digest = this.doubleDigest(Bytes.toArray(addressList));
                byte[] checkSumTwo = new byte[4];
                checkSumTwo[0] = digest[0];
                checkSumTwo[1] = digest[1];
                checkSumTwo[2] = digest[2];
                checkSumTwo[3] = digest[3];

                //CHECK IF CHECKSUMS ARE THE SAME
                return Arrays.equals(checkSum, checkSumTwo);
            } else if (addressBytes[0] == AT_ADDRESS_VERSION) {
                //TODO CHECK IF AT WITH THAT ID EXISTS
                //if (AT.getAT(addressBytes) != null)
                //{
                List<Byte> addressList = new ArrayList<Byte>();


                addressList.addAll(Bytes.asList(addressBytes));

                //REMOVE CHECKSUM
                byte[] checkSum = new byte[4];
                checkSum[3] = addressList.remove(addressList.size() - 1);
                checkSum[2] = addressList.remove(addressList.size() - 1);
                checkSum[1] = addressList.remove(addressList.size() - 1);
                checkSum[0] = addressList.remove(addressList.size() - 1);

                //GENERATE ADDRESS CHECKSUM
                byte[] digest = this.doubleDigest(Bytes.toArray(addressList));
                byte[] checkSumTwo = new byte[4];
                checkSumTwo[0] = digest[0];
                checkSumTwo[1] = digest[1];
                checkSumTwo[2] = digest[2];
                checkSumTwo[3] = digest[3];


                return Arrays.equals(checkSum, checkSumTwo);
                //}
            }

            return false;
        } catch (Exception e) {
            //ERROR DECODING
            return false;
        }
    }

    public byte[] sign(PrivateKeyAccount account, byte[] message) {
        try {
            //GET SIGNATURE
            return Ed25519.sign(account.getKeyPair(), message);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new byte[64];
        }
    }

    public boolean verify(byte[] publicKey, byte[] signature, byte[] message) {
        try {
            //VERIFY SIGNATURE
            return Ed25519.verify(signature, message, publicKey);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

}
