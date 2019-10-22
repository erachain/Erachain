package org.erachain.dbs.rocksDB.transformation.tuples;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;

public class ByteableTuple2LongLong extends ByteableTuple2<Long,Long>{

    public ByteableTuple2LongLong() {
        setByteables(new Byteable[]{new ByteableLong(), new ByteableLong()});
    }

    @Override
    public int sizeElements() {
        return Long.BYTES;
    }
}
