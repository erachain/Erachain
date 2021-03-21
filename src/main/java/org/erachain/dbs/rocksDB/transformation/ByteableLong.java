package org.erachain.dbs.rocksDB.transformation;

import com.google.common.primitives.Longs;

public class ByteableLong implements Byteable<Long> {

    @Override
    public Long receiveObjectFromBytes(byte[] bytes) {
        return Longs.fromByteArray(bytes);
    }

    @Override
    public byte[] toBytesObject(Long value) {
        if (value == null) {
            return null; // new byte[0];
        }
        return Longs.toByteArray(value);
    }
}
