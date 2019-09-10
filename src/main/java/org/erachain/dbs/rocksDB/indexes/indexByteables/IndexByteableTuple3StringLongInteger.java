package org.erachain.rocksDB.indexes.indexByteables;

import org.apache.flink.api.java.tuple.Tuple3;
import org.erachain.rocksDB.indexes.IndexByteable;
import org.erachain.rocksDB.transformation.ByteableInteger;
import org.erachain.rocksDB.transformation.ByteableLong;
import org.erachain.rocksDB.transformation.ByteableString;

public class IndexByteableTuple3StringLongInteger implements IndexByteable<Tuple3<String, Long, Integer>, Long> {
    @Override
    public byte[] toBytes(Tuple3<String, Long, Integer> result, Long key) {
        return org.bouncycastle.util.Arrays.concatenate(
                new ByteableString().toBytesObject(result.f0),
                new ByteableLong().toBytesObject(result.f1),
                new ByteableInteger().toBytesObject(result.f2)
        );
    }
}
