package org.erachain.dbs.rocksDB.transformation;

import com.google.common.primitives.Ints;

public class ByteableInteger implements Byteable<Integer> {
    @Override
    public Integer receiveObjectFromBytes(byte[] bytes) {
        return Ints.fromByteArray(bytes);
    }

    @Override
    public byte[] toBytesObject(Integer value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return Ints.toByteArray(value);
    }
}
