package org.erachain.dbs.rocksDB.transformation;

public class toBytesStringLongInteger {

    public static byte[] toBytes(String address, Long key, Integer type) {
        return org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(address),
                key == null? new byte[8] : new ByteableLong().toBytesObject(key),
                type == null? new byte[4] : new ByteableInteger().toBytesObject(type)
        );
    }
}
