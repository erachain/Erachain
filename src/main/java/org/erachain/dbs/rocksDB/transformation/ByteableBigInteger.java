package org.erachain.dbs.rocksDB.transformation;

import java.math.BigInteger;

public class ByteableBigInteger implements Byteable<BigInteger>{
    @Override
    public BigInteger receiveObjectFromBytes(byte[] bytes) {
        return new BigInteger(bytes);
    }

    @Override
    public byte[] toBytesObject(BigInteger value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return value.toByteArray();
    }
}
