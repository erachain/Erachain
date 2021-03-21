package org.erachain.dbs.rocksDB.transformation;

import com.google.common.primitives.Longs;

public class ByteableLongArray implements Byteable<long[]> {

    @Override
    public long[] receiveObjectFromBytes(byte[] bytes) {
        int length = bytes.length >> 3;
        long[] result = new long[length];
        for (int i = 0; i < length; i++) {
            byte[] buff = new byte[8];
            System.arraycopy(bytes, i * 8, buff, 0, 8);
            result[i] = Longs.fromByteArray(buff);
        }
        return result;
    }

    @Override
    public byte[] toBytesObject(long[] value) {
        if (value == null)
            return null; // need for Filter KEYS = null

        byte[] result = new byte[value.length << 3];
        for (int i = 0; i < value.length; i++) {
            System.arraycopy(Longs.toByteArray(value[i]), 0, result, i * 8, 8);
        }
        return result;
    }
}
