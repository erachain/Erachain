package org.erachain.dbs.rocksDB.transformation.tuples;

import org.erachain.dbs.rocksDB.transformation.Byteable;
import org.erachain.dbs.rocksDB.transformation.ByteableLong;
import org.erachain.dbs.rocksDB.transformation.ByteableString;

public class ByteableTuple2LongString extends ByteableTuple2<Long,String>{

    public ByteableTuple2LongString() {
        setByteables(new Byteable[]{new ByteableLong(),new ByteableString()});
    }

    @Override
    public int sizeElements() {
        return Long.BYTES;
    }
}
