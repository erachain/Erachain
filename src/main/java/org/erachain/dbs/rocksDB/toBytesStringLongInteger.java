package org.erachain.dbs.rocksDB;

import org.erachain.dbs.rocksDB.indexes.IndexByteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.mapdb.Fun;

public class toBytesStringLongInteger {

    public static byte[] toBytes(String address, Long key, Integer type) {
        return org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(address),
                key == null? new byte[8] : new ByteableLong().toBytesObject(key),
                type == null? new byte[4] : new ByteableInteger().toBytesObject(type)
        );
    }
}
