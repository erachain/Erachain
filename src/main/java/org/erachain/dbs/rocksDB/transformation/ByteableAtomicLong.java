package org.erachain.dbs.rocksDB.transformation;

import java.util.concurrent.atomic.AtomicLong;

public class ByteableAtomicLong implements Byteable<AtomicLong> {
    ByteableLong byteableLong = new ByteableLong();
    @Override
    public AtomicLong receiveObjectFromBytes(byte[] bytes) {
        return new AtomicLong(byteableLong.receiveObjectFromBytes(bytes));
    }

    @Override
    public byte[] toBytesObject(AtomicLong value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        return byteableLong.toBytesObject(value.longValue());
    }
}
