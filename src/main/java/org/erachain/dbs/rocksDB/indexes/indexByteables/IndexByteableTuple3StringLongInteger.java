package org.erachain.dbs.rocksDB.indexes.indexByteables;

import org.erachain.dbs.rocksDB.indexes.IndexByteable;
import org.erachain.dbs.rocksDB.transformation.ByteableInteger;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;
import org.mapdb.Fun;

public class IndexByteableTuple3StringLongInteger implements IndexByteable<Fun.Tuple3<String, Long, Integer>, Long> {
    @Override
    public byte[] toBytes(Fun.Tuple3<String, Long, Integer> result) {
        return org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(result.a),
                new ByteableLong().toBytesObject(result.b),
                new ByteableInteger().toBytesObject(result.c)
        );
    }
}
