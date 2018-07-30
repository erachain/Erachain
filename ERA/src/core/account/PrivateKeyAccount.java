package core.account;

import core.crypto.Base58;
import core.crypto.Crypto;
import utils.Pair;

import java.util.Arrays;

public class PrivateKeyAccount extends PublicKeyAccount {

    private byte[] seed;

    public PrivateKeyAccount(byte[] seed) {
        super(Crypto.getInstance().createKeyPair(seed));
        this.seed = seed;
        //this.keyPair = ;
        //this.publicKey = keyPair.getB();
        //this.address = Crypto.getInstance().getAddress(this.publicKey);
        //this.bytes = Base58.decode(address);
        //this.shortBytes = Arrays.copyOfRange(this.bytes, 5, this.bytes.length);
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
