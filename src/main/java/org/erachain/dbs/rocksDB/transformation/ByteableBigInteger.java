package org.erachain.rocksDB.transformation;

import java.math.BigInteger;
import java.util.Arrays;

public class ByteableBigInteger implements Byteable<BigInteger>{
    @Override
    public BigInteger receiveObjectFromBytes(byte[] bytes) {
        return new BigInteger(bytes);
    }

    @Override
    public byte[] toBytesObject(BigInteger value) {
        return value.toByteArray();
    }
}
