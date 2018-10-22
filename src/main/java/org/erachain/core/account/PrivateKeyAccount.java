package org.erachain.core.account;

import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.utils.Pair;

import java.util.Arrays;

public class PrivateKeyAccount extends PublicKeyAccount {

    private byte[] seed;
    private Pair<byte[], byte[]> keyPair;

    public PrivateKeyAccount(byte[] seed) {
        super(Crypto.getInstance().createKeyPair(seed).getB());
        this.seed = seed;
        this.keyPair = Crypto.getInstance().createKeyPair(seed);
    }

    public byte[] getSeed() {
        return this.seed;
    }

    public byte[] getPrivateKey() {
        return this.keyPair.getA();
    }

    public Pair<byte[], byte[]> getKeyPair() {
        return this.keyPair;
    }

}
