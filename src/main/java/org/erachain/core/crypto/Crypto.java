package org.erachain.core.crypto;

import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Crypto {

    public static final int HASH_LENGTH = 32;
    public static final int SIGNATURE_LENGTH = 2 * HASH_LENGTH;

    public static final byte ADDRESS_VERSION = 15;
    public static final byte DAPP_ADDRESS_VERSION = 23;
    static Logger LOGGER = LoggerFactory.getLogger(Crypto.class.getName());
    private static Crypto instance;

    private SecureRandom random;

    private Crypto() {

        //RANDOM
        this.random = new SecureRandom();

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

    public byte[] digest512(byte[] input) {
        try {
            //SHA256
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            return sha512.digest(input);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public byte[] doubleDigest(byte[] input) {
        //DOUBLE SHA256
        return this.digest(this.digest(input));
    }

    public Pair<byte[], byte[]> createKeyPair(byte[] seed) {
        if (seed.length == HASH_LENGTH) {
            try {
                //GENERATE PUBLIC KEY
                return Ed25519.createKeyPair(seed);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return null;
            }
        } else {
            return Ed25519.createKeyPair(seed);

        }
    }

    public byte[] getAddressFromShort(byte type, byte[] addressShort) {

        byte[] addressBytes = new byte[Account.ADDRESS_LENGTH];

        //ADD VERSION BYTE
        addressBytes[0] = type;
        if (type == DAPP_ADDRESS_VERSION) {
            addressBytes[1] = 83;
            addressBytes[2] = 118;
            System.arraycopy(addressShort, 2, addressBytes, 3, addressShort.length - 2);
        } else {
            System.arraycopy(addressShort, 0, addressBytes, 1, addressShort.length);
        }


        //GENERATE CHECKSUM
        byte[] checkSum = this.doubleDigest(Arrays.copyOfRange(addressBytes, 0, 21));
        System.arraycopy(checkSum, 0, addressBytes, 21, 4);

        return addressBytes;
    }

    public byte[] getAddressFromShortBytes(byte[] addressShort) {
        return getAddressFromShort(ADDRESS_VERSION, addressShort);
    }

    public byte[] getShortBytesFromAddress(String address) {
        byte[] bytes = Base58.decode(address);
        return Arrays.copyOfRange(bytes, 1, bytes.length - 4);
    }

    public String getAddressFromShort(byte[] addressShort) {
        return Base58.encode(getAddressFromShort(ADDRESS_VERSION, addressShort));
    }

    public byte[] getAddressBytes(byte[] publicKey) {
        return getAddressBytes(ADDRESS_VERSION, publicKey);

    }

    public byte[] getDAppAddress(byte[] signature) {
        return getAddressBytes(DAPP_ADDRESS_VERSION, signature);

    }

    public byte[] getAddressBytes(byte type, byte[] publicKeyOrSignarure) {

        if (type == DAPP_ADDRESS_VERSION && publicKeyOrSignarure.length >= HASH_LENGTH) {
            publicKeyOrSignarure[4] = (publicKeyOrSignarure[6] = (publicKeyOrSignarure[8] = (publicKeyOrSignarure[10] =
                    (publicKeyOrSignarure[12] = (publicKeyOrSignarure[14] =
                            (publicKeyOrSignarure[16] = (publicKeyOrSignarure[18] = (publicKeyOrSignarure[20] =
                                    (publicKeyOrSignarure[22] = (publicKeyOrSignarure[24] =
                                            (publicKeyOrSignarure[26] = (publicKeyOrSignarure[28] = 0))))))))))));
        } else if (type == ADDRESS_VERSION && publicKeyOrSignarure.length >= HASH_LENGTH
                && publicKeyOrSignarure[4] == 0 && publicKeyOrSignarure[6] == 0 && publicKeyOrSignarure[8] == 0
                && publicKeyOrSignarure[10] == 0 && publicKeyOrSignarure[12] == 0 && publicKeyOrSignarure[14] == 0
                && publicKeyOrSignarure[16] == 0 && publicKeyOrSignarure[18] == 0 && publicKeyOrSignarure[20] == 0
                && publicKeyOrSignarure[22] == 0 && publicKeyOrSignarure[24] == 0
                && publicKeyOrSignarure[26] == 0 && publicKeyOrSignarure[28] == 0) {
            type = DAPP_ADDRESS_VERSION;
        }

        //TO CONVERT IT TO 32BYTE
        byte[] publicKeyHash = this.digest(publicKeyOrSignarure);

        //RIPEMD160 TO CREATE A SHORTER ADDRESS
        RIPEMD160 ripEmd160 = new RIPEMD160();
        publicKeyHash = ripEmd160.digest(publicKeyHash);


        return getAddressFromShort(type, publicKeyHash);
    }

    public String getDAppAddressB58(byte[] signature) {
        return Base58.encode(getDAppAddress(signature));
    }

    public boolean isValidAddress(byte[] addressBytes) {

        //CHECK BYTES
        if (addressBytes.length != Account.ADDRESS_LENGTH) {
            return false;
        }

        //CHECK VERSION
        if (addressBytes[0] == ADDRESS_VERSION
                || addressBytes[0] == DAPP_ADDRESS_VERSION) {

            //REMOVE CHECKSUM
            byte[] checkSum = new byte[4];
            System.arraycopy(addressBytes, Account.ADDRESS_LENGTH - 4, checkSum, 0, 4);

            //GENERATE ADDRESS CHECKSUM
            byte[] shortBytes = new byte[Account.ADDRESS_LENGTH - 4];
            System.arraycopy(addressBytes, 0, shortBytes, 0, Account.ADDRESS_LENGTH - 4);
            byte[] digest = this.doubleDigest(shortBytes); // Arrays.copyOfRange(addressBytes, 0, 21));
            byte[] checkSumTwo = new byte[4];
            System.arraycopy(digest, 0, checkSumTwo, 0, 4);

            //CHECK IF CHECKSUMS ARE THE SAME
            return Arrays.equals(checkSum, checkSumTwo);
        }

        return false;
    }

    public boolean isValidAddress(String address) {

        byte[] addressBytes;

        if (address == null || address.isEmpty() || address.length() < 30) {
            return false;
        }

        try {
            //BASE 58 DECODE
            addressBytes = Base58.decode(address);
        } catch (Exception e) {
            //ERROR DECODING
            return false;
        }

        return isValidAddress(addressBytes);
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

    // make SEEd with length without empty symbols in Base58
    public byte[] createSeed(int length) {
        byte[] seed = new byte[length];
        // нужно быть уверенным что длинна при обратном преобразовании даст 32 байта
        do {
            this.random.nextBytes(seed);
        } while (Base58.decode(Base58.encode(seed)).length != length);
        return seed;

    }

}
